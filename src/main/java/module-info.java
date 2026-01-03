module cn.yenmor.portableappmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.google.gson;
    requires java.logging;

    // 开放反射访问给 Gson（JSON 序列化需要）
    opens cn.yenmor.portableappmanager to javafx.fxml, com.google.gson;
    exports cn.yenmor.portableappmanager;
}