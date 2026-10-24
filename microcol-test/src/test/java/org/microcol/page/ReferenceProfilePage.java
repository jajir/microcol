package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Player;
import org.microcol.model.PlayerKing;
import org.microcol.model.campaign.FreePlayMap;
import org.microcol.model.campaign.ReferenceExpedition;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.mockito.Mockito;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Labeled;

/**
 * Prepares a reference position save and observes its profile after loading
 * (R-0208). The verifications read the loaded model and the game difficulty
 * label; they never change the game.
 */
public final class ReferenceProfilePage extends AbstractScreen {
    public ReferenceProfilePage(final TestContext context) {
        super(context);
    }

    /**
     * Writes the reference position of one nation and difficulty, built by the
     * free-play builder on the bundled reference map, to a new temporary save
     * and makes it the file that the Load button opens.
     */
    public void chooseReferencePosition(final EuropeanNation nation,
            final Difficulty difficulty) throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo game = dao
                .loadFromClassPath(FreePlayMap.REFERENCE.getResource());
        game.setModel(ReferenceExpedition.create(game.getModel().getMap(),
                nation, difficulty));
        final File file = Files.createTempFile(
                "reference-" + nation.name() + "-" + difficulty.name() + "-",
                ".microcol").toFile();
        file.deleteOnExit();
        dao.saveToFile(file.getAbsolutePath(), game);
        Mockito.when(getContext().getClassFromGuice(FileSelectingService.class)
                .loadFile(Mockito.any(File.class))).thenReturn(file);
    }

    public void verifyProfile(final EuropeanNation nation,
            final Difficulty difficulty, final String difficultyLabel,
            final int gold) throws Exception {
        // Computer nations and Crowns before the human act on their own, so
        // the profile is read only once the human's turn has begun.
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                () -> getModel().isGameRunning()
                        && getModel().getCurrentPlayer() == getModel()
                                .getPlayerByName(nation.getPlayerName()));
        // The difficulty label is set on the FX thread when a turn starts.
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(difficulty, getModel().getDifficulty());
        final Labeled label = getLabeledById("gameDifficulty");
        assertTrue(label.isVisible());
        assertTrue(label.getText().contains(difficultyLabel), label.getText());
        assertEquals(8, getModel().getPlayers().size());
        for (final EuropeanNation colonial : EuropeanNation.values()) {
            final Player player = getModel()
                    .getPlayerByName(colonial.getPlayerName());
            assertEquals(colonial == nation, player.isHuman());
            assertEquals(colonial != nation, player.isComputer());
            final PlayerKing crown = (PlayerKing) getModel()
                    .getPlayerByName(colonial.getPlayerName() + "'s King");
            assertSame(player, crown.getWhosKingThisPlayerIs());
        }
        assertEquals(gold,
                getModel().getPlayerByName(nation.getPlayerName()).getGold());
    }

    public void verifyPlayedTurns(final int expectedTurns) {
        assertEquals(expectedTurns,
                getModel().getCalendar().getNumberOfPlayedTurns());
    }
}
