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

    private final static Map<ConstructionType, PanelConstructionProvider> constructionProviders = ImmutableMap
            .<ConstructionType, PanelConstructionProvider>builder()
            // warehouse
            .put(ConstructionType.WAREHOUSE_EXPANSION,
                    new PanelConstructionProvider("id_warehouse"))
            .put(ConstructionType.WAREHOUSE, new PanelConstructionProvider("id_warehouse"))
            .put(ConstructionType.WAREHOUSE_BASIC, new PanelConstructionProvider("id_warehouse"))
            // Townhall
            .put(ConstructionType.TOWN_HALL, new PanelConstructionProvider("id_town_hall"))
            // Church
            .put(ConstructionType.CHAPEL, new PanelConstructionProvider("id_church"))
            .put(ConstructionType.CATHEDRAL, new PanelConstructionProvider("id_church"))
            .put(ConstructionType.CHURCH, new PanelConstructionProvider("id_church"))
            // Fort
            .put(ConstructionType.FORTRESS, new PanelConstructionProvider("id_fort"))
            .put(ConstructionType.FORT, new PanelConstructionProvider("id_fort"))
            .put(ConstructionType.STOCKADE, new PanelConstructionProvider("id_fort"))
            // weaver
            .put(ConstructionType.TEXTILE_MILL, new PanelConstructionProvider("id_weaver"))
            .put(ConstructionType.WEAVERS_SHOP, new PanelConstructionProvider("id_weaver"))
            .put(ConstructionType.WEAVERS_HOUSE, new PanelConstructionProvider("id_weaver"))
            // cigars
            .put(ConstructionType.CIGAR_FACTORY, new PanelConstructionProvider("id_tobacco"))
            .put(ConstructionType.TOBACCONISTS_SHOP, new PanelConstructionProvider("id_tobacco"))
            .put(ConstructionType.TOBACCONISTS_HOUSE, new PanelConstructionProvider("id_tobacco"))
            // rum
            .put(ConstructionType.RUM_FACTORY, new PanelConstructionProvider("id_rum"))
            .put(ConstructionType.RUM_DISTILLERY, new PanelConstructionProvider("id_rum"))
            .put(ConstructionType.RUM_DISTILLERS_HOUSE, new PanelConstructionProvider("id_rum"))
            // School
            .put(ConstructionType.UNIVERSITY, new PanelConstructionProvider("id_school"))
            .put(ConstructionType.COLLEGE, new PanelConstructionProvider("id_school"))
            .put(ConstructionType.SCHOOLHOUSE, new PanelConstructionProvider("id_school"))
            // Guns
            .put(ConstructionType.ARSENAL, new PanelConstructionProvider("id_armory"))
            .put(ConstructionType.MAGAZINE, new PanelConstructionProvider("id_armory"))
            .put(ConstructionType.ARMORY, new PanelConstructionProvider("id_armory"))
            // Iron
            .put(ConstructionType.IRON_WORKS, new PanelConstructionProvider("id_blacksmith"))
            .put(ConstructionType.BLACKSMITHS_SHOP, new PanelConstructionProvider("id_blacksmith"))
            .put(ConstructionType.BLACKSMITHS_HOUSE, new PanelConstructionProvider("id_blacksmith"))
            // fur
            .put(ConstructionType.FUR_FACTORY, new PanelConstructionProvider("id_fur"))
            .put(ConstructionType.FUR_TRADING_POST, new PanelConstructionProvider("id_fur"))
            .put(ConstructionType.FUR_TRADERS_HOUSE, new PanelConstructionProvider("id_fur"))
            // lumber
            .put(ConstructionType.LUMBER_MILL, new PanelConstructionProvider("id_carpenter"))
            .put(ConstructionType.CARPENTERS_SHOP, new PanelConstructionProvider("id_carpenter"))
            .put(ConstructionType.CARPENTERS_STAND, new PanelConstructionProvider("id_carpenter"))
            /*
             * TODO following construction should be visible in colony
             */
            // Dock
            .put(ConstructionType.SHIPYARD, new PanelConstructionProvider("id_"))
            .put(ConstructionType.DRYDOCK, new PanelConstructionProvider("id_"))
            .put(ConstructionType.DOCK, new PanelConstructionProvider("id_"))
            // stable
            .put(ConstructionType.LARGE_STABLES, new PanelConstructionProvider("id_"))
            .put(ConstructionType.STABLES, new PanelConstructionProvider("id_"))
            // Newspaper
            .put(ConstructionType.NEWSPAPER, new PanelConstructionProvider("id_"))
            .put(ConstructionType.PRINTING_PRESS, new PanelConstructionProvider("id_"))
            // Custom house
            .put(ConstructionType.CUSTOM_HOUSE, new PanelConstructionProvider("id_"))

            .build();

    /**
     * Validation that constants are consistent.
     */
    static {
        Preconditions.checkState(ConstructionType.ALL.size() == constructionProviders.size(),
                String.format(
                        "There is different number of construction types '%s' and it's definitions of providers '%s'.",
                        ConstructionType.ALL.size(), constructionProviders.size()));
    }
    
    private final StackPane mainPanel = new StackPane();

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;

    @Inject
    PanelColonyStructures(final ImageProvider imageProvider,
            final GameModelController gameModelController, final EventBus eventBus,
            final I18n i18n) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        mainPanel.getStyleClass().add("colony-structures");
    }

    void repaint(final Colony colony) {
        mainPanel.getChildren().clear();
        colony.getConstructions().forEach(construction -> addConstruction(construction, colony));
    }

    private void addConstruction(final Construction construction, final Colony colony) {
        final PanelConstructionProvider provider = constructionProviders
                .get(construction.getType());
        final PanelConstructionContext context = new PanelConstructionContext(imageProvider,
                eventBus, i18n, construction, colony.getGoodsStats(),
                gameModelController.getModel());
        final PanelConstruction panel = provider.make(context);
        mainPanel.getChildren().add(panel.getContent());
        panel.paint();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
