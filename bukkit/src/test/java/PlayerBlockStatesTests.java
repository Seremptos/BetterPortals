import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.lauriethefish.betterportals.bukkit.block.IViewableBlockInfo;
import com.lauriethefish.betterportals.bukkit.block.IMultiBlockChangeManager;
import com.lauriethefish.betterportals.bukkit.player.view.block.IPlayerBlockStates;
import com.lauriethefish.betterportals.bukkit.player.view.block.PlayerBlockStates;
import com.lauriethefish.betterportals.shared.util.ReflectionUtil;
import implementations.TestLoggerModule;
import implementations.TestMultiBlockChangeManager;
import implementations.TestViewableBlockInfo;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerBlockStatesTests {
    private static final Field VIEWED_STATES_FIELD = ReflectionUtil.findField(PlayerBlockStates.class, "viewedStates");

    private IPlayerBlockStates blockView;
    private final Vector position = new Vector(0.0, 1.0, 0.0);

    @BeforeEach
    public void setUp() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new FactoryModuleBuilder().implement(
                        IMultiBlockChangeManager.class, TestMultiBlockChangeManager.class)
                        .build(IMultiBlockChangeManager.Factory.class));

                install(new FactoryModuleBuilder().implement(
                        IPlayerBlockStates.class, PlayerBlockStates.class)
                        .build(IPlayerBlockStates.Factory.class));
                install(new TestLoggerModule());
            }
        });

        ServerMock server = MockBukkit.mock();
        PlayerMock player = server.addPlayer();

        blockView = injector.getInstance(IPlayerBlockStates.Factory.class).create(player);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    // TODO: Some tests are temporarily disabled as MockBukkit is not yet up to date for 1.18

    //@Test
    @SuppressWarnings("unchecked")
    public void setViewableTest() {
        IViewableBlockInfo blockInfo = new TestViewableBlockInfo();

        assertTrue(blockView.setViewable(position, blockInfo));

        Map<Vector, IViewableBlockInfo> viewedStates = (Map<Vector, IViewableBlockInfo>) ReflectionUtil.getField(blockView, VIEWED_STATES_FIELD);
        assertEquals(viewedStates.get(position), blockInfo);

        // Setting it to viewable twice shouldn't return true multiple times
        assertFalse(blockView.setViewable(position, blockInfo));
    }

    //@Test
    @SuppressWarnings("unchecked")
    public void setNotViewableTest() {
        IViewableBlockInfo blockInfo = new TestViewableBlockInfo();

        // Make it viewable, and then non-viewable again to test that it is removed from the map correctly
        assertTrue(blockView.setViewable(position, blockInfo));
        assertTrue(blockView.setNonViewable(position, blockInfo));

        Map<Vector, IViewableBlockInfo> viewedStates = (Map<Vector, IViewableBlockInfo>) ReflectionUtil.getField(blockView, VIEWED_STATES_FIELD);
        assertNull(viewedStates.get(position));

        // Setting it not viewable twice shouldn't return true multiple times
        assertFalse(blockView.setNonViewable(position, blockInfo));
    }
}
