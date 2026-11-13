package org.akropia.gui.screen.colony;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.i18n.I18n;
import org.akropia.model.*;
import org.akropia.model.unit.UnitFreeColonist;
import com.google.common.eventbus.EventBus;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

/** Three independent teaching choices, validated by the shared colony model. */
final class SchoolPanel extends VBox {
    private final Colony colony;
    private final Model model;
    private final GameModelController game;
    private final I18n i18n;
    private final EventBus events;
    private final List<TeachingColumn> columns = new ArrayList<>();
    private final Label feedback = new Label();
    private boolean refreshing;

    SchoolPanel(Colony colony, GameModelController game, I18n i18n,
            EventBus events) {
        super(8);
        this.colony = colony;
        this.game = game;
        this.model = game.getModel();
        this.i18n = i18n;
        this.events = events;
        setId("schoolPanel");
        Label rule = new Label(i18n.get(EducationText.inlineRule));
        rule.setWrapText(true);
        HBox row = new HBox(16);
        for (int index = 0; index < 3; index++) {
            TeachingColumn column = new TeachingColumn(index);
            columns.add(column);
            row.getChildren().add(column);
            HBox.setHgrow(column, Priority.ALWAYS);
        }
        feedback.setId("schoolFeedback");
        feedback.setWrapText(true);
        getChildren().addAll(rule, row, feedback);
        refresh();
    }

    private boolean current() {
        return game.isGameModelReady() && game.getModel() == model
                && model.isGameRunning() && colony.isValid()
                && model.getCurrentPlayer() == colony.getOwner()
                && game.getHumanPlayer() == colony.getOwner();
    }

    private String unitName(Unit unit) {
        return unit == null ? i18n.get(EducationText.noTeaching)
                : "#" + unit.getId() + " "
                        + i18n.get(UnitTypes.getUnitName(unit.getType()));
    }

    private Unit resident(Integer id) {
        return id == null ? null
                : colony.getUnitsInsideColony().stream()
                        .filter(unit -> unit.getId() == id).findFirst()
                        .orElse(null);
    }

    void refresh() {
        refreshing = true;
        try {
            List<EducationCourse> courses = colony.getEducationCourses();
            List<Unit> teachers = colony.getUnitsInsideColony().stream()
                    .filter(unit -> !colony.getTeachingSkills(unit).isEmpty())
                    .collect(Collectors.toList());
            List<Unit> students = colony.getUnitsInsideColony().stream()
                    .filter(UnitFreeColonist.class::isInstance)
                    .collect(Collectors.toList());
            for (int i = 0; i < columns.size(); i++) {
                TeachingColumn column = columns.get(i);
                EducationCourse course = i < courses.size() ? courses.get(i)
                        : null;
                column.course = course;
                column.student.setDisable(course != null);
                column.teacher.getItems().setAll(teachers);
                column.student.getItems().setAll(students);
                column.teacher.setValue(course == null ? null
                        : resident(course.getTeacherId()));
                column.student.setValue(course == null ? null
                        : resident(course.getStudentId()));
                column.updateSkills();
                if (course != null) {
                    column.skill.getSelectionModel().select(course.getSkill());
                    column.progress.setText(i18n.get(EducationText.course,
                            course.getTeacherId() == null ? "—"
                                    : course.getTeacherId(),
                            course.getStudentId(),
                            i18n.get(UnitTypes.getUnitName(course.getSkill())),
                            course.getCompletedUpdates(),
                            course.getRequiredUpdates(),
                            i18n.get(course.isPaused() ? EducationText.paused
                                    : EducationText.active)));
                } else {
                    column.progress.setText("");
                }
            }
        } finally {
            refreshing = false;
        }
    }

    private final class TeachingColumn extends VBox {
        private final ComboBox<Unit> teacher = new ComboBox<>();
        private final ComboBox<Unit> student = new ComboBox<>();
        private final ComboBox<UnitType> skill = new ComboBox<>();
        private final Label progress = new Label();
        private EducationCourse course;
        private boolean applyScheduled;

