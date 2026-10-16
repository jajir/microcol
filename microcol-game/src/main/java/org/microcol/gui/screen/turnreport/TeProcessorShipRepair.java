package org.microcol.gui.screen.turnreport;

import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.inject.Inject;
import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.screen.europe.Europe;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventShipRepair;

/** Translate recorded outcomes without advancing repair or changing cargo. */
public final class TeProcessorShipRepair extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorShipRepair(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (!(event instanceof TurnEventShipRepair)) {
            return null;
        }
        TurnEventShipRepair repair = (TurnEventShipRepair) event;
        String ship = getI18n()
                .get(UnitTypes.getUnitName(repair.getShipType()));
        Europe key = Europe.valueOf("repair" + repair.getKind().name());
        if (repair.getKind() == TurnEventShipRepair.Kind.SUNK) {
            String goods = repair.getLostGoods().stream()
                    .map(item -> item.getAmount() + " "
                            + getI18n().get(GoodsTypeName
                                    .getNameForGoodsType(item.getType())))
                    .collect(Collectors.joining(", "));
            return new TeItemSimple(getI18n().get(key, ship, repair.getShipId(),
                    repair.getLostPassengers(), goods.isEmpty() ? "0" : goods));
        }
        String destination = repair.isEuropeDestination()
                ? getI18n().get(Europe.voyageDestinationEurope)
                : repair.getDestinationName();
        return new TeItemSimple(
                getI18n().get(key, ship, repair.getShipId(), destination));
    }
}
