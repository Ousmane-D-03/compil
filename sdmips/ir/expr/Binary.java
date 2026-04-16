package ir.expr;
import ast.BinOp;
import ir.Type;

public class Binary extends Expression
{
    final private Expression left;
    final private Expression right;
    final private ast.BinOp op;

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public ast.BinOp getOp() {
        return op;
    }

    public Binary(Expression left, Expression right, ast.BinOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public Type getType() {
        return switch (op) {
            case ADD, MIN-> Type.INT;
	    case AND, OR, EQ, LT  -> Type.BYTE;
            default -> Type.BYTE;
        };
    }

    @Override
    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
