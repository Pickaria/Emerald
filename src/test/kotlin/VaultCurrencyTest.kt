import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.controller.vault.VaultCurrency
import fr.pickaria.emerald.domain.Account
import fr.pickaria.emerald.domain.BalanceInsufficientException
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import io.mockk.*
import mocks.creditConfigMock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultCurrencyTest {
    private val repository = mockk<EconomyRepository>()
    private val service = mockk<EconomyService>()
    private lateinit var server: ServerMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        coEvery { repository.getConfig("credits") } returns creditConfigMock
        coEvery { repository.getConfig("unknown") } throws UnknownCurrencyException("unknown")
        coEvery { service.getAvailableCurrencyNames() } returns setOf("credits")
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `should return failure if currency is unknown`() {
        // Given / When / Then
        assertThrows<UnknownCurrencyException> { VaultCurrency(repository, service, "unknown") }
    }

    @Test
    fun `should check if account exists`() {
        // Given
        val player = server.addPlayer()
        val vaultCurrency = VaultCurrency(repository, service, "credits")

        // When
        val actual = vaultCurrency.hasAccount(player)

        // Then
        assertTrue(actual)
    }

    @Test
    fun `should format price`() {
        // Given
        val vaultCurrency = VaultCurrency(repository, service, "credits")
        coEvery { service.format(Price(5.6, "credits")) } returns "5.6 crédits"

        // When
        val actual = vaultCurrency.format(5.6)

        // Then
        assertEquals("5.6 crédits", actual)
    }

    @Test
    fun `should get currency name`() {
        // Given
        val vaultCurrency = VaultCurrency(repository, service, "credits")

        // When
        val actual = vaultCurrency.name

        // Then
        assertEquals("credits", actual)
    }

    @Test
    fun `should call service when getting balance`() {
        // Given
        val player = server.addPlayer()
        val vaultCurrency = VaultCurrency(repository, service, "credits")
        val mockAccount = mockk<Account>()
        coEvery { service.getAccount(player) } returns mockAccount
        coEvery { mockAccount.getBalance("credits") } returns Price(5.6, "credits")

        // When
        val balance = vaultCurrency.getBalance(player)

        // Then
        assertEquals(5.6, balance)
    }

    @Test
    fun `should call service when depositing`() {
        // Given
        val player = server.addPlayer()
        val vaultCurrency = VaultCurrency(repository, service, "credits")
        val mockAccount = mockk<Account>()
        coEvery { service.getAccount(player) } returns mockAccount
        coEvery { mockAccount.deposit(Price(5.6, "credits")) } just Runs
        coEvery { mockAccount.getBalance("credits") } returns Price(5.6, "credits")

        // When
        val economyResponse = vaultCurrency.depositPlayer(player, 5.6)

        // Then
        assertTrue(economyResponse.transactionSuccess())
        coVerify { mockAccount.deposit(Price(5.6, "credits")) }
    }

    @Test
    fun `should call service when withdrawing`() {
        // Given
        val player = server.addPlayer()
        val vaultCurrency = VaultCurrency(repository, service, "credits")
        val mockAccount = mockk<Account>()
        coEvery { service.getAccount(player) } returns mockAccount
        coEvery { mockAccount.withdraw(Price(5.6, "credits")) } just Runs
        coEvery { mockAccount.getBalance("credits") } returns Price(5.6, "credits")

        // When
        val economyResponse = vaultCurrency.withdrawPlayer(player, 5.6)

        // Then
        assertTrue(economyResponse.transactionSuccess())
        coVerify { mockAccount.withdraw(Price(5.6, "credits")) }
    }

    @Test
    fun `should get balance of player`() {
        // Given
        val player = server.addPlayer()
        val vaultCurrency = VaultCurrency(repository, service, "credits")
        val mockAccount = mockk<Account>()
        coEvery { service.getAccount(player) } returns mockAccount
        coEvery { mockAccount.getBalance("credits") } returns Price(5.6, "credits")

        // When
        val balance = vaultCurrency.getBalance(player)

        // Then
        assertEquals(5.6, balance)
    }

    @Test
    fun `player should have enough money`() {
        // Given
        val player = server.addPlayer()
        val vaultCurrency = VaultCurrency(repository, service, "credits")
        val mockAccount = mockk<Account>()
        coEvery { service.getAccount(player) } returns mockAccount
        coEvery { mockAccount.getBalance("credits") } returns Price(5.6, "credits")

        // When
        val economyResponse = vaultCurrency.has(player, 5.0)

        // Then
        assertTrue(economyResponse)
    }

    @Test
    fun `player should not have enough money`() {
        // Given
        val player = server.addPlayer()
        val vaultCurrency = VaultCurrency(repository, service, "credits")
        val mockAccount = mockk<Account>()
        coEvery { service.getAccount(player) } returns mockAccount
        coEvery { mockAccount.getBalance("credits") } returns Price(5.6, "credits")

        // When
        val economyResponse = vaultCurrency.has(player, 42.0)

        // Then
        assertFalse(economyResponse)
    }

    @Test
    fun `transaction should not be successful if player does not have enough money when withdrawing`() {
        // Given
        val player = server.addPlayer()
        val vaultCurrency = VaultCurrency(repository, service, "credits")
        val mockAccount = mockk<Account>()
        coEvery { service.getAccount(player) } returns mockAccount
        coEvery { mockAccount.withdraw(Price(5.6, "credits")) } throws BalanceInsufficientException()
        coEvery { mockAccount.getBalance("credits") } returns Price(0.0, "credits")

        // When
        val economyResponse = vaultCurrency.withdrawPlayer(player, 5.6)

        // Then
        assertFalse(economyResponse.transactionSuccess())
    }
}
