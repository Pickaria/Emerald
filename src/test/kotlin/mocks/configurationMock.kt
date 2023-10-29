package mocks

import fr.pickaria.emerald.data.CurrencyConfig
import fr.pickaria.emerald.data.EconomyConfig

val creditConfigMock = CurrencyConfig(nameSingular = "Crédit", namePlural = "Crédits", format = "###,##0.00", physicalCurrencies = emptyList())
val economyConfigMock = EconomyConfig(currencies = mapOf("credits" to creditConfigMock))
