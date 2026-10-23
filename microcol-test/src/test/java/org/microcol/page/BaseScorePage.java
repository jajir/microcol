package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

/**
 * Opens the read-only base score preview through the statistics screen. Each
 * component is read from its label; the verify methods compare the shown points
 * with the values remembered at an earlier moment of the same journey.
 */
public final class BaseScorePage extends AbstractScreen {

    private static final Pattern POINTS = Pattern.compile("(\\d+) points");

    private static final String POPULATION = "baseScorePopulation";
    private static final String FATHERS = "baseScoreFathers";
    private static final String GOLD = "baseScoreGold";
    private static final String SUPPORT = "baseScoreSupport";
    private static final String SUBTOTAL = "baseScoreSubtotal";

    private static final String RESULT_TITLE = "resultTitle";
    private static final String RESULT_DEDUCTION = "resultDeduction";
    private static final String RESULT_BELLS = "resultBells";
    private static final String RESULT_CLAMPED = "resultClampedBase";
    private static final String RESULT_BONUS = "resultBonus";
    private static final String RESULT_DIFFICULTY = "resultDifficulty";
    private static final String RESULT_TOTAL = "resultTotal";

    private static final Pattern BONUS_PERCENT = Pattern
            .compile("bonus \\+(\\d+)%");
    private static final Pattern EARLY_POINTS = Pattern
            .compile("early declaration points: (\\d+)");
    private static final Pattern DIFFICULTY_PERCENT = Pattern
            .compile("factor: (\\d+)%");
    private static final Pattern DESTROYED_COUNT = Pattern
            .compile("settlements: (\\d+) at");
    private static final Pattern POINTS_EACH = Pattern
            .compile("at (\\d+) each");

    /** Points of each component when the values were last remembered. */
    private final Map<String, Long> remembered = new HashMap<>();

    /** Calculated result when it was last remembered. */
    private long rememberedClampedBase;
    private long rememberedResultTotal;

    public BaseScorePage(final TestContext context) {
        super(context);
    }

