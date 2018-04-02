package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.GameFinishedEvent;

/**
 * Enhanced model listener adapter. It allows easily define and process game
 * over conditions.
 */
public abstract class ExtendedModelListenerAdapter extends ModelListenerAdapter {

    private final ChainOfCommandStrategy<GameFinishedEvent, String> cocs;

    protected ExtendedModelListenerAdapter() {
        cocs = new ChainOfCommandStrategy<>(prepareEvaluators());
    }

    protected abstract List<Function<GameFinishedEvent, String>> prepareEvaluators();

    @Override
    public void gameFinished(final GameFinishedEvent event) {
        cocs.apply(event);
    }

    class Holder {
        Function<GameFinishedEvent, String> function;
        String code;
        GameFinishedEvent event;

        Holder() {

        }
    }

}
