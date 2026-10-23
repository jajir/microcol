package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.microcol.model.store.RoyalMemberPo;
import org.microcol.model.store.RoyalRosterPo;

import com.google.common.base.Preconditions;

/**
 * Prewar regular force owned by one Crown. Members keep their identity for the
 * whole game. Bell growth adds members and never recreates a destroyed one;
 * destroyed members stay in the roster, so ship capacity never falls back. A
 * shipped member is bound to the actual unit that carries it (R-0186); the
 * binding is the only link, so a unit is never counted twice.
 */
public final class RoyalRoster {

    /** Bell base of the Crown growth rule, before the difficulty factor. */
    private static final int GROWTH_BELLS = 500;

    /** Each ship covers this many ground units. */
    private static final int GROUND_PER_SHIP = 6;

    /** Men-of-war of the starting force, before any growth. */
    private static final int START_SHIPS = 2;

    /**
     * Unsuccessful royal turns of a blocked route before another target is
     * tried (R-0186).
     */
    public static final int BLOCKED_TURNS_BEFORE_RETARGET = 3;

    /** Growth order of ground units; also the order of passenger loading. */
    private static final Family[] GROWTH_CYCLE = { Family.INFANTRY,
            Family.CAVALRY, Family.ARTILLERY };

    /** Unit family of a roster member. */
    public enum Family {
        /** Crown regular infantry, the {@link MilitaryRole#ROYAL} infantry. */
        INFANTRY(true),
        /** Crown regular cavalry, the {@link MilitaryRole#ROYAL} cavalry. */
        CAVALRY(true), ARTILLERY(true), MAN_OF_WAR(false);

        private final boolean ground;

        Family(final boolean ground) {
            this.ground = ground;
        }

        /**
         * @return whether the family counts toward the ground-to-ship ratio
         */
        public boolean isGround() {
            return ground;
        }

        /**
         * @return chart base of this family from the shared Crown role
         * @throws IllegalStateException for artillery and men-of-war, which
         *                               have no chart base
         */
        public int getChartBase() {
            Preconditions.checkState(this == INFANTRY || this == CAVALRY,
                    "Only royal infantry and cavalry have chart bases");
            return MilitaryRole.ROYAL.getBase(this == CAVALRY);
        }
    }

    /**
     * Position of a member; only explicit transitions change it. A captured
     * member's unit lives on under another owner, so it is neither shipped nor
     * destroyed, and like a destroyed one it never returns.
     */
    public enum State {
        RESERVE, SHIPPED, DESTROYED, CAPTURED
    }

    /**
     * One roster member with its stable identity. A shipped member names the
     * unit that carries it; a shipped man-of-war may also name its colony
     * target and count its unsuccessful turns.
     */
    public static final class Member {
        private final int id;
        private final Family family;
        private State state;
        private Integer unitId;
        private Integer targetColonyId;
        private int blockedTurns;

        private Member(final int id, final Family family, final State state) {
            this.id = id;
            this.family = family;
            this.state = state;
        }

        public int getId() {
            return id;
        }

        public Family getFamily() {
            return family;
        }

        public State getState() {
            return state;
        }

        /**
         * @return identity of the actual unit bound to this member, empty
         *         unless it is shipped
         */
        public Optional<Integer> getUnitId() {
            return Optional.ofNullable(unitId);
        }

        /**
         * @return colony chosen as the landing target of this man-of-war, empty
         *         when none is chosen
         */
        public Optional<Integer> getTargetColonyId() {
            return Optional.ofNullable(targetColonyId);
        }

        /**
         * @return unsuccessful royal turns since the route last worked
         */
        public int getBlockedTurns() {
            return blockedTurns;
        }
    }

    /** Growth of one update, computed completely before anything changes. */
    private static final class GrowthPlan {
        private final List<Member> added;
        private final int units;
        private final int remainder;

        private GrowthPlan(final List<Member> added, final int units,
                final int remainder) {
            this.added = added;
            this.units = units;
            this.remainder = remainder;
        }
    }

    private final int threshold;

    private final List<Member> members = new ArrayList<>();

    private int growthCount;

    private int remainder;

    /** Family of the next passenger to load, as an index of the cycle. */
    private int dispatchCycle;

