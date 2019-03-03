package org.microcol.gui.screen.game.components;

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
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Generate localized message for message bar.
 */
@Singleton
public class StatusBarMessageService {

    private final I18n i18n;

    private final GamePreferences gamePreferences;

    private final LocalizationHelper localizationHelper;

    @Inject
    StatusBarMessageService(final I18n i18n, final GamePreferences gamePreferences,
            final LocalizationHelper localizationHelper) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
    }

    public String getStatusMessage(final Location where, final Player player,
            final TerrainType terrain, final List<Unit> units, final Optional<Colony> oColony) {
        final StringBuilder buff = new StringBuilder();
        if (gamePreferences.isDevelopment()) {
            buff.append("[");
            buff.append(where.getX());
            buff.append(",");
            buff.append(where.getY());
            buff.append("] - ");
        }
        if (player.isVisible(where)) {
            buff.append(i18n.get(Loc.statusBar_tile_start));
            buff.append(localizationHelper.getTerrainName(terrain));

            final List<SbPart> parts = new ArrayList<>();
            addPartColony(parts, oColony);
            addPartUnits(parts, units);

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
        } else {
            buff.append(i18n.get(Loc.statusBar_tile_notExplored));
        }
        return buff.toString();
    }

    private void addPartColony(final List<SbPart> parts, final Optional<Colony> oColony) {
        if (oColony.isPresent()) {
            parts.add(new SbPartColony(i18n, oColony.get().getName()));
        }
    }

    private void addPartUnits(final List<SbPart> parts, final List<Unit> units) {
        final Map<UnitType, Long> groupedUnits = units.stream().map(unit -> unit.getType())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (Map.Entry<UnitType, Long> entry : groupedUnits.entrySet()) {
            final UnitType unitType = entry.getKey();
            final long count = entry.getValue();
            parts.add(new SbPartUnits(i18n, unitType, count));
        }
    }

}
