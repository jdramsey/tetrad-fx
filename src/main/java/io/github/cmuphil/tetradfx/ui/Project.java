package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataWriter;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphSaveLoadUtils;
import edu.cmu.tetrad.util.Parameters;
import io.github.cmuphil.tetradfx.utils.Utils;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * <p>Stores all of the tabbed panes for a given project in a session.</p>
 *
 * @author josephramsey
 */
public class Project {

    private final Tab dataTab;
    private final Tab valenceTab;
    private final Tab graphTab;
    private final Tab searchTab;
    private final Tab knowledgeTab;
    private final Tab gamesTab;

    private final TextArea parametersArea = new TextArea("");
    private final TextArea notesArea = new TextArea("");

    private final TabPane sessionTabPane;
    private final TabPane data = new TabPane();
    private final TabPane valence = new TabPane();
    private final TabPane graphs = new TabPane();
    private final TabPane search = new TabPane();
    private final TabPane knowledge = new TabPane();
    private final TabPane games = new TabPane();

    private final TreeItem<String> treeItem;

    private final File dataDir;
    private final File graphDir;
    private final File searchDir;
    private final File knowledgeDir;

    private final Map<Tab, String> tabsToParameters = new HashMap<>();
    private final Map<Tab, String> tabsToNotes = new HashMap<>();

    private final Map<TableView<DataView.DataRow>, DataSet> dataSetMap = new HashMap<>();
    private boolean valenceAdded = false;

