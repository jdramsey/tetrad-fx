package io.github.cmuphil.tetradfx.ui;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.StatUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class VariableView {
    private TableView<Row> tableView;

    public VariableView(DataSet dataSet) {
        tableView = new TableView<>();

        if (dataSet == null) {
            return;
        }

        TableView<Row> table = new TableView<>();

        TableColumn<Row, String> variableName = new TableColumn<>("Variable Name");
        variableName.setCellValueFactory(new PropertyValueFactory<>("variableName"));

        TableColumn<Row, String> variableType = new TableColumn<>("Variable Type");
        variableType.setCellValueFactory(new PropertyValueFactory<>("variableType"));

        TableColumn<Row, String> statsCol = new TableColumn<>("Stats");
        statsCol.setCellFactory(param -> new TextAreaCell());
        statsCol.setCellValueFactory(new PropertyValueFactory<>("stats"));

        TableColumn<Row, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellFactory(param -> new TextAreaCell());
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        table.getColumns().addAll(variableName, variableType, statsCol, notesCol);

        List<Row> rows = new ArrayList<>();

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            Node variable = dataSet.getVariable(i);
            rows.add(new Row(variable.getName(), variable instanceof DiscreteVariable ? "Discrete" : "Continuous",
                    getVariablestats(dataSet, variable)));

            ObservableList<Row> data = FXCollections.observableArrayList(rows);

            table.setItems(data);
            this.tableView = table;
        }
    }

    public TableView<Row> getTableView() {
        return tableView;
    }

    private String getVariablestats(DataSet dataSet, edu.cmu.tetrad.graph.Node variable) {
        if (variable instanceof DiscreteVariable) {
            return getDiscreteVariablestats(dataSet, (DiscreteVariable) variable);
        } else if (variable instanceof ContinuousVariable) {
            return getContinuousVariablestats(dataSet, (ContinuousVariable) variable);
        } else {
            throw new IllegalArgumentException("Unknown variable type: " + variable.getClass().getSimpleName());
        }
    }

    private String getContinuousVariablestats(DataSet dataSet, ContinuousVariable variable) {
        int index = dataSet.getColumn(variable);

        double[] values = new double[dataSet.getNumRows()];
        for (int i = 0; i < dataSet.getNumRows(); i++) {
            values[i] = dataSet.getDouble(i, index);
        }

        int N = values.length;
        double min = StatUtils.min(values);
        double max = StatUtils.max(values);
        double mean = StatUtils.mean(values);
        double median = StatUtils.median(values);
        double skewness = StatUtils.skewness(values);
        double kurtosis = StatUtils.kurtosis(values);

        NumberFormat nf = new DecimalFormat("#.####");

        return "N = " + N + "\n" +
                "Min: " + nf.format(min) + "\n" +
                "Max: " + nf.format(max) + "\n" +
                "Mean: " + nf.format(mean) + "\n" +
                "Median: " + nf.format(median) + "\n" +
                "Skewness: " + nf.format(skewness) + "\n" +
                "Kurtosis: " + nf.format(kurtosis);
    }

    private String getDiscreteVariablestats(DataSet dataSet, DiscreteVariable variable) {
        List<String> categories = variable.getCategories();
        int index = dataSet.getColumn(variable);

        int[] counts = new int[categories.size()];
        for (int i = 0; i < dataSet.getNumRows(); i++) {
            counts[dataSet.getInt(i, index)]++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("N = ").append(dataSet.getNumRows()).append("\n");

        for (int i = 0; i < categories.size(); i++) {
            sb.append(categories.get(i)).append(": ").append(counts[i]).append("\n");
        }

        return sb.toString();
    }

    public static class TextAreaCell extends TableCell<Row, String> {
        private final TextArea textArea;

        public TextAreaCell() {
            textArea = new TextArea();
            textArea.setPrefSize(200, 100);
            textArea.setWrapText(true);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                textArea.setText(item);
                setGraphic(textArea);
            }
        }
    }
}


