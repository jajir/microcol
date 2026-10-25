package org.microcol.gui.screen.colonizopedia;

import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.PaintService;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.TerrainType;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Illustrated reference data read from the same definitions as the game. */
final class ColonizopediaTables {

    private final I18n i18n;
    private final LocalizationHelper names;
    private final ImageProvider images;

    ColonizopediaTables(final I18n i18n, final ImageProvider images) {
        this.i18n = i18n;
        this.names = new LocalizationHelper(i18n);
        this.images = images;
    }

    GridPane create(final ColonizopediaTopic topic) {
        final GridPane table = new GridPane();
        table.setId("colonizopediaReference");
        final ColumnConstraints nameColumn = new ColumnConstraints();
        nameColumn.setPercentWidth(34);
        final ColumnConstraints detailColumn = new ColumnConstraints();
        detailColumn.setPercentWidth(66);
        table.getColumnConstraints().addAll(nameColumn, detailColumn);
        table.add(cell(i18n.get(topic == ColonizopediaTopic.TERRAIN
                ? Colonizopedia.topic_terrain_title
                : Colonizopedia.topic_resources_title)), 0, 0);
        table.add(cell(i18n.get(Colonizopedia.properties)), 1, 0);
        int row = 1;
        if (topic == ColonizopediaTopic.TERRAIN) {
            for (final TerrainType terrain : TerrainType.TERRAINS) {
                final String name = names.getTerrainName(terrain);
                final HBox previews = new HBox(6,
                        preview(terrain, false, ResourceOverlay.NONE));
                if (terrain.isCanHaveTree()) {
                    previews.getChildren()
                            .add(preview(terrain, true, ResourceOverlay.NONE));
                }
                final VBox identity = cell(name, previews);
                identity.setId("colonizopediaTerrain_" + terrain.name());
                table.add(identity, 0, row);
                String details = i18n
                        .get(terrain.isLand() ? Colonizopedia.land
                                : Colonizopedia.water)
                        + "\n" + terrainProperties(terrain, false);
                if (terrain.isCanHaveTree()) {
                    details += "\n" + i18n.get(Colonizopedia.forest) + "\n"
                            + terrainProperties(terrain, true);
                }
                details += "\n" + i18n.get(Colonizopedia.fields,
                        i18n.get(terrain.isCanHaveField() ? Colonizopedia.yes
                                : Colonizopedia.no));
                final VBox properties = cell(details);
                properties.setId(
                        "colonizopediaTerrainProperties_" + terrain.name());
                table.add(properties, 1, row++);
            }
        } else {
            for (final ResourceOverlay resource : ResourceOverlay.values()) {
                if (resource == ResourceOverlay.NONE) {
                    continue;
                }
                final TerrainType example = TerrainType.TERRAINS.stream()
                        .filter(resource::canBeOn).findFirst().orElseThrow();
                final VBox identity = cell(
                        names.getResourceOverlayName(resource),
                        preview(example, false, resource));
                identity.setId("colonizopediaResource_" + resource.name());
                table.add(identity, 0, row);
                final String terrains = TerrainType.TERRAINS.stream()
                        .filter(resource::canBeOn).map(names::getTerrainName)
                        .collect(Collectors.joining(", "));
                final String outputs = resource.getOutputs().stream()
                        .map(goods -> goodsName(goods) + " +"
                                + resource.getBonusFor(goods))
                        .collect(Collectors.joining(", "));
                final String reserves = resource.isFinite()
                        ? i18n.get(Colonizopedia.reserves, resource.getOutputs()
                                .stream()
                                .map(goods -> goodsName(goods) + " "
                                        + resource.getInitialReserve(goods))
                                .collect(Collectors.joining(", ")))
                        : i18n.get(Colonizopedia.renewable);
                final VBox properties = cell(
                        i18n.get(Colonizopedia.occurrence, terrains) + "\n"
                                + i18n.get(Colonizopedia.bonus, outputs) + "\n"
                                + reserves);
                properties.setId(
                        "colonizopediaResourceProperties_" + resource.name());
                table.add(properties, 1, row++);
            }
        }
        return table;
    }

    private String terrainProperties(final TerrainType terrain,
            final boolean forest) {
        final String production = GoodsType.GOOD_TYPES.stream().map(goods -> {
            final Optional<TerrainType.Production> output = forest
                    ? terrain.getForestProduction(goods)
                    : terrain.getBaseProduction(goods);
            return output.map(value -> goodsName(goods) + " " + value.getBase())
                    .orElse("");
        }).filter(text -> !text.isEmpty()).collect(Collectors.joining(", "));
        return i18n.get(Colonizopedia.movementDefense,
                forest ? terrain.getMoveCostWithTree() : terrain.getMoveCost(),
                forest ? terrain.getDefenseBonusWithTree()
                        : terrain.getDefenseBonus())
                + "\n"
                + i18n.get(Colonizopedia.production,
                        production.isEmpty() ? i18n.get(Colonizopedia.none)
                                : production);
    }

    private String goodsName(final GoodsType goods) {
        return i18n.get(GoodsTypeName.getNameForGoodsType(goods));
    }

    private Canvas preview(final TerrainType terrain, final boolean forest,
            final ResourceOverlay resource) {
        final Canvas canvas = new Canvas(Tile.TILE_WIDTH_IN_PX,
                Tile.TILE_WIDTH_IN_PX);
        canvas.getGraphicsContext2D().drawImage(images.getTerrainImage(terrain),
                0, 0);
        if (forest) {
            canvas.getGraphicsContext2D().drawImage(
                    images.getImage(ImageLoaderTerrain.IMG_TREE_1), 0, 0);
        }
        if (resource != ResourceOverlay.NONE) {
            PaintService.paintResource(canvas.getGraphicsContext2D(),
                    Point.of(0, 0), resource, images);
        }
        canvas.setAccessibleText(names.getTerrainName(terrain)
                + (forest ? " — " + i18n.get(Colonizopedia.forest) : "")
                + (resource == ResourceOverlay.NONE ? ""
                        : " — " + names.getResourceOverlayName(resource)));
        return canvas;
    }

    private VBox cell(final String text, final Node... illustrations) {
        final Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        final VBox cell = new VBox(6);
        cell.getChildren().addAll(illustrations);
        cell.getChildren().add(label);
        cell.setPadding(new Insets(8));
        cell.setStyle("-fx-border-color: #9b9585; -fx-border-width: 0 0 1 0;");
        GridPane.setFillHeight(cell, true);
        return cell;
    }
}
