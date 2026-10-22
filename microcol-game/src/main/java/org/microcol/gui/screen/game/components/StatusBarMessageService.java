package org.microcol.gui.screen.game.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.microcol.gui.Loc;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.NativeHostility;
import org.microcol.model.NativeLandQuote;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeRelationView;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeSettlementClass;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitNativeBrave;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Generate localized message for message bar.
 */
@Singleton
public class StatusBarMessageService {

    /** Decimal places kept when removing binary floating-point noise. */
    private static final int NOISE_DIGITS = 9;

    private final I18n i18n;

    private final GamePreferences gamePreferences;

    private final LocalizationHelper localizationHelper;

    @Inject
    StatusBarMessageService(final I18n i18n,
            final GamePreferences gamePreferences,
            final LocalizationHelper localizationHelper) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.localizationHelper = Preconditions
                .checkNotNull(localizationHelper);
    }

    public String getStatusMessage(final Location where, final Player player,
            final TerrainType terrain, final List<Unit> units,
            final Optional<Colony> oColony) {
        return getStatusMessage(where, player, terrain, units, oColony,
                Optional.empty());
    }

    /**
     * @param oNativeSettlement settlement the player may see on this square;
     *                          the model passes it only in present sight
     */
    public String getStatusMessage(final Location where, final Player player,
            final TerrainType terrain, final List<Unit> units,
            final Optional<Colony> oColony,
            final Optional<NativeSettlement> oNativeSettlement) {
        return getStatusMessage(where, player, terrain, units, oColony,
                oNativeSettlement, false);
    }

    /**
     * @param oNativeSettlement settlement the player may see on this square;
     *                          the model passes it only in present sight
     * @param lostCityRumor     whether an unresolved lost-city rumor waits on
     *                          the square; it is named only in present sight,
     *                          so a remembered square reveals nothing
     */
    public String getStatusMessage(final Location where, final Player player,
            final TerrainType terrain, final List<Unit> units,
            final Optional<Colony> oColony,
            final Optional<NativeSettlement> oNativeSettlement,
            final boolean lostCityRumor) {
        final StringBuilder buff = new StringBuilder();
        if (gamePreferences.isDevelopment()) {
            buff.append("[");
            buff.append(where.getX());
            buff.append(",");
            buff.append(where.getY());
            buff.append("] - ");
        }
        if (player.isVisible(where)) {
            // Terrain is remembered; units and colonies are shown only while
            // the tile is in present sight, otherwise a stale colony note.
            final boolean current = player.isCurrentlyVisible(where);
            buff.append(i18n.get(Loc.statusBar_tile_start));
            buff.append(localizationHelper.getTerrainName(terrain));

            final List<SbPart> parts = new ArrayList<>();
            if (current) {
                addPartColony(parts, oColony);
                addPartUnits(parts, units);
            }

            if (!parts.isEmpty()) {
                buff.append(" ");
                buff.append(i18n.get(Loc.statusBar_tile_withUnit));
                int index = 0;
                for (final SbPart part : parts) {
                    if (index == 0) {
                        buff.append(" ");
                    } else if (index + 1 == parts.size()) {
                        buff.append(" ");
                        buff.append(i18n.get(Loc.and));
                        buff.append(" ");
                    } else {
                        buff.append(", ");
                    }
                    part.writeTo(buff);
                    index++;
                }
            }
            buff.append(".");
            // A learned settlement stays named under fog, as a remembered
            // colony.
            oNativeSettlement.ifPresent(settlement -> buff.append(" ")
                    .append(nativeSettlementText(settlement)));
            // The marker's meaning in words, so it never depends on colour.
            if (current && lostCityRumor) {
                buff.append(" ").append(i18n.get(Loc.lostCityRumorStatus));
            }
            if (!current) {
                player.getKnownColonyAt(where)
                        .ifPresent(sighting -> buff.append(" ")
                                .append(i18n.get(Loc.colonyLastSeen,
                                        sighting.getColonyName(),
                                        sighting.getOwner().getDisplayName(),
                                        sighting.getObservedYear())));
            }
        } else {
            buff.append(i18n.get(Loc.statusBar_tile_notExplored));
        }
        return buff.toString();
    }

    /**
     * Nation, class and capital status are stated in words, so the settlement
     * never depends on its marker colour alone.
     */
    public String nativeSettlementText(final NativeSettlement settlement) {
        return i18n.get(
                settlement.isCapital() ? Loc.nativeCapitalStatus
                        : Loc.nativeSettlementStatus,
                i18n.get(nationLabel(settlement.getNation())),
                i18n.get(classLabel(settlement.getSettlementClass())));
    }

    /**
     * One sentence for a native brave the player currently sees. The nation is
     * public once the brave is seen; the home is named only when the player has
     * learned that settlement, so a hidden home is never disclosed.
     *
     * @param brave     required visible brave
     * @param knownHome the brave's home if the player has learned it
     * @return localized nation and home state
     */
    public String nativeBraveText(final UnitNativeBrave brave,
            final Optional<NativeSettlement> knownHome) {
        final String nation = i18n.get(nationLabel(brave.getNation()));
        if (brave.getHomeSettlementId().isEmpty()) {
            return i18n.get(Loc.nativeBraveHomeless, nation);
        }
        if (knownHome.isEmpty()) {
            return i18n.get(Loc.nativeBraveHomeUnknown, nation);
        }
        final NativeSettlement home = knownHome.get();
        return i18n.get(
                home.isCapital() ? Loc.nativeBraveHomeCapital
                        : Loc.nativeBraveHome,
                nation, i18n.get(classLabel(home.getSettlementClass())));
    }

    /**
     * Words for the settlement that owns a land claim: nation, class and
     * capital status. The caller passes only a settlement the player has
     * learned.
     *
     * @param settlement required learned settlement
     * @return localized claimant description
     */
    public String nativeClaimantText(final NativeSettlement settlement) {
        return i18n.get(
                settlement.isCapital() ? Loc.nativeLandClaimantCapital
                        : Loc.nativeLandClaimant,
                i18n.get(nationLabel(settlement.getNation())),
                i18n.get(classLabel(settlement.getSettlementClass())));
    }

    /**
     * One sentence for a learned settlement's relation toward the player's own
     * European: attitude band, signed alarm change with its direction, and the
     * visible provoker or its absence. Each meaning is stated in words, so
     * colour and the exclamation marks are never the only reading.
     *
     * @param view required view from {@code Model.getNativeRelationView}
     * @return localized attitude, change, direction and provoker
     */
    public String nativeRelationText(final NativeRelationView view) {
        return String.join(" ", nativeAttitudeText(view),
                nativeAlarmChangeText(view), nativeRivalText(view));
    }

    /**
     * Hover text of a learned settlement: the relation sentence and the key
     * that opens the keyboard-reachable details.
     *
     * @param view required view from {@code Model.getNativeRelationView}
     * @return localized relation text followed by the details hint
     */
    public String nativeRelationHoverText(final NativeRelationView view) {
        return nativeRelationText(view) + " " + i18n.get(Loc.nativeDetailsHint);
    }

    /**
     * @param view required view from {@code Model.getNativeRelationView}
     * @return localized attitude band in words
     */
    public String nativeAttitudeText(final NativeRelationView view) {
        return i18n.get(Loc.nativeAttitudeLine,
                i18n.get(attitudeLabel(view.getAttitude())));
    }

    /**
     * @param view required view from {@code Model.getNativeRelationView}
     * @return localized exact signed alarm change with its rising, falling or
     *         stable direction in words
     */
    public String nativeAlarmChangeText(final NativeRelationView view) {
        return i18n.get(Loc.nativeAlarmChangeLine,
                signedAlarmChange(view.getAlarmChange()),
                i18n.get(trendLabel(view.getTrend())));
    }

    /**
     * @param view required view from {@code Model.getNativeRelationView}
     * @return localized name of the visible provoker, or that it is unknown
     */
    public String nativeRivalText(final NativeRelationView view) {
        return view.getProvokingRival()
                .map(player -> i18n.get(Loc.nativeRivalProvoker,
                        player.getDisplayName()))
                .orElseGet(() -> i18n.get(Loc.nativeRivalUnknown));
    }

    /**
     * Exact signed change. Model changes are sums of quarters, halves and the
     * mean term; only binary floating-point noise beyond the ninth decimal is
     * removed, so every genuine fraction prints as itself. A negative zero
     * reads as zero.
     */
    static String signedAlarmChange(final double change) {
        if (change == 0) {
            return "0";
        }
        final BigDecimal magnitude = BigDecimal.valueOf(Math.abs(change))
                .setScale(NOISE_DIGITS, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        if (magnitude.signum() == 0) {
            return "0";
        }
        return (change > 0 ? "+" : "-") + magnitude.toPlainString();
    }

    private static Loc attitudeLabel(final NativeHostility attitude) {
        switch (attitude) {
        case CONTENT:
            return Loc.nativeAttitudeContent;
        case UNEASY:
            return Loc.nativeAttitudeUneasy;
        case ANGRY:
            return Loc.nativeAttitudeAngry;
        case HOSTILE:
            return Loc.nativeAttitudeHostile;
        default:
            throw new IllegalArgumentException(
                    "No label for attitude " + attitude);
        }
    }

    private static Loc trendLabel(final NativeRelationView.AlarmTrend trend) {
        switch (trend) {
        case RISING:
            return Loc.nativeTrendRising;
        case FALLING:
            return Loc.nativeTrendFalling;
        default:
            return Loc.nativeTrendStable;
        }
    }

    /**
     * One status sentence for a claimed square, using the model's quote.
     *
     * @param quote required quote from {@code Model.quoteNativeLand}
     * @return localized claimant, paid or waived state, and price when unpaid
     */
    public String nativeLandText(final NativeLandQuote quote) {
        final String claimant = nativeClaimantText(quote.getClaimant());
        switch (quote.getStatus()) {
        case PAID:
            return i18n.get(Loc.nativeLandHoverPaid, claimant);
        case WAIVED:
            return i18n.get(Loc.nativeLandHoverWaived, claimant);
        default:
            return i18n.get(Loc.nativeLandHoverUnpaid, claimant,
                    quote.getPrice());
        }
    }

    /**
     * One line per known claimed square a founding would use, marked as the
     * colony square or the first field, with its claimant and status.
     *
     * @param land  squares from {@code Model.getFoundingLand}, colony first
     * @param known quotes for the known claimed squares among them
     * @return localized lines, empty when no known claimed land is used
     */
    public String foundingLandText(final List<Location> land,
            final List<NativeLandQuote> known) {
        return known.stream()
                .map(quote -> i18n.get(
                        quote.getLocation().equals(land.get(0))
                                ? Loc.nativeLandColonySquare
                                : Loc.nativeLandFirstField,
                        nativeLandText(quote)))
                .collect(Collectors.joining("\n"));
    }

    /**
     * @param nation required native nation
     * @return the localized key of the nation's name
     */
    public static Loc nationLabel(final NativeNation nation) {
        switch (nation) {
        case ARAWAK:
            return Loc.nativeNationArawak;
        case AZTEC:
            return Loc.nativeNationAztec;
        case INCA:
            return Loc.nativeNationInca;
        case TUPI:
            return Loc.nativeNationTupi;
        case CHEROKEE:
            return Loc.nativeNationCherokee;
        case IROQUOIS:
            return Loc.nativeNationIroquois;
        case SIOUX:
            return Loc.nativeNationSioux;
        case APACHE:
            return Loc.nativeNationApache;
        default:
            throw new IllegalArgumentException("No label for nation " + nation);
        }
    }

    private static Loc classLabel(final NativeSettlementClass settlementClass) {
        switch (settlementClass) {
        case CAMP:
            return Loc.nativeClassCamp;
        case VILLAGE:
            return Loc.nativeClassVillage;
        case CITY:
            return Loc.nativeClassCity;
        default:
            throw new IllegalArgumentException(
                    "No label for class " + settlementClass);
        }
    }

    private void addPartColony(final List<SbPart> parts,
            final Optional<Colony> oColony) {
        if (oColony.isPresent()) {
            parts.add(new SbPartColony(i18n, oColony.get().getName()));
        }
    }

    private void addPartUnits(final List<SbPart> parts,
            final List<Unit> units) {
        final Map<UnitType, Long> groupedUnits = units.stream()
                .map(unit -> unit.getType()).collect(Collectors.groupingBy(
                        Function.identity(), Collectors.counting()));
        for (Map.Entry<UnitType, Long> entry : groupedUnits.entrySet()) {
            final UnitType unitType = entry.getKey();
            final long count = entry.getValue();
            parts.add(new SbPartUnits(i18n, unitType, count));
        }
    }

}
