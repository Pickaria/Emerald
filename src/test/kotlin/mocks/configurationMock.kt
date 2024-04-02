package mocks

import fr.pickaria.emerald.data.CurrencyConfig
import fr.pickaria.emerald.data.EconomyConfig
import fr.pickaria.emerald.data.Name
import fr.pickaria.emerald.data.PhysicalCurrencyConfig
import fr.pickaria.shared.DatabaseConfig
import org.bukkit.Material

internal val creditConfigMock = CurrencyConfig(
    name = Name(
        singular = "Crédit",
        plural = "Crédits",
    ),
    fractionalName = Name(
        singular = "Crodime",
        plural = "Crodimes",
    ),
    format = "###,##0.00",
    description = listOf("A test currency"),
    collectionSound = "block.amethyst_block.fall",
    physicalCurrencies = listOf(
        PhysicalCurrencyConfig(
            value = 1.0,
            material = Material.IRON_NUGGET,
        ),
        PhysicalCurrencyConfig(
            value = 64.0,
            material = Material.GOLD_NUGGET,
        ),
    ),
)

internal val databaseConfigMock = DatabaseConfig(
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
    driver = "org.h2.Driver",
)

internal val economyConfigMock = EconomyConfig(
    currencies = mapOf(
        "credits" to creditConfigMock,
        "shards" to creditConfigMock.copy(
            name = Name(
                singular = "Éclat",
                plural = "Éclats",
            ),
            fractionalName = Name(
                singular = "Éclat",
                plural = "Éclats",
            ),
            collectionSound = "block.amethyst_cluster.break",
        ),
    ),
    database = databaseConfigMock,
    defaultCurrency = "credits",
)
