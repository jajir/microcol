package org.microcol.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.microcol.model.store.CongressPo;

import com.google.common.base.Preconditions;

/** Persistent national bell account, membership, and frozen founder offer. */
public final class Congress {
    private final Random random = new Random();
    private final List<Founder> members;
    private final List<Founder> candidates;
    private boolean activated;
    private int bells;
    private Founder selected;
    private int generation;
    private boolean jonesDeliveryPending;

    Congress(final CongressPo po, final Difficulty difficulty) {
        Preconditions.checkArgument(po != null, "Missing colonial Congress");
        Preconditions.checkArgument(
                po.getActivated() != null && po.getBells() != null
                        && po.getBells() >= 0 && po.getMembers() != null
                        && po.getCandidates() != null
                        && po.getGeneration() != null && po.getGeneration() >= 0
                        && po.getJonesDeliveryPending() != null,
                "Incomplete Congress state");
        activated = po.getActivated();
        bells = po.getBells();
        generation = po.getGeneration();
        jonesDeliveryPending = po.getJonesDeliveryPending();
        selected = po.getSelected();
        members = new ArrayList<>(po.getMembers());
        candidates = new ArrayList<>(po.getCandidates());
        final Set<Founder> uniqueMembers = new HashSet<>(members);
        final Set<Founder.Category> categories = EnumSet
                .noneOf(Founder.Category.class);
        Preconditions.checkArgument(
                !members.contains(null)
                        && uniqueMembers.size() == members.size()
                        && members.size() <= Founder.values().length,
                "Invalid Congress membership");
        for (final Founder candidate : candidates) {
            Preconditions.checkArgument(
                    candidate != null && !uniqueMembers.contains(candidate)
                            && categories.add(candidate.getCategory()),
                    "Invalid Congress candidate");
        }
        Preconditions.checkArgument(
                selected == null || candidates.contains(selected),
                "Selected founder is not offered");
        Preconditions.checkArgument(
                !jonesDeliveryPending
                        || members.contains(Founder.JOHN_PAUL_JONES),
                "Jones delivery has no member");
        Preconditions.checkArgument(
                activated
                        ? generation > 0
                                && (members.size() == Founder.values().length
                                        || generation < Integer.MAX_VALUE)
                                && candidates.size() == remainingCategories()
                        : generation == 0 && bells == 0 && members.isEmpty()
                                && candidates.isEmpty() && selected == null
                                && !jonesDeliveryPending,
                "Invalid Congress activation or category offer");
        if (activated && !members.isEmpty() && members.size() < 25) {
            target(difficulty);
        }
    }

    /**
     * Create the strict saved state for a nation before its first colony.
     *
     * @return inactive Congress with no bells or offers
     */
    public static CongressPo initial() {
        final CongressPo po = new CongressPo();
        po.setActivated(false);
        po.setBells(0);
        po.setMembers(List.of());
        po.setCandidates(List.of());
        po.setSelected(null);
        po.setGeneration(0);
        po.setJonesDeliveryPending(false);
        return po;
    }

    CongressPo save() {
        final CongressPo po = new CongressPo();
        po.setActivated(activated);
        po.setBells(bells);
        po.setMembers(List.copyOf(members));
        po.setCandidates(List.copyOf(candidates));
        po.setSelected(selected);
        po.setGeneration(generation);
        po.setJonesDeliveryPending(jonesDeliveryPending);
        return po;
    }

    boolean isActivated() {
        return activated;
    }

    void activate() {
        if (!activated) {
            activated = true;
            generation = 1;
            redraw();
        }
    }

    private int remainingCategories() {
        int count = 0;
        for (final Founder.Category category : Founder.Category.values()) {
            if (List.of(Founder.values()).stream()
                    .anyMatch(founder -> founder.getCategory() == category
                            && !members.contains(founder))) {
                count++;
            }
        }
        return count;
    }

    private void redraw() {
        candidates.clear();
        for (final Founder.Category category : Founder.Category.values()) {
            final List<Founder> eligible = new ArrayList<>();
            for (final Founder founder : Founder.values()) {
                if (founder.getCategory() == category
                        && !members.contains(founder)) {
                    eligible.add(founder);
                }
            }
            if (!eligible.isEmpty()) {
                candidates.add(eligible.get(random.nextInt(eligible.size())));
            }
        }
    }

    List<FounderOffer> offers(final Player owner) {
        final List<FounderOffer> out = new ArrayList<>();
        if (activated && selected == null) {
            candidates.forEach(founder -> out
                    .add(new FounderOffer(owner, founder, generation)));
        }
        return List.copyOf(out);
    }

    List<Founder> members() {
        return List.copyOf(members);
    }

    Optional<Founder> selected() {
        return Optional.ofNullable(selected);
    }

    boolean canChoose(final Player owner, final FounderOffer offer) {
        return activated && selected == null && offer != null
                && offer.belongsTo(owner, generation)
                && candidates.contains(offer.getFounder());
    }

    void choose(final Player owner, final FounderOffer offer) {
        Preconditions.checkState(canChoose(owner, offer),
                "Founder offer is stale or unavailable");
        selected = offer.getFounder();
    }

    int bells() {
        return bells;
    }

    int target(final Difficulty difficulty) {
        if (!activated || members.size() == Founder.values().length) {
            return 0;
        }
        final int factor = RecruitmentPool.factor(difficulty);
        return Math.toIntExact(Math.addExact(
                Math.multiplyExact(
                        Math.multiplyExact(100L, members.size() + 1L), factor),
                99L) / 100L);
    }

    void checkCredit(final int produced) {
        Preconditions.checkArgument(produced >= 0,
                "Produced bells must be nonnegative");
        Math.addExact(bells, produced);
    }

    void credit(final int produced) {
        checkCredit(produced);
        if (activated) {
            bells += produced;
        }
    }

    boolean canJoin(final Difficulty difficulty) {
        return selected != null && bells >= target(difficulty);
    }

    boolean willJoinAfter(final int produced, final Difficulty difficulty) {
        checkCredit(produced);
        return selected != null
                && (long) bells + produced >= target(difficulty);
    }

    Founder join(final Difficulty difficulty) {
        Preconditions.checkState(canJoin(difficulty),
                "Founder objective has not been reached");
        Preconditions.checkState(generation < Integer.MAX_VALUE,
                "Congress generation exhausted");
        if (members.size() < Founder.values().length - 1) {
            Math.toIntExact(Math.addExact(Math.multiplyExact(
                    Math.multiplyExact(100L, members.size() + 2L),
                    RecruitmentPool.factor(difficulty)), 99L) / 100L);
        }
        final int cost = target(difficulty);
        final Founder joined = selected;
        bells -= cost;
        members.add(joined);
        selected = null;
        if (joined == Founder.JOHN_PAUL_JONES) {
            jonesDeliveryPending = true;
        }
        generation++;
        redraw();
        return joined;
    }

    boolean isJonesDeliveryPending() {
        return jonesDeliveryPending;
    }

    void markJonesDelivered() {
        Preconditions.checkState(jonesDeliveryPending);
        jonesDeliveryPending = false;
    }
}
