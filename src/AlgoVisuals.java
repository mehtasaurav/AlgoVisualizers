import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.util.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BaseClass extends Application {
    private static final int BOX_WIDTH = 300;
    private static final int BOX_HEIGHT = 300;
    private static final int MAX_HEIGHT = 200;
    private static final int PADDING = 30;
    private static final int SPACING = 5;
    private static final Color ORIGINAL_COLOR = Color.rgb(100, 4, 148);
    private static final Color HIGHLIGHT_COLOR = Color.RED;

    private VBox root;
    private int[] inputArray;
    private Map<String, Pane> algorithmPanes = new HashMap<>();
    private Map<String, List<Rectangle>> algorithmBars = new HashMap<>();

    @Override
    public void start(Stage stage) {
        TextField inputField = new TextField();
        inputField.setPromptText("Enter Numbers (e.g. 10,20,5)");

        Button generateButton = new Button("Generate");
        Button sortingButton = new Button("Start Sorting");

        HBox hb = new HBox(10);
        hb.getChildren().addAll(generateButton, sortingButton);
        root = new VBox(10);
        root.getChildren().addAll(inputField, hb);

        generateButton.setOnAction(e -> {
            String inputText = inputField.getText();
            if (!inputText.isEmpty()) {
                inputArray = Arrays.stream(inputText.split(","))
                                   .mapToInt(Integer::parseInt)
                                   .toArray();
                generateGraphs();
            }
        });

        sortingButton.setOnAction(e -> startSortingAnimation());

        Scene scene = new Scene(root, 650, 650);
        stage.setTitle("Sorting Algorithm Visualizer");
        stage.setScene(scene);
        stage.show();
    }

    private void generateGraphs() {
        TilePane tilePane = new TilePane();
        tilePane.setPrefColumns(2);
        tilePane.setHgap(10);
        tilePane.setVgap(10);

        String[] algorithms = {"Bubble Sort", "Selection Sort", "Insertion Sort", "Quick Sort"};
        for (String algorithm : algorithms) {
            Pane pane = createSortingBox(algorithm);
            algorithmPanes.put(algorithm, pane);
            tilePane.getChildren().add(pane);
        }

        if (root.getChildren().size() > 2) {
            root.getChildren().remove(2);
        }
        root.getChildren().add(tilePane);
    }

    private Pane createSortingBox(String algorithmName) {
        Pane box = new Pane();
        box.setPrefSize(BOX_WIDTH, BOX_HEIGHT);
        box.setStyle("-fx-border-color: black; -fx-padding: 10px;");
        Line xAxis = new Line(PADDING, BOX_HEIGHT - PADDING, BOX_WIDTH - PADDING, BOX_HEIGHT - PADDING);
        Line yAxis = new Line(PADDING, PADDING, PADDING, BOX_HEIGHT - PADDING);
        xAxis.setStroke(Color.BLACK);
        yAxis.setStroke(Color.BLACK);
        Text title = new Text(BOX_WIDTH / 2 - 40, 20, algorithmName);
        box.getChildren().addAll(xAxis, yAxis, title);

        int numBars = inputArray.length;
        int availableWidth = BOX_WIDTH - 2 * PADDING;
        int totalSpacing = (numBars - 1) * SPACING;
        int barWidth = (availableWidth - totalSpacing) / numBars;
        int xPosition = PADDING;
        int maxValue = Arrays.stream(inputArray).max().orElse(1);

        List<Rectangle> bars = new ArrayList<>();
        for (int value : inputArray) {
            int scaledHeight = (int) ((double) value / maxValue * MAX_HEIGHT);
            Rectangle bar = new Rectangle(barWidth, scaledHeight);
            bar.setFill(ORIGINAL_COLOR);
            bar.setX(xPosition);
            bar.setY(BOX_HEIGHT - PADDING - scaledHeight);
            bars.add(bar);
            box.getChildren().add(bar);
            xPosition += barWidth + SPACING;
        }

        algorithmBars.put(algorithmName, bars);
        return box;
    }

    private void startSortingAnimation() {
        startBackgroundThread("Bubble Sort");
        startBackgroundThread("Selection Sort");
        startBackgroundThread("Insertion Sort");
        startBackgroundThread("Quick Sort");
    }

    private void startBackgroundThread(String algorithmName) {
        Thread sortingThread = new Thread(() -> {
            sortWithAnimation(algorithmName);
        });
        sortingThread.setDaemon(true);
        sortingThread.start();
    }

    private void sortWithAnimation(String algorithmName) {
        switch (algorithmName) {
            case "Bubble Sort": {
                List<Trace> stackTrace = bubbleSort();
                animateSorting(algorithmName, stackTrace);
                break;
            }
            case "Selection Sort":
                // Add selection sort logic
                break;
            case "Insertion Sort":
                // Add insertion sort logic
                break;
            case "Quick Sort":
                // Add quick sort logic
                break;
        }
    }

    private List<Trace> bubbleSort() {
        List<Trace> trace = new ArrayList<>();
        int[] values = Arrays.copyOf(inputArray, inputArray.length);

        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values.length - 1 - i; j++) {
                if (values[j] > values[j + 1]) {
                    trace.add(new Trace(j, j + 1));
                    int temp = values[j];
                    values[j] = values[j + 1];
                    values[j + 1] = temp;
                }
            }
        }
        return trace;
    }

    private void animateSorting(String algorithmName, List<Trace> stackTrace) {
        Timeline timeline = new Timeline();
        List<Rectangle> bars = algorithmBars.get(algorithmName);

        for (int i = 0; i < stackTrace.size(); i++) {
            Trace trace = stackTrace.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(1000 * (i + 1)), e -> {
                Rectangle bar1 = bars.get(trace.index1);
                Rectangle bar2 = bars.get(trace.index2);
                bar1.setFill(HIGHLIGHT_COLOR);
                bar2.setFill(HIGHLIGHT_COLOR);
                swapBars(bars, trace.index1, trace.index2);
                PauseTransition pause = new PauseTransition(Duration.millis(900));
                pause.setOnFinished(event -> {
                    bar1.setFill(ORIGINAL_COLOR);
                    bar2.setFill(ORIGINAL_COLOR);
                });
                pause.play();
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
    }

    private void swapBars(List<Rectangle> bars, int i, int j) {
        Rectangle temp = bars.get(i);
        bars.set(i, bars.get(j));
        bars.set(j, temp);

        double tempX = bars.get(i).getX();
        bars.get(i).setX(bars.get(j).getX());
        bars.get(j).setX(tempX);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Trace {
    int index1;
    int index2;

    public Trace(int x, int y) {
        this.index1 = x;
        this.index2 = y;
    }
}