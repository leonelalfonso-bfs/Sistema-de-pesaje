package com.balanza;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginScene {
    private Stage stage;
    private Image originalLogo;

    public LoginScene(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Logo escalable (apenas más grande: 1/6 del ancho)
        ImageView logoView = new ImageView(new Image("file:C:/balanzas/Nuevo Java/truckscale/bfs_logo.png"));
        logoView.fitWidthProperty().bind(stage.widthProperty().divide(6)); // Apenas más grande
        logoView.setPreserveRatio(true);
        HBox topBox = new HBox(logoView);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20));
        root.setTop(topBox);

        // Formulario con fade-in
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label userLabel = new Label("Usuario:");
        TextField userField = new TextField();
        Label passLabel = new Label("Contraseña:");
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Ingresar");
        loginButton.getStyleClass().add("button");

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(loginButton, 1, 2);

        loginButton.setOnAction(e -> {
            if ("admin".equals(userField.getText()) && "1234".equals(passField.getText())) {
                MainScene mainScene = new MainScene(stage);
                mainScene.show();
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setContentText("Credenciales incorrectas");
                alert.show();
            }
        });

        root.setCenter(grid);

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);

        // Fade-in animation
        FadeTransition fade = new FadeTransition(Duration.millis(500), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        stage.show();
    }
}