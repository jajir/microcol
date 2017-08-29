package org.microcol.gui.town;

import java.util.Map;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.Point;
import org.microcol.gui.europe.TitledPanel;
import org.microcol.model.ConstructionType;
import org.microcol.model.Town;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Show building factories and other structures build in colony.
 */
public class PanelTownStructures extends TitledPanel {
	
	private final static int COLUMN_1 = 90;
	private final static int COLUMN_2 = 250;
	private final static int COLUMN_3 = 410;

	private final static int ROW_1 = 10;
	private final static int ROW_2 = 100;
	private final static int ROW_3 = 190;

	/**
	 * Following structure define position of constructions images on town map.
	 */
	private final static Map<ConstructionType, Point> constructionPlaces = ImmutableMap
			.<ConstructionType, Point>builder()
			.put(ConstructionType.TOWN_HALL,            Point.of(COLUMN_2, ROW_1))
			.put(ConstructionType.LUMBER_MILL,          Point.of(COLUMN_1, ROW_3))
			.put(ConstructionType.CARPENTERS_SHOP,      Point.of(COLUMN_1, ROW_3))
			.put(ConstructionType.IRON_WORKS,           Point.of(COLUMN_2, ROW_3))
			.put(ConstructionType.BLACKSMITHS_SHOP,     Point.of(COLUMN_2, ROW_3))
			.put(ConstructionType.BLACKSMITHS_HOUSE,    Point.of(COLUMN_2, ROW_3))
			.put(ConstructionType.FORTRESS,             Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.FORT,                 Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.STOCKADE,             Point.of(COLUMN_1, ROW_1))
			.put(ConstructionType.CIGAR_FACTORY,        Point.of(COLUMN_2, ROW_2))
			.put(ConstructionType.TOBACCONISTS_SHOP,    Point.of(COLUMN_2, ROW_2))
			.put(ConstructionType.TOBACCONISTS_HOUSE,   Point.of(COLUMN_2, ROW_2))
			.put(ConstructionType.TEXTILE_MILL,         Point.of(COLUMN_1, ROW_2))
			.put(ConstructionType.WEAVERS_SHOP,         Point.of(COLUMN_1, ROW_2))
			.put(ConstructionType.WEAVERS_HOUSE,        Point.of(COLUMN_1, ROW_2))
			.put(ConstructionType.RUM_FACTORY,          Point.of(COLUMN_3, ROW_2))
			.put(ConstructionType.RUM_DISTILLERY,       Point.of(COLUMN_3, ROW_2))
			.put(ConstructionType.RUM_DISTILLERS_HOUSE, Point.of(COLUMN_3, ROW_2))
			.put(ConstructionType.FUR_FACTORY,          Point.of(COLUMN_3, ROW_3))
			.put(ConstructionType.FUR_TRADING_POST,     Point.of(COLUMN_3, ROW_3))
			.put(ConstructionType.FUR_TRADERS_HOUSE,    Point.of(COLUMN_3, ROW_3))
			.put(ConstructionType.ARSENAL,              Point.of(10, 10))
			.put(ConstructionType.MAGAZINE,             Point.of(10, 10))
			.put(ConstructionType.ARMORY,               Point.of(10, 10))
			.put(ConstructionType.SHIPYARD,             Point.of(10, 10))
			.put(ConstructionType.DRYDOCK,              Point.of(10, 10))
			.put(ConstructionType.DOCK,                 Point.of(10, 10))
			.put(ConstructionType.UNIVERSITY,           Point.of(10, 10))
			.put(ConstructionType.COLLEGE,              Point.of(10, 10))
			.put(ConstructionType.SCHOOLHOUSE,          Point.of(10, 10))
			.put(ConstructionType.WAREHOUSE_EXPANSION,  Point.of(10, 10))
			.put(ConstructionType.WAREHOUSE,            Point.of(10, 10))
			.put(ConstructionType.STABLES,              Point.of(10, 10))
			.put(ConstructionType.CATHEDRAL,            Point.of(10, 10))
			.put(ConstructionType.CHURCH,               Point.of(10, 10))
			.put(ConstructionType.NEWSPAPER,            Point.of(10, 10))
			.put(ConstructionType.PRINTING_PRESS,       Point.of(10, 10))
			.put(ConstructionType.CUSTOM_HOUSE,         Point.of(10, 10))
			.build();
	
	{
		Preconditions.checkState(ConstructionType.ALL.size() == constructionPlaces.size(),
				String.format(
						"There is different number of construction types '%s' and it's definitions of position '%s'.",
						ConstructionType.ALL.size(), constructionPlaces.size()));
	}

	private final Canvas canvas;
	
	private final LocalizationHelper localizationHelper;
	
	public PanelTownStructures(final LocalizationHelper localizationHelper) {
		super("Colony Structures", null);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		canvas = new Canvas(500, 300);
		getContentPane().getChildren().add(canvas);
		setMinWidth(500);
		setMinHeight(300);
	}
	
	void repaint(final Town town){
		town.getConstructions().forEach(construction -> {
			final Point position = constructionPlaces.get(construction.getType());
			Preconditions.checkNotNull(position,
					String.format("There is no defined position for construction type '%s'", position));
			final String name = localizationHelper.getConstructionTypeName(construction.getType());
			final GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setTextBaseline(VPos.CENTER);
			gc.setFill(Color.BLACK);
			gc.fillText(name, position.getX(), position.getY());
			gc.setStroke(Color.DARKGRAY);
			paintWorkerContainer(gc, position.add(-58,10));
			paintWorkerContainer(gc, position.add(-18,10));
			paintWorkerContainer(gc, position.add(22,10));
		});
	}
	
	private void paintWorkerContainer(final GraphicsContext gc, final Point point){
		gc.strokeRect(point.getX(), point.getY(), 35, 35);
	}
	
}
