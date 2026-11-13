package org.akropia.gui.screen.turnreport;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.UnitTypes;

import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventFaminePlagueColony;

import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorFaminePlagueColony extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorFaminePlagueColony(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventFaminePlagueColony) {
            final TurnEventFaminePlagueColony te = (TurnEventFaminePlagueColony) turnEvent;
            if (te.getStocks().isEmpty()) {
                return new TeItemSimple(getI18n().get(
                        TurnEvents.faminePlagueColonyDestroyed,
                        te.getColonyName(),
                        getI18n().get(UnitTypes.getUnitName(te.getVictim()))));
            }
            return new TeItemColony(getI18n().get(TurnEvents.faminePlagueColony,
                    te.getColonyName(),
                    getI18n().get(UnitTypes.getUnitName(te.getVictim())),
                    te.getStocks().stream().filter(
                            goods -> goods.isNotZero() || goods.getType()
                                    .equals(org.akropia.model.GoodsType.GRAIN))
                            .map(goods -> getI18n()
                                    .get(GoodsTypeName.getNameForGoodsType(
                                            goods.getType()))
                                    + ": " + goods.getAmount())
                            .collect(Collectors.joining(", "))),
                    te.getColonyId());
        }
        return null;
    }

}
