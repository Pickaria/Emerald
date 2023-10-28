package fr.pickaria.emerald.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Accounts : IntIdTable() {
    val playerUuid = uuid("player_uuid")
    val accountName = varchar("account_name", 16)
    val balance = double("balance").default(0.0)

    init {
        index(true, playerUuid, accountName)
    }
}

class Account(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Account>(Accounts)

    var playerUuid by Accounts.playerUuid
    var accountName by Accounts.accountName
    var balance by Accounts.balance
}
