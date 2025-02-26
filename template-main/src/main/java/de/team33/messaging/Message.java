package de.team33.messaging;

public interface Message<SENDER> {
    SENDER getSender();
}
