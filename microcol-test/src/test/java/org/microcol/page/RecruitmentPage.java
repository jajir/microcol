package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import org.microcol.model.RecruitmentOffer;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

/** Actions and explicit observations for the three-candidate passage offer. */
public final class RecruitmentPage extends AbstractScreen {
    public RecruitmentPage(TestContext context) {
        super(context);
    }

    public void open() {
        clickOnButtonWithId("recruitButtonId");
    }

    public void cancel() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public String poolSnapshot() {
        return new com.google.gson.Gson().toJson(
                getModel().getPlayerByName("Dutch").save().getRecruitment());
    }

    public void confirm(int slot) {
        clickOnButtonWithId("recruit-confirm-" + slot);
    }

    public void confirmTwice(int slot) {
        Button button = getButtonById("recruit-confirm-" + slot);
        getRobot().clickOn(button);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(button::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmWithKeyboard(int slot) {
        Button button = getButtonById("recruit-confirm-" + slot);
        for (int i = 0; i < 8 && !button.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!button.isFocused()) {
            throw new IllegalStateException(
                    "Candidate is not keyboard reachable");
        }
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyOffer(List<String> names, int price, boolean enabled,
            String prefix, String reason) {
        assertEquals(3, getNodeFinder()
                .lookup(node -> node.getId() != null
                        && node.getId().startsWith("recruit-candidate-"))
                .queryAll().size());
        for (int slot = 0; slot < 3; slot++) {
            if (names != null) {
                assertEquals(names.get(slot),
                        getLabeledById("recruit-name-" + slot).getText());
            }
            assertEquals(prefix + price,
                    getButtonById("recruit-confirm-" + slot).getText());
            assertEquals(!enabled,
                    getButtonById("recruit-confirm-" + slot).isDisabled());
            assertEquals(reason,
                    getLabeledById("recruit-reason-" + slot).getText());
        }
    }

    public void verifyPool(String expected, int gold) {
        assertEquals(expected, poolSnapshot());
        assertEquals(gold, getModel().getPlayerByName("Dutch").getGold());
    }

    public void verifyCandidateIds(List<Integer> ids, int gold) {
        assertEquals(ids,
                getModel().getPlayerByName("Dutch").getRecruitmentOffers()
                        .stream().map(RecruitmentOffer::getCandidateId)
                        .collect(Collectors.toList()));
        assertEquals(gold, getModel().getPlayerByName("Dutch").getGold());
    }

    public void verifyRecruited(int id, String type, int gold, Class<?> place) {
        Unit unit = getModel().getUnitById(id);
        assertSame(UnitType.valueOf(type), unit.getType());
        assertTrue(place.isInstance(unit.getPlace()));
        assertSame(getModel().getPlayerByName("Dutch"), unit.getOwner());
        assertEquals(gold, unit.getOwner().getGold());
        assertEquals(1, getModel().getAllUnits().stream()
                .filter(value -> value.getId() == id).count());
    }

    public void verifyChangedWarning() {
        assertTrue(getNodeFinder().lookup(
                "The candidate or passage price changed. Reopen recruitment.")
                .queryLabeled().isVisible());
    }

    public void closeChangedWarning() {
        javafx.scene.Parent root = getNodeFinder().lookup(
                "The candidate or passage price changed. Reopen recruitment.")
                .queryLabeled().getScene().getRoot();
        getRobot().clickOn(
                getNodeFinder().from(root).lookup("#buttonOk").queryButton());
        WaitForAsyncUtils.waitForFxEvents();
    }
}
