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

    private final static Map<ConstructionType, ConstructionProvider> constructionProviders = ImmutableMap
            .<ConstructionType, ConstructionProvider>builder()
            // warehouse
            .put(ConstructionType.WAREHOUSE_EXPANSION,
                    new PanelConstructionProviderSimple("id_warehouse"))
            .put(ConstructionType.WAREHOUSE, new PanelConstructionProviderSimple("id_warehouse"))
            .put(ConstructionType.WAREHOUSE_BASIC, new PanelConstructionProviderSimple("id_warehouse"))
            // Townhall
            .put(ConstructionType.TOWN_HALL, new PanelConstructionProviderBasic("id_town_hall"))
            // Church
            .put(ConstructionType.CHAPEL, new PanelConstructionProviderBasic("id_church"))
            .put(ConstructionType.CATHEDRAL, new PanelConstructionProviderBasic("id_church"))
            .put(ConstructionType.CHURCH, new PanelConstructionProviderBasic("id_church"))
            // Fort
            .put(ConstructionType.FORTRESS, new PanelConstructionProviderBasic("id_fort"))
            .put(ConstructionType.FORT, new PanelConstructionProviderBasic("id_fort"))
            .put(ConstructionType.STOCKADE, new PanelConstructionProviderBasic("id_fort"))
            // weaver
            .put(ConstructionType.TEXTILE_MILL, new PanelConstructionProviderBasic("id_weaver"))
            .put(ConstructionType.WEAVERS_SHOP, new PanelConstructionProviderBasic("id_weaver"))
            .put(ConstructionType.WEAVERS_HOUSE, new PanelConstructionProviderBasic("id_weaver"))
            // cigars
            .put(ConstructionType.CIGAR_FACTORY, new PanelConstructionProviderBasic("id_tobacco"))
            .put(ConstructionType.TOBACCONISTS_SHOP, new PanelConstructionProviderBasic("id_tobacco"))
            .put(ConstructionType.TOBACCONISTS_HOUSE, new PanelConstructionProviderBasic("id_tobacco"))
            // rum
            .put(ConstructionType.RUM_FACTORY, new PanelConstructionProviderBasic("id_rum"))
            .put(ConstructionType.RUM_DISTILLERY, new PanelConstructionProviderBasic("id_rum"))
            .put(ConstructionType.RUM_DISTILLERS_HOUSE, new PanelConstructionProviderBasic("id_rum"))
            // School
            .put(ConstructionType.UNIVERSITY, new PanelConstructionProviderBasic("id_school"))
            .put(ConstructionType.COLLEGE, new PanelConstructionProviderBasic("id_school"))
            .put(ConstructionType.SCHOOLHOUSE, new PanelConstructionProviderBasic("id_school"))
            // Guns
            .put(ConstructionType.ARSENAL, new PanelConstructionProviderBasic("id_armory"))
            .put(ConstructionType.MAGAZINE, new PanelConstructionProviderBasic("id_armory"))
            .put(ConstructionType.ARMORY, new PanelConstructionProviderBasic("id_armory"))
            // Iron
            .put(ConstructionType.IRON_WORKS, new PanelConstructionProviderBasic("id_blacksmith"))
            .put(ConstructionType.BLACKSMITHS_SHOP, new PanelConstructionProviderBasic("id_blacksmith"))
            .put(ConstructionType.BLACKSMITHS_HOUSE, new PanelConstructionProviderBasic("id_blacksmith"))
            // fur
            .put(ConstructionType.FUR_FACTORY, new PanelConstructionProviderBasic("id_fur"))
            .put(ConstructionType.FUR_TRADING_POST, new PanelConstructionProviderBasic("id_fur"))
            .put(ConstructionType.FUR_TRADERS_HOUSE, new PanelConstructionProviderBasic("id_fur"))
            // lumber
            .put(ConstructionType.LUMBER_MILL, new PanelConstructionProviderCarpenter("id_carpenter"))
            .put(ConstructionType.CARPENTERS_SHOP, new PanelConstructionProviderCarpenter("id_carpenter"))
            .put(ConstructionType.CARPENTERS_STAND, new PanelConstructionProviderCarpenter("id_carpenter"))
            // Dock
            .put(ConstructionType.SHIPYARD, new PanelConstructionProviderEmpty())
            .put(ConstructionType.DRYDOCK, new PanelConstructionProviderEmpty())
            .put(ConstructionType.DOCK, new PanelConstructionProviderEmpty())
            // stable
            .put(ConstructionType.LARGE_STABLES, new PanelConstructionProviderEmpty())
            .put(ConstructionType.STABLES, new PanelConstructionProviderEmpty())
            // Newspaper
            .put(ConstructionType.NEWSPAPER, new PanelConstructionProviderEmpty())
            .put(ConstructionType.PRINTING_PRESS, new PanelConstructionProviderEmpty())
            // Custom house
            .put(ConstructionType.CUSTOM_HOUSE, new PanelConstructionProviderEmpty())

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
        final ConstructionProvider provider = constructionProviders
                .get(construction.getType());
        final PanelConstructionContext context = new PanelConstructionContext(imageProvider,
                eventBus, i18n, construction, colony.getGoodsStats(),
                gameModelController.getModel());
        context.setColony(colony);
        final AbstractPanelConstruction panel = provider.make(context);
        mainPanel.getChildren().add(panel.getContent());
        panel.paint();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