    /**
     * Creates a new project.
     *
     * @param dataSet     The dataset to add to the session. (This may be null.)
     * @param graph       The graph to add to the session. (This may be null.)
     * @param projectName The name of the project.
     * @param dataName    The name of the dataset. (This may be null.)
     * @param graphName   The name of the graph. (This may be null.)
     * @param dir         The directory to save the project in.
     */
    public Project(DataSet dataSet, Graph graph, String projectName, String dataName, String graphName, File dir) {
        this.treeItem = new TreeItem<>(projectName);

        this.sessionTabPane = new TabPane();
        this.sessionTabPane.setPrefSize(1000, 800);
        this.sessionTabPane.setSide(Side.LEFT);

        // Set the various tabs in the session tab pane.
        dataTab = new Tab("Data", data);
        dataDir = new File(dir, "data");

        if (!dataDir.exists()) {
            boolean made2 = dataDir.mkdir();

            if (!made2) {
                throw new IllegalArgumentException("Could not make directory " + dataDir.getPath());
            }
        }

        valenceTab = new Tab("Valence", valence);

        searchTab = new Tab("Search", search);
        searchDir = new File(dir, "search_graphs");

        if (!searchDir.exists()) {
            boolean made = searchDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + searchDir.getPath());
            }
        }

        knowledgeTab = new Tab("Knowledge", knowledge);
        knowledgeDir = new File(dir, "knowledge");

        if (!knowledgeDir.exists()) {
            boolean made = knowledgeDir.mkdir();

            if (!made) {
                throw new IllegalArgumentException("Could not make directory " + knowledgeDir.getPath());
            }
        }

        graphTab = new Tab("Other Graphs", graphs);
        graphDir = new File(dir, "other_graphs");

        if (!graphDir.exists()) {
            boolean made1 = graphDir.mkdir();

            if (!made1) {
                throw new IllegalArgumentException("Could not make directory " + graphDir.getPath());
            }
        }

        gamesTab = new Tab("Games", games);

        notesArea.setWrapText(true);
        parametersArea.setWrapText(true);

        if (graph != null) {
            addGraph(graphName, graph, true, false);
        }

        if (dataSet != null) {
            addDataSet(dataName, dataSet, false, false);
        }

        setUpTabPane(sessionTabPane, dataTab, data);
        setUpTabPane(sessionTabPane, valenceTab, valence);
        setUpTabPane(sessionTabPane, searchTab, search);
        setUpTabPane(sessionTabPane, knowledgeTab, knowledge);
        setUpTabPane(sessionTabPane, graphTab, graphs);
        setUpTabPane(sessionTabPane, gamesTab, games);

        setParametersAndNotesText();

        displayNonemptyTabsOnly();
        selectIfNonempty(dataTab);
    }

    /**
     * Returns the main tab pane for this project.
     *
     * @return The main tab pane.
     */
    public TabPane getSessionTabPane() {
        return sessionTabPane;
    }

    /**
     * Adds a dataset to the data tab.
     *
     * @param name     The name of the dataset.
     * @param dataSet  The dataset.
     * @param nextName Whether to append a number to the name if it already exists.
     * @param closable Whether the tab should be closable.
     */
    public void addDataSet(String name, DataSet dataSet, boolean nextName, boolean closable) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getDataNames());
        }

        TableView<DataView.DataRow> tableView = DataView.getTableView(dataSet);
        dataSetMap.put(tableView, dataSet);
        Tab tab = new Tab(name, tableView);
        tab.setClosable(closable);
        this.data.getTabs().add(tab);
        this.sessionTabPane.getSelectionModel().select(dataTab);
        this.data.getSelectionModel().select(tab);
        tabsToParameters.put(tab, "");
        tabsToNotes.put(tab, "");
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        File file = new File(dataDir, name.replace(' ', '_') + ".txt");

        try {
            try (PrintWriter writer = new PrintWriter(file)) {
                DataWriter.writeRectangularData(dataSet, writer, '\t');
            }
        } catch (IOException e) {
            System.out.println("Could not write data set to file");
        }

        if (!valenceAdded) {
            Tab valence = new Tab("Variables", new VariablesView(dataSet).getTableView());
            valence.setClosable(closable);
            this.valence.getTabs().add(valence);
            valenceAdded = true;
        }

        tab.setOnClosed(event -> {
            displayNonemptyTabsOnly();

            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });

        readNotes(tab, dataDir, name);
        persistNotes(tab, dataDir, name);

        displayNonemptyTabsOnly();
        selectIfNonempty(dataTab);
    }

    /**
     * Adds a graph to the graph tab.
     * @param name The name of the graph.
     * @param graph The graph.
     * @param nextName Whether to append a number to the name if it already exists.
     * @param closable Whether the tab should be closable.
     */
    public void addGraph(String name, Graph graph, boolean nextName, boolean closable) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getGraphNames());
        }

        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        tab.setClosable(closable);
        this.graphs.getTabs().add(tab);
        this.sessionTabPane.getSelectionModel().select(graphTab);
        this.graphs.getSelectionModel().select(tab);
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        var _name = name.replace(' ', '_') + ".txt";
        var file = new File(graphDir, _name);
        GraphSaveLoadUtils.saveGraph(graph , file, false);

        tab.setOnClosed(event -> {
            displayNonemptyTabsOnly();
            selectIfNonempty(graphTab);

            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });

        readNotes(tab, graphDir, name);
        persistNotes(tab, graphDir, name);

        displayNonemptyTabsOnly();
        selectIfNonempty(graphTab);
    }

    /**
     * Adds a search result to the search tab.
     * @param name The name of the search result.
     * @param graph The graph.
     * @param closable Whether the tab should be closable.
     * @param nextName Whether to append a number to the name if it already exists.
     * @param parameters The parameters used to generate the search result.
     * @param usedParameters The parameters that were actually used to generate the search result.
     */
    public void addSearchResult(String name, Graph graph, boolean closable, boolean nextName, Parameters parameters,
                                List<String> usedParameters) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getSearchNames());
        }

        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        tab.setClosable(closable);
        this.search.getTabs().add(tab);
        this.sessionTabPane.getSelectionModel().select(searchTab);
        this.search.getSelectionModel().select(tab);
        tabsToParameters.put(tab, "");
        tabsToNotes.put(tab, "");
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        var _name = name.replace(' ', '_') + ".txt";
        var file = new File(searchDir, _name);
        GraphSaveLoadUtils.saveGraph(graph , file, false);

        tab.setOnClosed(event -> {
            displayNonemptyTabsOnly();
            selectIfNonempty(searchTab);

            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });

        setParametersText(tab, parameters, usedParameters);
        readNotes(tab, searchDir, name);
        persistNotes(tab, searchDir, name);

        displayNonemptyTabsOnly();
        selectIfNonempty(searchTab);
    }

    public void addKnowledge(String name, Knowledge knowledge, boolean closable, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getKnowledgeNames());
        }

