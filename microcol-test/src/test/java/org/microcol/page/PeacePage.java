package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.*;
import javafx.scene.control.Button;
import org.testfx.util.WaitForAsyncUtils;

/** Peace actions use the report; direct edits simulate concurrent changes. */
public final class PeacePage extends AbstractScreen {
    public PeacePage(TestContext context) {
        super(context);
    }

    public void prepareFranklinFixture() throws Exception {
        com.google.gson.JsonObject saved = new com.google.gson.Gson().fromJson(
                java.nio.file.Files.readString(java.nio.file.Path.of(
                        "src/test/scenarios/F50-peace-negotiation.microcol")),
                com.google.gson.JsonObject.class);
        com.google.gson.JsonObject model = saved.getAsJsonObject("model");
        for (var element : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            var player = element.getAsJsonObject();
            if (player.get("name").getAsString().equals("Dutch")) {
                var congress = player.getAsJsonObject("congress");
                congress.addProperty("activated", true);
                congress.addProperty("generation", 2);
                congress.add("members", new com.google.gson.Gson()
                        .toJsonTree(java.util.List.of("BENJAMIN_FRANKLIN")));
                congress.add("candidates",
                        new com.google.gson.Gson().toJsonTree(java.util.List.of(
                                "JOHN_PAUL_JONES", "THOMAS_PAINE", "ADAM_SMITH",
                                "WILLIAM_PENN", "FERDINAND_MAGELLAN")));
            }
        }
        for (var element : model.getAsJsonArray("units")) {
            var unit = element.getAsJsonObject();
            if (unit.get("id").getAsInt() == 6)
                unit.addProperty("holdingGuns", false);
        }
        java.io.File file = new java.io.File(System.getProperty("user.home"),
                "franklin-peace.microcol");
        java.nio.file.Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito.when(getContext()
                .getClassFromGuice(org.microcol.gui.FileSelectingService.class)
                .loadFile(org.mockito.ArgumentMatchers.any(java.io.File.class)))
                .thenReturn(file);
    }

    public void verifyAutomaticOffers(int reports, int price) {
        Model model = getModel();
        Player actor = model.getPlayerByName("Dutch");
        assertEquals(reports,
                model.getTurnEventsLocalizedMessages(actor).stream().filter(
                        org.microcol.model.turnevent.TurnEventPeaceOffer.class::isInstance)
                        .count());
        assertEquals(price, model
                .getPendingPeaceOffer(actor, model.getPlayerByName("France"))
                .orElseThrow().getPrice());
    }

    public void review() {
        clickOnButtonWithId("peace-0-1");
    }

    public void cancel() {
        clickOnButtonWithId("peaceCancel");
    }

    public void escape() {
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void later() {
        clickOnButtonWithId("peaceLater");
    }

    public void confirm() {
        clickOnButtonWithId("peaceConfirm");
    }

    public void confirmTwice() {
        Button button = getButtonById("peaceConfirm");
        getRobot().push(javafx.scene.input.KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(button::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void moveVisitorAway() {
        getRobot().interact(() -> getModel().getUnitById(0)
                .placeToLocation(Location.of(24, 16)));
    }

    public void setGold(int gold) {
        getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(gold));
    }

    public void verifyQuote(int price, int gold, int after, boolean enabled) {
        String expected = "France offers peace for " + price + " gold.\n"
                + (enabled
                        ? "Your treasury: " + gold + " gold.\nAfter payment: "
                                + after + " gold."
                        : "Your treasury when quoted: " + gold + " gold.");
        assertEquals(expected, getLabeledById("peaceDetail").getText());
        assertEquals(!enabled, getButtonById("peaceConfirm").isDisabled());
    }

    public void verifyState(int actorGold, int rivalGold,
            EuropeanRelationState relation, boolean pending) {
        Model model = getModel();
        Player actor = model.getPlayerByName("Dutch");
        Player rival = model.getPlayerByName("France");
        assertEquals(actorGold, actor.getGold());
        assertEquals(rivalGold, rival.getGold());
        assertEquals(relation, model.getEuropeanRelation(actor, rival));
        assertEquals(relation, model.getEuropeanRelation(rival, actor));
        assertEquals(pending,
                model.getPendingPeaceOffer(actor, rival).isPresent());
    }

    public void verifyFeedback(String expected, boolean enabled) {
        assertTrue(
                getLabeledById("peaceFeedback").getText().contains(expected));
        assertEquals(!enabled, getButtonById("peaceConfirm").isDisabled());
    }

    public void verifyContactUnavailable() {
        assertTrue(getButtonById("peace-0-1").isDisabled());
    }
}
