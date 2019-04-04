package org.microcol.model;

import java.util.OptionalInt;

import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;

/**
 * Generate unique id's for units. It should be accessed in a way:
 * 
 * <pre>
 * int myId = idManager.nextId();
 * </pre>
 */
public class IdManager {

    private Integer nextId;

    public IdManager(final int initialNextId) {
        Preconditions.checkArgument(initialNextId >= 0,
                "lastUsedId have to by equals or greater than 0.");
        nextId = initialNextId;
    }

    public static IdManager makeFromModelPo(final ModelPo modelPo) {
        final OptionalInt optInt = modelPo.getUnits().stream().mapToInt(unit -> unit.getId()).max();
        if (optInt.isPresent()) {
            return new IdManager(optInt.getAsInt() + 1);
        } else {
            return new IdManager(0);
        }
    }

    public int nextId() {
        return nextId++;
    }

}
