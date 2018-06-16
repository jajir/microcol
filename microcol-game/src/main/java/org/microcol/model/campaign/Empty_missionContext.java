package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.Map;

import org.microcol.model.store.ModelPo;

/**
 * First mission. Thrive.
 */
public class Empty_missionContext extends AbstractMissionContext {

    @Override
    public void initialize(final ModelPo modelPo) {
        // Do nothing there are no variables.

    }

    @Override
    public Map<String, String> saveToMap() {
        // Do nothing there are no variables.
        return new HashMap<>();
    }

}
