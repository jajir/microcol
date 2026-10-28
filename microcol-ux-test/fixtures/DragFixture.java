import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class DragFixture {
    public static void main(String[] args) {
        Application.launch(DragApplication.class, args);
    }

    public static final class DragApplication extends Application {
        @Override
        public void start(Stage stage) {
            final Label source = new Label("Drag source");
            final Label target = new Label("Drop target");
            source.relocate(30, 40);
            target.relocate(330, 40);
            source.setOnDragDetected(event -> {
                final var board = source.startDragAndDrop(TransferMode.MOVE);
                final var content = new ClipboardContent();
                content.putString("fixture");
                board.setContent(content);
                event.consume();
            });
            target.setOnDragOver(event -> {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            });
            target.setOnDragDropped(event -> {
                if (!"fixture".equals(event.getDragboard().getString())) {
                    throw new AssertionError("Unexpected drag payload");
                }
                event.setDropCompleted(true);
                System.out.println("DROPPED");
                event.consume();
            });
            stage.setScene(new Scene(new Pane(source, target), 500, 200));
            stage.setMinWidth(500);
            stage.setMinHeight(240);
            stage.show();
            // Openbox can reposition the window after show().
            final PauseTransition mapped = new PauseTransition(
                    Duration.millis(500));
            mapped.setOnFinished(event -> {
                final var from = source
                        .localToScreen(source.getBoundsInLocal());
                final var to = target.localToScreen(target.getBoundsInLocal());
                System.out.println("READY " + Math.round(from.getCenterX())
                        + " " + Math.round(from.getCenterY()) + " "
                        + Math.round(to.getCenterX()) + " "
                        + Math.round(to.getCenterY()));
            });
            mapped.play();
        }
    }
}