    /**
     * Start with the fresh roster of this difficulty: {@code 4+d} royal
     * infantry, {@code 2+d} cavalry, two artillery and two men-of-war, where
     * {@code d} is the difficulty index.
     *
     * @param difficulty required selected difficulty
     * @return strict saved roster before any growth
     */
    public static RoyalRosterPo initial(final Difficulty difficulty) {
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        final int index = difficulty.ordinal();
        final RoyalRosterPo po = new RoyalRosterPo();
        po.setGrowthCount(0);
        po.setRemainder(0);
        final List<RoyalMemberPo> saved = new ArrayList<>();
        addSaved(saved, Family.INFANTRY, 4 + index);
        addSaved(saved, Family.CAVALRY, 2 + index);
        addSaved(saved, Family.ARTILLERY, 2);
        addSaved(saved, Family.MAN_OF_WAR, START_SHIPS);
        po.setMembers(saved);
        return po;
    }

    private static void addSaved(final List<RoyalMemberPo> saved,
            final Family family, final int count) {
        for (int i = 0; i < count; i++) {
            final RoyalMemberPo member = new RoyalMemberPo();
            member.setId(saved.size() + 1);
            member.setFamily(family);
            member.setState(State.RESERVE);
            saved.add(member);
        }
    }

    /**
     * @param difficulty selected difficulty
     * @return produced bells per ground unit, {@code ceil(500 / k)}
     */
    public static int bellThreshold(final Difficulty difficulty) {
        final int factor = RecruitmentPool.factor(difficulty);
        return (GROWTH_BELLS * 100 + factor - 1) / factor;
    }

    RoyalRoster(final RoyalRosterPo po, final Difficulty difficulty) {
        Preconditions.checkArgument(po != null, "Missing Crown roster");
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        Preconditions.checkArgument(
                po.getGrowthCount() != null && po.getGrowthCount() >= 0
                        && po.getRemainder() != null && po.getMembers() != null,
                "Incomplete Crown roster");
        this.threshold = bellThreshold(difficulty);
        Preconditions.checkArgument(
                po.getRemainder() >= 0 && po.getRemainder() < threshold,
                "Invalid Crown bell remainder");
        growthCount = po.getGrowthCount();
        remainder = po.getRemainder();
        // An absent cycle is the start of the cycle: no passenger has sailed.
        dispatchCycle = po.getDispatchCycle() == null ? 0
                : po.getDispatchCycle();
        Preconditions.checkArgument(
                dispatchCycle >= 0 && dispatchCycle < GROWTH_CYCLE.length,
                "Invalid Crown dispatch cycle");
        final List<Integer> ids = new ArrayList<>();
        for (final RoyalMemberPo saved : po.getMembers()) {
            Preconditions.checkArgument(saved != null && saved.getId() != null
                    && saved.getId() > 0 && saved.getFamily() != null
                    && saved.getState() != null && !ids.contains(saved.getId()),
                    "Invalid Crown roster member");
            ids.add(saved.getId());
            final Member member = new Member(saved.getId(), saved.getFamily(),
                    saved.getState());
            member.unitId = saved.getUnitId();
            member.targetColonyId = saved.getTargetColonyId();
            member.blockedTurns = saved.getBlockedTurns() == null ? 0
                    : saved.getBlockedTurns();
            members.add(member);
        }
        checkBindings(members);
        checkComposition(difficulty, growthCount, members);
    }

    /**
     * A shipped member is bound to exactly one unit; every other member has no
     * unit. A transport target and its retry count belong to a shipped
     * man-of-war only.
     */
    private static void checkBindings(final List<Member> members) {
        final Set<Integer> units = new HashSet<>();
        for (final Member member : members) {
            final boolean shipped = member.state == State.SHIPPED;
            Preconditions.checkArgument(
                    shipped ? member.unitId != null && member.unitId > 0
                            && units.add(member.unitId) : member.unitId == null,
                    "Invalid Crown unit binding");
            final boolean transporting = shipped
                    && member.family == Family.MAN_OF_WAR;
            Preconditions.checkArgument(
                    transporting || member.targetColonyId == null
                            && member.blockedTurns == 0,
                    "Crown transport state needs a shipped man-of-war");
            Preconditions.checkArgument(
                    member.targetColonyId == null || member.targetColonyId >= 0,
                    "Invalid Crown transport target");
            Preconditions.checkArgument(member.blockedTurns >= 0
                    && member.blockedTurns < BLOCKED_TURNS_BEFORE_RETARGET
                    && (member.targetColonyId != null
                            || member.blockedTurns == 0),
                    "Invalid Crown transport retry count");
        }
    }

