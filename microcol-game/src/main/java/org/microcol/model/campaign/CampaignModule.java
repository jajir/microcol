package org.microcol.model.campaign;

import org.microcol.model.store.ModelDao;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Campaigns require some classes that survive one model.
 */
public class CampaignModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ModelDao.class).in(Singleton.class);
        bind(ModelCampaignDao.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    CampaignManager getCampaignManager() {
        return new CampaignManager(Lists.newArrayList(new CampaignFreePlay()));
    }

}
