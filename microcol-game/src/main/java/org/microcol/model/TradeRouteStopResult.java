package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Outcome of processing one route stop. A refused result changed nothing and
 * gives the reason. A processed result lists every configured entry in
 * processing order: unload entries first, then load entries, each in the
 * player's priority order. A finite no-op is processed with explicit reasons.
 */
public final class TradeRouteStopResult {

    private final TradeRouteStopProblem problem;
    private final List<TradeRouteTransfer> transfers;

    private TradeRouteStopResult(final TradeRouteStopProblem problem,
            final List<TradeRouteTransfer> transfers) {
        this.problem = problem;
        this.transfers = ImmutableList.copyOf(transfers);
    }

    static TradeRouteStopResult refused(final TradeRouteStopProblem problem) {
        return new TradeRouteStopResult(
                Preconditions.checkNotNull(problem, "Problem is null"),
                ImmutableList.of());
    }

    static TradeRouteStopResult processed(
            final List<TradeRouteTransfer> transfers) {
        return new TradeRouteStopResult(null, transfers);
    }

    /**
     * @return why nothing was processed, or empty when the stop was processed
     */
    public Optional<TradeRouteStopProblem> getProblem() {
        return Optional.ofNullable(problem);
    }

    /** @return one entry per configured goods, in processing order */
    public List<TradeRouteTransfer> getTransfers() {
        return transfers;
    }

    /** @return goods actually unloaded, in processing order, without zeros */
    public List<Goods> getUnloaded() {
        return moved(true);
    }

    /** @return goods actually loaded, in processing order, without zeros */
    public List<Goods> getLoaded() {
        return moved(false);
    }

    private List<Goods> moved(final boolean unload) {
        return transfers.stream().filter(
                entry -> entry.isUnload() == unload && entry.getAmount() > 0)
                .map(entry -> Goods.of(entry.getGoodsType(), entry.getAmount()))
                .collect(Collectors.toList());
    }
}
