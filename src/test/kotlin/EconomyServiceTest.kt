import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import fr.pickaria.emerald.data.IEconomyRepository
import fr.pickaria.emerald.domain.Currencies
import fr.pickaria.emerald.domain.EconomyService
import fr.pickaria.emerald.domain.Price
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class EconomyServiceTest {
    private val repository = mockk<IEconomyRepository<Currencies>>()

    private var server: ServerMock? = null
    private var economyService: EconomyService? = null

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        economyService = EconomyService(repository)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `should call economy repository with appropriate user's data`() {
        // Given
        val player = server!!.addPlayer()
        coEvery { repository.getBalance(player.uniqueId, Currencies.Credits) } returns 5.6
        val expectedPrice = Price(5.6, Currencies.Credits)

        // When
        val balance = economyService!!.getBalance(player, Currencies.Credits)

        // Then
        assertEquals(expectedPrice, balance)
    }
}