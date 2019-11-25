package org.microcol.model.campaign;

import org.microcol.model.campaign.store.CampaignsDao;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Campaigns require some classes that survive one model.
 */
public final class CampaignModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GameModelDao.class).in(Singleton.class);
        bind(MissionFactoryManager.class).to(MissionFactoryManagerImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @SuppressWarnings("ucd")
    CampaignManager getCampaignManager(final CampaignsDao campaignsDao) {
        return new CampaignManager(
                Lists.newArrayList(new FreePlay_campaign(), new Default_campaign()), campaignsDao);
    }

}
