import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.controller.PayCommand
import fr.pickaria.emerald.domain.BalanceInsufficientException
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.SenderCannotBeRecipientException
import fr.pickaria.emerald.domain.Transaction
import io.mockk.*
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertContains

class PayCommandTest {
    private val service = mockk<EconomyService>()
    private lateinit var server: ServerMock
    private lateinit var plugin: JavaPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()

        val messager = Messager()
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS) {
            val commands = it.registrar()
            PayCommand(economyService = service, messager = messager).register(commands)
        }
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
        sender.addAttachment(plugin).setPermission("pickaria.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(5.0, "credits")
        val transaction = mockk<Transaction>()
        every { transaction.perform() } just Runs
        every { service.createTransaction(sender.uniqueId, recipient.uniqueId, priceToTransfer) } returns transaction
        every { service.format(priceToTransfer) } returns "5.0 crédits"

        // When
        sender.performCommand("pay ${recipient.name} 5.0")

        // Then
        coVerify { transaction.perform() }
        assertContains(sender.nextMessage()!!, "pay.sent")
        assertContains(recipient.nextMessage()!!, "pay.received")
    }

    @Test
    fun `decimals should be rounded to 2 when performing pay command`() {
        // Given
        val sender = server.addPlayer()
        val recipient = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(3.14, "credits")
        val transaction = mockk<Transaction>()
        every { transaction.perform() } just Runs
        every { service.createTransaction(sender.uniqueId, recipient.uniqueId, priceToTransfer) } returns transaction
        every { service.format(priceToTransfer) } returns "3.14 crédits"

        // When
        sender.performCommand("pay ${recipient.name} 3.14159")

        // Then
        coVerify { transaction.perform() }
        assertContains(sender.nextMessage()!!, "pay.sent")
        assertContains(recipient.nextMessage()!!, "pay.received")
    }

    @Test
    fun `transfer should not happen if sender does not have enough money`() {
        // Given
        val sender = server.addPlayer()
        val recipient = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(5.0, "credits")
        val transaction = mockk<Transaction>()
        every { transaction.perform() } throws BalanceInsufficientException()

        every { service.createTransaction(sender.uniqueId, recipient.uniqueId, priceToTransfer) } returns transaction

        // When
        sender.performCommand("pay ${recipient.name} 5.0")

        // Then
        assertContains(sender.nextMessage()!!, "pay.balance.insufficient")
    }

    @Test
    fun `should do nothing when player tries to send money to themselves`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(5.0, "credits")
        val transaction = mockk<Transaction>()
        every { transaction.perform() } throws SenderCannotBeRecipientException()

        every { service.createTransaction(sender.uniqueId, sender.uniqueId, priceToTransfer) } returns transaction

        // When
        sender.performCommand("pay ${sender.name} 5.0")

        // Then
        assertContains(sender.nextMessage()!!, "pay.cannot.self")
    }

    @Test
    fun `should do nothing when player tries to send an amount of 0`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.pay", true)
        sender.recalculatePermissions()
        val recipient = server.addPlayer()

        // When
        sender.performCommand("pay ${recipient.name} 0.0")

        // Then
        verify { service.getAccount(any<OfflinePlayer>()) wasNot Called }
        verify { service.getAccount(any<UUID>()) wasNot Called }
        verify { service.getAccount(any<Player>()) wasNot Called }
        verify { service.getAccount(any<String>()) wasNot Called }
    }

    @Test
    fun `should do nothing when player tries to send a negative amount`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.pay", true)
        sender.recalculatePermissions()
        val recipient = server.addPlayer()

        // When
        sender.performCommand("pay ${recipient.name} -50.0")

        // Then
        verify { service.getAccount(any<OfflinePlayer>()) wasNot Called }
        verify { service.getAccount(any<UUID>()) wasNot Called }
        verify { service.getAccount(any<Player>()) wasNot Called }
        verify { service.getAccount(any<String>()) wasNot Called }
    }
}
