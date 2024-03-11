import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.data.EconomyRepository
import fr.pickaria.emerald.domain.*
import io.mockk.*
import org.junit.jupiter.api.*
import kotlin.test.assertEquals

class AccountTest {
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
        val account = economyService.getAccount(player)

        // When
        val balance = account.getBalance(Currencies.CREDITS)

        // Then
        assertEquals(expectedPrice, balance)
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
            val account = economyService.getAccount(player)

            // When
            account.withdraw(withdrawingPrice)

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
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<BalanceInsufficientException> {
                account.withdraw( withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when player has negative amount of money`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns -5.6
            val withdrawingPrice = Price(32.8, Currencies.CREDITS)
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<BalanceInsufficientException> {
                account.withdraw(withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when invalid amount is specified`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(-3.1, Currencies.CREDITS)
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<InvalidAmountException> {
                account.withdraw(withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when amount of 0 is specified`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(0.0, Currencies.CREDITS)
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<InvalidAmountException> {
                account.withdraw(withdrawingPrice)
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
            val account = economyService.getAccount(player)

            // When
            account.deposit(withdrawingPrice)

            // Then
            verify { repository.depositPlayer(eq(player.uniqueId), eq(32.8), eq("credits")) }
        }

        @Test
        fun `should throw exception when invalid amount is specified`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(-3.1, Currencies.CREDITS)
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<InvalidAmountException> {
                account.deposit(withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when amount of 0 is specified`() {
            // Given
            val player = server.addPlayer()
            coEvery { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(0.0, Currencies.CREDITS)
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<InvalidAmountException> {
                account.deposit(withdrawingPrice)
            }
        }
    }

}