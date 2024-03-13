package fr.pickaria.emerald.domain

import org.bukkit.event.Event
import org.bukkit.event.HandlerList


class AccountChangeEvent(val account: Account) : Event() {
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