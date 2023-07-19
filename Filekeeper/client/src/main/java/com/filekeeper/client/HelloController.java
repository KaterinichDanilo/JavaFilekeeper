package com.filekeeper.client;

import cloud.CloudMessage;
import cloud.FileMessage;
import cloud.FileRequest;
import cloud.ListFiles;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    VBox clientPanel, serverPanel;
    @FXML
    TextArea commandsTextArea;

    private Socket socket;
    private static final int PORT = 5000;
    private static final String ADDRESS = "localhost";

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    PanelController clientPl;
    PanelServerController serverPl;


    private String homeDir = System.getProperty("user.home") + "/Desktop";
    private String currentDir = System.getProperty("user.home") + "/Desktop";
    private String currentServerDir;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clientPl = (PanelController)clientPanel.getProperties().get("ctrl");
        serverPl = (PanelServerController) serverPanel.getProperties().get("ctrl");
        commandsTextArea.setStyle("-fx-font-size: 15;");
        connect();
    }

    public void CopyBtnAction(ActionEvent actionEvent) {
        PanelController clientPl = (PanelController)clientPanel.getProperties().get("ctrl");
        PanelServerController serverPl = (PanelServerController) serverPanel.getProperties().get("ctrl");
        currentDir = clientPl.getCurrentPath();
        currentServerDir = serverPl.getCurrentPath();

        if (clientPl.getSelectedFileName() == null && serverPl.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You didn't choose any file", ButtonType.CLOSE);
            alert.showAndWait();
            return;
        }

        PanelController srcPc = null, dstPc = null;
        if (clientPl.getSelectedFileName() != null) {
            srcPc = clientPl;
            dstPc = serverPl;
        } else {
            srcPc = serverPl;
            dstPc = clientPl;
        }

        Path srcPath = Paths.get(srcPc.getCurrentPath(), srcPc.getSelectedFileName());
        Path dstPath = Paths.get(dstPc.getCurrentPath()).resolve(srcPath.getFileName().toString());

        if (srcPc == serverPl) {
            try {
                Fileinfo fileGet = serverPl.getTableView().getSelectionModel().getSelectedItem();
                getFile(fileGet.getFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                Fileinfo fileSend = clientPl.getTableView().getSelectionModel().getSelectedItem();
                sendFile(fileSend.getFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void DeleteBtnAction(ActionEvent actionEvent) {

        if (clientPl.getSelectedFileName() == null && serverPl.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You didn't choose any file", ButtonType.CLOSE);
            alert.showAndWait();
            return;
        }

        PanelController srcPc = null;
        if (clientPl.getSelectedFileName() != null) {
            srcPc = clientPl;
        } else {
            srcPc = serverPl;
        }

        Path srcPath = Paths.get(srcPc.getCurrentPath(), srcPc.getSelectedFileName());

    }

    private void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            serverPl.setOs(this.os);

            new Thread(() -> {
                try {
                    while (true) {
                        CloudMessage message = read();
                        if (message instanceof ListFiles listFiles) {
                            Platform.runLater(() -> {
                                ArrayList<Fileinfo> fileinfo = new ArrayList<>(Fileinfo.getFileInfoList(listFiles.getFiles()));
                                currentServerDir = fileinfo.get(0).getDir();
                                serverPl.updateList(currentServerDir, fileinfo);
                                serverPl.pathField.setText(currentServerDir);
                            });
                        } else if (message instanceof FileMessage fileMessage) {
                            System.out.println("FileMessage");
                            Path current = Path.of(currentDir).resolve(fileMessage.getName());
                            Files.write(current, fileMessage.getData());
                            LocalDateTime localDateTime = LocalDateTime.now();

                            Platform.runLater(() -> {
                                clientPl.updateList(Path.of(currentDir));
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<Fileinfo> getFiles(String dir) {
        ArrayList<Fileinfo> fileinfoArrayList = new ArrayList<>();
        for (String file : new File(dir).list()) {
            fileinfoArrayList.add(new Fileinfo(Path.of(file)));
        }
        return fileinfoArrayList;
    }


    public void sendFile(String fileToSend) throws IOException {
        commandsTextArea.appendText("File " + fileToSend + " was sent to server!\n");
        write(new FileMessage(Path.of(currentDir).resolve(fileToSend)));
    }


    private void getFile(String fileToGet) throws IOException {
        commandsTextArea.appendText("File " + fileToGet + " was downloaded from server!\n");
        write(new FileRequest(fileToGet));
    }

    public CloudMessage read() throws IOException, ClassNotFoundException {
        return (CloudMessage) is.readObject();
    }

    public void write(CloudMessage msg) throws IOException {
        os.writeObject(msg);
        os.flush();
    }

}

