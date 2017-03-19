package org.microcol.model.event;

import org.microcol.model.Calendar;
import org.microcol.model.Model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class RoundStartedEvent extends ModelEvent {
	private final Calendar calendar;

	public RoundStartedEvent(final Model model, final Calendar calendar) {
		super(model);

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
