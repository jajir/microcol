package org.microcol.integration;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Connector {

	private final List<Character> VALUES = ImmutableList.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b');

	private final Character value;

	private Connector(final Character ch) {
		value = Preconditions.checkNotNull(ch);
	}

	public static Connector of(final Character ch) {
		return new Connector(ch);
	}

	public Connector rotateRight() {
		int pos = VALUES.indexOf(value) + 3;
		if (pos > 11) {
			pos = pos - 12;
		}
		return Connector.of(VALUES.get(pos));
	}

	public char get() {
		return value;
	}

}
