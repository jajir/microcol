package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.ai.PassiveRivalPlayer;
import org.microcol.ai.RivalPlayerFactory;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ReferenceProfilePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * R-0208 / UC-003, UC-004 and UC-115, English cells TC_11 to TC_15 of the
 * twenty-cell profile matrix: England at each of the five difficulties starts
 * from its reference position, plays one actual owner turn, saves through the
 * menu and continues. The profile must still show the nation, the Crown, the
 * difficulty label, the gold and the date after each step. See
 * {@link TC_181_F208_reference_profile_matrix_test} for the reference position,
 * the expected gold and the split by nation.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_181_F208_reference_profile_matrix_english_test
        extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");

    /**
     * Starts the application with the usual test services and passive European
     * computer rivals, as in TC101. Each cell replaces the file chosen by the
     * Load button with its generated reference position.
     */
    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), binder -> {
            binder.bind(CursorService.class)
                    .toInstance(new CursorServiceNoOpp());
            final FileSelectingService selecting = Mockito
                    .mock(FileSelectingService.class);
            Mockito.when(selecting.loadFile(Mockito.any(File.class)))
                    .thenReturn(FIXTURE);
            binder.bind(FileSelectingService.class).toInstance(selecting);
            binder.bind(RivalPlayerFactory.class)
                    .toInstance(PassiveRivalPlayer::new);
        });
    }

    @Test
    @Tag("local")
    void TC_11_english_discoverer_profile_turn_and_continue() throws Exception {
        playReferenceProfile(EuropeanNation.ENGLISH, Difficulty.DISCOVERER,
                "Discoverer", 1000);
    }

    @Test
    @Tag("local")
    void TC_12_english_explorer_profile_turn_and_continue() throws Exception {
        playReferenceProfile(EuropeanNation.ENGLISH, Difficulty.EXPLORER,
                "Explorer", 750);
    }

    @Test
    @Tag("local")
    void TC_13_english_conquistador_profile_turn_and_continue()
            throws Exception {
        playReferenceProfile(EuropeanNation.ENGLISH, Difficulty.CONQUISTADOR,
                "Conquistador", 500);
    }

    @Test
    @Tag("local")
    void TC_14_english_governor_profile_turn_and_continue() throws Exception {
        playReferenceProfile(EuropeanNation.ENGLISH, Difficulty.GOVERNOR,
                "Governor", 250);
    }

    @Test
    @Tag("local")
    void TC_15_english_viceroy_profile_turn_and_continue() throws Exception {
        playReferenceProfile(EuropeanNation.ENGLISH, Difficulty.VICEROY,
                "Viceroy", 0);
    }

    private void playReferenceProfile(final EuropeanNation nation,
            final Difficulty difficulty, final String difficultyLabel,
            final int gold) throws Exception {
        final ReferenceProfilePage profile = new ReferenceProfilePage(
                getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());

        // Load the prepared reference position through the Load button.
        profile.chooseReferencePosition(nation, difficulty);
        final GamePage game = WelcomePage.of(getContext()).loadGame();

        // The new game starts in 1490 on the human's first turn.
        profile.verifyProfile(nation, difficulty, difficultyLabel, gold);
        profile.verifyPlayedTurns(0);
        audit.verifyDisplayedDate("AD 1490");

        // End the human turn; the computer nations and Crowns act, then the
        // next round starts. The profile and the gold must not change.
        game.nextTurnAndCloseDialogs();
        profile.verifyProfile(nation, difficulty, difficultyLabel, gold);
        profile.verifyPlayedTurns(1);
        audit.verifyDisplayedDate("AD 1491");

        // Leaving the game saves it; Continue reopens the same position.
        game.exitToMenu();
        audit.continueGame();
        profile.verifyProfile(nation, difficulty, difficultyLabel, gold);
        profile.verifyPlayedTurns(1);
        audit.verifyDisplayedDate("AD 1491");
    }
}
