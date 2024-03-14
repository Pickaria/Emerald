import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import co.aikar.commands.BukkitCommandManager
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.controller.PayCommand
import fr.pickaria.emerald.domain.Account
import fr.pickaria.emerald.domain.BalanceInsufficientException
import fr.pickaria.emerald.domain.Price
import io.mockk.*
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PayCommandTest {
    private val service = mockk<EconomyService>()
    private lateinit var server: ServerMock
    private lateinit var plugin: JavaPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()

        val manager = BukkitCommandManager(plugin)
        manager.registerCommand(PayCommand(service))
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `player should see their balance when performing money command`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.command.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(5.0, "credits")

        val senderAccount = mockk<Account>()
        coEvery { service.getAccount(sender) } returns senderAccount
        coEvery { senderAccount.withdraw(priceToTransfer) } just Runs

        val recipient = server.addPlayer()
        val recipientAccount = mockk<Account>()
        coEvery { service.getAccount(recipient) } returns recipientAccount
        coEvery { recipientAccount.deposit(priceToTransfer) } just Runs

        coEvery { service.format(priceToTransfer) } returns "5.0 crédits"

        // When
        sender.performCommand("pay ${recipient.name} 5.0")

        // Then
        coVerify { senderAccount.withdraw(priceToTransfer) }
        coVerify { recipientAccount.deposit(priceToTransfer) }
    }

    @Test
    fun `transfer should not happen if sender does not have enough money`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.command.pay", true)
        sender.recalculatePermissions()

        val priceToTransfer = Price(5.0, "credits")

        val senderAccount = mockk<Account>()
        coEvery { service.getAccount(sender) } returns senderAccount
        coEvery { senderAccount.withdraw(priceToTransfer) } throws BalanceInsufficientException()

        val recipient = server.addPlayer()

        coEvery { service.format(priceToTransfer) } returns "5.0 crédits"

        // When
        sender.performCommand("pay ${recipient.name} 5.0")

        // Then
        verify { service.getAccount(recipient) wasNot Called }
    }

    @Test
    fun `should do nothing when player tries to send money to themselves`() {
        // Given
        val sender = server.addPlayer()
        sender.addAttachment(plugin).setPermission("pickaria.command.pay", true)
        sender.recalculatePermissions()

        // When
        sender.performCommand("pay ${sender.name} 5.0")

        // Then
        verify { service.getAccount(sender) wasNot Called }
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
        verify { service.getAccount(sender) wasNot Called }
    }
}