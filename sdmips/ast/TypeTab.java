package ast;

public class TypeTab extends Type {

    private final Type elementType;

    public TypeTab(Position pos, Type elementType) {
        this.position = pos;
        this.elementType = elementType;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Type copy() {
        return new TypeTab(position, elementType.copy());
    }

    @Override
    public String toString() {
        return elementType.toString() + "[]";
    }
}
