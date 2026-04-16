package ir.expr;

import ir.Type;

public class NewArray extends Expression {
    private final Expression size;

    public NewArray(Expression size) {
        this.size = size;
    }

    public Expression getSize() {
        return size;
    }

    @Override
    public Type getType() {
        return Type.INT;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
