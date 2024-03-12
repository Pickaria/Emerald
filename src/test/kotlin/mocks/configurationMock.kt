package mocks

import fr.pickaria.emerald.data.CurrencyConfig
import fr.pickaria.emerald.data.DatabaseConfig
import fr.pickaria.emerald.data.EconomyConfig
import fr.pickaria.emerald.data.PhysicalCurrencyConfig
import org.bukkit.Material

val creditConfigMock = CurrencyConfig(
    nameSingular = "Crédit",
    namePlural = "Crédits",
    format = "###,##0.00",
    description = listOf("A test currency"),
    physicalCurrencies = listOf(
        PhysicalCurrencyConfig(
            value = 1.0,
            material = Material.IRON_NUGGET,
        ),
        PhysicalCurrencyConfig(
            value = 64.0,
            material = Material.GOLD_NUGGET,
        ),
    )
)

val databaseConfigMock = DatabaseConfig(
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
    driver = "org.h2.Driver"
)

val economyConfigMock = EconomyConfig(
    currencies = mapOf("credits" to creditConfigMock),
    database = databaseConfigMock
)
