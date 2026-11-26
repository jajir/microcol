package org.akropia.model.game.po;

import org.akropia.model.store.ModelPo;

public class GameModelPo {
    private String playthroughId;
    private String saveName;

    public String getPlaythroughId() {
        return playthroughId;
    }

    public void setPlaythroughId(final String value) {
        playthroughId = value;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(final String value) {
        saveName = value;
    }

    private ModelPo model;

    public ModelPo getModel() {
        return model;
    }

    public void setModel(ModelPo model) {
        this.model = model;
    }

}
