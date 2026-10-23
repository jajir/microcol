package org.microcol.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.microcol.model.store.GameResultPo;

import com.google.common.base.Preconditions;

/**
 * Frozen result of the human colonial nation in scored free play (R-0193). It
 * is taken once: at spring 1800 for a nation without a declared revolution, at
 * the end of a declared rebellion, or at an ordinary colony loss. Its identity
 * and its components never change afterwards; later unscored play, losses and
 * reloads cannot replace it. Only the status moves on continuation.
 */
public final class GameResult {

    /** Where the frozen result stands in the free-play calendar. */
    public enum Status {

        /** Spring 1800 has come; play waits for the unscored choice. */
        AWAITING_CONTINUATION,

        /** The player continued without scoring; the score stays as frozen. */
        CONTINUED_UNSCORED,

        /**
         * The game ended with this result: a declared rebellion's victory or
         * loss, the ordinary colony loss of an undeclared nation, or a
         * confirmed retirement that froze its score (R-0194).
         */
        FINAL
    }

    private final String identity;

    private final String playerName;

    private final BaseScore base;

    private final ResultScore score;

    private final Integer victoryOrder;

    private final Integer declarationYear;

    private volatile Status status;

    private GameResult(final String identity, final String playerName,
            final Status status, final BaseScore base, final ResultScore score,
            final Integer victoryOrder, final Integer declarationYear) {
        this.identity = identity;
        this.playerName = playerName;
        this.status = status;
        this.base = base;
        this.score = score;
        this.victoryOrder = victoryOrder;
        this.declarationYear = declarationYear;
    }

    /**
     * Freeze the score of the human colonial nation at its current state. The
     * identity is new and is never reused by another result.
     *
     * @param owner  required human colonial nation
     * @param status required starting status, awaiting or final
     * @return immutable frozen result
     */
    static GameResult freeze(final Player owner, final Status status) {
        Preconditions.checkNotNull(owner, "Owner is required");
        Preconditions.checkNotNull(status, "Status is required");
        Preconditions.checkArgument(owner.isHuman() && owner.isColonialNation(),
                "Only the human colonial nation has a result");
        Preconditions.checkArgument(status != Status.CONTINUED_UNSCORED,
                "A result starts awaiting continuation or final");
        return new GameResult(UUID.randomUUID().toString(), owner.getName(),
                status, owner.getBaseScore(), owner.getResultScore(),
                owner.getRebellionVictoryOrder().orElse(null),
                owner.getDeclarationYear().orElse(null));
    }

    /**
     * Load a saved result. Every component is required, the calculated total
     * must match, and the status must agree with the nation's live rebellion.
     *
     * @param po         required saved result
     * @param owner      required human colonial nation named by the result
     * @param difficulty required difficulty of the game
     * @return immutable frozen result
     * @throws IllegalArgumentException when the saved result is incomplete or
     *                                  inconsistent
     */
    static GameResult load(final GameResultPo po, final Player owner,
            final Difficulty difficulty) {
        Preconditions.checkNotNull(po, "Saved result is required");
        Preconditions.checkArgument(
                po.getIdentity() != null && !po.getIdentity().isBlank(),
                "Frozen result needs its identity");
        Preconditions.checkArgument(
                owner.isHuman() && owner.isColonialNation()
                        && owner.getName().equals(po.getPlayerName()),
                "Frozen result belongs to another nation");
        Preconditions.checkArgument(po.getServants() != null
                && po.getColonists() != null && po.getExperts() != null
                && po.getFathers() != null && po.getGold() != null
                && po.getSupportPercent() != null
                && po.getDestroyedSettlements() != null
                && po.getBellsBeyondTarget() != null && po.getTotal() != null,
                "Frozen result misses a component");
        final Status savedStatus = parseStatus(po.getStatus());
        final BaseScore savedBase = BaseScore.of(po.getServants(),
                po.getColonists(), po.getExperts(), po.getFathers(),
                po.getGold(), po.getSupportPercent());
        final ResultScore savedScore = ResultScore.of(savedBase,
                po.getDestroyedSettlements(), difficulty,
                po.getBellsBeyondTarget(), po.getVictoryOrder(),
                po.getDeclarationYear());
        Preconditions.checkArgument(savedScore.getTotal() == po.getTotal(),
                "Frozen result total does not match its components");
        checkStatus(savedStatus, owner, po);
        return new GameResult(po.getIdentity(), owner.getName(), savedStatus,
                savedBase, savedScore, po.getVictoryOrder(),
                po.getDeclarationYear());
    }

