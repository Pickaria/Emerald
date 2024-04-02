import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import co.aikar.commands.BukkitCommandManager
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.controller.PayCommand
import fr.pickaria.emerald.domain.BalanceInsufficientException
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.SenderCannotBeRecipientException
import fr.pickaria.emerald.domain.Transaction
import io.mockk.*
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class PayCommandTest {
    private val service = mockk<EconomyService>()
    private lateinit var server: ServerMock
    private lateinit var plugin: JavaPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()

        val manager = BukkitCommandManager(plugin)
        manager.registerCommand(PayCommand(service, "credits"))
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `transaction should be carried when performing pay command`() {
        // Given
        val sender = server.addPlayer()
        val recipient = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.command.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(5.0, "credits")
        val transaction = mockk<Transaction>()
        every { transaction.perform() } just Runs
        every { service.createTransaction(sender, recipient, priceToTransfer) } returns transaction
        every { service.format(priceToTransfer) } returns "5.0 crédits"

        // When
        sender.performCommand("pay ${recipient.name} 5.0")

        // Then
        coVerify { transaction.perform() }
        assertContains(sender.nextMessage()!!, "5.0 crédits")
        assertContains(recipient.nextMessage()!!, "5.0 crédits")
    }

    @Test
    fun `transfer should not happen if sender does not have enough money`() {
        // Given
        val sender = server.addPlayer()
        val recipient = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.command.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(5.0, "credits")
        val transaction = mockk<Transaction>()
        every { transaction.perform() } throws BalanceInsufficientException()

        every { service.createTransaction(sender, recipient, priceToTransfer) } returns transaction

        // When
        sender.performCommand("pay ${recipient.name} 5.0")

        // Then
        assertContains(sender.nextMessage()!!, "Tu n'as pas assez d'argent.")
    }

    @Test
    fun `should do nothing when player tries to send money to themselves`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.command.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(5.0, "credits")
        val transaction = mockk<Transaction>()
        every { transaction.perform() } throws SenderCannotBeRecipientException()

        every { service.createTransaction(sender, sender, priceToTransfer) } returns transaction

        // When
        sender.performCommand("pay ${sender.name} 5.0")

        // Then
        assertContains(sender.nextMessage()!!, "Tu ne peux pas envoyer de l'argent à toi-même.")
    }

    @Test
    fun `should do nothing when player tries to send an amount of 0`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.command.pay", true)
        sender.recalculatePermissions()
        val recipient = server.addPlayer()

        // When
        sender.performCommand("pay ${recipient.name} 0.0")

        // Then
        verify { service.getAccount(any()) wasNot Called }
    }

    @Test
    fun `should do nothing when player tries to send a negative amount`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.command.pay", true)
        sender.recalculatePermissions()
        val recipient = server.addPlayer()

        // When
        sender.performCommand("pay ${recipient.name} -50.0")

        // Then
        verify { service.getAccount(any()) wasNot Called }
    }
}