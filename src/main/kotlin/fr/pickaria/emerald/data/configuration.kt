package fr.pickaria.emerald.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material


@Serializable
data class PhysicalCurrencyConfig(
    val value: Double,
    val material: Material,
)

@Serializable
data class CurrencyConfig(
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
data class DatabaseConfig(
    @SerialName("url")
    val url: String,

    @SerialName("driver")
    val driver: String,
)

@Serializable
data class EconomyConfig(
    val database: DatabaseConfig,
    val currencies: Map<String, CurrencyConfig>
)
