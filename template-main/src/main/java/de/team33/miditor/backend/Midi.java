package de.team33.miditor.backend;

interface Midi {

    interface MetaMessage {

        enum Type {

            TRACK_NAME(0x03),
            SET_TEMPO(0x51);

            private final int value;

            Type(final int value) {
                this.value = value;
            }

            final int value() {
                return value;
            }
        }
    }
}
