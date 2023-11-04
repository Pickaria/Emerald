package fr.pickaria.emerald.domain

import fr.pickaria.emerald.data.CurrencyConfig
import fr.pickaria.emerald.data.EconomyRepository
import fr.pickaria.emerald.data.PhysicalCurrency
import fr.pickaria.shared.GlowEnchantment
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
import java.security.InvalidParameterException
import java.util.*
import kotlin.math.min

val currencyNamespace = NamespacedKey("pickaria", "currency")
val valueNamespace = NamespacedKey("pickaria", "value")

// TODO: Rewrite this class to make it simpler
class PhysicalCurrencyService(private val economyService: EconomyService, private val economyRepository: EconomyRepository, private val price: Price) {
    private val config: CurrencyConfig by lazy {
       economyRepository.getConfig(price.currency.serialName)
    }

    private val currencyDisplayName: Component by lazy {
        Component.text(config.nameSingular.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    }

    /**
     * All the physical currencies sorted by descending value.
     */
    private val physicalCurrencies by lazy {
        config.physicalCurrencies.sortedByDescending { it.value }
    }

    private fun overflowStacks(material: Material, amountToGive: Int, meta: ((ItemMeta) -> Unit)? = null): List<ItemStack> {
        val stacks = mutableListOf<ItemStack>()
        if (material.maxStackSize == 0) {
            throw InvalidParameterException("Material has a maxStackSize of 0.")
        }
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
    private fun items(totalValue: Double): List<ItemStack> {
        var remaining = totalValue
        val coins = mutableMapOf<PhysicalCurrency, Int>()

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

                meta.persistentDataContainer.set(currencyNamespace, PersistentDataType.STRING, price.currency.serialName)
                meta.persistentDataContainer.set(valueNamespace, PersistentDataType.DOUBLE, physicalCurrency.value)
            }
        }
    }

    /**
     * Put all items for a specified total value in a bundle.
     */
    private fun bundle(totalValue: Double, items: List<ItemStack>) = ItemStack(Material.BUNDLE).apply {
        editMeta { meta ->
            meta.addEnchant(Enchantment.DURABILITY, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

            val name = Component.text("Sacoche de")
                .append(Component.space())
                .append(currencyDisplayName)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)

            meta.displayName(name)

            val line = MiniMessage("<gray>Sacoche d''une valeur de <gold><value></gold>.") {
                "value" to economyService.format(Price(totalValue, price.currency))
            }.toComponent().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)

            meta.lore(listOf(line))

            meta.persistentDataContainer.set(currencyNamespace, PersistentDataType.STRING, price.currency.serialName)
            meta.persistentDataContainer.set(valueNamespace, PersistentDataType.DOUBLE, totalValue)

            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS)

            items.forEach {
                (meta as BundleMeta).addItem(it)
            }
        }
    }

    /**
     * Creates a new ItemStack for the currency.
     * @param amount Size of the stack, must be between 0 and `Material.maxStackSize`, defaults to 1.
     * @param value Value of each currency item, defaults to 1.0.
     */
    @Deprecated("Use items(totalValue).first() instead")
    private fun item(amount: Int = 1, value: Double = 1.0): ItemStack {
        val itemStack = ItemStack(config.physicalCurrencies.first().material, amount)

        itemStack.editMeta { meta ->
            meta.addEnchant(GlowEnchantment.instance, 1, true)
            meta.displayName(currencyDisplayName)

            val line = config.description.map {
                MiniMessage(it) {
                    "value" to economyService.format(Price(value, price.currency))
                }.toComponent().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            }

            meta.lore(line)

            meta.persistentDataContainer.set(currencyNamespace, PersistentDataType.STRING, price.currency.serialName)
            meta.persistentDataContainer.set(valueNamespace, PersistentDataType.DOUBLE, value)
        }

        return itemStack
    }

    fun item(totalValue: Double): ItemStack {
        val items = items(totalValue)

        return if (items.size > 1) {
            bundle(totalValue, items)
        } else {
            items.first()
        }
    }
}