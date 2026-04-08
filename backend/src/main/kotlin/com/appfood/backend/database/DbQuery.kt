package com.appfood.backend.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Wrapper pour les transactions Exposed.
 * Toutes les operations DB doivent passer par cette fonction.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
