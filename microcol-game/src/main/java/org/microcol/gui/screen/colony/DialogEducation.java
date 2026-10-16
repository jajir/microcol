package org.microcol.gui.screen.colony;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.*;
import org.microcol.model.unit.UnitFreeColonist;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

/** Preview model-approved teaching choices before assigning a resident. */
public final class DialogEducation extends AbstractMessageWindow {
    private final I18n i18n;
    private final GameModelController game;
    private final EventBus events;
    private final ComboBox<Unit> teachers = new ComboBox<>();
    private final ComboBox<Unit> students = new ComboBox<>();
    private final ComboBox<EducationOption> skills = new ComboBox<>();
    private final ListView<EducationCourse> courses = new ListView<>();
    private final Label title = new Label();
    private final Label rule = new Label();
    private final Label teacherLabel = new Label();
    private final Label studentLabel = new Label();
    private final Label skillLabel = new Label();
    private final Label preview = new Label();
    private final Label feedback = new Label();
    private final Button assign = new Button();
    private final Button cancelCourse = new Button();
    private final Button closeButton = new Button();
    private Model boundModel;
    private Colony colony;
    private Player owner;

    @Inject
    public DialogEducation(ViewUtil view, I18n i18n, GameModelController game,
            EventBus events) {
        super(view, i18n);
        this.i18n = i18n;
        this.game = game;
        this.events = events;
        teacherLabel.setLabelFor(teachers);
        studentLabel.setLabelFor(students);
        skillLabel.setLabelFor(skills);
        teachers.setId("educationTeacher");
        students.setId("educationStudent");
        skills.setId("educationSkill");
        courses.setId("educationCourses");
        preview.setId("educationPreview");
        feedback.setId("educationFeedback");
        assign.setId("educationAssign");
        cancelCourse.setId("educationCancelCourse");
        closeButton.setId("educationClose");
        for (Label label : new Label[] { rule, preview, feedback }) {
            label.setWrapText(true);
            label.setMinHeight(Region.USE_PREF_SIZE);
        }
        StringConverter<Unit> names = new StringConverter<Unit>() {
            public String toString(Unit unit) {
                return unit == null ? "" : unitName(unit);
            }

            public Unit fromString(String value) {
                throw new UnsupportedOperationException();
            }
        };
        teachers.setConverter(names);
        students.setConverter(names);
        skills.setConverter(new StringConverter<EducationOption>() {
            public String toString(EducationOption option) {
                return option == null ? "" : typeName(option.getSkill());
            }

            public EducationOption fromString(String value) {
                throw new UnsupportedOperationException();
            }
        });
        courses.setCellFactory(list -> new ListCell<EducationCourse>() {
            protected void updateItem(EducationCourse course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null : courseText(course));
                setWrapText(true);
            }
        });
        teachers.valueProperty().addListener((a, b, c) -> refreshOptions());
        students.valueProperty().addListener((a, b, c) -> refreshOptions());
        skills.valueProperty().addListener((a, b, c) -> refreshPreview());
        courses.getSelectionModel().selectedItemProperty()
                .addListener((a, b, c) -> {
                    cancelCourse.setDisable(c == null);
                });
        assign.setOnAction(event -> apply(false));
        cancelCourse.setOnAction(event -> apply(true));
        closeButton.setOnAction(event -> close());
        closeButton.setCancelButton(true);
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.addRow(0, teacherLabel, teachers);
        grid.addRow(1, studentLabel, students);
        grid.addRow(2, skillLabel, skills);
        teachers.setPrefWidth(420);
        students.setPrefWidth(420);
        skills.setPrefWidth(420);
        courses.setPrefHeight(130);
        VBox root = new VBox(10, title, rule, courses, cancelCourse, grid,
                preview, feedback, new HBox(12, assign, closeButton));
        root.setId("educationDialog");
        root.setPadding(new Insets(18));
        root.setPrefWidth(650);
        init(root);
    }

    public void showFor(Colony selected) {
        colony = selected;
        owner = colony.getOwner();
        boundModel = game.getModel();
        title.setText(i18n.get(EducationText.title, colony.getName()));
        setTitle(title.getText());
        rule.setText(i18n.get(EducationText.rule));
        teacherLabel.setText(i18n.get(EducationText.teacher));
        studentLabel.setText(i18n.get(EducationText.student));
        skillLabel.setText(i18n.get(EducationText.skill));
        assign.setText(i18n.get(EducationText.assign));
        cancelCourse.setText(i18n.get(EducationText.cancelCourse));
        closeButton.setText(i18n.get(EducationText.close));
        feedback.setText("");
        refresh();
        showAndWait();
    }

    private String typeName(UnitType type) {
        return i18n.get(UnitTypes.getUnitName(type));
    }

    private String unitName(Unit unit) {
        return "#" + unit.getId() + " — " + typeName(unit.getType())
                + (unit instanceof UnitFreeColonist
                        && ((UnitFreeColonist) unit).isVeteran()
                                ? " (" + i18n.get(EducationText.veteran) + ")"
                                : "");
    }

    private String courseText(EducationCourse course) {
        return i18n.get(EducationText.course,
                course.getTeacherId() == null ? "—" : course.getTeacherId(),
                course.getStudentId(), typeName(course.getSkill()),
                course.getCompletedUpdates(), course.getRequiredUpdates(),
                i18n.get(course.isPaused() ? EducationText.paused
                        : EducationText.active));
    }

    private boolean isCurrent() {
        return game.isGameModelReady() && game.getModel() == boundModel
                && boundModel.isGameRunning() && colony.isValid()
                && colony.getOwner() == owner
                && boundModel.getCurrentPlayer() == owner
                && game.getHumanPlayer() == owner;
    }

    private void refresh() {
        teachers.getItems()
                .setAll(colony.getUnitsInsideColony().stream().filter(
                        unit -> !colony.getTeachingSkills(unit).isEmpty())
                        .collect(java.util.stream.Collectors.toList()));
        students.getItems()
                .setAll(colony.getUnitsInsideColony().stream()
                        .filter(unit -> unit instanceof UnitFreeColonist)
                        .collect(java.util.stream.Collectors.toList()));
        courses.getItems().setAll(colony.getEducationCourses());
        cancelCourse.setDisable(true);
        refreshOptions();
    }

    private void refreshOptions() {
        skills.getItems().clear();
        if (colony != null && isCurrent() && teachers.getValue() != null
                && students.getValue() != null) {
            skills.getItems().setAll(colony.getEducationOptions(
                    teachers.getValue(), students.getValue()));
            if (!skills.getItems().isEmpty())
                skills.getSelectionModel().selectFirst();
        }
        refreshPreview();
    }

    private void refreshPreview() {
        EducationOption option = skills.getValue();
        assign.setDisable(option == null || !isCurrent());
        preview.setText(option == null ? i18n.get(EducationText.none)
                : i18n.get(EducationText.preview,
                        i18n.get(new ConstructionTypeName[] {
                                ConstructionTypeName.schoolhouse,
                                ConstructionTypeName.college,
                                ConstructionTypeName.university }[option
                                        .getRequiredSchoolTier() - 1]),
                        option.getRequiredUpdates(),
                        option.willBecomeVeteran()
                                ? i18n.get(EducationText.veteran)
                                : typeName(option.getNextType())));
    }

    private void apply(boolean cancel) {
        try {
            if (!isCurrent())
                throw new IllegalStateException("Education view changed");
            if (cancel) {
                EducationCourse selected = courses.getSelectionModel()
                        .getSelectedItem();
                if (selected == null)
                    return;
                Unit student = colony.getUnitsInsideColony().stream()
                        .filter(unit -> unit.getId() == selected.getStudentId())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Student left"));
                colony.cancelEducation(student);
            } else {
                EducationOption option = skills.getValue();
                if (option == null)
                    return;
                colony.assignEducation(teachers.getValue(), students.getValue(),
                        option.getSkill());
            }
            events.post(new RepaintColonyEvent());
            feedback.setText("");
            refresh();
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            feedback.setText(i18n.get(EducationText.changed));
            assign.setDisable(true);
            cancelCourse.setDisable(true);
        }
    }
}
