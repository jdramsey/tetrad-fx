package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.BoxDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DoubleDataBox;
import edu.cmu.tetrad.graph.Graph;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

/**
 * <p>Stores all of the tabbed panes for a given dataset.</p>
 *
 * @author josephramsey
 */
public class Contents {
    private final Tab dataTab;
    private final Tab graphTab;
    private final Tab knowledgeTab;
    private final Tab modelTab;
    private final Tab insightsTab;
    private final Tab gamesTab;

    private DataSet dataSet = new BoxDataSet(new DoubleDataBox(0, 0), new ArrayList<>());
    private TabPane main;

    private final TabPane data = new TabPane();
    private final TabPane graphs = new TabPane();
    private final TabPane knowledge = new TabPane();
    private final TabPane models = new TabPane();
    private final TabPane insights = new TabPane();
    private final TabPane games = new TabPane();

    public Contents(DataSet dataSet, String name) {
        this(dataSet, null, name, null);
    }

    public Contents(DataSet dataSet, Graph graph, String dataName, String graphName) {
        this.dataSet = dataSet;
        this.main = new TabPane();
        this.main.setPrefSize(1000, 800);
        this.main.setSide(Side.LEFT);

        dataTab = new Tab("Data", data);
        graphTab = new Tab("Graphs", graphs);
        knowledgeTab = new Tab("Knowledge", knowledge);
        modelTab = new Tab("Models", models);
        insightsTab = new Tab("Insights", insights);
        gamesTab = new Tab("Games", games);

        this.main.getTabs().add(dataTab);
        this.main.getTabs().add(graphTab);
        this.main.getTabs().add(knowledgeTab);
        this.main.getTabs().add(modelTab);
        this.main.getTabs().add(insightsTab);
        this.main.getTabs().add(gamesTab);

        dataTab.setClosable(false);
        graphTab.setClosable(false);
        knowledgeTab.setClosable(false);
        modelTab.setClosable(false);
        insightsTab.setClosable(false);
        gamesTab.setClosable(false);

        this.data.setSide(Side.TOP);
        this.graphs.setSide(Side.TOP);
        this.knowledge.setSide(Side.TOP);
        this.models.setSide(Side.TOP);
        this.insights.setSide(Side.TOP);
        this.games.setSide(Side.TOP);

        this.data.getTabs().add(new Tab(dataName, DataView.getTableView(dataSet)));

        if (graph != null) {
            this.graphs.getTabs().add(new Tab(graphName, GraphView.getGraphDisplay(graph)));
        }
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public TabPane getMain() {
        return main;
    }

    public TabPane getData() {
        return data;
    }

    public TabPane getGraphs() {
        return graphs;
    }

    public TabPane getKnowledge() {
        return knowledge;
    }

    public TabPane getModels() {
        return models;
    }

    public TabPane getInsights() {
        return insights;
    }

    public TabPane getGames() {
        return games;
    }

    public void addDataSet(String name, DataSet dataSet) {
        this.dataSet = dataSet;
        this.data.getTabs().add(new Tab(name, DataView.getTableView(dataSet)));
    }

    public void addGraph(String name, Graph graph) {
        Tab tab = new Tab(name, GraphView.getGraphDisplay(graph));
        this.graphs.getTabs().add(tab);
        this.main.getSelectionModel().select(graphTab);
        this.graphs.getSelectionModel().select(tab);
    }

    public void addKnowledge(String name, Pane pane) {
        this.knowledge.getTabs().add(new Tab(name, pane));
    }

    public void addModel(String name, Pane pane) {
        this.models.getTabs().add(new Tab(name, pane));
    }

    public void addInsight(String name, Pane pane) {
        this.insights.getTabs().add(new Tab(name, pane));
    }

    public void addGame(String name, Pane pane) {
        Tab tab = new Tab(name, pane);
        this.games.getTabs().add(tab);
        this.main.getSelectionModel().select(gamesTab);
        this.games.getSelectionModel().select(tab);
    }

    public void rmoveDataSet(String name) {
        this.data.getTabs().removeIf(tab -> tab.getText().equals(name));
    }

    public void removeGraph(String name) {
        this.graphs.getTabs().removeIf(tab -> tab.getText().equals(name));
    }

    public void removeKnowledge(String name) {
        this.knowledge.getTabs().removeIf(tab -> tab.getText().equals(name));
    }

    public void removeModel(String name) {
        this.models.getTabs().removeIf(tab -> tab.getText().equals(name));
    }

    public void removeInsight(String name) {
        this.insights.getTabs().removeIf(tab -> tab.getText().equals(name));
    }

    public void removeGame(String name) {
        this.games.getTabs().removeIf(tab -> tab.getText().equals(name));
    }

    public void clearGames() {
        this.games.getTabs().clear();
    }
}
