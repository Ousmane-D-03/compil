package ast;
public class ExpRead extends Expression{

	private int value;

	public ExpRead(Position position){
		this.position=position;
	}

	public int getValue(){
		return value;
	}

	
	public <T> T accept(Visitor<T> visitor){
       		return visitor.visit(this);
	}
	public String toString(){
		return "read()";
	}
}