    public void open() {
        new FreePlayAuditPage(getContext()).openStatistics();
        getRobot().clickOn("#baseScoreTab");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public GamePage close() {
        clickOnButtonWithId("closeButtonId");
        return GamePage.of(getContext());
    }

    /** Stores the shown points of every component for later comparison. */
    public void rememberScore() {
        remembered.clear();
        for (final String id : new String[] { POPULATION, FATHERS, GOLD,
                SUPPORT, SUBTOTAL }) {
            remembered.put(id, points(id));
        }
    }

    public void verifyTabSelected() {
        final ToggleButton tab = getNodeFinder().lookup("#baseScoreTab")
                .queryAs(ToggleButton.class);
        assertTrue(tab.isSelected(), "Base score tab selected");
    }

    /** The preview names itself as a base score and not as the final result. */
    public void verifyBaseNotFinal() {
        final String title = text("baseScoreTitle");
        assertTrue(title.contains("Base score"), "Base title: " + title);
        assertTrue(title.contains("not the final ranked score"),
                "Base title is not final: " + title);
    }

    public void verifyTreasuryPoints(final long expected) {
        assertEquals(expected, points(GOLD), "Treasury points");
    }

    /** Every component is a whole, nonnegative number of points. */
    public void verifySubtotalIsSumOfComponents() {
        final long sum = points(POPULATION) + points(FATHERS) + points(GOLD)
                + points(SUPPORT);
        assertEquals(sum, points(SUBTOTAL), "Base subtotal");
    }

    /**
     * After a legal action, population and treasury move by the given points,
     * the fathers and national support stay, and the subtotal follows.
     */
    public void verifyChangeFromRemembered(final long populationDelta,
            final long goldDelta) {
        assertEquals(remembered.get(POPULATION) + populationDelta,
                points(POPULATION), "Population points");
        assertEquals(remembered.get(GOLD) + goldDelta, points(GOLD),
                "Treasury points");
        assertEquals(remembered.get(FATHERS), points(FATHERS),
                "Fathers points");
        assertEquals(remembered.get(SUPPORT), points(SUPPORT),
                "Support points");
        assertEquals(remembered.get(SUBTOTAL) + populationDelta + goldDelta,
                points(SUBTOTAL), "Base subtotal");
    }

    /** Reopening or Continue shows exactly the remembered components. */
    public void verifyRememberedScoreKept() {
        for (final String id : remembered.keySet()) {
            assertEquals(remembered.get(id), points(id), "Points of " + id);
        }
    }

    /** Stores the shown calculated result for a later comparison. */
    public void rememberCalculatedResult() {
        rememberedClampedBase = points(RESULT_CLAMPED);
        rememberedResultTotal = points(RESULT_TOTAL);
    }

    /** The calculated block names itself as a preview that records nothing. */
    public void verifyCalculatedPreviewNotRecorded() {
        final String title = text(RESULT_TITLE);
        assertTrue(title.contains("Calculated result preview"),
                "Result title: " + title);
        assertTrue(title.contains("not recorded"),
                "Result title is not recorded: " + title);
    }

    /**
     * The clamped base is the subtotal less the deductions plus the bells, and
     * the total is the floor of the calculated result from the shown factors.
     */
    public void verifyCalculatedResultFromComponents() {
        final long clamped = points(RESULT_CLAMPED);
        assertEquals(
                Math.max(0,
                        points(SUBTOTAL) - points(RESULT_DEDUCTION)
                                + points(RESULT_BELLS)),
                clamped, "Clamped base");
        final long bonus = number(RESULT_BONUS, BONUS_PERCENT);
        final long early = number(RESULT_BONUS, EARLY_POINTS);
        final long factor = number(RESULT_DIFFICULTY, DIFFICULTY_PERCENT);
        assertEquals((clamped * (100 + bonus) + 100 * early) * factor / 10000,
                points(RESULT_TOTAL), "Calculated result");
    }

    /** Without a successful independence both bonus terms are zero. */
    public void verifyNoIndependenceTerms() {
        assertEquals(0, number(RESULT_BONUS, BONUS_PERCENT), "Order bonus");
        assertEquals(0, number(RESULT_BONUS, EARLY_POINTS), "Early points");
    }

    /**
     * The shown destroyed settlements and their price, and the deduction that
     * follows from them.
     */
    public void verifyDestructionTerms(final long destroyed,
            final long pointsEach) {
        assertEquals(destroyed, number(RESULT_DEDUCTION, DESTROYED_COUNT),
                "Destroyed settlements");
        assertEquals(pointsEach, number(RESULT_DEDUCTION, POINTS_EACH),
                "Points per settlement");
        assertEquals(destroyed * pointsEach, points(RESULT_DEDUCTION),
                "Destruction deduction");
    }

    public void verifyDifficultyPercent(final long expected) {
        assertEquals(expected, number(RESULT_DIFFICULTY, DIFFICULTY_PERCENT),
                "Difficulty factor percent");
    }

    /** After a legal action the clamped base moves by the given points. */
    public void verifyCalculatedChangeFromRemembered(final long clampedDelta) {
        assertEquals(rememberedClampedBase + clampedDelta,
                points(RESULT_CLAMPED), "Clamped base points");
    }

    /** Reopening or Continue shows exactly the remembered calculated result. */
    public void verifyRememberedCalculatedResultKept() {
        assertEquals(rememberedClampedBase, points(RESULT_CLAMPED),
                "Clamped base points");
        assertEquals(rememberedResultTotal, points(RESULT_TOTAL),
                "Calculated result points");
    }

    private String text(final String id) {
        return getNodeFinder().lookup("#" + id).queryAs(Label.class).getText();
    }

    /** The first number the pattern finds in the text of the label. */
    private long number(final String id, final Pattern pattern) {
        final Matcher matcher = pattern.matcher(text(id));
        if (!matcher.find()) {
            throw new IllegalStateException(
                    "No " + pattern.pattern() + " in " + id + ": " + text(id));
        }
        return Long.parseLong(matcher.group(1));
    }

    /**
     * The first "N points" phrase of the component label. A missing phrase is a
     * broken preview, reported as a failure of the reading itself.
     */
    private long points(final String id) {
        final Matcher matcher = POINTS.matcher(text(id));
        if (!matcher.find()) {
            throw new IllegalStateException(
                    "No points phrase in " + id + ": " + text(id));
        }
        return Long.parseLong(matcher.group(1));
    }
}
