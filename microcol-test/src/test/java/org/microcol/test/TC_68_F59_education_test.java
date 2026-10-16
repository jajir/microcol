package org.microcol.test;

import java.io.File;
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
}
