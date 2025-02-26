package de.team33.messaging;

import java.util.function.Consumer;

public interface Relay<MSG> extends Consumer<MSG>, Registry<MSG> {
}
