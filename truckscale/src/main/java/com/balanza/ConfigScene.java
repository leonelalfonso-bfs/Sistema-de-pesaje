package com.balanza;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.fazecast.jSerialComm.SerialPort;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigScene {
    private Stage stage;
    private SerialReader serialReader;
    private DatabaseManager dbManager;
    private ComboBox<String> modelCombo;
    private ComboBox<String> portCombo;
    private ComboBox<Integer> baudCombo;
    private TextField regexField;

    public ConfigScene(Stage stage, SerialReader serialReader, DatabaseManager dbManager) {
        this.stage = stage;
        this.serialReader = serialReader;
        this.dbManager = dbManager;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Top with logo (apenas más grande: 1/6 del ancho)
        ImageView logoView = new ImageView(new Image("file:C:/balanzas/Nuevo Java/truckscale/bfs_logo.png"));
        logoView.fitWidthProperty().bind(stage.widthProperty().divide(6)); // Apenas más grande
        logoView.setPreserveRatio(true);
        HBox topBox = new HBox(logoView);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20));
        root.setTop(topBox);

        // Center form
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label modelLabel = new Label("Modelo Balanza:");
        modelCombo = new ComboBox<>();
        loadModels();
        modelCombo.setOnAction(e -> loadConfigForSelectedModel());
        Button newModelButton = new Button("Nuevo");
        newModelButton.getStyleClass().add("button");
        newModelButton.setOnAction(e -> addNewModel());

        Label portLabel = new Label("Puerto:");
        portCombo = new ComboBox<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            portCombo.getItems().add(port.getSystemPortName());
        }

        Label baudLabel = new Label("Baudrate:");
        baudCombo = new ComboBox<>();
        baudCombo.getItems().addAll(2400, 4800, 9600, 19200, 38400, 57600, 115200);

        Label regexLabel = new Label("Regex para peso:");
        regexField = new TextField();

        Button saveButton = new Button("Guardar");
        saveButton.getStyleClass().add("button");
        saveButton.setOnAction(e -> saveConfig());

        grid.add(modelLabel, 0, 0);
        grid.add(modelCombo, 1, 0);
        grid.add(newModelButton, 2, 0);
        grid.add(portLabel, 0, 1);
        grid.add(portCombo, 1, 1);
        grid.add(baudLabel, 0, 2);
        grid.add(baudCombo, 1, 2);
        grid.add(regexLabel, 0, 3);
        grid.add(regexField, 1, 3);
        grid.add(saveButton, 1, 4);

        root.setCenter(grid);

        // Bottom with back button
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        Button backButton = new Button("Atrás");
        backButton.getStyleClass().add("button");
        backButton.setOnAction(e -> new MainScene(stage).show());
        bottomBox.getChildren().add(backButton);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 450, 350);
        stage.setScene(scene);

        FadeTransition fade = new FadeTransition(Duration.millis(500), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        stage.show();
    }

    private void loadModels() {
        List<String> models = dbManager.getAllBalanzaModels();
        modelCombo.getItems().clear();
        modelCombo.getItems().addAll(models);
        if (models.isEmpty()) {
            modelCombo.getItems().add("BFS IND-100");
        }
    }

    private void loadConfigForSelectedModel() {
        String selectedModel = modelCombo.getValue();
        if (selectedModel != null) {
            Map<String, String> config = dbManager.getConfigForModel(selectedModel);
            if (!config.isEmpty()) {
                portCombo.setValue(config.get("port"));
                baudCombo.setValue(Integer.parseInt(config.get("baudrate")));
                regexField.setText(config.get("regex"));
            }
        }
    }

    private void addNewModel() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Modelo");
        dialog.setContentText("Ingrese nombre del nuevo modelo:");
        dialog.showAndWait().ifPresent(newModel -> {
            if (!newModel.trim().isEmpty()) {
                modelCombo.getItems().add(newModel);
                modelCombo.setValue(newModel);
            }
        });
    }

    private void loadProperties() {
        Properties props = SerialReader.loadProperties();
        String defaultModel = props.getProperty("model", "BFS IND-100");
        modelCombo.setValue(defaultModel);
        loadConfigForSelectedModel();
    }

    private void saveConfig() {
        String selectedModel = modelCombo.getValue();
        if (selectedModel == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Seleccione un modelo");
            alert.show();
            return;
        }
        String port = portCombo.getValue();
        String baudrate = baudCombo.getValue().toString();
        String regex = regexField.getText();

        dbManager.saveConfigForModel(selectedModel, port, baudrate, regex);

        Properties props = new Properties();
        props.setProperty("model", selectedModel);
        try (FileOutputStream out = new FileOutputStream("config.properties")) {
            props.store(out, null);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Configuración guardada para " + selectedModel);
            alert.show();
            new MainScene(stage).show();
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al guardar: " + ex.getMessage());
            alert.show();
        }
    }
}