//        Tab tab = new Tab(name, new TextArea(knowledge.toString()));


        Tab tab = new Tab(name, new RegexKnowledgeEditor(knowledge).makeRegexFilter());
        tab.setClosable(closable);
        this.knowledge.getTabs().add(tab);
        this.sessionTabPane.getSelectionModel().select(searchTab);
        this.knowledge.getSelectionModel().select(tab);
        tabsToParameters.put(tab, "");
        tabsToNotes.put(tab, "");
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());
        var _name = name.replace(' ', '_') + ".txt";
        var file = new File(knowledgeDir, _name);

        try {
            DataWriter.saveKnowledge(knowledge, new FileWriter(file));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText(null); // You can set a header text or keep it null
            alert.setContentText("Could not save knowledge: " + e.getMessage());
            alert.showAndWait();
        }

        tab.setOnClosed(event -> {
            displayNonemptyTabsOnly();
            selectIfNonempty(searchTab);

            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
        });

        readNotes(tab, knowledgeDir, name);
        persistNotes(tab, knowledgeDir, name);

        displayNonemptyTabsOnly();
        selectIfNonempty(knowledgeTab);
    }

    /**
     * Adds a game to the games tab.
     * @param name The name of the game.
     * @param pane The pane containing the game.
     * @param nextName Whether to append a number to the name if it already exists.
     */
    public void addGame(String name, Pane pane, boolean nextName) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }

        if (nextName) {
            name = Utils.nextName(name, this.getGameNames());
        }

        Tab tab = new Tab(name, pane);
        this.games.getTabs().add(tab);
        this.sessionTabPane.getSelectionModel().select(gamesTab);
        this.games.getSelectionModel().select(tab);
        tabsToParameters.put(tab, "");
        tabsToNotes.put(tab, "");
        tab.setOnSelectionChanged(event -> setParametersAndNotesText());

        tab.setOnClosed(event -> {
            displayNonemptyTabsOnly();
            selectIfNonempty(gamesTab);
        });

        readNotes(tab, dataDir, name);
        persistNotes(tab, dataDir, name);

        displayNonemptyTabsOnly();
        selectIfNonempty(gamesTab);
    }

    /**
     * Returns the names of the datasets in this project.
     * @return The names of the datasets.
     */
    public Collection<String> getDataNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.data.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    private void readNotes(Tab tab, File dir, String name) {
        String _filename1 = dir + File.separator + name.replace(' ', '_') + ".notes" + ".txt";

        tabsToNotes.putIfAbsent(tab, "");
        tabsToParameters.putIfAbsent(tab, "");

        if (new File(_filename1).exists()) {
            tabsToNotes.put(tab, Utils.loadTextFromFile(new File(_filename1)));
            notesArea.setText(tabsToNotes.get(tab));
        } else {
            tabsToNotes.put(tab, "Notes for " + name + ":");
            notesArea.setText(tabsToNotes.get(tab));
            Utils.saveTextToFile(new File(_filename1), tabsToNotes.get(tab));
        }

        String _filename2 = dir + File.separator + name.replace(' ', '_') + ".paramsNote" + ".txt";

        if (new File(_filename2).exists()) {
            tabsToParameters.put(tab, Utils.loadTextFromFile(new File(_filename2)));
            parametersArea.setText(tabsToParameters.get(tab));
        } else {
            if (tabsToParameters.get(tab).isEmpty()) {
                tabsToParameters.put(tab, "Parameters for " + name + ":");
            }
            parametersArea.setText(tabsToParameters.get(tab));
            Utils.saveTextToFile(new File(_filename2), tabsToParameters.get(tab));
        }
    }

    private void persistNotes(Tab tab, File dir, String _name) {
        notesArea.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, event -> {
            String filename = dir + File.separator + _name.replace(' ', '_') + ".notes" + ".txt";
            Utils.saveTextToFile(new File(filename), tabsToNotes.get(tab));

//            persisteParmaeters(dir, _name);
        });

        parametersArea.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, event -> {
            String filename = dir + File.separator + _name.replace(' ', '_') + ".paramsNote" + ".txt";
            Utils.saveTextToFile(new File(filename), tabsToParameters.get(tab));
        });

    }

    /**
     * Returns the names of the graphs in this project.
     * @return The names of the graphs.
     */
    public Collection<String> getGraphNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.graphs.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the names of the search results in this project.
     * @return The names of the search results.
     */
    public Collection<String> getSearchNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.search.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the names of the knowledge files in this project.
     * @return The names of the knowledge files.
     */
    public Collection<String> getKnowledgeNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.knowledge.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the names of the games in this project.
     * @return The names of the games.
     */
    public Collection<String> getGameNames() {
        List<String> names = new ArrayList<>();

        for (Tab tab : this.games.getTabs()) {
            names.add(tab.getText());
        }

        return names;
    }

    /**
     * Returns the tree item for this project. This is used to display the project in the project tree and
     * is stored in the project so that it can be reacted to.
     * @return The tree item.
     */
    public TreeItem<String> getTreeItem() {
        return treeItem;
    }

    /**
     * Returns the parameters area, which is stored in this project so that its text can be modified.
     * @return The parameters area.
     */
    public TextArea getParametersArea() {
        return parametersArea;
    }

    /**
     * Returns the notes area, which is stored in this project so that its text can be modified.
     *
     * @return The notes area.
     */
    public TextArea getNotesArea() {
        return notesArea;
    }

    /**
     * Returns the selected dataset for this project.
     * @return The selected dataset.
     */
    public DataSet getSelectedDataSet() {
        Tab selected = data.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Node content1 = selected.getContent();

            if (content1 instanceof TableView content) {
                return dataSetMap.get(content);
            }
        }

        return null;
    }

    /**
     * Sets the text of the parameters and notes areas.
     */
    public void setParametersAndNotesText() {
        Tab selectedItem = sessionTabPane.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        Node content = selectedItem.getContent();
        TabPane tabPane = (TabPane) content;
        setParametersAndNotesText(tabPane);
    }

    /**
     * Sets the text of the parameters area.
     * @param parameters     The parameters.
     * @param usedParameters The parameter names that were actually used.
     */
    private void setParametersText(Tab tab, Parameters parameters, List<String> usedParameters) {
        this.parametersArea.clear();

        for (String parameter : usedParameters) {
            String s = parameter + "=" + parameters.get(parameter) + "\n";
            this.parametersArea.appendText(s);
        }

        tabsToParameters.put(tab, parametersArea.getText());
    }

    /**
     * Sets the text of the parameters and notes areas and adds key listeners to the parameters area and notes area so
     * that the text can be saved when the user types.
     *
     * @param tabPane The tab pane to get the selected tab from.
     */
    private void setParametersAndNotesText(TabPane tabPane) {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        parametersArea.setText(getParameterString(selected));
        notesArea.setText(getNoteString(selected));

        parametersArea.setOnKeyTyped(event -> tabsToParameters.put(selected, parametersArea.getText()));

        notesArea.setOnKeyTyped(event -> tabsToNotes.put(selected, notesArea.getText()));
    }

    private String getParameterString(Tab tab) {
        tabsToParameters.putIfAbsent(tab, "");
        return tabsToParameters.get(tab);
    }

    private String getNoteString(Tab selected) {
        tabsToNotes.putIfAbsent(selected, "");
        return tabsToNotes.get(selected);
    }

    /**
     * Sets up a tab pane.
     *
     * @param _mainTabPane The main tab pane.
     * @param _dataTab     The data tab.
     * @param _data        The data tab pane.
     */
    private void setUpTabPane(TabPane _mainTabPane, Tab _dataTab, TabPane _data) {
        _mainTabPane.getTabs().add(_dataTab);
        _dataTab.setClosable(false);
        _dataTab.setOnSelectionChanged(event -> setParametersAndNotesText(_data));
        _data.setSide(Side.TOP);
    }

    private void displayNonemptyTabsOnly() {
        sessionTabPane.getTabs().clear();

        List<Tab> tabs = new ArrayList<>();
        tabs.add(dataTab);
        tabs.add(valenceTab);
        tabs.add(searchTab);
        tabs.add(knowledgeTab);
        tabs.add(graphTab);
        tabs.add(gamesTab);

        for (Tab tab : tabs) {
            if (tab.getContent() instanceof TabPane tabPane) {
                if (!tabPane.getTabs().isEmpty()) {
                    sessionTabPane.getTabs().add(tab);
                }
            }
        }
    }

    private void selectIfNonempty(Tab tab) {
        TabPane tabPane = (TabPane) tab.getContent();
        if (!tabPane.getTabs().isEmpty()) {
            sessionTabPane.getSelectionModel().select(tab);
        }
    }
}

