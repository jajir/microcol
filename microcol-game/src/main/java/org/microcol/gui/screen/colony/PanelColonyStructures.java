package org.microcol.gui.screen.colony;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Construction;
import org.microcol.model.ConstructionType;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Stable positions for all sixteen building families and their upgrades. */
@Singleton
final class PanelColonyStructures implements JavaFxComponent {
    private static final ConstructionType[] FAMILIES = {
            ConstructionType.TOWN_HALL, ConstructionType.CHAPEL,
            ConstructionType.SCHOOLHOUSE, ConstructionType.PRINTING_PRESS,
            ConstructionType.CARPENTERS_STAND,
            ConstructionType.BLACKSMITHS_HOUSE, ConstructionType.ARMORY,
            ConstructionType.STOCKADE, ConstructionType.TOBACCONISTS_HOUSE,
            ConstructionType.WEAVERS_HOUSE,
            ConstructionType.RUM_DISTILLERS_HOUSE,
            ConstructionType.FUR_TRADERS_HOUSE,
            ConstructionType.WAREHOUSE_BASIC, ConstructionType.STABLES,
            ConstructionType.DOCK, ConstructionType.CUSTOM_HOUSE };
    private static final String[] IDS = { "id_town_hall", "id_church",
            "id_school", "id_press", "id_carpenter", "id_blacksmith",
            "id_armory", "id_fort", "id_tobacco", "id_weaver", "id_rum",
            "id_fur", "id_warehouse", "id_stables", "id_dock", "id_customs" };
    private final GridPane mainPanel = new GridPane();
    private final ImageProvider images;
    private final GameModelController controller;
    private final I18n i18n;
    private final EventBus events;

    @Inject
    PanelColonyStructures(final ImageProvider images,
            final GameModelController controller, final EventBus events,
            final I18n i18n) {
        this.images = images;
        this.controller = controller;
        this.events = events;
        this.i18n = i18n;
        mainPanel.getStyleClass().add("colony-structures");
        for (int i = 0; i < 4; i++) {
            final javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();
            column.setPercentWidth(25);
            column.setHalignment(javafx.geometry.HPos.CENTER);
            mainPanel.getColumnConstraints().add(column);
        }
    }

    void repaint(final Colony colony) {
        mainPanel.getChildren().clear();
        for (int i = 0; i < FAMILIES.length; i++) {
            final ConstructionType family = FAMILIES[i];
            final Construction construction = colony
                    .getConstructions().stream().filter(item -> item.getType()
                            .getUpgradeFromChain().contains(family))
                    .findFirst().orElse(null);
            final Region content;
            if (construction == null) {
                final ImageView image = new ImageView(
                        images.getConstructionImage(family).orElseThrow());
                image.setFitWidth(137);
                image.setFitHeight(68.5);
                image.setOpacity(0.3);
                final Label name = new Label(
                        i18n.get(ConstructionTypeName.getNameForType(family)));
                name.getStyleClass().add("building-name");
                final Label state = new Label(i18n.get(ColonyMsg.notBuilt));
                state.getStyleClass().add("building-state");
                final VBox empty = new VBox(image, name, state);
                empty.getStyleClass().addAll("constructionPane",
                        "building-empty");
                empty.setId(IDS[i]);
                empty.setUserData(family);
                content = empty;
            } else {
                final PanelConstructionBasic panel = new PanelConstructionBasic(
                        images, events, i18n, construction,
                        colony.getGoodsStats(), controller.getModel());
                panel.setCssId(IDS[i]);
                panel.paint();
                content = panel.getContent();
            }
            mainPanel.add(content, i % 4, i / 4);
        }
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }
}
