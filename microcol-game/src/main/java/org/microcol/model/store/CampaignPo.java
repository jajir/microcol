package org.microcol.model.store;

import java.util.Map;

public class CampaignPo {

    private String name;

    private String mission;
    
    private Map<String, String> data;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the mission
     */
    public String getMission() {
        return mission;
    }

    /**
     * @param mission the mission to set
     */
    public void setMission(String mission) {
        this.mission = mission;
    }

    /**
     * @return the data
     */
    public Map<String, String> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Map<String, String> data) {
        this.data = data;
    }
    
}
