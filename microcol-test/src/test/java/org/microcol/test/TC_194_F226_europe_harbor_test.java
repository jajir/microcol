package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.page.EuropeHarborPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.InGameLayoutPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_194_F226_europe_harbor_test extends AbstractMicroColTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F226-europe-harbor.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_independent_scrolls_and_last_ship_trade() throws Exception {
        // All four lists exceed forty-two units without resizing their artwork.
        final EuropePortScreen port = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        final EuropeHarborPage harbor = new EuropeHarborPage(getContext());
        harbor.resize(1280, 900);
        harbor.verifyGroups(43);
        harbor.verifyLayoutAndNativeImages();
        harbor.verifyScrollPositions(0, 0, 0, 0);

        // Reach the last docked ship and select it with the keyboard.
        harbor.scrollGroup("dock", 1);
        harbor.verifyLastItemVisible("dock");
        harbor.verifyScrollPositions(0, 0, 1, 0);
        harbor.selectShip(3142, true);
        harbor.verifySelectedCargo(3142, 0, Goods.of(GoodsType.ORE, 43));
        port.verifyAvailableHoldCount(4);

        // Buy into this ship, keeping its selection and existing cargo.
        port.buyGoodsAndMoveItToCargoSlot(0, 1);
        harbor.verifySelectedCargo(3142, 1, Goods.of(GoodsType.CORN, 100));
        harbor.verifySelectedCargo(3142, 0, Goods.of(GoodsType.ORE, 43));
        port.verifyAvailableHoldCount(4);
        port.verifyCargoSlotEmpty(0, 1);
        harbor.verifyScrollPositions(0, 0, 1, 0);

        // Every other last item is reachable independently.
        harbor.scrollGroup("incoming", 1);
        harbor.verifyLastItemVisible("incoming");
        harbor.openVoyageOrders(1142, true);
        harbor.verifyVoyageActions(1142);
        harbor.verifyVoyageOrdersShowing(1142, true);
        harbor.closeVoyageOrders();
        harbor.verifyVoyageOrdersShowing(1142, false);
        harbor.verifyScrollPositions(1, 0, 1, 0);
        harbor.scrollGroup("outgoing", 1);
        harbor.verifyLastItemVisible("outgoing");
        harbor.openVoyageOrders(2142, false);
        harbor.verifyVoyageActions(2142);
        harbor.verifyVoyageOrdersShowing(2142, true);
        harbor.closeVoyageOrders();
        harbor.verifyVoyageOrdersShowing(2142, false);
        harbor.scrollGroup("pier", 1);
        harbor.verifyLastItemVisible("pier");
        harbor.verifyScrollPositions(1, 1, 1, 1);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0336-harbor-full.png");

        // Resizing keeps cargo, all prices and the original image size usable.
        harbor.resize(1000, 760);
        harbor.showGoods();
        harbor.verifyLayoutAndNativeImages();
        harbor.verifyAllGoodsVisible();
        harbor.verifySelectedCargo(3142, 0, Goods.of(GoodsType.ORE, 43));
        port.verifyAvailableHoldCount(4);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0336-harbor-small.png");
        port.close();
        new InGameLayoutPage(getContext()).verifyMapReturned();
    }

    @Test
    @Tag("local")
    void TC_02_czech_crowded_harbor() throws Exception {
        // Select Czech through the existing settings screen before loading.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.verifyLanguageOptionsShown();
        settings.setCzechLanguage();
        final EuropePortScreen port = settings.goBack().loadGame()
                .openEuroperPort("Evropa");
        final EuropeHarborPage harbor = new EuropeHarborPage(getContext());
        harbor.resize(1280, 900);
        harbor.verifyGroups(43);
        harbor.verifyLayoutAndNativeImages();
        harbor.scrollGroup("dock", 1);
        harbor.selectShip(3142, false);
        harbor.verifySelectedCargo(3142, 0, Goods.of(GoodsType.ORE, 43));
        port.verifyAvailableHoldCount(4);
        harbor.showGoods();
        harbor.verifyAllGoodsVisible();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0336-harbor-czech.png");
    }
}
