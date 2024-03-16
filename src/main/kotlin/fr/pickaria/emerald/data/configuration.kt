package fr.pickaria.emerald.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material


@Serializable
internal data class PhysicalCurrencyConfig(
    val value: Double,
    val material: Material,
)

@Serializable
internal data class CurrencyConfig(
    @SerialName("name_singular")
    val nameSingular: String,

    @SerialName("name_plural")
    val namePlural: String,
    val format: String,
    val description: List<String>,

    @SerialName("physical_currencies")
    val physicalCurrencies: List<PhysicalCurrencyConfig>,
)

@Serializable
internal data class DatabaseConfig(
    val url: String,
    val driver: String,
)

@Serializable
internal data class EconomyConfig(
    @SerialName("default_currency")
    val defaultCurrency: String,

    val database: DatabaseConfig,
    val currencies: Map<String, CurrencyConfig>,
)
