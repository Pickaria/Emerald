import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.data.EconomyRepositoryImpl
import fr.pickaria.emerald.domain.UnknownCurrencyException
import mocks.economyConfigMock
import org.junit.jupiter.api.*
import kotlin.test.assertEquals


class EconomyRepositoryImplTest {
    private lateinit var server: ServerMock
    private lateinit var economyRepository: EconomyRepositoryImpl

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        economyRepository = EconomyRepositoryImpl(economyConfigMock)
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
        economyRepository.depositPlayer(player.name, player.uniqueId, 5.6, "credits")

        // Then
        val balance = economyRepository.getBalance(player.uniqueId, "credits")
        assertEquals(5.6, balance)
    }

    @Test
    fun `should update balance on withdraw and deposit`() {
        // Given
        val player = server.addPlayer()

        // When
        economyRepository.depositPlayer(player.name, player.uniqueId, 50.0, "credits")
        economyRepository.withdrawPlayer(player.name, player.uniqueId, 5.0, "credits")

        // Then
        val balance = economyRepository.getBalance(player.uniqueId, "credits")
        assertEquals(45.0, balance)
    }

    @Test
    fun `should withdraw and set negative balance`() {
        // Given
        val player = server.addPlayer()

        // When
        economyRepository.withdrawPlayer(player.name, player.uniqueId, 5.0, "credits")

        // Then
        val balance = economyRepository.getBalance(player.uniqueId, "credits")
        assertEquals(-5.0, balance)
    }

    @Test
    fun `should handle multiple accounts correctly`() {
        // Given
        val player = server.addPlayer()

        // When
        economyRepository.depositPlayer(player.name, player.uniqueId, 50.0, "credits")
        economyRepository.depositPlayer(player.name, player.uniqueId, 45.0, "shards")
        economyRepository.withdrawPlayer(player.name, player.uniqueId, 5.0, "shards")

        // Then
        val creditsBalance = economyRepository.getBalance(player.uniqueId, "credits")
        assertEquals(50.0, creditsBalance)

        val shardsBalance = economyRepository.getBalance(player.uniqueId, "shards")
        assertEquals(40.0, shardsBalance)
    }

    @Test
    fun `should return balance of 0 when no account`() {
        // Given
        val player = server.addPlayer()

        // When
        val balance = economyRepository.getBalance(player.uniqueId, "credits")

        // Then
        assertEquals(0.0, balance)
    }

    @Nested
    inner class Configuration {
        @Test
        fun `should throw exception when asked format of unknown currency`() {
            // Given / When / Then
            assertThrows<UnknownCurrencyException> {
                economyRepository.getConfig("unknown")
            }
        }
    }
}
