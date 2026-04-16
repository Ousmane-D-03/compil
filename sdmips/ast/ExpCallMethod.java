package ast;
import java.util.List;
 public class ExpCallMethod extends Expression{

	private Id method;
	private List<Expression> args;

	public ExpCallMethod(Position position,Id method,List<Expression> args){

		this.position=position;
		this.method=method;
		this.args=args;
	}

	public Id getMethod(){return method;}
	public List<Expression> getArgs(){return args;}

	public <T> T accept(Visitor<T> visitor){
       		return visitor.visit(this);
	}
	public String toString(){
		String res=method.toString()+"(";
		for(Expression a : args){
			res = res+a.toString()+",";
		}
		return res+")";
	}

}

	
