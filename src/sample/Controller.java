package sample;

import DataAcquisition.DataInput;
import DatabaseConnection.Connect;
import chemaxon.struc.Molecule;
import com.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Controller {
    @FXML
    private TableView<ObservableList> tableview;

    @FXML
    private AnchorPane anch1;

    @FXML
    private Button loader;

    @FXML
    private TextField path;

    @FXML
    private TextField dbid;

    @FXML
    private TextField rootpswd;

    @FXML
    private AnchorPane anch2;

    @FXML
    private Button filter;

    @FXML
    private Button saveCSV;

    @FXML
    private TextField massField;

    @FXML
    private TextField logPfield;

    @FXML
    private TextField logDdown;

    @FXML
    private TextField donorCount;

    @FXML
    private TextField acceptorCount;

    @FXML
    private TextField logDup;

    @FXML
    private TextField rotatorField;

    @FXML
    private TextField psaField;

    @FXML
    private TextField ringField;

    @FXML
    private TextField aromaDownField;

    @FXML
    private TextField aromaUpField;

    @FXML
    private RadioButton massBool;

    @FXML
    private RadioButton logPBool;

    @FXML
    private RadioButton logDBool;

    @FXML
    private RadioButton donoBool;

    @FXML
    private RadioButton acceptBool;

    @FXML
    private RadioButton rotaBool;

    @FXML
    private RadioButton psaBool;

    @FXML
    private RadioButton ringBool;

    @FXML
    private RadioButton aromaBool;

    @FXML
    private ProgressBar progBar;

    private ResultSet rs;
    private Connect c;
    private ObservableList<ObservableList> data = FXCollections.observableArrayList();
    private String query;

    //The task passed onto a separate thread from the GUI thread responsible for invoking the calculation of the values as well as insertion into the DB
    Task task = new Task() {
        @Override
        protected Object call() throws Exception {
            DataInput di = new DataInput();

            ArrayList<Molecule> dataMole = di.inputFile(path.getText());
            String compNumQuery;
            for (int k = 0; k < dataMole.size() - 1; k++) {
                System.out.println(di.databaseInput(dataMole)[k]);
                compNumQuery = "INSERT INTO molecules (Name, Mass, logP, logD, H_Donors, H_Acceptors, ringCount, " +
                        "rotatableBondCount, PSA, fusedAromaticCount)" + "VALUES (" + di.databaseInput(dataMole)[k] + ")";
                try {
                    PreparedStatement state = c.getConnection().prepareStatement(compNumQuery);
                    state.execute();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            return null;
        }
    };
// Method responsible for starting the concurrent thread's task as well as displaying the data in the table once the task is complete
// Worth mentioning is the fact that it is necessary to delay the query until the task is complete, despite it being handled on the main thread
    public void buildData() {
        c = new Connect(dbid.getText(), rootpswd.getText());
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
        task.setOnSucceeded(event -> {
        try {

            query = "SELECT * from molecules";
            rs = c.getConnection().createStatement().executeQuery(query);
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory((Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                tableview.getColumns().addAll(col);
                System.out.println("Column [" + i + "] ");
            }

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }
            tableview.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
        //Once it is done the filtration panel appears and the previous one is disabled
        anch1.setVisible(false);
        anch1.setDisable(true);
        anch2.setVisible(true);
        anch2.setDisable(false);
        });

    }
    //Method for filtration of the data, refreshing of the table has to be done on the main thread, hence the new lambda expresion
    @FXML
    void queryFilter(ActionEvent event) {
        String chosenVariables = " Name ";
        String filterVariables = "Name is not null ";
        if (massBool.isSelected() && massField.getText() != null) {
            chosenVariables += " ,Mass ";
            filterVariables += " and Mass < " + massField.getText();
        }
        if (logPBool.isSelected() && logPfield.getText() != null) {
            chosenVariables += " ,logP ";
            filterVariables += " and logP < " + logPfield.getText();
        }
        if (logDBool.isSelected() && logDdown.getText() != null && logDup.getText() != null) {
            chosenVariables += " ,logD ";
            filterVariables += " and logD > " + logDup.getText();
            filterVariables += " and logD < " + logDdown.getText();
        }
        if (acceptBool.isSelected() && acceptorCount.getText() != null) {
            chosenVariables += " ,H_Acceptors ";
            filterVariables += " and H_Acceptors < " + acceptorCount.getText();
        }
        if (donoBool.isSelected() && donorCount.getText() != null) {
            chosenVariables += " ,H_Donors ";
            filterVariables += " and H_Donors < " + donorCount.getText();
        }
        if (ringBool.isSelected() && ringField.getText() != null) {
            chosenVariables += " ,ringCount ";
            filterVariables += " and ringCount < " + ringField.getText();
        }
        if (rotaBool.isSelected() && rotatorField.getText() != null) {
            chosenVariables += " ,rotatableBondCount ";
            filterVariables += " and rotatableBondCount < " + rotatorField.getText();
        }
        if (psaBool.isSelected() && psaField.getText() != null) {
            chosenVariables += " ,PSA ";
            filterVariables += " and PSA < " + psaField.getText();
        }
        if (aromaBool.isSelected() && aromaDownField.getText() != null && aromaUpField.getText() != null) {
            chosenVariables += " ,fusedAromaticCount ";
            filterVariables += " and fusedAromaticCount < " + aromaDownField.getText();
            filterVariables += " and fusedAromaticCount > " + aromaUpField.getText();
        }

        query = "Select name " + chosenVariables + " from molecules where " + filterVariables;
        data.removeAll(data);
        tableview.getColumns().clear();
        try {
            rs = c.getConnection().createStatement().executeQuery(query);
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory((Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                tableview.getColumns().addAll(col);
                System.out.println("Column [" + i + "] ");
            }

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added " + row);
                data.add(row);

            }
            Platform.runLater(() -> tableview.setItems(data));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    //Saving to file method using OpenCSV library
    @FXML
    void saveCSV(ActionEvent event) {
        Path path = Paths.get("csvMolecule.csv");

        try {
            rs = c.getConnection().createStatement().executeQuery(query);
            CSVWriter writer = new CSVWriter(new FileWriter("csvMolecule.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER, "\n");
            writer.writeAll(rs, true);
            System.out.println("Before");
            while (rs.next()) {
                System.out.println("After");
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                System.out.println("Here " + row);
                writer.writeNext(row.toArray(new String[0]));
            }
            writer.flush();
            writer.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
