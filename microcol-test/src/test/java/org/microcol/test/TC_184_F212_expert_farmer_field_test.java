package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.PlaceColonyField;
import org.microcol.model.PlaceEuropePier;
import org.microcol.model.PlaceLocation;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.EuropeEquipmentPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.ExpertPurchasePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * R-0212: an Expert Farmer hired in Europe sails to the colony and works bare
 * grassland, where the expert doubles the ordinary corn of 4 to 8. Switched to
 * tobacco, which is not its specialty, it yields the ordinary 3. The ordinary
 * workers on bare grassland are the references: corn 4 and tobacco 3.
 *
 * <p>
 * Fixture: the shared expert-purchase scenario with the single tree on
 * {@code (24,15)} removed, so that square is bare grassland and the best free
 * corn square. The other free square {@code (25,15)} keeps its tree and yields
 * only the forest corn 3, doubled to 6, so the choice needs no tie rule. The
 * scenario holds no road, river, resource, cleared or field overlay.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_184_F212_expert_farmer_field_test extends AbstractMicroColTest {
    private static final String BASE_SCENARIO = "src/test/scenarios/F22-expert-purchase.microcol";

    private static final Location COLONY = Location.of(24, 16);

    /** Bare grassland, free in the fixture, best corn square for the expert. */
    private static final Location FARM_SQUARE = Location.of(24, 15);

    /** Bare grassland worked by ordinary worker 1, the corn reference. */
    private static final Location ORDINARY_CORN_SQUARE = Location.of(23, 17);

    /** Bare grassland worked by ordinary worker 0, the tobacco reference. */
    private static final Location ORDINARY_TOBACCO_SQUARE = Location.of(23, 15);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), fixture());
    }

    /**
     * Derives the fixture from the checked-in scenario and writes it under
     * target. Only the tree on the farm square is removed.
     */
    private static File fixture() throws Exception {
        final GameModelPo data = new GameModelPoDao()
                .loadFromFile(new File(BASE_SCENARIO));
        final Set<Location> trees = new HashSet<>(
                data.getModel().getMap().getTreeSet());
        trees.remove(FARM_SQUARE);
        data.getModel().getMap().setTrees(trees);
        final Path directory = Files
                .createDirectories(Path.of("target", "functional-fixtures"));
        final File file = directory.resolve("F212-expert-farmer-field.microcol")
                .toFile();
        new GameModelPoDao().saveToFile(file.getPath(), data);
        return file;
    }

    @Test
    @Tag("local")
    void TC_01_hired_farmer_sails_works_the_field_and_continues()
            throws Exception {
        // Hire the Expert Farmer from the offer; the single charge is 1000.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        ExpertPurchasePage purchase = new ExpertPurchasePage(getContext());
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        europe.openBuyUnitScreen();
        purchase.buySpecialist("EXPERT_FARMER");
        purchase.verifyPurchasedSpecialist(103, "EXPERT_FARMER", 3400);
        europe.verifyGold(3400);
        GamePage game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        purchase.verifySpecialistState(103, UnitType.EXPERT_FARMER, 3400,
                PlaceEuropePier.class);

        // Embark on the ship in port and depart; Continue keeps the voyage.
        europe = game.openEuroperPort();
        europe.selectUnitFromPort(0);
        equipment.embark(103);
        equipment.verifyPassenger(103, 102);
        equipment.depart(102);
        final int voyageDuration = equipment.verifyVoyageSample(102);
        final int voyageWeather = equipment.getVoyageWeather(102);
        game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        equipment.verifyVoyageMetadata(102, voyageWeather, voyageDuration,
                voyageDuration);
        equipment.verifyPassenger(103, 102);
        purchase.verifySpecialistState(103, UnitType.EXPERT_FARMER, 3400,
                PlaceCargoSlot.class);
        for (int remaining = voyageDuration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                equipment.verifyVoyageMetadata(102, voyageWeather,
                        voyageDuration, remaining);
            }
        }
        equipment.verifyArrived(102);

        // Sail the ship to the owned colony in the same steps as TC30.
        equipment.verifyCarrierAt(102, Location.of(49, 15));
        equipment.showSeaRoute(Location.of(49, 15), Location.of(43, 16));
        game.moveMouseAtLocation(Location.of(49, 15));
        game.dragMouseAtLocation(Location.of(43, 16));
        equipment.verifyCarrierAt(102, Location.of(43, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(43, 16), Location.of(37, 16));
        game.moveMouseAtLocation(Location.of(43, 16));
        game.dragMouseAtLocation(Location.of(37, 16));
        equipment.verifyCarrierAt(102, Location.of(37, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(37, 16), Location.of(31, 16));
        game.moveMouseAtLocation(Location.of(37, 16));
        game.dragMouseAtLocation(Location.of(31, 16));
        equipment.verifyCarrierAt(102, Location.of(31, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(31, 16), Location.of(26, 16));
        game.moveMouseAtLocation(Location.of(31, 16));
        game.dragMouseAtLocation(Location.of(26, 16));
        equipment.verifyCarrierAt(102, Location.of(26, 16));
        game.nextTurnAndCloseDialogs();
        equipment.showSeaRoute(Location.of(26, 16), COLONY);
        game.moveMouseAtLocation(Location.of(26, 16));
        game.dragMouseAtLocation(COLONY);
        game.waitForNumberOfUnitsAt(COLONY, 2);

        // The expert steps ashore at the colony centre, still unassigned.
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        colony.verifyNumberOfUnitsAtPier(1);
        purchase.verifySpecialistState(103, UnitType.EXPERT_FARMER, 3400,
                PlaceLocation.class);
        equipment.verifyLandedPassenger(103, 102, COLONY);
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        purchase.verifySpecialistState(103, UnitType.EXPERT_FARMER, 3400,
                PlaceLocation.class);

        // Send the expert to the best corn field through the job menu. It takes
        // the bare square and doubles the ordinary corn of the worker beside
        // it.
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenter(103);
        jobs.select(ColonyJobChoice.Kind.BEST_FIELD, GoodsType.CORN);
        jobs.confirm();
        jobs.verifyFieldJob(103, FARM_SQUARE, Goods.of(GoodsType.CORN, 8));
        jobs.verifyFieldJob(1, ORDINARY_CORN_SQUARE,
                Goods.of(GoodsType.CORN, 4));
        purchase.verifySpecialistState(103, UnitType.EXPERT_FARMER, 3400,
                PlaceColonyField.class);

        // Save and Continue keep the assignment, the same square and the
        // output.
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        jobs.verifyFieldJob(103, FARM_SQUARE, Goods.of(GoodsType.CORN, 8));
        jobs.verifyFieldJob(1, ORDINARY_CORN_SQUARE,
                Goods.of(GoodsType.CORN, 4));
        purchase.verifySpecialistState(103, UnitType.EXPERT_FARMER, 3400,
                PlaceColonyField.class);

        // Off its specialty the same expert on the same square yields the
        // ordinary tobacco, equal to the ordinary tobacco worker beside it.
        jobs.openFromField(103);
        jobs.select(ColonyJobChoice.Kind.CURRENT_FIELD, GoodsType.TOBACCO);
        jobs.confirm();
        jobs.verifyFieldJob(103, FARM_SQUARE, Goods.of(GoodsType.TOBACCO, 3));
        jobs.verifyFieldJob(0, ORDINARY_TOBACCO_SQUARE,
                Goods.of(GoodsType.TOBACCO, 3));
        purchase.verifySpecialistState(103, UnitType.EXPERT_FARMER, 3400,
                PlaceColonyField.class);

        // Continue keeps the expert on the same square with the ordinary yield.
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        jobs.verifyFieldJob(103, FARM_SQUARE, Goods.of(GoodsType.TOBACCO, 3));
        jobs.verifyFieldJob(0, ORDINARY_TOBACCO_SQUARE,
                Goods.of(GoodsType.TOBACCO, 3));
        purchase.verifySpecialistState(103, UnitType.EXPERT_FARMER, 3400,
                PlaceColonyField.class);
        colony.close();
    }
}
