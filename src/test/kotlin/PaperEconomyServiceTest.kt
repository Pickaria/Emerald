import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.data.EconomyRepository
import fr.pickaria.emerald.data.UnknownCurrencyException
import fr.pickaria.emerald.domain.*
import io.mockk.*
import mocks.creditConfigMock
import mocks.physicalCurrencyItemMock
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.persistence.PersistentDataType
import org.junit.jupiter.api.*
import kotlin.test.assertEquals


class PaperEconomyServiceTest {
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
    fun `should call economy repository with appropriate user's data to get balance`() {
        // Given
        val player = server.addPlayer()
        coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
        val expectedPrice = Price(5.6, Currencies.CREDITS)

        // When
        val balance = economyService.getBalance(player, Currencies.CREDITS)

        // Then
        assertEquals(expectedPrice, balance)
    }

    @Test
    fun `should return formatted price according to currency's format`() {
        // Given
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(5.6, Currencies.CREDITS)
        val expectedFormattedPrice = "5.60 Crédits"

        // When
        val formattedPrice = economyService.format(price)

        // Then
        assertEquals(expectedFormattedPrice, formattedPrice)
    }

    @Nested
    inner class Withdraw {
        @Test
        fun `should call repository when amount is valid`() {
            // Given
            val player = server.addPlayer()
            val withdrawingPrice = Price(32.8, Currencies.CREDITS)
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 50.0
            coEvery { repository.withdrawPlayer(player.uniqueId, 32.8, "credits") } just Runs

            // When
            economyService.withdraw(player, withdrawingPrice)

            // Then
            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            verify { repository.withdrawPlayer(eq(player.uniqueId), eq(32.8), eq("credits")) }
        }

        @Test
        fun `should throw exception when withdrawing more than a player has`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(32.8, Currencies.CREDITS)

            // When / Then
            assertThrows<BalanceInsufficientException> {
                economyService.withdraw(player, withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when player has negative amount of money`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns -5.6
            val withdrawingPrice = Price(32.8, Currencies.CREDITS)

            // When / Then
            assertThrows<BalanceInsufficientException> {
                economyService.withdraw(player, withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when invalid amount is specified`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(-3.1, Currencies.CREDITS)

            // When / Then
            assertThrows<InvalidAmountException> {
                economyService.withdraw(player, withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when amount of 0 is specified`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(0.0, Currencies.CREDITS)

            // When / Then
            assertThrows<InvalidAmountException> {
                economyService.withdraw(player, withdrawingPrice)
            }
        }
    }

    @Nested
    inner class Deposit {
        @Test
        fun `should call repository when amount is valid`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.depositPlayer(eq(player.uniqueId), eq(32.8), eq("credits")) } just Runs
            val withdrawingPrice = Price(32.8, Currencies.CREDITS)

            // When
            economyService.deposit(player, withdrawingPrice)

            // Then
            verify { repository.depositPlayer(eq(player.uniqueId), eq(32.8), eq("credits")) }
        }

        @Test
        fun `should throw exception when invalid amount is specified`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(-3.1, Currencies.CREDITS)

            // When / Then
            assertThrows<InvalidAmountException> {
                economyService.deposit(player, withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when amount of 0 is specified`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(0.0, Currencies.CREDITS)

            // When / Then
            assertThrows<InvalidAmountException> {
                economyService.deposit(player, withdrawingPrice)
            }
        }
    }

    @Nested
    inner class PhysicalCurrency {
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
    }

    @Nested
    inner class ValueOfPhysicalCurrency {
        @Test
        fun `should throw exception when currency has an invalid amount`() {
            // Given
            coEvery { repository.getConfig("credits") } returns creditConfigMock
            val item = physicalCurrencyItemMock(value = -5.0)

            // When / Then
            assertThrows<InvalidAmountException> {
                economyService.getValueOfItem(item)
            }
        }

        @Test
        fun `should throw exception when item is not a currency`() {
            // Given
            val item = ItemStack(Material.IRON_NUGGET)

            // When / Then
            assertThrows<ItemIsNotACurrencyException> {
                economyService.getValueOfItem(item)
            }
        }

        @Test
        fun `should throw exception when item has an invalid currency`() {
            // Given
            coEvery { repository.getConfig("unknown") } throws UnknownCurrencyException("unknown")
            val item = physicalCurrencyItemMock(currency = "unknown")

            // When / Then
            assertThrows<ItemIsNotACurrencyException> {
                economyService.getValueOfItem(item)
            }
        }

        @Test
        fun `should get the correct expected price from a single item`() {
            // Given
            coEvery { repository.getConfig("credits") } returns creditConfigMock
            val expectedPrice = Price(3.0, Currencies.CREDITS)
            val item = physicalCurrencyItemMock(count = 3, material = Material.IRON_NUGGET)

            // When
            val result = economyService.getValueOfItem(item)

            // Then
            assertEquals(expectedPrice, result)
        }

        @Test
        fun `should get the correct expected price from a bundle`() {
            // Given
            coEvery { repository.getConfig("credits") } returns creditConfigMock
            val expectedPrice = Price(9999.0, Currencies.CREDITS)
            val item = physicalCurrencyItemMock(count = 1, material = Material.BUNDLE, value = 9999.0)

            // When
            val result = economyService.getValueOfItem(item)

            // Then
            assertEquals(expectedPrice, result)
        }
    }
}