    private static Status parseStatus(final String name) {
        Preconditions.checkArgument(name != null,
                "Frozen result needs its status");
        try {
            return Status.valueOf(name);
        } catch (final IllegalArgumentException unknown) {
            throw new IllegalArgumentException(
                    "Unknown frozen result status: " + name, unknown);
        }
    }

    private static void checkStatus(final Status status, final Player owner,
            final GameResultPo po) {
        if (owner.isDeclaredIndependence()) {
            Preconditions.checkArgument(status == Status.FINAL,
                    "A declared nation keeps only the final result of its"
                            + " rebellion");
            // An open rebellion keeps a final result only when it was retired
            // (R-0194); such a result never carries an independence bonus.
            Preconditions.checkArgument(
                    owner.isRebellionDecided() || po.getVictoryOrder() == null,
                    "A retired rebellion carries no independence bonus");
            Preconditions
                    .checkArgument(
                            Objects.equals(po.getVictoryOrder(),
                                    owner.getRebellionVictoryOrder()
                                            .orElse(null))
                                    && Objects.equals(po.getDeclarationYear(),
                                            owner.getDeclarationYear()
                                                    .orElse(null)),
                            "Final result disagrees with the saved rebellion");
        } else {
            Preconditions.checkArgument(
                    !owner.isDeclaredIndependence()
                            && po.getVictoryOrder() == null
                            && po.getDeclarationYear() == null
                            && po.getBellsBeyondTarget() == 0L,
                    "An unscored or colony-loss result needs an undeclared nation");
        }
    }

    /**
     * @return the identity of this result, unique for the game and never reused
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @return the internal name of the nation that owns this result
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return where this frozen result stands
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return the frozen base score (R-0195) with every named component
     */
    public BaseScore getBaseScore() {
        return base;
    }

    /**
     * @return the frozen calculated result (R-0196) with every named component
     */
    public ResultScore getScore() {
        return score;
    }

    /**
     * @return the completion order of the won rebellion, empty otherwise
     */
    public Optional<Integer> getVictoryOrder() {
        return Optional.ofNullable(victoryOrder);
    }

    /**
     * @return the declaration year of a declared rebellion, empty otherwise
     */
    public Optional<Integer> getDeclarationYear() {
        return Optional.ofNullable(declarationYear);
    }

    boolean isAwaitingContinuation() {
        return status == Status.AWAITING_CONTINUATION;
    }

    /** Record the unscored continuation; the frozen score is not touched. */
    void continueUnscored() {
        Preconditions.checkState(isAwaitingContinuation(),
                "Only a result awaiting continuation can continue unscored");
        status = Status.CONTINUED_UNSCORED;
    }

    GameResultPo save() {
        final GameResultPo out = new GameResultPo();
        out.setIdentity(identity);
        out.setStatus(status.name());
        out.setPlayerName(playerName);
        out.setServants(base.getServants());
        out.setColonists(base.getColonists());
        out.setExperts(base.getExperts());
        out.setFathers(base.getFathers());
        out.setGold(base.getGold());
        out.setSupportPercent(base.getSupportPercent());
        out.setDestroyedSettlements(score.getDestroyedSettlements());
        out.setBellsBeyondTarget(score.getBellsAfterTarget());
        out.setVictoryOrder(victoryOrder);
        out.setDeclarationYear(declarationYear);
        out.setTotal(score.getTotal());
        return out;
    }

}
