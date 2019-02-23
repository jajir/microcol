package org.microcol.gui;

import java.io.File;

import org.microcol.gui.dialog.Dialog;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.stage.FileChooser;

public class FileSelectingService extends AbstractMessageWindow {

    public static final String SAVE_FILE_EXTENSION = "microcol";

    private final I18n i18n;

    @Inject
    public FileSelectingService(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    public File saveFile(final File saveDirectory, final String defaultFileName) {
        final FileChooser fileChooser = prepareFileChooser(saveDirectory, Dialog.saveGame_title);
        fileChooser.setInitialFileName(defaultFileName);
        return fileChooser.showSaveDialog(getViewUtil().getPrimaryStage());
    }

    public File loadFile(final File saveDirectory) {
        final FileChooser fileChooser = prepareFileChooser(saveDirectory, Dialog.loadGame_title);
        return fileChooser.showOpenDialog(getViewUtil().getPrimaryStage());
    }

    private FileChooser prepareFileChooser(final File saveDirectory, final Dialog caption) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(i18n.get(caption));
        fileChooser.setInitialDirectory(saveDirectory);
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("MicroCol data files", "*.microcol"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Images", "*.*"));
        return fileChooser;
    }

}
