package com.appfood.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.appfood.shared.db.AppDatabase

/**
 * JVM implementation — in-memory SQLite for backend context.
 * The backend uses Exposed/PostgreSQL, not SQLDelight.
 * This driver exists only to satisfy the expect/actual contract.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.create(driver)
        return driver
    }
}
