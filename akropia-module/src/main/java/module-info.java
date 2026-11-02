module akropia.game {

//    requires com.google.common; // Guava
//    requires transitive com.google.guice;
//    requires javafx.controls;
    requires java.base;
    requires java.prefs;
    requires java.desktop;
    requires java.sql;

    requires jdk.unsupported;
    requires java.management;

    // Shading replaces Log4j's descriptors, so retain its runtime services here.
    uses org.apache.logging.log4j.spi.Provider;
    uses org.apache.logging.log4j.util.PropertySource;
    uses org.apache.logging.log4j.message.ThreadDumpMessage.ThreadInfoFactory;
    uses org.apache.logging.log4j.core.util.ContextDataProvider;
    uses org.apache.logging.log4j.core.util.WatchEventService;

    provides org.apache.logging.log4j.spi.Provider
            with org.apache.logging.log4j.core.impl.Log4jProvider;
    provides org.apache.logging.log4j.util.PropertySource
            with org.apache.logging.log4j.util.EnvironmentPropertySource,
            org.apache.logging.log4j.util.SystemPropertiesPropertySource;
    provides org.apache.logging.log4j.message.ThreadDumpMessage.ThreadInfoFactory
            with org.apache.logging.log4j.core.message.ExtendedThreadInfoFactory;
    provides org.apache.logging.log4j.core.util.ContextDataProvider
            with org.apache.logging.log4j.core.impl.ThreadContextDataProvider;

//    requires gson;
//    requires jsr305; // javax.annotation;
//    requires javax.inject;
//    requires slf4j.api;
//    requires cssfx;

    exports org.akropia;

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
