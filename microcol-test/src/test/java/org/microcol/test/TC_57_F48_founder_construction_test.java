package org.microcol.test;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.*;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Recruit the actual founder before admitting or resuming its projects. */
@ExtendWith(ApplicationExtension.class)
public class TC_57_F48_founder_construction_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F48-founder-construction.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_smith_unlocks_six_factories_and_ironworks_completes()
            throws Exception {
        // Prepare the founder offer and load the colony.
        FounderConstructionPage founder = new FounderConstructionPage(
                getContext());
        founder.prepareOffer(Founder.ADAM_SMITH, false);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        colony.rememberWorld();
        construction.open();
        for (String project : List.of("IRON_WORKS", "CIGAR_FACTORY",
                "TEXTILE_MILL", "RUM_FACTORY", "FUR_FACTORY", "ARSENAL")) {
            construction.select(project);
            construction.verifyOffer(false, "Requires founder: Adam Smith.",
                    project.equals("IRON_WORKS") || project.equals("ARSENAL")
                            ? 240
                            : 160,
                    100);
        }
        construction.captureDialog("constructionProjects",
                "r84-smith-gate.png");
        construction.cancel();
        colony.verifyWorldUnchanged();
        game = colony.close();
        // Choose the founder and earn the final bell through an ordinary turn.
        CongressPage congress = new CongressPage(getContext());
        congress.open();
        congress.select(Founder.ADAM_SMITH);
        congress.choose();
        congress.close();
        game.nextTurnAndCloseDialogs();
        founder.verifyMember(Founder.ADAM_SMITH);
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.open();
        for (String project : List.of("IRON_WORKS", "CIGAR_FACTORY",
                "TEXTILE_MILL", "RUM_FACTORY", "FUR_FACTORY", "ARSENAL")) {
            construction.select(project);
            construction.verifyOffer(true, "Available.",
                    project.equals("IRON_WORKS") || project.equals("ARSENAL")
                            ? 240
                            : 160,
                    100);
        }
        construction.select("CUSTOM_HOUSE");
        construction.verifyOffer(false, "Requires founder: Peter Stuyvesant.",
                160, 50);
        construction.select("IRON_WORKS");
        construction.confirm();
        construction.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyBuilding(ConstructionType.IRON_WORKS, 1);
        construction.verifyQueue(List.of(), List.of(), 266);
        game = colony.close();
        // Continue from disk and verify the same membership, building or paused
        // head.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        founder.verifyMember(Founder.ADAM_SMITH);
        construction.verifyBuilding(ConstructionType.IRON_WORKS, 1);
        construction.verifyQueue(List.of(), List.of(), 266);
    }

    @Test
    @Tag("local")
    void TC_02_stuyvesant_unlocks_only_custom_house_and_continue()
            throws Exception {
        // Prepare the founder offer and load the colony.
        FounderConstructionPage founder = new FounderConstructionPage(
                getContext());
        founder.prepareOffer(Founder.PETER_STUYVESANT, false);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.open();
        construction.select("CUSTOM_HOUSE");
        construction.verifyOffer(false, "Requires founder: Peter Stuyvesant.",
                160, 50);
        construction.cancel();
        game = colony.close();
        // Choose the founder and earn the final bell through an ordinary turn.
        CongressPage congress = new CongressPage(getContext());
        congress.open();
        congress.select(Founder.PETER_STUYVESANT);
        congress.choose();
        congress.close();
        game.nextTurnAndCloseDialogs();
        founder.verifyMember(Founder.PETER_STUYVESANT);
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.open();
        construction.select("IRON_WORKS");
        construction.verifyOffer(false, "Requires founder: Adam Smith.", 240,
                100);
        construction.select("CUSTOM_HOUSE");
        construction.verifyOffer(true, "Available.", 160, 50);
        construction.confirm();
        construction.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyBuilding(ConstructionType.CUSTOM_HOUSE, 1);
        game = colony.close();
        // Continue from disk and verify the same membership, building or paused
        // head.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        founder.verifyMember(Founder.PETER_STUYVESANT);
        construction.verifyBuilding(ConstructionType.CUSTOM_HOUSE, 1);
    }

    @Test
    @Tag("local")
    void TC_03_retained_project_cannot_spend_without_smith() throws Exception {
        // Prepare the founder offer and load the colony.
        FounderConstructionPage founder = new FounderConstructionPage(
                getContext());
        founder.prepareOffer(Founder.ADAM_SMITH, true);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.verifyStatus("Requires founder: Adam Smith.");
        colony.rememberWorld();
        construction.open();
        ConstructionRushPage rush = new ConstructionRushPage(getContext());
        rush.open();
        founder.verifyRushUnavailable();
        rush.cancel();
        construction.cancel();
        colony.verifyWorldUnchanged();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyStatus("Requires founder: Adam Smith.");
        construction.verifyQueue(List.of("IRON_WORKS"), List.of(9), 503);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 100));
        construction.verifyBuilding(ConstructionType.IRON_WORKS, 0);
        game = colony.close();
        // Continue from disk and verify the same membership, building or paused
        // head.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        construction.verifyStatus("Requires founder: Adam Smith.");
        construction.verifyQueue(List.of("IRON_WORKS"), List.of(9), 503);
    }
}
