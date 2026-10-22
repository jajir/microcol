package org.microcol.test;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Founder;
import org.microcol.page.CongressPage;
import org.microcol.page.EuropeanRelationsPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Foreign power comparison. The Dutch start without Jan de Witt, earn him by
 * choosing him in Congress and ending the turn, and only then see France's
 * actual values, while Spain, which they never met, stays withheld.
 *
 * <p>
 * Fixture (see {@link EuropeanRelationsPage#foreignReportFixture()}): France
 * owns Quebec with one resident, so population 1, colonies 1 and average 1; its
 * armed land units are soldier 6 (base 2) and a veteran dragoon (base 4.5), so
 * military 6.5; its frigate has base 16; and its net European sales in the last
 * ten owner turns are 12.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_144_F135_foreign_power_report_test
        extends AbstractMicroColTest {
    private static final String FRANCE = "France";
    private static final String SPAIN = "Spain";
    private static final String FRANCE_NAME = "Nová Francie (France)";
    private static final String SPAIN_NAME = "Nová Francie (Spain)";
    private static final String LOCKED = "Locked: recruit Jan de Witt to compare powers.";
    private static final String UNKNOWN = "Unknown: no contact with this power yet.";
    private static final List<String> FRANCE_VALUES = List.of("1", "1", "1",
            "6.5", "16", "12");
    private static final List<String> FRANCE_VALUES_CZ = List.of("1", "1", "1",
            "6,5", "16", "12");
    private static final String LOCKED_CZ = "Zamčeno: získejte Jana de Witta pro srovnání mocností.";
    private static final String UNKNOWN_CZ = "Neznámé: zatím žádný kontakt s touto mocností.";
    private static final List<Founder> OFFER = List.of(Founder.JOHN_PAUL_JONES,
            Founder.THOMAS_JEFFERSON, Founder.JAN_DE_WITT, Founder.WILLIAM_PENN,
            Founder.FERDINAND_MAGELLAN);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                EuropeanRelationsPage.foreignReportFixture());
    }

    @Test
    @Tag("local")
    void TC_01_earn_de_witt_then_compare_known_power_and_keep_unknown_withheld()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        CongressPage congress = new CongressPage(getContext());

        // Before the founder every rival keeps its relation, and no figure is
        // shown, not even for France, which the Dutch already see.
        relations.open();
        relations.verifyForeignWithheld(FRANCE, FRANCE_NAME, "Peace", LOCKED);
        relations.verifyForeignWithheld(SPAIN, SPAIN_NAME, "Peace", LOCKED);
        relations.verifyForeignTableFits();
        relations.captureDialog("foreignPowers", "foreign-power-locked.png");
        game = relations.close();

        // Choosing the founder is not recruiting him: still locked.
        congress.open();
        congress.verifyOffer(OFFER);
        congress.select(Founder.JAN_DE_WITT);
        congress.choose();
        congress.close();
        relations.open();
        relations.verifyForeignWithheld(FRANCE, FRANCE_NAME, "Peace", LOCKED);
        game = relations.close();

        // The next owner turn, after the computer nations played, brings him.
        var report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.orElseThrow().verifyThatAtLeastOneEventMessageContains(
                "Jan de Witt has joined Congress.");
        report.orElseThrow().close();

        // Now the contacted France shows all six values, the unknown Spain
        // stays withheld and says why.
        relations.open();
        relations.verifyForeignRow(FRANCE, FRANCE_NAME, "Peace", FRANCE_VALUES);
        relations.verifyForeignWithheld(SPAIN, SPAIN_NAME, "Peace", UNKNOWN);
        relations.verifyForeignTableFits();
        relations.captureDialog("foreignPowers", "foreign-power-revealed.png");
        game = relations.close();

        // Reopening and Continue show the same actual state.
        relations.open();
        relations.verifyForeignRow(FRANCE, FRANCE_NAME, "Peace", FRANCE_VALUES);
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.openWithKeyboard();
        relations.verifyForeignRow(FRANCE, FRANCE_NAME, "Peace", FRANCE_VALUES);
        relations.verifyForeignWithheld(SPAIN, SPAIN_NAME, "Peace", UNKNOWN);
        relations.verifyForeignTableFits();
    }

    @Test
    @Tag("local")
    void TC_02_czech_report_uses_localized_decimals_and_messages()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        CongressPage congress = new CongressPage(getContext());

        relations.openWithKeyboard();
        relations.verifyForeignWithheld(FRANCE, FRANCE_NAME, "Mír", LOCKED_CZ);
        relations.verifyForeignWithheld(SPAIN, SPAIN_NAME, "Mír", LOCKED_CZ);
        relations.captureDialog("foreignPowers",
                "foreign-power-locked-czech.png");
        final GamePage game = relations.close();

        congress.open();
        congress.select(Founder.JAN_DE_WITT);
        congress.choose();
        congress.close();
        game.nextTurnAndCloseDialogs();

        relations.openWithKeyboard();
        relations.verifyForeignRow(FRANCE, FRANCE_NAME, "Mír",
                FRANCE_VALUES_CZ);
        relations.verifyForeignWithheld(SPAIN, SPAIN_NAME, "Mír", UNKNOWN_CZ);
        relations.verifyForeignTableFits();
        relations.captureDialog("foreignPowers",
                "foreign-power-revealed-czech.png");
    }

}
