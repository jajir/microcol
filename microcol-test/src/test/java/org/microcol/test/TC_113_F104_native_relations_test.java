package org.microcol.test;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.model.NativeHostility;
import org.microcol.model.NativeNation;
import org.microcol.page.CongressPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeRelationsPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Tribal anger and local alarm of the Dutch toward the Aztec capital change
 * only at the actual Pocahontas arrival: choosing, cancelling and Continue keep
 * the saved values, the next real turn clears them once, and the pressure of
 * the following turns is added on top of the non-zero result.
 *
 * <p>
 * The Dutch pressure on the capital is 0.25 for each of the two colonists in
 * New Amsterdam one square away, plus 2 for the armed frigate two squares away:
 * 2.5. Pocahontas halves it to 1.25, so the arrival turn ends with local alarm
 * 1.25 and tribal anger 1.25 / 20 = 0.0625. The next turn gives alarm 1.25 - 1
 * + 1.25 = 1.5 and anger 0 + 1.5 / 20 = 0.075.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_113_F104_native_relations_test extends AbstractMicroColTest {

    /**
     * Aztec capital next to the Dutch colony, learned by sight at the start.
     */
    private static final Location AZTEC_CAPITAL = Location.of(23, 15);

    /** Inca capital far inland, never inside Dutch sight. */
    private static final Location INCA_CAPITAL = Location.of(15, 22);

    private static final List<Founder> OFFER = List.of(Founder.JOHN_PAUL_JONES,
            Founder.POCAHONTAS, Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
            Founder.FERDINAND_MAGELLAN);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F104-native-relations.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_pocahontas_arrival_clears_positive_relations_once()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        NativeRelationsPage relations = new NativeRelationsPage(getContext());

        // The fixture starts with non-zero Dutch relations toward the Aztecs;
        // the larger local alarm makes the capital uneasy.
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                10);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 30);
        relations.verifyHostility(EuropeanNation.DUTCH, AZTEC_CAPITAL,
                NativeHostility.UNEASY);
        relations.verifyFounderMember(EuropeanNation.DUTCH, Founder.POCAHONTAS,
                false);
        // The unseen Inca capital offers no relations at all.
        relations.verifyRelationsHidden(EuropeanNation.DUTCH, INCA_CAPITAL);

        // Selecting and then cancelling chooses nothing and changes nothing.
        congress.open();
        congress.verifyOffer(OFFER);
        congress.select(Founder.POCAHONTAS);
        congress.close();
        relations.verifyNoSelectedFounder(EuropeanNation.DUTCH);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                10);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 30);

        // Choosing is not arrival: the choice is recorded, relations wait.
        congress.open();
        congress.select(Founder.POCAHONTAS);
        congress.choose();
        congress.close();
        relations.verifySelectedFounder(EuropeanNation.DUTCH,
                Founder.POCAHONTAS);
        relations.verifyFounderMember(EuropeanNation.DUTCH, Founder.POCAHONTAS,
                false);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                10);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 30);

        // Continue before arrival keeps the choice and the saved relations.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        relations.verifySelectedFounder(EuropeanNation.DUTCH,
                Founder.POCAHONTAS);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                10);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 30);

        // The next Dutch turn has enough bells: Pocahontas joins Congress, the
        // old 10 and 30 are cleared once and only 1.25 of halved pressure and
        // its 1.25 / 20 remain.
        var report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.orElseThrow().verifyThatAtLeastOneEventMessageContains(
                "Pocahontas has joined Congress.");
        report.orElseThrow().close();
        relations.verifyFounderMember(EuropeanNation.DUTCH, Founder.POCAHONTAS,
                true);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.0625);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 1.25);
        relations.verifyHostility(EuropeanNation.DUTCH, AZTEC_CAPITAL,
                NativeHostility.CONTENT);
        relations.rememberNativeValues(EuropeanNation.DUTCH, NativeNation.AZTEC,
                AZTEC_CAPITAL);

        // Continue after arrival keeps the membership and the same non-zero
        // values: loading does not clear or update them again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        relations.verifyFounderMember(EuropeanNation.DUTCH, Founder.POCAHONTAS,
                true);
        relations.verifyNativeValuesUnchangedSinceRemembered(
                EuropeanNation.DUTCH, NativeNation.AZTEC, AZTEC_CAPITAL);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.0625);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 1.25);

        // The following real turn adds the same halved pressure to what is
        // left instead of resetting it: 1.25 - 1 + 1.25 and 0 + 1.5 / 20.
        game.nextTurn().ifPresent(ScreenTurnReport::close);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.075);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 1.5);
        relations.rememberNativeValues(EuropeanNation.DUTCH, NativeNation.AZTEC,
                AZTEC_CAPITAL);

        // Continue after that turn keeps the new non-zero values, and the
        // hidden capital still exposes nothing.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        relations.verifyNativeValuesUnchangedSinceRemembered(
                EuropeanNation.DUTCH, NativeNation.AZTEC, AZTEC_CAPITAL);
        relations.verifyTribalAnger(EuropeanNation.DUTCH, NativeNation.AZTEC,
                0.075);
        relations.verifyLocalAlarm(EuropeanNation.DUTCH, AZTEC_CAPITAL, 1.5);
        relations.verifyRelationsHidden(EuropeanNation.DUTCH, INCA_CAPITAL);
    }
}
