package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.microcol.model.*;
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

    public void verifyChanged() {
        assertEquals("The offer changed. Close Congress and open it again.",
                getLabeledById("congressFeedback").getText());
        assertTrue(getButtonById("congressChoose").isDisabled());
    }
}
