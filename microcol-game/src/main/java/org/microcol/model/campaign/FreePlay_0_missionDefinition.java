package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.dialog.Dialog;
import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;

import com.google.common.collect.Lists;

public final class FreePlay_0_missionDefinition extends MissionDefinition<MissionGoalsEmpty> {

    FreePlay_0_missionDefinition(final MissionCallBack missionCallBack, final Model model,
            final MissionGoalsEmpty goals) {
        super(missionCallBack, model, goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR);
    }

    @Override
    public void onIndependenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        missionCallBack.showMessage(Dialog.independenceWasDeclared_caption);
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {

    }
}
