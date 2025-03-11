import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lauriethefish.betterportals.api.PortalDirection;
import com.lauriethefish.betterportals.api.PortalPosition;
import com.lauriethefish.betterportals.bukkit.portal.predicate.ActivationDistance;
import com.lauriethefish.betterportals.bukkit.portal.predicate.PermissionsChecker;
import implementations.TestConfigHandler;
import implementations.TestLoggerModule;
import implementations.TestPortal;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PortalPredicateTests {
    private Injector injector;
    private PlayerMock player;
    private TestPortal portal;
    private WorldMock overworld;
    private WorldMock nether;

    @BeforeEach
    public void setup() {
        ServerMock server = MockBukkit.mock();
        overworld = server.addSimpleWorld("world");
        nether = server.addSimpleWorld("world_nether");

        injector = Guice.createInjector(
                new TestLoggerModule()
        );
        TestConfigHandler.prepareConfig(injector);

        player = server.addPlayer();

        PortalPosition originPos = new PortalPosition(new Location(overworld, 0, 64, 0), PortalDirection.EAST);
        PortalPosition destPos = new PortalPosition(new Location(nether, 1000, 64, 0), PortalDirection.EAST);

        portal = new TestPortal(originPos, destPos, new Vector(3.0, 3.0, 0.0), true, UUID.randomUUID(), null, null, true);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    // TODO: Some tests are temporarily disabled as MockBukkit is not yet up to date for 1.18

    //@Test
    public void testViewDistance() {
        ActivationDistance predicate = injector.getInstance(ActivationDistance.class);

        // Portal activation distance is 20 blocks, make sure that only portals inside that are activated
        player.setLocation(new Location(overworld, 0, 64, 0));
        assertTrue(predicate.test(portal, player));

        player.setLocation(new Location(overworld, 21, 64, 0));
        assertFalse(predicate.test(portal, player));

        // Positions in other worlds should always return false, regardless of if the absolute position is closer
        player.setLocation(new Location(nether, 0, 64, 0));
        assertFalse(predicate.test(portal, player));
    }

    //@Test
    public void testViewPermissions() {
        // This is done with operator status for now, until I can figure out how to make MockBukkit add/revoke permissions.
        PermissionsChecker predicate = new PermissionsChecker("betterportals.see");
        player.setOp(true);
        assertTrue(predicate.test(portal, player));
        player.setOp(false);
        assertFalse(predicate.test(portal, player));
    }
}
