package fr.pickaria.emerald.domain

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import fr.pickaria.emerald.data.PhysicalCurrencyConfig
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

val currencyNamespace = NamespacedKey("pickaria", "currency")
val valueNamespace = NamespacedKey("pickaria", "value")

internal class PhysicalCurrency(
    economyRepository: EconomyRepository,
    private val economyService: EconomyService,
    private val price: Price,
) {
    private val config = economyRepository.getConfig(price.currency.serialName)

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
            val itemStack = ItemStack(physicalCurrency.material).apply {
                editMeta { meta ->
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

            overflowStacks(itemStack, amount)
        }
    }

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

    fun getItem(): ItemStack {
        val currencyDisplayName: Component by lazy {
            Component.text(config.nameSingular.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        }

        val items = items(currencyDisplayName)

        return if (items.size > 1) {
            bundle(price, items, currencyDisplayName)
        } else {
            items.first()
        }
    }
}
