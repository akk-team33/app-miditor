package de.team33.cmd.template.runtime;

import java.util.List;

public class ConditionProxy implements Condition {

    private final Condition backing;

    protected ConditionProxy(final Condition backing) {
        this.backing = backing;
    }

    @Override
    public final Output out() {
        return backing.out();
    }

    @Override
    public final List<String> args() {
        return backing.args();
    }
}
