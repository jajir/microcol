package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.BaseScore;
import org.microcol.model.Calendar;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.ResultScore;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for the free-play score boundaries (R-0193). It
 * derives prepared copies of the shared free-play scenario, so the authored
 * file keeps its content. Both copies carry the explicit scored calendar. The
 * first stands one round before spring 1800. The second is an authored valid
 * saved state one round before spring 1850: the Dutch have already continued
 * without scoring, and the frozen result carries the components that the model
 * computed for the nation at the earlier date. No 200-round play is needed to
 * reach it.
 */
final class ScoreBoundaryFixture {

    /** Winter 1599: the next round ends at spring 1600. */
    static final int WINTER_1599 = 109;

    /** Spring 1600: the round after winter 1599 (R-0193). */
    static final int SPRING_1600 = 110;

    /** Winter 1799: the next round ends at spring 1800. */
    static final int WINTER_1799 = 909;

    /** Winter 1849: the next round ends at spring 1850. */
    static final int WINTER_1849 = 1109;

    /** Identity of the authored frozen result of the second copy. */
    static final String AUTHORED_IDENTITY = "f193-authored-frozen-result";

    private static final String DUTCH = "Dutch";

    private static final String FREE_PLAY = "/maps/free-play.microcol";

    private ScoreBoundaryFixture() {
    }

    /** Write a copy one round before the frozen score of spring 1800. */
    static void prepareBeforeBoundary(final TestContext context,
            final File source, final String name) throws IOException {
        final JsonObject saved = scored(source, WINTER_1799);
        final File prepared = write(saved, name);
        select(context, prepared);
    }

    /**
     * Write a started copy of the shipped free-play map, one round before
     * spring 1600, with the Dutch on turn. The shipped map is not started and
     * its first nation is the computer France, so a plain load would not reach
     * the human; the saved state is the one a game has after France has played
     * and the Dutch are current. The Dutch own a galleon and no colony, so the
     * round that ends winter 1599 leaves the human with none at spring 1600.
     * The shipped map itself is not changed.
     */
    static void prepareNoColonyBeforeBoundary(final TestContext context,
            final String name) throws IOException {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromClassPath(FREE_PLAY);
        final ModelPo model = data.getModel();
        model.getCalendar().setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        model.getCalendar().setScoredPlay(true);
        model.getCalendar().setNumberOfPlayedTurns(WINTER_1599);
        model.getGameManager().setGameStarted(true);
        model.getGameManager().setCurrentPlayer(DUTCH);
        final File prepared = new File(System.getProperty("user.home"), name);
        dao.saveToFile(prepared.getPath(), data);
        select(context, prepared);
    }

    /**
     * Write a copy one round before the end of free play, with the unscored
     * continuation and its frozen result already recorded.
     */
    static void prepareBeforeEnd(final TestContext context, final File source,
            final String name) throws IOException {
        // The components are those of the nation one round before 1800.
        final JsonObject saved = scored(source, WINTER_1799);
        final File earlier = write(saved, name);
        final JsonObject frozen = frozenOf(earlier);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonObject("gameManager").add("gameResult", frozen);
        model.getAsJsonObject("calendar").addProperty("numberOfPlayedTurns",
                WINTER_1849);
        final File prepared = write(saved, name);
        select(context, prepared);
    }

    private static JsonObject scored(final File source, final int turns)
            throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject calendar = saved.getAsJsonObject("model")
                .getAsJsonObject("calendar");
        calendar.addProperty("numberOfPlayedTurns", turns);
        calendar.addProperty("endYear", Calendar.YEAR_OF_FREE_PLAY_END);
        calendar.addProperty("scoredPlay", true);
        return saved;
    }

    private static File write(final JsonObject saved, final String name)
            throws IOException {
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        return prepared;
    }

    private static void select(final TestContext context, final File prepared) {
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    /**
     * The saved unscored continuation of the Dutch, from the components the
     * model reads for the nation in the given valid saved game.
     */
    private static JsonObject frozenOf(final File valid) {
        final Model model = Model.make(
                new GameModelPoDao().loadFromFile(valid).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player dutch = model.getPlayerByName(DUTCH);
        final BaseScore base = dutch.getBaseScore();
        final ResultScore result = dutch.getResultScore();
        final JsonObject frozen = new JsonObject();
        frozen.addProperty("identity", AUTHORED_IDENTITY);
        frozen.addProperty("status", "CONTINUED_UNSCORED");
        frozen.addProperty("playerName", DUTCH);
        frozen.addProperty("servants", base.getServants());
        frozen.addProperty("colonists", base.getColonists());
        frozen.addProperty("experts", base.getExperts());
        frozen.addProperty("fathers", base.getFathers());
        frozen.addProperty("gold", base.getGold());
        frozen.addProperty("supportPercent", base.getSupportPercent());
        frozen.addProperty("destroyedSettlements",
                result.getDestroyedSettlements());
        frozen.addProperty("bellsBeyondTarget", result.getBellsAfterTarget());
        frozen.addProperty("total", result.getTotal());
        return frozen;
    }
}
