module example.concurrency.demo_db {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.h2database;


    opens example.concurrency.demo_db to javafx.fxml;
    exports example.concurrency.demo_db;
}