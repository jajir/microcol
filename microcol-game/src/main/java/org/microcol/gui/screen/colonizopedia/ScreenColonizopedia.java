package org.microcol.gui.screen.colonizopedia;

import org.microcol.gui.Loc;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.InGamePage;
import org.microcol.gui.util.ButtonBarOk;
import com.google.common.eventbus.EventBus;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Rules help with a searchable topic list. Showing it reads reference
 * definitions and never mutates the game.
 */
public final class ScreenColonizopedia implements GameScreen {

    public static final String SEARCH_ID = "colonizopediaSearch";

    public static final String TOPICS_ID = "colonizopediaTopics";

    public static final String BODY_ID = "colonizopediaBody";

    public static final String BODY_SCROLL_ID = "colonizopediaBodyScroll";

    public static final String NO_RESULTS_ID = "colonizopediaNoResults";

    public static final String TITLE_ID = "colonizopediaTitle";

    private final I18n i18n;
    private final InGamePage page;

    private final ButtonBarOk buttonBar;

    private final Label title = new Label();

    private final TextField search = new TextField();

    private final FilteredList<ColonizopediaTopic> topics = new FilteredList<>(
            FXCollections.observableArrayList(ColonizopediaTopic.values()),
            topic -> true);

    private final ListView<ColonizopediaTopic> topicList = new ListView<>(
            topics);

    private final Label body = new Label();

    private final Label topicTitle = new Label();

    private final VBox bodyContent = new VBox(12, topicTitle, body);
    private final ScrollPane bodyScroll = new ScrollPane(bodyContent);
    private final ColonizopediaTables tables;

    private final Label noResults = new Label();

    @Inject
    public ScreenColonizopedia(final I18n i18n, final EventBus eventBus,
            final ImageProvider images, final InGamePage page) {
        this.i18n = i18n;
        this.page = page;
        tables = new ColonizopediaTables(i18n, images);
        buttonBar = new ButtonBarOk(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            eventBus.post(new ShowScreenEvent(Screen.GAME));
        });

        search.setId(SEARCH_ID);
        search.textProperty()
                .addListener((observable, oldText, newText) -> applyFilter());

        topicList.setId(TOPICS_ID);
        topicList.setPrefSize(220, 340);
        topicList.setMinWidth(220);
        topicList.setMinHeight(0);
        topicList.setCellFactory(list -> new ListCell<ColonizopediaTopic>() {

            @Override
            protected void updateItem(final ColonizopediaTopic topic,
                    final boolean empty) {
                super.updateItem(topic, empty);
                setText(empty || topic == null ? null : topic.getTitle(i18n));
            }

        });
        topicList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTopic, newTopic) -> showBody(newTopic));

        body.setId(BODY_ID);
        body.setWrapText(true);
        topicTitle.getStyleClass().add("help-topic-title");
        topicTitle.setWrapText(true);
        topicTitle.setMaxWidth(Double.MAX_VALUE);
        bodyContent.getStyleClass().add("help-article");
        bodyScroll.setId(BODY_SCROLL_ID);
        bodyScroll.setFitToWidth(true);
        bodyScroll.setMinSize(0, 0);
        bodyScroll.setPrefViewportWidth(540);
        bodyScroll.setPrefViewportHeight(340);

        noResults.setId(NO_RESULTS_ID);
        noResults.setWrapText(true);
        noResults.setPrefWidth(540);

        final VBox rightColumn = new VBox(10, noResults, bodyScroll);
        rightColumn.setMinWidth(0);
        VBox.setVgrow(bodyScroll, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        final VBox leftColumn = new VBox(10, search, topicList);
        leftColumn.setMinSize(220, 0);
        leftColumn.setPrefWidth(220);
        leftColumn.setMaxWidth(220);
        VBox.setVgrow(topicList, Priority.ALWAYS);
        final HBox root = new HBox(20, leftColumn, rightColumn);
        root.setMinHeight(0);
        VBox.setVgrow(root, Priority.ALWAYS);
        // Handle Escape before the focused list consumes it.
        page.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                eventBus.post(new ShowScreenEvent(Screen.GAME));
                event.consume();
            }
        });
        title.setId(TITLE_ID);
        page.setTitle(title);
        page.setToolbar(buttonBar.getContent());
        page.setContent(root);
        page.getContent().setId("colonizopediaPage");
        page.getContent().getStylesheets().add(ScreenColonizopedia.class
                .getResource("/gui/Colonizopedia.css").toExternalForm());
        // ScrollPane attaches its content only after its skin is created.
        topicList.sceneProperty()
                .addListener((observable, oldScene, newScene) -> {
                    if (newScene != null) {
                        Platform.runLater(topicList::requestFocus);
                    }
                });

        topicList.getSelectionModel().selectFirst();
        applyFilter();
    }

    /**
     * Each opening starts with an empty search and keyboard focus on the topic
     * list, so arrow keys and Escape work without a mouse.
     */
    @Override
    public void beforeShow() {
        page.beforeShow();
        search.clear();
    }

    @Override
    public void beforeHide() {
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        title.setText(i18n.get(Colonizopedia.title));
        search.setPromptText(i18n.get(Colonizopedia.searchPrompt));
        noResults.setText(i18n.get(Colonizopedia.noResults));
        buttonBar.setButtonText(i18n.get(Loc.ok));
        topicList.refresh();
        showBody(topicList.getSelectionModel().getSelectedItem());
    }

    private void applyFilter() {
        final String query = search.getText();
        topics.setPredicate(topic -> topic.matches(i18n, query));
        final boolean found = !topics.isEmpty();
        noResults.setVisible(!found);
        noResults.setManaged(!found);
        if (found && topicList.getSelectionModel().isEmpty()) {
            topicList.getSelectionModel().selectFirst();
        }
    }

    private void showBody(final ColonizopediaTopic topic) {
        body.setText(topic == null ? "" : topic.getBody(i18n));
        topicTitle.setText(topic == null ? "" : topic.getTitle(i18n));
        topicTitle.setVisible(topic != null);
        topicTitle.setManaged(topic != null);
        bodyContent.getChildren().setAll(topicTitle, body);
        if (topic == ColonizopediaTopic.TERRAIN
                || topic == ColonizopediaTopic.RESOURCES
                || topic == ColonizopediaTopic.UNITS) {
            bodyContent.getChildren().add(tables.create(topic));
        }
        bodyScroll.setVvalue(0);
    }

}
