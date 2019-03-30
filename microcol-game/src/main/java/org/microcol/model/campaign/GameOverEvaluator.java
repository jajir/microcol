package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * Enhanced model listener adapter. It allows easily define and process game
 * over conditions.
 */
final class GameOverEvaluator {

    private final ChainOfCommandStrategy<GameOverProcessingContext, String> cocs;

    private final EventBus eventBus;

    private final CampaignMission campaignMission;

    private final CampaignManager campaignManager;

    protected GameOverEvaluator(final EventBus eventBus,
            final Supplier<List<Function<GameOverProcessingContext, String>>> processorsSupplier,
            final CampaignMission campaignMission, final CampaignManager campaignManager) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        cocs = new ChainOfCommandStrategy<>(processorsSupplier.get());
        this.campaignMission = Preconditions.checkNotNull(campaignMission);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
    }

    public void onGameFinished(final GameFinishedEvent event) {
        final String out = cocs.apply(new GameOverProcessingContext(event, eventBus));
        if (out != null) {
            campaignMission.setFinished(true);
            campaignManager.saveMissionState();
        }
    }

}
