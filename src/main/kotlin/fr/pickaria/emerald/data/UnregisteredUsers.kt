package fr.pickaria.emerald.data

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

internal object UnregisteredUsers : IntIdTable() {
    val uniqueId = uuid("unique_id")
    val username = varchar("name", 255)

    init {
        index(true, uniqueId, username)
    }
}

internal class UnregisteredUser(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UnregisteredUser>(UnregisteredUsers)

    var uniqueId by UnregisteredUsers.uniqueId
    var username by UnregisteredUsers.username
}
