package de.team33.messaging;

public interface Registry<MSG> {
    <MSX extends MSG> Register<MSX> getRegister(Class<MSX> var1);
}
