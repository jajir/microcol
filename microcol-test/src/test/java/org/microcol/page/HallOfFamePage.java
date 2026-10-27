package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.microcol.model.GameResult;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The persistent Hall of Fame as the player meets it (R-0197): the ranking
 * opened from the main menu and from the statistics of a game, the window that
 * a scored game shows when it ends or spring 1800 freezes the score, the
 * current result with its breakdown whether or not it is ranked, and the saved
 * file the ranking is read from. The page reads what the screen shows and what
 * the file holds.
 */
public final class HallOfFamePage extends AbstractScreen {

    private static final String DIALOG = "hallOfFameDialog";

    private static final String ROW_STYLE = ".hall-of-fame-row";

    private static final String CURRENT = "hallOfFameCurrent";

    private static final String IDENTITY = "hallOfFameIdentity";

    private static final String STORAGE = "hallOfFameStorage";

    private static final String TAB = "hallOfFameTab";

    private static final Pattern POINTS = Pattern.compile("(\\d+) points");

    public HallOfFamePage(final TestContext context) {
        super(context);
    }

    private GameResult frozen() {
        return getModel().getFrozenResult().orElseThrow();
    }

    private static Path savedFile() {
        return Path.of(System.getProperty("user.home"), ".microcol",
                "hall-of-fame.json");
    }

    // --- player gestures ---------------------------------------------------

    /** Open the ranking from the main menu and wait for its window. */
    public void openFromMenu() throws Exception {
        clickOnButtonWithId("buttonHallOfFame");
        awaitResultDialog();
    }

    /** Wait until the Hall of Fame window is on screen. */
    public void awaitResultDialog() throws Exception {
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> getNodeFinder()
                .lookup("#" + DIALOG).tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the Hall of Fame window opened from the main menu. */
    public void closeMenuDialog() {
        closeResultDialog();
    }

    /** Close the Hall of Fame window, whichever screen opened it. */
    public void closeResultDialog() {
        clickOnButtonWithId("hallOfFameClose");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Open the statistics of the game and its Hall of Fame tab. */
    public void openFromStatistics() {
        new FreePlayAuditPage(getContext()).openStatistics();
        clickOnStatisticsTab(TAB);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Leave the statistics for the map.
     *
     * @return the game map
     */
    public GamePage closeStatistics() {
        return new BaseScorePage(getContext()).close();
    }

    // --- explicit observations --------------------------------------------

    /** The Hall of Fame window is open. */
    public void verifyResultDialogShown() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(isCssIdVisible(DIALOG), "Hall of Fame window shown");
    }

    /** The ranking shows exactly the given number of ranked rows. */
    public void verifyRankingRows(final int expected) {
        assertEquals(expected,
                getNodeFinder().lookup(ROW_STYLE).queryAll().size(),
                "Ranked rows");
    }

    /** Before spring 1800 no result is frozen, so none is shown as current. */
    public void verifyNoCurrentResult() {
        assertFalse(isCssIdVisible(CURRENT), "Current result shown");
        assertFalse(isCssIdVisible(IDENTITY), "Current identity shown");
    }

    /** The frozen result is ranked at the given place of the top ten. */
    public void verifyRankedAt(final int rank) {
        assertEquals("Your result is ranked " + rank + " in the top ten.",
                text(CURRENT));
    }

    /**
     * The frozen result is below the full top ten: the screen says so, and its
     * breakdown is still shown.
     */
    public void verifyNotRanked() {
        assertEquals(
                "Your result is not in the top ten. Its breakdown is shown below.",
                text(CURRENT));
    }

    /**
     * The first place names the nation that owns the frozen result, with the
     * total that was recorded for it.
     */
    public void verifyTopRowIsFrozenResult() {
        final String name = getModel().getPlayerByName(frozen().getPlayerName())
                .getDisplayName();
        final String row = text("hallOfFameRank1");
        assertTrue(
                row.startsWith("1. " + name + " - "
                        + frozen().getScore().getTotal() + " points"),
                "Top row: " + row);
    }

    /**
     * The current result names its frozen identity and shows the recorded base
     * subtotal and calculated total, whether or not it is ranked.
     */
    public void verifyCurrentResultShown() {
        assertTrue(text(IDENTITY).contains(frozen().getIdentity()),
                "Identity in: " + text(IDENTITY));
        assertEquals(frozen().getBaseScore().getSubtotal(),
                points("baseScoreSubtotal"), "Base subtotal");
        assertEquals(frozen().getScore().getTotal(), points("resultTotal"),
                "Calculated total");
    }

    /**
     * The storage failure is named on the window, and the current result still
     * shows its identity and breakdown, marked as not yet saved.
     */
    public void verifyStorageFailure() {
        assertTrue(text(STORAGE).contains("could not be read or saved"),
                "Storage failure in: " + text(STORAGE));
        assertTrue(text(CURRENT).contains("could not be updated yet"),
                "Current result not saved: " + text(CURRENT));
        verifyCurrentResultShown();
    }

    /** The saved file holds exactly the given number of entries. */
    public void verifyStoredEntries(final int expected) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(savedFile()), JsonObject.class);
        assertEquals(expected, saved.getAsJsonArray("entries").size(),
                "Stored entries");
    }

    /** The saved file is byte for byte the given text. */
    public void verifyStoredBytes(final String expected) throws IOException {
        assertEquals(expected, Files.readString(savedFile()),
                "Saved ranking bytes");
    }

    /** No ranking file has been written by this game. */
    public void verifyNoStoredFile() {
        assertFalse(Files.exists(savedFile()), "Saved ranking exists");
    }

    private String text(final String id) {
        return getLabeledById(id).getText();
    }

    /** The first "N points" phrase of the label, as a number. */
    private long points(final String id) {
        final Matcher matcher = POINTS.matcher(text(id));
        if (!matcher.find()) {
            throw new IllegalStateException(
                    "No points phrase in " + id + ": " + text(id));
        }
        return Long.parseLong(matcher.group(1));
    }
}
