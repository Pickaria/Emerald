import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.domain.*
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import io.mockk.coEvery
import io.mockk.mockk
import mocks.creditConfigMock
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.persistence.PersistentDataType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ValuableItemTest {
    private val repository = mockk<EconomyRepository>()
    private lateinit var server: ServerMock
    private lateinit var economyService: PaperEconomyService

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        economyService = PaperEconomyService(repository)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `should throw exception when amount of 0 is specified`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(0.0, "credits")

        // When / Then
        assertThrows<InvalidAmountException> {
            economyService.getValuableItemStack(price)
        }
    }

    @Test
    fun `should throw exception when initializing a valuable item with unknown currency from a price`() {
        // Given
        coEvery { repository.getConfig("unknown") } throws UnknownCurrencyException("unknown")
        val price = Price(1.0, "unknown")

        // When / Then
        assertThrows<UnknownCurrencyException> {
            economyService.getValuableItemStack(price)
        }
    }

    @Test
    fun `physical currency should have the correct value`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(1.0, "credits")

        // minecraft:iron_nugget{PublicBukkitValues:{"pickaria:value":1.0d,"pickaria:currency":"credits"}}
        val expected = ItemStack(Material.IRON_NUGGET).apply {
            editMeta {
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:value")!!,
                    PersistentDataType.DOUBLE,
                    1.0
                )
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:currency")!!,
                    PersistentDataType.STRING,
                    "credits"
                )
            }
        }

        // When
        val result = economyService.getValuableItemStack(price)

        // Then
        assertEquals(
            result.itemMeta.persistentDataContainer.toString(),
            expected.itemMeta.persistentDataContainer.toString()
        )
        assertEquals(result.amount, expected.amount)
        assertEquals(result.type, expected.type)
    }

    @Test
    fun `should throw exception when trying to initialize a unknown currency from an item stack`() {
        // Given
        coEvery { repository.getConfig("unknown") } throws UnknownCurrencyException("unknown")
        val item = ItemStack(Material.GOLD_NUGGET).apply {
            editMeta {
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:value")!!,
                    PersistentDataType.DOUBLE,
                    64.0
                )
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:currency")!!,
                    PersistentDataType.STRING,
                    "unknown"
                )
            }
            amount = 2
        }

        // When / Then
        assertThrows<ItemIsNotACurrencyException> { economyService.getValueOfItem(item) }
    }

    @Test
    fun `should throw exception when trying to initialize a valuable item from an item stack with value missing`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val item = ItemStack(Material.GOLD_NUGGET).apply {
            editMeta {
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:currency")!!,
                    PersistentDataType.STRING,
                    "credits"
                )
            }
            amount = 2
        }

        // When / Then
        assertThrows<ItemIsNotACurrencyException> { economyService.getValueOfItem(item) }
    }

    @Test
    fun `should throw exception when trying to initialize a valuable item from an item stack with currency missing`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val item = ItemStack(Material.GOLD_NUGGET).apply {
            editMeta {
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:value")!!,
                    PersistentDataType.DOUBLE,
                    64.0
                )
            }
            amount = 2
        }

        // When / Then
        assertThrows<ItemIsNotACurrencyException> { economyService.getValueOfItem(item) }
    }

    @Test
    fun `physical currency should handle increasing item rarity`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(128.0, "credits")

        // minecraft:gold_nugget{PublicBukkitValues:{"pickaria:value":64.0d,"pickaria:currency":"credits"}}
        val expected = ItemStack(Material.GOLD_NUGGET).apply {
            editMeta {
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:value")!!,
                    PersistentDataType.DOUBLE,
                    64.0
                )
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:currency")!!,
                    PersistentDataType.STRING,
                    "credits"
                )
            }
            amount = 2
        }

        // When
        val result = economyService.getValuableItemStack(price)

        // Then
        assertEquals(
            result.itemMeta.persistentDataContainer.toString(),
            expected.itemMeta.persistentDataContainer.toString()
        )
        assertEquals(result.amount, expected.amount)
        assertEquals(result.type, expected.type)
    }

    @Test
    fun `physical currency should multiple stacks in a bundle`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(96.0, "credits")

        // minecraft:gold_nugget{PublicBukkitValues:{"pickaria:value":64.0d,"pickaria:currency":"credits"}}
        val expected = ItemStack(Material.BUNDLE).apply {
            editMeta {
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:value")!!,
                    PersistentDataType.DOUBLE,
                    96.0
                )
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:currency")!!,
                    PersistentDataType.STRING,
                    "credits"
                )
            }
        }

        // When
        val result = economyService.getValuableItemStack(price)
        val bundleMeta = result.itemMeta as BundleMeta

        // Then
        assertEquals(
            result.itemMeta.persistentDataContainer.toString(),
            expected.itemMeta.persistentDataContainer.toString()
        )
        assertEquals(result.amount, expected.amount)
        assertEquals(result.type, expected.type)
        assertEquals(2 /* stacks */, bundleMeta.items.size)

        assertEquals(Material.GOLD_NUGGET, bundleMeta.items[0].type)
        assertEquals(1, bundleMeta.items[0].amount)
        assertEquals(Material.IRON_NUGGET, bundleMeta.items[1].type)
        assertEquals(32, bundleMeta.items[1].amount)
    }

    @Test
    fun `physical currency should large amount of money in a bundle`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(9999.0, "credits")

        // minecraft:gold_nugget{PublicBukkitValues:{"pickaria:value":64.0d,"pickaria:currency":"credits"}}
        val expected = ItemStack(Material.BUNDLE).apply {
            editMeta {
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:value")!!,
                    PersistentDataType.DOUBLE,
                    9999.0
                )
                it.persistentDataContainer.set(
                    NamespacedKey.fromString("pickaria:currency")!!,
                    PersistentDataType.STRING,
                    "credits"
                )
            }
        }

        // When
        val result = economyService.getValuableItemStack(price)
        val bundleMeta = result.itemMeta as BundleMeta

        // Then
        assertEquals(
            result.itemMeta.persistentDataContainer.toString(),
            expected.itemMeta.persistentDataContainer.toString()
        )
        assertEquals(result.amount, expected.amount)
        assertEquals(result.type, expected.type)
        assertEquals(4 /* stacks */, bundleMeta.items.size)

        assertEquals(Material.GOLD_NUGGET, bundleMeta.items[0].type)
        assertEquals(64, bundleMeta.items[0].amount)
        assertEquals(Material.GOLD_NUGGET, bundleMeta.items[1].type)
        assertEquals(64, bundleMeta.items[1].amount)
        assertEquals(Material.GOLD_NUGGET, bundleMeta.items[2].type)
        assertEquals(28, bundleMeta.items[2].amount)
        assertEquals(Material.IRON_NUGGET, bundleMeta.items[3].type)
        assertEquals(15, bundleMeta.items[3].amount)
    }

    @Test
    fun `physical currency should be enchanted`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(1.0, "credits")

        // When
        val result = economyService.getValuableItemStack(price)

        // Then
        assertEquals(1, result.enchantments.size)
    }

    @Test
    fun `physical currency should have a name and lore`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(1.0, "credits")

        // When
        val result = economyService.getValuableItemStack(price)

        // Then
        assertEquals("Crédit", (result.itemMeta.displayName() as TextComponent).content())
    }
}