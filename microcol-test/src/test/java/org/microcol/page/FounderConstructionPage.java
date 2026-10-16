package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Files;
import com.google.gson.*;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.*;

/** Founder-specific fixture choices and distinct observations of the colony. */
public final class FounderConstructionPage extends AbstractScreen {
    public FounderConstructionPage(TestContext context) {
        super(context);
    }

    public void prepareOffer(Founder founder, boolean queued) throws Exception {
        JsonObject saved = new Gson().fromJson(Files.readString(
                new File("src/test/scenarios/F48-founder-construction.microcol")
                        .toPath()),
                JsonObject.class);
        JsonObject model = saved.getAsJsonObject("model");
        JsonObject congress = model.getAsJsonObject("gameManager")
                .getAsJsonArray("players").get(0).getAsJsonObject()
                .getAsJsonObject("congress");
        congress.getAsJsonArray("candidates").set(2,
                new JsonPrimitive(founder.name()));
        if (queued) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", 9);
            entry.addProperty("type", "CONSTRUCTION");
            entry.addProperty("constructionType", "IRON_WORKS");
            model.getAsJsonArray("colonies").get(0).getAsJsonObject()
                    .getAsJsonArray("buildingQueue").add(entry);
        }
        File file = new File(System.getProperty("user.home"),
                "founder-construction.microcol");
        Files.writeString(file.toPath(), saved.toString());
        Mockito.when(getContext().getClassFromGuice(FileSelectingService.class)
                .loadFile(ArgumentMatchers.any(File.class))).thenReturn(file);
    }

    public void verifyMember(Founder founder) {
        assertTrue(getModel().getCurrentPlayer().getCongressMembers()
                .contains(founder));
    }

    public void verifyRushUnavailable() {
        assertTrue(getButtonById("constructionRushConfirm").isDisabled());
        assertTrue(getLabeledById("constructionRushDetail").getText()
                .contains("Completion cannot be bought now."));
        assertTrue(getModel().getColonyById(0).orElseThrow()
                .getColonyBuildingQueue().getRushQuote().isEmpty());
    }
}
