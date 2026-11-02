package org.akropia.gui.event.model;

import java.util.HashSet;
import java.util.Set;

import org.akropia.gui.SuccessCuePlayer;
import org.akropia.model.campaign.EventFinishMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Requests the success cue as the victory message of a finished mission begins
 * (R-0228). Only an explicit victory is cued, and each playthrough at most
 * once, so a repeated notification, a reopened game or a victory won again from
 * an earlier save of the same playthrough stay silent. The visual result never
 * depends on the cue.
 */
final class MissionVictoryCue {

    private final Logger logger = LoggerFactory
            .getLogger(MissionVictoryCue.class);

    private final SuccessCuePlayer player;

    private final Set<String> cuedPlaythroughs = new HashSet<>();

    MissionVictoryCue(final SuccessCuePlayer player) {
        this.player = Preconditions.checkNotNull(player);
    }

    /**
     * @param victory       whether the finished mission whose first message is
     *                      being shown is a victory, as stated by
     *                      {@link EventFinishMission#isVictory()}
     * @param playthroughId required identity of the game that has just ended
     */
    synchronized void onFinishMessageShown(final boolean victory,
            final String playthroughId) {
        Preconditions.checkNotNull(playthroughId);
        if (!victory || !cuedPlaythroughs.add(playthroughId)) {
            return;
        }
        try {
            player.play();
        } catch (RuntimeException e) {
            logger.warn("Mission success cue failed: {}", e.getMessage());
        }
    }

}
