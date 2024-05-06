package fr.pickaria.emerald.domain.events

import fr.pickaria.emerald.domain.Account
import fr.pickaria.emerald.domain.Price
import org.bukkit.event.Event
import org.bukkit.event.HandlerList


class AsyncAccountChangeEvent(
    val account: Account,
    val price: Price,
    val changeType: AccountChangeType
) : Event(true) {
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
