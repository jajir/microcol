package org.microcol.model;

import org.microcol.model.store.ModelDao;

public abstract class AbstractMapTest {
    
    private final ModelDao dao = new ModelDao();


    /**
     * Load predefined world map stored on class path.
     *
     * @param fileName
     *            required file name on class path
     * @return loaded world map
     */
    protected WorldMap loadPredefinedWorldMap(final String fileName) {
        return new WorldMap(dao.loadFromClassPath(fileName));
    }


    public ModelDao getDao() {
        return dao;
    }
    
}
