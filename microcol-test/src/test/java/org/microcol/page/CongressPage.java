package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.microcol.model.*;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.TilePane;

/** Congress choices and persistent outcomes through the player's screen. */
public final class CongressPage extends AbstractScreen {
    private com.google.gson.JsonElement previousWorld;

    private int rememberedBells;
    private double windowWidth;
    private double windowHeight;
    private boolean windowFullScreen;

    public CongressPage(TestContext context) {
        super(context);
    }

    private Player player() {
        return getModel().getCurrentPlayer();
    }

    public void open() {
        rememberWindow();
        clickOnButtonWithId("congress");
    }

    public void hoverOpenButton() {
        getRobot().moveTo(getButtonById("congress"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openWithShortcut() {
        rememberWindow();
        getRobot().push(javafx.scene.input.KeyCode.K);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void rememberWindow() {
        getRobot().interact(() -> {
            final javafx.stage.Stage stage = getContext().getPrimaryStage();
            windowWidth = stage.getWidth();
            windowHeight = stage.getHeight();
            windowFullScreen = stage.isFullScreen();
        });
    }

    public void closeWithEscape() {
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyHeading(final String expectedTitle) {
        assertEquals(expectedTitle,
                ((javafx.scene.control.Label) getNodeFinder()
                        .lookup("#congressHeading").query()).getText());
    }

    public void verifyFillsGameWindow(final String expectedReturnText) {
        getRobot().interact(() -> {
            final javafx.scene.Node page = getNodeFinder()
                    .lookup("#congressPage").query();
            final javafx.scene.Scene scene = getContext().getPrimaryStage()
                    .getScene();
            assertEquals(scene, page.getScene(),
                    "Congress uses the primary scene");
            final javafx.geometry.Bounds bounds = page
                    .localToScene(page.getLayoutBounds());
            assertEquals(0, bounds.getMinX(), 1);
            assertEquals(0, bounds.getMinY(), 1);
            assertEquals(scene.getWidth(), bounds.getWidth(), 1);
            assertEquals(scene.getHeight(), bounds.getHeight(), 1);
            assertEquals(expectedReturnText,
                    getButtonById("congressClose").getText());
            final javafx.scene.Node status = page.lookup("#statusBar");
            assertTrue(status.isVisible());
            assertTrue(insideScene(status),
                    "The live status bar stays in the game window");
            assertFalse(isCssIdVisible("congress"),
                    "The map toolbar is replaced");
            verifyWindowUnchanged();
        });
    }

    public void verifyReturnedToGame() throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> isCssIdVisible("congress"));
        assertFalse(isCssIdVisible("congressPage"));
        assertFalse(getButtonById("congress").isDisabled());
        getRobot().interact(this::verifyWindowUnchanged);
    }

    private void verifyWindowUnchanged() {
        final javafx.stage.Stage stage = getContext().getPrimaryStage();
        assertEquals(1,
                javafx.stage.Window.getWindows().stream()
                        .filter(javafx.stage.Window::isShowing).count(),
                "Congress creates no additional window");
        assertEquals(windowWidth, stage.getWidth(), 1);
        assertEquals(windowHeight, stage.getHeight(), 1);
        assertEquals(windowFullScreen, stage.isFullScreen());
    }

    public void close() {
        clickOnButtonWithId("congressClose");
    }

    public void select(Founder founder) {
        clickOnButtonWithId("founder-" + founder.name());
    }

    public void choose() {
        clickOnButtonWithId("congressChoose");
    }

    public void rememberWorld() {
        previousWorld = new com.google.gson.Gson()
                .toJsonTree(getModel().save());
    }

    public void chooseElsewhere(Founder founder) {
        getRobot().interact(
                () -> player().chooseFounder(player().getFounderOffers()
                        .stream().filter(offer -> offer.getFounder() == founder)
                        .findFirst().orElseThrow()));
    }

    public void verifyWorldUnchanged() {
        assertEquals(previousWorld,
                new com.google.gson.Gson().toJsonTree(getModel().save()));
    }

    public void verifyOffer(List<Founder> expected) {
        final TilePane cards = getNodeFinder().lookup("#congressCandidates")
                .queryAs(TilePane.class);
        final List<Founder> actual = cards.getChildren().stream()
                .map(node -> ((FounderOffer) node.getUserData()).getFounder())
                .collect(Collectors.toList());
        assertEquals(expected, actual);
        assertEquals(expected.size(),
                actual.stream().map(Founder::getCategory).distinct().count());
        for (javafx.scene.Node node : cards.getChildren()) {
            final ToggleButton card = (ToggleButton) node;
            assertTrue(card.getStyleClass().contains("congress-card"));
            assertNotNull(card.getGraphic());
            assertTrue(card.getAccessibleText().length() > 20);
            assertTrue(card.getWidth() <= cards.getWidth() + 1);
            assertTrue(
                    card.getHeight() + 1 >= card.prefHeight(card.getWidth()));
        }
    }

    public void verifyCardColumns(final int columns) {
        final TilePane cards = getNodeFinder().lookup("#congressCandidates")
                .queryAs(TilePane.class);
        final double firstRow = cards.getChildren().get(0).getLayoutY();
        final var row = cards.getChildren().stream()
                .filter(node -> Math.abs(node.getLayoutY() - firstRow) < 1)
                .collect(Collectors.toList());
        assertEquals(columns, row.size(), "Cards in the first row");
        assertEquals(cards.prefHeight(cards.getWidth()), cards.getHeight(), 1,
                "Card panel must fit its current rows after resizing");
        final javafx.scene.layout.Region bar = getNodeFinder()
                .lookup("#congressBellProgress")
                .queryAs(javafx.scene.layout.Region.class);
        assertTrue(bar.getHeight() > 0
                && bar.getHeight() + 1 >= bar.prefHeight(bar.getWidth()),
                "Bell progress must retain its height");
        final double right = row.get(row.size() - 1).getBoundsInParent()
                .getMaxX();
        assertTrue(Math.abs(cards.getWidth() - right) < columns + 1,
                "Cards must use the row width");
    }

    public void verifyFounderSelection(final Founder founder,
            final String expectedEffect) {
        final ToggleButton card = getNodeFinder()
                .lookup("#founder-" + founder.name())
                .queryAs(ToggleButton.class);
        assertTrue(card.isSelected());
        assertEquals(expectedEffect,
                getLabeledById("congressDetail").getText());
        assertFalse(getButtonById("congressChoose").isDisabled());
        assertTrue(player().getSelectedFounder().isEmpty());
    }

    public void verifySelectionRequired() {
        assertTrue(getButtonById("congressChoose").isDisabled());
        assertTrue(player().getSelectedFounder().isEmpty());
    }

    public void verifyProgress(int bells, int target, String objective,
            String members) {
        assertEquals("Congress bells: " + bells + " / " + target,
                getLabeledById("congressProgress").getText());
        assertEquals(objective, getLabeledById("congressObjective").getText());
        assertEquals("Members: " + members,
                getLabeledById("congressMembers").getText());
        assertEquals(bells, player().getCongressBells());
        assertEquals(target, player().getCongressTarget());
    }

    public void verifyReward(int frigates, int gold, int colonyBells) {
        assertEquals(frigates,
                player().getAllUnits().stream()
                        .filter(unit -> unit.getType() == UnitType.FRIGATE
                                && unit.isAtEuropePort())
                        .count());
        assertEquals(gold, player().getGold());
        assertEquals(colonyBells, getModel().getColonyById(0).orElseThrow()
                .getWarehouse().getGoods(GoodsType.BELL).getAmount());
        assertEquals(Set.of(Founder.JOHN_PAUL_JONES),
                Set.copyOf(player().getCongressMembers()));
        assertFalse(player().isJonesDeliveryPending());
        assertEquals("Jones: one free frigate delivered.",
                getLabeledById("congressReward").getText());
    }

    public void verifyRoyalForce(int infantry, int cavalry, int artillery,
            int menOfWar, int remainder) {
        verifyRoyalForce(infantry, cavalry, artillery, menOfWar, remainder,
                false);
    }

    public void verifyRoyalForce(int infantry, int cavalry, int artillery,
            int menOfWar, int remainder, boolean shipped) {
        assertEquals(
                "Royal infantry: " + infantry + " total, "
                        + (shipped ? 0 : infantry) + " reserve, "
                        + (shipped ? infantry : 0)
                        + " shipped, 0 destroyed, 0 captured",
                getLabeledById("congressRoyalInfantry").getText());
        assertEquals("Royal cavalry: " + cavalry + " total, "
                + (shipped ? 0 : cavalry) + " reserve, "
                + (shipped ? cavalry : 0) + " shipped, 0 destroyed, 0 captured",
                getLabeledById("congressRoyalCavalry").getText());
        assertEquals(
                "Royal artillery: " + artillery + " total, "
                        + (shipped ? 0 : artillery) + " reserve, "
                        + (shipped ? artillery : 0)
                        + " shipped, 0 destroyed, 0 captured",
                getLabeledById("congressRoyalArtillery").getText());
        assertEquals(
                "Men-of-war: " + menOfWar + " total, "
                        + (shipped ? 0 : menOfWar) + " reserve, "
                        + (shipped ? menOfWar : 0)
                        + " shipped, 0 destroyed, 0 captured",
                getLabeledById("congressRoyalMenOfWar").getText());
        assertEquals(
                "Crown bells toward the next unit: " + remainder + " / 500",
                getLabeledById("congressRoyalBells").getText());
        RoyalRoster crown = player().getCrownRoster().orElseThrow();
        assertEquals(infantry, crown.getTotal(RoyalRoster.Family.INFANTRY));
        assertEquals(cavalry, crown.getTotal(RoyalRoster.Family.CAVALRY));
        assertEquals(artillery, crown.getTotal(RoyalRoster.Family.ARTILLERY));
        assertEquals(menOfWar, crown.getTotal(RoyalRoster.Family.MAN_OF_WAR));
        assertEquals(remainder, crown.getBellRemainder());
        assertEquals(500, crown.getBellThreshold());
    }

    /**
     * The support line shows the model's own national support, rounded down
     * (R-0203).
     */
    public void verifySupport(String label, int percent) {
        assertEquals(percent,
                player().getNationalRebelSupport().getDisplayPercent());
        assertEquals(label, getLabeledById("congressSupport").getText());
    }

    /**
     * Every Crown line equals the owner's live roster now, so neither a turn
     * nor Continue leaves the dialog on an older count (R-0203).
     */
    public void verifyRoyalForceMatchesModel() {
        RoyalRoster crown = player().getCrownRoster().orElseThrow();
        assertEquals(
                royalFamilyText("Royal infantry", crown,
                        RoyalRoster.Family.INFANTRY),
                getLabeledById("congressRoyalInfantry").getText());
        assertEquals(
                royalFamilyText("Royal cavalry", crown,
                        RoyalRoster.Family.CAVALRY),
                getLabeledById("congressRoyalCavalry").getText());
        assertEquals(
                royalFamilyText("Royal artillery", crown,
                        RoyalRoster.Family.ARTILLERY),
                getLabeledById("congressRoyalArtillery").getText());
        assertEquals(
                royalFamilyText("Men-of-war", crown,
                        RoyalRoster.Family.MAN_OF_WAR),
                getLabeledById("congressRoyalMenOfWar").getText());
        assertEquals(
                "Crown bells toward the next unit: " + crown.getBellRemainder()
                        + " / " + crown.getBellThreshold(),
                getLabeledById("congressRoyalBells").getText());
    }

    private static String royalFamilyText(String label, RoyalRoster crown,
            RoyalRoster.Family family) {
        return label + ": " + crown.getTotal(family) + " total, "
                + crown.getCount(family, RoyalRoster.State.RESERVE)
                + " reserve, "
                + crown.getCount(family, RoyalRoster.State.SHIPPED)
                + " shipped, "
                + crown.getCount(family, RoyalRoster.State.DESTROYED)
                + " destroyed, "
                + crown.getCount(family, RoyalRoster.State.CAPTURED)
                + " captured";
    }

    /** Remember the Congress bells before a turn produces more of them. */
    public void rememberBells() {
        rememberedBells = player().getCongressBells();
    }

    /** Congress keeps banking bells after independence; the Crown does not. */
    public void verifyBellsProducedSinceRemembered() {
        assertTrue(player().getCongressBells() > rememberedBells,
                "The colony must produce bells after the declaration");
    }

    public void verifyRoyalGrowthRunning() {
        assertFalse(player().isDeclaredIndependence());
        assertEquals("", getLabeledById("congressRoyalStopped").getText());
    }

    public void verifyRoyalGrowthStopped() {
        assertTrue(player().isDeclaredIndependence());
        assertEquals("Crown growth stopped at independence.",
                getLabeledById("congressRoyalStopped").getText());
    }

    /** Before the rebel's first update there is no target, only the notice. */
    public void verifyInterventionAwaiting() {
        assertTrue(player().getIntervention().isEmpty());
        assertEquals(
                "Foreign intervention target is set at the next rebel update.",
                getLabeledById("congressIntervention").getText());
    }

    public void verifyInterventionProgress(final long bells, final int target) {
        final InterventionTarget state = player().getIntervention()
                .orElseThrow();
        assertEquals(target, state.getTarget());
        assertEquals(bells, state.getBells());
        assertFalse(state.isEarned());
        assertEquals("Foreign intervention bells: " + bells + " / " + target,
                getLabeledById("congressIntervention").getText());
    }

    public void verifyInterventionEarned(final int target) {
        assertTrue(player().getIntervention().orElseThrow().isEarned());
        assertEquals(
                "Foreign intervention is earned: the target of " + target
                        + " bells is reached.",
                getLabeledById("congressIntervention").getText());
    }

    /** The earned forces wait until a free owned coastal colony is reached. */
    public void verifyInterventionForcesPending() {
        assertTrue(player().getIntervention().orElseThrow().isEarned());
        assertFalse(player().getIntervention().orElseThrow().isDelivered());
        assertEquals(
                "Foreign intervention forces wait for a free owned coastal colony that the sea reaches.",
                getLabeledById("congressInterventionForces").getText());
    }

    /** The earned forces have been delivered to the map. */
    public void verifyInterventionForcesArrived() {
        assertTrue(player().getIntervention().orElseThrow().isDelivered());
        assertEquals("Foreign intervention forces have arrived.",
                getLabeledById("congressInterventionForces").getText());
    }

    /** Every line of the Congress report, the ones that may wrap included. */
    private static final String[] TEXT_IDS = { "congressSupport",
            "congressProgress", "congressObjective", "congressMembers",
            "congressReward", "congressRoyalInfantry", "congressRoyalCavalry",
            "congressRoyalArtillery", "congressRoyalMenOfWar",
            "congressRoyalBells", "congressRoyalStopped",
            "congressIntervention", "congressInterventionForces",
            "congressDetail", "congressFeedback" };

    /** The primary window is no larger than the given size (R-0203). */
    public void verifyWindowFits(int width, int height) {
        javafx.stage.Window window = getButtonById("congressClose").getScene()
                .getWindow();
        assertTrue(window.getWidth() <= width,
                "Congress width " + window.getWidth());
        assertTrue(window.getHeight() <= height,
                "Congress height " + window.getHeight());
    }

    /**
     * Each text line is laid out at the height its wrapped text needs, so no
     * line is cut off. The check reads the laid-out size, not the text.
     */
    public void verifyTextNotClipped() {
        for (String id : TEXT_IDS) {
            var label = getLabeledById(id);
            if (!label.getText().isEmpty()) {
                assertTrue(
                        label.getHeight() + 1 >= label
                                .prefHeight(label.getWidth()),
                        id + " is clipped");
            }
        }
    }

    /** The support line is inside the report viewport before any scrolling. */
    public void verifySupportVisible() throws Exception {
        scrollTo(0.0);
        final javafx.scene.Node support = getLabeledById("congressSupport");
        final boolean visible = WaitForAsyncUtils
                .asyncFx(() -> insideViewport(support))
                .get(5, TimeUnit.SECONDS);
        assertTrue(visible, "The support line must be inside the viewport");
    }

    /** Close stays fixed; the confirmation beside the detail is scrollable. */
    public void verifyCloseAlwaysVisible() throws Exception {
        for (double position : new double[] { 0.0, 1.0 }) {
            scrollTo(position);
            final javafx.scene.Node close = getButtonById("congressClose");
            final boolean visible = WaitForAsyncUtils.asyncFx(
                    () -> insideScene(close) && !insideViewportArea(close))
                    .get(5, TimeUnit.SECONDS);
            assertTrue(visible, "Close must stay visible at " + position);
        }
    }

    public void showFounderDecision() {
        revealGameContent(getButtonById("congressChoose"));
    }

    public void verifyFounderDecisionVisible() throws Exception {
        final javafx.scene.Node choose = getButtonById("congressChoose");
        final boolean reachable = WaitForAsyncUtils
                .asyncFx(() -> insideViewport(choose)).get(5, TimeUnit.SECONDS);
        assertTrue(reachable, "Founder confirmation must be reachable");
    }

    /**
     * Scrolled to the end, the last non-empty report line (the Crown roster and
     * any later line) ends inside the viewport, not below it.
     */
    public void verifyReportReachableByScrolling() throws Exception {
        scrollTo(1.0);
        javafx.scene.Node last = null;
        for (String id : TEXT_IDS) {
            if (!getLabeledById(id).getText().isEmpty()) {
                last = getLabeledById(id);
            }
        }
        final javafx.scene.Node end = last;
        final boolean reachable = WaitForAsyncUtils
                .asyncFx(() -> insideViewport(end)).get(5, TimeUnit.SECONDS);
        assertTrue(reachable, "The last report line must be reachable");
    }

    /** Show the top of the report, where the support line is. */
    public void scrollReportToStart() throws Exception {
        scrollTo(0.0);
    }

    /** Show the end of the report, where the Crown roster is. */
    public void scrollReportToEnd() throws Exception {
        showRoyalDetails();
        scrollTo(1.0);
    }

    public void showRoyalDetails() throws Exception {
        final TitledPane details = getNodeFinder()
                .lookup("#congressRoyalDetails").queryAs(TitledPane.class);
        if (details.isVisible() && !details.isExpanded()) {
            scrollTo(1.0);
            revealGameContent(details.lookup(".title"));
            getRobot().clickOn(details.lookup(".arrow-button"));
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void verifyRoyalDetailsExpanded() throws Exception {
        final TitledPane details = getNodeFinder()
                .lookup("#congressRoyalDetails").queryAs(TitledPane.class);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils
                .asyncFx(details::isExpanded).get(5, TimeUnit.SECONDS));
        assertTrue(
                WaitForAsyncUtils.asyncFx(details::isExpanded).get(5,
                        TimeUnit.SECONDS),
                "Royal deployment details must be expanded");
    }

    private void scrollTo(double position) throws Exception {
        final javafx.scene.control.ScrollPane scroll = getNodeFinder()
                .lookup("#congressScroll")
                .queryAs(javafx.scene.control.ScrollPane.class);
        WaitForAsyncUtils.asyncFx(() -> {
            scroll.setVvalue(position);
            return true;
        }).get(5, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** The node lies within the scrolling report's visible area. */
    private boolean insideViewport(javafx.scene.Node node) {
        final javafx.scene.Node scroll = getNodeFinder()
                .lookup("#congressScroll").query();
        final javafx.geometry.Bounds area = scroll
                .localToScene(scroll.getBoundsInLocal());
        final javafx.geometry.Bounds bounds = node
                .localToScene(node.getBoundsInLocal());
        return bounds.getMinX() >= area.getMinX()
                && bounds.getMinY() >= area.getMinY()
                && bounds.getMaxX() <= area.getMaxX()
                && bounds.getMaxY() <= area.getMaxY();
    }

    /** The node overlaps the scrolling report's area at all. */
    private boolean insideViewportArea(javafx.scene.Node node) {
        final javafx.scene.Node scroll = getNodeFinder()
                .lookup("#congressScroll").query();
        return scroll.localToScene(scroll.getBoundsInLocal())
                .intersects(node.localToScene(node.getBoundsInLocal()));
    }

    private static boolean insideScene(javafx.scene.Node node) {
        final javafx.geometry.Bounds bounds = node
                .localToScene(node.getBoundsInLocal());
        return bounds.getMinX() >= 0 && bounds.getMinY() >= 0
                && bounds.getMaxX() <= node.getScene().getWidth()
                && bounds.getMaxY() <= node.getScene().getHeight();
    }

    public void verifyChanged() {
        assertEquals("The offer changed. Close Congress and open it again.",
                getLabeledById("congressFeedback").getText());
        assertTrue(getButtonById("congressChoose").isDisabled());
    }
}
