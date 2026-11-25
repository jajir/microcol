package org.akropia.gui.image;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.model.Player;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public final class HiddenCoastMapGenerator extends AbstractCoastMapGenerator {

    private final java.util.function.Supplier<Player> player;

    @Inject
    HiddenCoastMapGenerator(final ImageProvider imageProvider,
            final GameModelController gameModelController) {
        this(imageProvider, gameModelController::getHumanPlayer);
    }

    public HiddenCoastMapGenerator(final ImageProvider imageProvider,
            final java.util.function.Supplier<Player> player) {
        super(imageProvider);
        this.player = Preconditions.checkNotNull(player);
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
        return player.get();
    }
}
