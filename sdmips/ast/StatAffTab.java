package ast;

public class StatAffTab extends Statement {

    private final ExpTabElt lhs;
    private final Expression expression;

    public StatAffTab(Position pos, ExpTabElt lhs, Expression expression) {
        this.position = pos;
        this.lhs = lhs;
        this.expression = expression;
    }

    public ExpTabElt getLhs() {
        return lhs;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
