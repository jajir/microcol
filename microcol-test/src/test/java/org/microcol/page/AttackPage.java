package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.screen.game.gamepanel.MoveModeSupport;
import org.microcol.model.Location;
import org.testfx.util.WaitForAsyncUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import javafx.scene.input.MouseButton;

/** Exercise attack selection through the player's map gesture. */
public final class AttackPage extends AbstractScreen {
    private final Gson gson = new Gson();
    private JsonElement before;
    private org.microcol.model.Model original;
    private javafx.stage.Stage review;
    private final FogPage map;

    public AttackPage(final TestContext context) {
        super(context);
        map = new FogPage(context);
    }

    private JsonElement gameplay(final org.microcol.model.Model model) {
        final com.google.gson.JsonObject state = gson.toJsonTree(model.save())
                .getAsJsonObject();
        // Selecting a map unit legitimately changes the persisted camera focus.
        state.remove("focusedField");
        return state;
    }

    public void rememberWorld() {
        original = getModel();
        before = gameplay(original);
    }

    public void preview(final Location from, final Location target)
            throws Exception {
        map.preview(from, target);
    }

    public void cancelPreview() {
        map.cancelPreview();
    }

    public void release() {
        getRobot().release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyAttackPreview(final Location target) {
        final MoveModeSupport preview = getContext()
                .getClassFromGuice(MoveModeSupport.class);
        assertEquals("FIGHT", String.valueOf(preview.getMoveMode()));
        assertEquals(java.util.List.of(target), preview.getMoveLocations());
    }

    public void verifyGameplayUnchanged() {
        assertEquals(before, gameplay(getModel()));
    }

    public void chooseAttack(final Location from, final Location target)
            throws Exception {
        preview(from, target);
        release();
        review = (javafx.stage.Stage) getButtonById("fightConfirm").getScene()
                .getWindow();
    }

    public void configureHiddenAdvisor() {
        getRobot().interact(() -> getContext().getClassFromGuice(
                org.microcol.gui.preferences.PreferencesProperties.class)
                .getShowFightAdvisorProperty().set(false));
    }

    public void cancel() {
        clickOnButtonWithId("fightCancel");
    }

    public void escape() {
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeWindow() {
        getRobot().interact(review::close);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("fightConfirm");
    }

    public void closeMessage() {
        clickOnButtonWithId("buttonOk");
    }

    public void establishExistingWar() {
        getRobot().interact(() -> getModel().declareHostilities(
                getModel().getPlayerByName("Dutch"),
                getModel().getPlayerByName("France")));
    }

    public void simulateDisarm(final int unit) {
        getRobot().interact(
                () -> ((org.microcol.model.unit.UnitFreeColonist) getModel()
                        .getUnitById(unit)).setHoldingGuns(false));
    }

    public void simulateMovedDefender(final Location to) {
        getRobot()
                .interact(() -> getModel().getUnitById(6).placeToLocation(to));
    }

    public void replaceGame(final java.io.File fixture) {
        getRobot().interact(() -> getContext()
                .getClassFromGuice(
                        org.microcol.gui.event.model.GameController.class)
                .loadModelFromFile(fixture));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void chooseExistingOutcome(final int attacker, final boolean wins)
            throws Exception {
        final java.lang.reflect.Field random = org.microcol.model.AbstractUnit.class
                .getDeclaredField("random");
        random.setAccessible(true);
        ((java.util.Random) random.get(getModel().getUnitById(attacker)))
                .setSeed(wins ? 4096L : 0L);
    }

    public void verifyReview(final String attacker, final String defender,
            final String declaration, final int attackerId,
            final int defenderId) {
        assertTrue(review.isShowing());
        assertEquals(attacker, getLabeledById("fightAttacker").getText());
        assertEquals(defender, getLabeledById("fightDefender").getText());
        assertTrue(getLabeledById("fightDeclaration").getText()
                .contains(declaration));
        final org.microcol.gui.image.ImageProvider images = getContext()
                .getClassFromGuice(org.microcol.gui.image.ImageProvider.class);
        assertSame(images.getUnitImage(getModel().getUnitById(attackerId)),
                getNodeFinder().lookup("#fightAttackerImage")
                        .queryAs(javafx.scene.image.ImageView.class)
                        .getImage());
        assertSame(images.getUnitImage(getModel().getUnitById(defenderId)),
                getNodeFinder().lookup("#fightDefenderImage")
                        .queryAs(javafx.scene.image.ImageView.class)
                        .getImage());
        assertFalse(getLabeledById("fightHideAdvisor").isVisible());
    }

    public void verifyReviewClosed() {
        assertFalse(review.isShowing());
    }

    public void verifyPreviewUnavailable() {
        map.verifyPreviewEmpty();
    }

    public void verifyRejection(final String expected) throws Exception {
        WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS,
                () -> getListOfNodes(".label").stream()
                        .filter(javafx.scene.control.Label.class::isInstance)
                        .map(node -> ((javafx.scene.control.Label) node)
                                .getText())
                        .anyMatch(text -> text.contains(expected)));
        assertTrue(
                getButtonById("buttonOk").getScene().getWindow().isShowing());
    }

    public void verifyOriginalGameplayUnchanged() {
        assertEquals(before, gameplay(original));
    }

    public void verifyBattle(final int survivor, final int lost,
            final Location at) throws Exception {
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().tryGetUnitById(lost).isEmpty());
        GamePage.of(getContext()).waitWhileMoving();
        assertTrue(getModel().tryGetUnitById(lost).isEmpty());
        assertEquals(at, getModel().getUnitById(survivor).getLocation());
        assertEquals(0, getModel().getUnitById(survivor).getActionPoints());
        assertEquals(org.microcol.model.EuropeanRelationState.WAR,
                getModel().getEuropeanRelation(
                        getModel().getPlayerByName("Dutch"),
                        getModel().getPlayerByName("France")));
    }
}
