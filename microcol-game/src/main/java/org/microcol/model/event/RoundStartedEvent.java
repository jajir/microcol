package org.microcol.model.event;

import org.microcol.model.Calendar;
import org.microcol.model.Game;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class RoundStartedEvent extends GameEvent {
	private final Calendar calendar;

	public RoundStartedEvent(final Game game, final Calendar calendar) {
		super(game);

		this.calendar = Preconditions.checkNotNull(calendar);
	}

	public Calendar getCalendar() {
		return calendar;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("calendar", calendar)
			.toString();
	}
}
