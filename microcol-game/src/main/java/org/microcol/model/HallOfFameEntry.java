package org.microcol.model;

import org.microcol.model.store.HallOfFameEntryPo;

import com.google.common.base.Preconditions;

/**
 * One scored result in the persistent top ten (R-0197). It copies the frozen
 * identity and components of a {@link GameResult} together with the difficulty
 * and the displayed name of the player at the moment of the record. The entry
 * is never rebuilt from a later game; loading only checks that the stored total
 * agrees with the stored components.
 */
public final class HallOfFameEntry {

    private final String identity;

    private final String nationName;

    private final String playerDisplayName;

    private final Difficulty difficulty;

    private final BaseScore base;

    private final ResultScore score;

    private final Integer victoryOrder;

    private final Integer declarationYear;

    private HallOfFameEntry(final String identity, final String nationName,
            final String playerDisplayName, final Difficulty difficulty,
            final BaseScore base, final ResultScore score,
            final Integer victoryOrder, final Integer declarationYear) {
        this.identity = identity;
        this.nationName = nationName;
        this.playerDisplayName = playerDisplayName;
        this.difficulty = difficulty;
        this.base = base;
        this.score = score;
        this.victoryOrder = victoryOrder;
        this.declarationYear = declarationYear;
    }

    /**
     * Copy a frozen result at the moment it is recorded.
     *
     * @param result     required frozen result of the human colonial nation
     * @param owner      required nation that owns the result
     * @param difficulty required difficulty of the game
     * @return immutable ranking entry
     */
    public static HallOfFameEntry of(final GameResult result,
            final Player owner, final Difficulty difficulty) {
        Preconditions.checkNotNull(result, "Result is required");
        Preconditions.checkNotNull(owner, "Owner is required");
        Preconditions.checkNotNull(difficulty, "Difficulty is required");
        Preconditions.checkArgument(
                owner.getName().equals(result.getPlayerName()),
                "Result belongs to another nation");
        return new HallOfFameEntry(result.getIdentity(), result.getPlayerName(),
                owner.getDisplayName(), difficulty, result.getBaseScore(),
                result.getScore(), result.getVictoryOrder().orElse(null),
                result.getDeclarationYear().orElse(null));
    }

    /**
     * Load a stored entry. Every component is required and the calculated total
     * must match the stored one.
     *
     * @param po required stored entry
     * @return immutable ranking entry
     * @throws IllegalArgumentException when the stored entry is incomplete or
     *                                  inconsistent
     */
    public static HallOfFameEntry load(final HallOfFameEntryPo po) {
        Preconditions.checkNotNull(po, "Saved entry is required");
        Preconditions.checkArgument(
                po.getIdentity() != null && !po.getIdentity().isBlank(),
                "Hall of Fame entry needs its identity");
        Preconditions.checkArgument(
                po.getPlayerName() != null && po.getPlayerDisplayName() != null,
                "Hall of Fame entry needs its nation and name");
        Preconditions.checkArgument(po.getServants() != null
                && po.getColonists() != null && po.getExperts() != null
                && po.getFathers() != null && po.getGold() != null
                && po.getSupportPercent() != null
                && po.getDestroyedSettlements() != null
                && po.getBellsBeyondTarget() != null && po.getTotal() != null,
                "Hall of Fame entry misses a component");
        final Difficulty difficulty = parseDifficulty(po.getDifficulty());
        final BaseScore base = BaseScore.of(po.getServants(), po.getColonists(),
                po.getExperts(), po.getFathers(), po.getGold(),
                po.getSupportPercent());
        final ResultScore score = ResultScore.of(base,
                po.getDestroyedSettlements(), difficulty,
                po.getBellsBeyondTarget(), po.getVictoryOrder(),
                po.getDeclarationYear());
        Preconditions.checkArgument(score.getTotal() == po.getTotal(),
                "Hall of Fame total does not match its components");
        return new HallOfFameEntry(po.getIdentity(), po.getPlayerName(),
                po.getPlayerDisplayName(), difficulty, base, score,
                po.getVictoryOrder(), po.getDeclarationYear());
    }

    private static Difficulty parseDifficulty(final String name) {
        Preconditions.checkArgument(name != null,
                "Hall of Fame entry needs its difficulty");
        try {
            return Difficulty.valueOf(name);
        } catch (final IllegalArgumentException unknown) {
            throw new IllegalArgumentException(
                    "Unknown Hall of Fame difficulty: " + name, unknown);
        }
    }

    /**
     * @return the identity of the recorded result
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @return the internal name of the nation, its identity and not its label
     */
    public String getNationName() {
        return nationName;
    }

    /**
     * @return the name the player had when the result was recorded
     */
    public String getPlayerDisplayName() {
        return playerDisplayName;
    }

    /**
     * @return the difficulty the game was played with
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * @return the recorded base score
     */
    public BaseScore getBaseScore() {
        return base;
    }

    /**
     * @return the recorded calculated result
     */
    public ResultScore getScore() {
        return score;
    }

    /**
     * @return the final score that places the entry in the ranking
     */
    public long getTotal() {
        return score.getTotal();
    }

    /**
     * @return the stored form of this entry
     */
    public HallOfFameEntryPo save() {
        final HallOfFameEntryPo out = new HallOfFameEntryPo();
        out.setIdentity(identity);
        out.setPlayerName(nationName);
        out.setPlayerDisplayName(playerDisplayName);
        out.setDifficulty(difficulty.name());
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
