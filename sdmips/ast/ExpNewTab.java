package ast;

public class ExpNewTab extends Expression {

    private final Type type;
    private final Expression taille;

    public ExpNewTab(Position pos, Type type, Expression taille) {
        this.position = pos;
        this.type = type;
        this.taille = taille;
    }

    public Type getType() {
        return type;
    }

    public Expression getTaille() {
        return taille;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "new " + type.toString() + "[" + taille.toString() + "]";
    }
}
