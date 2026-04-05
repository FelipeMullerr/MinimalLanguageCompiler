module com.minimalide {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.minimalide.ide to javafx.fxml;
    exports com.minimalide.ide;
}