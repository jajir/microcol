package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Build real stables, breed after food/growth and preserve saved stocks. */
@ExtendWith(ApplicationExtension.class)
public class TC_49_F40_horse_breeding_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F40-horse-breeding.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_build_stables_then_double_breeding_and_continue()
            throws Exception {
        // Previewing and reopening a colony never breeds its horses.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        HorseBreedingPage horses = new HorseBreedingPage(getContext());
        horses.verifyPreview(4, 2);
        horses.verifyStocks(4, 100);
        int firstFood = horses.forecastFood();
        game = colony.close();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        horses.verifyStocks(4, 100);
        // Complete an actual stable project at the end of this update.
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.open();
        construction.select("STABLES");
        construction.verifyOffer(true, "Available.", 64, 0);
        construction.confirm();
        construction.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyBuilding(ConstructionType.STABLES, 1);
        horses.verifyStocks(6, firstFood);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.HORSE, 6));
        horses.verifyPreview(6, 4);
        int secondFood = horses.forecastFood();
        // The completed stable improves the following update, not the previous
        // one.
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        horses.verifyStocks(10, secondFood);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.HORSE, 10));
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        horses.verifyStocks(10, secondFood);
        construction.verifyBuilding(ConstructionType.STABLES, 1);
        horses.verifyPreview(10, 4);
    }

    @Test
    @Tag("local")
    void TC_02_capacity_charges_only_one_then_no_food() throws Exception {
        // Arrange a colony one horse below capacity; only one new horse can
        // fit.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        HorseBreedingPage horses = new HorseBreedingPage(getContext());
        horses.prepareStocks(99, 20);
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        horses.verifyPreview(99, 1);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        horses.verifyStocks(100, 19);
        horses.verifyPreview(100, 0);
        int food = horses.forecastFood();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        horses.verifyStocks(100, food);
    }

    @Test
    @Tag("local")
    void TC_03_birth_uses_food_before_horses() throws Exception {
        // Effective food 201 creates a colonist first and leaves one food for
        // breeding.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        HorseBreedingPage horses = new HorseBreedingPage(getContext());
        horses.prepareStocks(4, 201);
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        int residents = horses.population();
        horses.verifyPreview(4, 1);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.openColonyAt(COLONY, "New Amsterdam");
        horses.verifyStocks(5, 0);
        horses.verifyBornOutside(residents, 2);
    }
}
