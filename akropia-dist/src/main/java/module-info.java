module akropia.dist {

//    requires com.google.common; // Guava
//    requires transitive com.google.guice;
//    requires javafx.controls;
    requires java.base;
    requires java.prefs;
    requires java.desktop;
    requires java.sql;
    requires java.naming;
    //sun/misc/Unsafe
    requires jdk.unsupported;
    requires java.management;

//    requires gson;
//    requires jsr305; // javax.annotation;
//    requires javax.inject;
//    requires slf4j.api;
//    requires cssfx;
    requires akropia.game;

    exports org.akropia.start;

/**
    opens org.akropia.gui to com.google.guice, com.google.common;
    opens org.akropia.gui.buttonpanel to com.google.guice, com.google.common;
    opens org.akropia.gui.dialog to com.google.guice;
    opens org.akropia.gui.event to com.google.guice, com.google.common;
    opens org.akropia.gui.event.model to com.google.guice;
    opens org.akropia.gui.image to com.google.guice;
    opens org.akropia.gui.preferences to com.google.guice, gson;
    opens org.akropia.gui.screen.campaign to com.google.guice;
    opens org.akropia.gui.screen.colony to com.google.guice;
    opens org.akropia.gui.screen.colony.buildingqueue to com.google.guice;
    opens org.akropia.gui.screen.colonizopedia to com.google.guice;
    opens org.akropia.gui.screen.editor to com.google.guice;
    opens org.akropia.gui.screen.europe to com.google.guice;
    opens org.akropia.gui.screen.game to com.google.guice;
    opens org.akropia.gui.screen.game.components to com.google.guice, com.google.common;
    opens org.akropia.gui.screen.game.gamepanel to com.google.guice, com.google.common;
    opens org.akropia.gui.screen.goals to com.google.guice;
    opens org.akropia.gui.screen.market to com.google.guice;
    opens org.akropia.gui.screen.menu to com.google.guice, com.google.common;
    opens org.akropia.gui.screen.setting to com.google.guice;
    opens org.akropia.gui.screen.statistics to com.google.guice;
    opens org.akropia.gui.screen.turnreport to com.google.guice, com.google.common;
    opens org.akropia.gui.util to com.google.guice, com.google.common;
    opens org.akropia.model.campaign to com.google.guice;
    opens org.akropia.model.campaign.po to com.google.guice;
    opens org.akropia.model.campaign.store to com.google.guice, gson;
    */

}