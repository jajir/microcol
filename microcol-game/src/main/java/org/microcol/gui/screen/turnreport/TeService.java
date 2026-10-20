package org.microcol.gui.screen.turnreport;

import java.util.List;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.Model;
import org.microcol.model.turnevent.TurnEvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manage turn events. Provide list of turn events to user interface.
 */
@Singleton
public class TeService {

    private final GameModelController gameModelController;
    private final ChainOfCommandStrategy<TurnEvent, TeItemSimple> turnEventResolver;

    @Inject
    TeService(final GameModelController gameModelController,
            final TeProcessorGoodsWasThrownAway teProcessorGoodsWasThrownAway,
            final TeProcessorNewUnitInColony teProcessorNewUnitInColony,
            final TeProcessorNewUnitInEurope teProcessorNewUnitInEurope,
            final TeProcessorColonyWasDestroyed teProcessorColonyWasDestroyed,
            final TeProcessorColonyWasLost teProcessorColonyWasLost,
            final TeProcessorFamineWillPlagueColony teProcessorFamineWillPlagueColony,
            final TeProcessorFaminePlagueColony teProcessorFaminePlagueColony,
            final TeProcessorShipComeToEuropePort teProcessorShipComeToEuropePort,
            final TeProcessorShipComeToHighSeas teProcessorShipComeToHighSeas,
            final TeProcessorFounderArrived teProcessorFounderArrived,
            final TeProcessorCustomHouseExport teProcessorCustomHouseExport,
            final TeProcessorPeaceOffer teProcessorPeaceOffer,
            final TeProcessorDiplomacy teProcessorDiplomacy,
            final TeProcessorShipRepair teProcessorShipRepair,
            final TeProcessorGoTo teProcessorGoTo,
            final TeProcessorEducation education,
            final TeProcessorConstructionWaitingForTools constructionWaitingForTools,
            final TeProcessorForestLumberLost forestLumberLost) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);

        this.turnEventResolver = new ChainOfCommandStrategy<>(Lists
                .newArrayList(Preconditions.checkNotNull(teProcessorShipRepair),
                        Preconditions.checkNotNull(teProcessorGoTo),
                        Preconditions.checkNotNull(education),
                        Preconditions.checkNotNull(constructionWaitingForTools),
                        Preconditions.checkNotNull(forestLumberLost),
                        Preconditions.checkNotNull(teProcessorPeaceOffer),
                        Preconditions.checkNotNull(teProcessorDiplomacy),
                        Preconditions
                                .checkNotNull(teProcessorCustomHouseExport),
                        Preconditions.checkNotNull(teProcessorFounderArrived),
                        Preconditions
                                .checkNotNull(teProcessorGoodsWasThrownAway),
                        Preconditions.checkNotNull(teProcessorNewUnitInColony),
                        Preconditions.checkNotNull(teProcessorNewUnitInEurope),
                        Preconditions
                                .checkNotNull(teProcessorColonyWasDestroyed),
                        Preconditions.checkNotNull(teProcessorColonyWasLost),
                        Preconditions.checkNotNull(
                                teProcessorFamineWillPlagueColony),
                        Preconditions
                                .checkNotNull(teProcessorFaminePlagueColony),
                        Preconditions
                                .checkNotNull(teProcessorShipComeToEuropePort),
                        Preconditions
                                .checkNotNull(teProcessorShipComeToHighSeas)));
    }

    private Model getModel() {
        return gameModelController.getModel();
    }

    public List<TeItemSimple> getMessages() {
        final List<TurnEvent> turnEvents = getModel()
                .getTurnEventsLocalizedMessages(
                        gameModelController.getHumanPlayer());
        return turnEvents.stream()
                .map(turnEvent -> turnEventResolver.apply(turnEvent))
                .collect(ImmutableList.toImmutableList());
    }

}
