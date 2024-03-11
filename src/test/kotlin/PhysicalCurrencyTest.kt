import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import fr.pickaria.emerald.domain.*
import io.mockk.*
import mocks.creditConfigMock
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.persistence.PersistentDataType
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

class PhysicalCurrencyTest {
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
        val price = Price(0.0, Currencies.CREDITS)

        // When / Then
        assertThrows<InvalidAmountException> {
            economyService.getPhysicalCurrency(price)
        }
    }

    @Test
    fun `physical currency should have the correct value`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(1.0, Currencies.CREDITS)

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
        val result = economyService.getPhysicalCurrency(price)

        // Then
        assertEquals(
            result.itemMeta.persistentDataContainer.toString(),
            expected.itemMeta.persistentDataContainer.toString()
        )
        assertEquals(result.amount, expected.amount)
        assertEquals(result.type, expected.type)
    }

    @Test
    fun `physical currency should handle increasing item rarity`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(128.0, Currencies.CREDITS)

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
        val result = economyService.getPhysicalCurrency(price)

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
        val price = Price(96.0, Currencies.CREDITS)

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
        val result = economyService.getPhysicalCurrency(price)
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
        val price = Price(9999.0, Currencies.CREDITS)

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
        val result = economyService.getPhysicalCurrency(price)
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
        val price = Price(1.0, Currencies.CREDITS)

        // When
        val result = economyService.getPhysicalCurrency(price)

        // Then
        assertEquals(1, result.enchantments.size)
    }

    @Test
    fun `physical currency should have a name and lore`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(1.0, Currencies.CREDITS)

        // When
        val result = economyService.getPhysicalCurrency(price)

        // Then
        assertEquals("Crédit", (result.itemMeta.displayName() as TextComponent).content())
    }
}