package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Naval review, resolved cargo losses and persistence through the player's UI.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_64_F55_naval_combat_test extends AbstractMicroColTest {
    private static final Location ATTACKER = Location.of(20, 10);
    private static final Location DEFENDER = Location.of(21, 10);
    private static final File FIXTURE = new File(
            "src/test/scenarios/F55-naval-combat.microcol");
    private static final String DRAKE_ATTACKER = "Base: 8\nAttack: +50%\nPrivateers under Francis Drake: +50%\nFinal strength: 16";
    private static final String GALLEON_DEFENDER = "Base: 10\nFinal strength: 10";

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_cancel_then_sink_and_keep_prize_after_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        NavalBattlePage naval = new NavalBattlePage(getContext());
        // Review states the actual ship strengths before any cargo or war
        // mutation.
        attack.rememberWorld();
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.verifyReview("Frigate", "Galleon", "declares war", 0, 6);
        attack.verifyStrengths("Base: 16\nAttack: +50%\nFinal strength: 24",
                "Base: 10\nFinal strength: 10", "Attacker win chance: 70.59%");
        attack.cancel();
        attack.verifyGameplayUnchanged();
        // Saved draws choose victory and sinking; only fitting goods are
        // prizes.
        naval.chooseSavedSeed(7);
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.confirm();
        naval.verifyOutcomeText("#6 sank.", "captured: 60 Fur, 7 Ore.",
                "Lost passengers: 1. Lost goods: 150 Silver.");
        naval.captureDialog("navalBattleOutcome", "naval-sinking-english.png");
        naval.closeOutcome();
        naval.verifyAttackerPrizeAndSinking();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        naval.verifyAttackerPrizeAndSinking();
    }

    @Test
    @Tag("local")
    void TC_02_czech_defeat_preserves_passenger_and_saved_repair()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        AttackPage attack = new AttackPage(getContext());
        NavalBattlePage naval = new NavalBattlePage(getContext());
        // The second saved draw damages the losing ship rather than sinking it.
        naval.chooseSavedSeed(22);
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.confirm();
        naval.verifyOutcomeText("poškozena", "Evropa", "95",
                "Pasažéři na palubě: 1.");
        naval.captureDialog("navalBattleOutcome", "naval-damage-czech.png");
        naval.closeOutcome();
        naval.verifyAttackerDamaged();
        EuropePortScreen europe = GamePage.of(getContext())
                .openEuroperPort("Evropa");
        ShipRepairPage repairs = new ShipRepairPage(getContext());
        repairs.open();
        naval.verifyRepairOverview(
                "frigata #0 — Evropa\nPřeprava; do zprovoznění zbývá tahů vlastníka: 3");
        repairs.close();
        europe.close();
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        naval.verifyAttackerDamaged();
    }

    @Test
    @Tag("local")
    void TC_03_changed_cargo_rejects_without_consuming_battle()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        NavalBattlePage naval = new NavalBattlePage(getContext());
        attack.chooseAttack(ATTACKER, DEFENDER);
        // Simulate a live state change after the player saw the cargo-dependent
        // review.
        naval.changeReviewedCargo();
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.closeMessage();
        attack.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_04_drake_privateer_preview_survives_continue() throws Exception {
        // Load the same battle with a privateer whose owner has recruited
        // Drake.
        prepareDrakePrivateer();
        WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        // The review names the Drake bonus and shows the exact 16 against 10
        // odds.
        attack.rememberWorld();
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.verifyStrengths(DRAKE_ATTACKER, GALLEON_DEFENDER,
                "Attacker win chance: 61.54%");
        attack.cancel();
        attack.verifyGameplayUnchanged();

        // Continue restores the saved membership; reopening shows the same
        // odds.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        attack.rememberWorld();
        attack.chooseAttack(ATTACKER, DEFENDER);
        attack.verifyStrengths(DRAKE_ATTACKER, GALLEON_DEFENDER,
                "Attacker win chance: 61.54%");
        attack.cancel();
        attack.verifyGameplayUnchanged();
    }

    private void prepareDrakePrivateer() throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(FIXTURE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if (unit.get("id").getAsInt() == 0) {
                unit.addProperty("type", "PRIVATEER");
                // A privateer holds two goods or passengers; keep ore and the
                // passenger in the first two holds.
                final JsonArray slots = unit.getAsJsonObject("cargo")
                        .getAsJsonArray("slots");
                while (slots.size() > 2) {
                    slots.remove(slots.size() - 1);
                }
            }
        });
        model.getAsJsonObject("gameManager").getAsJsonArray("players")
                .forEach(value -> {
                    final JsonObject player = value.getAsJsonObject();
                    if ("Dutch".equals(player.get("name").getAsString())) {
                        player.add("congress", drakeCongress());
                    }
                });
        final File prepared = new File(System.getProperty("user.home"),
                "naval-drake-prepared.microcol");
        Files.writeString(prepared.toPath(), saved.toString());
        when(getContext().getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    private static JsonObject drakeCongress() {
        final JsonObject congress = new JsonObject();
        congress.addProperty("activated", true);
        congress.addProperty("bells", 0);
        congress.add("members", strings("FRANCIS_DRAKE"));
        congress.add("candidates",
                strings("JOHN_PAUL_JONES", "THOMAS_JEFFERSON", "ADAM_SMITH",
                        "WILLIAM_PENN", "FERDINAND_MAGELLAN"));
        congress.addProperty("generation", 1);
        congress.addProperty("jonesDeliveryPending", false);
        return congress;
    }

    private static JsonArray strings(final String... values) {
        final JsonArray array = new JsonArray();
        for (final String value : values) {
            array.add(value);
        }
        return array;
    }
}
