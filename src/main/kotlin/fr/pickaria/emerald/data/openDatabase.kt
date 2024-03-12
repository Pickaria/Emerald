package fr.pickaria.emerald.data

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal fun openDatabase(url: String, driver: String): Database {
    val database = Database.connect(url, driver)

    transaction {
        SchemaUtils.create(Accounts)
    }

    return database
}
