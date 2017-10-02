package org.microcol.model.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.microcol.gui.MicroColException;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WorldMapDao {

	public WorldMapDao() {
	}

	@Deprecated
	public void save(final WorldMap worldMap, final String fileName) throws FileNotFoundException {
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonGeneratorFactory jgf = Json.createGeneratorFactory(properties);
		JsonGenerator jg = jgf.createGenerator(new FileOutputStream(new File(fileName)));
		jg.writeStartObject();
		jg.write("maxX", worldMap.getMaxX());
		jg.write("maxY", worldMap.getMaxY());
		jg.writeStartArray("map-layer");
		for (int rowNo = 1; rowNo <= worldMap.getMaxY(); rowNo++) {
			final StringBuilder buff = new StringBuilder();
			for (int columnNo = 1; columnNo <= worldMap.getMaxX(); columnNo++) {
				final TerrainType tt = worldMap.getTerrainTypeAt(Location.of(columnNo, rowNo));
				if (columnNo != 1) {
					buff.append(",");
				}
				buff.append(tt.getCode());
			}
			jg.write(buff.toString());
		}
		jg.writeEnd();
		jg.writeEnd();
		jg.close();
	}

	@Deprecated
	public WorldMap load(final String fileName) throws FileNotFoundException {
		final InputStream is = WorldMap.class.getResourceAsStream(fileName);
		Preconditions.checkState(is != null, "Unable to find (%s)", fileName);
		JsonReader reader = Json.createReader(is);
		JsonObject o = reader.readObject();
		final int maxX = readInt(o, "maxX");
		final int maxY = readInt(o, "maxY");
		JsonArray pole = o.getJsonArray("map-layer");
		final Map<Location, TerrainType> terrainMap = new HashMap<>();
		final AtomicInteger rowCx = new AtomicInteger(1);
		pole.forEach(row -> {
			Preconditions.checkState(row instanceof JsonString, "row is not instance of JsonString but (%s)",
					row.getClass().getName());
			final JsonString js = (JsonString) row;
			final AtomicInteger columnCx = new AtomicInteger(1);
			final String[] tileCodes = js.getString().split(",");
			for (final String code : tileCodes) {
				final TerrainType tt = TerrainType.valueOfCode(code);
				if (!tt.equals(TerrainType.OCEAN)) {
					terrainMap.put(Location.of(columnCx.get(), rowCx.get()), tt);
				}
				columnCx.incrementAndGet();
			}
			rowCx.incrementAndGet();
		});
		return new WorldMap(maxX, maxY, terrainMap);
	}

	public WorldMap loadMap(final String fileName) throws FileNotFoundException {
		final InputStream is = WorldMap.class.getResourceAsStream(fileName);
		Preconditions.checkState(is != null, "Unable to find (%s)", fileName);
		
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		
		GamePo loaded = gson.fromJson(new InputStreamReader(is), GamePo.class);		
		
		return new WorldMap(loaded);
	}

	int readInt(final JsonObject json, final String key) {
		if (json.containsKey(key)) {
			return json.getInt(key);
		} else {
			throw new MicroColException(String.format("Unable to find key '%s'", key));
		}
	}

}
