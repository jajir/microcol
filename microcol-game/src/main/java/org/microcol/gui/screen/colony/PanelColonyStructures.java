package org.microcol.gui.screen.colony;

import java.util.Map;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Construction;
import org.microcol.model.ConstructionType;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Show building factories and other structures build in colony.
 */
@Singleton
final class PanelColonyStructures implements JavaFxComponent {

    private final Map<ConstructionType, PanelConstructionFactory> constructionProviders;

    private final StackPane mainPanel = new StackPane();

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;

    @Inject
    PanelColonyStructures(final ImageProvider imageProvider,
            final GameModelController gameModelController, final EventBus eventBus, final I18n i18n,
            final PanelConstructionWarehouseFactory panelConstructionWarehouseFactory) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        constructionProviders = ImmutableMap.<ConstructionType, PanelConstructionFactory>builder()
                // warehouse
                .put(ConstructionType.WAREHOUSE_EXPANSION, panelConstructionWarehouseFactory)
                .put(ConstructionType.WAREHOUSE, panelConstructionWarehouseFactory)
                .put(ConstructionType.WAREHOUSE_BASIC, panelConstructionWarehouseFactory)
                // Townhall
                .put(ConstructionType.TOWN_HALL, new PanelConstructionBasicFactory("id_town_hall"))
                // Church
                .put(ConstructionType.CHAPEL, new PanelConstructionBasicFactory("id_church"))
                .put(ConstructionType.CATHEDRAL, new PanelConstructionBasicFactory("id_church"))
                .put(ConstructionType.CHURCH, new PanelConstructionBasicFactory("id_church"))
                // Fort
                .put(ConstructionType.FORTRESS, new PanelConstructionBasicFactory("id_fort"))
                .put(ConstructionType.FORT, new PanelConstructionBasicFactory("id_fort"))
                .put(ConstructionType.STOCKADE, new PanelConstructionBasicFactory("id_fort"))
                // weaver
                .put(ConstructionType.TEXTILE_MILL, new PanelConstructionBasicFactory("id_weaver"))
                .put(ConstructionType.WEAVERS_SHOP, new PanelConstructionBasicFactory("id_weaver"))
                .put(ConstructionType.WEAVERS_HOUSE, new PanelConstructionBasicFactory("id_weaver"))
                // cigars
                .put(ConstructionType.CIGAR_FACTORY,
                        new PanelConstructionBasicFactory("id_tobacco"))
                .put(ConstructionType.TOBACCONISTS_SHOP,
                        new PanelConstructionBasicFactory("id_tobacco"))
                .put(ConstructionType.TOBACCONISTS_HOUSE,
                        new PanelConstructionBasicFactory("id_tobacco"))
                // rum
                .put(ConstructionType.RUM_FACTORY, new PanelConstructionBasicFactory("id_rum"))
                .put(ConstructionType.RUM_DISTILLERY, new PanelConstructionBasicFactory("id_rum"))
                .put(ConstructionType.RUM_DISTILLERS_HOUSE,
                        new PanelConstructionBasicFactory("id_rum"))
                // School
                .put(ConstructionType.UNIVERSITY, new PanelConstructionBasicFactory("id_school"))
                .put(ConstructionType.COLLEGE, new PanelConstructionBasicFactory("id_school"))
                .put(ConstructionType.SCHOOLHOUSE, new PanelConstructionBasicFactory("id_school"))
                // Guns
                .put(ConstructionType.ARSENAL, new PanelConstructionBasicFactory("id_armory"))
                .put(ConstructionType.MAGAZINE, new PanelConstructionBasicFactory("id_armory"))
                .put(ConstructionType.ARMORY, new PanelConstructionBasicFactory("id_armory"))
                // Iron
                .put(ConstructionType.IRON_WORKS,
                        new PanelConstructionBasicFactory("id_blacksmith"))
                .put(ConstructionType.BLACKSMITHS_SHOP,
                        new PanelConstructionBasicFactory("id_blacksmith"))
                .put(ConstructionType.BLACKSMITHS_HOUSE,
                        new PanelConstructionBasicFactory("id_blacksmith"))
                // fur
                .put(ConstructionType.FUR_FACTORY, new PanelConstructionBasicFactory("id_fur"))
                .put(ConstructionType.FUR_TRADING_POST, new PanelConstructionBasicFactory("id_fur"))
                .put(ConstructionType.FUR_TRADERS_HOUSE,
                        new PanelConstructionBasicFactory("id_fur"))
                // lumber
                .put(ConstructionType.LUMBER_MILL,
                        new PanelConstructionCarpenterFactory("id_carpenter"))
                .put(ConstructionType.CARPENTERS_SHOP,
                        new PanelConstructionCarpenterFactory("id_carpenter"))
                .put(ConstructionType.CARPENTERS_STAND,
                        new PanelConstructionCarpenterFactory("id_carpenter"))
                // Dock
                .put(ConstructionType.SHIPYARD, new PanelConstructionEmptyFactory())
                .put(ConstructionType.DRYDOCK, new PanelConstructionEmptyFactory())
                .put(ConstructionType.DOCK, new PanelConstructionEmptyFactory())
                // stable
                .put(ConstructionType.LARGE_STABLES, new PanelConstructionEmptyFactory())
                .put(ConstructionType.STABLES, new PanelConstructionEmptyFactory())
                // Newspaper
                .put(ConstructionType.NEWSPAPER, new PanelConstructionEmptyFactory())
                .put(ConstructionType.PRINTING_PRESS, new PanelConstructionEmptyFactory())
                // Custom house
                .put(ConstructionType.CUSTOM_HOUSE, new PanelConstructionEmptyFactory())

                .build();

        // Validate that number of construction factories is same as number of
        // constructions.
        Preconditions.checkState(ConstructionType.ALL.size() == constructionProviders.size(),
                String.format(
                        "There is different number of construction types '%s' and it's definitions of providers '%s'.",
                        ConstructionType.ALL.size(), constructionProviders.size()));

        mainPanel.getStyleClass().add("colony-structures");
    }

    void repaint(final Colony colony) {
        mainPanel.getChildren().clear();
        colony.getConstructions().forEach(construction -> addConstruction(construction, colony));
    }

    private void addConstruction(final Construction construction, final Colony colony) {
        final PanelConstructionFactory provider = constructionProviders.get(construction.getType());
        final PanelConstructionContext context = new PanelConstructionContext(imageProvider,
                eventBus, i18n, construction, colony, gameModelController.getModel());
        final AbstractPanelConstruction panel = provider.make(context);
        mainPanel.getChildren().add(panel.getContent());
        panel.paint();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
