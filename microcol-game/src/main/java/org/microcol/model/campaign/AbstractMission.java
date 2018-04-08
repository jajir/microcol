package org.microcol.model.campaign;

import java.util.HashMap;
import java.util.Map;

import org.microcol.gui.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.store.ModelPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Abstract mission. Contain some basic functionality.
 */
public abstract class AbstractMission implements Mission {

    private final String name;

    /**
     * Class path related mission model file name. It's something like
     * <i>/maps/03-before-game-over.json</i>.
     */
    private final String modelFileName;

    private final Integer orderNo;

    private boolean isFinished;

    private CampaignManager campaignManager;

    AbstractMission(final String name, final Integer orderNo, final String modelFileName) {
        this.name = Preconditions.checkNotNull(name);
        this.orderNo = Preconditions.checkNotNull(orderNo);
        this.modelFileName = Preconditions.checkNotNull(modelFileName);
        setFinished(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Mission#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Mission#getOrderNo()
     */
    @Override
    public Integer getOrderNo() {
        return orderNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Mission#getModelFileName()
     */
    @Override
    public String getModelFileName() {
        return modelFileName;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void setCampaignManager(final CampaignManager campaignManager) {
        this.campaignManager = campaignManager;
    }

    void flush() {
        Preconditions.checkNotNull(campaignManager, "campaignManager is null");
        campaignManager.saveMissionState();
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
    public Map<String, String> saveToMap() {
        return new HashMap<>();
    }

    @Override
    public void initialize(final ModelPo modelPo) {
        // default do nothing implementation
    }

    protected boolean isFirstTurn(final Model model) {
        return model.getCalendar().getCurrentYear() == model.getCalendar().getStartYear();
    }

    protected Player getHumanPlayer(final Model model) {
        return model.getPlayers().stream().filter(p -> p.isHuman()).findAny()
                .orElseThrow(() -> new MicroColException("There is no human player."));
    }

    protected boolean playerHaveMoreOrEqualColonies(final Model model, final int numberOfColonies) {
        return model.getColonies(getHumanPlayer(model)).size() >= numberOfColonies;
    }

    protected int getNumberOfMilitaryUnits(final Model model) {
        return getNumberOfMilitaryUnitsForPlayer(getHumanPlayer(model));
    }

    // TODO this info should be provided by player object itself.
    protected int getNumberOfMilitaryUnitsForPlayer(final Player player) {
        return (int) player.getAllUnits().stream().filter(unit -> unit.getType().canAttack())
                .count();
    }

    protected Integer get(final Map<String, String> map, final String key) {
        String val = map.get(key);
        if (val == null) {
            return 0;
        } else {
            return Integer.parseInt(val);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("name", name)
                .add("isFinished", isFinished).add("orderNo", orderNo)
                .add("modelFileName", modelFileName).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isFinished ? 1231 : 1237);
        result = prime * result + ((modelFileName == null) ? 0 : modelFileName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((orderNo == null) ? 0 : orderNo.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractMission other = (AbstractMission) obj;
        if (isFinished != other.isFinished)
            return false;
        if (modelFileName == null) {
            if (other.modelFileName != null)
                return false;
        } else if (!modelFileName.equals(other.modelFileName))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (orderNo == null) {
            if (other.orderNo != null)
                return false;
        } else if (!orderNo.equals(other.orderNo))
            return false;
        return true;
    }

}
