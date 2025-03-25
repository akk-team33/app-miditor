package de.team33.midi;

import de.team33.patterns.enums.pan.Values;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.team33.midi.Util.CNV;

public enum PlayTrigger {

    ON(Choice.on(PlayState.OFF).apply(Action.OPEN)),
    START(Choice.on(PlayState.OFF).apply(Action.OPEN, Action.START),
          Choice.on(PlayState.READY).apply(Action.START),
          Choice.on(PlayState.PAUSED).apply(Action.START)),
    STOP(Choice.on(PlayState.PAUSED).apply(Action.RESET),
         Choice.on(PlayState.RUNNING).apply(Action.STOP, Action.RESET)),
    PAUSE(Choice.on(PlayState.RUNNING).apply(Action.STOP)),
    OFF(Choice.on(PlayState.READY).apply(Action.CLOSE, Action.RESET),
        Choice.on(PlayState.RUNNING).apply(Action.CLOSE, Action.RESET),
        Choice.on(PlayState.PAUSED).apply(Action.CLOSE, Action.RESET));

    private static final Values<PlayTrigger> VALUES = Values.of(PlayTrigger.class);
    private static final Map<PlayState, Set<PlayTrigger>> effectiveMap = new ConcurrentHashMap<>(0);

    private final Map<PlayState, List<Action>> map;

    PlayTrigger(final Choice... choices) {
        this.map = Stream.of(choices).collect(HashMap::new, PlayTrigger::put, Map::putAll);
    }

    private static void put(final Map<? super PlayState, ? super List<Action>> map, final Choice choice) {
        map.put(choice.state, choice.methods);
    }

    public static Set<PlayTrigger> allEffectiveOn(final PlayState state) {
        return effectiveMap.computeIfAbsent(state, PlayTrigger::newEffectiveSet);
    }

    private static Set<PlayTrigger> newEffectiveSet(final PlayState state) {
        return VALUES.stream()
                     .filter(value -> value.hasEffectOn(state))
                     .collect(Collectors.toUnmodifiableSet());
    }

    final Set<MidiPlayer.Channel> apply(final MidiPlayer player, final PlayState state) {
        return Optional.ofNullable(map.get(state))
                       .orElseGet(List::of)
                       .stream()
                       .map(action -> action.apply(player))
                       .collect(Collectors.toSet());
    }

    final boolean hasEffectOn(final PlayState state) {
        return map.containsKey(state);
    }

    @FunctionalInterface
    private interface Action extends Function<MidiPlayer, MidiPlayer.Channel> {

        Action OPEN = act(CNV.consumer(MidiPlayer::open), MidiPlayer.Channel.SET_STATE);
        Action START = act(mp -> mp.backing.start(), MidiPlayer.Channel.SET_STATE);
        Action STOP = act(mp -> mp.backing.stop(), MidiPlayer.Channel.SET_STATE);
        Action RESET = act(mp -> mp.backing.setTickPosition(0L), MidiPlayer.Channel.SET_POSITION);
        Action CLOSE = act(MidiPlayer::close, MidiPlayer.Channel.SET_STATE);

        @SuppressWarnings("BoundedWildcard")
        static Action act(final Consumer<MidiPlayer> consumer, final MidiPlayer.Channel result) {
            return sequencer -> {
                consumer.accept(sequencer);
                return result;
            };
        }
    }

    private record Choice(PlayState state, List<Action> methods) {

        static Stage on(final PlayState state) {
            return actions -> new Choice(state, Arrays.asList(actions));
        }

        private interface Stage {
            Choice apply(Action... actions);
        }
    }
}
