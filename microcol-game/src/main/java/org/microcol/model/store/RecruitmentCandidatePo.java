package org.microcol.model.store;

import org.microcol.model.UnitType;

import com.google.gson.annotations.JsonAdapter;

/** One persistent candidate; its identity is independent of unit IDs. */
public final class RecruitmentCandidatePo {

    private Integer id;

    @JsonAdapter(GsonUnitTypeAdapter.class)
    private UnitType type;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public UnitType getType() {
        return type;
    }

    public void setType(final UnitType type) {
        this.type = type;
    }
}
