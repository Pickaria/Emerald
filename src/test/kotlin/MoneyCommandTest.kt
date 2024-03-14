import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import co.aikar.commands.BukkitCommandManager
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.controller.MoneyCommand
import fr.pickaria.emerald.domain.Account
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.UnknownCurrencyException
import io.mockk.coEvery
import io.mockk.mockk
import net.kyori.adventure.text.TextComponent
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull

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
        player.addAttachment(plugin).setPermission("pickaria.command.balance", true)
        player.recalculatePermissions()

        val account = mockk<Account>()
        val price = Price(5.6, "credits")
        coEvery { service.getAccount(player) } returns account
        coEvery { service.format(price) } returns "5.6 crédits"
        coEvery { account.getBalance("credits") } returns price

        // When
        player.performCommand("money")

        // Then
        val message = player.nextComponentMessage()
        assertNotNull(message)
        assertContains((message as TextComponent).content(), "5.6 crédits")
    }

    @Test
    fun `error message should be sent if the player asks for an unknown currency`() {
        // Given
        val player = server.addPlayer()
        player.addAttachment(plugin).setPermission("pickaria.command.balance", true)
        player.recalculatePermissions()

        coEvery { service.getAccount(player) } throws UnknownCurrencyException("unknown")

        // When
        player.performCommand("money unknown")

        // Then
        val message = player.nextComponentMessage()
        assertNotNull(message)
        assertContains((message as TextComponent).content(), "Le compte 'unknown' n'existe pas.")
    }
}