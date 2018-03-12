package org.microcol.model.campaign;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * Contains all campaigns objects. Allows to get campaign by it's name.
 */
public class CampaignManager {

    private final Map<String, AbstractCampaign> campaigns;

    CampaignManager(final List<AbstractCampaign> campaigns) {
        this.campaigns = Preconditions.checkNotNull(campaigns).stream()
                .collect(Collectors.toMap(c -> c.getName(), Function.identity()));
    }

    /**
     * Get campaign by it's name.
     *
     * @param name
     *            required campaign name
     * @return campaign object
     */
    AbstractCampaign getCmapaignByName(final String name) {
        Preconditions.checkNotNull(name, "Campaign name is null");
        final AbstractCampaign out = campaigns.get(name);
        if (out == null) {
            throw new IllegalArgumentException(
                    String.format("There is no campaign for name '%s'", name));
        }
        return out;
    }

}
