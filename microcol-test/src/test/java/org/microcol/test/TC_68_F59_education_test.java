package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.microcol.gui.FileSelectingService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.*;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** School assignment, cancellation, saved progress and actual graduation. */
@ExtendWith(ApplicationExtension.class)
public class TC_68_F59_education_test extends AbstractMicroColTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F59-education.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_school_progress_continue_and_graduation() throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        EducationPage education = new EducationPage(getContext());
        education.verifySchoolVisible();
        // Preview and close leave the student in their original job.
        education.open();
        education.chooseTeacher(200);
        education.chooseStudent(0);
        education.verifyPreview("School", 4, "Master Carpenter");
        education.close();
        education.open();
        education.verifyCourses(0, 0);
        education.chooseTeacher(200);
        education.chooseStudent(0);
        education.assign();
        education.verifyCourses(1, 0);
        education.verifyStudentKeptJob();
        education.captureDialog("educationDialog", "education-english.png");
        education.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        education.open();
        education.verifyCourses(1, 1);
        education.close();
        game = colony.close();
        // Continue retains one completed update without advancing on load.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        education.open();
        education.verifyCourses(1, 1);
        education.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        ScreenTurnReport graduation = game.nextTurn().orElseThrow();
        graduation.verifyThatAtLeastOneEventMessageContains(
                "student #0 completed education");
        graduation.close();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        education.verifyUnitType(0, UnitType.MASTER_CARPENTER);
        education.verifyStudentKeptJob();
        education.open();
        education.verifyCourses(0, 0);
        education.close();
    }

    @Test
    @Tag("local")
    void TC_02_czech_cancel_and_productive_work() throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        GamePage game = settings.goBack().loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        EducationPage education = new EducationPage(getContext());
        education.open();
        // Self-teaching is unavailable; canceling a valid course is explicit.
        education.chooseTeacher(200);
        education.chooseStudent(200);
        education.verifyAssignmentUnavailable();
        education.chooseStudent(0);
        education.assign();
        education.verifyCourses(1, 0);
        education.captureDialog("educationDialog", "education-czech.png");
        education.cancelFirstCourse();
        education.verifyCourses(0, 0);
        education.close();
        education.captureDialog("colonyName", "education-colony-czech.png");
        game = colony.close();
        // The saved nineteenth productive turn becomes expertise on actual
        // output.
        ScreenTurnReport learning = game.nextTurn().orElseThrow();
        learning.verifyThatAtLeastOneEventMessageContains(
                "pracovník #303 získal praxí odbornost");
        learning.close();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        education.verifyUnitType(303, UnitType.MASTER_FUR_TRADER);
        education.verifyUnitType(0, UnitType.COLONIST);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        education.verifyUnitType(303, UnitType.MASTER_FUR_TRADER);
    }

    @Test
    @Tag("local")
    void TC_03_teacher_departure_pauses_and_replacement_resumes()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        EducationPage education = new EducationPage(getContext());
        education.open();
        education.chooseTeacher(200);
        education.chooseStudent(0);
        education.assign();
        education.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        // Returning the teacher to carpentry pauses the existing student's
        // course.
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_school", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.HAMMERS);
        jobs.confirm();
        education.open();
        education.verifyCourses(1, 1);
        education.verifyPaused(true);
        education.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        education.open();
        education.verifyCourses(1, 1);
        education.chooseTeacher(201);
        education.chooseStudent(0);
        education.assign();
        education.verifyCourses(1, 1);
        education.verifyPaused(false);
        education.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        education.open();
        education.verifyCourses(1, 2);
        education.close();
    }

    @Test
    @Tag("local")
    void TC_04_changed_student_is_rejected_without_moving_teacher()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        game.openColonyAt(COLONY, "New Amsterdam");
        EducationPage education = new EducationPage(getContext());
        education.open();
        education.chooseTeacher(200);
        education.chooseStudent(0);
        education.verifyPreview("School", 4, "Master Carpenter");
        // A changed resident invalidates the preview before confirmation.
        education.moveStudentOutside(0);
        education.assign();
        education.verifyStaleAssignmentRejected(200);
        education.close();
    }

    @Test
    @Tag("local")
    void TC_05_clear_specialty_cancel_escape_confirm_continue_and_relearn()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        EducationPage education = new EducationPage(getContext());
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        // Canceling the named confirmation leaves the specialist unchanged.
        jobs.rememberWorld();
        jobs.openFromIndoor("id_town_hall", 2);
        jobs.verifyClearSpecialtyOffered(true);
        jobs.openClearSpecialty();
        jobs.verifyClearPrompt(201, "Master Carpenter", "Free Colonist");
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        education.verifyUnitType(201, UnitType.MASTER_CARPENTER);
        // Escape from the confirmation is equally harmless.
        jobs.openFromIndoor("id_town_hall", 2);
        jobs.openClearSpecialty();
        jobs.verifyClearPrompt(201, "Master Carpenter", "Free Colonist");
        jobs.escape();
        jobs.verifyClosed();
        jobs.verifyWorldUnchanged();
        // An ordinary colonist has nothing to clear.
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.verifyClearSpecialtyOffered(false);
        jobs.cancel();
        // Confirming keeps the same person in the same job.
        jobs.openFromIndoor("id_town_hall", 2);
        jobs.openClearSpecialty();
        jobs.confirm();
        jobs.verifyClosed();
        education.verifyUnitType(201, UnitType.COLONIST);
        jobs.verifyIndoorJob(201, ConstructionType.TOWN_HALL, 2);
        // Continue retains the cleared role without reviving the skill.
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        education.verifyUnitType(201, UnitType.COLONIST);
        jobs.verifyIndoorJob(201, ConstructionType.TOWN_HALL, 2);
        // The existing school path can teach the person again.
        education.open();
        education.chooseTeacher(200);
        education.chooseStudent(201);
        education.verifyPreview("School", 4, "Master Carpenter");
        education.assign();
        education.verifyCourseFor(201, UnitType.MASTER_CARPENTER, 0);
        education.close();
    }

    @Test
    @Tag("local")
    void TC_06_clear_veteran_expert_keeps_rank_equipment_and_place()
            throws Exception {
        prepareEquippedVeteranExpert();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        EducationPage education = new EducationPage(getContext());
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        education.verifyEquippedColonist(4, UnitType.EXPERT_FARMER, true, 100,
                COLONY);
        // The prompt names the exact person and both roles.
        jobs.openFromCenter(4);
        jobs.openClearSpecialty();
        jobs.verifyClearPrompt(4, "Expert Farmer", "Free Colonist");
        jobs.confirm();
        jobs.verifyClosed();
        // Rank, tools and the warehouse are untouched.
        education.verifyEquippedColonist(4, UnitType.COLONIST, true, 100,
                COLONY);
        education.verifyWarehouseTools(100);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        game.openColonyAt(COLONY, "New Amsterdam");
        education.verifyEquippedColonist(4, UnitType.COLONIST, true, 100,
                COLONY);
    }

    @Test
    @Tag("local")
    void TC_07_teacher_course_and_school_slot_block_clear_until_removed()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        game.openColonyAt(COLONY, "New Amsterdam");
        EducationPage education = new EducationPage(getContext());
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        education.open();
        education.chooseTeacher(200);
        education.chooseStudent(0);
        education.assign();
        education.close();
        // A teacher with an active course cannot become a free colonist.
        jobs.rememberWorld();
        jobs.openFromIndoor("id_school", 0);
        jobs.openClearSpecialty();
        jobs.verifyClearBlocked("teaches a course");
        jobs.cancel();
        jobs.verifyWorldUnchanged();
        education.verifyUnitType(200, UnitType.MASTER_CARPENTER);
        // Without the course, the school place itself is still incompatible.
        education.open();
        education.cancelFirstCourse();
        education.close();
        jobs.rememberWorld();
        jobs.openFromIndoor("id_school", 0);
        jobs.openClearSpecialty();
        jobs.verifyClearBlocked("school place");
        jobs.escape();
        jobs.verifyWorldUnchanged();
        education.verifyUnitType(200, UnitType.MASTER_CARPENTER);
        // Existing job selection moves the teacher out of the school.
        jobs.openFromIndoor("id_school", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.TOOLS);
        jobs.confirm();
        jobs.verifyIndoorJob(200, ConstructionType.BLACKSMITHS_HOUSE, 1);
        jobs.openFromIndoor("id_blacksmith", 1);
        jobs.openClearSpecialty();
        jobs.verifyClearPrompt(200, "Master Carpenter", "Free Colonist");
        jobs.confirm();
        jobs.verifyClosed();
        education.verifyUnitType(200, UnitType.COLONIST);
        jobs.verifyIndoorJob(200, ConstructionType.BLACKSMITHS_HOUSE, 1);
    }

    @Test
    @Tag("local")
    void TC_08_stale_specialist_is_rejected_without_changes() throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        game.openColonyAt(COLONY, "New Amsterdam");
        EducationPage education = new EducationPage(getContext());
        ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_blacksmith", 0);
        jobs.openClearSpecialty();
        jobs.verifyClearPrompt(300, "Firebrand Preacher", "Free Colonist");
        // The specialty disappears before the player confirms.
        jobs.clearSpecialtyBehindDialog(300);
        jobs.confirm();
        jobs.verifyClearRejectedAsChanged("no civilian specialty");
        jobs.verifyWorldUnchanged();
        education.verifyUnitType(300, UnitType.COLONIST);
        jobs.cancel();
        jobs.verifyClosed();
        jobs.verifyIndoorJob(300, ConstructionType.BLACKSMITHS_HOUSE, 0);
    }

    /** Make the outside colonist a veteran farmer carrying tools. */
    private void prepareEquippedVeteranExpert() throws Exception {
        final JsonObject saved = new Gson().fromJson(Files.readString(
                new File("src/test/scenarios/F59-education.microcol").toPath()),
                JsonObject.class);
        saved.getAsJsonObject("model").getAsJsonArray("units")
                .forEach(value -> {
                    final JsonObject unit = value.getAsJsonObject();
                    if (unit.get("id").getAsInt() == 4) {
                        unit.addProperty("type", "EXPERT_FARMER");
                        unit.addProperty("veteran", true);
                        unit.addProperty("tools", 100);
                    }
                });
        final File prepared = new File(System.getProperty("user.home"),
                "clear-specialty-prepared.microcol");
        Files.writeString(prepared.toPath(), saved.toString());
        when(getContext().getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
