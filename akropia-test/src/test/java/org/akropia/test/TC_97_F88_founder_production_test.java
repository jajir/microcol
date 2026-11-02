package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.*;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.CongressPo;
import org.akropia.model.store.PlayerPo;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Jefferson, Paine and Hudson change the town-hall total and the clay field of
 * the Dutch colony. The scenario is the printing scenario with three real
 * Congress members, valid candidates and a 20 percent tax rate.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_97_F88_founder_production_test extends AbstractAkropiaTest {
    private static final Location COLONY = Location.of(24, 16);
    private static final String BASE_SCENARIO = "src/test/scenarios/F43-printing-production.akropia";
    private static final int TAX_RATE = 20;

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), founderScenario());
    }

    /**
     * Derives the founder scenario from the checked-in printing scenario and
     * writes it under target. Only the Dutch Congress and tax rate change.
     */
    private static File founderScenario() throws Exception {
        final GameModelPo data = new GameModelPoDao()
                .loadFromFile(new File(BASE_SCENARIO));
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals("Dutch"))
                .findFirst().orElseThrow();
        dutch.getTax().setRate(TAX_RATE);
        final CongressPo congress = dutch.getCongress();
        congress.setMembers(List.of(Founder.THOMAS_JEFFERSON,
                Founder.THOMAS_PAINE, Founder.HENRY_HUDSON));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.SIMON_BOLIVAR, Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
                Founder.FERDINAND_MAGELLAN));
        final Path directory = Files
                .createDirectories(Path.of("target", "functional-fixtures"));
        final File file = directory.resolve("F88-founder-production.akropia")
                .toFile();
        new GameModelPoDao().saveToFile(file.getPath(), data);
        return file;
    }

    @Test
    @Tag("local")
    void TC_01_founders_change_town_hall_press_newspaper_and_clay_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        PrintingProductionPage owls = new PrintingProductionPage(getContext());
        FounderProductionPage founders = new FounderProductionPage(
                getContext());
        // Unattended 1 at 120% plus two ordinary statesmen 6 at 170%:
        // floor((120 + 1020) / 100) = 11.
        owls.verifyOwls(11, 0, 0, 1);
        ConstructionPage building = new ConstructionPage(getContext());
        building.open();
        building.select("PRINTING_PRESS");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        // The turn credits 11 owls before the press completes; with the press
        // floor((170 + 1320) / 100) = 14.
        owls.verifyOwls(14, 11, 0, 1);
        owls.prepareNewspaperMaterials();
        building.open();
        building.select("NEWSPAPER");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        // Newspaper replaces the press: floor((220 + 1620) / 100) = 18.
        owls.verifyOwls(18, 25, 0, 1);
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        // Ordinary colonist on bare grassland: clay pit gives 2 clay, and
        // Hudson doubles them to 4.
        jobs.openFromCenter(4);
        jobs.select(ColonyJobChoice.Kind.BEST_FIELD, GoodsType.CLAY);
        jobs.verifyDetails("Best eligible field: 4 Clay per turn", true);
        jobs.confirm();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        founders.verifyFieldClay(4, 4);
        owls.verifyOwls(18, 43, 0, 1);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        owls.verifyOwls(18, 43, 0, 1);
        founders.verifyFieldClay(4, 4);
    }
}
