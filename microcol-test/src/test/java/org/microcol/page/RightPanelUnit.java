package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Preconditions;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Object allows to test one unit shown in right panel.
 */
public class RightPanelUnit extends AbstractPageComponent {

    private final VBox unitBox;

    public static RightPanelUnit of(final TestContext context,
            final VBox unitBox) {
        return new RightPanelUnit(context, unitBox);
    }

    private RightPanelUnit(final TestContext context, final VBox unitBox) {
        super(context);
        this.unitBox = Preconditions.checkNotNull(unitBox);
    }

    public void selectUnit() {
        // The card identifies the unit; selection now belongs to the map menu.
        final String id = getNodeFinder().from(unitBox).lookup(
                (java.util.function.Predicate<javafx.scene.Node>) node -> node
                        .getId() != null
                        && node.getId().startsWith("unitPanel-"))
                .query().getId();
        GamePage.of(getContext()).selectMapUnit(
                Integer.parseInt(id.substring("unitPanel-".length())));
    }

    public RightPanelUnit verifyFreeActionPoints(
            final Integer expectedNUmberOfActionPoints) {
        Preconditions.checkNotNull(expectedNUmberOfActionPoints);
        final Label l = getNodeFinder().from(unitBox).lookup(".unitMoves")
                .queryAs(Label.class);
        final String txt = l.getText().substring("Moves: ".length());
        final Integer numberOfActionPoints = Integer.valueOf(txt);
        assertEquals(expectedNUmberOfActionPoints, numberOfActionPoints);
        return this;
    }

    public boolean isSelected() {
        return getNodeFinder().from(unitBox).lookup(".selected").tryQuery()
                .isPresent();
    }

    public void verifySelected(final boolean expectedSelected) {
        assertEquals(expectedSelected, isSelected());
    }

}
