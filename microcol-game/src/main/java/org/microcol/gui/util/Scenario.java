package org.microcol.gui.util;

import com.google.common.base.Preconditions;

public class Scenario {

	private final String name;

	private final String fileName;

	public Scenario(final String name, final String fileName) {
		this.name = Preconditions.checkNotNull(name);
		this.fileName = Preconditions.checkNotNull(fileName);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

}
