module microcol.game {

//    requires com.google.common; // Guava
//    requires transitive com.google.guice;
//    requires javafx.controls;
    requires java.base;
    requires java.prefs;
    requires java.desktop;
    requires java.sql;
    
    requires jdk.unsupported;
    requires java.management;
    
//    requires gson;
//    requires jsr305; // javax.annotation;
//    requires javax.inject;
//    requires slf4j.api;
//    requires cssfx;

    exports org.microcol;
    
/**
    opens org.microcol.gui to com.google.guice, com.google.common;
    opens org.microcol.gui.buttonpanel to com.google.guice, com.google.common;
    opens org.microcol.gui.dialog to com.google.guice;
    opens org.microcol.gui.event to com.google.guice, com.google.common;
    opens org.microcol.gui.event.model to com.google.guice;
    opens org.microcol.gui.image to com.google.guice;
    opens org.microcol.gui.preferences to com.google.guice, gson;
    opens org.microcol.gui.screen.campaign to com.google.guice;
    opens org.microcol.gui.screen.colony to com.google.guice;
    opens org.microcol.gui.screen.colony.buildingqueue to com.google.guice;
    opens org.microcol.gui.screen.colonizopedia to com.google.guice;
    opens org.microcol.gui.screen.editor to com.google.guice;
    opens org.microcol.gui.screen.europe to com.google.guice;
    opens org.microcol.gui.screen.game to com.google.guice;
    opens org.microcol.gui.screen.game.components to com.google.guice, com.google.common;
    opens org.microcol.gui.screen.game.gamepanel to com.google.guice, com.google.common;
    opens org.microcol.gui.screen.goals to com.google.guice;
    opens org.microcol.gui.screen.market to com.google.guice;
    opens org.microcol.gui.screen.menu to com.google.guice, com.google.common;
    opens org.microcol.gui.screen.setting to com.google.guice;
    opens org.microcol.gui.screen.statistics to com.google.guice;
    opens org.microcol.gui.screen.turnreport to com.google.guice, com.google.common;
    opens org.microcol.gui.util to com.google.guice, com.google.common;
    opens org.microcol.model.campaign to com.google.guice;
    opens org.microcol.model.campaign.po to com.google.guice;
    opens org.microcol.model.campaign.store to com.google.guice, gson;
    */
    
}