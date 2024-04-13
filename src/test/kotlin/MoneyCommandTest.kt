import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import co.aikar.commands.BukkitCommandManager
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.controller.MoneyCommand
import fr.pickaria.emerald.domain.Account
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.UnknownCurrencyException
import io.mockk.every
import io.mockk.mockk
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class MoneyCommandTest {
    private val service = mockk<EconomyService>()
    private lateinit var server: ServerMock
    private lateinit var plugin: JavaPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()

        val manager = BukkitCommandManager(plugin)
        manager.registerCommand(MoneyCommand(manager, service))
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `player should see their balance when performing money command`() {
        // Given
        val player = server.addPlayer()
        player.addAttachment(plugin).setPermission("pickaria.balance", true)
        player.recalculatePermissions()

        val account = mockk<Account>()
        val price = Price(5.6, "credits")
        every { service.getAccount(player) } returns account
        every { account.getBalance("credits") } returns price
        every { service.format(price) } returns "5.6 crédits"

        // When
        player.performCommand("money")

        // Then
        val message = player.nextMessage()
        assertContains(message!!, "5.6 crédits")
    }

    @Test
    fun `error message should be sent if the player asks for an unknown currency`() {
        // Given
        val player = server.addPlayer()
        player.addAttachment(plugin).setPermission("pickaria.balance", true)
        player.recalculatePermissions()

        every { service.getAccount(player) } throws UnknownCurrencyException("unknown")

        // When
        player.performCommand("money unknown")

        // Then
        val message = player.nextMessage()
        assertContains(message!!, "Le compte 'unknown' n'existe pas.")
    }
}