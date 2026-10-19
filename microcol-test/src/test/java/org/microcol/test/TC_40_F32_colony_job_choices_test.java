package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice.Kind;
import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Player job choices, previews, cancellation and stale-state rejection. */
@ExtendWith(ApplicationExtension.class)
public class TC_40_F32_colony_job_choices_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);
    private static final String FIXTURE = "src/test/scenarios/F32-colony-job-choices.microcol";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(FIXTURE));
    }

    @Test
    @Tag("local")
    void TC_01_current_crop_and_explicit_best_move() throws Exception {
        // Compare current yield with eligible fields, excluding the neighbor's
        // better square.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.rememberWorld();
        jobs.openFromField(0);
        // Forested desert has no tobacco, but forested grassland nearby does.
        jobs.select(Kind.CURRENT_FIELD, GoodsType.TOBACCO);
        jobs.verifyDetails("This crop cannot be produced on the current field",
                false);
        jobs.select(Kind.CURRENT_FIELD, GoodsType.LUMBER);
        jobs.verifyVisibleChoice("Keep this field: Lumber");
        jobs.verifyDetails("Current field: 2 Lumber per turn", true);
        jobs.verifyDetails("Best eligible field: 6 Lumber per turn", true);
        jobs.captureDialog("jobDialog", "job-choices-english.png");
        jobs.escape();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();

        // A crop choice keeps the original field.
        jobs.openFromField(0);
        jobs.select(Kind.CURRENT_FIELD, GoodsType.CORN);
        jobs.confirm();
        jobs.verifyClosed();
        jobs.verifyFieldJob(0, Location.of(23, 16),
                Goods.of(GoodsType.CORN, 2));

        // Moving to the shown best field is a separate confirmed action.
        jobs.openFromField(0);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.verifyDetails("Field coordinates: 23, 15", true);
        jobs.confirmWithEnter();
        jobs.verifyClosed();
        jobs.verifyFieldJob(0, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 6));
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

        // A center worker can take outdoor work directly from the picker.
        jobs.openFromCenter(4);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.confirm();
        jobs.verifyFieldJob(4, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 6));

        // Move that worker indoors using the field entry point.
        jobs.openFromField(4);
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.confirm();
        jobs.verifyIndoorJob(4, ConstructionType.BLACKSMITHS_HOUSE, 1);

        // The same picker lets the indoor worker take the best outdoor job.
        jobs.openFromIndoor("id_blacksmith", 1);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.confirm();
        jobs.verifyFieldJob(4, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 6));
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
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.verifyDetails("Potential output: 6 Tools per turn", true);
        jobs.verifyDetails("Required each turn: 6 Ore; in warehouse: 1", true);
        jobs.verifyDetails("Not enough input goods", true);
        jobs.verifyDetails("Specialty: Tools", true);
        jobs.captureDialog("jobDialog", "job-expert-shortage.png");
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        jobs.verifyIndoorJob(7, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    @Test
    @Tag("local")
    void TC_04_stale_destination_rejects_and_refreshes_without_assignment()
            throws Exception {
        // Keep an offer open while another worker takes its exact destination.
        WelcomePage.of(getContext()).loadGame().openColonyAt(COLONY,
                "New Amsterdam");
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromField(0);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.occupyBestFieldWhileOpen(4);
        jobs.confirm();
        jobs.verifyMessage("Nothing was assigned");
        jobs.verifyWorldUnchanged();
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.verifyDetails("Best eligible field: 4 Lumber per turn", true);
        jobs.verifyDetails("Field coordinates: 24, 15", true);
        jobs.cancel();
        jobs.verifyFieldJob(0, Location.of(23, 16),
                Goods.of(GoodsType.LUMBER, 2));
        jobs.verifyFieldJob(4, Location.of(23, 15),
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
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.changeSourceCropWhileOpen(GoodsType.CORN);
        jobs.confirm();
        jobs.verifyMessage("Nothing was assigned");
        jobs.verifyWorldUnchanged();
        jobs.cancel();
        jobs.verifyFieldJob(0, Location.of(23, 16),
                Goods.of(GoodsType.CORN, 2));
    }

    @Test
    @Tag("local")
    void TC_06_czech_labels_and_keyboard_cancel() throws Exception {
        // Choose Czech through settings before entering the same worker
        // journey.
        final org.microcol.page.SettingPage settings = WelcomePage
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
        jobs.captureDialog("jobDialog", "job-choices-czech.png");
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

        // Assign master7 to best lumber field
        jobs.openFromIndoor("id_blacksmith", 0);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.confirm();
        jobs.verifyFieldJob(7, Location.of(23, 15),
                Goods.of(GoodsType.LUMBER, 6));

        // Assign worker6 from town hall to best lumber field
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.confirm();
        jobs.verifyFieldJob(6, Location.of(24, 15),
                Goods.of(GoodsType.LUMBER, 4));

        // Attempt to assign center worker 4 to lumber but none available
        jobs.rememberWorld();
        jobs.openFromCenter(4);
        jobs.select(Kind.BEST_FIELD, GoodsType.LUMBER);
        jobs.verifyDetails("No eligible field is available for this crop.",
                false);
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
        jobs.openFromField(0);
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.confirm();
        jobs.verifyIndoorJob(0, ConstructionType.BLACKSMITHS_HOUSE, 1);

        // Fill second smithy slot from a center worker
        jobs.openFromCenter(4);
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.confirm();
        jobs.verifyIndoorJob(4, ConstructionType.BLACKSMITHS_HOUSE, 2);

        // Verify that occupied slots stay unchanged when a new picker is opened
        jobs.rememberWorld();
        jobs.openFromIndoor("id_town_hall", 0); // worker6
        jobs.select(Kind.INDOOR, GoodsType.TOOLS);
        jobs.verifyDetails("All jobs in this building are occupied.", false);
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
        jobs.openFromIndoor("id_blacksmith", 0); // master7
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
        final org.microcol.page.EquipmentPage equipment = new org.microcol.page.EquipmentPage(
                getContext());
        equipment.open("Equip with muskets");
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
        final org.microcol.page.EquipmentPage equipment = new org.microcol.page.EquipmentPage(
                getContext());
        equipment.open("Equip with muskets");
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
        jobs.captureDialog("jobDialog", "job-stale-drop.png");
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
