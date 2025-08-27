package com.balanza;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        LoginScene loginScene = new LoginScene(primaryStage);
        loginScene.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}