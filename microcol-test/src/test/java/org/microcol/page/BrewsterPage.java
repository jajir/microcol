package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.model.Founder;
import org.microcol.model.GoodsType;
import org.microcol.model.Player;
import org.microcol.model.UnitType;
import org.testfx.util.WaitForAsyncUtils;

/** The earned arrival checkpoint and its actual European chooser. */
public final class BrewsterPage extends AbstractScreen {
    private JsonElement colonyBefore;
    private String poolBefore;

    public BrewsterPage(final TestContext context) {
        super(context);
    }

    private Player player() {
        return getModel().getPlayerByName("Dutch");
    }

    public void loadChoiceFixture() {
        new AttackPage(getContext()).replaceGame(
                new File("src/test/scenarios/F62-brewster-choice.microcol"));
    }

    public void rememberColony() {
        colonyBefore = new Gson().toJsonTree(getModel().save().getColonies());
    }

    public void rememberPool() {
        poolBefore = new Gson().toJson(player().save().getRecruitment());
    }

    public void startArrival() throws Exception {
        clickOnButtonWithId(ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
        waitForChoice();
    }

    public void waitForChoice() throws Exception {
        WaitForAsyncUtils.waitFor(20, TimeUnit.SECONDS,
                () -> getModel().isAwaitingImmigrationChoice()
                        && getNodeFinder().lookup("#recruit-confirm-0")
                                .tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void deferChoice() {
        final var root = getNodeFinder().lookup("#recruitment-dialog").query();
        getRobot().clickOn(
                getNodeFinder().from(root).lookup("#buttonOk").queryButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void returnToMap() {
        clickOnButtonWithId("closeButtonId");
    }

    public void continuePending() throws Exception {
        clickOnButtonWithId("buttonContinue");
        waitForChoice();
    }

    public void finishArrivalReport() throws Exception {
        WaitForAsyncUtils.waitFor(20, TimeUnit.SECONDS,
                () -> getModel().isGameRunning() && getNodeFinder()
                        .lookup(".turn-event").tryQuery().isPresent());
        ScreenTurnReport.of(getContext()).close();
    }

    public void verifyFilteredArrival() {
        final var pool = player().save().getRecruitment();
        assertTrue(player().getCongressMembers()
                .contains(Founder.WILLIAM_BREWSTER));
        assertEquals(List.of(4, 5, 3), pool.getCandidates().stream()
                .map(c -> c.getId()).collect(Collectors.toList()));
        assertEquals(UnitType.MASTER_CARPENTER,
                pool.getCandidates().get(2).getType());
        assertTrue(pool.getCandidates().stream()
                .allMatch(c -> c.getType() != UnitType.PETTY_CRIMINAL
                        && c.getType() != UnitType.INDENTURED_SERVANT));
        assertEquals(UnitType.INDENTURED_SERVANT,
                getModel().getUnitById(100).getType());
        assertEquals(0, pool.getPaidPassages());
        assertEquals(0, pool.getEarnedImmigrants());
        assertEquals(10000, player().getGold());
    }

    public void verifyPoolUnchanged() {
        assertEquals(poolBefore,
                new Gson().toJson(player().save().getRecruitment()));
    }

    public void verifyWaiting(final int crosses, final int earned,
            final List<Integer> ids, final String buttonText) {
        assertTrue(getModel().isAwaitingImmigrationChoice());
        assertFalse(getModel().isGameRunning());
        final var pool = player().save().getRecruitment();
        assertEquals(crosses, pool.getCrosses());
        assertEquals(earned, pool.getEarnedImmigrants());
        assertEquals(0, pool.getPaidPassages());
        assertEquals(10000, player().getGold());
        assertEquals(ids, pool.getCandidates().stream().map(c -> c.getId())
                .collect(Collectors.toList()));
        assertEquals(colonyBefore,
                new Gson().toJsonTree(getModel().save().getColonies()));
        assertEquals(3, player().getEarnedImmigrationOffers().size());
        for (int slot = 0; slot < 3; slot++) {
            assertEquals(buttonText,
                    getButtonById("recruit-confirm-" + slot).getText());
            assertFalse(getButtonById("recruit-confirm-" + slot).isDisabled());
        }
    }

    public void verifyTurnCommandsUnavailable() {
        assertTrue(getButtonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID)
                .isDisabled());
        assertFalse(
                getButtonById(ButtonsGamePanel.BUTTON_EUROPE_ID).isDisabled());
        assertFalse(getButtonById("exit").isDisabled());
    }

    public void verifyCompleted(final int crosses, final int earned,
            final int bells, final UnitType first, final UnitType second) {
        assertTrue(getModel().isGameRunning());
        assertFalse(getModel().isAwaitingImmigrationChoice());
        final var pool = player().save().getRecruitment();
        assertEquals(crosses, pool.getCrosses());
        assertEquals(earned, pool.getEarnedImmigrants());
        assertEquals(0, pool.getPaidPassages());
        assertEquals(10000, player().getGold());
        assertEquals(bells, getModel().getColonyById(0).orElseThrow()
                .getWarehouse().getGoods(GoodsType.BELL).getAmount());
        assertEquals(first, getModel().getUnitById(201).getType());
        assertEquals(second, getModel().getUnitById(202).getType());
        assertTrue(getModel().getUnitById(201).isAtEuropePier());
        assertTrue(getModel().getUnitById(202).isAtEuropePier());
    }
}
