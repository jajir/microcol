package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.microcol.model.*;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.ListView;

/** Congress choices and persistent outcomes through the player's dialog. */
public final class CongressPage extends AbstractScreen {
    private com.google.gson.JsonElement previousWorld;

    private int rememberedBells;

    public CongressPage(TestContext context) {
        super(context);
    }

    private Player player() {
        return getModel().getCurrentPlayer();
    }

    public void open() {
        clickOnButtonWithId("congress");
    }

    public void close() {
        clickOnButtonWithId("congressClose");
    }

    public void select(Founder founder) {
        getRobot().clickOn("#founder-" + founder.name());
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
        ListView<FounderOffer> list = getNodeFinder()
                .lookup("#congressCandidates").queryAs(ListView.class);
        assertEquals(expected, list.getItems().stream()
                .map(FounderOffer::getFounder).collect(Collectors.toList()));
        assertEquals(expected.size(), list.getItems().stream()
                .map(o -> o.getFounder().getCategory()).distinct().count());
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

    /** The dialog window is no larger than the given size (R-0203). */
    public void verifyDialogFits(int width, int height) {
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

    /**
     * The Choose and Close actions are inside the window and outside the
     * scrolling report at any scroll position, so a player can always act.
     */
    public void verifyActionsAlwaysVisible() throws Exception {
        for (double position : new double[] { 0.0, 1.0 }) {
            scrollTo(position);
            for (String id : new String[] { "congressChoose",
                    "congressClose" }) {
                final javafx.scene.Node action = getButtonById(id);
                final boolean visible = WaitForAsyncUtils
                        .asyncFx(() -> insideScene(action)
                                && !insideViewportArea(action))
                        .get(5, TimeUnit.SECONDS);
                assertTrue(visible, id + " must stay visible at " + position);
            }
        }
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
        scrollTo(1.0);
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
