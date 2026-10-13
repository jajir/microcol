package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.PlaceEuropePier;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

/**
 * Baseline observations, including explicit gaps; not future rule acceptance.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_22_F15_free_play_capability_audit_test
        extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_pier_clicks_offer_equipment() throws Exception {
        // An ordinary colonist is waiting on the real European pier.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropePortScreen europe = game.openEuroperPort();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyPierEquipmentBaseline(1, 0);
        // Both primary and context click expose the same equipment choices.
        EuropeEquipmentPage equipment = new EuropeEquipmentPage(getContext());
        audit.clickPierColonist(MouseButton.PRIMARY);
        equipment.verifyMenu("Buy 50 muskets", "Buy 50 horses",
                "Buy 100 tools");
        equipment.escape();
        audit.verifyPierEquipmentBaseline(1, 0);
        audit.clickPierColonist(MouseButton.SECONDARY);
        equipment.verifyMenu("Buy 50 muskets", "Buy 50 horses",
                "Buy 100 tools");
        equipment.escape();
        audit.verifyPierEquipmentBaseline(1, 0);
        europe.verifyGold(10000);
        europe.close();
    }

    @Test
    @Tag("local")
    void TC_02_purchase_roster_excludes_expert_blacksmith() throws Exception {
        // Reach the purchase dialog through Europe, not a model factory.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        europe.openBuyUnitScreen();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyPurchaseOffers(java.util.List.of("Free Colonist", "Frigate",
                "Galleon", "Master Blacksmith"));
        // Cancel the offer list without spending money.
        audit.closePurchaseDialog();
        europe.verifyGold(10000);
        europe.close();
    }

    @Test
    @Tag("local")
    void TC_03_colonist_embarks_and_returns_to_european_pier()
            throws Exception {
        // Select the waiting ship and board one real pier colonist.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        europe.selectUnitFromPort(0);
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.embarkPierColonist();
        audit.verifyPassengerPlace(100, PlaceCargoSlot.class, 0, false);
        // Drag the same passenger back; no unit is created or lost.
        audit.disembarkToPier();
        audit.verifyPassengerPlace(100, PlaceEuropePier.class, 0, false);
        audit.verifyPierEquipmentBaseline(1, 0);
        europe.close();
    }

    @Test
    @Tag("local")
    void TC_04_wagon_artwork_gap_after_loading_fixture() throws Exception {
        // The fixture contains no wagon because unsupported artwork prevents
        // opening the map;
        // explicitly observe ImageProvider failure and verify ordinary colony
        // screen still opens.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        new FreePlayAuditPage(getContext()).verifyWagonArtworkUnsupported();
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_05_construction_queue_is_hidden() throws Exception {
        // The queue control exists but is hidden in the actual colony screen.
        ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY, "New Amsterdam");
        colony.verifyColonyName("New Amsterdam");
        new FreePlayAuditPage(getContext())
                .verifyConstructionQueueUnavailable(2);
        // Population eligibility remains a source finding, not a UI result.
        colony.close();
    }
}
