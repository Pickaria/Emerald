package mocks

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Creates a mocked physical currency item.
 *
 * @param material the material of the currency item (default is IRON_NUGGET)
 * @param value the value of the currency item (default is 1.0)
 * @param count the number of currency items in the stack (default is 1)
 * @param currency the currency name associated with the item (default is "credits")
 * @return an ItemStack representing the mocked physical currency item
 */
fun physicalCurrencyItemMock(
    material: Material = Material.IRON_NUGGET,
    value: Double = 1.0,
    count: Int = 1,
    currency: String = "credits"
) = ItemStack(material).apply {
    editMeta {
        it.persistentDataContainer.set(
            NamespacedKey.fromString("pickaria:value")!!,
            PersistentDataType.DOUBLE,
            value
        )
        it.persistentDataContainer.set(
            NamespacedKey.fromString("pickaria:currency")!!,
            PersistentDataType.STRING,
            currency
        )
    }

    amount = count
}
