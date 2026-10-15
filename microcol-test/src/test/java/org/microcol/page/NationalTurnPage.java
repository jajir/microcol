package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.TurnFinishedEvent;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/** Observes completed national turns and saved action boundaries. */
public final class NationalTurnPage extends AbstractScreen {
    private final List<String> finished = new CopyOnWriteArrayList<>();

    public NationalTurnPage(final TestContext context) {
        super(context);
    }

    public void observeTurnOrder() {
        finished.clear();
        getRobot().interact(
                () -> getModel().addListener(new ModelListenerAdapter() {
                    @Override
                    public void onTurnFinished(final TurnFinishedEvent event) {
                        finished.add(event.getPlayer().getName());
                    }
                }));
    }

    public void verifyTurnOrder(final List<String> expected) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> finished.size() >= expected.size()
                        && getModel().isGameRunning()
                        && getModel().getCurrentPlayer().isHuman());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, finished);
        assertEquals("Dutch", getModel().getCurrentPlayer().getName());
    }

    public JsonElement readTurnState() {
        return new Gson().toJsonTree(getModel().save());
    }

    public void verifyTurnState(final JsonElement expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, readTurnState(),
                "Continue must preserve the already prepared turn exactly");
    }
}
