import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.domain.*
import fr.pickaria.emerald.domain.events.AccountChangeEvent
import fr.pickaria.emerald.domain.events.AsyncAccountChangeEvent
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import io.mockk.*
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountTest {
    private val repository = mockk<EconomyRepository>()
    private lateinit var server: ServerMock
    private lateinit var economyService: EconomyServiceImpl
    private lateinit var plugin: JavaPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
        economyService = EconomyServiceImpl(repository, plugin)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Nested
    inner class Balance {
        @Test
        fun `should call economy repository with appropriate user's data to get balance`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val expectedPrice = Price(5.6, "credits")
            val account = economyService.getAccount(player)

            // When
            val balance = account.getBalance("credits")

            // Then
            assertEquals(expectedPrice, balance)
        }

        @Test
        fun `should throw exception when trying to get balance of unknown currency`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "unknown") } throws UnknownCurrencyException("unknown")
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<UnknownCurrencyException> { account.getBalance("unknown") }
        }
    }

    @Nested
    inner class Has {
        @Test
        fun `should return true if player has enough of currency`() {
            // Given
            val player = server.addPlayer()
            val withdrawingPrice = Price(32.8, "credits")
            every { repository.getBalance(player.uniqueId, "credits") } returns 50.0
            val account = economyService.getAccount(player)

            // When
            val result = account.has(withdrawingPrice)

            // Then
            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            assertTrue(result)
        }

        @Test
        fun `should return false if player does not have enough of currency`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(32.8, "credits")
            val account = economyService.getAccount(player)

            // When
            val result = account.has(withdrawingPrice)

            // Then
            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            assertFalse(result)
        }

        @Test
        fun `should throw exception when player has negative amount of money`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns -5.6
            val withdrawingPrice = Price(32.8, "credits")
            val account = economyService.getAccount(player)

            // When
            val result = account.has(withdrawingPrice)

            // Then
            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            assertFalse(result)
        }

        @Test
        fun `should throw exception when invalid amount is specified`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(-3.1, "credits")
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<InvalidAmountException> {
                account.has(withdrawingPrice)
            }
        }

        @Test
        fun `should return true when amount of 0 is specified`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(0.0, "credits")
            val account = economyService.getAccount(player)
            val expectedResult = true

            // When
            val result = account.has(withdrawingPrice)

            // Then
            assertEquals(expectedResult, result)
        }

        @Test
        fun `should throw exception when trying to withdraw unknown currency`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "unknown") } throws UnknownCurrencyException("unknown")
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<UnknownCurrencyException> { account.has(Price(5.6, "unknown")) }
        }
    }

    @Nested
    inner class Withdraw {
        @Test
        fun `should call repository when amount is valid`() {
            // Given
            val player = server.addPlayer()
            val withdrawingPrice = Price(32.8, "credits")
            every { repository.getBalance(player.uniqueId, "credits") } returns 50.0
            every { repository.withdrawPlayer(player.name, player.uniqueId, 32.8, "credits") } just Runs
            val account = economyService.getAccount(player)

            // When
            account.withdraw(withdrawingPrice)

            // Then
            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            verify { repository.withdrawPlayer(eq(player.name), eq(player.uniqueId), eq(32.8), eq("credits")) }
        }

        @Test
        fun `should throw exception when withdrawing more than a player has`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(32.8, "credits")
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<BalanceInsufficientException> {
                account.withdraw(withdrawingPrice)
            }
        }

        @Test
        fun `should throw exception when player has negative amount of money`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns -5.6
            val withdrawingPrice = Price(32.8, "credits")
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
            every { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(-3.1, "credits")
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<InvalidAmountException> {
                account.withdraw(withdrawingPrice)
            }
        }

        @Test
        fun `should call repository when amount of 0 is specified`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            every { repository.withdrawPlayer(player.name, player.uniqueId, 0.0, "credits") } just Runs
            val withdrawingPrice = Price(0.0, "credits")
            val account = economyService.getAccount(player)

            // When
            account.withdraw(withdrawingPrice)

            // Then
            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            verify { repository.withdrawPlayer(eq(player.name), eq(player.uniqueId), eq(0.0), eq("credits")) }
        }

        @Test
        fun `account change event is called when a withdrawal is made`() {
            // Given
            val player = server.addPlayer()
            every { repository.withdrawPlayer(eq(player.name), eq(player.uniqueId), eq(32.8), eq("credits")) } just Runs
            every { repository.getBalance(player.uniqueId, "credits") } returns 32.8
            val withdrawingPrice = Price(32.8, "credits")
            val account = economyService.getAccount(player)

            // When
            account.withdraw(withdrawingPrice)
            server.scheduler.waitAsyncTasksFinished()

            // Then
            server.pluginManager.assertEventFired(AccountChangeEvent::class.java)
            server.pluginManager.assertEventFired(AsyncAccountChangeEvent::class.java)
        }

        @Test
        fun `should throw exception when trying to withdraw unknown currency`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "unknown") } throws UnknownCurrencyException("unknown")
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<UnknownCurrencyException> { account.withdraw(Price(5.6, "unknown")) }
        }
    }

    @Nested
    inner class WithdrawAll {
        @Test
        fun `should withdraw all if player has enough`() {
            // Given
            val player = server.addPlayer()
            val withdrawingPrice1 = Price(32.8, "credits")
            val withdrawingPrice2 = Price(3.0, "shards")
            every { repository.getBalance(player.uniqueId, any()) } returns 50.0
            every { repository.withdrawPlayer(player.name, player.uniqueId, any(), any()) } just Runs
            val account = economyService.getAccount(player)

            // When
            account.withdrawAll(listOf(withdrawingPrice1, withdrawingPrice2))

            // Then
            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            verify { repository.getBalance(eq(player.uniqueId), eq("shards")) }
            verify { repository.withdrawPlayer(eq(player.name), eq(player.uniqueId), eq(32.8), eq("credits")) }
            verify { repository.withdrawPlayer(eq(player.name), eq(player.uniqueId), eq(3.0), eq("shards")) }
        }

        @Test
        fun `should throw exception if player does not have enough`() {
            // Given
            val player = server.addPlayer()
            val withdrawingPrice1 = Price(32.8, "credits")
            val withdrawingPrice2 = Price(3.0, "shards")
            every { repository.getBalance(player.uniqueId, "credits") } returns 50.0
            every { repository.getBalance(player.uniqueId, "shards") } returns 1.0
            every { repository.withdrawPlayer(player.name, player.uniqueId, 32.8, eq("credits")) } just Runs
            every { repository.depositPlayer(player.name, player.uniqueId, 32.8, eq("credits")) } just Runs
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<BalanceInsufficientException> {
                account.withdrawAll(listOf(withdrawingPrice1, withdrawingPrice2))
            }
        }

        @Test
        fun `should rollback and then throws exception if player is missing second price after unexpected balance change`() {
            // Given
            val player = server.addPlayer()
            val withdrawingPrice1 = Price(32.8, "credits")
            val withdrawingPrice2 = Price(3.0, "shards")
            every { repository.getBalance(player.uniqueId, "credits") } returns 50.0
            every { repository.getBalance(player.uniqueId, "shards") } returns 50.0 andThen 1.0
            every { repository.withdrawPlayer(player.name, player.uniqueId, 32.8, eq("credits")) } just Runs
            every { repository.depositPlayer(player.name, player.uniqueId, 32.8, eq("credits")) } just Runs
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<BalanceInsufficientException> {
                account.withdrawAll(listOf(withdrawingPrice1, withdrawingPrice2))
            }

            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            verify { repository.getBalance(eq(player.uniqueId), eq("shards")) }
            verify { repository.withdrawPlayer(eq(player.name), eq(player.uniqueId), eq(32.8), eq("credits")) }
            verify { repository.depositPlayer(eq(player.name), eq(player.uniqueId), eq(32.8), eq("credits")) }
        }

        @Test
        fun `should rollback nothing and throws exception if player is missing first price after unexpected balance change`() {
            // Given
            val player = server.addPlayer()
            val withdrawingPrice1 = Price(32.8, "credits")
            val withdrawingPrice2 = Price(3.0, "shards")
            every { repository.getBalance(player.uniqueId, "credits") } returns 50.0 andThen 1.0
            every { repository.getBalance(player.uniqueId, "shards") } returns 50.0
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<BalanceInsufficientException> {
                account.withdrawAll(listOf(withdrawingPrice1, withdrawingPrice2))
            }

            verify { repository.getBalance(eq(player.uniqueId), eq("credits")) }
            verify { repository.getBalance(eq(player.uniqueId), eq("shards")) }
        }
    }

    @Nested
    inner class Deposit {
        @Test
        fun `should call repository when amount is valid`() {
            // Given
            val player = server.addPlayer()
            every { repository.depositPlayer(eq(player.name), eq(player.uniqueId), eq(32.8), eq("credits")) } just Runs
            val withdrawingPrice = Price(32.8, "credits")
            val account = economyService.getAccount(player)

            // When
            account.deposit(withdrawingPrice)

            // Then
            verify { repository.depositPlayer(eq(player.name), eq(player.uniqueId), eq(32.8), eq("credits")) }
        }

        @Test
        fun `should throw exception when invalid amount is specified`() {
            // Given
            val player = server.addPlayer()
            every { repository.getBalance(player.uniqueId, "credits") } returns 5.6
            val withdrawingPrice = Price(-3.1, "credits")
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<InvalidAmountException> {
                account.deposit(withdrawingPrice)
            }
        }

        @Test
        fun `should call repository when amount of 0 is specified`() {
            // Given
            val player = server.addPlayer()
            every { repository.depositPlayer(eq(player.name), eq(player.uniqueId), eq(0.0), eq("credits")) } just Runs
            val withdrawingPrice = Price(0.0, "credits")
            val account = economyService.getAccount(player)

            // When
            account.deposit(withdrawingPrice)

            // Then
            verify { repository.depositPlayer(eq(player.name), eq(player.uniqueId), eq(0.0), eq("credits")) }
        }

        @Test
        fun `account change event is called when a deposit is made`() {
            // Given
            val player = server.addPlayer()
            every { repository.depositPlayer(eq(player.name), eq(player.uniqueId), eq(32.8), eq("credits")) } just Runs
            val withdrawingPrice = Price(32.8, "credits")
            val account = economyService.getAccount(player)

            // When
            account.deposit(withdrawingPrice)
            server.scheduler.waitAsyncTasksFinished()

            // Then
            server.pluginManager.assertEventFired(AccountChangeEvent::class.java)
            server.pluginManager.assertEventFired(AsyncAccountChangeEvent::class.java)
        }

        @Test
        fun `should throw exception when trying to deposit unknown currency`() {
            // Given
            val player = server.addPlayer()
            every {
                repository.depositPlayer(
                    eq(player.name),
                    eq(player.uniqueId),
                    eq(5.6),
                    eq("unknown")
                )
            } throws UnknownCurrencyException("unknown")
            val account = economyService.getAccount(player)

            // When / Then
            assertThrows<UnknownCurrencyException> { account.deposit(Price(5.6, "unknown")) }
        }
    }
}
