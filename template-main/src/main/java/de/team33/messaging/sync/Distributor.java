package de.team33.messaging.sync;

import de.team33.messaging.CollectiveRuntimeException;
import de.team33.messaging.Splitter;
import de.team33.messaging.util.ListenerUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;

public class Distributor<MSG> implements Splitter<MSG> {
    private REGISTER<MSG> register = new REGISTER();
    private Set<MSG> initials = new HashSet();

    public Distributor() {
    }

    public final boolean add(Consumer<? super MSG> listener) {
        boolean result;
        synchronized (this) {
            if (this.register == null) {
                result = false;
            } else {
                this.register = new REGISTER(this.register);
                result = this.register.add(listener);
            }
        }

        if (result) {
            ListenerUtil.pass(listener, this.getInitials());
        }

        return result;
    }

    public final synchronized boolean addInitials(Collection<? extends MSG> initials) {
        this.initials = new HashSet(this.initials);
        return this.initials.addAll(initials);
    }

    public final synchronized void clear() {
        if (this.register != null) {
            this.register.clear();
        }

    }

    public final synchronized void close() {
        this.clear();
        this.initials.clear();
        this.register = null;
    }

    private final synchronized REGISTER<MSG> getCurrentRegister() {
        return this.register;
    }

    private final synchronized Set<MSG> getInitials() {
        return this.initials;
    }

    public final void accept(MSG message) throws CollectiveRuntimeException, IllegalStateException {
        REGISTER<MSG> reg = this.getCurrentRegister();
        if (reg == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " is closed");
        } else {
            List<Throwable> causes = new Vector();
            Iterator var5 = reg.iterator();

            while (var5.hasNext()) {
                Consumer<? super MSG> listener = (Consumer) var5.next();

                try {
                    listener.accept(message);
                } catch (RuntimeException var7) {
                    causes.add(var7);
                }
            }

            if (causes.size() != 0) {
                throw new CollectiveRuntimeException(causes);
            }
        }
    }

    public final int size() {
        return this.getCurrentRegister().size();
    }

    private static class REGISTER<T> extends HashSet<Consumer<? super T>> {
        private REGISTER() {
        }

        private REGISTER(Collection<? extends Consumer<? super T>> c) {
            super(c);
        }
    }
}
