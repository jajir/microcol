package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.microcol.gui.event.model.GameController;
import org.microcol.model.Colony;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Actions use the report; model edits only simulate a concurrent change. */
public final class EuropeanRelationsPage extends AbstractScreen {
    private final Gson gson = new Gson();
    private JsonObject worldBefore;
    private JsonObject colonyStores;
    private Window reviewWindow;
    private Model original;

    public EuropeanRelationsPage(final TestContext context) {
        super(context);
    }

    public void open() {
        new FreePlayAuditPage(getContext()).openStatistics();
        getRobot().clickOn("#relationsTab");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openWithKeyboard() {
        new FreePlayAuditPage(getContext()).openStatistics();
        final ToggleButton relations = getNodeFinder().lookup("#relationsTab")
                .queryAs(ToggleButton.class);
        focusWithKeyboard(
                (Node) relations.getToggleGroup().getSelectedToggle());
        // Tab enters a toggle group; arrow keys select its other members.
        for (int i = 0; i < relations.getToggleGroup().getToggles().size()
                && !relations.isSelected(); i++) {
            getRobot().push(KeyCode.RIGHT);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!relations.isSelected()) {
            throw new IllegalStateException(
                    "Relations tab was not selected by keyboard");
        }
    }

    private void activateWithKeyboard(final Node node) {
        focusWithKeyboard(node);
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void focusWithKeyboard(final Node node) {
        for (int i = 0; i < 40 && !node.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!node.isFocused()) {
            throw new IllegalStateException(
                    "Relation control is not keyboard reachable: "
                            + node.getId());
        }
    }

    public void review(final String pair) {
        clickOnButtonWithId("declare-" + pair);
        reviewWindow = getButtonById("declarationConfirm").getScene()
                .getWindow();
    }

    public void reviewWithKeyboard(final String pair) {
        activateWithKeyboard(getButtonById("declare-" + pair));
        reviewWindow = getButtonById("declarationConfirm").getScene()
                .getWindow();
    }

    public void closeMapMessage() {
        clickOnButtonWithId("buttonOk");
    }

    public void simulateDisplayName(final String key, final String name) {
        getRobot().interact(
                () -> getModel().getPlayerByName(key).setDisplayName(name));
    }

    public void verifyCaptureFeedback(final String expected) throws Exception {
        WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS,
                () -> getListOfNodes(".label").stream()
                        .filter(Label.class::isInstance)
                        .map(node -> ((Label) node).getText())
                        .anyMatch(text -> text.contains(expected)));
        assertTrue(
                getButtonById("buttonOk").getScene().getWindow().isShowing());
    }

    public void verifyMoveRejection() {
        assertTrue(
                getButtonById("buttonOk").getScene().getWindow().isShowing());
        assertTrue(getListOfNodes(".label").stream()
                .filter(Label.class::isInstance)
                .map(node -> ((Label) node).getText()).anyMatch(text -> text
                        .contains("not possible to move unit here")));
    }

    public void cancel() {
        clickOnButtonWithId("declarationCancel");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("declarationConfirm");
    }

    public void confirmWithKeyboard() {
        activateWithKeyboard(getButtonById("declarationConfirm"));
    }

    public void confirmTwice() {
        final Button confirmation = getButtonById("declarationConfirm");
        getRobot().clickOn(confirmation);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(confirmation::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public GamePage close() {
        clickOnButtonWithId("closeButtonId");
        return GamePage.of(getContext());
    }

    public void rememberWorld() {
        original = getModel();
        worldBefore = gson.toJsonTree(original.save()).getAsJsonObject();
        colonyStores = stocks();
    }

    private JsonObject stocks() {
        return gson.toJsonTree(getModel().save().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getColonyWarehouse()).getAsJsonObject();
    }

    public void simulateAcceptedDeclaration(final String target) {
        getRobot().interact(() -> getModel().declareHostilities(
                getModel().getCurrentPlayer(),
                getModel().getPlayerByName(target)));
    }

    public void simulateGameReplacement(final File fixture) {
        getRobot().interact(
                () -> getContext().getClassFromGuice(GameController.class)
                        .loadModelFromFile(fixture));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void simulateStoppedGame() {
        getRobot().interact(() -> getModel().stop());
    }

    public void verifyWorldUnchanged() {
        assertEquals(worldBefore, gson.toJsonTree(getModel().save()));
    }

    public void verifyOnlyRelationsChanged() {
        final JsonObject before = worldBefore.deepCopy();
        final JsonObject after = gson.toJsonTree(getModel().save())
                .getAsJsonObject();
        before.remove("europeanRelations");
        after.remove("europeanRelations");
        assertEquals(before, after);
    }

    public void verifyPairs(final EuropeanRelationState dutchFrench,
            final EuropeanRelationState dutchSpanish,
            final EuropeanRelationState frenchSpanish) {
        final Model model = getModel();
        assertEquals(dutchFrench,
                model.getEuropeanRelation(model.getPlayerByName("Dutch"),
                        model.getPlayerByName("France")));
        assertEquals(dutchSpanish,
                model.getEuropeanRelation(model.getPlayerByName("Dutch"),
                        model.getPlayerByName("Spain")));
        assertEquals(frenchSpanish,
                model.getEuropeanRelation(model.getPlayerByName("France"),
                        model.getPlayerByName("Spain")));
    }

    public void verifyReport(final List<String> names,
            final List<String> states, final int declarationCount) {
        final List<VBox> rows = getListOfNodes(".relation-row");
        rows.sort(java.util.Comparator.comparing(Node::getId));
        assertEquals(names.size(), rows.size());
        for (int i = 0; i < names.size(); i++) {
            final VBox row = rows.get(i);
            assertEquals(names.get(i),
                    ((Label) row.getChildren().get(0)).getText());
            assertEquals(states.get(i),
                    ((Label) row.getChildren().get(1)).getText());
            assertEquals(2,
                    row.getChildren().stream().filter(Label.class::isInstance)
                            .count(),
                    "Only names and relation state belong in the political readout");
            for (final Node node : row.getChildren()) {
                final javafx.geometry.Bounds bounds = node
                        .localToScene(node.getBoundsInLocal());
                assertTrue(bounds.getMinX() >= 0
                        && bounds.getMaxX() <= node.getScene().getWidth(),
                        "Relation text and actions must fit the window");
            }
        }
        assertEquals(declarationCount,
                rows.stream().flatMap(row -> row.getChildren().stream())
                        .filter(Button.class::isInstance)
                        .filter(node -> node.getId() != null
                                && node.getId().startsWith("declare-"))
                        .count());
        assertTrue(getListOfNodes(".chart").isEmpty());
    }

    public void verifyReview(final String first, final String second,
            final String consequence) {
        final String text = getLabeledById("declarationMessage").getText();
        assertTrue(text.contains(first));
        assertTrue(text.contains(second));
        assertTrue(text.contains(consequence));
        assertTrue(reviewWindow.isShowing());
        assertFalse(getButtonById("declarationConfirm").isDefaultButton());
    }

    public void verifyReviewClosed() {
        assertFalse(reviewWindow.isShowing());
    }

    public void verifyFeedback(final String expected) {
        assertTrue(getLabeledById("relationsFeedback").getText()
                .contains(expected));
    }

    public void verifyReplacementUntouched() {
        assertNotSame(original, getModel());
        assertEquals(EuropeanRelationState.PEACE,
                original.getEuropeanRelation(original.getPlayerByName("Dutch"),
                        original.getPlayerByName("France")));
        assertEquals(EuropeanRelationState.PEACE,
                getModel().getEuropeanRelation(
                        getModel().getPlayerByName("Dutch"),
                        getModel().getPlayerByName("France")));
    }

    public void verifyColonyAndForce(final String owner, final Location forceAt,
            final int movement) {
        final Colony colony = getModel().getColonyById(0).orElseThrow();
        assertEquals(owner, colony.getOwner().getName());
        assertEquals("Quebec", colony.getName());
        final UnitFreeColonist force = (UnitFreeColonist) getModel()
                .getUnitById(0);
        assertTrue(force.isAtPlaceLocation());
        assertEquals(forceAt, force.getLocation());
        assertTrue(force.isHoldingGuns());
        assertEquals(movement, force.getActionPoints());
        assertEquals(colonyStores, stocks());
        assertEquals(List.of(owner),
                colony.getUnitsInsideColony().stream()
                        .map(unit -> unit.getOwner().getName()).distinct()
                        .collect(Collectors.toList()));
    }
}
