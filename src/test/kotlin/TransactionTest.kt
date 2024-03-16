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
        val sender = mockk<Account>()
        val recipient = mockk<Account>()
        val amount = Price(5.6, "credits")
        val transaction = Transaction(sender, recipient, amount)
        coEvery { sender.owner } returns server.addPlayer()
        coEvery { recipient.owner } returns server.addPlayer()
        coEvery { sender.getBalance("credits") } returns Price(8.0, "credits")
        coEvery { sender.withdraw(Price(5.6, "credits")) } just Runs
        coEvery { recipient.deposit(Price(5.6, "credits")) } just Runs

        // When
        transaction.perform()

        // Then
        coVerify { recipient.deposit(Price(5.6, "credits")) }
    }

    @Test
    fun `should throw exception when amount is invalid`() {
        // Given
        val sender = mockk<Account>()
        val recipient = mockk<Account>()
        val amount = Price(0.0, "credits")
        val transaction = Transaction(sender, recipient, amount)
        coEvery { sender.owner } returns server.addPlayer()
        coEvery { recipient.owner } returns server.addPlayer()
        coEvery { sender.getBalance("credits") } returns Price(8.0, "credits")
        coEvery { sender.withdraw(Price(0.0, "credits")) } throws InvalidAmountException()

        // When / Then
        assertThrows<InvalidAmountException> { transaction.perform() }
    }

    @Test
    fun `should throw exception when currency is unknown`() {
        // Given
        val sender = mockk<Account>()
        val recipient = mockk<Account>()
        val amount = Price(5.6, "unknown")
        val transaction = Transaction(sender, recipient, amount)
        coEvery { sender.owner } returns server.addPlayer()
        coEvery { recipient.owner } returns server.addPlayer()
        coEvery { sender.getBalance("unknown") } returns Price(8.0, "credits")
        coEvery { sender.withdraw(Price(5.6, "unknown")) } throws UnknownCurrencyException("unknown")

        // When / Then
        assertThrows<UnknownCurrencyException> { transaction.perform() }
    }

    @Test
    fun `should throw exception when balance is insufficient`() {
        // Given
        val sender = mockk<Account>()
        val recipient = mockk<Account>()
        val amount = Price(5.6, "credits")
        val transaction = Transaction(sender, recipient, amount)
        coEvery { sender.owner } returns server.addPlayer()
        coEvery { recipient.owner } returns server.addPlayer()
        coEvery { sender.getBalance("credits") } returns Price(3.0, "credits")
        coEvery { sender.withdraw(Price(5.6, "credits")) } throws BalanceInsufficientException()

        // When / Then
        assertThrows<BalanceInsufficientException> { transaction.perform() }
    }

    @Test
    fun `should throw exception when sender is the recipient`() {
        // Given
        val sender = mockk<Account>()
        val amount = Price(5.6, "credits")
        val transaction = Transaction(sender, sender, amount)
        coEvery { sender.owner } returns server.addPlayer()

        // When / Then
        assertThrows<SenderCannotBeRecipientException> { transaction.perform() }
    }
}