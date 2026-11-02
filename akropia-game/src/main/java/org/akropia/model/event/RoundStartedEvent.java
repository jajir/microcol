package org.akropia.model.event;

import org.akropia.model.Calendar;
import org.akropia.model.Model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class RoundStartedEvent extends AbstractModelEvent {
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
        return MoreObjects.toStringHelper(this).add("calendar", calendar)
                .toString();
    }
}
