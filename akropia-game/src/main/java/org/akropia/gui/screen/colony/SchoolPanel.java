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
    private final org.akropia.gui.image.ImageProvider images;
    private final List<TeachingColumn> columns = new ArrayList<>();
    private final Label feedback = new Label();
    private boolean refreshing;

    SchoolPanel(Colony colony, GameModelController game, I18n i18n,
            EventBus events, org.akropia.gui.image.ImageProvider images) {
        super(8);
        this.colony = colony;
        this.game = game;
        this.model = game.getModel();
        this.i18n = i18n;
        this.events = events;
        this.images = images;
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
        return unit == null ? ""
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
            Construction school = colony.getConstructions().stream()
                    .filter(building -> List
                            .of(ConstructionType.SCHOOLHOUSE,
                                    ConstructionType.COLLEGE,
                                    ConstructionType.UNIVERSITY)
                            .contains(building.getType()))
                    .findFirst().orElse(null);
            List<Unit> students = colony.getUnitsInsideColony().stream()
                    .filter(UnitFreeColonist.class::isInstance)
                    .collect(Collectors.toList());
            for (int i = 0; i < columns.size(); i++) {
                TeachingColumn column = columns.get(i);
                column.teacher = school == null
                        || school.getConstructionSlots().get(i).isEmpty() ? null
                                : school.getConstructionSlots().get(i)
                                        .getUnit();
                EducationCourse course = column.teacher == null ? null
                        : courses.stream()
                                .filter(item -> java.util.Objects.equals(
                                        item.getTeacherId(),
                                        column.teacher.getId()))
                                .findFirst().orElse(null);
                column.course = course;
                column.teacherPortrait.setImage(column.teacher == null ? null
                        : images.getUnitImage(column.teacher));
                column.teacherPortrait
                        .setAccessibleText(unitName(column.teacher));
                column.teacherName.setText(column.teacher == null
                        ? i18n.get(EducationText.noTeacher)
                        : unitName(column.teacher));
                column.choices.setVisible(column.teacher != null);
                column.choices.setManaged(column.teacher != null);
                column.student.setDisable(course != null);
                column.student.getItems().setAll(students);
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
        private Unit teacher;
        private final javafx.scene.image.ImageView teacherPortrait = new javafx.scene.image.ImageView();
        private final Label teacherName = new Label();
        private final VBox choices = new VBox(6);
        private final ComboBox<Unit> student = new ComboBox<>();
        private final ComboBox<UnitType> skill = new ComboBox<>();
        private final Label progress = new Label();
        private EducationCourse course;
        private boolean applyScheduled;

        TeachingColumn(int index) {
            super(6);
            setMinWidth(0);
            setPrefWidth(300);
            teacherPortrait.setId("schoolTeacher-" + index);
            teacherPortrait.setFitWidth(45);
            teacherPortrait.setFitHeight(45);
            teacherPortrait.setPreserveRatio(true);
            teacherName.setWrapText(true);
            teacherName.setId("schoolTeacherName-" + index);
            getChildren().addAll(new HBox(8, teacherPortrait, teacherName),
                    choices);
            student.setId("schoolStudent-" + index);
            skill.setId("schoolSkill-" + index);
            progress.setId("schoolProgress-" + index);
            for (ComboBox<?> box : List.of(student, skill)) {
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
            skill.setPromptText(i18n.get(EducationText.noTeaching));
            progress.setWrapText(true);
            for (var entry : List.of(
                    new Object[] { EducationText.student, student },
                    new Object[] { EducationText.skill, skill })) {
                Label label = new Label(i18n.get((EducationText) entry[0]));
                label.setLabelFor((ComboBox<?>) entry[1]);
                choices.getChildren().addAll(label, (ComboBox<?>) entry[1]);
            }
            getChildren().add(progress);
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
            if (current() && teacher != null && student.getValue() != null) {
                colony.getEducationOptions(teacher, student.getValue()).stream()
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
                            && teacher != null
                            && java.util.Objects.equals(course.getTeacherId(),
                                    teacher.getId())
                            && student.getValue() != null
                            && course.getStudentId() == student.getValue()
                                    .getId())
                        return;
                    colony.assignEducation(teacher, student.getValue(),
                            skill.getValue());
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
