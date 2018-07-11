package org.microcol.model.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.microcol.gui.MicroColException;
import org.microcol.model.ConstructionType;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;

public final class ColonyPo {

    private String name;

    private String ownerName;

    private Location location;

    private List<ColonyFieldPo> colonyFields = new ArrayList<>();

    private List<ConstructionPo> constructions = new ArrayList<>();

    private Map<String, Integer> colonyWarehouse = new HashMap<>();

    public ColonyPo() {
        Location.DIRECTIONS.forEach(direction -> {
            final ColonyFieldPo colonyFieldPo = new ColonyFieldPo();
            colonyFieldPo.setDirection(direction);
            colonyFields.add(colonyFieldPo);
        });
    }

    public ConstructionPo getConstructionByType(final ConstructionType constructionType) {
        Preconditions.checkNotNull(constructionType);
        return constructions.stream()
                .filter(constructionPo -> constructionType.equals(constructionPo.getType()))
                .findFirst().orElseThrow(() -> new MicroColException(
                        String.format("There is not defined construction (%s)", constructionType)));
    }

    public ColonyFieldPo getFieldByDirection(final Location direction) {
        Preconditions.checkNotNull(direction);
        return colonyFields.stream()
                .filter(colonyField -> direction.equals(colonyField.getDirection())).findFirst()
                .orElseThrow(() -> new MicroColException(
                        String.format("There is not defined field for direction (%s)", direction)));
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
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
     * @param ownerName
     *            the ownerName to set
     */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location
     *            the location to set
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
     * @param colonyFields
     *            the colonyFields to set
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
     * @param constructions
     *            the constructions to set
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
     * @param colonyWarehouse
     *            the colonyWarehouse to set
     */
    public void setColonyWarehouse(final Map<String, Integer> colonyWarehouse) {
        this.colonyWarehouse = colonyWarehouse;
    }

}
