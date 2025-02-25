package de.team33.cmd.template.runtime;

import java.util.List;

class ConditionImpl implements Condition {

    private final Output out;
    private final List<String> args;

    private ConditionImpl(final Output out, final List<String> args) throws BadConditionException {
        if (args.isEmpty()) {
            throw BadConditionException.read(Condition.class);
        } else {
            this.out = out;
            this.args = args;
        }
    }

    ConditionImpl(final Output out, final String[] args) throws BadConditionException {
        this(out, List.of(args));
    }

    @Override
    public final Output out() {
        return out;
    }

    @Override
    public final List<String> args() {
        return args;
    }
}
