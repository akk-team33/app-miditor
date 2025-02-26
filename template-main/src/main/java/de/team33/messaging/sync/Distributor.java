package de.team33.messaging.sync;

import de.team33.messaging.Listener;
import de.team33.messaging.Splitter;
import de.team33.messaging.util.ListenerUtil;
import net.team33.throwable.CollectiveRuntimeException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Distributor<MSG> implements Splitter<MSG> {
    private REGISTER<MSG> register = new REGISTER((REGISTER)null);
    private Set<MSG> initials = new HashSet();

    public Distributor() {
    }

    public final boolean add(Listener<? super MSG> listener) {
        boolean result;
        synchronized(this) {
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

    public final void pass(MSG message) throws CollectiveRuntimeException, IllegalStateException {
        REGISTER<MSG> reg = this.getCurrentRegister();
        if (reg == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " is closed");
        } else {
            List<Throwable> causes = new Vector();
            Iterator var5 = reg.iterator();

            while(var5.hasNext()) {
                Listener<? super MSG> listener = (Listener)var5.next();

                try {
                    listener.pass(message);
                } catch (RuntimeException var7) {
                    causes.add(var7);
                }
            }

            if (causes.size() != 0) {
                throw new CollectiveRuntimeException(causes);
            }
        }
    }

    public final synchronized boolean remove(Object listener) {
        this.register = new REGISTER(this.register);
        return this.register.remove(listener);
    }

    public final int size() {
        return this.getCurrentRegister().size();
    }

    private static class REGISTER<T> extends HashSet<Listener<? super T>> {
        private REGISTER() {
        }

        private REGISTER(Collection<? extends Listener<? super T>> c) {
            super(c);
        }
    }
}
