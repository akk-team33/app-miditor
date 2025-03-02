package de.team33.miditor.backend;

interface Channel<M> extends de.team33.patterns.notes.eris.Channel<M> {

    Channel<State> SET_STATE = () -> "SET_STATE";
    Channel<Long> SET_POSITION = () -> "SET_POSITION";

    String name();
}
