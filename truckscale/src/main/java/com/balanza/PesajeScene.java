package com.balanza;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

public class PesajeScene {
    private Stage stage;
    private SerialReader serialReader;
    private DatabaseManager dbManager;
    private Label pesoValueLabel;

    // Fields para datos
    private TextField patenteChasisField, patenteAcopladoField, nombreChoferField, dniChoferField;
    private TextField clientRazonField, clientCuitField, transportRazonField, transportCuitField;
    private TextField procedenciaField, destinoField, observacionesField;
    private TextField entradaField, salidaField, brutoField, taraField, netoField;

    public PesajeScene(Stage stage, SerialReader serialReader, DatabaseManager dbManager) {
        this.stage = stage;
        this.serialReader = serialReader;
        this.dbManager = dbManager;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Top bar con peso LED on left, logo in center, back on right
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-background-color: #2c3e50;");

        // VBox para peso y leyenda
        VBox pesoVBox = new VBox(2);
        pesoVBox.setPadding(new Insets(5));

        // Label para el número del peso (estilo LED, más grande)
        pesoValueLabel = new Label("0 kg");
        pesoValueLabel.setId("pesoLedLabel");
        pesoVBox.getChildren().add(pesoValueLabel);

        // Label para "BALANZA 1" (letra común, abajo)
        Label balanzaLabel = new Label("BALANZA 1");
        balanzaLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e5e7eb;");
        pesoVBox.getChildren().add(balanzaLabel);

        topBox.getChildren().add(pesoVBox);

        // Logo in center
        ImageView logoView = new ImageView(new Image("file:C:/balanzas/Nuevo Java/truckscale/bfs_logo.png"));
        logoView.fitWidthProperty().bind(stage.widthProperty().divide(6));
        logoView.setPreserveRatio(true);
        HBox logoBox = new HBox(logoView, new Label("Tomar Pesaje"));
        logoBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(logoBox, Priority.ALWAYS);
        topBox.getChildren().add(logoBox);

        // Back button on right
        Button backButton = new Button("Atrás");
        backButton.getStyleClass().add("button");
        backButton.setOnAction(e -> new HomeScene(stage, serialReader, dbManager).show());
        HBox backBox = new HBox(backButton);
        backBox.setAlignment(Pos.CENTER_RIGHT);
        topBox.getChildren().add(backBox);

        root.setTop(topBox);

        // Center: All fields in one scrollable pane
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Datos Camión
        GridPane camionGrid = new GridPane();
        camionGrid.setHgap(10);
        camionGrid.setVgap(10);
        camionGrid.add(new Label("Patente Chasis:"), 0, 0);
        patenteChasisField = new TextField();
        camionGrid.add(patenteChasisField, 1, 0);
        Button suggestButton = new Button("Sugerir previos");
        suggestButton.getStyleClass().add("button");
        suggestButton.setOnAction(e -> showSuggestions());
        camionGrid.add(suggestButton, 2, 0);
        camionGrid.add(new Label("Patente Acoplado:"), 0, 1);
        patenteAcopladoField = new TextField();
        camionGrid.add(patenteAcopladoField, 1, 1);
        camionGrid.add(new Label("Nombre Chofer:"), 0, 2);
        nombreChoferField = new TextField();
        camionGrid.add(nombreChoferField, 1, 2);
        camionGrid.add(new Label("DNI Chofer:"), 0, 3);
        dniChoferField = new TextField();
        camionGrid.add(dniChoferField, 1, 3);

        // Datos Cliente/Transporte
        GridPane clienteGrid = new GridPane();
        clienteGrid.setHgap(10);
        clienteGrid.setVgap(10);
        clienteGrid.add(new Label("Cliente Razón Social:"), 0, 0);
        clientRazonField = new TextField();
        clienteGrid.add(clientRazonField, 1, 0);
        clienteGrid.add(new Label("Cliente CUIT:"), 0, 1);
        clientCuitField = new TextField();
        clienteGrid.add(clientCuitField, 1, 1);
        clienteGrid.add(new Label("Transporte Razón Social:"), 0, 2);
        transportRazonField = new TextField();
        clienteGrid.add(transportRazonField, 1, 2);
        clienteGrid.add(new Label("Transporte CUIT:"), 0, 3);
        transportCuitField = new TextField();
        clienteGrid.add(transportCuitField, 1, 3);
        clienteGrid.add(new Label("Procedencia:"), 0, 4);
        procedenciaField = new TextField();
        clienteGrid.add(procedenciaField, 1, 4);
        clienteGrid.add(new Label("Destino:"), 0, 5);
        destinoField = new TextField();
        clienteGrid.add(destinoField, 1, 5);
        clienteGrid.add(new Label("Observaciones:"), 0, 6);
        observacionesField = new TextField();
        clienteGrid.add(observacionesField, 1, 6);

        // Pesos
        GridPane pesosGrid = new GridPane();
        pesosGrid.setHgap(10);
        pesosGrid.setVgap(10);
        pesosGrid.add(new Label("Peso Entrada:"), 0, 0);
        entradaField = new TextField();
        pesosGrid.add(entradaField, 1, 0);
        Button captureEntrada = new Button("Capturar");
        captureEntrada.getStyleClass().add("button");
        captureEntrada.setOnAction(e -> entradaField.setText(String.valueOf(serialReader.getCurrentWeight())));
        pesosGrid.add(captureEntrada, 2, 0);
        pesosGrid.add(new Label("Peso Salida:"), 0, 1);
        salidaField = new TextField();
        pesosGrid.add(salidaField, 1, 1);
        Button captureSalida = new Button("Capturar");
        captureSalida.getStyleClass().add("button");
        captureSalida.setOnAction(e -> salidaField.setText(String.valueOf(serialReader.getCurrentWeight())));
        pesosGrid.add(captureSalida, 2, 1);
        pesosGrid.add(new Label("Bruto:"), 0, 2);
        brutoField = new TextField();
        pesosGrid.add(brutoField, 1, 2);
        pesosGrid.add(new Label("Tara:"), 0, 3);
        taraField = new TextField();
        pesosGrid.add(taraField, 1, 3);
        pesosGrid.add(new Label("Neto:"), 0, 4);
        netoField = new TextField();
        pesosGrid.add(netoField, 1, 4);
        Button calculateButton = new Button("Calcular");
        calculateButton.getStyleClass().add("button");
        calculateButton.setOnAction(e -> calculateWeights());
        pesosGrid.add(calculateButton, 1, 5);

        Button saveButton = new Button("Guardar y Generar Ticket");
        saveButton.getStyleClass().add("button");
        saveButton.setOnAction(e -> {
            saveWeighing();
            Map<String, Object> pesada = dbManager.getLastWeighing();
            Map<String, String> clientConfig = dbManager.getClientConfig();
            try {
                TicketGenerator.generatePdf(pesada, clientConfig, "ticket_" + pesada.get("id") + ".pdf");
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ticket generado en ticket_" + pesada.get("id") + ".pdf");
                alert.show();
            } catch (DocumentException | IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error generando ticket: " + ex.getMessage());
                alert.show();
            }
        });
        pesosGrid.add(saveButton, 1, 6);

        content.getChildren().addAll(camionGrid, clienteGrid, pesosGrid);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return scroll;
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