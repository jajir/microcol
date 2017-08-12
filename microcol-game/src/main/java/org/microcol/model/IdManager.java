package org.microcol.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generate unique id's for units. It could be accessed in a static way:
 * 
 * <pre>
 *   int myId = IdManager.nextId();
 * </pre>
 */
public class IdManager {

	private final static AtomicInteger nextId = new AtomicInteger(0);

	public static final int nextId() {
		return nextId.incrementAndGet();
	}

}
