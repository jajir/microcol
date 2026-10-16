package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.microcol.model.PlaceEuropePier;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.store.RecruitmentPo;
import org.testfx.util.WaitForAsyncUtils;

/** Visible national progress and the resulting person on the European pier. */
public final class ImmigrationPage extends AbstractScreen {
    public ImmigrationPage(TestContext context) {
        super(context);
    }

    public void verifyProgress(String expected, int crosses, int target,
            boolean pending) {
        Player player = getModel().getPlayerByName("Dutch");
        assertEquals(expected,
                getLabeledById("immigration-progress").getText());
        assertEquals(crosses, player.getImmigrationCrosses());
        assertEquals(target, player.getImmigrationTarget());
        assertEquals(pending, player.hasPendingImmigrant());
    }

    public void verifyArrival(int id, int expectedGold, int earned, int paid,
            int nextCandidateId) {
        Player player = getModel().getPlayerByName("Dutch");
        RecruitmentPo pool = player.save().getRecruitment();
        Unit unit = getModel().getUnitById(id);
        assertTrue(unit.getPlace() instanceof PlaceEuropePier);
        assertSame(player, unit.getOwner());
        assertEquals(expectedGold, player.getGold());
        assertEquals(earned, pool.getEarnedImmigrants());
        assertEquals(paid, pool.getPaidPassages());
        assertEquals(nextCandidateId, pool.getNextCandidateId());
        List<String> originals = List.of("COLONIST", "INDENTURED_SERVANT",
                "PETTY_CRIMINAL");
        int changed = 0;
        for (int slot = 0; slot < 3; slot++) {
            if (pool.getCandidates().get(slot).getId() != slot + 1) {
                changed++;
                assertEquals(4, pool.getCandidates().get(slot).getId());
                assertEquals(originals.get(slot), unit.getType().name());
            }
        }
        assertEquals(1, changed);
        assertEquals(1, getModel().getAllUnits().stream()
                .filter(value -> value.getId() == id).count());
    }

    public void verifyPendingWarning(String expected) {
        assertTrue(getNodeFinder().lookup(expected).queryLabeled().isVisible());
    }

    public void closePendingWarning(String text) {
        javafx.scene.Parent root = getNodeFinder().lookup(text).queryLabeled()
                .getScene().getRoot();
        getRobot().clickOn(
                getNodeFinder().from(root).lookup("#buttonOk").queryButton());
        WaitForAsyncUtils.waitForFxEvents();
    }
}
