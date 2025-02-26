package de.team33.messaging.sync;

import de.team33.messaging.Listener;
import de.team33.messaging.Register;
import de.team33.messaging.Relay;
import net.team33.throwable.CollectiveRuntimeException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Router<MSG> implements Relay<MSG> {
    private REGISTRY registry = new REGISTRY((REGISTRY)null);
    private Set<MSG> initials = new HashSet();

    public Router() {
    }

    private static <MSG, MSX extends MSG> Set<MSX> getSubset(Class<MSX> msgClass, Collection<MSG> messages) {
        Set<MSX> result = new HashSet();
        Iterator var4 = messages.iterator();

        while(var4.hasNext()) {
            MSG message = (MSG)var4.next();
            if (msgClass.isAssignableFrom(message.getClass())) {
                result.add(msgClass.cast(message));
            }
        }

        return result;
    }

    public final synchronized boolean addInitials(Collection<? extends MSG> initials) {
        this.initials = new HashSet(this.initials);
        return this.initials.addAll(initials);
    }

    public final synchronized void clear() {
        if (this.registry != null) {
            Iterator var2 = this.registry.values().iterator();

            while(var2.hasNext()) {
                Distributor<?> next = (Distributor)var2.next();
                next.close();
            }

            this.registry.clear();
        }

    }

    public final synchronized void close() {
        this.clear();
        this.registry = null;
    }

    private final synchronized REGISTRY getCurrentRegistry() {
        return this.registry;
    }

    public final synchronized <MSX extends MSG> Register<MSX> getRegister(Class<MSX> msgClass) {
        if (this.registry == null) {
            return new DUMMY();
        } else {
            if (!this.registry.containsKey(msgClass)) {
                this.registry = new REGISTRY(this.registry);
                Distributor<MSX> distributor = new Distributor();
                distributor.addInitials(getSubset(msgClass, this.initials));
                this.registry.put(msgClass, distributor);
            }

            return (Register)this.registry.get(msgClass);
        }
    }

    private final void pass(Distributor distributor, Object message) {
        distributor.pass(message);
    }

    public final void pass(MSG message) {
        REGISTRY reg = this.getCurrentRegistry();
        if (reg == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " is closed");
        } else {
            List<Throwable> causes = new Vector();
            Iterator var5 = reg.entrySet().iterator();

            while(var5.hasNext()) {
                Map.Entry<Class<?>, Distributor<?>> entry = (Map.Entry)var5.next();
                if (((Class)entry.getKey()).isAssignableFrom(message.getClass())) {
                    try {
                        this.pass((Distributor)entry.getValue(), message);
                    } catch (CollectiveRuntimeException var7) {
                        causes.addAll(var7.getCauses());
                    }
                }
            }

            if (causes.size() != 0) {
                throw new CollectiveRuntimeException(causes);
            }
        }
    }

    public final int size() {
        int ret = 0;
        REGISTRY reg = this.getCurrentRegistry();

        Distributor supplier;
        for(Iterator var4 = reg.values().iterator(); var4.hasNext(); ret += supplier.size()) {
            supplier = (Distributor)var4.next();
        }

        return ret;
    }

    private static class DUMMY<T> implements Register<T> {
        private DUMMY() {
        }

        public final boolean add(Listener<? super T> listener) {
            return false;
        }

        public final boolean remove(Object listener) {
            return false;
        }

        public int size() {
            return 0;
        }
    }

    private static class REGISTRY extends HashMap<Class<?>, Distributor<?>> {
        private REGISTRY() {
        }

        private REGISTRY(Map<? extends Class<?>, ? extends Distributor<?>> m) {
            super(m);
        }
    }
}
