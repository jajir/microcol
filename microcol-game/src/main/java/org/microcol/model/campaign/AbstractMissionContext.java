package org.microcol.model.campaign;

import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;

/**
 * Abstract Class that helps with extracting variable from mission data in
 * persistent model.
 */
public abstract class AbstractMissionContext implements MissionContext {

    protected boolean getBoolean(final ModelPo modelPo, final String key) {
        Preconditions.checkNotNull(modelPo, "modelPo is null");
        Preconditions.checkNotNull(key, "key is null");
        return Boolean.parseBoolean(modelPo.getCampaign().getData().get(key));
    }

    protected Integer getInt(final ModelPo modelPo, final String key) {
        Preconditions.checkNotNull(modelPo, "modelPo is null");
        Preconditions.checkNotNull(key, "key is null");
        String val = modelPo.getCampaign().getData().get(key);
        if (val == null) {
            return 0;
        } else {
            return Integer.parseInt(val);
        }
    }

}
