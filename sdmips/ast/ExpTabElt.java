package ast;

public class ExpTabElt extends Expression {

    private final Expression tab;
    private final Expression indice;

    public ExpTabElt(Position pos, Expression tab, Expression indice) {
        this.position = pos;
        this.tab = tab;
        this.indice = indice;
    }

    public Expression getTab() {
        return tab;
    }

    public Expression getIndice() {
        return indice;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return tab.toString() + "[" + indice.toString() + "]";
    }
}
