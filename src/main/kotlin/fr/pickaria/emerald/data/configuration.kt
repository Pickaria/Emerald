package fr.pickaria.emerald.data

import fr.pickaria.shared.DatabaseConfig
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

    @SerialName("collection_sound")
    val collectionSound: String,
)

@Serializable
internal data class EconomyConfig(
    @SerialName("default_currency")
    val defaultCurrency: String,

    val database: DatabaseConfig,
    val currencies: Map<String, CurrencyConfig>,
)
