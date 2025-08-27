package com.balanza;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class MainScene {
    private Stage stage;
    private Label pesoValueLabel; // Label solo para el número del peso
    private SerialReader serialReader;
    private DatabaseManager dbManager;
    private Image originalLogo;

    // Fields para datos
    private TextField patenteChasisField, patenteAcopladoField, nombreChoferField, dniChoferField;
    private TextField clientRazonField, clientCuitField, transportRazonField, transportCuitField;
    private TextField procedenciaField, destinoField, observacionesField;
    private TextField entradaField, salidaField, brutoField, taraField, netoField;

    public MainScene(Stage stage) {
        this.stage = stage;
        serialReader = new SerialReader();
        dbManager = new DatabaseManager();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Top bar gris con peso en esquina izquierda (estilo LED para el número, leyenda abajo)
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-background-color: #2c3e50;"); // Fondo gris oscuro para "parte gris"

        // VBox para peso y leyenda
        VBox pesoVBox = new VBox(2);
        pesoVBox.setPadding(new Insets(5));

        // Label para el número del peso (estilo LED, más grande)
        pesoValueLabel = new Label("0 kg");
        pesoValueLabel.setId("pesoLedLabel"); // ID para CSS LED
        pesoVBox.getChildren().add(pesoValueLabel);

        // Label para "BALANZA 1" (letra común, debajo)
        Label balanzaLabel = new Label("BALANZA 1");
        balanzaLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e5e7eb;"); // Letra común, gris claro
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

        // Config button on right
        Button configButton = new Button("Configurar");
        configButton.getStyleClass().add("button");
        configButton.setOnAction(e -> new ConfigScene(stage, serialReader, dbManager).show());
        HBox configBox = new HBox(configButton);
        configBox.setAlignment(Pos.CENTER_RIGHT);
        topBox.getChildren().add(configBox);

        root.setTop(topBox);

        // Tabs (sin iconos)
        TabPane tabPane = new TabPane();
        Tab camionTab = new Tab("Datos Camión", createCamionPane());
        Tab clienteTab = new Tab("Cliente/Transporte", createClientePane());
        Tab pesosTab = new Tab("Pesos", createPesosPane());
        tabPane.getTabs().addAll(camionTab, clienteTab, pesosTab);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);

        FadeTransition fade = new FadeTransition(Duration.millis(500), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // Timer para peso (solo actualiza el número)
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

    private GridPane createCamionPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label patenteChasisLabel = new Label("Patente Chasis:");
        patenteChasisField = new TextField();
        Button suggestButton = new Button("Sugerir");
        suggestButton.getStyleClass().add("button");
        suggestButton.setOnAction(e -> showSuggestions());

        Label patenteAcopladoLabel = new Label("Patente Acoplado:");
        patenteAcopladoField = new TextField();
        Label nombreChoferLabel = new Label("Nombre Chofer:");
        nombreChoferField = new TextField();
        Label dniChoferLabel = new Label("DNI Chofer:");
        dniChoferField = new TextField();

        grid.add(patenteChasisLabel, 0, 0);
        grid.add(patenteChasisField, 1, 0);
        grid.add(suggestButton, 2, 0);
        grid.add(patenteAcopladoLabel, 0, 1);
        grid.add(patenteAcopladoField, 1, 1);
        grid.add(nombreChoferLabel, 0, 2);
        grid.add(nombreChoferField, 1, 2);
        grid.add(dniChoferLabel, 0, 3);
        grid.add(dniChoferField, 1, 3);

        return grid;
    }

    private GridPane createClientePane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label clientRazonLabel = new Label("Cliente Razón Social:");
        clientRazonField = new TextField();
        Label clientCuitLabel = new Label("Cliente CUIT:");
        clientCuitField = new TextField();
        Label transportRazonLabel = new Label("Transporte Razón Social:");
        transportRazonField = new TextField();
        Label transportCuitLabel = new Label("Transporte CUIT:");
        transportCuitField = new TextField();
        Label procedenciaLabel = new Label("Procedencia:");
        procedenciaField = new TextField();
        Label destinoLabel = new Label("Destino:");
        destinoField = new TextField();
        Label observacionesLabel = new Label("Observaciones:");
        observacionesField = new TextField();

        grid.add(clientRazonLabel, 0, 0);
        grid.add(clientRazonField, 1, 0);
        grid.add(clientCuitLabel, 0, 1);
        grid.add(clientCuitField, 1, 1);
        grid.add(transportRazonLabel, 0, 2);
        grid.add(transportRazonField, 1, 2);
        grid.add(transportCuitLabel, 0, 3);
        grid.add(transportCuitField, 1, 3);
        grid.add(procedenciaLabel, 0, 4);
        grid.add(procedenciaField, 1, 4);
        grid.add(destinoLabel, 0, 5);
        grid.add(destinoField, 1, 5);
        grid.add(observacionesLabel, 0, 6);
        grid.add(observacionesField, 1, 6);

        return grid;
    }

    private GridPane createPesosPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label entradaLabel = new Label("Peso Entrada:");
        entradaField = new TextField();
        Button captureEntrada = new Button("Capturar");
        captureEntrada.getStyleClass().add("button");
        captureEntrada.setOnAction(e -> entradaField.setText(String.valueOf(serialReader.getCurrentWeight())));

        Label salidaLabel = new Label("Peso Salida:");
        salidaField = new TextField();
        Button captureSalida = new Button("Capturar");
        captureSalida.getStyleClass().add("button");
        captureSalida.setOnAction(e -> salidaField.setText(String.valueOf(serialReader.getCurrentWeight())));

        Label brutoLabel = new Label("Bruto:");
        brutoField = new TextField();
        Label taraLabel = new Label("Tara:");
        taraField = new TextField();
        Label netoLabel = new Label("Neto:");
        netoField = new TextField();

        Button calculateButton = new Button("Calcular");
        calculateButton.getStyleClass().add("button");
        calculateButton.setOnAction(e -> calculateWeights());

        grid.add(entradaLabel, 0, 0);
        grid.add(entradaField, 1, 0);
        grid.add(captureEntrada, 2, 0);
        grid.add(salidaLabel, 0, 1);
        grid.add(salidaField, 1, 1);
        grid.add(captureSalida, 2, 1);
        grid.add(brutoLabel, 0, 2);
        grid.add(brutoField, 1, 2);
        grid.add(taraLabel, 0, 3);
        grid.add(taraField, 1, 3);
        grid.add(netoLabel, 0, 4);
        grid.add(netoField, 1, 4);
        grid.add(calculateButton, 1, 5);

        return grid;
    }

    private void showSuggestions() {
        List<String> suggestions = dbManager.getPreviousPatentesChasis();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, suggestions);
        dialog.setTitle("Sugerencias");
        dialog.setContentText("Selecciona patente previa:");
        dialog.showAndWait().ifPresent(selected -> patenteChasisField.setText(selected));
    }

    private void calculateWeights() {
        try {
            double entrada = Double.parseDouble(entradaField.getText());
            double salida = Double.parseDouble(salidaField.getText());
            double bruto = Math.max(entrada, salida);
            double tara = Math.min(entrada, salida);
            double neto = bruto - tara;
            brutoField.setText(String.valueOf(bruto));
            taraField.setText(String.valueOf(tara));
            netoField.setText(String.valueOf(neto));
        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ingresa pesos válidos");
            alert.show();
        }
    }

    private void saveWeighing() {
        try {
            double entrada = Double.parseDouble(entradaField.getText());
            double salida = Double.parseDouble(salidaField.getText());
            dbManager.saveWeighing(patenteChasisField.getText(), patenteAcopladoField.getText(), nombreChoferField.getText(), dniChoferField.getText(),
                    clientRazonField.getText(), clientCuitField.getText(), transportRazonField.getText(), transportCuitField.getText(),
                    procedenciaField.getText(), destinoField.getText(), observacionesField.getText(), entrada, salida, 1); // 1 para pendiente
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Pesada guardada exitosamente");
            alert.show();
        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ingresa todos los datos válidos");
            alert.show();
        }
    }

    private Map<String, Object> getLastWeighing() {
        Map<String, Object> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DatabaseManager.DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM weighings ORDER BY id DESC LIMIT 1")) {
            if (rs.next()) {
                map.put("id", rs.getInt("id"));
                map.put("patente_chasis", rs.getString("patente_chasis"));
                map.put("patente_acoplado", rs.getString("patente_acoplado"));
                map.put("nombre_chofer", rs.getString("nombre_chofer"));
                map.put("dni_chofer", rs.getString("dni_chofer"));
                map.put("client_razon", rs.getString("client_razon"));
                map.put("client_cuit", rs.getString("client_cuit"));
                map.put("transport_razon", rs.getString("transport_razon"));
                map.put("transport_cuit", rs.getString("transport_cuit"));
                map.put("procedencia", rs.getString("procedencia"));
                map.put("destino", rs.getString("destino"));
                map.put("observaciones", rs.getString("observaciones"));
                map.put("peso_entrada", rs.getDouble("peso_entrada"));
                map.put("peso_salida", rs.getDouble("peso_salida"));
                map.put("bruto", rs.getDouble("bruto"));
                map.put("tara", rs.getDouble("tara"));
                map.put("neto", rs.getDouble("neto"));
                map.put("fecha", rs.getString("fecha"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}