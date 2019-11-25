package org.microcol.gui.image;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public final class HiddenCoastMapGenerator extends AbstractCoastMapGenerator {

    private final GameModelController gameModelController;

    @Inject
    HiddenCoastMapGenerator(final ImageProvider imageProvider,
            final GameModelController gameModelController) {
        super(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Override
    public String getPrefix() {
        return "hidden-";
    }

    @Override
    public boolean isVoid(final InfoHolder infoHolder) {
        return getPlayer().isVisible(infoHolder.loc());
    }

    @Override
    public boolean isMass(final InfoHolder infoHolder) {
        return !getPlayer().isVisible(infoHolder.loc());
    }

    @Override
    public boolean skipp(final InfoHolder infoHolder) {
        return !getPlayer().isVisible(infoHolder.loc());
    }

    private Player getPlayer() {
        return gameModelController.getHumanPlayer();
    }
}
