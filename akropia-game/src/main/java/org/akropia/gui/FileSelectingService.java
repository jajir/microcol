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

    /**
     * @param directory required initial directory
     * @param title     required window title
     * @return chosen authored scenario to open, or <code>null</code> when
     *         canceled
     */
    public File openScenario(final File directory, final String title) {
        final FileChooser fileChooser = prepareFileChooser(directory, title);
        return fileChooser.showOpenDialog(getViewUtil().getPrimaryStage());
    }

    /**
     * @param directory required initial directory
     * @param fileName  required initial file name
     * @param title     required window title
     * @return chosen file to write the authored scenario to, or
     *         <code>null</code> when canceled
     */
    public File saveScenario(final File directory, final String fileName,
            final String title) {
        final FileChooser fileChooser = prepareFileChooser(directory, title);
        fileChooser.setInitialFileName(fileName);
        return fileChooser.showSaveDialog(getViewUtil().getPrimaryStage());
    }

    private FileChooser prepareFileChooser(final File saveDirectory,
            final Dialog caption) {
        return prepareFileChooser(saveDirectory, i18n.get(caption));
    }

    private FileChooser prepareFileChooser(final File saveDirectory,
            final String title) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(saveDirectory);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Akropia data files", "*.akropia"));
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("All Images", "*.*"));
        return fileChooser;
    }

}
