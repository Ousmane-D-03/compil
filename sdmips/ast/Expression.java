package ast;
 public abstract class Expression extends Node{
	 abstract public String toString();
	abstract public <T> T accept(Visitor <T> visitor);
}

