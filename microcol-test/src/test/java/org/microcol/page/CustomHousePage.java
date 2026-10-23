package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Set;
import com.google.gson.*;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.*;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import javafx.scene.control.CheckBox;

/** Export choices through UI actions and explicit accounting observations. */
public final class CustomHousePage extends AbstractScreen {
    public CustomHousePage(TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void prepareFixture(boolean revolution, boolean customHouse)
            throws Exception {
        JsonObject saved = new Gson().fromJson(Files.readString(
                new File("src/test/scenarios/F49-custom-house.microcol")
                        .toPath()),
                JsonObject.class);
        JsonObject model = saved.getAsJsonObject("model");
        RebellionJson.set(
                model, model.getAsJsonObject("gameManager")
                        .getAsJsonArray("players").get(0).getAsJsonObject(),
                revolution);
        if (!customHouse) {
            JsonArray buildings = model.getAsJsonArray("colonies").get(0)
                    .getAsJsonObject().getAsJsonArray("constructions");
            for (int i = buildings.size() - 1; i >= 0; i--) {
                if (buildings.get(i).getAsJsonObject().get("type").getAsString()
                        .equals("CUSTOM_HOUSE"))
                    buildings.remove(i);
            }
        }
        File file = new File(System.getProperty("user.home"),
                "custom-house.microcol");
        Files.writeString(file.toPath(), saved.toString());
        Mockito.when(getContext().getClassFromGuice(FileSelectingService.class)
                .loadFile(ArgumentMatchers.any(File.class))).thenReturn(file);
    }

    public void open() {
        clickOnButtonWithId("customHouseOpen");
    }

    public void toggle(GoodsType type) {
        getRobot().clickOn("#customHouse-" + type.name());
    }

    public void confirm() {
        clickOnButtonWithId("customHouseConfirm");
    }

    public void cancel() {
        clickOnButtonWithId("customHouseCancel");
    }

    public void configureElsewhere(Set<GoodsType> selected) {
        getRobot().interact(() -> colony()
                .configureExportGoods(colony().getExportGoods(), selected));
    }

    public void verifySelection(Set<GoodsType> selected) {
        assertEquals(selected, colony().getExportGoods());
    }

    public void verifyDraft(Set<GoodsType> selected, boolean revolution) {
        for (GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
            assertEquals(selected.contains(type),
                    getNodeFinder().lookup("#customHouse-" + type.name())
                            .queryAs(CheckBox.class).isSelected());
        }
        assertTrue(getLabeledById("customHouseRule").getText()
                .contains("Keep 100"));
        assertEquals(revolution, getLabeledById("customHouseRule").getText()
                .contains("During revolution"));
    }

    public void verifyStocks(int tobacco, int ore, int silver, int gold) {
        assertEquals(tobacco, colony().getWarehouse()
                .getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(ore,
                colony().getWarehouse().getGoods(GoodsType.ORE).getAmount());
        assertEquals(silver,
                colony().getWarehouse().getGoods(GoodsType.SILVER).getAmount());
        assertEquals(gold, colony().getOwner().getGold());
    }

    public void verifyChanged() {
        assertTrue(getButtonById("customHouseConfirm").isDisabled());
        assertEquals(
                "The colony or export choices changed. Close this window and open it again.",
                getLabeledById("customHouseFeedback").getText());
    }

    public void verifyUnavailable() {
        assertTrue(getButtonById("customHouseOpen").isDisabled());
    }

    public void verifyHomeMarketClosed() {
        assertFalse(colony().getOwner().canTradeInEurope());
    }
}
