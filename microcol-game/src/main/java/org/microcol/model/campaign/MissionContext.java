package org.microcol.model.campaign;

import java.util.Map;

import org.microcol.model.store.ModelPo;

/**
 * Define variables for mission. Allows to persist into game model and load
 * back.
 */
public interface MissionContext {

    void initialize(ModelPo modelPo);

    public Map<String, String> saveToMap();

}
