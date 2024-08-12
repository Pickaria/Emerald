package fr.pickaria.emerald.data

import fr.pickaria.database.DatabaseConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material


@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
internal data class PhysicalCurrencyConfig(
    val value: Double,
    val material: Material,
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
internal data class Name(
    val singular: String,
    val plural: String,
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
internal data class CurrencyConfig(
    val name: Name,
    val description: List<String>,

    @SerialName("physical_currencies")
    val physicalCurrencies: List<PhysicalCurrencyConfig>,

    @SerialName("collection_sound")
    val collectionSound: String,

    @SerialName("start_amount")
    val startAmount: Double,
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
internal data class EconomyConfig(
    @SerialName("default_currency")
    val defaultCurrency: String,

    val database: DatabaseConfig,
    val currencies: Map<String, CurrencyConfig>,
)
