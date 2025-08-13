package com.receiptprinter;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ReceiptPrinterApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML directly from src
        File fxmlFile = new File("src/resources/ReceiptPrinter.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // Load CSS directly from src
        scene.getStylesheets().add(new File("src/resources/styles.css").toURI().toURL().toExternalForm());

        primaryStage.setTitle("ASCII Receipt Printer");
        primaryStage.setScene(scene);
        primaryStage.setHeight(900);
        primaryStage.setMinWidth(930);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}