package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

/** Dock controls for immigrant boarding and their observed model state. */
public final class ImmigrantBoardingPage extends AbstractScreen {
    public ImmigrantBoardingPage(final TestContext context) {
        super(context);
    }

    public void openPierOrders(final int id) {
        getRobot().clickOn("#europe-pier-unit-" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelMenu() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseBoardingOrder(final int id) {
        getRobot().clickOn("#europe-boarding-" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseFrontOfQueue(final int id) {
        getRobot().clickOn("#europe-boarding-front-" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyBoardingState(final int id, final boolean willing,
            final boolean front) {
        final Unit unit = getModel().getUnitById(id);
        assertTrue(unit.isAtEuropePier());
        assertTrue(unit.isImmigrant());
        assertEquals(willing, unit.isWillingToBoard());
        assertEquals(front, unit.isBoardingPriority());
        assertEquals(front || !willing, !getNodeFinder()
                .lookup("#immigrant-marker-" + id).queryAll().isEmpty());
    }

    public void verifyMarkerText(final int id, final String text) {
        assertEquals(text, getNodeFinder().lookup("#immigrant-marker-" + id)
                .queryLabeled().getText());
    }

    public void verifyRecruitedImmigrant(final int id, final UnitType type) {
        final Unit unit = getModel().getUnitById(id);
        assertSame(type, unit.getType());
        assertSame(getModel().getPlayerByName("Dutch"), unit.getOwner());
        assertTrue(unit.isAtEuropePier());
        assertTrue(unit.isImmigrant());
        assertTrue(unit.isWillingToBoard());
        assertFalse(unit.isBoardingPriority());
    }

    public void verifyPierOrder(final List<Integer> ids) {
        assertEquals(ids,
                getModel().getEurope().getPier()
                        .getUnits(getModel().getPlayerByName("Dutch")).stream()
                        .map(Unit::getId).collect(Collectors.toList()));
    }

    public void verifyDisplayedPierOrder(final List<Integer> ids) {
        final HBox panel = getNodeFinder().lookup(".panel-port-pier")
                .queryAs(HBox.class);
        assertEquals(ids,
                panel.getChildren().stream()
                        .map(node -> Integer.valueOf(((Parent) node)
                                .getChildrenUnmodifiable().get(0).getId()
                                .replace("europe-pier-unit-", "")))
                        .collect(Collectors.toList()));
    }
}
