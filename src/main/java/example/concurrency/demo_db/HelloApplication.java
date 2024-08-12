package example.concurrency.demo_db;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.h2.tools.Server;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class HelloApplication extends Application {
    private Stage stage;

    private String DB_URL = "jdbc:h2:mem:demo_db;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM './src/main/resources/User.sql'";
    private String USERNAME = "sa";
    private String PASSWORD = "";
    private Connection appConnection;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        this.stage = stage;
        stage.setTitle("Database Demo");
        Button newButton = new Button("New");
        newButton.setOnAction(e -> navigateToNewScene());
        Button openButton = new Button("Open");
        openButton.setOnAction(e -> navigateToDataScene());

        VBox vbox = new VBox(newButton, openButton);
        Scene scene = new Scene(vbox, 300, 200);

        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            try {
                saveToFile();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        stage.show();
    }

    public void navigateToNewScene() {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));

        Label fnameLabel = new Label("First Name:");
        TextField fNameField = new TextField();
        Label lnameLabel = new Label("Last Name:");
        TextField lNameField = new TextField();
        Button submit = new Button("Submit");

        submit.setOnAction(e -> {
            try {
                saveNameToDB(fNameField.getText(), lNameField.getText());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        vbox.getChildren().addAll(fnameLabel, fNameField, lnameLabel, lNameField, submit);
        stage.setScene(new Scene(vbox));
    }

    public void saveToFile() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Database");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try (Connection connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                String scriptCommand = "SCRIPT TO '" + file.getAbsolutePath() + "'";
                try (Statement statement = appConnection.createStatement()) {
                    statement.execute(scriptCommand);
                    System.out.println("Database successfully saved to " + file.getAbsolutePath());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(appConnection != null) {
            appConnection.close();
        }


    }

    public void navigateToDataScene() {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        Label welcomeText = new Label("Welcome back");
        vbox.getChildren().add(welcomeText);


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open SQL File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));
        File sqlFile = fileChooser.showOpenDialog(new Stage());

        if (sqlFile != null) {
            String filePath = sqlFile.getAbsolutePath().replace("\\", "/");
            String dbUrl = "jdbc:h2:mem:demo_db;INIT=RUNSCRIPT FROM '" + filePath + "'";

            try (Connection newConnection = DriverManager.getConnection(dbUrl, USERNAME, PASSWORD)) {
                try (Statement statement = newConnection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM USER_TABLE");

                    while (resultSet.next()) {
                        String firstName = resultSet.getString("fname");
                        String lastName = resultSet.getString("lname");
                        String fullName = firstName + " " + lastName;

                        Label userLabel = new Label(fullName);
                        vbox.getChildren().add(userLabel);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        stage.setScene(new Scene(vbox));
    }

    public void saveNameToDB(String fname, String lname) throws SQLException {
        appConnection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

        try {

            String sql = "INSERT INTO USER_TABLE (fname, lname) VALUES (?, ?)";
            try (PreparedStatement statement = appConnection.prepareStatement(sql)) {
                statement.setString(1, fname);
                statement.setString(2, lname);
                statement.execute();
                System.out.println("Successfully saved user");
            }
        }catch (SQLException ex) {
            ex.printStackTrace();
        }


    }

    public static void main(String[] args) throws SQLException {
        Server server = Server.createWebServer(args);
        System.out.println("Starting server on port " + server.getPort());
        server.start();
        launch();
        server.stop();
    }
}
