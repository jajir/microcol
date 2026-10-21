package org.microcol.test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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
import com.google.inject.Module;

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
            context.getRobot().interact(() -> getMicroCol().stop());
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
        System.setProperty(GamePreferences.SYSTEM_PROPERTY_DEVELOPMENT,
                Boolean.TRUE.toString());
        final MicroColApplication microCol = new MicroColApplication(
                overridenModule);
        microCol.start(primaryStage);
        context = new TestContext(microCol, primaryStage);
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
