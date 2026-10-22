package org.microcol.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.EuropeanRelationsPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Foreign power comparison while Jan de Witt has not been recruited: both
 * rivals keep their relation, no figure is shown, and the state is reachable by
 * keyboard and mouse. Earning the founder is covered by
 * {@link TC_144_F135_foreign_power_report_test}.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_144_F135_foreign_power_locked_test
        extends AbstractMicroColTest {
    private static final String FRANCE = "France";
    private static final String SPAIN = "Spain";
    private static final String FRANCE_NAME = "Nová Francie (France)";
    private static final String SPAIN_NAME = "Nová Francie (Spain)";
    private static final String PEACE = "Peace";
    private static final String LOCKED = "Locked: recruit Jan de Witt to compare powers.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                EuropeanRelationsPage.foreignReportFixture());
    }

    @Test
    @Tag("local")
    void TC_01_locked_report_keeps_relations_and_hides_every_figure()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        relations.verifyForeignWithheld(FRANCE, FRANCE_NAME, PEACE, LOCKED);
        relations.verifyForeignWithheld(SPAIN, SPAIN_NAME, PEACE, LOCKED);
        relations.verifyForeignTableFits();
    }

    @Test
    @Tag("local")
    void TC_02_locked_report_is_reachable_by_keyboard() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.openWithKeyboard();
        relations.verifyForeignWithheld(FRANCE, FRANCE_NAME, PEACE, LOCKED);
        relations.verifyForeignWithheld(SPAIN, SPAIN_NAME, PEACE, LOCKED);
    }

}
