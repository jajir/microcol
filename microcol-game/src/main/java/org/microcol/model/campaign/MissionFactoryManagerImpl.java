package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MissionFactoryManagerImpl implements MissionFactoryManager {

    private final Map<MissionName, AbstractMissionFactory> factories = new HashMap<>();

    @Inject
    MissionFactoryManagerImpl() {
        factories.put(Default_missionNames.findNewWorld, new Default_0_missionFactory());
        factories.put(Default_missionNames.buildArmy, new Default_1_missionFactory());
        factories.put(Default_missionNames.thrive, new Default_2_missionFactory());

        factories.put(FreePlay_missionNames.freePlay, new FreePlay_0_missionFactory());
    }

    @Override
    public Mission<?> make(final MissionName missionName, final MissionCreationContext context) {
        final AbstractMissionFactory out = factories.get(missionName);
        if (out == null) {
            throw new NoSuchElementException(
                    String.format("There is no factory for mission '%s'", missionName));
        }
        return out.make(context);
    }

}
