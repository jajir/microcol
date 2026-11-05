package org.akropia.test;

import java.io.File;
import java.util.List;
import org.akropia.page.CarrierRoutePage;
import org.akropia.page.GamePage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.akropia.model.Location;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_205_F237_carrier_route_change_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F237-carrier-route-change.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_panel_assigns_wagon_to_nearest_stop_and_cancel_preserves_state()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final CarrierRoutePage route = new CarrierRoutePage(getContext());
        // The selected wagon exposes its assignment and the matching routes.
        game.selectMapUnit(4);
        route.verifyPanel("Wagon #4 — Trade route: No trade route",
                "Assign trade route");
        route.remember(4);
        route.openFromPanel();
        route.verifyDialog("Wagon #4 — Trade route: No trade route",
                List.of("Idle cart", "Supply circuit"), null);
        route.choose("Supply circuit");
        route.verifyUnchanged();
        route.captureDialog("carrierRouteDialog", "r0415-wagon-dialog.png");
        route.cancelWithEscape();
        route.verifyUnchanged();
        // Confirming joins at the current colony, although it is stop two.
        route.openFromPanel();
        route.choose("Supply circuit");
        route.confirm();
        route.verifyAssigned(4, "Supply circuit", 1);
        route.verifyPanel("Wagon #4 — Trade route: Supply circuit",
                "Change route assignment");
        route.captureDialog("rightPanel", "r0415-wagon-panel.png");
        // Save and Continue retain the new assignment without advancing it.
        game.exitToMenu();
        new org.akropia.page.FreePlayAuditPage(getContext()).continueGame();
        route.verifyAssigned(4, "Supply circuit", 1);
    }

    @Test
    @Tag("local")
    void TC_02_context_replaces_paused_assignment_and_same_route_is_no_op() {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final CarrierRoutePage route = new CarrierRoutePage(getContext());
        // A paused assigned wagon is selected without canceling its order.
        game.selectMapUnit(6);
        route.remember(6);
        game.openUnitContextAt(Location.of(26, 16));
        route.openFromContext();
        route.verifyDialog("Wagon #6 — Trade route: Idle cart",
                List.of("Idle cart", "Supply circuit"), "Idle cart");
        route.confirm();
        route.verifyUnchanged();
        // The explicit replacement affects this wagon only and spends no moves.
        game.openUnitContextAt(Location.of(26, 16));
        route.openFromContext();
        route.choose("Supply circuit");
        route.confirm();
        route.verifyAssigned(6, "Supply circuit", 1);
        route.verifyPanel("Wagon #6 — Trade route: Supply circuit",
                "Change route assignment");
    }

    @Test
    @Tag("local")
    void TC_03_ship_lists_sea_routes_and_joins_its_current_port() {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final CarrierRoutePage route = new CarrierRoutePage(getContext());
        // Sea transport does not offer the land routes.
        game.selectMapUnit(3);
        route.remember(3);
        route.openFromPanel();
        route.verifyDialog("Galleon #3 — Trade route: No trade route",
                List.of("Ocean circuit"), null);
        route.choose("Ocean circuit");
        route.confirm();
        route.verifyAssigned(3, "Ocean circuit", 1);
    }

    @Test
    @Tag("local")
    void TC_04_czech_panel_and_dialog_keep_the_same_assignment_contract()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final CarrierRoutePage route = new CarrierRoutePage(getContext());
        // Localized controls use the same selection and model operation.
        game.selectMapUnit(4);
        route.verifyPanel("Povoz #4 — Obchodní trasa: Bez obchodní trasy",
                "Přiřadit obchodní trasu");
        route.remember(4);
        route.openFromPanel();
        route.choose("Supply circuit");
        route.captureDialog("carrierRouteDialog", "r0415-wagon-dialog-cs.png");
        route.confirmWithKeyboard();
        route.verifyAssigned(4, "Supply circuit", 1);
        route.verifyPanel("Povoz #4 — Obchodní trasa: Supply circuit",
                "Upravit přiřazení trasy");
    }
}
