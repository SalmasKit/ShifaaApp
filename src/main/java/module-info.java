module Shifaaproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    requires java.desktop;

    
    requires com.github.librepdf.openpdf; 

    opens controller to javafx.fxml;
    opens model to javafx.fxml;
    opens utils to javafx.fxml;
    opens main to javafx.fxml;

    exports controller;
    exports model;
    exports utils;
    exports main;
}