    /**
     * @return saved copy of identities, bindings, transport state, growth
     *         count, remainder and the dispatch cycle
     */
    RoyalRosterPo save() {
        final List<RoyalMemberPo> saved = new ArrayList<>();
        for (final Member member : members) {
            final RoyalMemberPo out = new RoyalMemberPo();
            out.setId(member.id);
            out.setFamily(member.family);
            out.setState(member.state);
            out.setUnitId(member.unitId);
            out.setTargetColonyId(member.targetColonyId);
            out.setBlockedTurns(member.blockedTurns);
            saved.add(out);
        }
        final RoyalRosterPo po = new RoyalRosterPo();
        po.setGrowthCount(growthCount);
        po.setRemainder(remainder);
        po.setDispatchCycle(dispatchCycle);
        po.setMembers(saved);
        return po;
    }

    /**
     * @return read-only snapshot of members in identity order of creation
     */
    public List<Member> getMembers() {
        return List.copyOf(members);
    }

    /**
     * @param family required family
     * @return every member of the family, in any state
     */
    public int getTotal(final Family family) {
        return getCount(family, null);
    }

    /**
     * @param family required family
     * @param state  required state
     * @return members of the family currently in this state
     */
    public int getCount(final Family family, final State state) {
        return Math
                .toIntExact(members.stream()
                        .filter(member -> member.family == family
                                && (state == null || member.state == state))
                        .count());
    }

    /**
     * Living ground members of the Crown, whether in reserve, carried or
     * landed. A captured member lives on under another owner and a destroyed
     * one is gone; neither counts (R-0191). Ships never count.
     *
     * @return number of living ground members
     */
    public int getLivingGroundCount() {
        return Math.toIntExact(members.stream()
                .filter(member -> member.family.isGround()
                        && (member.state == State.RESERVE
                                || member.state == State.SHIPPED))
                .count());
    }

    /**
     * @return produced bells not yet converted into a ground unit
     */
    public int getBellRemainder() {
        return remainder;
    }

    /**
     * @return produced bells needed for the next ground unit
     */
    public int getBellThreshold() {
        return threshold;
    }

    private int groundCount() {
        return Math.toIntExact(members.stream()
                .filter(member -> member.family.isGround()).count());
    }

    private int shipCount() {
        return members.size() - groundCount();
    }

    private static int startingGround(final Difficulty difficulty) {
        return 8 + 2 * difficulty.ordinal();
    }

    /**
     * Saved families must be exactly the starting force, the growth cycle and
     * the ships that growth requires. Shipped and destroyed members still
     * count.
     */
    private static void checkComposition(final Difficulty difficulty,
            final int growth, final List<Member> members) {
        final int index = difficulty.ordinal();
        Preconditions.checkArgument(
                count(members, Family.INFANTRY) == 4L + index
                        + grownCount(growth, 0)
                        && count(members, Family.CAVALRY) == 2L + index
                                + grownCount(growth, 1)
                        && count(members, Family.ARTILLERY) == 2L
                                + grownCount(growth, 2)
                        && count(members, Family.MAN_OF_WAR) == expectedShips(
                                difficulty, growth),
                "Crown families do not match its starting force and growth");
    }

    private static long count(final List<Member> members, final Family family) {
        return members.stream().filter(member -> member.family == family)
                .count();
    }

    /**
     * @return ground units of one cycle position among the first growth units
     */
    private static long grownCount(final int growth, final int cycle) {
        final long units = growth - (long) cycle;
        return units <= 0 ? 0
                : (units + GROWTH_CYCLE.length - 1) / GROWTH_CYCLE.length;
    }

    /**
     * @return ships after growth: the starting two until the first unit, then
     *         enough that ground units never exceed six per ship
     */
    private static long expectedShips(final Difficulty difficulty,
            final int growth) {
        if (growth == 0) {
            return START_SHIPS;
        }
        final long ground = startingGround(difficulty) + (long) growth;
        return Math.max(START_SHIPS,
                (ground + GROUND_PER_SHIP - 1) / GROUND_PER_SHIP);
    }

    /**
     * Check growth without changing the roster.
     *
     * @param produced bells the colonial nation actually produced
     */
    void checkCredit(final int produced) {
        planGrowth(produced);
    }

