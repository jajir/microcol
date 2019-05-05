package org.microcol.gui.screen.editor;

import java.io.File;

import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ModelService {

    private final GameModelPoDao gameModelPoDao;

    private GameModelPo gameModelPo;

    private File file;

    @Inject
    ModelService(final GameModelPoDao gameModelPoDao) {
        this.gameModelPoDao = Preconditions.checkNotNull(gameModelPoDao);
    }

    /**
     * Load game model from given file.
     * 
     * @param file
     *            required file
     */
    void load(final File file) {
        this.file = Preconditions.checkNotNull(file);
        this.gameModelPo = gameModelPoDao.loadFromFile(file);
    }

    /**
     * Save currently loaded game model.
     */
    public void save() {
        gameModelPoDao.saveToFile(file.getAbsolutePath(), gameModelPo);
    }

    public ModelPo getModel() {
        return gameModelPo.getModel();
    }

}
