package com.balanza;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class HomeScene {
    private Stage stage;
    private SerialReader serialReader;
    private DatabaseManager dbManager;
    private Label pesoValueLabel; // Para el peso estilo LED
    private Image originalLogo;

    public HomeScene(Stage stage, SerialReader serialReader, DatabaseManager dbManager) {
        this.stage = stage;
        this.serialReader = serialReader;
        this.dbManager = dbManager;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Top bar con peso LED on left, logo in center, no config here (since it's home)
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-background-color: #2c3e50;"); // Parte gris/oscura

        // VBox para peso y leyenda
        VBox pesoVBox = new VBox(2);
        pesoVBox.setPadding(new Insets(5));

        // Label para el número del peso (estilo LED, más grande)
        pesoValueLabel = new Label("0 kg");
        pesoValueLabel.setId("pesoLedLabel");
        pesoVBox.getChildren().add(pesoValueLabel);

        // Label para "BALANZA 1" (letra común, debajo)
        Label balanzaLabel = new Label("BALANZA 1");
        balanzaLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e5e7eb;");
        pesoVBox.getChildren().add(balanzaLabel);

        topBox.getChildren().add(pesoVBox);

        // Logo in center
        ImageView logoView = new ImageView(new Image("file:C:/balanzas/Nuevo Java/truckscale/bfs_logo.png"));
        logoView.fitWidthProperty().bind(stage.widthProperty().divide(6));
        logoView.setPreserveRatio(true);
        HBox logoBox = new HBox(logoView, new Label("Sistema de Pesadas"));
        logoBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(logoBox, Priority.ALWAYS);
        topBox.getChildren().add(logoBox);

        root.setTop(topBox);

        // Center: Buttons for navigation in a grid
        GridPane buttonGrid = new GridPane();
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(20);
        buttonGrid.setVgap(20);
        buttonGrid.setPadding(new Insets(50));

        Button pesajesButton = new Button("Tomar Pesajes");
        pesajesButton.getStyleClass().add("button");
        pesajesButton.setPrefSize(200, 100);
        pesajesButton.setOnAction(e -> new PesajeScene(stage, serialReader, dbManager).show());

        Button reportesButton = new Button("Reportes");
        reportesButton.getStyleClass().add("button");
        reportesButton.setPrefSize(200, 100);
        reportesButton.setOnAction(e -> new ReportesScene(stage, serialReader, dbManager).show());

        Button configuracionesButton = new Button("Configuraciones");
        configuracionesButton.getStyleClass().add("button");
        configuracionesButton.setPrefSize(200, 100);
        configuracionesButton.setOnAction(e -> new ConfiguracionesScene(stage, serialReader, dbManager).show());

        Button sistemaButton = new Button("Sistema");
        sistemaButton.getStyleClass().add("button");
        sistemaButton.setPrefSize(200, 100);
        sistemaButton.setOnAction(e -> new SistemaScene(stage, serialReader, dbManager).show());

        buttonGrid.add(pesajesButton, 0, 0);
        buttonGrid.add(reportesButton, 1, 0);
        buttonGrid.add(configuracionesButton, 0, 1);
        buttonGrid.add(sistemaButton, 1, 1);

        root.setCenter(buttonGrid);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);

        FadeTransition fade = new FadeTransition(Duration.millis(500), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // Timer para peso
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    double weight = serialReader.getCurrentWeight();
                    String formatted = (weight % 1 == 0) ? String.format("%.0f", weight) : String.format("%.1f", weight);
                    pesoValueLabel.setText(formatted + " kg");
                });
            }
        }, 0, 1000);

        stage.show();
    }

    // Añade este método si showSuggestions es necesario
    private void showSuggestions() {
        List<String> suggestions = dbManager.getPreviousPatentesChasis();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, suggestions);
        dialog.setTitle("Sugerencias");
        dialog.setContentText("Selecciona patente previa:");
        dialog.showAndWait().ifPresent(selected -> patenteChasisField.setText(selected));
    }
}