package org.microcol.gui.screen.colonizopedia;

import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.colony.ConstructionTypeName;
import org.microcol.gui.screen.congress.CongressText;
import org.microcol.gui.util.PaintService;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import javafx.scene.image.ImageView;

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
        table.add(cell(topic.getTitle(i18n)), 0, 0);
        table.add(cell(i18n.get(Colonizopedia.properties)), 1, 0);
        int row = 1;
        if (topic == ColonizopediaTopic.BUILDINGS) {
            for (final ConstructionType root : ConstructionType.ALL) {
                if (root.getUpgradeFrom().isPresent()) {
                    continue;
                }
                for (final ConstructionType type : root.getUpgradeChain()) {
                    final String name = buildingName(type);
                    final ImageView picture = new ImageView(
                            images.getConstructionImage(type).orElseThrow());
                    picture.setFitWidth(128);
                    picture.setFitHeight(96);
                    picture.setPreserveRatio(true);
                    picture.setAccessibleText(name);
                    final VBox identity = cell(name, picture);
                    identity.setId("colonizopediaBuilding_" + type.name());
                    table.add(identity, 0, row);
                    final VBox properties = cell(buildingProperties(type));
                    properties.setId(
                            "colonizopediaBuildingProperties_" + type.name());
                    table.add(properties, 1, row++);
                }
            }
        } else if (topic == ColonizopediaTopic.UNITS) {
            for (final UnitType type : UnitType.UNIT_TYPES) {
                final String name = names.getUnitName(type);
                final ImageView picture = new ImageView(
                        images.getUnitImage(type));
                picture.setFitWidth(Tile.TILE_WIDTH_IN_PX);
                picture.setFitHeight(Tile.TILE_WIDTH_IN_PX);
                picture.setPreserveRatio(true);
                picture.setAccessibleText(name);
                final VBox identity = cell(name, picture);
                identity.setId("colonizopediaUnit_" + type.name());
                table.add(identity, 0, row);
                final VBox properties = cell(unitProperties(type));
                properties.setId("colonizopediaUnitProperties_" + type.name());
                table.add(properties, 1, row++);
            }
        } else if (topic == ColonizopediaTopic.TERRAIN) {
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

    private String buildingName(final ConstructionType type) {
        return i18n.get(ConstructionTypeName.getNameForType(type));
    }

    private String buildingProperties(final ConstructionType type) {
        String details = i18n
                .get(Colonizopedia.valueOf("building_" + type.name()));
        details += "\n" + i18n.get(Colonizopedia.buildingCost,
                type.getRequiredHammers(), type.getRequiredTools());
        details += "\n" + (type.isBuildFromFounding()
                ? i18n.get(Colonizopedia.buildingFounded)
                : i18n.get(Colonizopedia.buildingPopulation,
                        type.getRequiredColonyPopulation()));
        if (type.getUpgradeFrom().isPresent()) {
            details += "\n" + i18n.get(Colonizopedia.buildingPredecessor,
                    buildingName(type.getUpgradeFrom().get()));
        }
        if (type.getRequiredFounder().isPresent()) {
            details += "\n" + i18n.get(Colonizopedia.buildingFounder,
                    CongressText.name(i18n, type.getRequiredFounder().get()));
        }
        if (type.getSlotsForWorkers() > 0) {
            details += "\n" + i18n.get(Colonizopedia.buildingWorkers,
                    type.getSlotsForWorkers());
        }
        if (type.getProductionPerTurn().isPresent()) {
            final Goods output = type.getProductionPerTurn().get();
            details += "\n" + i18n.get(Colonizopedia.buildingOutput,
                    goodsName(output.getType()), output.getAmount());
            if (type.getConsumptionPerTurn().isPresent()) {
                final Goods input = type.getConsumptionPerTurn().get();
                details += "\n" + i18n.get(Colonizopedia.buildingInput,
                        goodsName(input.getType()), input.getAmount());
            }
            if (type.getBaseProductionPerTurn() > 0) {
                details += "\n" + i18n.get(Colonizopedia.buildingBaseOutput,
                        goodsName(output.getType()),
                        type.getBaseProductionPerTurn());
            }
        }
        return details;
    }

    private String unitProperties(final UnitType type) {
        final boolean colonist = type != UnitType.ARTILLERY
                && type != UnitType.BRAVE && type != UnitType.TREASURE_TRAIN
                && !type.isShip() && !type.canHoldCargo();
        String details = i18n.get(Colonizopedia.unitMovement,
                i18n.get(type.isShip() ? Colonizopedia.water
                        : Colonizopedia.land),
                type.getSpeed()) + "\n"
                + i18n.get(Colonizopedia.unitCargo, type.getCargoCapacity())
                + "\n"
                + (type.isStorable()
                        ? i18n.get(Colonizopedia.unitPassenger,
                                type.getRequiredHolds())
                        : i18n.get(Colonizopedia.unitNotPassenger))
                + "\n"
                + (type.getEuropePrice() > 0
                        ? i18n.get(Colonizopedia.unitPrice,
                                type.getEuropePrice())
                        : i18n.get(Colonizopedia.unitNotForSale))
                + "\n"
                + i18n.get(Colonizopedia.unitCombat,
                        i18n.get(colonist ? Colonizopedia.unitArmedRoles
                                : type.canAttack() ? Colonizopedia.yes
                                        : Colonizopedia.no));
        final GoodsType expertise = type.getExpertInProducing();
        if (expertise != null) {
            details += "\n" + i18n.get(Colonizopedia.unitExpertise,
                    goodsName(expertise),
                    type.getProductionModifier(expertise));
            if (type == UnitType.EXPERT_FARMER) {
                details += "\n" + i18n.get(Colonizopedia.unitFarmer);
            } else if (type == UnitType.EXPERT_FISHERMAN) {
                details += "\n" + i18n.get(Colonizopedia.unitFisherman);
            }
        }
        if (colonist) {
            details += "\n" + i18n.get(Colonizopedia.unitPioneer,
                    yesNo(type.isCanPlowField()), yesNo(type.isCanBuildRoad()),
                    yesNo(type.isCanCutTrees()));
        }
        final Colonizopedia note;
        if (type == UnitType.SEASONED_SCOUT) {
            note = Colonizopedia.unitScout;
        } else if (type == UnitType.HARDY_PIONEER) {
            note = Colonizopedia.unitHardyPioneer;
        } else if (type == UnitType.VETERAN_SOLDIER) {
            note = Colonizopedia.unitVeteran;
        } else if (type == UnitType.JESUIT_MISSIONARY) {
            note = Colonizopedia.unitMissionary;
        } else if (type == UnitType.INDENTURED_SERVANT) {
            note = Colonizopedia.unitServant;
        } else if (type == UnitType.PETTY_CRIMINAL) {
            note = Colonizopedia.unitCriminal;
        } else if (type == UnitType.TREASURE_TRAIN) {
            note = Colonizopedia.unitTreasure;
        } else if (type == UnitType.BRAVE) {
            note = Colonizopedia.unitBrave;
        } else if (type == UnitType.ARTILLERY) {
            note = Colonizopedia.unitArtillery;
        } else {
            note = null;
        }
        return note == null ? details : details + "\n" + i18n.get(note);
    }

    private String yesNo(final boolean value) {
        return i18n.get(value ? Colonizopedia.yes : Colonizopedia.no);
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
