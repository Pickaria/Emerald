package fr.pickaria.emerald

import fr.pickaria.emerald.data.Accounts
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun openTestDatabase() {
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

    transaction {
        SchemaUtils.create(Accounts)
    }
}
