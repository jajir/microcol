package org.microcol.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import org.microcol.MicroColApplication;
import org.microcol.model.Model;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.finder.NodeFinder;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.base.Preconditions;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public abstract class AbstractScreen {

    private static final Set<String> UNIT_COMMANDS = Set.of("move",
            "buildColony", "plowField", "cancelPlowField", "clearForest",
            "cancelClearForest", "buildRoad", "cancelBuildRoad", "waitUnit",
            "skipUnitTurn", "sentryUnit", "fortifyUnit", "goToUnit",
            "goToColonyUnit", "disbandUnit", "jettisonUnit");

    private final TestContext context;

    AbstractScreen(final TestContext context) {
        this.context = Preconditions.checkNotNull(context);
    }

    protected TestContext getContext() {
        return context;
    }

    protected MicroColApplication getMicroCol() {
        return context.getMicroCol();
    }

    protected Stage getPrimaryStage() {
        return context.getPrimaryStage();
    }

    protected FxRobot getRobot() {
        return context.getRobot();
    }

    protected KeyCode[] getGoodsAmountModifiers() {
        // JavaFX LINK uses Control on macOS and Control+Shift on Linux/Windows.
        return System.getProperty("os.name").startsWith("Mac")
                ? new KeyCode[] { KeyCode.CONTROL }
                : new KeyCode[] { KeyCode.CONTROL, KeyCode.SHIFT };
    }

    protected Model getModel() {
        return context.getModel();
    }

    protected NodeFinder getNodeFinder() {
        return FxAssert.assertContext().getNodeFinder();
    }

    protected void clickOnButtonWithId(final String buttonId) {
        if (UNIT_COMMANDS.contains(buttonId)) {
            openUnitActions();
        } else {
            closeUnitActions();
        }
        final String id = "#" + buttonId;
        getRobot().clickOn(id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    protected Labeled getLabeledById(final String cssId) {
        final String id = "#" + cssId;
        final Labeled label = getNodeFinder().lookup(id).queryLabeled();
        return Preconditions.checkNotNull(label,
                String.format("unable to find labeled by id '%s'", cssId));
    }

    protected Button getButtonById(final String cssId) {
        if (UNIT_COMMANDS.contains(cssId)) {
            openUnitActions();
        }
        final String id = "#" + cssId;
        final Button label = getNodeFinder().lookup(id).queryButton();
        return Preconditions.checkNotNull(label,
                String.format("unable to find button by id '%s'", cssId));
    }

    protected void openUnitActions() {
        WaitForAsyncUtils.waitForFxEvents();
        if (getNodeFinder().lookup("#waitUnit").tryQuery().isEmpty()
                && getNodeFinder().lookup("#unitActions").tryQuery()
                        .isPresent()) {
            getRobot().clickOn("#unitActions");
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    protected void closeUnitActions() {
        if (getNodeFinder().lookup("#waitUnit").tryQuery().isPresent()) {
            getRobot().push(KeyCode.ESCAPE);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    protected boolean isButtonBackVisible() {
        return isCssIdVisible("closeButtonId");
    }

    public void captureDialog(final String contentId, final String fileName)
            throws IOException {
        if (UNIT_COMMANDS.contains(contentId)) {
            openUnitActions();
        }
        final Node root = getNodeFinder().lookup("#" + contentId).query()
                .getScene().getRoot();
        final Path output = Path.of("target", "warehouse-qa", fileName);
        Files.createDirectories(output.getParent());
        // Snapshot layout otherwise uses the menu's oversized preferred
        // dimensions.
        final javafx.scene.image.WritableImage image = WaitForAsyncUtils
                .waitForAsyncFx(5000, () -> snapshotScene(root.getScene()));
        getRobot().robotContext().getCaptureSupport()
                .saveImage(getRobot().capture(image).getImage(), output);
    }

    public static javafx.scene.image.WritableImage snapshotScene(
            final javafx.scene.Scene scene) {
        final javafx.scene.layout.Region region = (javafx.scene.layout.Region) scene
                .getRoot();
        final double width = scene.getWidth();
        final double height = scene.getHeight();
        final double preferredWidth = region.getPrefWidth();
        final double preferredHeight = region.getPrefHeight();
        try {
            region.setPrefSize(width, height);
            return scene.snapshot(null);
        } finally {
            region.setPrefSize(preferredWidth, preferredHeight);
            region.resize(width, height);
            region.layout();
        }
    }

    protected boolean isCssIdVisible(final String cssId) {
        if (UNIT_COMMANDS.contains(cssId)) {
            openUnitActions();
            return isCssIdShown(cssId);
        }
        final String id = "#" + cssId;
        return getNodeFinder().lookup(id).tryQuery().isPresent();
    }

    /**
     * Unlike {@link #isCssIdVisible(String)}, which only finds the node, this
     * also requires the node and every parent to be visible, so a control
     * hidden in place with setVisible(false) does not count as shown.
     */
    protected boolean isCssIdShown(final String cssId) {
        final java.util.Optional<Node> found = getNodeFinder()
                .lookup("#" + cssId).tryQuery();
        if (found.isEmpty()) {
            return false;
        }
        for (Node node = found.get(); node != null; node = node.getParent()) {
            if (!node.isVisible()) {
                return false;
            }
        }
        return true;
    }

    protected <T extends Node> List<T> getListOfNodes(final String cssClass) {
        final Set<T> cratesSet = getNodeFinder().lookup(cssClass).queryAll();
        return new ArrayList<T>(cratesSet);
    }

}
