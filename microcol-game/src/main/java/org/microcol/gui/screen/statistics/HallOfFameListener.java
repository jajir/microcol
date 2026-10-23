package org.microcol.gui.screen.statistics;

import org.microcol.gui.util.Listener;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.campaign.po.HallOfFameDao;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.ScoreBoundaryRequiredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Records the frozen score in the Hall of Fame when spring 1800 freezes it and
 * when the game ends (R-0197). A storage failure is only logged, so play is
 * never interrupted; the statistics tab records the same result again when it
 * opens.
 */
@Listener
public final class HallOfFameListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HallOfFameListener.class);

    private final HallOfFameDao dao;

    @Inject
    HallOfFameListener(final HallOfFameDao dao) {
        this.dao = Preconditions.checkNotNull(dao);
    }

    @Subscribe
    private void onScoreBoundaryRequired(
            final ScoreBoundaryRequiredEvent event) {
        record(event.getModel());
    }

    @Subscribe
    private void onGameFinished(final GameFinishedEvent event) {
        record(event.getModel());
    }

    private void record(final Model model) {
        try {
            dao.recordFrozenResult(model);
        } catch (final MicroColException failure) {
            LOGGER.warn("Hall of Fame was not saved", failure);
        }
    }
}
