import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.domain.*
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import io.mockk.every
import io.mockk.mockk
import mocks.creditConfigMock
import mocks.physicalCurrencyItemMock
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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

    @ParameterizedTest(name = "should return '{1}' when value is '{0}'")
    @CsvSource(value = ["5.6,5.60 Crédits", "0.0,0.00 Crédit", "1.00,1.00 Crédit", "0.20,0.20 Crodimes"])
    fun `should return formatted price according to currency's format`(
        value: Double,
        expectedFormattedPrice: String
    ) {
        // Given
        every { repository.getConfig("credits") } returns creditConfigMock
        val price = Price(value, "credits")

        // When
        val formattedPrice = economyService.format(price)

        // Then
        assertEquals(expectedFormattedPrice, formattedPrice)
    }

    @Nested
    inner class ValueOfValuableItemConfig {
        @Test
        fun `should throw exception when currency has an invalid amount`() {
            // Given
            every { repository.getConfig("credits") } returns creditConfigMock
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
        fun `should throw exception when item has value but no a currency`() {
            // Given
            val item = physicalCurrencyItemMock(value = null)

            // When / Then
            assertThrows<ItemIsNotACurrencyException> {
                economyService.getValueOfItem(item)
            }
        }

        @Test
        fun `should throw exception when item has a currency but no value`() {
            // Given
            val item = physicalCurrencyItemMock(currency = null)

            // When / Then
            assertThrows<ItemIsNotACurrencyException> {
                economyService.getValueOfItem(item)
            }
        }

        @Test
        fun `should throw exception when item has an invalid currency`() {
            // Given
            every { repository.getConfig("unknown") } throws UnknownCurrencyException("unknown")
            val item = physicalCurrencyItemMock(currency = "unknown")

            // When / Then
            assertThrows<ItemIsNotACurrencyException> {
                economyService.getValueOfItem(item)
            }
        }

        @Test
        fun `should get the correct expected price from a single item`() {
            // Given
            every { repository.getConfig("credits") } returns creditConfigMock
            val expectedPrice = Price(3.0, "credits")
            val item = physicalCurrencyItemMock(count = 3, material = Material.IRON_NUGGET)

            // When
            val result = economyService.getValueOfItem(item)

            // Then
            assertEquals(expectedPrice, result)
        }

        @Test
        fun `should get the correct expected price from a bundle`() {
            // Given
            every { repository.getConfig("credits") } returns creditConfigMock
            val expectedPrice = Price(9999.0, "credits")
            val item = physicalCurrencyItemMock(count = 1, material = Material.BUNDLE, value = 9999.0)

            // When
            val result = economyService.getValueOfItem(item)

            // Then
            assertEquals(expectedPrice, result)
        }
    }
}
