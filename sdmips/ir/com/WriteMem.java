package ir.com;

import ir.expr.Expression;

public class WriteMem extends Command {
    private final Expression base;
    private final Expression index;
    private final Expression value;

    public WriteMem(Expression base, Expression index, Expression value) {
        this.base = base;
        this.index = index;
        this.value = value;
    }

    public Expression getBase() {
        return base;
    }

    public Expression getIndex() {
        return index;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
