package org.microcol.model.campaign;

import java.util.function.Function;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Abstract campaign mission. Holds basic info about campaign mission.
 */
public class DefaultCampaignMission implements CampaignMission {

	private final MissionName missionName;

	private final Integer orderNo;

	private final Function<MissionCreationContext, Mission<?>> missionSupplier;

	private boolean isFinished;

	DefaultCampaignMission(final MissionName missionName, final Integer orderNo,
			final Function<MissionCreationContext, Mission<?>> missionSupplier) {
		this.missionName = Preconditions.checkNotNull(missionName);
		this.orderNo = Preconditions.checkNotNull(orderNo);
		this.missionSupplier = Preconditions.checkNotNull(missionSupplier);
		setFinished(false);
	}

	@Override
	public String getName() {
		return missionName.getName();
	}

	@Override
	public Integer getOrderNo() {
		return orderNo;
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * @param isFinished
	 *            the isFinished to set
	 */
	@Override
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(getClass()).add("name", missionName.getName()).add("isFinished", isFinished)
				.add("orderNo", orderNo).toString();
	}

	@Override
	public Mission<?> makeMission(final MissionCallBack missionCallBack, final Model model, final ModelPo modelPo,
			final CampaignManager campaignManager) {
		final MissionCreationContext context = new MissionCreationContext(missionCallBack, model, modelPo,
				campaignManager);
		return missionSupplier.apply(context);
	}

	@Override
	public String getClassPathFile() {
		return missionName.getClassPathFile();
	}

	@Override
	public MissionName getMissionName() {
		return missionName;
	}

}
