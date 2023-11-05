package fr.pickaria.emerald.domain

import fr.pickaria.emerald.data.CurrencyConfig
import fr.pickaria.emerald.data.EconomyRepository
import fr.pickaria.emerald.data.PhysicalCurrency
import fr.pickaria.shared.MiniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.min

val currencyNamespace = NamespacedKey("pickaria", "currency")
val valueNamespace = NamespacedKey("pickaria", "value")

class PhysicalCurrencyService(
    private val economyService: EconomyService,
    private val economyRepository: EconomyRepository
) {
    private fun overflowStacks(
        material: Material,
        amountToGive: Int,
        meta: ((ItemMeta) -> Unit)? = null
    ): List<ItemStack> {
        require(material.maxStackSize > 0) {
            "Material has a maxStackSize of 0."
        }

        val stacks = mutableListOf<ItemStack>()
        var restToGive = amountToGive
        val item = ItemStack(material).apply {
            editMeta {
                meta?.invoke(it)
            }
        }

        while (restToGive > 0) {
            val amount = min(restToGive, material.maxStackSize)
            stacks += item.asQuantity(amount)
            restToGive -= material.maxStackSize
        }

        return stacks
    }

    /**
     * Get the items required to match a specified total value.
     */
    private fun items(price: Price, config: CurrencyConfig, currencyDisplayName: Component): List<ItemStack> {
        var remaining = price.amount
        val coins = mutableMapOf<PhysicalCurrency, Int>()

        val physicalCurrencies = config.physicalCurrencies.sortedByDescending { it.value }

        physicalCurrencies.forEach {
            val amount = (remaining / it.value).toInt()
            coins[it] = amount
            remaining -= amount * it.value
        }

        return coins.flatMap { (physicalCurrency, amount) ->
            overflowStacks(physicalCurrency.material, amount) { meta ->
                meta.addEnchant(Enchantment.DURABILITY, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                meta.displayName(currencyDisplayName)

                val line = config.description.map {
                    MiniMessage(it) {
                        "value" to economyService.format(Price(physicalCurrency.value, price.currency))
                    }.toComponent().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                }

                meta.lore(line)

                meta.persistentDataContainer.set(
                    currencyNamespace,
                    PersistentDataType.STRING,
                    price.currency.serialName
                )
                meta.persistentDataContainer.set(valueNamespace, PersistentDataType.DOUBLE, physicalCurrency.value)
            }
        }
    }

    /**
     * Put all items for a specified total value in a bundle.
     */
    private fun bundle(
        price: Price,
        items: List<ItemStack>,
        currencyDisplayName: Component
    ): ItemStack {
        return ItemStack(Material.BUNDLE).apply {
            editMeta { meta ->
                meta.addEnchant(Enchantment.DURABILITY, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                val name = Component.text("Sacoche de")
                    .append(Component.space())
                    .append(currencyDisplayName)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)

                meta.displayName(name)

                val line = MiniMessage("<gray>Sacoche d''une valeur de <gold><value></gold>.") {
                    "value" to economyService.format(price)
                }.toComponent().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)

                meta.lore(listOf(line))

                meta.persistentDataContainer.set(
                    currencyNamespace,
                    PersistentDataType.STRING,
                    price.currency.serialName
                )
                meta.persistentDataContainer.set(valueNamespace, PersistentDataType.DOUBLE, price.amount)

                meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS)

                items.forEach {
                    (meta as BundleMeta).addItem(it)
                }
            }
        }
    }

    fun item(price: Price): ItemStack {
        val config: CurrencyConfig by lazy {
            economyRepository.getConfig(price.currency.serialName)
        }

        val currencyDisplayName: Component by lazy {
            Component.text(config.nameSingular.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        }

        val items = items(price, config, currencyDisplayName)

        return if (items.size > 1) {
            bundle(price, items, currencyDisplayName)
        } else {
            items.first()
        }
    }
}
