import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.controller.CollectValuableListener
import fr.pickaria.emerald.domain.PaperEconomyService
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import mocks.creditConfigMock
import mocks.physicalCurrencyItemMock
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollectValuableTest {
    private val repository = mockk<EconomyRepository>()
    private lateinit var server: ServerMock
    private lateinit var service: PaperEconomyService
    private lateinit var plugin: JavaPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
        service = PaperEconomyService(repository)
        server.pluginManager.registerEvents(CollectValuableListener(service), plugin)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `should make deposit and removes the item when player left clicks on a valuable item`() {
        // Given
        val player = server.addPlayer()
        every { repository.getConfig("credits") } returns creditConfigMock
        every { repository.getBalance(player.uniqueId, "credits") } returns 5.0
        every { repository.depositPlayer(player.uniqueId, 5.0, "credits") } just Runs
        val valuableItem = physicalCurrencyItemMock(value = 1.0, count = 5, currency = "credits")
        player.inventory.setItem(0, valuableItem)

        // When
        val view = player.openInventory(player.inventory)!!
        player.simulateInventoryClick(view, ClickType.LEFT, 0)
        player.closeInventory()

        // Then
        val balance = service.getAccount(player).getBalance("credits")
        assertEquals(5.0, balance.amount)
        assertTrue(player.inventory.isEmpty)
    }

    @Test
    fun `should make deposit and removes the item when player right clicks on a valuable item`() {
        // Given
        val player = server.addPlayer()
        every { repository.getConfig("credits") } returns creditConfigMock
        every { repository.getBalance(player.uniqueId, "credits") } returns 5.0
        every { repository.depositPlayer(player.uniqueId, 5.0, "credits") } just Runs
        val valuableItem = physicalCurrencyItemMock(value = 1.0, count = 5, currency = "credits")
        player.inventory.setItem(0, valuableItem)

        // When
        val view = player.openInventory(player.inventory)!!
        player.simulateInventoryClick(view, ClickType.RIGHT, 0)
        player.closeInventory()

        // Then
        val balance = service.getAccount(player).getBalance("credits")
        assertEquals(5.0, balance.amount)
        assertTrue(player.inventory.isEmpty)
    }

    @Test
    fun `should do nothing when valuable item has an invalid value`() {
        // Given
        val player = server.addPlayer()
        every { repository.getConfig("credits") } returns creditConfigMock
        val valuableItem = physicalCurrencyItemMock(value = -10.0, count = 5, currency = "credits")
        player.inventory.setItem(0, valuableItem)

        // When
        val view = player.openInventory(player.inventory)!!
        player.simulateInventoryClick(view, ClickType.LEFT, 0)
        player.closeInventory()

        // Then
        assertEquals(Material.IRON_NUGGET, player.inventory.getItem(0)!!.type)
    }

    @Test
    fun `should do nothing when player do not clicks on valuable item`() {
        // Given
        val player = server.addPlayer()
        val normalItem = ItemStack(Material.IRON_NUGGET)
        player.inventory.setItem(0, normalItem)

        // When
        val view = player.openInventory(player.inventory)!!
        player.simulateInventoryClick(view, ClickType.LEFT, 0)
        player.closeInventory()

        // Then
        assertEquals(Material.IRON_NUGGET, player.inventory.getItem(0)!!.type)
    }
}
