package org.microcol.model.campaign;

import java.util.Map;

import com.google.common.base.Preconditions;

public abstract class MissionGoal {

    /**
     * Mission name used for loading/saving.
     */
    private final String name;

    /**
     * Message key containing goal description.
     */
    private final String descriptionKey;

    /**
     * Is goal finished.
     */
    private boolean finished;

    MissionGoal(final String name, final String descriptionKey, final Map<String, String> data) {
        this.name = Preconditions.checkNotNull(name);
        this.descriptionKey = Preconditions.checkNotNull(descriptionKey);
        finished = Boolean.valueOf(data.get(name));
    }

    void save(final Map<String, String> data) {
        data.put(name, String.valueOf(finished));
    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param finished
     *            the finished to set
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * @return the description
     */
    public String getDescriptionKey() {
        return descriptionKey;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}
