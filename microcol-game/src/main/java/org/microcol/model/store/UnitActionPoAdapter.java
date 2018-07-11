package org.microcol.model.store;

import java.io.IOException;

import org.microcol.model.ChainOfCommandOptionalStrategy;
import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.unit.UnitActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class UnitActionPoAdapter extends TypeAdapter<UnitActionPo> {

    private final Logger logger = LoggerFactory.getLogger(UnitActionPoAdapter.class);

    /**
     * Holds input parameters for reader.
     */
    private class ReadParameters {
	private final UnitActionType type;
	private final JsonReader in;

	ReadParameters(final UnitActionType type, final JsonReader in) {
	    this.type = Preconditions.checkNotNull(type);
	    this.in = Preconditions.checkNotNull(in);
	}

	@Override
	public String toString() {
	    return MoreObjects.toStringHelper(getClass()).add("type", type).add("in", in).toString();
	}
    }

    /**
     * For given unit action type try to create proper persistent object instance.
     */
    private final ChainOfCommandStrategy<ReadParameters, UnitActionPo> reader = new ChainOfCommandStrategy<>(
	    Lists.newArrayList(read -> {
		if (UnitActionType.noAction.equals(read.type)) {
		    return new UnitActionNoActionPo();
		}
		return null;
	    }, read -> {
		if (UnitActionType.plowField.equals(read.type)) {
		    try {
			final UnitActionPlowFieldPo out = new UnitActionPlowFieldPo();
			Preconditions.checkState(read.in.hasNext(), "There should be remainingTurns key value");
			final String name = read.in.nextName();
			Preconditions.checkState("remainingTurns".equals(name));
			out.setRemainingTurns(read.in.nextInt());
			return out;
		    } catch (IOException e) {
			logger.error(e.getMessage(), e);
		    }
		}
		return null;
	    }));

    /**
     * Holds parameters for writer.
     */
    private class WriteParameters {

	private final JsonWriter out;
	private final UnitActionPo value;

	WriteParameters(final JsonWriter out, final UnitActionPo value) {
	    this.out = Preconditions.checkNotNull(out);
	    this.value = Preconditions.checkNotNull(value);
	}

	@Override
	public String toString() {
	    return MoreObjects.toStringHelper(getClass()).add("value", value).add("out", out).toString();
	}
    }

    /**
     * It's chain of commands for writing to json. Just unit actions with special
     * attributes have own command.
     */
    private final ChainOfCommandOptionalStrategy<WriteParameters, Boolean> write = new ChainOfCommandOptionalStrategy<>(
	    Lists.newArrayList(write -> {
		if (UnitActionType.plowField.equals(write.value.getType())) {
		    final UnitActionPlowFieldPo ua = (UnitActionPlowFieldPo) write.value;
		    try {
			write.out.name("remainingTurns").value(ua.getRemainingTurns());
		    } catch (IOException e) {
			logger.error(e.getMessage(), e);
		    }
		    return true;
		}
		return null;
	    }));

    @Override
    public void write(final JsonWriter out, final UnitActionPo value) throws IOException {
	Preconditions.checkNotNull(value);
	out.beginObject();
	out.name("type").value(value.getType().name());
	write.apply(new WriteParameters(out, value));
	out.endObject();
    }

    @Override
    public UnitActionPo read(final JsonReader in) throws IOException {
	in.beginObject();
	Preconditions.checkState(in.hasNext(), "There should be some content");
	final String name = in.nextName();
	Preconditions.checkState("type".equals(name));
	final UnitActionType type = UnitActionType.valueOf(in.nextString());
	final UnitActionPo out = reader.apply(new ReadParameters(type, in));
	in.endObject();
	return out;
    }

}
