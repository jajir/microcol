package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;

/**
 * Enhanced model listener adapter. It allows easily define and process game
 * over conditions.
 */
public abstract class AbstractModelListenerAdapter extends ModelListenerAdapter {

    private final ChainOfCommandStrategy<GameOverProcessingContext, String> cocs;

    protected final MissionCallBack missionCallBack;

    protected AbstractModelListenerAdapter(final MissionCallBack missionCallBack) {
        this.missionCallBack = Preconditions.checkNotNull(missionCallBack);
        cocs = new ChainOfCommandStrategy<>(prepareProcessors());
    }

    /**
     * list of methods that react on game over event.
     *
     * @return list of functions
     */
    protected abstract List<Function<GameOverProcessingContext, String>> prepareProcessors();

    @Override
    public void onGameFinished(final GameFinishedEvent event) {
        cocs.apply(new GameOverProcessingContext(event, missionCallBack));
    }

}
