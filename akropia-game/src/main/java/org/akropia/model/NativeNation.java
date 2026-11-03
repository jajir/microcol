package org.akropia.model;

/**
 * The eight native nations. Each nation builds one class of settlement, so the
 * class is fixed by the nation.
 */
public enum NativeNation {

    ARAWAK(NativeSettlementClass.VILLAGE), AZTEC(NativeSettlementClass.CITY),
    INCA(NativeSettlementClass.CITY), TUPI(NativeSettlementClass.CAMP),
    CHEROKEE(NativeSettlementClass.VILLAGE),
    IROQUOIS(NativeSettlementClass.VILLAGE), SIOUX(NativeSettlementClass.CAMP),
    APACHE(NativeSettlementClass.CAMP);

    private final NativeSettlementClass settlementClass;

    NativeNation(final NativeSettlementClass settlementClass) {
        this.settlementClass = settlementClass;
    }

    /**
     * @return the only settlement class this nation builds
     */
    public String getDisplayName() {
        return new String[] { "Phoenicians", "Egyptians", "Etruscans",
                "Libyans", "Thracians", "Illyrians", "Scythians",
                "Arabs" }[ordinal()];
    }

    public NativeSettlementClass getSettlementClass() {
        return settlementClass;
    }

}
