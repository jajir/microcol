package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.*;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Jefferson, Paine and Hudson change the town-hall total and the fur field of
 * the Dutch colony. The scenario is the printing scenario with three real
 * Congress members, valid candidates and a 20 percent tax rate.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_97_F88_founder_production_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);
    private static final String BASE_SCENARIO = "src/test/scenarios/F43-printing-production.microcol";
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
        final File file = directory.resolve("F88-founder-production.microcol")
                .toFile();
        new GameModelPoDao().saveToFile(file.getPath(), data);
        return file;
    }

    @Test
    @Tag("local")
    void TC_01_founders_change_town_hall_press_newspaper_and_fur_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        PrintingProductionPage bells = new PrintingProductionPage(getContext());
        FounderProductionPage founders = new FounderProductionPage(
                getContext());
        // Unattended 1 at 120% plus two ordinary statesmen 6 at 170%:
        // floor((120 + 1020) / 100) = 11.
        bells.verifyBells(11, 0, 0, 1);
        ConstructionPage building = new ConstructionPage(getContext());
        building.open();
        building.select("PRINTING_PRESS");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        // The turn credits 11 bells before the press completes; with the press
        // floor((170 + 1320) / 100) = 14.
        bells.verifyBells(14, 11, 0, 1);
        bells.prepareNewspaperMaterials();
        building.open();
        building.select("NEWSPAPER");
        building.confirm();
        building.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        // Newspaper replaces the press: floor((220 + 1620) / 100) = 18.
        bells.verifyBells(18, 25, 0, 1);
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        // Ordinary colonist on bare grassland: trapping gives 2 furs, and
        // Hudson doubles them to 4.
        jobs.openFromCenter(4);
        jobs.select(ColonyJobChoice.Kind.BEST_FIELD, GoodsType.FUR);
        jobs.verifyDetails("Best eligible field: 4 Fur per turn", true);
        jobs.confirm();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        founders.verifyFieldFur(4, 4);
        bells.verifyBells(18, 43, 0, 1);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        bells.verifyBells(18, 43, 0, 1);
        founders.verifyFieldFur(4, 4);
    }
}
