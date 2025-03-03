package de.team33.patterns.notes.alpha;

import de.team33.patterns.building.elara.DataBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public final class Mapping {

    @SuppressWarnings("rawtypes")
    private final Map<Channel, Supplier> backing;

    @SuppressWarnings("rawtypes")
    private Mapping(final Map<Channel, Supplier> backing) {
        this.backing = Collections.unmodifiableMap(new HashMap<>(backing));
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    public final <M> M get(final Channel<M> channel) {
        return (M) Optional.ofNullable(backing.get(channel))
                           .map(Supplier::get)
                           .orElseThrow(() -> new NoSuchElementException("no mapping found for channel <" + channel.name() + ">"));
    }

    @SuppressWarnings("rawtypes")
    public static final class Builder extends DataBuilder<Map<Channel, Supplier>, Mapping, Builder> {

        private Builder() {
            super(new HashMap<>(0), Mapping::new, Builder.class);
        }

        public final <M> Builder put(final Channel<M> channel, final Supplier<? extends M> supplier) {
            return setup(map -> map.put(channel, supplier));
        }
    }
}
