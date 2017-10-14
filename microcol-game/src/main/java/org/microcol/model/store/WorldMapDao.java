package org.microcol.model.store;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.microcol.gui.MicroColException;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WorldMapDao {

	public WorldMapDao() {
	}

	public WorldMap loadMap(final String fileName) {
		try {
			return internalLoadMap(fileName);
		} catch (FileNotFoundException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

	private WorldMap internalLoadMap(final String fileName) throws FileNotFoundException {
		final InputStream is = WorldMap.class.getResourceAsStream(fileName);
		Preconditions.checkArgument(is != null, "Unable to find (%s)", fileName);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		GamePo loaded = gson.fromJson(new InputStreamReader(is), GamePo.class);

		return new WorldMap(loaded);
	}

}
