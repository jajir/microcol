package org.microcol.model.campaign;

/**
 * Base class of mission abstract factory. Each campaign for each mission name
 * store corresponding mission factory.
 */
interface AbstractMissionFactory {

    /**
     * From mission creation context create mission instance.
     * 
     * @param context
     *            required mission creating context
     * @return Return newly created mission instance.
     */
    Mission<?> make(MissionCreationContext context);

}
