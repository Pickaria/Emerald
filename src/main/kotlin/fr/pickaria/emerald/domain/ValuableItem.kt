package fr.pickaria.emerald.domain

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.data.PhysicalCurrencyConfig
import fr.pickaria.emerald.domain.repositories.EconomyRepository
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
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.min

internal val currencyNamespace = NamespacedKey("pickaria", "currency")
internal val valueNamespace = NamespacedKey("pickaria", "value")

internal class ValuableItem(
    economyRepository: EconomyRepository,
    private val economyService: EconomyService,
    private val price: Price
) {
    private val config = economyRepository.getConfig(price.currency)

    private fun overflowStacks(
        item: ItemStack,
        amountToGive: Int,
    ): List<ItemStack> {
        val stacks = mutableListOf<ItemStack>()

        var remainingAmount = amountToGive
        while (remainingAmount > 0) {
            val stackSize = min(remainingAmount, item.type.maxStackSize)
            stacks.add(item.asQuantity(stackSize))
            remainingAmount -= stackSize
        }

        return stacks
    }

    private fun createItem(
        material: Material,
        value: Double,
        displayName: Component,
        lore: List<Component>
    ): ItemStack = ItemStack(material).apply {
        editMeta {
            it.addEnchant(Enchantment.UNBREAKING, 1, true)
            it.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            it.displayName(displayName)
            it.lore(lore)

            it.persistentDataContainer.set(
                currencyNamespace,
                PersistentDataType.STRING,
                price.currency
            )
            it.persistentDataContainer.set(
                valueNamespace,
                PersistentDataType.DOUBLE,
                value
            )
        }
    }

    private fun items(currencyDisplayName: Component): List<ItemStack> {
        var remaining = price.amount
        val coins = mutableMapOf<PhysicalCurrencyConfig, Int>()

        val physicalCurrenciesValues = config.physicalCurrencies.sortedByDescending { it.value }
        physicalCurrenciesValues.forEach {
            val amount = (remaining / it.value).toInt()
            coins[it] = amount
            remaining -= amount * it.value
        }

        return coins.flatMap { (physicalCurrency, amount) ->
            val lore = config.description.map {
                MiniMessage(it) {
                    "value" to economyService.format(Price(physicalCurrency.value, price.currency))
                }.toComponent().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            }

            val itemStack = createItem(physicalCurrency.material, physicalCurrency.value, currencyDisplayName, lore)

            overflowStacks(itemStack, amount)
        }
    }

    private fun bundle(
        items: List<ItemStack>,
        currencyDisplayName: Component
    ): ItemStack {
        val lore = MiniMessage("<gray>Sacoche d''une valeur de <gold><value></gold>.") {
            "value" to economyService.format(price)
        }.toComponent().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)

        return createItem(Material.BUNDLE, price.amount, currencyDisplayName, listOf(lore)).apply {
            val bundleMeta = itemMeta as BundleMeta

            bundleMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            items.forEach {
                bundleMeta.addItem(it)
            }

            itemMeta = bundleMeta
        }
    }

    fun getItem(): ItemStack {
        val currencyDisplayName: Component by lazy {
            Component.text(config.name.singular.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        }

        val items = items(currencyDisplayName)

        return if (items.size > 1) {
            bundle(items, currencyDisplayName)
        } else {
            items.firstOrNull() ?: bundle(emptyList(), currencyDisplayName)
        }
    }
}
