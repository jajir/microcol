package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.IndependencePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Independence follows exact national support and the date, asks for a
 * confirmation that can be canceled, and musters the armed veterans of every
 * supporting colony once, with Save and Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_81_F73_declare_independence_support_test
        extends AbstractMicroColTest {

    /** Winter 1799 and the last turn before spring 1800 (start 1490). */
    private final static int WINTER_1799 = 909;

    /** The turn count of the shared scenario: the year is 1747. */
    private final static int YEAR_1747 = 700;

    private final static int END_YEAR = 1900;

    private final static Location NEW_AMSTERDAM = Location.of(25, 14);
    private final static Location DELFT = Location.of(22, 12);
    private final static Location HAARLEM = Location.of(23, 17);
    private final static Location LEIDEN = Location.of(19, 11);

    /** Eight residents in every colony, so 1600 liberty is full support. */
    private final static long FULL = 1600L;

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    private GamePage prepareAndLoad(final String name, final int turns,
            final long[] liberty, final IndependenceFixture.Soldier... soldiers)
            throws Exception {
        IndependenceFixture.prepare(getContext(), testFileName, name, turns,
                END_YEAR, liberty, soldiers);
        return WelcomePage.of(getContext()).loadGame();
    }

    /**
     * Five armed veterans at New Amsterdam (a dragoon and an expert among them)
     * at 60 percent, two at Delft and one at Leiden at full support, three at
     * Haarlem below 50 percent, plus people who are never mustered.
     */
    private GamePage prepareUnequalColonies(final String name)
            throws Exception {
        return prepareAndLoad(name, YEAR_1747,
                new long[] { 960L, FULL, 640L, FULL },
                IndependenceFixture.soldier(101, "COLONIST", NEW_AMSTERDAM,
                        true, false, true),
                IndependenceFixture.soldier(102, "COLONIST", NEW_AMSTERDAM,
                        true, true, true),
                IndependenceFixture.soldier(103, "MASTER_BLACKSMITH",
                        NEW_AMSTERDAM, true, false, true),
                IndependenceFixture.soldier(104, "COLONIST", NEW_AMSTERDAM,
                        true, false, true),
                IndependenceFixture.soldier(105, "COLONIST", NEW_AMSTERDAM,
                        true, true, true),
                IndependenceFixture.soldier(106, "COLONIST", NEW_AMSTERDAM,
                        false, false, true),
                IndependenceFixture.soldier(107, "COLONIST", NEW_AMSTERDAM,
                        true, false, false),
                IndependenceFixture.soldier(111, "COLONIST", DELFT, true, false,
                        true),
                IndependenceFixture.soldier(112, "COLONIST", DELFT, true, true,
                        true),
                IndependenceFixture.soldier(121, "COLONIST", HAARLEM, true,
                        false, true),
                IndependenceFixture.soldier(122, "COLONIST", HAARLEM, true,
                        false, true),
                IndependenceFixture.soldier(123, "COLONIST", HAARLEM, true,
                        true, true),
                IndependenceFixture.soldier(131, "COLONIST", LEIDEN, true,
                        false, true));
    }

    @Test
    @Tag("local")
    void TC_01_exact_half_of_the_national_support_is_required()
            throws Exception {
        // Load a game whose exact support is just below half: 3199 of 6400.
        final GamePage gamePage = prepareAndLoad(
                "independence-f73-below.microcol", YEAR_1747,
                new long[] { FULL, FULL - 1, 0L, 0L });
        final IndependencePage independence = new IndependencePage(
                getContext());

        // The declaration is not offered and nothing is declared.
        independence.verifyDeclareButtonVisible(false);
        independence.verifyDeclared(false);

        // Load a game with exactly half: 3200 of 6400.
        IndependenceFixture.prepare(getContext(), testFileName,
                "independence-f73-exact.microcol", YEAR_1747, END_YEAR,
                new long[] { FULL, FULL, 0L, 0L });
        gamePage.exitToMenu().loadGame();

        // The declaration is offered at exactly 50 percent.
        independence.verifyDeclareButtonVisible(true);
        independence.verifyDeclared(false);
    }

    @Test
    @Tag("local")
    void TC_02_declaration_is_offered_until_winter_1799_and_not_in_spring_1800()
            throws Exception {
        // Load full support in winter 1799, the last turn before spring 1800.
        final GamePage gamePage = prepareAndLoad(
                "independence-f73-winter.microcol", WINTER_1799,
                new long[] { FULL, FULL, FULL, FULL });
        final IndependencePage independence = new IndependencePage(
                getContext());
        independence.verifyDeclareButtonVisible(true);

        // Spring 1800 starts: the declaration is no longer offered.
        gamePage.nextTurnAndCloseDialogs();
        independence.verifyDeclareButtonVisible(false);
        independence.verifyDeclared(false);
    }

    @Test
    @Tag("local")
    void TC_03_a_stale_confirmation_is_explained_and_changes_nothing()
            throws Exception {
        // Load full support in winter 1799 and open the confirmation.
        final GamePage gamePage = prepareAndLoad(
                "independence-f73-stale.microcol", WINTER_1799,
                new long[] { FULL, FULL, FULL, FULL },
                IndependenceFixture.soldier(101, "COLONIST", NEW_AMSTERDAM,
                        true, false, true));
        final IndependencePage independence = gamePage.declareIndependence();
        independence.rememberUnitCount();
        independence.verifyConfirmationShown(
                "National support for independence: 100%.",
                "1 armed veteran(s) standing in colonies with at least 50% local support will become Continental troops. They keep their equipment and remain the same units.");

        // The calendar reaches spring 1800 while the confirmation is open.
        independence.advanceCalendarBehindOpenConfirmation();

        // Confirming is refused with an explanation and nothing changes.
        independence.confirm();
        independence.verifyConfirmationRefused(
                "Independence can no longer be declared: the conditions changed. Nothing was changed.");
        independence.cancel();
        independence.verifyConfirmationClosed();
        independence.verifyDeclared(false);
        independence.verifyMustered();
        independence.verifyUnitCountUnchanged();
        independence.verifyDeclareButtonVisible(false);
    }

    @Test
    @Tag("local")
    void TC_04_cancel_keeps_everything_and_confirm_musters_each_colony_once()
            throws Exception {
        // Load unequal colonies: 60, 100, 40 and 100 percent, 75 percent
        // nationally.
        GamePage gamePage = prepareUnequalColonies(
                "independence-f73-muster.microcol");
        final IndependencePage independence = new IndependencePage(
                getContext());
        independence.verifyDeclareButtonVisible(true);
        independence.rememberUnitCount();

        // The confirmation lists the consequences; Cancel changes nothing.
        gamePage.declareIndependence();
        independence.verifyConfirmationShown(
                "National support for independence: 75%.",
                "6 armed veteran(s) standing in colonies with at least 50% local support will become Continental troops. They keep their equipment and remain the same units.");
        independence.cancel();
        independence.verifyConfirmationClosed();
        independence.verifyDeclared(false);
        independence.verifyMustered();
        independence.verifyDeclareButtonVisible(true);

        // Escape cancels the same way.
        gamePage.declareIndependence();
        independence.pressEscape();
        independence.verifyConfirmationClosed();
        independence.verifyDeclared(false);
        independence.verifyMustered();
        independence.verifyUnitCountUnchanged();

        // Confirm once: the mission messages follow the declaration.
        gamePage.declareIndependence();
        independence.confirm();
        independence.verifyConfirmationClosed();
        final DialogMessagePage dialogMessagePage = DialogMessagePage
                .of(getContext());
        dialogMessagePage.verifyReady();
        dialogMessagePage.close();
        dialogMessagePage.close();

        // Three of five at New Amsterdam, both at Delft, none at Haarlem and
        // the one at Leiden are mustered, lowest ID first.
        independence.verifyDeclared(true);
        independence.verifyMustered(101, 102, 103, 111, 112, 131);
        independence.verifyDeclareButtonVisible(false);

        // The same people keep their equipment, expertise and place.
        independence.verifyUnit(102, UnitType.COLONIST, true, true, true,
                NEW_AMSTERDAM);
        independence.verifyUnit(103, UnitType.MASTER_BLACKSMITH, true, false,
                true, NEW_AMSTERDAM);
        independence.verifyUnit(112, UnitType.COLONIST, true, true, true,
                DELFT);
        independence.verifyUnit(104, UnitType.COLONIST, true, false, true,
                NEW_AMSTERDAM);
        independence.verifyUnit(106, UnitType.COLONIST, false, false, true,
                NEW_AMSTERDAM);
        independence.verifyUnit(107, UnitType.COLONIST, true, false, false,
                NEW_AMSTERDAM);
        independence.verifyUnit(121, UnitType.COLONIST, true, false, true,
                HAARLEM);
        independence.verifyUnitCountUnchanged();

        // Continue neither repeats the muster nor offers the declaration.
        gamePage = GamePage.of(getContext());
        gamePage.nextTurnAndCloseDialogs();
        gamePage.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        independence.verifyDeclared(true);
        independence.verifyMustered(101, 102, 103, 111, 112, 131);
        independence.verifyContinental(103);
        independence.verifyUnit(102, UnitType.COLONIST, true, true, true,
                NEW_AMSTERDAM);
        independence.verifyDeclareButtonVisible(false);
    }

}
