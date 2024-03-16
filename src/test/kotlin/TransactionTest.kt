import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.*
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TransactionTest {
    private val repository = mockk<EconomyRepository>()
    private lateinit var service: EconomyService
    private lateinit var server: ServerMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        service = PaperEconomyService(repository)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `should make transaction`() {
        // Given
        val sender = server.addPlayer()
        val recipient = server.addPlayer()
        val amount = Price(5.6, "credits")
        val transaction = Transaction(sender, recipient, amount)
        coEvery { repository.getBalance(sender.uniqueId, "credits") } returns 8.0
        coEvery { repository.withdrawPlayer(sender.uniqueId, 5.6, "credits") } just Runs
        coEvery { repository.depositPlayer(recipient.uniqueId, 5.6, "credits") } just Runs

        // When
        service.makeTransaction(transaction)

        // Then
        coVerify { repository.depositPlayer(recipient.uniqueId, 5.6, "credits") }
    }

    @Test
    fun `should throw exception when amount is invalid`() {
        // Given
        val sender = server.addPlayer()
        val recipient = server.addPlayer()
        val amount = Price(0.0, "credits")
        val transaction = Transaction(sender, recipient, amount)
        coEvery { repository.getBalance(sender.uniqueId, "credits") } returns 8.0
        coEvery { repository.withdrawPlayer(sender.uniqueId, 0.0, "credits") } throws InvalidAmountException()

        // When / Then
        assertThrows<InvalidAmountException> { service.makeTransaction(transaction) }
    }

    @Test
    fun `should throw exception when currency is unknown`() {
        // Given
        val sender = server.addPlayer()
        val recipient = server.addPlayer()
        val amount = Price(5.6, "unknown")
        val transaction = Transaction(sender, recipient, amount)
        coEvery { repository.getBalance(sender.uniqueId, "unknown") } returns 8.0
        coEvery {
            repository.withdrawPlayer(
                sender.uniqueId,
                5.6,
                "unknown"
            )
        } throws UnknownCurrencyException("unknown")

        // When / Then
        assertThrows<UnknownCurrencyException> { service.makeTransaction(transaction) }
    }

    @Test
    fun `should throw exception when balance is insufficient`() {
        // Given
        val sender = server.addPlayer()
        val recipient = server.addPlayer()
        val amount = Price(5.6, "credits")
        val transaction = Transaction(sender, recipient, amount)
        coEvery { repository.getBalance(sender.uniqueId, "credits") } returns 3.0
        coEvery { repository.withdrawPlayer(sender.uniqueId, 5.6, "credits") } throws BalanceInsufficientException()

        // When / Then
        assertThrows<BalanceInsufficientException> { service.makeTransaction(transaction) }
    }

    @Test
    fun `should throw exception when sender is the recipient`() {
        // Given
        val sender = server.addPlayer()
        val amount = Price(5.6, "credits")
        val transaction = Transaction(sender, sender, amount)

        // When / Then
        assertThrows<SenderCannotBeRecipientException> { service.makeTransaction(transaction) }
    }
}