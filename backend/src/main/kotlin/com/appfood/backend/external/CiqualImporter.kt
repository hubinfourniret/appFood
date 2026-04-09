package com.appfood.backend.external

import com.appfood.backend.database.dao.AlimentDao
import com.appfood.backend.database.dao.AlimentRow
import com.appfood.backend.database.tables.RegimeAlimentaire
import com.appfood.backend.database.tables.SourceAliment
import com.appfood.backend.search.AlimentIndexer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.util.UUID

/**
 * Import CSV de la base Ciqual (ANSES) vers le schema appFood.
 *
 * Parse le fichier CSV, mappe les colonnes Ciqual vers AlimentRow,
 * gere les valeurs speciales ("-", "traces", "N/A", "<") et detecte
 * les regimes compatibles par heuristique de categorie.
 *
 * Complete pipeline: parse CSV -> upsert PostgreSQL -> index Meilisearch.
 * Idempotent: uses INSERT ON CONFLICT (source, source_id) DO UPDATE.
 */
class CiqualImporter(
    private val alimentDao: AlimentDao,
    private val alimentIndexer: AlimentIndexer,
) {
    private val logger = LoggerFactory.getLogger(CiqualImporter::class.java)

    companion object {
        private const val CSV_SEPARATOR = ','
        private const val DECIMAL_COMMA = ','
        private const val DECIMAL_DOT = '.'
        private const val LOW_QUALITY_THRESHOLD = 8
        private const val UPSERT_BATCH_SIZE = 500
        private const val MEILISEARCH_BATCH_SIZE = 100

        // -- Column patterns for nutriment mapping (Ciqual headers vary between versions) --

        // Identifier columns
        private val PATTERN_SOURCE_ID = listOf("alim_code")
        private val PATTERN_NOM = listOf("alim_nom_fr")
        private val PATTERN_CATEGORIE_SUB = listOf("alim_ssgrp_nom_fr")
        private val PATTERN_CATEGORIE_GROUP = listOf("alim_grp_nom_fr")

        // Nutriment column detection: each pair is (required_pattern_1, required_pattern_2)
        private val NUTRIMENT_PATTERNS: Map<String, List<Pair<String, String>>> =
            mapOf(
                "calories" to listOf("Énergie, Règlement UE" to "(kcal", "Energie, Règlement UE" to "(kcal"),
                "proteines" to listOf("Protéines" to "(g", "Proteines" to "(g"),
                "glucides" to listOf("Glucides" to "(g"),
                "lipides" to listOf("Lipides" to "(g"),
                "fibres" to listOf("Fibres" to "(g"),
                "sel" to listOf("Sel chlorure de sodium" to "(g"),
                "sucres" to listOf("Sucres" to "(g"),
                "fer" to listOf("Fer" to "(mg"),
                "calcium" to listOf("Calcium" to "(mg"),
                "zinc" to listOf("Zinc" to "(mg"),
                "magnesium" to listOf("Magnésium" to "(mg", "Magnesium" to "(mg"),
                "vitamineB12" to listOf("Vitamine B12" to "(µg", "Vitamine B12" to "(ug"),
                "vitamineD" to listOf("Vitamine D" to "(µg", "Vitamine D" to "(ug"),
                "vitamineC" to listOf("Vitamine C" to "(mg"),
            )

        // Omega-3 fatty acid column patterns (ALA, EPA, DHA)
        private val OMEGA3_PATTERNS =
            listOf(
                "AG 18:3 c9,c12,c15 (n-3)", // ALA
                "AG 20:5 5c,8c,11c,14c,17c (n-3)", // EPA
                "AG 22:6 4c,7c,10c,13c,16c,19c (n-3)", // DHA
            )

        // Omega-6 fatty acid column pattern (linoleic acid)
        private val OMEGA6_PATTERNS =
            listOf(
                "AG 18:2 9c,12c (n-6)", // LA
            )

        // -- Diet heuristic keywords (lowercased, without accents) --

        private val MEAT_FISH_KEYWORDS =
            listOf(
                "viande", "boeuf", "veau", "porc", "agneau", "mouton", "cheval",
                "volaille", "poulet", "dinde", "canard", "lapin",
                "poisson", "saumon", "thon", "sardine", "cabillaud", "merlu",
                "crustace", "crevette", "homard", "crabe",
                "mollusque", "moule", "huitre", "calamar",
                "charcuterie", "jambon", "saucisse", "pate", "boudin",
                "abats", "foie", "rognon",
                "gibier", "chevreuil", "sanglier",
            )

        private val DAIRY_EGG_KEYWORDS =
            listOf(
                "lait",
                "fromage",
                "yaourt",
                "beurre",
                "creme",
                "oeuf",
                "egg",
                "miel",
            )
    }

    /**
     * Result of parsing the Ciqual CSV.
     *
     * @param rows Parsed aliment rows ready for DB insertion
     * @param totalLines Total data lines read from CSV
     * @param skippedLines Number of lines skipped (parse errors)
     * @param lowQualityCount Number of aliments with >8 zero nutriments
     */
    data class ImportResult(
        val rows: List<AlimentRow>,
        val totalLines: Int,
        val skippedLines: Int,
        val lowQualityCount: Int,
    )

    /**
     * Full import pipeline: parse CSV, upsert to PostgreSQL, index in Meilisearch.
     * Idempotent — can be re-run safely without duplicating data.
     *
     * @param inputStream The CSV file input stream (UTF-8 or UTF-8 BOM)
     * @return ImportResult with parsed AlimentRow list and statistics
     */
    suspend fun importAndIndex(inputStream: InputStream): ImportResult {
        val result = parse(inputStream)

        if (result.rows.isEmpty()) {
            logger.warn("No aliments parsed from CSV — skipping DB insert and Meilisearch indexation")
            return result
        }

        // Upsert to PostgreSQL in batches (idempotent via source + source_id unique constraint)
        val batchSize = UPSERT_BATCH_SIZE
        val batches = result.rows.chunked(batchSize)
        var upsertedCount = 0

        for ((index, batch) in batches.withIndex()) {
            alimentDao.upsertBatch(batch)
            upsertedCount += batch.size
            logger.info("PostgreSQL upsert: batch ${index + 1}/${batches.size} ($upsertedCount / ${result.rows.size})")
        }

        logger.info("PostgreSQL import complete: $upsertedCount aliments upserted")

        // Index in Meilisearch in batches
        val indexBatches = result.rows.chunked(MEILISEARCH_BATCH_SIZE)
        var indexedCount = 0

        for ((index, batch) in indexBatches.withIndex()) {
            try {
                alimentIndexer.indexBatch(batch)
                indexedCount += batch.size
                logger.info(
                    "Meilisearch index: batch ${index + 1}/${indexBatches.size} ($indexedCount / ${result.rows.size})",
                )
                // Pause entre batches pour laisser Meilisearch indexer sans OOM
                if (index < indexBatches.size - 1) {
                    kotlinx.coroutines.delay(2000)
                }
            } catch (e: Exception) {
                logger.error(
                    "Meilisearch indexation failed at batch ${index + 1}: ${e.message}",
                    e,
                )
            }
        }

        logger.info("Meilisearch indexation complete: $indexedCount aliments indexed")

        return result
    }

    /**
     * Full import pipeline from a file path.
     */
    suspend fun importAndIndex(filePath: String): ImportResult {
        return java.io.File(filePath).inputStream().use { importAndIndex(it) }
    }

    /**
     * Parse a Ciqual CSV file from an InputStream.
     *
     * @param inputStream The CSV file input stream (UTF-8 or UTF-8 BOM)
     * @return ImportResult with parsed AlimentRow list and statistics
     */
    fun parse(inputStream: InputStream): ImportResult {
        val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

        // Read first complete CSV record (header may span multiple lines due to quoted newlines)
        val headerRecord =
            readCsvRecord(reader)?.removeBomPrefix()
                ?: throw IllegalArgumentException("Empty CSV file: no header line found")

        val headers = parseCsvLine(headerRecord)
        val columnMap = resolveColumnIndices(headers)
        logColumnMapping(columnMap, headers)

        val rows = mutableListOf<AlimentRow>()
        var recordNumber = 0
        var skippedLines = 0
        var lowQualityCount = 0

        var record = readCsvRecord(reader)
        while (record != null) {
            recordNumber++
            try {
                val fields = parseCsvLine(record)
                if (fields.size < headers.size) {
                    logger.warn("Record $recordNumber: expected ${headers.size} fields, got ${fields.size} — skipping")
                    skippedLines++
                    record = readCsvRecord(reader)
                    continue
                }

                val row = mapToAlimentRow(fields, columnMap, recordNumber)
                val zeroCount = countZeroNutriments(row)
                if (zeroCount > LOW_QUALITY_THRESHOLD) {
                    lowQualityCount++
                    logger.debug(
                        "Record $recordNumber: aliment '${row.nom}' has $zeroCount/16 zero nutriments (low quality)",
                    )
                }

                rows.add(row)
            } catch (e: Exception) {
                logger.warn("Record $recordNumber: parse error — ${e.message}")
                skippedLines++
            }
            record = readCsvRecord(reader)
        }

        logger.info(
            "Ciqual import: ${rows.size} aliments parsed, $skippedLines lines skipped, " +
                "$lowQualityCount low-quality aliments (from $recordNumber data records)",
        )

        return ImportResult(
            rows = rows,
            totalLines = recordNumber,
            skippedLines = skippedLines,
            lowQualityCount = lowQualityCount,
        )
    }

    /**
     * Parse a Ciqual CSV file from a file path.
     */
    fun parse(filePath: String): ImportResult {
        return java.io.File(filePath).inputStream().use { parse(it) }
    }

    // -- Column resolution --

    /**
     * Indices of all columns needed for mapping.
     */
    data class ColumnMap(
        val sourceId: Int,
        val nom: Int,
        val categorie: Int,
        val nutriments: Map<String, Int>,
        val omega3Indices: List<Int>,
        val omega6Indices: List<Int>,
    )

    private fun resolveColumnIndices(headers: List<String>): ColumnMap {
        val sourceIdIdx =
            findColumnIndex(headers, PATTERN_SOURCE_ID)
                ?: findFirstNumericColumnIndex(headers)
                ?: throw IllegalArgumentException("Cannot find alim_code column in CSV header")

        val nomIdx =
            findColumnIndex(headers, PATTERN_NOM)
                ?: throw IllegalArgumentException("Cannot find alim_nom_fr column in CSV header")

        // Prefer sous-groupe, fall back to groupe
        val categorieIdx =
            findColumnIndex(headers, PATTERN_CATEGORIE_SUB)
                ?: findColumnIndex(headers, PATTERN_CATEGORIE_GROUP)
                ?: throw IllegalArgumentException("Cannot find category column in CSV header")

        val nutrimentIndices = mutableMapOf<String, Int>()
        for ((nutriment, patterns) in NUTRIMENT_PATTERNS) {
            val idx = findNutrimentColumnIndex(headers, patterns)
            if (idx != null) {
                nutrimentIndices[nutriment] = idx
            } else {
                logger.warn("Column not found for nutriment '$nutriment' — will default to 0.0")
            }
        }

        val omega3Indices =
            OMEGA3_PATTERNS.mapNotNull { pattern ->
                headers.indexOfFirst { it.contains(pattern) }.takeIf { it >= 0 }
            }
        if (omega3Indices.isEmpty()) {
            logger.warn("No omega-3 fatty acid columns found — omega3 will be 0.0 for all aliments")
        }

        val omega6Indices =
            OMEGA6_PATTERNS.mapNotNull { pattern ->
                headers.indexOfFirst { it.contains(pattern) }.takeIf { it >= 0 }
            }
        if (omega6Indices.isEmpty()) {
            logger.warn("No omega-6 fatty acid columns found — omega6 will be 0.0 for all aliments")
        }

        return ColumnMap(
            sourceId = sourceIdIdx,
            nom = nomIdx,
            categorie = categorieIdx,
            nutriments = nutrimentIndices,
            omega3Indices = omega3Indices,
            omega6Indices = omega6Indices,
        )
    }

    private fun findColumnIndex(
        headers: List<String>,
        patterns: List<String>,
    ): Int? {
        for (pattern in patterns) {
            val idx = headers.indexOfFirst { it.contains(pattern, ignoreCase = true) }
            if (idx >= 0) return idx
        }
        return null
    }

    private fun findNutrimentColumnIndex(
        headers: List<String>,
        patterns: List<Pair<String, String>>,
    ): Int? {
        for ((pattern1, pattern2) in patterns) {
            val idx =
                headers.indexOfFirst {
                    it.contains(pattern1, ignoreCase = true) && it.contains(pattern2, ignoreCase = true)
                }
            if (idx >= 0) return idx
        }
        return null
    }

    private fun findFirstNumericColumnIndex(headers: List<String>): Int? {
        return headers.indexOfFirst { it.trim().all { c -> c.isDigit() || c == '_' } }
            .takeIf { it >= 0 }
    }

    private fun logColumnMapping(
        columnMap: ColumnMap,
        headers: List<String>,
    ) {
        logger.info("Column mapping resolved:")
        logger.info("  sourceId → [${columnMap.sourceId}] '${headers[columnMap.sourceId]}'")
        logger.info("  nom → [${columnMap.nom}] '${headers[columnMap.nom]}'")
        logger.info("  categorie → [${columnMap.categorie}] '${headers[columnMap.categorie]}'")
        for ((nutriment, idx) in columnMap.nutriments) {
            logger.info("  $nutriment → [$idx] '${headers[idx]}'")
        }
        for (idx in columnMap.omega3Indices) {
            logger.info("  omega3 component → [$idx] '${headers[idx]}'")
        }
        for (idx in columnMap.omega6Indices) {
            logger.info("  omega6 component → [$idx] '${headers[idx]}'")
        }
    }

    // -- Row mapping --

    private fun mapToAlimentRow(
        fields: List<String>,
        columnMap: ColumnMap,
        lineNumber: Int,
    ): AlimentRow {
        val sourceId = fields[columnMap.sourceId].trim()
        val nom = fields[columnMap.nom].trim()
        val categorie = fields[columnMap.categorie].trim()

        if (nom.isBlank()) {
            throw IllegalArgumentException("Empty nom at line $lineNumber")
        }

        val nutrimentValues = mutableMapOf<String, Double>()
        for ((nutriment, idx) in columnMap.nutriments) {
            nutrimentValues[nutriment] = parseCiqualValue(fields[idx], nom, nutriment)
        }

        // Omega-3 = sum of ALA + EPA + DHA
        val omega3 =
            columnMap.omega3Indices.sumOf { idx ->
                parseCiqualValue(fields[idx], nom, "omega3")
            }

        // Omega-6 = linoleic acid (LA)
        val omega6 =
            columnMap.omega6Indices.sumOf { idx ->
                parseCiqualValue(fields[idx], nom, "omega6")
            }

        val regimesCompatibles = detectRegimesCompatibles(categorie)
        val regimesJson = JsonArray(regimesCompatibles.map { JsonPrimitive(it.name) }).toString()

        return AlimentRow(
            id = UUID.randomUUID().toString(),
            nom = nom,
            marque = null,
            source = SourceAliment.CIQUAL,
            sourceId = sourceId,
            codeBarres = null,
            categorie = categorie,
            regimesCompatibles = regimesJson,
            calories = nutrimentValues["calories"] ?: 0.0,
            proteines = nutrimentValues["proteines"] ?: 0.0,
            glucides = nutrimentValues["glucides"] ?: 0.0,
            lipides = nutrimentValues["lipides"] ?: 0.0,
            fibres = nutrimentValues["fibres"] ?: 0.0,
            sel = nutrimentValues["sel"] ?: 0.0,
            sucres = nutrimentValues["sucres"] ?: 0.0,
            fer = nutrimentValues["fer"] ?: 0.0,
            calcium = nutrimentValues["calcium"] ?: 0.0,
            zinc = nutrimentValues["zinc"] ?: 0.0,
            magnesium = nutrimentValues["magnesium"] ?: 0.0,
            vitamineB12 = nutrimentValues["vitamineB12"] ?: 0.0,
            vitamineD = nutrimentValues["vitamineD"] ?: 0.0,
            vitamineC = nutrimentValues["vitamineC"] ?: 0.0,
            omega3 = omega3,
            omega6 = omega6,
        )
    }

    // -- Ciqual value parsing --

    /**
     * Parse a Ciqual cell value to Double.
     * Handles special markers: "-", "traces", "N/A", "< X", empty, and French decimal comma.
     * Any non-parseable value defaults to 0.0 with a warning.
     */
    internal fun parseCiqualValue(
        raw: String,
        alimentName: String,
        nutrimentName: String,
    ): Double {
        val trimmed = raw.trim()

        // Special Ciqual markers → 0.0
        if (trimmed.isEmpty() || trimmed == "-" || trimmed.equals("N/A", ignoreCase = true)) {
            return 0.0
        }
        if (trimmed.equals("traces", ignoreCase = true) || trimmed.startsWith("<")) {
            return 0.0
        }

        // French decimal: replace comma with dot
        val normalized = trimmed.replace(DECIMAL_COMMA, DECIMAL_DOT)

        return try {
            normalized.toDouble()
        } catch (e: NumberFormatException) {
            logger.warn(
                "Cannot parse value '$raw' for nutriment '$nutrimentName' " +
                    "in aliment '$alimentName' — defaulting to 0.0",
            )
            0.0
        }
    }

    // -- Regime detection --

    /**
     * Detect compatible diets based on the Ciqual category string.
     * Uses case-insensitive, accent-insensitive matching.
     */
    internal fun detectRegimesCompatibles(categorie: String): List<RegimeAlimentaire> {
        val normalized = removeAccents(categorie.lowercase())

        // Check meat/fish keywords → OMNIVORE + FLEXITARIEN only
        for (keyword in MEAT_FISH_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return listOf(RegimeAlimentaire.OMNIVORE, RegimeAlimentaire.FLEXITARIEN)
            }
        }

        // Check dairy/egg/honey keywords → VEGETARIEN + FLEXITARIEN + OMNIVORE
        for (keyword in DAIRY_EGG_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return listOf(
                    RegimeAlimentaire.VEGETARIEN,
                    RegimeAlimentaire.FLEXITARIEN,
                    RegimeAlimentaire.OMNIVORE,
                )
            }
        }

        // Default: plant-based → all regimes compatible
        return listOf(
            RegimeAlimentaire.VEGAN,
            RegimeAlimentaire.VEGETARIEN,
            RegimeAlimentaire.FLEXITARIEN,
            RegimeAlimentaire.OMNIVORE,
        )
    }

    // -- CSV parsing (handles quoted fields with newlines) --

    /**
     * Read a complete CSV record from the reader.
     * A record may span multiple lines if fields contain quoted newlines.
     * Returns null at end of stream.
     */
    private fun readCsvRecord(reader: BufferedReader): String? {
        val sb = StringBuilder()
        var firstLine = true
        var inQuotes = false

        while (true) {
            val line = reader.readLine() ?: return if (firstLine) null else sb.toString()
            if (firstLine) {
                firstLine = false
            } else {
                sb.append('\n')
            }
            sb.append(line)

            // Count unescaped quotes to track state
            for (c in line) {
                if (c == '"') inQuotes = !inQuotes
            }

            if (!inQuotes) return sb.toString()
        }
    }

    /**
     * Parse a single CSV record with comma separator and optional quoting.
     * Handles double-quote escaping ("" inside quoted fields).
     */
    internal fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> {
                    inQuotes = true
                }
                c == '"' && inQuotes -> {
                    // Check for escaped quote ""
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // skip next quote
                    } else {
                        inQuotes = false
                    }
                }
                c == CSV_SEPARATOR && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> {
                    current.append(c)
                }
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }

    // -- Utility --

    private fun countZeroNutriments(row: AlimentRow): Int {
        var count = 0
        if (row.calories == 0.0) count++
        if (row.proteines == 0.0) count++
        if (row.glucides == 0.0) count++
        if (row.lipides == 0.0) count++
        if (row.fibres == 0.0) count++
        if (row.sel == 0.0) count++
        if (row.sucres == 0.0) count++
        if (row.fer == 0.0) count++
        if (row.calcium == 0.0) count++
        if (row.zinc == 0.0) count++
        if (row.magnesium == 0.0) count++
        if (row.vitamineB12 == 0.0) count++
        if (row.vitamineD == 0.0) count++
        if (row.vitamineC == 0.0) count++
        if (row.omega3 == 0.0) count++
        if (row.omega6 == 0.0) count++
        return count
    }

    /**
     * Remove Unicode accents from a string for comparison purposes.
     */
    private fun removeAccents(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    /**
     * Remove UTF-8 BOM prefix if present.
     */
    private fun String.removeBomPrefix(): String {
        return if (startsWith("\uFEFF")) substring(1) else this
    }
}
