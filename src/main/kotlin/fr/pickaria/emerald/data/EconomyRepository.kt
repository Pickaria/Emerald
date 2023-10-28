package fr.pickaria.emerald.data

import fr.pickaria.emerald.domain.Currencies
import java.util.*

class EconomyRepository: IEconomyRepository<Currencies> {
    override fun depositPlayer(accountId: UUID, currency: Currencies, amount: Double) {
        TODO("Not yet implemented")
    }

    override fun withdrawPlayer(accountId: UUID, currency: Currencies, amount: Double) {
        TODO("Not yet implemented")
    }

    override fun getBalance(accountId: UUID, currency: Currencies): Double {
        TODO("Not yet implemented")
    }

    override fun createPlayerAccount(accountId: UUID, currency: Currencies) {
        TODO("Not yet implemented")
    }

    override fun getFormat(): String {
        TODO("Not yet implemented")
    }

    override fun currencyNamePlural(): String {
        TODO("Not yet implemented")
    }

    override fun currencyNameSingular(): String {
        TODO("Not yet implemented")
    }
}