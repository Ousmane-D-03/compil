package ast;
 public abstract class Statement extends Node{
	abstract public <T> T accept(Visitor <T> visitor);
}

