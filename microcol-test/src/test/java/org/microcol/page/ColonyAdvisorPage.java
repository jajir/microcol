package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.microcol.model.GoodsType;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.geometry.Bounds;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;

/**
 * Opens the colony advisor (R-0200) through the statistics screen. Colony rows
 * and goods cells are addressed by stable colony IDs and goods names; the
 * verify methods read the shown text, and the actions only press the controls
 * or replace the saved fixture.
 */
public final class ColonyAdvisorPage extends AbstractScreen {

    private static final String FIXTURE = "src/test/scenarios/F58-religious-production.microcol";

    private static final String COLONY_NAME = "New Amsterdam";

    private static final int MAX_SCROLL_STEPS = 20;

    /**
     * Next stock shown by the report, read before the update, by colony and
     * good.
     */
    private final Map<String, Integer> rememberedNext = new HashMap<>();

    public ColonyAdvisorPage(final TestContext context) {
        super(context);
    }

    /** Opens statistics and the colonies tab with the mouse. */
    public void open() {
        new FreePlayAuditPage(getContext()).openStatistics();
        getRobot().clickOn("#coloniesTab");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Opens statistics and reaches the colonies tab with Tab and arrow keys:
     * Tab enters the toggle group and the arrow keys select its other members.
     */
    public void openWithKeyboard() {
        new FreePlayAuditPage(getContext()).openStatistics();
        final ToggleButton colonies = coloniesTab();
        focusWithKeyboard((Node) colonies.getToggleGroup().getSelectedToggle());
        for (int i = 0; i < colonies.getToggleGroup().getToggles().size()
                && !colonies.isSelected(); i++) {
            getRobot().push(KeyCode.RIGHT);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!colonies.isSelected()) {
            throw new IllegalStateException(
                    "Colonies tab was not selected by keyboard");
        }
    }

    /** Closes the report with the existing Escape path of the statistics. */
    public GamePage closeWithEscape() {
        return new FreePlayAuditPage(getContext()).closeWithEscape();
    }

    /**
     * Presses the Open button of one colony, scrolling its section into view
     * first like a player does; the colony screen replaces the report.
     */
    public void openColony(final int colonyId) {
        revealOpen(colonyId);
        getRobot().clickOn("#colonyOpen-" + colonyId);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private ScrollPane reportScroll() {
        return getNodeFinder().lookup("#colonyAdvisorScroll")
                .queryAs(ScrollPane.class);
    }

    /** Scrolls the report down until the Open button lies in the viewport. */
    private void revealOpen(final int colonyId) {
        final ScrollPane scroll = reportScroll();
        for (int i = 0; i < MAX_SCROLL_STEPS
                && !insideViewport(scroll, openButton(colonyId)); i++) {
            getRobot().moveTo(scroll);
            getRobot().scroll(1, VerticalDirection.DOWN);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    private Node openButton(final int colonyId) {
        return getNodeFinder().lookup("#colonyOpen-" + colonyId).query();
    }

    private static boolean insideViewport(final ScrollPane scroll,
            final Node node) {
        final Bounds viewport = scroll.localToScene(scroll.getLayoutBounds());
        final Bounds shown = node.localToScene(node.getBoundsInLocal());
        return shown.getMinY() >= viewport.getMinY() - 1
                && shown.getMaxY() <= viewport.getMaxY() + 1
                && shown.getMinX() >= viewport.getMinX() - 1
                && shown.getMaxX() <= viewport.getMaxX() + 1;
    }

    /** Right edge of the visible scene, where the window cuts content off. */
    private double sceneRight() {
        return reportScroll().getScene().getWidth();
    }

    private Bounds sceneBounds(final String id) {
        final Node node = getNodeFinder().lookup("#" + id).query();
        return node.localToScene(node.getBoundsInLocal());
    }

    /**
     * Replaces the saved game with the F58 fixture whose New Amsterdam holds 70
     * hammers and 25 tools and has a blacksmith's shop queued. Everything else
     * of the fixture stays as saved; the shop needs 64 hammers and 20 tools, so
     * it completes at the next update whatever the production is.
     */
    public void loadProjectFixture() throws Exception {
        loadFixture(null);
    }

    /**
     * The project fixture with no corn stored. The eight residents of New
     * Amsterdam work in buildings and eat 16 a turn, and no field feeds them,
     * so the next update is a real food shortage without any change of the
     * rules.
     */
    public void loadFamineFixture() throws Exception {
        loadFixture(0);
    }

    private void loadFixture(final Integer corn) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson
                .fromJson(Files.readString(Path.of(FIXTURE)), JsonObject.class);
        for (final JsonElement entry : game.getAsJsonObject("model")
                .getAsJsonArray("colonies")) {
            final JsonObject colony = entry.getAsJsonObject();
            if (COLONY_NAME.equals(colony.get("name").getAsString())) {
                colony.getAsJsonObject("colonyWarehouse").addProperty("HAMMERS",
                        70);
                colony.getAsJsonObject("colonyWarehouse").addProperty("TOOLS",
                        25);
                if (corn != null) {
                    colony.getAsJsonObject("colonyWarehouse")
                            .addProperty("CORN", corn);
                }
                final JsonObject project = new JsonObject();
                project.addProperty("id", 0);
                project.addProperty("type", "CONSTRUCTION");
                project.addProperty("constructionType", "BLACKSMITHS_SHOP");
                colony.getAsJsonArray("buildingQueue").add(project);
            }
        }
        final Path file = Files.createTempFile("colony-advisor-", ".microcol");
        try {
            Files.writeString(file, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(file.toFile());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * Stores the next stock the report shows now, to compare after the update.
     */
    public void rememberNext(final int colonyId, final GoodsType type) {
        rememberedNext.put(key(colonyId, type), nextShown(colonyId, type));
    }

    private int nextShown(final int colonyId, final GoodsType type) {
        // The next cell is "amount" or "amount (n thrown away)"; the amount
        // comes first in both languages.
        final String shown = text("colonyNext-" + colonyId + "-" + type.name());
        return Integer.parseInt(shown.split(" ")[0]);
    }

    private static String key(final int colonyId, final GoodsType type) {
        return colonyId + "-" + type.name();
    }

    private ToggleButton coloniesTab() {
        return getNodeFinder().lookup("#coloniesTab")
                .queryAs(ToggleButton.class);
    }

    private void focusWithKeyboard(final Node node) {
        for (int i = 0; i < 40 && !node.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!node.isFocused()) {
            throw new IllegalStateException(
                    "Statistics control is not keyboard reachable: "
                            + node.getId());
        }
    }

    private String text(final String id) {
        return getNodeFinder().lookup("#" + id).queryAs(Label.class).getText();
    }

    public void verifyTabSelected() {
        assertTrue(coloniesTab().isSelected(), "Colonies tab selected");
    }

    /** Keyboard arrival leaves the colonies tab both selected and focused. */
    public void verifyTabSelectedAndFocused() {
        assertTrue(coloniesTab().isSelected(), "Colonies tab selected");
        assertTrue(coloniesTab().isFocused(), "Colonies tab focused");
    }

    /** The colony heading names the colony and its residents. */
    public void verifyColonyHeader(final int colonyId, final String name,
            final int population) {
        assertEquals("Colony " + name + ": " + population + " colonists",
                text("colonyName-" + colonyId), "Header of " + name);
    }

    public void verifyCzechColonyHeader(final int colonyId, final String name,
            final int population) {
        assertEquals("Kolonie " + name + ": " + population + " kolonistů",
                text("colonyName-" + colonyId), "Czech header of " + name);
    }

    /**
     * The shop is named with its localized name and completes at the update.
     */
    public void verifyProjectCompletes(final int colonyId) {
        final String project = text("colonyProject-" + colonyId);
        assertTrue(project.contains("completes at the next update"),
                "Project state: " + project);
    }

    public void verifyCzechProjectCompletes(final int colonyId) {
        final String project = text("colonyProject-" + colonyId);
        assertTrue(project.contains("se při dalším tahu dokončí"),
                "Czech project state: " + project);
    }

    public void verifyNoProject(final int colonyId) {
        assertEquals("No project is queued.", text("colonyProject-" + colonyId),
                "Project of colony");
    }

    /** The spending column shows the cost the project takes at the update. */
    public void verifySpent(final int colonyId, final GoodsType type,
            final String expected) {
        assertEquals(expected,
                text("colonyOther-" + colonyId + "-" + type.name()),
                "Spent " + type);
    }

    /** Gross output of a good, which is not limited when no input is short. */
    public void verifyOutput(final int colonyId, final GoodsType type,
            final int expected) {
        assertEquals(String.valueOf(expected),
                text("colonyOutput-" + colonyId + "-" + type.name()),
                "Output of " + type);
    }

    /** The stock now is the next stock the report showed before the update. */
    public void verifyStockIsRememberedNext(final int colonyId,
            final GoodsType type) {
        final Integer expected = rememberedNext.get(key(colonyId, type));
        assertEquals(expected.toString(),
                text("colonyStock-" + colonyId + "-" + type.name()),
                "Stock after the update of " + type);
    }

    /** Reopening the report shows the same next stock as before closing it. */
    public void verifyNextIsRemembered(final int colonyId,
            final GoodsType type) {
        assertEquals(rememberedNext.get(key(colonyId, type)).intValue(),
                nextShown(colonyId, type), "Next stock of " + type);
    }

    public void verifyFeedback(final String expected) {
        assertEquals(expected, text("colonyAdvisorFeedback"),
                "Colony advisor feedback");
    }

    /** The growth line warns that food runs short and one colonist is lost. */
    public void verifyFamineWarning(final int colonyId) {
        assertEquals("Next update: food runs short and one colonist is lost.",
                text("colonyGrowth-" + colonyId), "Famine warning");
    }

    public void verifyCzechFamineWarning(final int colonyId) {
        assertEquals(
                "Při dalším tahu dojde k nedostatku jídla a jeden kolonista přijde o život.",
                text("colonyGrowth-" + colonyId), "Czech famine warning");
    }

    /** The report says why output, project and next stock are not exact. */
    public void verifyUncertaintyExplained(final int colonyId) {
        assertEquals(
                "A random colonist is lost to the food shortage, so output, use, project and next stock depend on who is lost. They are not shown as exact.",
                text("colonyUncertain-" + colonyId), "Uncertainty sentence");
    }

    public void verifyCzechUncertaintyExplained(final int colonyId) {
        assertEquals(
                "Při nedostatku jídla přijde náhodný kolonista o život, takže výroba, spotřeba, projekt a příští stav záleží na tom, kdo to bude. Nezobrazují se jako přesné.",
                text("colonyUncertain-" + colonyId),
                "Czech uncertainty sentence");
    }

    /** A colony without a shortage has no uncertainty sentence at all. */
    public void verifyNoUncertainty(final int colonyId) {
        assertTrue(getNodeFinder().lookup("#colonyUncertain-" + colonyId)
                .tryQuery().isEmpty(), "Uncertainty must not be shown");
    }

    /** The current stock of a good stays an exact number under uncertainty. */
    public void verifyStock(final int colonyId, final GoodsType type,
            final int expected) {
        assertEquals(String.valueOf(expected),
                text("colonyStock-" + colonyId + "-" + type.name()),
                "Stock of " + type);
    }

    /**
     * Output, use, spending, change and next stock of a good are shown as a
     * question mark, never as an amount that depends on the lost colonist.
     */
    public void verifyUnknownProjection(final int colonyId,
            final GoodsType type) {
        for (final String cell : new String[] { "colonyOutput", "colonyUsed",
                "colonyOther", "colonyChange", "colonyNext" }) {
            assertEquals("?", text(cell + "-" + colonyId + "-" + type.name()),
                    cell + " of " + type);
        }
    }

    /** The project line names that the project depends on the lost colonist. */
    public void verifyProjectUncertain(final int colonyId) {
        final String project = text("colonyProject-" + colonyId);
        assertTrue(project.contains("depends on which colonist is lost"),
                "Project state: " + project);
    }

    public void verifyCzechProjectUncertain(final int colonyId) {
        final String project = text("colonyProject-" + colonyId);
        assertTrue(project.contains("záleží na tom, který kolonista"),
                "Czech project state: " + project);
    }

    /**
     * The whole report, including its explanatory note, ends inside the window,
     * so no part of it is cut off at the right edge.
     */
    public void verifyReportInsideWindow() {
        assertTrue(sceneBounds("colonyAdvisor").getMaxX() <= sceneRight() + 1,
                "Report ends at " + sceneBounds("colonyAdvisor").getMaxX()
                        + " beyond window width " + sceneRight());
        assertTrue(
                sceneBounds("colonyAdvisorNote").getMaxX() <= sceneRight() + 1,
                "Note is cut off at the window edge");
    }

    /**
     * The colony heading and the sentences under it wrap within the visible
     * part of the report, so they are readable without scrolling sideways.
     */
    public void verifySentencesInsideViewport(final int colonyId) {
        final Bounds viewport = reportScroll()
                .localToScene(reportScroll().getLayoutBounds());
        for (final String id : new String[] { "colonyName-" + colonyId,
                "colonyGrowth-" + colonyId, "colonyUncertain-" + colonyId,
                "colonyProject-" + colonyId }) {
            if (getNodeFinder().lookup("#" + id).tryQuery().isEmpty()) {
                // The uncertainty sentence exists only for an uncertain update.
                continue;
            }
            final Bounds shown = sceneBounds(id);
            assertTrue(
                    shown.getMinX() >= viewport.getMinX() - 1
                            && shown.getMaxX() <= viewport.getMaxX() + 1,
                    id + " reaches beyond the visible width");
        }
    }

    /**
     * Every column heading of the goods table has at least its natural width,
     * so it is shown on one line and never broken inside a word.
     */
    public void verifyTableHeadingsNotSqueezed(final int colonyId,
            final int columns) {
        for (int column = 0; column < columns; column++) {
            final Label heading = getNodeFinder()
                    .lookup("#colonyHeader-" + colonyId + "-" + column)
                    .queryAs(Label.class);
            assertTrue(heading.getWidth() >= heading.prefWidth(-1) - 0.5,
                    "Heading '" + heading.getText() + "' is squeezed to "
                            + heading.getWidth());
        }
    }

    /**
     * The Open button is visible beside the colony name when the report opens.
     */
    public void verifyOpenVisible(final int colonyId) {
        assertTrue(openButton(colonyId).isVisible(), "Open button shown");
        assertTrue(insideViewport(reportScroll(), openButton(colonyId)),
                "Open button lies inside the visible report");
    }

    /** The colony screen that Open requested shows the expected colony. */
    public void verifyColonyOpened(final String expectedNamePart) {
        ColonyScreen.of(getContext(), expectedNamePart)
                .verifyColonyName(expectedNamePart);
    }

    /** Leaves the opened colony with Escape and returns to the map. */
    public GamePage closeOpenedColony(final String expectedName) {
        return ColonyScreen.of(getContext(), expectedName).closeWithEscape();
    }
}
