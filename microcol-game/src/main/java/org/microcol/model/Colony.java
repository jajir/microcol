package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.EducationCoursePo;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitLaborer;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Colony {

    private final int id;

    /**
     * When this level is reached than new colonist appears.
     */
    private final static int FOOD_LEVEL_TO_FREE_COLONIST = 200;

    /**
     * Colony unique name.
     */
    private String name;

    private Player owner;

    /**
     * Largest liberty a save may hold; keeps values exactly representable in
     * JSON consumers.
     */
    private static final long MAX_LIBERTY = 9_007_199_254_740_991L;

    /**
     * Accumulated colony liberty, between zero and
     * {@link RebelSupport#POINTS_PER_RESIDENT} times the resident count after
     * every completed update.
     */
    private long liberty;

    /**
     * Last non-Crown nation this colony belonged to. It owns the colony's
     * national contribution and Bolivar bonus, also while the Crown occupies
     * it.
     */
    private Player lastColonialAffiliation;

    private final Location location;

    private final List<ColonyField> colonyFields;

    private final List<Construction> constructions;

    private final ColonyWarehouse warehouse;

    private final ColonyBuildingQueue colonyBuildingQueue;

    private final Set<GoodsType> exportGoods = new HashSet<>();

    private final List<EducationCourse> educationCourses = new ArrayList<>();

    private final Model model;

    private final Random random = new Random();

    public Colony(final Model model, final int id, final String name,
            final Player owner, final Location location,
            final Function<Colony, List<Construction>> constructionsBuilder,
            final Map<String, Integer> initialGoods,
            final List<ColonyBuildingItemProgress<?>> buildingQueue,
            final long liberty, final Player lastColonialAffiliation) {
        this.model = Preconditions.checkNotNull(model);
        Preconditions.checkArgument(id >= 0, "Invalid colony id");
        this.id = id;
        this.name = Preconditions.checkNotNull(name);
        this.owner = Preconditions.checkNotNull(owner, "owner is null");
        Preconditions.checkArgument(liberty >= 0 && liberty <= MAX_LIBERTY,
                "Invalid colony liberty");
        this.liberty = liberty;
        Preconditions.checkArgument(
                lastColonialAffiliation != null
                        && !lastColonialAffiliation.isKing(),
                "Colony needs a registered non-Crown colonial affiliation");
        Preconditions.checkArgument(
                owner.isKing() || owner == lastColonialAffiliation,
                "Colonial owner must be the colony's affiliation");
        this.lastColonialAffiliation = lastColonialAffiliation;
        this.location = Preconditions.checkNotNull(location);
        colonyFields = new ArrayList<>();
        Direction.getAll().forEach(
                loc -> colonyFields.add(new ColonyField(model, loc, this)));
        this.constructions = Preconditions
                .checkNotNull(constructionsBuilder.apply(this));
        warehouse = new ColonyWarehouse(this, initialGoods);
        colonyBuildingQueue = new ColonyBuildingQueue(model, this,
                buildingQueue);
        checkConstructions();
    }

    /**
     * Verify that constructions are consistent. It verify:
     * <ul>
     * <li>That all construction types are at list just one.</li>
     * <li>That each good is produces by just one construction.</li>
     * <li>That no building and it's upgrade are at constructions list.</li>
     * </ul>
     */
    private void checkConstructions() {
        Preconditions.checkState(colonyFields.size() == 8, String.format(
                "Incorrect colony filed number '%s'", colonyFields.size()));
        Map<ConstructionType, Long> l1 = constructions.stream()
                .collect(Collectors.groupingBy(Construction::getType,
                        Collectors.counting()));
        l1.forEach((constructionType, count) -> {
            if (count > 1) {
                throw new IllegalStateException(
                        String.format("Construction type '%s' is duplicated",
                                constructionType.name()));
            }
        });

        final Map<GoodsType, Long> l2 = constructions.stream()
                .filter(construction -> construction.getType()
                        .getProductionPerTurn().isPresent())
                .collect(Collectors.groupingBy(
                        construction -> construction.getType()
                                .getProductionPerTurn().get().getType(),
                        Collectors.counting()));
        l2.forEach((goodsType, count) -> {
            if (count != 1) {
                throw new IllegalStateException(String.format(
                        "Good type type '%s' is prodecen in more than one building",
                        goodsType.name()));
            }
        });

        constructions.forEach(constructio -> {
            if (constructio.getType().getUpgradeTo().isPresent()) {
                final ConstructionType upgradeTo = constructio.getType()
                        .getUpgradeTo().get();
                constructions.stream()
                        .filter(cc -> cc.getType().equals(upgradeTo)).findAny()
                        .ifPresent(cc -> {
                            throw new IllegalStateException(String.format(
                                    "There is building type '%s' and it's upgrade '%s'",
                                    constructio.getType(), cc));
                        });
            }
        });
    }

    ColonyPo save() {
        ColonyPo out = new ColonyPo();
        out.setId(id);
        out.setName(name);
        out.setOwnerName(owner.getName());
        out.setLiberty(liberty);
        out.setLastColonialAffiliation(lastColonialAffiliation.getName());
        out.setLocation(location);
        out.setColonyFields(saveColonyFields());
        out.setColonyWarehouse(warehouse.save());
        out.setConstructions(saveCostructions());
        out.setBuildingQueue(colonyBuildingQueue.save());
        out.setExportGoods(GoodsType.GOOD_TYPES.stream()
                .filter(exportGoods::contains).collect(Collectors.toList()));
        out.setEducationCourses(educationCourses.stream()
                .map(EducationCourse::save).collect(Collectors.toList()));
        return out;
    }

    /** @return stored colony liberty; read-only, never clamped here */
    public long getLiberty() {
        return liberty;
    }

    /**
     * @return the last non-Crown nation of this colony, also under occupation
     */
    public Player getLastColonialAffiliation() {
        return lastColonialAffiliation;
    }

    /**
     * @return number of residents working in fields or buildings; outside
     *         garrisons, passengers and Europe are excluded
     */
    public int getPopulation() {
        return getUnitsInsideColony().size();
    }

    /**
     * Exact local support: {@code min(liberty + bonus, 200 * N)} out of
     * {@code 200 * N}, where the bonus is 40 per resident when the affiliated
     * nation has Simon Bolivar. Read-only; no bells are created.
     */
    public RebelSupport getRebelSupport() {
        return getRebelSupport(getPopulation());
    }

    private RebelSupport getRebelSupport(final long population) {
        final long capacity = Math.multiplyExact(population,
                (long) RebelSupport.POINTS_PER_RESIDENT);
        final long bonus = lastColonialAffiliation.getCongressMembers()
                .contains(Founder.SIMON_BOLIVAR)
                        ? Math.multiplyExact(population,
                                (long) RebelSupport.BOLIVAR_POINTS_PER_RESIDENT)
                        : 0;
        return new RebelSupport(
                Math.min(Math.addExact(liberty, bonus), capacity), capacity);
    }

    /**
     * Fresh political production change from the current support and residents.
     * Read-only; used for previews and for the snapshot taken at update start.
     *
     * @return support bonus and Tory penalty of this colony right now
     */
    public PoliticalProduction getPoliticalProduction() {
        return PoliticalProduction.of(getRebelSupport(), model.getDifficulty());
    }

    /**
     * Political production change that applies to the given worker's job. A
     * resident keeps the current resident count; an outside worker would
     * increase it by one, which changes the exact support fraction.
     *
     * @param worker resident or prospective worker
     * @return fresh read-only political production change
     */
    PoliticalProduction getPoliticalProductionFor(final Unit worker) {
        if (isResident(worker)) {
            return getPoliticalProduction();
        }
        return PoliticalProduction.of(getRebelSupport(getPopulation() + 1L),
                model.getDifficulty());
    }

    /** Apply the population cap once a resident exit has completed. */
    private void capLibertyToPopulation() {
        liberty = Math.min(liberty, Math.multiplyExact((long) getPopulation(),
                (long) RebelSupport.POINTS_PER_RESIDENT));
    }

    /**
     * Reject a loaded colony whose liberty exceeds its population capacity.
     * Called once all residents have been restored.
     */
    void checkRestoredLiberty() {
        Preconditions.checkArgument(
                liberty <= Math.multiplyExact((long) getPopulation(),
                        (long) RebelSupport.POINTS_PER_RESIDENT),
                "Unsupported save: colony liberty exceeds its population");
    }

    /**
     * Add produced bells to local liberty, or remove them under Crown
     * occupation; liberty never leaves zero to 200 per resident.
     */
    private void applyProducedBells(final int producedBells) {
        if (owner.isKing()) {
            liberty = Math.max(0, liberty - producedBells);
        } else {
            liberty = Math.min(Math.addExact(liberty, producedBells),
                    Math.multiplyExact((long) getPopulation(),
                            (long) RebelSupport.POINTS_PER_RESIDENT));
        }
    }

    /**
     * @param points requested liberty
     * @return the liberty that fits below 200 per resident right now
     */
    long previewProtestLiberty(final long points) {
        final long capacity = Math.multiplyExact((long) getPopulation(),
                (long) RebelSupport.POINTS_PER_RESIDENT);
        return Math.max(0, Math.min(points, capacity - liberty));
    }

    /** Add protest liberty to this colony, capped by its residents. */
    void addProtestLiberty(final long points) {
        liberty = Math.addExact(liberty, previewProtestLiberty(points));
    }

    /** @return immutable education progress with current pause state */
    public List<EducationCourse> getEducationCourses() {
        return educationCourses.stream()
                .map(course -> new EducationCourse(course.getTeacherId(),
                        course.getStudentId(), course.getSkill(),
                        course.getCompletedUpdates(),
                        course.getRequiredUpdates(), !isTeaching(course)))
                .collect(ImmutableList.toImmutableList());
    }

    /** @return skills this registered resident is qualified to teach */
    public List<UnitType> getTeachingSkills(final Unit teacher) {
        if (!isResident(teacher) || !(teacher instanceof UnitFreeColonist)) {
            return List.of();
        }
        final List<UnitType> out = new ArrayList<>();
        if (EducationSkills.tier(teacher.getType()) > 0) {
            out.add(teacher.getType());
        }
        if (((UnitFreeColonist) teacher).isVeteran()
                && !out.contains(UnitType.VETERAN_SOLDIER)) {
            out.add(UnitType.VETERAN_SOLDIER);
        }
        return List.copyOf(out);
    }

    /** @return current, read-only choices including the next social step */
    public List<EducationOption> getEducationOptions(final Unit teacher,
            final Unit student) {
        if (!isValid() || !model.isGameRunning()
                || model.getCurrentPlayer() != owner || teacher == student
                || !isResident(teacher) || !isResident(student)
                || !(student instanceof UnitFreeColonist)
                || educationCourses.stream()
                        .anyMatch(course -> Integer.valueOf(teacher.getId())
                                .equals(course.getTeacherId())
                                && course.getStudentId() != student.getId()
                                || course.getStudentId() == teacher.getId()
                                || Integer.valueOf(student.getId())
                                        .equals(course.getTeacherId()))) {
            return List.of();
        }
        final UnitFreeColonist pupil = (UnitFreeColonist) student;
        final List<EducationOption> out = new ArrayList<>();
        for (final UnitType skill : getTeachingSkills(teacher)) {
            if (pupil.isVeteran() && skill == UnitType.VETERAN_SOLDIER) {
                continue;
            }
            if (student.getType() != UnitType.PETTY_CRIMINAL
                    && student.getType() != UnitType.INDENTURED_SERVANT
                    && student.getType() != UnitType.COLONIST
                    && skill != UnitType.VETERAN_SOLDIER) {
                continue;
            }
            final ConstructionSlot current = teacher.isAtPlaceConstructionSlot()
                    ? teacher.getPlaceConstruction().getConstructionSlot()
                    : null;
            final boolean currentSchool = current != null
                    && current.getColony() == this
                    && EducationSkills.tier(
                            current.getConstructionType()) >= EducationSkills
                                    .tier(skill);
            final boolean available = currentSchool || constructions.stream()
                    .filter(con -> EducationSkills
                            .tier(con.getType()) >= EducationSkills.tier(skill))
                    .flatMap(con -> con.getConstructionSlots().stream())
                    .anyMatch(slot -> slot.canAccept(teacher));
            if (available) {
                final UnitType next = student
                        .getType() == UnitType.PETTY_CRIMINAL
                                ? UnitType.INDENTURED_SERVANT
                                : student
                                        .getType() == UnitType.INDENTURED_SERVANT
                                                ? UnitType.COLONIST
                                                : skill == UnitType.VETERAN_SOLDIER
                                                        ? student.getType()
                                                        : skill;
                out.add(new EducationOption(skill, EducationSkills.tier(skill),
                        EducationSkills.requiredUpdates(skill,
                                student.getType()),
                        next, skill == UnitType.VETERAN_SOLDIER
                                && next == student.getType()));
            }
        }
        return List.copyOf(out);
    }

    /**
     * @param unit a colonist working in, or standing at, this colony
     * @return the reason its civilian specialty cannot be cleared now, or empty
     *         when {@link #clearSpecialty(Unit)} would succeed
     */
    public Optional<ClearSpecialtyProblem> getClearSpecialtyProblem(
            final Unit unit) {
        if (unit == null || !canWorkerActHere(unit)) {
            return Optional.of(ClearSpecialtyProblem.UNAVAILABLE);
        }
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        if (!colonist.hasCivilianSpecialty()) {
            return Optional.of(ClearSpecialtyProblem.NO_CIVILIAN_SPECIALTY);
        }
        // Only separate military rank can still qualify as a teaching skill.
        final List<UnitType> remaining = colonist.isVeteran()
                ? List.of(UnitType.VETERAN_SOLDIER)
                : List.of();
        if (educationCourses.stream()
                .anyMatch(course -> Integer.valueOf(unit.getId())
                        .equals(course.getTeacherId())
                        && !remaining.contains(course.getSkill()))) {
            return Optional.of(ClearSpecialtyProblem.TEACHER_COURSE);
        }
        if (unit.isAtPlaceConstructionSlot()) {
            final ConstructionSlot slot = unit.getPlaceConstruction()
                    .getConstructionSlot();
            final int tier = EducationSkills.tier(slot.getConstructionType());
            if (tier > 0 && (slot.getConstruction().getConstructionSlots()
                    .indexOf(slot) >= tier
                    || remaining.stream().noneMatch(
                            skill -> EducationSkills.tier(skill) <= tier))) {
                return Optional.of(ClearSpecialtyProblem.SCHOOL_SLOT);
            }
        }
        return Optional.empty();
    }

    /**
     * Turn a specialist into a free colonist in place. Every precondition is
     * validated before the first change.
     */
    public void clearSpecialty(final Unit unit) {
        validateClearSpecialty(unit);
        ((UnitFreeColonist) unit).clearCivilianSpecialty(this);
    }

    /**
     * Authoritative pre-mutation check for clearing a specialty.
     *
     * @throws IllegalStateException when the turn is not ready or the target is
     *                               stale, foreign, unspecialized or holds an
     *                               incompatible teaching or school place
     */
    public void validateClearSpecialty(final Unit unit) {
        model.checkTurnPreparationComplete();
        final Optional<ClearSpecialtyProblem> problem = getClearSpecialtyProblem(
                unit);
        Preconditions.checkState(problem.isEmpty(),
                "Specialty cannot be cleared: %s", problem.orElse(null));
    }

    public boolean canAssignEducation(final Unit teacher, final Unit student,
            final UnitType skill) {
        return skill != null && getEducationOptions(teacher, student).stream()
                .anyMatch(option -> option.getSkill() == skill);
    }

    boolean canAcceptSchoolTeacher(final ConstructionSlot slot,
            final Unit unit) {
        final int tier = EducationSkills.tier(slot.getConstructionType());
        return tier == 0 || slot.getConstruction().getConstructionSlots()
                .indexOf(slot) < tier
                && getTeachingSkills(unit).stream()
                        .anyMatch(skill -> EducationSkills.tier(skill) <= tier);
    }

    private boolean isResident(final Unit unit) {
        return unit != null && unit.getOwner() == owner
                && model.tryGetUnitById(unit.getId()).orElse(null) == unit
                && getUnitsInsideColony().contains(unit);
    }

    private boolean isTeaching(final EducationCourse course) {
        if (course.getTeacherId() == null) {
            return false;
        }
        final Unit teacher = model.tryGetUnitById(course.getTeacherId())
                .orElse(null);
        if (!isResident(teacher)
                || !getTeachingSkills(teacher).contains(course.getSkill())
                || !teacher.isAtPlaceConstructionSlot()) {
            return false;
        }
        final ConstructionSlot slot = teacher.getPlaceConstruction()
                .getConstructionSlot();
        return slot.getColony() == this && EducationSkills
                .tier(slot.getConstructionType()) >= EducationSkills
                        .tier(course.getSkill());
    }

    /** Assign one teacher to a student, placing that teacher at school. */
    public void assignEducation(final Unit teacher, final Unit student,
            final UnitType skill) {
        model.checkTurnPreparationComplete();
        Preconditions.checkState(
                isValid() && model.isGameRunning()
                        && model.getCurrentPlayer() == owner,
                "Education is unavailable on this turn");
        Preconditions.checkArgument(
                teacher != null && student != null && teacher != student
                        && skill != null && isResident(teacher)
                        && isResident(student)
                        && canAssignEducation(teacher, student, skill),
                "Invalid teacher or student");
        final UnitFreeColonist pupil = (UnitFreeColonist) student;
        Preconditions.checkArgument(
                student.getType() == UnitType.COLONIST
                        || student.getType() == UnitType.INDENTURED_SERVANT
                        || student.getType() == UnitType.PETTY_CRIMINAL
                        || skill == UnitType.VETERAN_SOLDIER
                                && !pupil.isVeteran(),
                "Student already has a specialty");
        Preconditions.checkArgument(
                skill != UnitType.VETERAN_SOLDIER || !pupil.isVeteran(),
                "Student is already a veteran");
        Preconditions.checkState(
                educationCourses.stream()
                        .noneMatch(course -> Integer.valueOf(teacher.getId())
                                .equals(course.getTeacherId())
                                && course.getStudentId() != student.getId()),
                "Teacher has another student");
        final ConstructionSlot current = teacher.isAtPlaceConstructionSlot()
                ? teacher.getPlaceConstruction().getConstructionSlot()
                : null;
        final ConstructionSlot schoolSlot = current != null
                && current.getColony() == this
                && EducationSkills.tier(current
                        .getConstructionType()) >= EducationSkills.tier(skill)
                                ? current
                                : constructions.stream()
                                        .filter(con -> EducationSkills.tier(con
                                                .getType()) >= EducationSkills
                                                        .tier(skill))
                                        .flatMap(con -> con
                                                .getConstructionSlots()
                                                .stream())
                                        .filter(slot -> slot.canAccept(teacher))
                                        .findFirst().orElse(null);
        Preconditions.checkState(schoolSlot != null,
                "No eligible school teacher slot");
        final EducationCourse previous = educationCourses.stream()
                .filter(course -> course.getStudentId() == student.getId())
                .findFirst().orElse(null);
        if (schoolSlot != current) {
            teacher.placeToColonyStructureSlot(schoolSlot);
        }
        if (previous != null) {
            educationCourses.remove(previous);
        }
        educationCourses.add(new EducationCourse(teacher.getId(),
                student.getId(), skill,
                previous != null && previous.getSkill() == skill
                        ? previous.getCompletedUpdates()
                        : 0,
                EducationSkills.requiredUpdates(skill, student.getType()),
                false));
    }

    /** Cancel a student's course without changing its earned skill. */
    public void cancelEducation(final Unit student) {
        model.checkTurnPreparationComplete();
        Preconditions.checkState(isValid() && model.isGameRunning()
                && model.getCurrentPlayer() == owner && isResident(student),
                "Education is unavailable on this turn");
        Preconditions.checkState(
                educationCourses.removeIf(
                        course -> course.getStudentId() == student.getId()),
                "Student has no course");
    }

    void onResidentLeaving(final Unit unit) {
        educationCourses
                .removeIf(course -> course.getStudentId() == unit.getId());
        for (int index = 0; index < educationCourses.size(); index++) {
            final EducationCourse course = educationCourses.get(index);
            if (Integer.valueOf(unit.getId()).equals(course.getTeacherId())) {
                educationCourses.set(index,
                        new EducationCourse(null, course.getStudentId(),
                                course.getSkill(), course.getCompletedUpdates(),
                                course.getRequiredUpdates(), true));
            }
        }
    }

    void restoreEducationCourses(final List<EducationCoursePo> saved) {
        Preconditions.checkArgument(saved != null,
                "Missing saved education courses");
        for (final Construction building : constructions) {
            final int tier = EducationSkills.tier(building.getType());
            if (tier == 0) {
                continue;
            }
            for (int index = 0; index < building.getConstructionSlots()
                    .size(); index++) {
                final ConstructionSlot slot = building.getSlotAt(index);
                Preconditions.checkArgument(
                        slot.isEmpty() || index < tier
                                && getTeachingSkills(slot.getUnit()).stream()
                                        .anyMatch(skill -> EducationSkills
                                                .tier(skill) <= tier),
                        "Invalid saved school staff");
            }
        }
        final Set<Integer> students = new HashSet<>();
        final Set<Integer> teachers = new HashSet<>();
        for (final EducationCoursePo po : saved) {
            Preconditions.checkArgument(
                    po != null && po.getStudentId() != null
                            && po.getSkill() != null
                            && EducationSkills.tier(po.getSkill()) > 0
                            && po.getCompletedUpdates() != null
                            && po.getCompletedUpdates() >= 0
                            && students.add(po.getStudentId()),
                    "Invalid saved education course");
            final Unit student = model.tryGetUnitById(po.getStudentId())
                    .orElse(null);
            Preconditions.checkArgument(isResident(student)
                    && student instanceof UnitFreeColonist
                    && (student.getType() == UnitType.COLONIST
                            || student.getType() == UnitType.PETTY_CRIMINAL
                            || student.getType() == UnitType.INDENTURED_SERVANT
                            || po.getSkill() == UnitType.VETERAN_SOLDIER
                                    && !((UnitFreeColonist) student)
                                            .isVeteran())
                    && po.getCompletedUpdates() < EducationSkills
                            .requiredUpdates(po.getSkill(), student.getType()),
                    "Invalid saved education student or progress");
            if (po.getTeacherId() != null) {
                final Unit teacher = model.tryGetUnitById(po.getTeacherId())
                        .orElse(null);
                Preconditions.checkArgument(
                        !po.getTeacherId().equals(po.getStudentId())
                                && teachers.add(po.getTeacherId())
                                && isResident(teacher)
                                && getTeachingSkills(teacher)
                                        .contains(po.getSkill()),
                        "Invalid saved education teacher");
            }
            educationCourses.add(new EducationCourse(
                    po.getTeacherId(), po.getStudentId(), po.getSkill(),
                    po.getCompletedUpdates(), EducationSkills
                            .requiredUpdates(po.getSkill(), student.getType()),
                    po.getTeacherId() == null));
        }
        Preconditions.checkArgument(
                students.stream().noneMatch(teachers::contains),
                "Education participant has two roles");
    }

    /** @return an immutable snapshot of the selected export commodities */
    public Set<GoodsType> getExportGoods() {
        return Set.copyOf(exportGoods);
    }

    void restoreExportGoods(final List<GoodsType> saved) {
        Preconditions
                .checkArgument(
                        saved != null
                                && saved.stream()
                                        .noneMatch(type -> type == null)
                                && new HashSet<>(saved).size() == saved.size()
                                && GoodsType.BUYABLE_GOOD_TYPES
                                        .containsAll(saved),
                        "Invalid saved custom-house export selection");
        exportGoods.clear();
        exportGoods.addAll(saved);
    }

    /** Replace a current export selection after checking its saved snapshot. */
    public void configureExportGoods(final Set<GoodsType> expected,
            final Set<GoodsType> selected) {
        model.checkTurnPreparationComplete();
        Preconditions
                .checkArgument(
                        expected != null && selected != null
                                && selected.stream()
                                        .noneMatch(type -> type == null)
                                && GoodsType.BUYABLE_GOOD_TYPES
                                        .containsAll(selected),
                        "Invalid export selection");
        Preconditions.checkState(
                model.isGameRunning() && isValid()
                        && model.getColonyById(id).orElse(null) == this
                        && model.getPlayers().contains(owner)
                        && model.getCurrentPlayer() == owner
                        && wasConstructionBuilded(
                                ConstructionType.CUSTOM_HOUSE),
                "Custom house is unavailable");
        Preconditions.checkState(exportGoods.equals(expected),
                "Export selection changed");
        exportGoods.clear();
        exportGoods.addAll(selected);
    }

    /**
     * @param at world square
     * @return whether a resident of this colony works the field on that square
     */
    boolean hasWorkerAt(final Location at) {
        return colonyFields.stream().anyMatch(
                field -> field.isNotEmpty() && field.getLocation().equals(at));
    }

    /**
     * Plan the defense of one worker of a field that is attacked on its own
     * field tile. Reads only.
     *
     * @param attacker required attacking land unit
     * @param target   required field tile the attacker is adjacent to
     * @return frozen defense, or empty when no foreign worker works that tile
     */
    Optional<ColonyDefense> planFieldDefense(final Unit attacker,
            final Location target) {
        if (!isValid() || attacker.getOwner() == owner || attacker.isShip()) {
            return Optional.empty();
        }
        final Optional<Unit> worker = colonyFields.stream()
                .filter(field -> field.isNotEmpty()
                        && field.getLocation().equals(target))
                .map(field -> field.getUnit().get()).findFirst();
        if (worker.isEmpty() || !(worker.get() instanceof UnitFreeColonist)
                || !attacker.getType().getAttackableUnitType()
                        .contains(worker.get().getType())) {
            return Optional.empty();
        }
        final List<Unit> roster = List.of(worker.get());
        return Optional.of(new ColonyDefense(this, target,
                ColonyDefense.Kind.FIELD, worker.get(), roster, Map.of(), 0,
                false, describeDefense(target, ColonyDefense.Kind.FIELD,
                        worker.get(), roster, Map.of(), 0, false)));
    }

    /**
     * Plan what a colony defense quote would do to this colony's defenders if
     * the attacker wins: the armed tier first, then the inhabitants as one
     * group, optionally led by a Paul Revere emergency militiaman. Reads only.
     *
     * @param attacker   required attacking land unit
     * @param candidates required map units on the colony tile that this
     *                   attacker may legally fight
     * @return frozen defense, or empty when nothing defends or the capture that
     *         victory would cause has no safe atomic outcome
     */
    Optional<ColonyDefense> planDefense(final Unit attacker,
            final List<Unit> candidates) {
        if (!isValid() || attacker.getOwner() == owner || attacker.isShip()) {
            return Optional.empty();
        }
        final List<Unit> armed = new ArrayList<>();
        candidates.stream().filter(
                unit -> !unit.isShip() && CombatStrengths.isArmedLand(unit))
                .forEach(armed::add);
        getUnitsInsideColony().stream().filter(CombatStrengths::isArmedLand)
                .forEach(armed::add);
        if (!armed.isEmpty()) {
            armed.sort(Comparator
                    .comparing((Unit unit) -> !model
                            .areHostile(attacker.getOwner(), unit.getOwner()))
                    .thenComparingDouble(
                            unit -> -CombatStrengths
                                    .colonyDefenderStrength(model, unit, this,
                                            location, ColonyDefense.Kind.ARMED)
                                    .getTotal())
                    .thenComparingInt(Unit::getId));
            return Optional.of(new ColonyDefense(this, location,
                    ColonyDefense.Kind.ARMED, armed.get(0), armed, Map.of(), 0,
                    false, describeDefense(location, ColonyDefense.Kind.ARMED,
                            armed.get(0), armed, Map.of(), 0, false)));
        }
        final List<Unit> civilians = new ArrayList<>();
        getUnitsInsideColony().stream()
                .filter(unit -> unit instanceof UnitFreeColonist)
                .forEach(civilians::add);
        candidates.stream()
                .filter(unit -> unit.getOwner() == owner && !unit.isShip()
                        && unit instanceof UnitFreeColonist)
                .forEach(civilians::add);
        if (civilians.isEmpty()) {
            return Optional.empty();
        }
        civilians.sort(Comparator.comparingInt(Unit::getId));
        // Victory transfers the whole colony: decide before any draw whether
        // ships and peaceful occupants allow one atomic outcome.
        final Optional<Map<Unit, Location>> transfer = planOccupantTransfer(
                attacker, !model.areHostile(attacker.getOwner(), owner));
        if (transfer.isEmpty()) {
            return Optional.empty();
        }
        ColonyDefense.Kind kind = ColonyDefense.Kind.CIVILIANS;
        Unit representative = civilians.get(0);
        int muskets = 0;
        boolean exits = false;
        if (owner.getCongressMembers().contains(Founder.PAUL_REVERE)
                && warehouse.getGoods(GoodsType.MUSKET)
                        .getAmount() >= UnitFreeColonist.REQUIRED_MUSKETS_FOR_ARMED_UNIT) {
            final Optional<Unit> militia = getUnitsInsideColony().stream()
                    .filter(this::isEmergencyEligible)
                    .min(Comparator
                            .<Unit>comparingDouble(unit -> -CombatStrengths
                                    .colonyDefenderStrength(model, unit, this,
                                            location, ColonyDefense.Kind.REVERE)
                                    .getTotal())
                            .thenComparingInt(Unit::getId));
            if (militia.isPresent()) {
                kind = ColonyDefense.Kind.REVERE;
                representative = militia.get();
                muskets = UnitFreeColonist.REQUIRED_MUSKETS_FOR_ARMED_UNIT;
                // Only the last resident stays inside. A victory is a forced
                // exit, so the voluntary fortified-colony minimum is not
                // consulted.
                exits = getPopulation() > 1;
            }
        }
        return Optional.of(new ColonyDefense(this, location, kind,
                representative, civilians, transfer.get(), muskets, exits,
                describeDefense(location, kind, representative, civilians,
                        transfer.get(), muskets, exits)));
    }

    private boolean isEmergencyEligible(final Unit unit) {
        if (!(unit instanceof UnitFreeColonist)) {
            return false;
        }
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        return !colonist.isHoldingGuns() && !colonist.isMounted()
                && !colonist.isHoldingTools();
    }

    /**
     * Everything a confirmed battle depends on, as text: the complete roster
     * with actual places and equipment, the occupants of the tile, the ship
     * escape plan and the Revere inputs.
     */
    private String describeDefense(final Location battlefield,
            final ColonyDefense.Kind kind, final Unit representative,
            final List<Unit> roster, final Map<Unit, Location> escapes,
            final int muskets, final boolean exits) {
        final StringBuilder out = new StringBuilder();
        out.append(id).append('@').append(battlefield).append('/')
                .append(owner.getName()).append('/').append(kind)
                .append("/rep=").append(representative.getId()).append("/exit=")
                .append(exits).append("/muskets=").append(muskets)
                .append("/stock=")
                .append(warehouse.getGoods(GoodsType.MUSKET).getAmount())
                .append("/revere=")
                .append(owner.getCongressMembers()
                        .contains(Founder.PAUL_REVERE))
                .append("/pop=").append(getPopulation());
        roster.forEach(unit -> out.append('|').append(describeUnit(unit)));
        model.getUnitsAt(battlefield)
                .forEach(unit -> out.append('#').append(describeUnit(unit)));
        escapes.forEach((ship, target) -> out.append('>').append(ship.getId())
                .append("->").append(target));
        return out.toString();
    }

    private String describeUnit(final Unit unit) {
        final StringBuilder out = new StringBuilder().append(unit.getId())
                .append(':').append(unit.getType().name()).append(':')
                .append(unit.getOwner().getName()).append(':')
                .append(describePlace(unit));
        if (unit instanceof UnitFreeColonist) {
            final UnitFreeColonist colonist = (UnitFreeColonist) unit;
            out.append(':').append(colonist.isHoldingGuns())
                    .append(colonist.isMounted())
                    .append(colonist.isHoldingTools())
                    .append(colonist.isVeteran());
        } else if (unit instanceof UnitArtillery) {
            out.append(':').append(((UnitArtillery) unit).isDamaged());
        }
        return out.append(':').append(model.isFortified(unit)).toString();
    }

    private String describePlace(final Unit unit) {
        if (unit.isAtPlaceLocation()) {
            return "MAP" + unit.getLocation();
        }
        if (unit.isAtPlaceColonyField()) {
            return "FIELD" + colonyFields.stream()
                    .filter(field -> field.isWorkedBy(unit))
                    .map(field -> String.valueOf(field.getDirection()))
                    .findFirst().orElse("?");
        }
        if (unit.isAtPlaceConstructionSlot()) {
            final ConstructionSlot slot = unit.getPlaceConstruction()
                    .getConstructionSlot();
            return "JOB" + slot.getConstructionType() + slot.getConstruction()
                    .getConstructionSlots().indexOf(slot);
        }
        return "OTHER";
    }

    /**
     * Plan the transfer of units standing on the colony tile. Peaceful
     * occupants and armed units stop the capture, and every ship needs its own
     * safe adjacent water. It never changes anything.
     *
     * @param force    required capturing unit
     * @param declared whether the colony owner becomes hostile by the same
     *                 battle's declaration
     */
    private Optional<Map<Unit, Location>> planOccupantTransfer(final Unit force,
            final boolean declared) {
        final Player attacker = force.getOwner();
        if (!(declared || model.areHostile(attacker, owner)) || !force.getType()
                .canMoveAtTerrain(model.getMap().getTerrainTypeAt(location))) {
            return Optional.empty();
        }
        final List<Unit> occupants = model.getEnemyUnitsAt(attacker, location);
        if (occupants.stream()
                .anyMatch(unit -> !(model.areHostile(attacker, unit.getOwner())
                        || declared && unit.getOwner() == owner)
                        || (!unit.isShip()
                                && CombatStrengths.isArmedLand(unit)))
                || getUnitsInsideColony().stream()
                        .anyMatch(CombatStrengths::isArmedLand)) {
            return Optional.empty();
        }
        final Map<Unit, Location> escapes = new LinkedHashMap<>();
        for (final Unit ship : occupants) {
            if (!ship.isShip()) {
                continue;
            }
            final Optional<Location> destination = location.getNeighbors()
                    .stream().filter(model.getMap()::isValid)
                    .filter(target -> ship.getType().canMoveAtTerrain(
                            model.getMap().getTerrainTypeAt(target)))
                    .filter(target -> model
                            .getEnemyUnitsAt(ship.getOwner(), target).isEmpty())
                    .filter(target -> escapes.entrySet().stream().noneMatch(
                            entry -> entry.getValue().equals(target) && entry
                                    .getKey().getOwner() != ship.getOwner()))
                    // Use owner-only occupied destinations before reserving
                    // shared empty water.
                    .sorted(Comparator.comparing(
                            target -> model.getUnitsAt(target).isEmpty()))
                    .findFirst();
            if (destination.isEmpty()) {
                return Optional.empty();
            }
            escapes.put(ship, destination.get());
        }
        return Optional.of(escapes);
    }

    /**
     * Take the whole colony after the capturer won the defense battle. Only the
     * battle in {@link AbstractUnit} calls this, with the defense quote it was
     * confirmed against; there is no other way to take a colony. The ship
     * escapes were validated in that quote, before the first draw.
     *
     * @param capturingUnit required victor, already on the colony tile
     * @param defense       required frozen defense that was just defeated
     */
    void captureAfterWonBattle(final Unit capturingUnit,
            final ColonyDefense defense) {
        final Player player = capturingUnit.getOwner();
        Preconditions.checkState(
                defense.getColony() == this && defense.capturesColony()
                        && capturingUnit.isAtPlaceLocation()
                        && location.equals(capturingUnit.getLocation()),
                "Capturing force must have won the battle on the colony tile");
        final Map<Unit, Location> escapes = defense.getEscapes();
        Preconditions.checkState(
                planOccupantTransfer(capturingUnit, false)
                        .filter(escapes::equals).isPresent(),
                "The planned colony capture changed");
        escapes.forEach(this::escapeOneStep);
        model.getEnemyUnitsAt(player, location).stream()
                .filter(unit -> !unit.getType().isShip())
                .forEach(unit -> unit.takeOver(player));
        colonyFields.stream().filter(field -> !field.isEmpty())
                .forEach(field -> field.getUnit().get().takeOver(player));
        constructions.forEach(construction -> construction
                .getConstructionSlots().stream().filter(slot -> !slot.isEmpty())
                .forEach(slot -> slot.getUnit().takeOver(player)));

        if (owner.isHuman()) {
            model.getTurnEventStore()
                    .add(TurnEventProvider.getColonyWasLost(owner, this));
        }

        final Player previousOwner = owner;
        owner = player;
        // A European conqueror keeps liberty and becomes the affiliation; the
        // Crown leaves the last colonial affiliation untouched.
        if (!player.isKing()) {
            lastColonialAffiliation = player;
        }
        grantLaSalleStockade();
        // The former owner is told about the loss, so it remembers the new
        // owner even though it no longer sees the colony.
        previousOwner.recordColony(this);
        model.refreshObservations();
        model.fireColonyWasCaptured(model, capturingUnit, this);
    }

    /**
     * Unit escape from occupied place. When colony is captured naval unit
     * escape from occupied colony.
     *
     * @param unit   required escaping unit
     * @param target legal adjacent destination validated before capture
     */
    private void escapeOneStep(final Unit unit, final Location target) {
        final Location oldLocation = unit.getLocation();
        model.fireUnitMovedStepStarted(unit, oldLocation, target,
                unit.getPlaceLocation().getOrientation());
        unit.placeToLocation(target);
        model.fireUnitMovedStepFinished(unit, oldLocation, target);
    }

    /**
     * Get list of empty fields with maximal corn production. At all returned
     * field could be place unit and start producing food.
     *
     * @return list of colony fields
     */
    List<ColonyField> getEmptyFieldsWithMaxCornProduction() {
        final List<ColonyField> available = colonyFields.stream()
                .filter(ColonyField::isEmpty)
                .filter(field -> model.isColonyFieldAvailable(owner,
                        field.getLocation()))
                .filter(field -> field.canProduce(GoodsType.CORN)
                        && field.getGoodsTypeProduction(GoodsType.CORN) > 0)
                .collect(ImmutableList.toImmutableList());
        final int max = available.stream()
                .map(field -> field.getGoodsTypeProduction(GoodsType.CORN))
                .max(Comparator.naturalOrder()).orElse(0);
        return available.stream().filter(
                field -> field.getGoodsTypeProduction(GoodsType.CORN) == max)
                .collect(ImmutableList.toImmutableList());
    }

    private List<ColonyFieldPo> saveColonyFields() {
        final List<ColonyFieldPo> out = new ArrayList<>();
        colonyFields.forEach(field -> out.add(field.save()));
        return out;
    }

    private List<ConstructionPo> saveCostructions() {
        final List<ConstructionPo> out = new ArrayList<>();
        constructions.forEach(construction -> out.add(construction.save()));
        return out;
    }

    /**
     * Perform operation for next turn. Producing order:
     * <ul>
     * <li>Outside colony produce</li>
     * <li>Inside colony</li>
     * </ul>
     */
    public void startTurn() {
        // One snapshot for food, births, famine and post-famine output; bells
        // credited below affect the next update only.
        final PoliticalProduction political = getPoliticalProduction();
        final ColonyProductionStats initialProduction = getRawGoodsStats(
                political);
        final int breeding = getBreedingCount(initialProduction);
        // Validate national progress before famine, births or warehouse
        // changes.
        final int possibleCrosses = Math.max(0, initialProduction
                .getStatsByType(GoodsType.CROSS).getRowProduction());
        owner.checkCanCreditProducedCrosses(possibleCrosses);
        final int possibleBells = Math.max(0, initialProduction
                .getStatsByType(GoodsType.BELL).getRowProduction());
        owner.checkCanCreditProducedBells(possibleBells);
        final Unit famineVictim = eatFoodAndOptionallyMakeNewColonist(
                political);
        // from now colony could be destroyed by famine.
        if (isValid()) {
            final ColonyProductionStats colonyStats = getRawGoodsStats(
                    political);
            final int producedCrosses = Math.max(0, colonyStats
                    .getStatsByType(GoodsType.CROSS).getRowProduction());
            final int producedBells = Math.max(0, colonyStats
                    .getStatsByType(GoodsType.BELL).getRowProduction());
            colonyStats.forEach((goodsType, stats) -> {
                if (GoodsType.CORN.equals(goodsType)) {
                    if (famineVictim != null) {
                        warehouse.setGoods(Goods.of(GoodsType.CORN,
                                Math.max(0, stats.getInWarehouseAfter())));
                    }
                    if (stats.getInWarehouseAfter() < 0) {
                        model.addTurnEvent(TurnEventProvider
                                .getFamineWillPlagueColony(owner, this));
                    }
                    return;
                }
                if (stats.getDiff() > 0) {
                    warehouse.stageProducedGoods(
                            Goods.of(goodsType, stats.getDiff()));

                } else {
                    warehouse
                            .removeGoods(Goods.of(goodsType, -stats.getDiff()));
                }
            });
            owner.creditProducedCrosses(producedCrosses);
            owner.creditProducedBells(producedBells);
            applyProducedBells(producedBells);
            advanceEducationAndExperience(colonyStats, political);
            if (breeding > 0) {
                warehouse.addGoods(Goods.of(GoodsType.HORSE, breeding));
                warehouse.removeGoods(Goods.of(GoodsType.CORN, breeding));
            }
            colonyBuildingQueue.startTurn();
            exportSelectedSurplus();
            warehouse.clampProducedOverflow(thrownAwayGoods -> model
                    .addTurnEvent(TurnEventProvider.getGoodsWasThrowsAway(owner,
                            thrownAwayGoods, this)));
        }
        if (famineVictim != null) {
            model.addTurnEvent(TurnEventProvider.getFaminePlagueColony(owner,
                    this, famineVictim));
        }
    }

    private void advanceEducationAndExperience(
            final ColonyProductionStats committed,
            final PoliticalProduction political) {
        final Set<Integer> schoolCompletions = new HashSet<>();
        final Map<Unit, UnitType> jobs = new LinkedHashMap<>();
        final Set<Unit> productive = new HashSet<>();
        for (final ColonyField field : colonyFields) {
            if (field.getUnit().isEmpty()
                    || field.getProduction(political).isEmpty()) {
                continue;
            }
            final Goods goods = field.getProduction(political).get();
            EducationSkills
                    .forProduction(goods.getType(),
                            goods.getType() == GoodsType.CORN
                                    && field.getTerrainType().isSea())
                    .ifPresent(skill -> jobs.put(field.getUnit().get(), skill));
            if (goods.getAmount() > 0) {
                productive.add(field.getUnit().get());
            }
        }
        for (final Construction building : constructions) {
            if (building.getType().getProductionPerTurn().isEmpty()) {
                continue;
            }
            final GoodsType goods = building.getType().getProductionPerTurn()
                    .get().getType();
            final Optional<UnitType> skill = EducationSkills
                    .forProduction(goods, false);
            if (skill.isEmpty()) {
                continue;
            }
            int remaining = building.getType().getConsumptionPerTurn()
                    .isPresent()
                            ? committed.getStatsByType(goods).getRowProduction()
                            : Integer.MAX_VALUE;
            for (final ConstructionSlot slot : building.getOrderedSlots()) {
                if (slot.isEmpty()) {
                    continue;
                }
                final Unit worker = slot.getUnit();
                jobs.put(worker, skill.get());
                final int potential = slot
                        .getPotentialProduction(worker, political).getAmount();
                final int assigned = Math.min(potential, remaining);
                remaining -= assigned;
                if (assigned > 0) {
                    productive.add(worker);
                }
            }
        }
        for (int index = 0; index < educationCourses.size();) {
            EducationCourse course = educationCourses.get(index);
            final Unit student = model.tryGetUnitById(course.getStudentId())
                    .orElse(null);
            if (!isResident(student) || !(student instanceof UnitFreeColonist)
                    || student.getType() != UnitType.COLONIST
                            && student.getType() != UnitType.PETTY_CRIMINAL
                            && student.getType() != UnitType.INDENTURED_SERVANT
                            && course.getSkill() != UnitType.VETERAN_SOLDIER
                    || course.getSkill() == UnitType.VETERAN_SOLDIER
                            && ((UnitFreeColonist) student).isVeteran()) {
                educationCourses.remove(index);
                continue;
            }
            final int required = EducationSkills
                    .requiredUpdates(course.getSkill(), student.getType());
            if (course.getRequiredUpdates() != required) {
                course = new EducationCourse(course.getTeacherId(),
                        course.getStudentId(), course.getSkill(), 0, required,
                        !isTeaching(course));
                educationCourses.set(index, course);
            }
            if (!isTeaching(course)) {
                index++;
                continue;
            }
            final int next = course.getCompletedUpdates() + 1;
            if (next < course.getRequiredUpdates()) {
                educationCourses.set(index,
                        new EducationCourse(course.getTeacherId(),
                                course.getStudentId(), course.getSkill(), next,
                                course.getRequiredUpdates(), false));
                index++;
                continue;
            }
            final UnitFreeColonist pupil = (UnitFreeColonist) student;
            final UnitType before = student.getType();
            final boolean veteranBefore = pupil.isVeteran();
            if (pupil instanceof UnitLaborer
                    && (before == UnitType.PETTY_CRIMINAL
                            || before == UnitType.INDENTURED_SERVANT)) {
                ((UnitLaborer) pupil).advanceEducationStage();
                educationCourses.set(index,
                        new EducationCourse(course.getTeacherId(),
                                course.getStudentId(), course.getSkill(), 0,
                                EducationSkills.requiredUpdates(
                                        course.getSkill(), student.getType()),
                                false));
                index++;
            } else {
                pupil.completeEducation(course.getSkill());
                educationCourses.remove(index);
            }
            schoolCompletions.add(student.getId());
            model.addTurnEvent(TurnEventProvider.getEducationCompleted(owner,
                    this, student, before, veteranBefore, false));
        }

        for (final Unit resident : getUnitsInsideColony()) {
            if (resident instanceof UnitFreeColonist) {
                final UnitFreeColonist worker = (UnitFreeColonist) resident;
                final UnitType before = worker.getType();
                final boolean veteranBefore = worker.isVeteran();
                if (worker.recordWork(jobs.get(resident),
                        productive.contains(resident),
                        schoolCompletions.contains(resident.getId()))) {
                    educationCourses.removeIf(course -> course
                            .getStudentId() == resident.getId()
                            && course.getSkill() != UnitType.VETERAN_SOLDIER);
                    model.addTurnEvent(
                            TurnEventProvider.getEducationCompleted(owner, this,
                                    resident, before, veteranBefore, true));
                }
            }
        }
    }

    private void exportSelectedSurplus() {
        if (!wasConstructionBuilded(ConstructionType.CUSTOM_HOUSE)
                || exportGoods.isEmpty()) {
            return;
        }
        final List<Goods> sales = new ArrayList<>();
        final List<Integer> incomes = new ArrayList<>();
        int finalGold = owner.getGold();
        for (final GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
            if (!exportGoods.contains(type)) {
                continue;
            }
            // A boycott closes ordinary exports; the revolutionary custom
            // house bypasses it.
            if (owner.isBoycotted(type) && !owner.isDeclaredIndependence()) {
                continue;
            }
            final int amount = warehouse.getGoods(type).getAmount() - 100;
            if (amount <= 0) {
                continue;
            }
            final Goods goods = Goods.of(type, amount);
            final int marketIncome = owner.quoteEuropeTrade(goods, false)
                    .getTotal();
            final int income = owner.isDeclaredIndependence() ? marketIncome / 2
                    : marketIncome;
            finalGold = Math.addExact(finalGold, income);
            sales.add(goods);
            incomes.add(income);
        }
        if (sales.isEmpty()) {
            return;
        }
        owner.setGold(finalGold);
        for (int index = 0; index < sales.size(); index++) {
            final Goods goods = sales.get(index);
            warehouse.removeGoods(goods);
            model.addTurnEvent(TurnEventProvider.getCustomHouseExport(owner,
                    this, goods, incomes.get(index)));
        }
    }

    private Unit eatFoodAndOptionallyMakeNewColonist(
            final PoliticalProduction political) {
        final ColonyProductionStats stats = getRawGoodsStats(political);
        final GoodsProductionStats cornStats = stats
                .getStatsByType(GoodsType.CORN);
        if (cornStats.getInWarehouseAfter() < 0) {
            final Unit victim = killOneRandomUnit();
            warehouse.setGoods(Goods.of(GoodsType.CORN, 0));
            return victim;
        } else {
            final int foodAfterConsumption = cornStats.getInWarehouseAfter();
            if (foodAfterConsumption >= FOOD_LEVEL_TO_FREE_COLONIST) {
                final int births = foodAfterConsumption
                        / FOOD_LEVEL_TO_FREE_COLONIST;
                warehouse.setGoods(Goods.of(GoodsType.CORN,
                        foodAfterConsumption % FOOD_LEVEL_TO_FREE_COLONIST));
                for (int index = 0; index < births; index++) {
                    produceNewColonist();
                }
            } else {
                if (cornStats.getDiff() < 0) {
                    warehouse.removeGoods(
                            Goods.of(GoodsType.CORN, -cornStats.getDiff()));
                } else {
                    warehouse.addGoodsWithThrowingAway(
                            Goods.of(GoodsType.CORN, cornStats.getDiff()),
                            thrownAwayGoods -> {
                                model.addTurnEvent(
                                        TurnEventProvider.getGoodsWasThrowsAway(
                                                owner, thrownAwayGoods, this));
                            });
                }
            }
        }
        return null;
    }

    /**
     * New colonist will appears outside of colony.
     */
    private void produceNewColonist() {
        final Unit newUnit = model.addUnitOutSideColony(this);
        model.getTurnEventStore()
                .add(TurnEventProvider.getNewUnitInColony(getOwner(), this));
        model.fireNewUnitWasBorn(newUnit);
    }

    private Unit killOneRandomUnit() {
        int index = random.nextInt(getUnitsInsideColony().size());
        final Unit u = getUnitsInsideColony().get(index);
        u.getPlace().destroy();
        model.destroyUnit(u);
        return u;
    }

    public Integer getRequiredFoodPerTurn() {
        return getUnitsInsideColony().stream()
                .mapToInt(unit -> unit.getType().getAteFoodPerTurn()).sum();
    }

    /**
     * @return owned cargo carriers currently at the colony, in registry order
     */
    public List<UnitWithCargo> getUnitsInPort() {
        return model.getUnitsAt(location).stream()
                .filter(unit -> unit instanceof UnitWithCargo
                        && unit.getOwner().equals(owner))
                .map(UnitWithCargo.class::cast)
                .collect(ImmutableList.toImmutableList());
    }

    public List<Unit> getUnitsOutSideColony() {
        // Allied passage never gives a colony control of a visitor.
        return model.getUnitsAt(location).stream()
                .filter(unit -> unit.getOwner().equals(owner))
                .filter(unit -> !unit.getType().canHoldCargo())
                .collect(ImmutableList.toImmutableList());
    }

    public Construction getConstructionByType(
            final ConstructionType constructionType) {
        return constructions.stream().filter(
                construction -> construction.getType().equals(constructionType))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "No such construction type (%s) in colony (%s)",
                        constructionType, getName())));
    }

    public boolean isContainsConstructionByType(
            final ConstructionType constructionType) {
        return constructions.stream()
                .map(construction -> construction.getType()).filter(type -> type
                        .getUpgradeFromChain().contains(constructionType))
                .findAny().isPresent();
    }

    /**
     * Investigate in construction type is builded or was builded and
     * construction was upgraded.
     *
     * @param constructionType required construction type
     * @return Return <code>true</code> when construction type is builded or was
     *         builded and was upgraded otherwise return <code>false</code>.
     */
    public boolean wasConstructionBuilded(
            final ConstructionType constructionType) {
        return constructions.stream().filter(
                construction -> construction.getType().equals(constructionType))
                .findAny().isPresent();
    }

    ConstructionType getWarehouseType() {
        return constructions.stream()
                .filter(cont -> ConstructionType.WAREHOUSES
                        .contains(cont.getType()))
                .findAny().orElseThrow(() -> new IllegalStateException(
                        "Colony doesn't contains any warehouse."))
                .getType();
    }

    /**
     * Get colony in direction. All fields are identified by direction in which
     * it lies.
     *
     * @param fieldDirection required filed direction
     * @return return colony field
     */
    public ColonyField getColonyFieldInDirection(
            final Direction fieldDirection) {
        Preconditions.checkNotNull(fieldDirection, "Field direction is null");
        return colonyFields.stream()
                .filter(colonyFiled -> colonyFiled.getDirection()
                        .equals(fieldDirection))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Field directiond (%s) is not in colony (%s)",
                        fieldDirection, this)));
    }

    void createConstruction(final ConstructionType constructionType) {
        if (constructionType.getUpgradeFrom().isPresent()) {
            final ConstructionType alreadyHave = constructionType
                    .getUpgradeFrom().get();
            getConstructionByType(alreadyHave).upgrade();
        } else {
            constructions
                    .add(Construction.build(model, this, constructionType));
        }
    }

    /**
     * Grant once after gameplay transitions, never while restoring a colony.
     */
    void grantLaSalleStockade() {
        if (isValid()
                && owner.getCongressMembers()
                        .contains(Founder.SIEUR_DE_LA_SALLE)
                && getUnitsInsideColony().size() >= ConstructionType.STOCKADE
                        .getRequiredColonyPopulation()
                && !isContainsConstructionByType(ConstructionType.STOCKADE)) {
            createConstruction(ConstructionType.STOCKADE);
        }
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    void setName(final String name) {
        this.name = Preconditions.checkNotNull(name);
    }

    public Location getLocation() {
        return location;
    }

    public Player getOwner() {
        return owner;
    }

    public List<ColonyField> getColonyFields() {
        return colonyFields;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Colony.class).add("name", name)
                .add("location", location).toString();
    }

    public List<Construction> getConstructions() {
        return constructions;
    }

    public ColonyWarehouse getWarehouse() {
        return warehouse;
    }

    /**
     * Residents working in fields or buildings; excludes visitors and
     * passengers.
     */
    public List<Unit> getUnitsInsideColony() {
        final List<Unit> out = new ArrayList<>();
        constructions.forEach(construction -> {
            construction.getConstructionSlots().forEach(slot -> {
                if (!slot.isEmpty()) {
                    out.add(slot.getUnit());
                }
            });
        });
        colonyFields.forEach(field -> {
            if (field.getUnit().isPresent()) {
                out.add(field.getUnit().get());
            }
        });
        return ImmutableList.copyOf(out);
    }

    public boolean isLastUnitIncolony(final Unit unit) {
        Preconditions.checkNotNull(unit);
        final List<Unit> unitsInColony = getUnitsInsideColony();
        if (unitsInColony.size() > 1) {
            return false;
        } else if (unitsInColony.size() == 1) {
            final Unit u = unitsInColony.get(0);
            return unit.equals(u);
        } else {
            throw new IllegalStateException(
                    String.format("Colony have invalid number of units (%s)",
                            unitsInColony.size()));
        }
    }

    /**
     * Verify number of units in colony and when it's 0 than destroy colony.
     */
    void verifyNumberOfUnitsOptionallyDestroyColony() {
        if (getUnitsInsideColony().isEmpty()) {
            model.destroyColony(this);
        } else {
            // Completed resident exit; same-colony reassignment never gets
            // here.
            capLibertyToPopulation();
        }
    }

    public boolean isValid() {
        return model.isExists(this);
    }

    boolean canVoluntarilyLoseResident() {
        return (!wasConstructionBuilded(ConstructionType.STOCKADE)
                && !wasConstructionBuilded(ConstructionType.FORT)
                && !wasConstructionBuilded(ConstructionType.FORTRESS))
                || getUnitsInsideColony().size() > 3;
    }

    public List<ColonyJobChoice> getJobChoices(final Unit worker) {
        return ColonyJobChoice.forWorker(model, this, worker);
    }

    boolean canWorkerActHere(final Unit unit) {
        if (!(unit instanceof UnitFreeColonist) || !isValid()
                || !model.isGameRunning() || model.getCurrentPlayer() != owner
                || unit.getOwner() != owner
                || model.getPlayers().stream()
                        .noneMatch(player -> player == unit.getOwner())
                || model.tryGetUnitById(unit.getId()).orElse(null) != unit) {
            return false;
        }
        if (unit.isAtPlaceColonyField()) {
            return getColonyFields().stream()
                    .anyMatch(field -> field.isWorkedBy(unit));
        }
        if (unit.isAtPlaceConstructionSlot()) {
            final ConstructionSlot slot = unit.getPlaceConstruction()
                    .getConstructionSlot();
            return !slot.isEmpty() && slot.getUnit() == unit
                    && getConstructions().stream()
                            .flatMap(construction -> construction
                                    .getConstructionSlots().stream())
                            .anyMatch(candidate -> candidate == slot);
        }
        return unit.isAtPlaceLocation()
                && unit.getLocation().equals(getLocation());
    }

    boolean canAcceptWorker(final Unit unit) {
        if (!canWorkerActHere(unit)) {
            return false;
        }
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        return (!colonist.isHoldingGuns() || colonist
                .getEquipmentProblem(GoodsType.MUSKET, false).isEmpty())
                && (!colonist.isHoldingTools() || colonist
                        .getEquipmentProblem(GoodsType.TOOLS, false).isEmpty())
                && (!colonist.isMounted() || colonist
                        .getEquipmentProblem(GoodsType.HORSE, false).isEmpty());
    }

    /**
     * Compute military strength of colony. Method just sum up strength of all
     * units in colony.
     *
     * @return Return military power of colony
     */
    public int getMilitaryForce() {
        double force = 0;
        // count units outside colony
        for (final Unit unit : model.getUnitsAt(owner, location)) {
            force += unit.getMilitaryStrenght();
        }
        // count units in colony
        for (final Unit unit : getUnitsInsideColony()) {
            force += unit.getMilitaryStrenght();
        }
        return (int) force;
    }

    /**
     * Get production statistics per turn.
     *
     * @return Return colony production statistics per turn.
     */
    public ColonyProductionStats getGoodsStats() {
        final ColonyProductionStats preview = getRawGoodsStats();
        final GoodsProductionStats food = preview
                .getStatsByType(GoodsType.CORN);
        final int afterResidents = food.getInWarehouseAfter();
        if (afterResidents >= 0) {
            final int births = afterResidents / FOOD_LEVEL_TO_FREE_COLONIST;
            final int breeding = getBreedingCount(preview);
            food.addConsumed(Math.addExact(
                    Math.multiplyExact(births, FOOD_LEVEL_TO_FREE_COLONIST),
                    breeding));
            preview.getStatsByType(GoodsType.HORSE).addRowProduction(breeding);
        }
        return preview;
    }

    private int getBreedingCount(final ColonyProductionStats raw) {
        final int foodAfterResidents = raw.getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter();
        if (foodAfterResidents < 0) {
            return 0;
        }
        final int remainingFood = foodAfterResidents
                % FOOD_LEVEL_TO_FREE_COLONIST;
        final int horses = warehouse.getGoods(GoodsType.HORSE).getAmount();
        if (horses < 2) {
            return 0;
        }
        final int base = Math.min(2, horses / 2);
        final int rate = wasConstructionBuilded(ConstructionType.STABLES)
                || wasConstructionBuilded(ConstructionType.LARGE_STABLES)
                        ? base * 2
                        : base;
        final int freeCapacity = warehouse.getStorageCapacity(GoodsType.HORSE)
                .getAmount() - horses;
        return Math.max(0,
                Math.min(rate, Math.min(remainingFood, freeCapacity)));
    }

    ColonyProductionStats getRawGoodsStats() {
        return getRawGoodsStats(getPoliticalProduction());
    }

    private ColonyProductionStats getRawGoodsStats(
            final PoliticalProduction political) {
        final ColonyProductionStats out = new ColonyProductionStats();
        // set initial warehouse stack
        GoodsType.GOOD_TYPES.forEach(goodsType -> {
            GoodsProductionStats goodsStats = out.getStatsByType(goodsType);
            goodsStats.setInWarehouseBefore(
                    warehouse.getGoods(goodsType).getAmount());
        });

        // get production from all fields
        colonyFields.forEach(field -> {
            if (field.getProduction(political).isPresent()) {
                final Goods production = field.getProduction(political).get();
                final GoodsProductionStats goodsStats = out
                        .getStatsByType(production.getType());
                goodsStats.addRowProduction(production.getAmount());
            }
        });

        out.getStatsByType(GoodsType.CORN)
                .addConsumed(getRequiredFoodPerTurn());

        // get production from town factories that doesn't consume any sources
        ConstructionType.SOURCE_1.forEach(goodsType -> {
            getConstructionProducing(goodsType).ifPresent(con -> {
                GoodsProductionStats goodsStats = out.getStatsByType(goodsType);
                ConstructionTurnProduction turnProd = con
                        .getProduction(political);
                goodsStats.setRowProduction(
                        turnProd.getProducedGoods().get().getAmount());
            });
        });

        // get production from town factories that consume some primary sources
        ConstructionType.SOURCE_2.forEach(goodsType -> {
            computeSecondaryProduction(out, goodsType, political);
        });

        // get production from town factories that consume secondary sources
        ConstructionType.SOURCE_3.forEach(goodsType -> {
            computeSecondaryProduction(out, goodsType, political);
        });

        return out;
    }

    private void computeSecondaryProduction(final ColonyProductionStats out,
            final GoodsType goodsTypeProduced,
            final PoliticalProduction political) {
        if (getConstructionProducing(goodsTypeProduced).isPresent()) {
            final Construction producedAt = getConstructionProducing(
                    goodsTypeProduced).get();
            GoodsProductionStats goodProdStats = out
                    .getStatsByType(goodsTypeProduced);
            GoodsType goodsTypeConsumed = producedAt.getType()
                    .getConsumptionPerTurn().get().getType();
            GoodsProductionStats goodConsumedStats = out
                    .getStatsByType(goodsTypeConsumed);

            Preconditions.checkState(goodConsumedStats.getConsumed() == 0,
                    "good type was already computed, good was already consumed.");
            int numberOfavailableInputGoods = goodConsumedStats
                    .getInWarehouseAfter();

            final ConstructionTurnProduction turnProd = producedAt
                    .getProduction(Goods.of(goodsTypeConsumed,
                            numberOfavailableInputGoods), political);
            goodConsumedStats
                    .addConsumed(turnProd.getConsumedGoods().get().getAmount());
            goodProdStats.setRowProduction(
                    turnProd.getProducedGoods().get().getAmount());
            if (turnProd.getBlockedGoods().isPresent()) {
                goodProdStats.setBlockedProduction(
                        turnProd.getBlockedGoods().get().getAmount());
            } else {
                goodProdStats.setBlockedProduction(0);
            }
        }
    }

    private Optional<Construction> getConstructionProducing(
            final GoodsType goodsType) {
        return constructions.stream()
                .filter(construction -> construction.getType()
                        .getProductionPerTurn().isPresent()
                        && construction.getType().getProductionPerTurn().get()
                                .getType().equals(goodsType))
                .findAny();
    }

    /**
     * @return the colonyBuildingQueue
     */
    public ColonyBuildingQueue getColonyBuildingQueue() {
        return colonyBuildingQueue;
    }
}
