package com.filekeeper.client;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {
    private String HOMEDIR = System.getProperty("user.home") + "/Desktop";
    @FXML
    TableView<Fileinfo> tableView;
    @FXML
    ComboBox disksBox;
    @FXML
    TextField pathField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<Fileinfo, String> fileTypeColumn = new TableColumn<>("Type");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<Fileinfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(200);

        TableColumn<Fileinfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(100);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        TableColumn<Fileinfo, Long> fileDateColumn = new TableColumn<>("Date");
        fileDateColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getLastModified().format(dateTimeFormatter)));
        fileDateColumn.setPrefWidth(180);



        tableView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);
        tableView.getSortOrder().add(fileTypeColumn);
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<Fileinfo, Long>() {
                @Override
                protected void updateItem(Long aLong, boolean b) {
                    super.updateItem(aLong, b);
                    if (aLong == null || b) {
                        setText("");
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", aLong);
                        if (aLong == -1L){
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        disksBox.getItems().clear();

        for (Path path : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(path.toString());
        }
        disksBox.getSelectionModel().select(0);

        tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText()).resolve(tableView.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)){
                        updateList(path);
                    }
                }
            }
        });

        updateList(Paths.get(HOMEDIR));
    }

    public TableView<Fileinfo> getTableView() {
        return tableView;
    }

    public void updateList(Path path) {
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            tableView.getItems().clear();
            tableView.getItems().addAll(Files.list(path).map(Fileinfo::new).collect(Collectors.toList()));
            tableView.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "It's impossible to update file list", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnUpPathAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> comboBox = (ComboBox<String>)actionEvent.getSource();
        updateList(Paths.get(comboBox.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFileName() {
        if (!tableView.isFocused()) {
            return null;
        }
        return tableView.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }

}
