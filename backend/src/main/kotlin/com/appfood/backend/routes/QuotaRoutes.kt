package com.appfood.backend.routes

import com.appfood.backend.database.dao.QuotaRow
import com.appfood.backend.plugins.ValidationException
import com.appfood.backend.plugins.userId
import com.appfood.backend.routes.dto.QuotaListResponse
import com.appfood.backend.routes.dto.QuotaResponse
import com.appfood.backend.routes.dto.QuotaStatusListResponse
import com.appfood.backend.routes.dto.QuotaStatusResponse
import com.appfood.backend.routes.dto.UpdateQuotaRequest
import com.appfood.backend.service.QuotaService
import com.appfood.backend.service.QuotaStatusResult
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate
import org.koin.ktor.ext.inject

fun Route.quotaRoutes() {
    val quotaService by inject<QuotaService>()

    authenticate("auth-jwt") {
        route("/api/v1/quotas") {
            // Specific routes BEFORE parameterized routes

            get("/status") {
                val userId = call.userId()
                val dateStr = call.request.queryParameters["date"]
                val date =
                    if (dateStr != null) {
                        parseQuotaDate(dateStr)
                    } else {
                        todayQuotaDate()
                    }
                val statuses = quotaService.getQuotaStatus(userId, date)
                call.respond(
                    HttpStatusCode.OK,
                    QuotaStatusListResponse(
                        date = date.toString(),
                        data = statuses.map { it.toResponse() },
                    ),
                )
            }

            post("/reset-all") {
                val userId = call.userId()
                val quotas = quotaService.resetAllQuotas(userId)
                call.respond(
                    HttpStatusCode.OK,
                    QuotaListResponse(
                        data = quotas.map { it.toResponse() },
                        total = quotas.size,
                    ),
                )
            }

            post("/recalculate") {
                val userId = call.userId()
                val quotas = quotaService.recalculateQuotas(userId)
                call.respond(
                    HttpStatusCode.OK,
                    QuotaListResponse(
                        data = quotas.map { it.toResponse() },
                        total = quotas.size,
                    ),
                )
            }

            // GET /quotas — all quotas
            get {
                val userId = call.userId()
                val quotas = quotaService.getAllQuotas(userId)
                call.respond(
                    HttpStatusCode.OK,
                    QuotaListResponse(
                        data = quotas.map { it.toResponse() },
                        total = quotas.size,
                    ),
                )
            }

            // POST /quotas/{nutriment}/reset — reset single
            post("/{nutriment}/reset") {
                val userId = call.userId()
                val nutriment =
                    call.parameters["nutriment"]
                        ?: throw ValidationException("Nutriment requis")
                val quota = quotaService.resetQuota(userId, nutriment)
                call.respond(HttpStatusCode.OK, quota.toResponse())
            }

            // PUT /quotas/{nutriment} — customize single
            put("/{nutriment}") {
                val userId = call.userId()
                val nutriment =
                    call.parameters["nutriment"]
                        ?: throw ValidationException("Nutriment requis")
                val request = call.receive<UpdateQuotaRequest>()
                val quota = quotaService.updateQuota(userId, nutriment, request.valeurCible)
                call.respond(HttpStatusCode.OK, quota.toResponse())
            }
        }
    }
}

// --- Mapping helpers ---

internal fun QuotaRow.toResponse() =
    QuotaResponse(
        nutriment = nutriment.name,
        valeurCible = valeurCible,
        estPersonnalise = estPersonnalise,
        valeurCalculee = valeurCalculee,
        unite = unite,
        updatedAt = updatedAt.toString(),
    )

internal fun QuotaStatusResult.toResponse() =
    QuotaStatusResponse(
        nutriment = nutriment.name,
        valeurCible = valeurCible,
        valeurConsommee = valeurConsommee,
        pourcentage = pourcentage,
        unite = unite,
    )

private fun parseQuotaDate(dateStr: String): LocalDate {
    return try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        throw ValidationException("Format de date invalide: '$dateStr'. Attendu: YYYY-MM-DD")
    }
}

private fun todayQuotaDate(): LocalDate {
    val todayStr = kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
    return LocalDate.parse(todayStr)
}
