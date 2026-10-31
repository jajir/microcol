package org.microcol.gui.screen.colony;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.event.UnitMovedToColonyFieldEvent;
import org.microcol.model.event.UnitMovedToConstructionEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

/**
 * The colony workspace, using shared model operations and cargo controls.
 */
@Listener
public final class ColonyPanel implements JavaFxComponent, UpdatableLanguage {

    private final VBox mainPanel;
    private final Label outsideTitle = new Label();
    private final Label harborTitle = new Label();
    private final Label goodsTitle = new Label();
    private final PanelColonyConstruction construction;
    private final I18n i18n;

    private final PanelColonyName panelColonyName;

    private final PanelColonyFields colonyFields;

    private final PanelColonyStructures colonyStructures;

    private final PanelColonyGoods goods;

    private final PanelDockColony panelDock;

    private final PanelOutsideColony panelOutsideColony;

    private Colony colony;
    private final ColonyButtonsPanel buttons;

    private final ColonyQuickCargo quickCargo;

    private final EventBus eventBus;

    @Inject
    public ColonyPanel(final PanelColonyFields panelColonyFields,
            final PanelColonyStructures panelColonyStructures,
            final PanelOutsideColony panelOutsideColony,
            final PanelColonyGoods panelColonyGoods,
            final ColonyButtonsPanel colonyButtonsPanel,
            final PanelDockColony panelDockColony,
            final PanelColonyName panelColonyName,
            final ColonyQuickCargo colonyQuickCargo, final EventBus eventBus,
            final PanelColonyConstruction construction, final I18n i18n) {
        this.construction = construction;
        this.i18n = i18n;
        this.buttons = colonyButtonsPanel;
        this.quickCargo = Preconditions.checkNotNull(colonyQuickCargo);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        colonyButtonsPanel.addQuickCargoButtons(quickCargo.getButtons());
        this.colonyFields = Preconditions.checkNotNull(panelColonyFields);
        this.colonyStructures = Preconditions
                .checkNotNull(panelColonyStructures);
        this.panelDock = Preconditions.checkNotNull(panelDockColony);
        this.goods = Preconditions.checkNotNull(panelColonyGoods);
        this.panelOutsideColony = Preconditions
                .checkNotNull(panelOutsideColony);
        this.panelColonyName = Preconditions.checkNotNull(panelColonyName);

        final ScrollPane outside = new ScrollPane(
                panelOutsideColony.getContent());
        outside.setId("colony-outside-scroll");
        outside.setFitToHeight(true);
        outside.setFitToWidth(true);
        outside.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        outside.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        outside.getStyleClass().add("colony-unit-scroll");
        final VBox people = new VBox(3, outsideTitle, outside);
        people.getStyleClass().add("colony-section");
        final VBox harbor = new VBox(3, harborTitle, panelDock.getContent());
        harbor.getStyleClass().addAll("colony-section", "colony-harbor");
        final VBox right = new VBox(10, colonyFields.getContent(),
                construction.getContent(), harbor, people);
        right.setMinWidth(378);
        right.setPrefWidth(520);
        right.setMaxWidth(Double.MAX_VALUE);
        right.getStyleClass().add("colony-details");
        HBox.setHgrow(right, Priority.ALWAYS);
        HBox.setHgrow(colonyStructures.getContent(), Priority.ALWAYS);
        final HBox town = new HBox(18, colonyStructures.getContent(), right);
        town.setId("colony-town");
        final VBox stock = new VBox(2, goodsTitle, goods.getContent());
        stock.getStyleClass().add("colony-stock");
        mainPanel = new VBox(10, town, stock);
        mainPanel.getStyleClass().add("colony-layout");
        mainPanel.setMinWidth(980);
        mainPanel.setMaxWidth(1440);
        updateLanguage(i18n);

        mainPanel.getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    @Subscribe
    private void onUnitMovedOutsideColony(
            @SuppressWarnings("unused") final UnitMovedOutsideColonyEvent event) {
        repaint();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onUnitMovedToConstruction(
            final UnitMovedToConstructionEvent event) {
        // Keep the native drop target attached until its event finishes.
        javafx.application.Platform.runLater(this::repaint);
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onUnitMovedToField(final UnitMovedToColonyFieldEvent event) {
        repaint();
    }

    @SuppressWarnings("unused")
    @Subscribe
    private void onRepainColonyEvent(final RepaintColonyEvent event) {
        repaint();
    }

    /**
     * Method should show given colony.
     *
     * @param colony required colony to show
     */
    void showColony(final Colony colony) {
        this.colony = Preconditions.checkNotNull(colony);
        panelColonyName.setColony(colony);
        goods.setColony(colony);
        repaint();
    }

    void repaint() {
        if (colony != null) {
            if (!colony.isValid()) {
                colony = null;
                eventBus.post(new ShowScreenEvent(Screen.GAME));
                return;
            }
            buttons.setColony(colony);
            colonyFields.setColony(colony);
            goods.repaint();
            panelDock.repaint();
            colonyStructures.repaint(colony);
            construction.repaint(colony);
            panelOutsideColony.setColony(colony);
            panelOutsideColony.repaint();
        }
    }

    public Colony getColony() {
        return colony;
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        panelDock.updateLanguage(i18n);
        quickCargo.updateLanguage(i18n);
        outsideTitle.setText(i18n.get(ColonyMsg.outsideSection));
        harborTitle.setText(i18n.get(ColonyMsg.harborSection));
        goodsTitle.setText(i18n.get(ColonyMsg.stockSection));
        repaint();
    }
}
