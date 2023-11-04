package mocks

import fr.pickaria.emerald.data.CurrencyConfig
import fr.pickaria.emerald.data.EconomyConfig
import fr.pickaria.emerald.data.PhysicalCurrency
import org.bukkit.Material

val creditConfigMock = CurrencyConfig(
    nameSingular = "Crédit",
    namePlural = "Crédits",
    format = "###,##0.00",
    description = listOf("A test currency"),
    physicalCurrencies = listOf(
        PhysicalCurrency(
            value = 1.0,
            material = Material.IRON_NUGGET,
        ),
        PhysicalCurrency(
            value = 64.0,
            material = Material.GOLD_NUGGET,
        ),
    )
)
val economyConfigMock = EconomyConfig(currencies = mapOf("credits" to creditConfigMock))
