package org.microcol.model.campaign;

/**
 * Enum defining available campaigns.
 *
 */
public enum CampaignNames implements CampaignName {
	freePlay,

	/**
	 * Default campaign implementation.
	 */
	defaultCampaign {
		@Override
		public String toString() {
			return "default";
		}
	};

}
