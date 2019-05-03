package org.microcol.gui.screen.editor;

import org.microcol.gui.screen.GameScreen;
import org.microcol.i18n.I18n;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class EditorPanel implements GameScreen {

    private final VBox mainPanel = new VBox();

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void beforeShow() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void beforeHide() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateLanguage(I18n i18n) {
        // TODO Auto-generated method stub
        
    }

}
