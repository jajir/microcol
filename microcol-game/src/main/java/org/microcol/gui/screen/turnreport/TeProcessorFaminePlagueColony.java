package org.microcol.gui.screen.turnreport;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.UnitTypes;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventFaminePlagueColony;

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
            return new TeItemSimple(getI18n().get(TurnEvents.faminePlagueColony,
                    te.getColonyName(),
                    getI18n().get(UnitTypes.getUnitName(te.getVictim())),
                    te.getStocks().stream().filter(
                            goods -> goods.isNotZero() || goods.getType()
                                    .equals(org.microcol.model.GoodsType.CORN))
                            .map(goods -> getI18n()
                                    .get(GoodsTypeName.getNameForGoodsType(
                                            goods.getType()))
                                    + ": " + goods.getAmount())
                            .collect(Collectors.joining(", "))));
        }
        return null;
    }

}
