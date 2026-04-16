package ast;
 
import java.util.List;

public class Formal extends Node{

	private Type type;
	private Id id;
	public Formal(Position pos, Type t, Id i){
		this.type=t;
		this.id=i;
		this.position=position;
	}
	
	public Id getId(){return id;}
	public Type getType(){return type;}

	public <T> T accept(Visitor<T> visitor){
       		return visitor.visit(this);
	}
}
