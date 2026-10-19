package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ConstructionType;
import org.microcol.model.Location;
import org.microcol.model.PlaceConstructionSlot;
import org.microcol.model.PlaceEuropePier;
import org.microcol.model.PlaceLocation;
import org.microcol.model.UnitType;
import org.microcol.page.ColonyScreen;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.MissionaryOrdinationPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * R-0163, UC-066/UC-101: a colonist becomes a missionary on the European pier
 * or outside an owned colony with a church or cathedral. Ordination is free and
 * keeps identity, equipment and movement; only a Jesuit or a nation with Jean
 * de Brebeuf makes the missionary an expert. Fixture purpose: F19 has pier
 * colonists 100/101 (101 carries 37 tools) and the colony New Amsterdam with a
 * chapel; the page object patches in the religious building, Brebeuf and four
 * colonists outside the colony.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_96_F87_missionary_ordination_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F19-europe-equipment.microcol");
    private static final Location COLONY = Location.of(24, 16);
    private static final String COLONY_NAME = "New Amsterdam";
    private static final String MENU = "Ordain as missionary…";
    private static final String CAPTION = "Ordain missionary";
    private static final String DONE = "This colonist is now a missionary. Nothing else changed.";
    private static final String ALREADY = "This colonist is already a missionary. Nothing was changed.";
    private static final String NO_CHURCH = "This colony needs a church or cathedral. A chapel alone is not enough.";
    private static final String ASSIGNED = "This colonist works in a field or building. Leave that job first, then ordain.";
    private static final String MENU_CZ = "Vysvětit misionáře…";
    private static final String CAPTION_CZ = "Vysvěcení misionáře";
    private static final String DONE_CZ = "Tento kolonista je nyní misionář. Nic dalšího se nezměnilo.";
    private static final int PIER_WITH_TOOLS = 101;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_europe_pier_cancel_confirm_repeat_and_continue()
            throws Exception {
        // Open the real European pier with the tool-carrying colonist.
        final EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        final MissionaryOrdinationPage ordination = new MissionaryOrdinationPage(
                getContext());
        ordination.verifyMissionary(PIER_WITH_TOOLS, false, false,
                UnitType.COLONIST, 37, 3, PlaceEuropePier.class);

        // Cancel and Escape change nothing.
        ordination.openFromPier(PIER_WITH_TOOLS, MENU);
        ordination.verifyChoice(CAPTION, true, "");
        ordination.captureDialog("missionaryDialog", "ordination-europe.png");
        ordination.cancel();
        ordination.verifyMissionary(PIER_WITH_TOOLS, false, false,
                UnitType.COLONIST, 37, 3, PlaceEuropePier.class);
        ordination.openFromPier(PIER_WITH_TOOLS, MENU);
        ordination.escape();
        ordination.verifyMissionary(PIER_WITH_TOOLS, false, false,
                UnitType.COLONIST, 37, 3, PlaceEuropePier.class);

        // Confirmation is free; the same person keeps tools and movement.
        ordination.openFromPier(PIER_WITH_TOOLS, MENU);
        ordination.confirm();
        ordination.verifyChoice(CAPTION, false, DONE);
        ordination.cancel();
        ordination.verifyMissionary(PIER_WITH_TOOLS, true, false,
                UnitType.COLONIST, 37, 3, PlaceEuropePier.class);
        ordination.verifyGold(1000);

        // A second attempt is refused and changes nothing.
        ordination.openFromPier(PIER_WITH_TOOLS, MENU);
        ordination.verifyChoice(CAPTION, false, ALREADY);
        ordination.cancel();
        ordination.verifyMissionary(PIER_WITH_TOOLS, true, false,
                UnitType.COLONIST, 37, 3, PlaceEuropePier.class);
        ordination.verifyGold(1000);

        // The role survives leaving the game and Continue.
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openEuroperPort();
        ordination.verifyMissionary(PIER_WITH_TOOLS, true, false,
                UnitType.COLONIST, 37, 3, PlaceEuropePier.class);
        ordination.verifyGold(1000);
    }

    @Test
    @Tag("local")
    void TC_02_chapel_alone_cannot_ordain() throws Exception {
        // The colony has only the F19 chapel.
        WelcomePage.of(getContext()).loadGame();
        final MissionaryOrdinationPage ordination = new MissionaryOrdinationPage(
                getContext());
        ordination.loadFixture(null, false);
        GamePage.of(getContext()).openColonyAt(COLONY, COLONY_NAME);

        // The dialog explains the missing building and cannot be confirmed.
        ordination.openFromColony(MissionaryOrdinationPage.ORDINARY, MENU);
        ordination.verifyChoice(CAPTION, false, NO_CHURCH);
        ordination.cancel();
        ordination.verifyMissionary(MissionaryOrdinationPage.ORDINARY, false,
                false, UnitType.COLONIST, 20, 3, PlaceLocation.class);
    }

    @Test
    @Tag("local")
    void TC_03_church_colony_confirm_stale_job_and_continue() throws Exception {
        // A church colony with colonists standing outside its workforce.
        WelcomePage.of(getContext()).loadGame();
        final MissionaryOrdinationPage ordination = new MissionaryOrdinationPage(
                getContext());
        ordination.loadFixture(ConstructionType.CHURCH, false);
        ColonyScreen colony = GamePage.of(getContext()).openColonyAt(COLONY,
                COLONY_NAME);

        // Cancel first, then confirm; tools and movement stay with the person.
        ordination.openFromColony(MissionaryOrdinationPage.ORDINARY, MENU);
        ordination.verifyChoice(CAPTION, true, "");
        ordination.captureDialog("missionaryDialog", "ordination-colony.png");
        ordination.cancel();
        ordination.verifyMissionary(MissionaryOrdinationPage.ORDINARY, false,
                false, UnitType.COLONIST, 20, 3, PlaceLocation.class);
        ordination.openFromColony(MissionaryOrdinationPage.ORDINARY, MENU);
        ordination.confirm();
        ordination.verifyChoice(CAPTION, false, DONE);
        ordination.cancel();
        ordination.verifyMissionary(MissionaryOrdinationPage.ORDINARY, true,
                false, UnitType.COLONIST, 20, 3, PlaceLocation.class);
        ordination.verifyGold(1000);

        // A colonist who takes a job while the dialog is open is refused.
        ordination.openFromColony(MissionaryOrdinationPage.PLAIN, MENU);
        ordination.verifyChoice(CAPTION, true, "");
        ordination.assignToTownHall(MissionaryOrdinationPage.PLAIN);
        ordination.confirm();
        ordination.verifyChoice(CAPTION, false, ASSIGNED);
        ordination.cancel();
        ordination.verifyMissionary(MissionaryOrdinationPage.PLAIN, false,
                false, UnitType.COLONIST, 0, 3, PlaceConstructionSlot.class);

        // The Jesuit and the existing missionary are refused as already such.
        ordination.openFromColony(MissionaryOrdinationPage.MISSIONARY, MENU);
        ordination.verifyChoice(CAPTION, false, ALREADY);
        ordination.cancel();
        ordination.openFromColony(MissionaryOrdinationPage.JESUIT, MENU);
        ordination.verifyChoice(CAPTION, false, ALREADY);
        ordination.cancel();

        // Roles, identities and the refused worker survive Continue.
        colony.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                COLONY_NAME);
        ordination.verifyMissionary(MissionaryOrdinationPage.ORDINARY, true,
                false, UnitType.COLONIST, 20, 3, PlaceLocation.class);
        ordination.verifyMissionary(MissionaryOrdinationPage.PLAIN, false,
                false, UnitType.COLONIST, 0, 3, PlaceConstructionSlot.class);
        ordination.verifyMissionary(MissionaryOrdinationPage.MISSIONARY, true,
                false, UnitType.COLONIST, 0, 3, PlaceLocation.class);
        ordination.verifyMissionary(MissionaryOrdinationPage.JESUIT, true, true,
                UnitType.JESUIT_MISSIONARY, 0, 3, PlaceLocation.class);
    }

    @Test
    @Tag("local")
    void TC_04_cathedral_colony_brebeuf_before_and_after() throws Exception {
        // Without Brebeuf the cathedral ordains an ordinary missionary.
        WelcomePage.of(getContext()).loadGame();
        final MissionaryOrdinationPage ordination = new MissionaryOrdinationPage(
                getContext());
        ordination.loadFixture(ConstructionType.CATHEDRAL, false);
        ColonyScreen colony = GamePage.of(getContext()).openColonyAt(COLONY,
                COLONY_NAME);
        ordination.openFromColony(MissionaryOrdinationPage.ORDINARY, MENU);
        ordination.confirm();
        ordination.verifyChoice(CAPTION, false, DONE);
        ordination.cancel();
        ordination.verifyBrebeufMember(false);
        ordination.verifyMissionary(MissionaryOrdinationPage.ORDINARY, true,
                false, UnitType.COLONIST, 20, 3, PlaceLocation.class);
        ordination.verifyMissionary(MissionaryOrdinationPage.MISSIONARY, true,
                false, UnitType.COLONIST, 0, 3, PlaceLocation.class);
        ordination.verifyMissionary(MissionaryOrdinationPage.JESUIT, true, true,
                UnitType.JESUIT_MISSIONARY, 0, 3, PlaceLocation.class);

        // With Brebeuf the existing and the newly ordained missionaries act as
        // experts; the Jesuit stays one expert and an ordinary colonist stays
        // a colonist.
        colony.close();
        ordination.loadFixture(ConstructionType.CATHEDRAL, true);
        colony = GamePage.of(getContext()).openColonyAt(COLONY, COLONY_NAME);
        ordination.verifyBrebeufMember(true);
        ordination.verifyMissionary(MissionaryOrdinationPage.MISSIONARY, true,
                true, UnitType.COLONIST, 0, 3, PlaceLocation.class);
        ordination.verifyMissionary(MissionaryOrdinationPage.JESUIT, true, true,
                UnitType.JESUIT_MISSIONARY, 0, 3, PlaceLocation.class);
        ordination.verifyMissionary(MissionaryOrdinationPage.PLAIN, false,
                false, UnitType.COLONIST, 0, 3, PlaceLocation.class);
        ordination.openFromColony(MissionaryOrdinationPage.ORDINARY, MENU);
        ordination.confirm();
        ordination.verifyChoice(CAPTION, false, DONE);
        ordination.cancel();
        ordination.verifyMissionary(MissionaryOrdinationPage.ORDINARY, true,
                true, UnitType.COLONIST, 20, 3, PlaceLocation.class);

        // Membership and expert status survive Continue.
        colony.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                COLONY_NAME);
        ordination.verifyBrebeufMember(true);
        ordination.verifyMissionary(MissionaryOrdinationPage.ORDINARY, true,
                true, UnitType.COLONIST, 20, 3, PlaceLocation.class);
        ordination.verifyMissionary(MissionaryOrdinationPage.MISSIONARY, true,
                true, UnitType.COLONIST, 0, 3, PlaceLocation.class);
    }

    @Test
    @Tag("local")
    void TC_05_czech_dialog_and_keyboard_confirmation() throws Exception {
        // Use the player's language preference before opening Europe.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final MissionaryOrdinationPage ordination = new MissionaryOrdinationPage(
                getContext());
        new FreePlayAuditPage(getContext()).press(javafx.scene.input.KeyCode.E);

        // The Czech dialog explains the choice and Enter confirms it.
        ordination.openFromPier(PIER_WITH_TOOLS, MENU_CZ);
        ordination.verifyChoice(CAPTION_CZ, true, "");
        ordination.captureDialog("missionaryDialog", "ordination-czech.png");
        ordination.confirmWithEnter();
        ordination.verifyChoice(CAPTION_CZ, false, DONE_CZ);
        ordination.cancel();
        ordination.verifyMissionary(PIER_WITH_TOOLS, true, false,
                UnitType.COLONIST, 37, 3, PlaceEuropePier.class);
    }
}
