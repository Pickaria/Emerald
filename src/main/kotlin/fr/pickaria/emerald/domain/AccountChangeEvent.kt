package fr.pickaria.emerald.domain

import org.bukkit.event.Event
import org.bukkit.event.HandlerList


enum class AccountChangeType {
    WITHDRAW,
    DEPOSIT,
}

class AccountChangeEvent(
    val account: Account,
    val price: Price,
    val changeType: AccountChangeType
) : Event() {
    companion object {
        @JvmStatic
        private val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }
}