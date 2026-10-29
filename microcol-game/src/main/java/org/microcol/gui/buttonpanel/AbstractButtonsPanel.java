package org.microcol.gui.buttonpanel;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.colony.ScreenColony;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Simplify creating of top panel with list of buttons.
 */
public class AbstractButtonsPanel implements JavaFxComponent {

    private static final String STYLE_SHEET_BUTTONS_PANEL = ScreenColony.class
            .getResource("/gui/ButtonsPanel.css").toExternalForm();

    private final Source source;
    private final ImageProvider imageProvider;
    private final EventBus eventBus;
    private final I18n i18n;

    private final VBox mainPanel;
    private final FlowPane buttonPanel;

    private boolean updatingToolbarLayout;

    protected AbstractButtonsPanel(final ImageProvider imageProvider,
            final EventBus eventBus, final Source source, final I18n i18n) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.source = Preconditions.checkNotNull(source);
        this.i18n = Preconditions.checkNotNull(i18n);

        buttonPanel = new FlowPane();
        buttonPanel.getStyleClass().add("buttonPanel");

        mainPanel = new VBox();
        mainPanel.getStyleClass().add("buttons-panel-container");
        mainPanel.getChildren().add(buttonPanel);
        mainPanel.setPickOnBounds(false);
        mainPanel.getStylesheets().add(STYLE_SHEET_BUTTONS_PANEL);
        VBox.setVgrow(mainPanel, Priority.ALWAYS);
        final InvalidationListener layoutListener = observable -> updateToolbarLayout();
        buttonPanel.widthProperty().addListener(layoutListener);
        buttonPanel.sceneProperty().addListener(layoutListener);
        buttonPanel.getChildren()
                .addListener((ListChangeListener<Node>) change -> {
                    while (change.next()) {
                        for (final Node node : change.getRemoved()) {
                            if (node instanceof Labeled) {
                                final Labeled control = (Labeled) node;
                                control.textProperty()
                                        .removeListener(layoutListener);
                                control.fontProperty()
                                        .removeListener(layoutListener);
                                control.graphicProperty()
                                        .removeListener(layoutListener);
                                control.managedProperty()
                                        .removeListener(layoutListener);
                            }
                        }
                        for (final Node node : change.getAddedSubList()) {
                            if (node instanceof Labeled) {
                                final Labeled control = (Labeled) node;
                                control.textProperty()
                                        .addListener(layoutListener);
                                control.fontProperty()
                                        .addListener(layoutListener);
                                control.graphicProperty()
                                        .addListener(layoutListener);
                                control.managedProperty()
                                        .addListener(layoutListener);
                            }
                        }
                    }
                    updateToolbarLayout();
                });
    }

    private void updateToolbarLayout() {
        if (updatingToolbarLayout || getButtonPanel().getScene() == null
                || getButtonPanel().getWidth() <= 0) {
            return;
        }
        final List<Labeled> controls = buttonPanel.getChildren().stream()
                .filter(Labeled.class::isInstance).map(Labeled.class::cast)
                .filter(control -> control.getGraphic() != null)
                .collect(Collectors.toList());
        updatingToolbarLayout = true;
        try {
            getButtonPanel().applyCss();
            controls.forEach(
                    control -> control.setContentDisplay(ContentDisplay.LEFT));
            final double namedWidth = toolbarPreferredWidth();
            controls.forEach(control -> control.setContentDisplay(
                    control.getStyleClass().contains("back-to-game-button")
                            ? ContentDisplay.LEFT
                            : ContentDisplay.GRAPHIC_ONLY));
            final double iconWidth = toolbarPreferredWidth();
            if (getButtonPanel().getWidth() >= namedWidth) {
                controls.forEach(control -> control
                        .setContentDisplay(ContentDisplay.LEFT));
            } else {
                for (final Labeled control : controls) {
                    if (control.getTooltip() == null
                            && !control.getText().isEmpty()
                            && !control.getStyleClass()
                                    .contains("game-toolbar-control")) {
                        final Tooltip tooltip = new Tooltip();
                        tooltip.textProperty().bind(control.textProperty());
                        control.setTooltip(tooltip);
                    }
                }
            }
            final Window window = getButtonPanel().getScene().getWindow();
            if (window instanceof Stage) {
                final Stage stage = (Stage) window;
                double frameWidth = 0;
                for (Node parent = buttonPanel
                        .getParent(); parent != null; parent = parent
                                .getParent()) {
                    if (parent instanceof org.microcol.gui.util.GameScreenHeader) {
                        final org.microcol.gui.util.GameScreenHeader header = (org.microcol.gui.util.GameScreenHeader) parent;
                        frameWidth = header.getInsets().getLeft()
                                + header.getInsets().getRight()
                                + header.getHeadingWidth();
                        break;
                    }
                }
                final double borders = Math.max(0, stage.getWidth()
                        - getButtonPanel().getScene().getWidth());
                stage.setMinWidth(Math.max(stage.getMinWidth(),
                        Math.ceil(iconWidth + frameWidth + borders)));
                if (stage.getWidth() < stage.getMinWidth()) {
                    stage.setWidth(stage.getMinWidth());
                }
            }
        } finally {
            updatingToolbarLayout = false;
        }
    }

    private double toolbarPreferredWidth() {
        final List<Node> controls = getButtonPanel().getChildren().stream()
                .filter(Node::isManaged).collect(Collectors.toList());
        return getButtonPanel().getInsets().getLeft()
                + getButtonPanel().getInsets().getRight()
                + Math.max(0, controls.size() - 1)
                        * getButtonPanel().getHgap()
                + controls.stream()
                        .mapToDouble(
                                control -> Math
                                        .ceil(Math.max(control.minWidth(-1),
                                                Math.min(control.prefWidth(-1),
                                                        control.maxWidth(-1)))))
                        .sum();
    }

    protected <T extends Enum<T> & MessageKeyResource> Button makeButton(
            final ButtonIcon icon, final T buttonsKey) {
        final ButtonImage<T> buttonImage = new ButtonImage<T>(eventBus, source,
                i18n, buttonsKey, icon);
        return buttonImage.getContent();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    protected boolean isContaining(final Node node) {
        return buttonPanel.getChildren().stream().filter(n -> n.equals(node))
                .findFirst().isPresent();
    }

    protected void remove(final Node node) {
        buttonPanel.getChildren().remove(node);
    }

    /**
     * @return the buttonPanel
     */
    public FlowPane getButtonPanel() {
        return buttonPanel;
    }

}
