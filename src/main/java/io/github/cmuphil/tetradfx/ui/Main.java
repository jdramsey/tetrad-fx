package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.algcomparison.graph.RandomForward;
import edu.cmu.tetrad.algcomparison.simulation.BayesNetSimulation;
import edu.cmu.tetrad.algcomparison.simulation.LeeHastieSimulation;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.graph.RandomGraph;
import edu.cmu.tetrad.sem.LargeScaleSimulation;
import edu.cmu.tetrad.util.Parameters;
import edu.cmu.tetrad.util.RandomUtil;
import edu.pitt.dbmi.data.reader.Delimiter;
import io.github.cmuphil.tetradfx.for751lib.ChangedStuffINeed;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * <p>The main display for Tetrad-FX. This one uses a split pane layout. Work in progress.</p>
 *
 * @author josephramsey
 */
public class Main {
    private static final Main INSTANCE = new Main();

    public static Main getInstance() {
        return Main.INSTANCE;
    }

    // Passing primaryStage in here so that I can quit the application from a menu item
    // and pop up dialogs.
    public Pane getRoot(Stage primaryStage) {
        BorderPane activePane = NamesToContents.getInstance().getActivePane();
        MenuBar menuBar = getMenuBar(primaryStage);
        activePane.setTop(menuBar);
        activePane.setPrefSize(1000, 800);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setDividerPosition(0, 0.2);

        SplitPane leftSplit = new SplitPane();
        leftSplit.setOrientation(Orientation.VERTICAL);
        leftSplit.setDividerPosition(0, 0.5);
        leftSplit.getItems().addAll(NamesToContents.getInstance().getDataTreeView(),
                new TextArea("Parameters:\n" + new Parameters()));

        mainSplit.getItems().addAll(leftSplit, activePane);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mainSplit);
        borderPane.setTop(menuBar);

        StackPane root = new StackPane();
        root.getChildren().add(borderPane);

