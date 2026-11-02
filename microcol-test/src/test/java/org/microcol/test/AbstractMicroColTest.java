package org.microcol.test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.microcol.MicroCol;
import org.microcol.MicroColApplication;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Model;
import org.microcol.page.PlayerSaveFixture;
import org.microcol.page.TestContext;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.util.Modules;

import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class AbstractMicroColTest {

    private final static String DEFAULT_DIRECTORY_WITH_TEST_SCENARIOS = "src/test/scenarios/";

    private final Logger logger = LoggerFactory
            .getLogger(AbstractMicroColTest.class);

    private TestContext context;

    AbstractMicroColTest(final String directoryWithTestScenarios) {
        /*
         * Each test gets writable copies of the fixture settings. Running the
         * game must not change source fixtures or the player's personal
         * configuration.
         */
        final Path settings = Path.of(
                Preconditions.checkNotNull(directoryWithTestScenarios),
                ".microcol");
        try {
            final Path homes = Files
                    .createDirectories(Path.of("target", "test-homes"));
            final Path home = Files.createTempDirectory(homes,
                    getClass().getSimpleName() + "-");
            final Path targetSettings = Files
                    .createDirectories(home.resolve(".microcol"));
            try (Stream<Path> files = Files.list(settings)) {
                for (Path file : (Iterable<Path>) files::iterator) {
                    Files.copy(file,
                            targetSettings.resolve(file.getFileName()));
                }
            }
            System.setProperty("user.home", home.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to prepare isolated test settings", e);
        }
    }

    AbstractMicroColTest() {
        this(DEFAULT_DIRECTORY_WITH_TEST_SCENARIOS);
    }

    @BeforeAll
    static void configureJavaFx() {
        MicroCol.configureJavaFx();
    }

    @BeforeEach
    void beforeEach(final FxRobot robot) {
        getContext().setRobot(robot);
        robot.interact(() -> getPrimaryStage().requestFocus());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @org.junit.jupiter.api.AfterEach
    void stopApplication() {
        if (context != null) {
            // A scenario that failed while a dialog was open leaves that modal
            // window over the primary stage, which the next scenario reuses.
            // It is closed first, so its waiting caller finishes before the
            // game stops and the next scenario starts unobstructed.
            context.getRobot()
                    .interact(() -> new ArrayList<>(Window.getWindows())
                            .stream()
                            .filter(window -> window instanceof Stage
                                    && window != getPrimaryStage()
                                    && window.isShowing())
                            .forEach(window -> ((Stage) window).close()));
            try {
                context.getRobot().interact(() -> getMicroCol().stop());
            } finally {
                // JUnit/TestFX may retain the test instance after its method
                // ends.
                context = null;
            }
        }
    }

    /**
     * Method that initialize MicroCol game.
     * <p>
     * MicroCol will by started in development mode and with fake file loading
     * service.
     * </p>
     *
     * @param primaryStage       required primary stage
     * @param clazz              required class of junit test
     * @param microcolFileToLoad required file which will be loaded in game
     * @throws Exception
     */
    protected void initialize(final Stage primaryStage, final Class<?> clazz,
            final File microcolFileToLoad) throws Exception {
        initialize(primaryStage, clazz, binder -> {
            binder.bind(CursorService.class)
                    .toInstance(new CursorServiceNoOpp());
            FileSelectingService fileSelectingService = Mockito
                    .mock(FileSelectingService.class);
            Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class)))
                    .thenReturn(microcolFileToLoad);
            binder.bind(FileSelectingService.class)
                    .toInstance(fileSelectingService);
            bind(binder);
        });
    }

    /**
     * Method that initialize MicroCol game.
     * <p>
     * MicroCol will by started in development mode and with fake file loading
     * service.
     * </p>
     *
     * @param primaryStage    required primary stage
     * @param clazz           required class of junit test
     * @param overridenModule guice module where should be overridden MicroCol
     *                        services
     * @throws Exception
     */
    protected void initialize(final Stage primaryStage, final Class<?> clazz,
            final Module overridenModule) throws Exception {
        logger.info("Starting MicroCol UI test " + clazz.getName());
        // Tests use loaded styles, not development-time file watching.
        System.setProperty("microcol.test.disableCssReload", "true");
        System.setProperty(GamePreferences.SYSTEM_PROPERTY_DEVELOPMENT,
                Boolean.TRUE.toString());
        // Fixtures stub the bound chooser; the game sees what it chooses as a
        // player would, with authored scenarios saved as player saves.
        final FileSelectingService stubbed = boundChooser(overridenModule);
        final Module module = stubbed == null ? overridenModule
                : Modules.override(overridenModule)
                        .with(binder -> binder.bind(FileSelectingService.class)
                                .toInstance(playerChooser(stubbed)));
        final MicroColApplication microCol = new MicroColApplication(module);
        microCol.start(primaryStage);
        context = new TestContext(microCol, primaryStage);
        context.useStubbedChooser(stubbed);
    }

    /**
     * @return the chooser instance the test module binds, or <code>null</code>
     */
    private static FileSelectingService boundChooser(final Module module) {
        for (final Element element : Elements.getElements(module)) {
            if (element instanceof InstanceBinding
                    && ((InstanceBinding<?>) element).getKey()
                            .equals(Key.get(FileSelectingService.class))) {
                return (FileSelectingService) ((InstanceBinding<?>) element)
                        .getInstance();
            }
        }
        return null;
    }

    /**
     * A chooser forwarding every call to the stubbed one; a chosen file to load
     * reaches the game through {@link PlayerSaveFixture#asPlayerSave(File)}.
     */
    private static FileSelectingService playerChooser(
            final FileSelectingService stubbed) {
        return Mockito.mock(FileSelectingService.class, invocation -> {
            final Object answer;
            try {
                answer = invocation.getMethod().invoke(stubbed,
                        invocation.getArguments());
            } catch (final InvocationTargetException failed) {
                throw failed.getCause();
            }
            return "loadFile".equals(invocation.getMethod().getName())
                    ? PlayerSaveFixture.asPlayerSave((File) answer)
                    : answer;
        });
    }

    /**
     * Allows to custom guice in every test case.
     *
     * @param binder required guice binder
     */
    protected void bind(final Binder binder) {
        // by default do nothing
    }

    public MicroColApplication getMicroCol() {
        return context.getMicroCol();
    }

    public Stage getPrimaryStage() {
        return context.getPrimaryStage();
    }

    public TestContext getContext() {
        return context;
    }

    public NodeFinder getNodeFinder() {
        return FxAssert.assertContext().getNodeFinder();
    }

    public Model getModel() {
        return getContext().getModel();
    }

}
