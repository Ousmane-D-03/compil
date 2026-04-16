package ir.expr;

import ir.Type;

public class ReadMem extends Expression {
    private final Expression base;
    private final Expression index;
    private final Type type;

    public ReadMem(Expression base, Expression index, Type type) {
        this.base = base;
        this.index = index;
        this.type = type;
    }

    public Expression getBase() {
        return base;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
