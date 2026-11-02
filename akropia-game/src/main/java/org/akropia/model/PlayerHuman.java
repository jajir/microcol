package org.akropia.model;

import java.util.Map;
import java.util.Set;
import org.akropia.model.store.RecruitmentPo;
import org.akropia.model.store.CongressPo;
import org.akropia.model.store.TaxPo;

/**
 * Human player class.
 */
public class PlayerHuman extends Player {

    protected PlayerHuman(final String name, final boolean computer,
            final int initialGold, final Model model,
            final boolean declaredIndependence,
            final Map<String, Object> extraData, Set<Location> visible) {
        this(name, computer, initialGold, model, declaredIndependence,
                extraData, visible, null);
    }

    protected PlayerHuman(final String name, final boolean computer,
            final int initialGold, final Model model,
            final boolean declaredIndependence,
            final Map<String, Object> extraData, Set<Location> visible,
            final RecruitmentPo recruitment) {
        this(name, computer, initialGold, model, declaredIndependence,
                extraData, visible, recruitment, Congress.initial());
    }

    protected PlayerHuman(final String name, final boolean computer,
            final int initialGold, final Model model,
            final boolean declaredIndependence,
            final Map<String, Object> extraData, Set<Location> visible,
            final RecruitmentPo recruitment, final CongressPo congress) {
        this(name, computer, initialGold, model, declaredIndependence,
                extraData, visible, recruitment, congress, TaxState.initial());
    }

    protected PlayerHuman(final String name, final boolean computer,
            final int initialGold, final Model model,
            final boolean declaredIndependence,
            final Map<String, Object> extraData, Set<Location> visible,
            final RecruitmentPo recruitment, final CongressPo congress,
            final TaxPo tax) {
        super(name, computer, initialGold, model, declaredIndependence,
                extraData, visible, recruitment, congress, tax);
    }
}