    /**
     * Convert produced bells into ground units; the remainder carries over.
     * Either the whole update is applied or nothing changes.
     *
     * @param produced bells the colonial nation actually produced
     */
    void credit(final int produced) {
        final GrowthPlan plan = planGrowth(produced);
        members.addAll(plan.added);
        growthCount += plan.units;
        remainder = plan.remainder;
    }

    private GrowthPlan planGrowth(final int produced) {
        Preconditions.checkArgument(produced >= 0,
                "Produced bells must be nonnegative");
        final long total = (long) remainder + produced;
        final long units = total / threshold;
        Preconditions.checkState(units <= Integer.MAX_VALUE - growthCount,
                "Crown growth is exhausted");
        final List<Member> added = new ArrayList<>();
        long id = members.stream().mapToLong(Member::getId).max().orElse(0L)
                + 1;
        long ground = groundCount();
        long ships = shipCount();
        for (long unit = 0; unit < units; unit++) {
            final Family family = GROWTH_CYCLE[(int) ((growthCount + unit)
                    % GROWTH_CYCLE.length)];
            ground++;
            added.add(newMember(id++, family));
            while (ground > GROUND_PER_SHIP * ships) {
                added.add(newMember(id++, Family.MAN_OF_WAR));
                ships++;
            }
        }
        return new GrowthPlan(added, (int) units, (int) (total % threshold));
    }

    private static Member newMember(final long id, final Family family) {
        Preconditions.checkState(id <= Integer.MAX_VALUE,
                "Crown roster identities are exhausted");
        return new Member((int) id, family, State.RESERVE);
    }

    /**
     * Move a reserve member to the actual unit that carries it; a member is
     * consumed once. A ground member advances the passenger cycle.
     *
     * @param id     identity of the member
     * @param unitId identity of the new unit, bound to no other member
     */
    void ship(final int id, final int unitId) {
        final Member member = find(id);
        Preconditions.checkState(member.state == State.RESERVE,
                "Only reserve Crown members can ship");
        Preconditions.checkArgument(unitId > 0, "Invalid Crown unit identity");
        Preconditions.checkArgument(findByUnitId(unitId).isEmpty(),
                "Crown unit is already bound to a member");
        member.state = State.SHIPPED;
        member.unitId = unitId;
        if (member.family.isGround()) {
            dispatchCycle = cycleIndex(member.family) + 1;
            dispatchCycle %= GROWTH_CYCLE.length;
        }
    }

    /**
     * Record a battle loss. A destroyed member never returns to the roster and
     * releases its unit binding and transport state.
     *
     * @param id identity of the member
     */
    void destroy(final int id) {
        final Member member = find(id);
        Preconditions.checkState(
                member.state != State.DESTROYED
                        && member.state != State.CAPTURED,
                "Crown member has already left the force");
        member.state = State.DESTROYED;
        member.unitId = null;
        member.targetColonyId = null;
        member.blockedTurns = 0;
    }

    /**
     * Record the loss of a unit that carries a member. A unit outside the
     * roster, such as a colonist of the Crown, changes nothing; a bound unit is
     * destroyed exactly once.
     *
     * @param unitId identity of the lost unit
     */
    void destroyBoundUnit(final int unitId) {
        findByUnitId(unitId).ifPresent(member -> destroy(member.id));
    }

    /**
     * Record that another owner took the unit of a shipped member. The unit
     * stays in the game, so the member is captured rather than destroyed; it
     * leaves the Crown's force exactly once and releases its binding. A unit
     * outside the roster changes nothing.
     *
     * @param unitId identity of the unit that changed owner
     */
    void captureBoundUnit(final int unitId) {
        findByUnitId(unitId).ifPresent(member -> {
            member.state = State.CAPTURED;
            member.unitId = null;
            member.targetColonyId = null;
            member.blockedTurns = 0;
        });
    }

    /**
     * @return whether any ground member still waits in reserve, so the Crown
     *         still has an army to send even when none stands on the map
     */
    public boolean hasReserveGround() {
        return members.stream().anyMatch(member -> member.family.isGround()
                && member.state == State.RESERVE);
    }

    /**
     * @return whether reserve ground members can still reach the colonies: some
     *         man-of-war is in reserve or afloat. A reserve whose carriers are
     *         all lost is stranded and no longer counts as a force on its way.
     */
    public boolean hasDeliverableReserve() {
        return hasReserveGround() && members.stream()
                .anyMatch(member -> member.family == Family.MAN_OF_WAR
                        && (member.state == State.RESERVE
                                || member.state == State.SHIPPED));
    }

