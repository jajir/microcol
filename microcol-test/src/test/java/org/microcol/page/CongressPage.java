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

    public void verifyChanged() {
        assertEquals("The offer changed. Close Congress and open it again.",
                getLabeledById("congressFeedback").getText());
        assertTrue(getButtonById("congressChoose").isDisabled());
    }
}
