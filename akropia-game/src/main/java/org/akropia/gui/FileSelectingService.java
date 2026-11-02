package org.akropia.gui;

import java.io.File;

import org.akropia.gui.dialog.Dialog;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.stage.FileChooser;

public class FileSelectingService extends AbstractMessageWindow {

    private final I18n i18n;

    @Inject
    public FileSelectingService(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    public File saveFile(final File saveDirectory,
            final String defaultFileName) {
        final FileChooser fileChooser = prepareFileChooser(saveDirectory,
                Dialog.saveGame_title);
        fileChooser.setInitialFileName(defaultFileName);
        return fileChooser.showSaveDialog(getViewUtil().getPrimaryStage());
    }

    public File loadFile(final File saveDirectory) {
        final FileChooser fileChooser = prepareFileChooser(saveDirectory,
                Dialog.loadGame_title);
        return fileChooser.showOpenDialog(getViewUtil().getPrimaryStage());
    }

    private FileChooser prepareFileChooser(final File saveDirectory,
            final Dialog caption) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18n.get(caption));
        fileChooser.setInitialDirectory(saveDirectory);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Akropia data files", "*.akropia"));
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("All Images", "*.*"));
        return fileChooser;
    }

}