        return root;
    }

    // This will eventually be replaced by some flexible UI for making simulations (or eliminated).
    @NotNull
    private static Result getSimulation(Parameters parameters, SimulationType type) {
        if (type == SimulationType.CONTINUOUS) {
            Graph graph = RandomGraph.randomGraphRandomForwardEdges(100, 0,
                    200, 500, 100, 1000, false);
            LargeScaleSimulation simulation = new LargeScaleSimulation(graph);
            simulation.setCoefRange(0, 0.5);
            simulation.setSelfLoopCoef(0.1);
            DataSet dataSet = simulation.simulateDataReducedForm(1000);
            return new Result(graph, dataSet);
        } else if (type == SimulationType.DISCRETE) {
            BayesNetSimulation simulation = new BayesNetSimulation(new RandomForward());
            simulation.createData(parameters, true);
            Graph graph = simulation.getTrueGraph(0);
            DataSet dataSet = (DataSet) simulation.getDataModel(0);
            return new Result(graph, dataSet);
        } else {
            LeeHastieSimulation simulation = new LeeHastieSimulation(new RandomForward());
            simulation.createData(parameters, true);
            Graph graph = simulation.getTrueGraph(0);
            DataSet dataSet = (DataSet) simulation.getDataModel(0);
            return new Result(graph, dataSet);
        }
    }

    private void loadDataAction() {
        ButtonType applyButtonType = new ButtonType("Load");
        RadioButton continuousBtn = new RadioButton("Optimize for Continuous");
        RadioButton discreteBtn = new RadioButton("Optimize for Discrete");
        RadioButton mixedBtn = new RadioButton("General");
        ToggleGroup toggleGroup = new ToggleGroup();
        continuousBtn.setToggleGroup(toggleGroup);
        discreteBtn.setToggleGroup(toggleGroup);
        mixedBtn.setToggleGroup(toggleGroup);
        mixedBtn.setSelected(true);
        TextField textField = new TextField("3");
        textField.setPrefColumnCount(2);

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\D")) {
                textField.setText(newValue.replaceAll("\\D", ""));
            }
        });

        HBox choice = new HBox(10, mixedBtn, new Label("(Max Categories"), textField, new Label(")"),
                continuousBtn, discreteBtn);
        VBox layout = new VBox(10, choice);

        Dialog<VBox> dialog = new Dialog<>();
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, applyButtonType);

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(layout.getScene().getWindow());

        ((Button) dialog.getDialogPane().lookupButton(applyButtonType)).setOnAction(e ->
                loadTheData(selectedFile, continuousBtn, discreteBtn, textField));

        dialog.showAndWait();
    }

    private void loadGraphAction(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        Graph graph = GraphSaveLoadUtils.loadGraphTxt(selectedFile);
        NamesToContents.getInstance().add(null, graph,
                NamesToContents.getInstance().nextName(selectedFile.getName()),
                null, "Graph");
    }

    @NotNull
    public MenuBar getMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(new MenuItem("Load Session"));
        fileMenu.getItems().add(new MenuItem("Save Session"));
        fileMenu.getItems().add(new SeparatorMenuItem());
        MenuItem loadData = new MenuItem("Load Data");
        MenuItem loadGraph = new MenuItem("Load Graph");
        Menu simulation = new Menu("Simulation");
        MenuItem continuousSimulation = new MenuItem("Continuous");
        MenuItem discreteSimulation = new MenuItem("Discrete");
        MenuItem mixedSimulation = new MenuItem("Mixed");
        simulation.getItems().addAll(continuousSimulation, discreteSimulation, mixedSimulation);
        fileMenu.getItems().add(new SeparatorMenuItem());
        MenuItem exitItem = new MenuItem("Exit");

        loadData.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));
        loadGraph.setAccelerator(KeyCombination.keyCombination("Ctrl+G"));
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));

        loadData.setOnAction(e -> loadDataAction());
        loadGraph.setOnAction(e -> loadGraphAction(primaryStage));
        continuousSimulation.setOnAction(e -> addSimulation(SimulationType.CONTINUOUS));
        discreteSimulation.setOnAction(e -> addSimulation(SimulationType.DISCRETE));
        mixedSimulation.setOnAction(e -> addSimulation(SimulationType.MIXED));
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(loadData, loadGraph, simulation, new SeparatorMenuItem(), exitItem);

        Menu search = new Menu("Search");
        search.getItems().add(new Menu("Do a search using the selected dataset"));
        search.getItems().add(new Menu("Do an search using the selected graph as an oracle"));

        Menu insights = new Menu("Insights");
        Menu histogramsAndScatterplots = new Menu("Histograms and scatterplots");
        insights.getItems().add(histogramsAndScatterplots);
        histogramsAndScatterplots.getItems().add(new MenuItem("Plot Matrix"));
        histogramsAndScatterplots.getItems().add(new MenuItem("By Edge/Node"));
        insights.getItems().add(new MenuItem("Graph Metrics"));
        insights.getItems().add(new MenuItem("Data Metrics"));
        insights.getItems().add(new MenuItem("Causal Effects"));
        insights.getItems().add(new MenuItem("Check Markov and Faithfulness Assumptions"));
        insights.getItems().add(new MenuItem("Check for D-separation/M-separation"));

        Menu layout = new Menu("Layout");
        layout.getItems().add(new Menu("Do a layout"));

        Menu games = new Menu("Games");

        MenuItem basedOnGraph_4_4 =  new MenuItem("Make a random game with 4 nodes and 4 edges");
        MenuItem basedOnGraph_5_5 =  new MenuItem("Make a random game with 5 nodes and 5 edges");
        MenuItem basedOnGraph_6_6 =  new MenuItem("Make a random game with 6 nodes and 6 edges");
        MenuItem basedOnGraph_7_7 =  new MenuItem("Make a random game with 7 nodes and 7 edges");
        MenuItem basedOnGraph_10_10 =  new MenuItem("Make a random game with 10 nodes and 10 edges");
        MenuItem basedOnGraph_10_15 =  new MenuItem("Make a random game with 10 nodes and 15 edges");
        MenuItem surpriseMe =  new MenuItem("Surprise me!");

        basedOnGraph_4_4.setOnAction(e -> Games.baseGamesOnGraph(randomDag(4, 4)));
        basedOnGraph_5_5.setOnAction(e -> Games.baseGamesOnGraph(randomDag(5, 5)));
        basedOnGraph_6_6.setOnAction(e -> Games.baseGamesOnGraph(randomDag(6, 6)));
        basedOnGraph_7_7.setOnAction(e -> Games.baseGamesOnGraph(randomDag(7, 7)));
        basedOnGraph_10_10.setOnAction(e -> Games.baseGamesOnGraph(randomDag(10, 10)));
        basedOnGraph_10_15.setOnAction(e -> Games.baseGamesOnGraph(randomDag(10, 15)));
        surpriseMe.setOnAction(e -> Games.baseGamesOnGraph(randomDag(RandomUtil.getInstance().nextInt(6) + 5,
                RandomUtil.getInstance().nextInt(10) + 3)));

        games.getItems().addAll(basedOnGraph_4_4, basedOnGraph_5_5, basedOnGraph_6_6, basedOnGraph_7_7,
                basedOnGraph_10_10, basedOnGraph_10_15, surpriseMe);

        Menu help = new Menu("Help");
        help.getItems().add(new MenuItem("About"));
        help.getItems().add(new MenuItem("Help"));
        help.getItems().add(new MenuItem("Tetrad Website"));
        help.getItems().add(new MenuItem("Tetrad Manual"));
        help.getItems().add(new MenuItem("Tetrad Forum"));

        menuBar.getMenus().addAll(fileMenu, search, insights, layout, games, help);
        return menuBar;
    }

    @NotNull
    private static Graph randomDag(int numNodes, int numEdges) {
        return RandomGraph.randomGraph(numNodes, 0, numEdges, 100, 100,
                100, false);
    }

    private void loadTheData(File selectedFile, RadioButton continuousBtn, RadioButton discreteBtn,
                             TextField textField) {
        if (selectedFile != null) {

            // You can add further processing based on the type of dataset chosen.
            if (continuousBtn.isSelected()) {
                loadContinuous(selectedFile);
            } else if (discreteBtn.isSelected()) {
                loadDiscrete(selectedFile);
            } else {
                loadMixed(selectedFile, textField);
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    private void loadContinuous(File selectedFile) {
        try {
            DataSet dataSet = ChangedStuffINeed.loadContinuousData(selectedFile, "//", '\"',
                    "*", true, Delimiter.TAB, false);
            String name = selectedFile.getName();
            dataSet.setName(name);
            NamesToContents.getInstance().add(dataSet, null, NamesToContents.getInstance().nextName(selectedFile.getName()),
                    "Data", null);
        } catch (IOException ex) {
            System.out.println("Error loading continuous data.");
            throw new RuntimeException(ex);
        }
    }

    private void loadDiscrete(File selectedFile) {
        try {
            DataSet dataSet = ChangedStuffINeed.loadDiscreteData(selectedFile, "//",
                    '\"', "*", true, Delimiter.TAB, false);
            NamesToContents.getInstance().add(dataSet, null, NamesToContents.getInstance().nextName(selectedFile.getName()),
                    "Data", null);
        } catch (IOException ex) {
            System.out.println("Error loading discrete data.");
            throw new RuntimeException(ex);
        }
    }

    private void loadMixed(File selectedFile, TextField textField) {
        try {
            int maxNumCategories = Integer.parseInt(textField.getText());
            DataSet dataSet = ChangedStuffINeed.loadMixedData(selectedFile, "//", '\"',
                    "*", true, maxNumCategories, Delimiter.TAB, false);
            NamesToContents.getInstance().add(dataSet, null, NamesToContents.getInstance().nextName(selectedFile.getName()),
                    "Data", null);
        } catch (IOException ex) {
            System.out.println("Error loading mixed data.");
            throw new RuntimeException(ex);
        }
    }

    private void addSimulation(SimulationType type) {
        Result result = getSimulation(new Parameters(), type);
        NamesToContents.getInstance().add(result.dataSet(), result.graph(), NamesToContents.getInstance().nextName("Simulation"),
                "simulated_data", "simulated_graph");
    }

    public enum SimulationType {
        CONTINUOUS,
        DISCRETE,
        MIXED
    }

    private record Result(Graph graph, DataSet dataSet) {
    }
}