    /**
     * @return whether the roster has ever been put to use: a member is shipped
     *         or lost. A roster that never sailed belongs to a scenario whose
     *         expedition, if any, is authored on the map.
     */
    public boolean hasSailed() {
        return members.stream()
                .anyMatch(member -> member.state != State.RESERVE);
    }

    /**
     * @param id identity of a roster member
     * @return the member with this identity, empty when there is none
     */
    public Optional<Member> findMember(final int id) {
        return members.stream().filter(member -> member.id == id).findFirst();
    }

    /**
     * @param unitId identity of a unit
     * @return the shipped member bound to this unit, if any
     */
    public Optional<Member> findByUnitId(final int unitId) {
        return members.stream().filter(
                member -> member.unitId != null && member.unitId == unitId)
                .findFirst();
    }

    /**
     * Ground members that a man-of-war would load now, in cycle order. The
     * cycle takes the next family with reserve members and each family in turn;
     * the lowest identity of a family is loaded first. Nothing changes.
     *
     * @param capacity   maximal number of passengers, at least one
     * @param shipMember identity of the reserve man-of-war
     * @return passengers to ship in this order; empty when none is reserved
     * @throws IllegalStateException when the member is not a reserve man-of-war
     */
    List<Member> planPassengers(final int capacity, final int shipMember) {
        final Member ship = find(shipMember);
        Preconditions.checkState(
                ship.family == Family.MAN_OF_WAR && ship.state == State.RESERVE,
                "Only a reserve man-of-war can sail");
        return planPassengers(capacity);
    }

    /**
     * Ground members that a carrier would load now, in cycle order; the carrier
     * itself is the caller's concern. Nothing changes.
     *
     * @param capacity maximal number of passengers, at least one
     * @return reserve passengers in loading order, empty when none is reserved
     */
    List<Member> planPassengers(final int capacity) {
        Preconditions.checkArgument(capacity >= 1, "Invalid hold capacity");
        final List<Member> available = members.stream()
                .filter(member -> member.family.isGround()
                        && member.state == State.RESERVE)
                .collect(java.util.stream.Collectors
                        .toCollection(ArrayList::new));
        final List<Member> planned = new ArrayList<>();
        int cycle = dispatchCycle;
        while (planned.size() < capacity) {
            final Optional<Family> family = nextFamily(available, cycle);
            if (family.isEmpty()) {
                break;
            }
            final Member next = available.stream()
                    .filter(member -> member.family == family.get()).findFirst()
                    .orElseThrow();
            available.remove(next);
            planned.add(next);
            cycle = (cycleIndex(family.get()) + 1) % GROWTH_CYCLE.length;
        }
        return planned;
    }

    private static Optional<Family> nextFamily(final List<Member> available,
            final int cycle) {
        for (int step = 0; step < GROWTH_CYCLE.length; step++) {
            final Family family = GROWTH_CYCLE[(cycle + step)
                    % GROWTH_CYCLE.length];
            if (available.stream()
                    .anyMatch(member -> member.family == family)) {
                return Optional.of(family);
            }
        }
        return Optional.empty();
    }

    private static int cycleIndex(final Family family) {
        return java.util.Arrays.asList(GROWTH_CYCLE).indexOf(family);
    }

    /**
     * Save the landing target and retry count of a shipped man-of-war. A target
     * is an identity only, and the retry count stays below the limit.
     *
     * @param unitId       identity of the carrying unit
     * @param colonyId     chosen target colony, or none
     * @param blockedTurns unsuccessful turns of the current route
     */
    void setTransportPlan(final int unitId, final Optional<Integer> colonyId,
            final int blockedTurns) {
        final Member member = findByUnitId(unitId).orElseThrow(
                () -> new IllegalArgumentException("Unknown Crown ship"));
        Preconditions.checkState(member.family == Family.MAN_OF_WAR,
                "Only a Crown man-of-war has a transport plan");
        Preconditions.checkArgument(
                blockedTurns >= 0
                        && blockedTurns < BLOCKED_TURNS_BEFORE_RETARGET
                        && (colonyId.isPresent() || blockedTurns == 0),
                "Invalid Crown transport retry count");
        member.targetColonyId = colonyId.orElse(null);
        member.blockedTurns = blockedTurns;
    }

    private Member find(final int id) {
        return members.stream().filter(member -> member.id == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown Crown roster member"));
    }

}
