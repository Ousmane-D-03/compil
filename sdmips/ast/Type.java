package ast;
public abstract class Type extends Node{
	public boolean equals(Type t){
		return t.toString().equals(this.toString());
	}

	public abstract Type copy();
	public abstract String toString();


}
