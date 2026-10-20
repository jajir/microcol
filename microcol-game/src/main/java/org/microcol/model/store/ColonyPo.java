package org.microcol.model.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.GoodsType;

public final class ColonyPo {

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    private String name;

    private String ownerName;

    /**
     * Accumulated colony liberty, required and never negative. Wrapper type so
     * a missing value is detected on load instead of becoming zero.
     */
    private Long liberty;

    /**
     * Name of the last non-Crown player this colony belonged to; required.
     */
    private String lastColonialAffiliation;

    /**
     * Saved town commons commodity name, or {@code NONE} for no second output;
     * required, never guessed on load.
     */
    private String townCommonsGoods;

    private Location location;

    private List<ColonyFieldPo> colonyFields = new ArrayList<>();

    private List<ConstructionPo> constructions = new ArrayList<>();

    private List<ColonyBuildingQueueItemPo> buildingQueue = new ArrayList<>();

    private Map<String, Integer> colonyWarehouse = new HashMap<>();

    private List<GoodsType> exportGoods;

    private List<EducationCoursePo> educationCourses;

    public List<EducationCoursePo> getEducationCourses() {
        return educationCourses;
    }

    public void setEducationCourses(final List<EducationCoursePo> value) {
        educationCourses = value;
    }

    public List<GoodsType> getExportGoods() {
        return exportGoods;
    }

    public void setExportGoods(final List<GoodsType> exportGoods) {
        this.exportGoods = exportGoods;
    }

    public ColonyPo() {
        Direction.getVectors().forEach(direction -> {
            final ColonyFieldPo colonyFieldPo = new ColonyFieldPo();
            colonyFieldPo.setDirection(direction);
            colonyFields.add(colonyFieldPo);
        });
    }

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
     * @return the ownerName
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * @param ownerName the ownerName to set
     */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getLiberty() {
        return liberty;
    }

    public void setLiberty(final Long liberty) {
        this.liberty = liberty;
    }

    public String getLastColonialAffiliation() {
        return lastColonialAffiliation;
    }

    public void setLastColonialAffiliation(
            final String lastColonialAffiliation) {
        this.lastColonialAffiliation = lastColonialAffiliation;
    }

    /**
     * @return saved town commons commodity, or {@code NONE}
     */
    public String getTownCommonsGoods() {
        return townCommonsGoods;
    }

    /**
     * @param townCommonsGoods saved town commons commodity to set
     */
    public void setTownCommonsGoods(final String townCommonsGoods) {
        this.townCommonsGoods = townCommonsGoods;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the colonyFields
     */
    public List<ColonyFieldPo> getColonyFields() {
        return colonyFields;
    }

    /**
     * @param colonyFields the colonyFields to set
     */
    public void setColonyFields(final List<ColonyFieldPo> colonyFields) {
        this.colonyFields = colonyFields;
    }

    /**
     * @return the constructions
     */
    public List<ConstructionPo> getConstructions() {
        return constructions;
    }

    /**
     * @param constructions the constructions to set
     */
    public void setConstructions(List<ConstructionPo> constructions) {
        this.constructions = constructions;
    }

    /**
     * @return the colonyWarehouse
     */
    public Map<String, Integer> getColonyWarehouse() {
        return colonyWarehouse;
    }

    /**
     * @param colonyWarehouse the colonyWarehouse to set
     */
    public void setColonyWarehouse(final Map<String, Integer> colonyWarehouse) {
        this.colonyWarehouse = colonyWarehouse;
    }

    /**
     * @return the buildingQueue
     */
    public List<ColonyBuildingQueueItemPo> getBuildingQueue() {
        return buildingQueue;
    }

    /**
     * @param buildingQueue the buildingQueue to set
     */
    public void setBuildingQueue(
            List<ColonyBuildingQueueItemPo> buildingQueue) {
        this.buildingQueue = buildingQueue;
    }

}
