package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.EuropeanNation;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeMarketPage;
import org.microcol.page.NativeTeachingPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A land introduction opens native trade (R-0159). Before any introduction the
 * loaded wagon and the loaded ship beside the Aztec capital are both refused;
 * the ship lies in the Dutch colony New Amsterdam, because the inland capital
 * has no sea beside it. Cancelling the Dutch scout's chooser changes nothing,
 * and the introduction by land then makes the tribe know the Dutch nation.
 * Afterwards the ship is still refused at the inland capital, while the loaded
 * wagon is admitted and the market shows its stock, wanted goods and treasury.
 * Opening the market starts one expedition at the wagon's square, and the owner
 * update of the natives restocks once. Save and Continue keep the contact, the
 * expedition, the stock and the treasury. The gates, the reasons and the saved
 * rules are checked in the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_129_F120_native_market_test extends AbstractMicroColTest {

    /** Current-schema scenario; the market is derived from it at test start. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital, with corn and tobacco on offer. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Ordinary scout, land square west of the capital. */
    private static final int SCOUT_ID = 300;
    private static final Location SCOUT_AT = Location.of(22, 15);

    /** Loaded wagon, land square north of the capital. */
    private static final int WAGON_ID = 301;
    private static final Location WAGON_AT = Location.of(23, 14);

    /**
     * Loaded caravel docked in New Amsterdam, the Dutch colony on the land
     * square south-east of the capital; the capital itself has no sea beside
     * it.
     */
    private static final int SHIP_ID = 302;
    private static final Location SHIP_AT = Location.of(24, 16);

    /** The coastal Cherokee capital, with sea to its west. */
    private static final Location CHEROKEE_CAPITAL = Location.of(30, 18);

    /** Land squares north and north-east of the Cherokee capital. */
    private static final Location CHEROKEE_SCOUT_AT = Location.of(30, 17);
    private static final Location CHEROKEE_WAGON_AT = Location.of(31, 17);

    /** Sea square west of the Cherokee capital. */
    private static final Location CHEROKEE_SHIP_AT = Location.of(29, 18);

    private static final GoodsType CORN = GoodsType.CORN;
    private static final GoodsType TOBACCO = GoodsType.TOBACCO;

    private static final String CONTACT_NONE_EN = "Contact: none yet. A land unit must introduce your nation first.";
    private static final String CONTACT_KNOWN_EN = "Contact: this tribe knows your nation.";
    private static final String NO_CONTACT_EN = "Trade: needs a land introduction to this tribe first.";
    private static final String ADMITTED_EN = "Trade: this unit may inspect the market here.";
    private static final String SHIP_UNEASY_EN = "Trade: uneasy natives refuse ship trade.";
    private static final String SHIP_INLAND_EN = "Trade: a ship can trade only from the sea beside a coastal settlement.";
    private static final String OFFER_CORN_EN = "Sells Corn: 100 in stock.";
    private static final String OFFER_TOBACCO_EN = "Sells Tobacco: 100 in stock.";
    private static final String UNAVAILABLE_EN = "Sale slot unavailable: no more goods grow here.";
    private static final String WANTED_EN = "Wants to buy: Musket, Tools, Horse.";
    private static final String TREASURY_EN = "Treasury: 2000 gold.";
    private static final String EXPEDITION_CLOSED_EN = "No expedition is open here yet.";
    private static final String EXPEDITION_OPEN_EN = "This carrier's expedition is open here.";

    private static final String CONTACT_NONE_CS = "Kontakt: zatím žádný. Jednotka na souši musí nejprve představit váš národ.";
    private static final String CONTACT_KNOWN_CS = "Kontakt: tento kmen zná váš národ.";
    private static final String ADMITTED_CS = "Obchod: tato jednotka si může trh prohlédnout.";
    private static final String OFFER_CORN_CS = "Prodává Jídlo: na skladě 100.";
    private static final String OFFER_TOBACCO_CS = "Prodává Tabák: na skladě 100.";
    private static final String UNAVAILABLE_CS = "Prodejní místo je nedostupné: zde už neroste žádné zboží.";
    private static final String WANTED_CS = "Chce koupit: Muškety, Nářadí, Koně.";
    private static final String TREASURY_CS = "Pokladna: 2000 zlata.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_introduction_admits_the_wagon_and_the_market_survives_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTeachingPage teaching = new NativeTeachingPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        market.loadFixture(FIXTURE, null, SCOUT_ID, SCOUT_AT, WAGON_ID,
                WAGON_AT, SHIP_ID, SHIP_AT);
        fog.centerFrontier();

        // Before any introduction the loaded ship is refused, and the market
        // shows nothing about the settlement.
        visit.enterSettlement(SHIP_AT, CAPITAL);
        market.verifyContact(CONTACT_NONE_EN);
        market.verifyAdmission(NO_CONTACT_EN);
        market.verifyMarketHidden();
        visit.escape();
        visit.verifyChooserClosed();
        market.verifyNotIntroduced(EuropeanNation.DUTCH, NativeNation.AZTEC);
        market.verifyCarrierStays(SHIP_ID, SHIP_AT);

        // The loaded wagon's first trade attempt is refused the same way.
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.verifyAdmission(NO_CONTACT_EN);
        market.verifyMarketHidden();
        visit.escape();
        visit.verifyChooserClosed();
        market.verifyCarrierStays(WAGON_ID, WAGON_AT);

        // Cancelling the scout's chooser keeps the tribe unaware of the nation.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        market.verifyIntroductionOffered(true);
        visit.escape();
        visit.verifyChooserClosed();
        market.verifyNotIntroduced(EuropeanNation.DUTCH, NativeNation.AZTEC);

        // The land introduction makes the tribe know the nation; the chooser
        // reopens with the contact recorded.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        market.introduce();
        market.verifyContact(CONTACT_KNOWN_EN);
        market.verifyIntroductionOffered(false);
        market.verifyIntroduced(EuropeanNation.DUTCH, NativeNation.AZTEC);
        visit.escape();
        visit.verifyChooserClosed();

        // With contact made, the docked ship is still refused: the inland
        // capital has no sea beside it, so no market details are shown.
        visit.enterSettlement(SHIP_AT, CAPITAL);
        market.verifyContact(CONTACT_KNOWN_EN);
        market.verifyAdmission(SHIP_INLAND_EN);
        market.verifyMarketHidden();
        visit.escape();
        visit.verifyChooserClosed();
        market.verifyCarrierStays(SHIP_ID, SHIP_AT);

        // The loaded wagon's first trade is now admitted, and the market shows
        // stock, wanted goods and treasury.
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.verifyAdmission(ADMITTED_EN);
        market.verifyMarketShown(OFFER_CORN_EN, OFFER_TOBACCO_EN,
                UNAVAILABLE_EN, WANTED_EN, TREASURY_EN);
        market.verifyExpeditionLine(EXPEDITION_CLOSED_EN);
        visit.verifyChooserFits(900, 750);
        market.captureChooser("native-market-en.png");

        // Opening the market starts one expedition at the wagon's square; the
        // chooser reopens showing it, and closing it keeps it open.
        market.openMarket();
        market.verifyExpeditionLine(EXPEDITION_OPEN_EN);
        market.verifyExpeditionOpenFor(WAGON_ID, CAPITAL);
        visit.escape();
        visit.verifyChooserClosed();
        market.verifyExpeditionOpenFor(WAGON_ID, CAPITAL);
        market.verifyCarrierStays(WAGON_ID, WAGON_AT);

        // The natives' own owner update restocks once and adds their treasury.
        teaching.nextTurn();
        market.verifyStock(CAPITAL, CORN, 110);
        market.verifyStock(CAPITAL, TOBACCO, 110);
        market.verifyTreasury(CAPITAL, 2040);
        market.verifyExpeditionOpenFor(WAGON_ID, CAPITAL);

        // Save and Continue keep everything, without a second restock.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        market.verifyIntroduced(EuropeanNation.DUTCH, NativeNation.AZTEC);
        market.verifyExpeditionOpenFor(WAGON_ID, CAPITAL);
        market.verifyStock(CAPITAL, CORN, 110);
        market.verifyTreasury(CAPITAL, 2040);
    }

    @Test
    @Tag("local")
    void TC_02_uneasy_natives_refuse_ships_but_admit_the_wagon()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        // The Cherokee capital has sea beside it, so a ship there is judged by
        // the uneasy attitude rather than by the coast.
        market.loadFixture(FIXTURE, "CHEROKEE", SCOUT_ID, CHEROKEE_SCOUT_AT,
                WAGON_ID, CHEROKEE_WAGON_AT, SHIP_ID, CHEROKEE_SHIP_AT);
        fog.centerFrontier();

        // Uneasy natives still accept the land introduction.
        visit.enterSettlement(CHEROKEE_SCOUT_AT, CHEROKEE_CAPITAL);
        market.introduce();
        market.verifyIntroduced(EuropeanNation.DUTCH, NativeNation.CHEROKEE);
        visit.escape();
        visit.verifyChooserClosed();

        // The ship is refused for its uneasy attitude; nothing is shown.
        visit.enterSettlement(CHEROKEE_SHIP_AT, CHEROKEE_CAPITAL);
        market.verifyAdmission(SHIP_UNEASY_EN);
        market.verifyMarketHidden();
        visit.escape();
        visit.verifyChooserClosed();
        market.verifyCarrierStays(SHIP_ID, CHEROKEE_SHIP_AT);

        // The wagon is admitted at the same attitude.
        visit.enterSettlement(CHEROKEE_WAGON_AT, CHEROKEE_CAPITAL);
        market.verifyAdmission(ADMITTED_EN);
        market.verifyMarketShown(OFFER_CORN_EN, OFFER_TOBACCO_EN,
                UNAVAILABLE_EN, WANTED_EN, TREASURY_EN);
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_03_czech_contact_and_market_text() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        market.loadFixture(FIXTURE, null, SCOUT_ID, SCOUT_AT, WAGON_ID,
                WAGON_AT, SHIP_ID, SHIP_AT);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        market.verifyContact(CONTACT_NONE_CS);
        market.introduce();
        market.verifyContact(CONTACT_KNOWN_CS);
        visit.escape();
        visit.verifyChooserClosed();

        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.verifyAdmission(ADMITTED_CS);
        market.verifyMarketShown(OFFER_CORN_CS, OFFER_TOBACCO_CS,
                UNAVAILABLE_CS, WANTED_CS, TREASURY_CS);
        visit.verifyChooserFits(900, 750);
        market.captureChooser("native-market-cs.png");
        visit.escape();
        visit.verifyChooserClosed();
    }

}
