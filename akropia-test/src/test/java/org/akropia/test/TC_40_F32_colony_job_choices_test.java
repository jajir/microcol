package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.ColonyJobChoice.Kind;
import org.akropia.model.ConstructionType;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.page.ColonyJobPage;
import org.akropia.page.ColonyScreen;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Player job choices, previews, cancellation and stale-state rejection. */
@ExtendWith(ApplicationExtension.class)
public class TC_40_F32_colony_job_choices_test extends AbstractAkropiaTest {
    private static final Location COLONY = Location.of(24, 16);
    private static final String FIXTURE = "src/test/scenarios/F32-colony-job-choices.akropia";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(FIXTURE));
    }

    @Test
    @Tag("local")
    void TC_01_current_crop_and_explicit_field_move() throws Exception {
        // Compare current yield with eligible fields, excluding the neighbor's
        // better square.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        jobs.openFromField(0);
        // Every entry offers only the current field, in the common goods order.
        jobs.verifyNoAutomaticFieldChoice();
        jobs.verifyCurrentTileChoices(false);
        new org.akropia.page.ContextMenuPage(getContext())
                .captureMenu("worker-field-context.png");
        jobs.verifySameProductionTable();
        jobs.select(Kind.CURRENT_FIELD, GoodsType.LUMBER);
        jobs.verifyDetails("Current field: 3 Lumber per turn", true);
        jobs.captureProductionTable("unit-production-table.png");
        jobs.escape();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();

        // A crop choice keeps the original field.
        jobs.openFromField(0);
        jobs.select(Kind.CURRENT_FIELD, GoodsType.GRAIN);
        jobs.confirm();
        jobs.verifyClosed();
        jobs.verifyFieldJob(0, Location.of(23, 16),
                Goods.of(GoodsType.GRAIN, 2));

        // Drag explicitly to the chosen field, then select its output.
        jobs.assignField(0, Location.of(23, 15), GoodsType.LUMBER);
        jobs.verifyClosed();
        jobs.verifyFieldJob(0, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 7));
        jobs.verifyNeighborUnchanged(1, Location.of(25, 16),
                Goods.of(GoodsType.LUMBER, 6));
    }

    @Test
    @Tag("local")
    void TC_02_center_and_indoor_entries_assign_work() throws Exception {
        // Open from a center worker with the existing keyboard context menu.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromCenterWithKeyboard(4);
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.verifyDetails("Potential output: 3 Tools per turn", true);
        jobs.verifyDetails("Required each turn: 3 Ore; in warehouse: 1", true);
        jobs.verifyDetails("Not enough input goods", true);
        jobs.cancel();

        // Drag a center worker to a specific outdoor square.
        jobs.assignField(4, Location.of(23, 15), GoodsType.LUMBER);
        jobs.verifyFieldJob(4, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 7));

        // Move that worker indoors using the field entry point.
        jobs.assignIndoorFromField(4, "id_blacksmith");
        jobs.verifyIndoorJob(4, ConstructionType.BLACKSMITHS_HOUSE, 1);

        // Drag the indoor worker back to the same explicit outdoor square.
        jobs.assignIndoorWorkerToField("id_blacksmith", 1, 4,
                Location.of(23, 15), GoodsType.LUMBER);
        jobs.verifyFieldJob(4, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 7));
        jobs.verifyNeighborUnchanged(1, Location.of(25, 16),
                Goods.of(GoodsType.LUMBER, 6));
    }

    @Test
    @Tag("local")
    void TC_03_expert_shortage_preview_and_cancel_are_read_only()
            throws Exception {
        // Inspect the existing master blacksmith's potential, input and
        // specialty.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        jobs.openFromIndoor("id_blacksmith", 0);
        jobs.verifyCurrentTileChoices(true);
        new org.akropia.page.ContextMenuPage(getContext())
                .captureMenu("worker-indoor-context.png");
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.verifyDetails("Potential output: 6 Tools per turn", true);
        jobs.verifyDetails("Required each turn: 6 Ore; in warehouse: 1", true);
        jobs.verifyDetails("Not enough input goods", true);
        jobs.verifyDetails("Specialty: Tools", true);
        jobs.captureProductionTable("job-expert-shortage.png");
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    @Test
    @Tag("local")
    void TC_04_city_context_limits_work_to_open_colony() throws Exception {
        // Opening the production submenu never offers a destination in another
        // colony.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        jobs.openFromField(0);
        jobs.verifyNoAutomaticFieldChoice();
        jobs.verifyCurrentTileChoices(false);
        jobs.verifyOnlyColonyMenu(0);
        jobs.cancel();
        jobs.verifyWorldUnchanged();
        jobs.verifyNeighborUnchanged(1, Location.of(25, 16),
                Goods.of(GoodsType.LUMBER, 6));
    }

    @Test
    @Tag("local")
    void TC_05_stale_source_crop_rejects_without_changing_the_new_crop()
            throws Exception {
        // Changing the source crop can invalidate an offer without moving the
        // worker.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromField(0);
        jobs.select(Kind.CURRENT_FIELD, GoodsType.LUMBER);
        jobs.changeSourceCropWhileOpen(GoodsType.GRAIN);
        jobs.confirm();
        ColonyScreen.of(getContext(), "New Amsterdam")
                .verifyThatStatusBarContains(
                        "Work is no longer available. Refresh the selection.");
        jobs.verifyWorldUnchanged();
        jobs.cancel();
        jobs.verifyFieldJob(0, Location.of(23, 16),
                Goods.of(GoodsType.GRAIN, 2));
    }

    @Test
    @Tag("local")
    void TC_06_czech_labels_and_keyboard_cancel() throws Exception {
        // Choose Czech through settings before entering the same worker
        // journey.
        final org.akropia.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame().openColonyAt(COLONY, "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        jobs.openFromCenterWithKeyboard(4);
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.verifyDetails("Potřeba za tah: 3", true);
        jobs.verifyDetails("ve skladu: 1", true);
        jobs.verifyDetails("Nedostatek vstupních surovin", true);
        jobs.captureProductionTable("job-choices-czech.png");
        jobs.escape();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
    }

    @Test
    @Tag("local")
    void TC_07_manual_indoor_drag_remains_available() throws Exception {
        // Drop on an explicit empty indoor slot using the existing drag path.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.dragCenterToIndoor(4, "id_blacksmith", 1);
        jobs.verifyIndoorJob(4, ConstructionType.BLACKSMITHS_HOUSE, 1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.ORE, 1));
    }

    @Test
    @Tag("local")
    void TC_08_indoor_and_outdoor_jobs_assign() throws Exception {
        // Open colony
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        // Assign master7 to the north-west lumber field
        jobs.assignIndoorWorkerToField("id_blacksmith", 0, 7,
                Location.of(23, 15), GoodsType.LUMBER);
        jobs.verifyFieldJob(7, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 7));

        // Assign worker6 from the town hall to the north lumber field
        jobs.assignIndoorWorkerToField("id_town_hall", 0, 6,
                Location.of(24, 15), GoodsType.LUMBER);
        jobs.verifyFieldJob(6, Location.of(24, 15),
                Goods.of(GoodsType.LUMBER, 5));

        // Attempt to assign center worker 4 to lumber but none available
        jobs.rememberWorld();
        jobs.openFromCenter(4);
        jobs.verifyNoAutomaticFieldChoice();
        jobs.verifyCurrentTileChoices(true, true);
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        jobs.verifyNeighborUnchanged(1, Location.of(25, 16),
                Goods.of(GoodsType.LUMBER, 6));
    }

    @Test
    @Tag("local")
    void TC_09_fill_smithy_slots_through_picker() throws Exception {
        // Open colony
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        // Fill first smithy slot from a field worker
        jobs.assignIndoorFromField(0, "id_blacksmith");
        jobs.verifyIndoorJob(0, ConstructionType.BLACKSMITHS_HOUSE, 1);

        // Fill second smithy slot from a center worker
        jobs.openFromCenter(4);
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.confirm();
        jobs.verifyIndoorJob(4, ConstructionType.BLACKSMITHS_HOUSE, 2);

        // Verify that occupied slots stay unchanged when a new picker is opened
        jobs.rememberWorld();
        jobs.openFromIndoor("id_town_hall", 0); // worker6
        jobs.verifyJobNotOffered(Kind.INDOOR, GoodsType.TOOLS);
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    @Test
    @Tag("local")
    void TC_10_confirm_current_indoor_job_noop() throws Exception {
        // Open colony
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        jobs.rememberWorld();
        jobs.openFromIndoor("id_blacksmith", 0);
        jobs.verifyCurrentTileChoices(true); // master7
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.verifyDetails("Potential output: 6 Tools per turn", true);
        jobs.confirmWithEnter();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    @Test
    @Tag("local")
    void TC_11_manual_drag_on_building_body() throws Exception {
        // Open colony and keep the screen for later warehouse check
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        jobs.dragCenterToBuilding(4, "id_blacksmith");
        jobs.verifyIndoorJob(4, ConstructionType.BLACKSMITHS_HOUSE, 1);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.ORE, 1));
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    @Test
    @Tag("local")
    void TC_12_stale_explicit_slot_drop_preserves_equipment_and_stock()
            throws Exception {
        // Equip the center worker using the ordinary player control.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final org.akropia.page.EquipmentPage equipment = new org.akropia.page.EquipmentPage(
                getContext());
        equipment.open("Equip with weapons");
        equipment.confirm();
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        // A stock change after hover makes the exact slot illegal at drop.
        jobs.dragAfterEquipmentStockChanges(4, "id_blacksmith", false);
        jobs.verifyEquipmentDropRejected(4);
        jobs.cancel();
        jobs.verifyWorldUnchanged();
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    @Test
    @Tag("local")
    void TC_13_stale_building_drop_preserves_equipment_and_stock()
            throws Exception {
        // Equip the center worker using the ordinary player control.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final org.akropia.page.EquipmentPage equipment = new org.akropia.page.EquipmentPage(
                getContext());
        equipment.open("Equip with weapons");
        equipment.confirm();
        final ColonyJobPage jobs = new ColonyJobPage(getContext());

        // The building-body fallback must also recheck before returning
        // equipment.
        jobs.dragAfterEquipmentStockChanges(4, "id_blacksmith", true);
        jobs.verifyEquipmentDropRejected(4);
        jobs.cancel();
        jobs.verifyWorldUnchanged();
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    @Test
    @Tag("local")
    void TC_14_stale_explicit_slot_drop_does_not_choose_another_slot()
            throws Exception {
        // A second worker takes the exact hovered slot before release
        // completes.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.dragAfterSlotIsTaken(4, false);
        jobs.verifyDropRejected(4);
        jobs.captureDialog(org.akropia.gui.util.ButtonBarOk.BUTTON_OK_ID,
                "job-stale-drop.png");
        jobs.cancel();
        jobs.verifyWorldUnchanged();
        jobs.verifyIndoorJob(6, ConstructionType.BLACKSMITHS_HOUSE, 1);
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    @Test
    @Tag("local")
    void TC_15_stale_building_drop_does_not_choose_another_slot()
            throws Exception {
        // The building-body fallback also retains the slot offered during
        // hover.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.dragAfterSlotIsTaken(4, true);
        jobs.verifyDropRejected(4);
        jobs.cancel();
        jobs.verifyWorldUnchanged();
        jobs.verifyIndoorJob(6, ConstructionType.BLACKSMITHS_HOUSE, 1);
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

}