        TeachingColumn(int index) {
            super(6);
            setMinWidth(0);
            setPrefWidth(300);
            teacher.setId("schoolTeacher-" + index);
            student.setId("schoolStudent-" + index);
            skill.setId("schoolSkill-" + index);
            progress.setId("schoolProgress-" + index);
            for (ComboBox<?> box : List.of(teacher, student, skill)) {
                box.setMaxWidth(Double.MAX_VALUE);
                box.setMinWidth(0);
                box.getStyleClass().add("paper-input");
            }
            StringConverter<Unit> names = new StringConverter<Unit>() {
                public String toString(Unit unit) {
                    return unitName(unit);
                }

                public Unit fromString(String value) {
                    throw new UnsupportedOperationException();
                }
            };
            teacher.setConverter(names);
            student.setConverter(names);
            skill.setConverter(new StringConverter<UnitType>() {
                public String toString(UnitType type) {
                    return type == null ? i18n.get(EducationText.noTeaching)
                            : i18n.get(UnitTypes.getUnitName(type));
                }

                public UnitType fromString(String value) {
                    throw new UnsupportedOperationException();
                }
            });
            skill.setCellFactory(list -> new ListCell<UnitType>() {
                @Override
                protected void updateItem(UnitType item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : skill.getConverter().toString(item));
                }
            });
            teacher.setPromptText(i18n.get(EducationText.teacher));
            student.setPromptText(i18n.get(EducationText.student));
            skill.setPromptText(i18n.get(EducationText.noTeaching));
            progress.setWrapText(true);
            for (var entry : List.of(
                    new Object[] { EducationText.teacher, teacher },
                    new Object[] { EducationText.student, student },
                    new Object[] { EducationText.skill, skill })) {
                Label label = new Label(i18n.get((EducationText) entry[0]));
                label.setLabelFor((ComboBox<?>) entry[1]);
                getChildren().addAll(label, (ComboBox<?>) entry[1]);
            }
            getChildren().add(progress);
            teacher.valueProperty()
                    .addListener((a, b, c) -> participantsChanged());
            student.valueProperty()
                    .addListener((a, b, c) -> participantsChanged());
            skill.setOnAction(event -> scheduleApply());
            skill.setOnHidden(event -> scheduleApply());
        }

        private void participantsChanged() {
            if (refreshing)
                return;
            refreshing = true;
            try {
                updateSkills();
            } finally {
                refreshing = false;
            }
            if (course == null)
                progress.setText(i18n.get(EducationText.none));
        }

        private void updateSkills() {
            skill.getItems().clear();
            skill.getItems().add(null);
            if (current() && teacher.getValue() != null
                    && student.getValue() != null) {
                colony.getEducationOptions(teacher.getValue(),
                        student.getValue()).stream()
                        .map(EducationOption::getSkill)
                        .forEach(skill.getItems()::add);
            }
            if (course != null && !skill.getItems().contains(course.getSkill()))
                skill.getItems().add(course.getSkill());
            skill.getSelectionModel().clearSelection();
        }

        private void scheduleApply() {
            if (refreshing || applyScheduled)
                return;
            applyScheduled = true;
            // Do not rebuild ComboBox items inside its popup selection event.
            javafx.application.Platform.runLater(() -> {
                applyScheduled = false;
                if (!skill.isShowing())
                    apply();
            });
        }

        private void apply() {
            if (refreshing)
                return;
            try {
                if (!current())
                    throw new IllegalStateException("School changed");
                if (skill.getValue() == null) {
                    if (course == null)
                        return;
                    Unit pupil = resident(course.getStudentId());
                    if (pupil == null)
                        throw new IllegalStateException("Student left");
                    colony.cancelEducation(pupil);
                } else {
                    if (course != null && course.getSkill() == skill.getValue()
                            && teacher.getValue() != null
                            && java.util.Objects.equals(course.getTeacherId(),
                                    teacher.getValue().getId())
                            && student.getValue() != null
                            && course.getStudentId() == student.getValue()
                                    .getId())
                        return;
                    colony.assignEducation(teacher.getValue(),
                            student.getValue(), skill.getValue());
                }
                feedback.setText("");
                events.post(new RepaintColonyEvent());
                refresh();
            } catch (IllegalArgumentException
                    | IllegalStateException rejected) {
                feedback.setText(i18n.get(EducationText.changed));
                refresh();
            }
        }
    }
}
