import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.data.H2EconomyRepository
import fr.pickaria.emerald.data.UnknownCurrencyException
import mocks.economyConfigMock
import org.junit.jupiter.api.*
import kotlin.test.assertEquals


class H2EconomyRepositoryTest {
    private lateinit var server: ServerMock
    private lateinit var economyRepository: H2EconomyRepository

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUpAll() {
            openTestDatabase()
        }
    }

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        economyRepository = H2EconomyRepository(economyConfigMock)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `should update balance on first deposit`() {
        // Given
        val player = server.addPlayer()

        // When
        economyRepository.depositPlayer(player.uniqueId, 5.6, "Credits")

        // Then
        val balance = economyRepository.getBalance(player.uniqueId, "Credits")
        assertEquals(5.6, balance)
    }

    @Test
    fun `should update balance on withdraw and deposit`() {
        // Given
        val player = server.addPlayer()

        // When
        economyRepository.depositPlayer(player.uniqueId, 50.0, "Credits")
        economyRepository.withdrawPlayer(player.uniqueId, 5.0, "Credits")

        // Then
        val balance = economyRepository.getBalance(player.uniqueId, "Credits")
        assertEquals(45.0, balance)
    }

    @Test
    fun `should withdraw and set negative balance`() {
        // Given
        val player = server.addPlayer()

        // When
        economyRepository.withdrawPlayer(player.uniqueId, 5.0, "Credits")

        // Then
        val balance = economyRepository.getBalance(player.uniqueId, "Credits")
        assertEquals(-5.0, balance)
    }

    @Test
    fun `should handle multiple accounts correctly`() {
        // Given
        val player = server.addPlayer()

        // When
        economyRepository.depositPlayer(player.uniqueId, 50.0, "Credits")
        economyRepository.depositPlayer(player.uniqueId, 45.0, "Shards")
        economyRepository.withdrawPlayer(player.uniqueId, 5.0, "Shards")

        // Then
        val creditsBalance = economyRepository.getBalance(player.uniqueId, "Credits")
        assertEquals(50.0, creditsBalance)

        val shardsBalance = economyRepository.getBalance(player.uniqueId, "Shards")
        assertEquals(40.0, shardsBalance)
    }

    @Test
    fun `should return balance of 0 when no account`() {
        // Given
        val player = server.addPlayer()

        // When
        val balance = economyRepository.getBalance(player.uniqueId, "Credits")

        // Then
        assertEquals(0.0, balance)
    }

    @Nested
    inner class Configuration {
        @Test
        fun `should return valid currency format`() {
            // Given / When
            val config = economyRepository.getConfig("credits")

            // Then
            assertEquals("###,##0.00", config.format)
        }

        @Test
        fun `should throw exception when asked format of unknown currency`() {
            // Given / When / Then
            assertThrows<UnknownCurrencyException> {
                economyRepository.getConfig("unknown")
            }
        }
    }
}
