package printers;
import java.util.List;
import ast.*;

public class AstPrinter extends ast.BaseVisitor<Void>{
	private int indent;
	public void indent(){
		System.out.println();
		for(int i=0; i<indent;i++){
			print("   ");
		}
	}
	private void print(String s){
		System.out.print(s);
	}
		
	public void end(){
		print(";");
	}

	public AstPrinter(){
		super(null);
		indent=0;
	}


	public Void visit(StatVarDecl v){
		indent();
		print(v.getType().toString());
		print(" ");
		print(v.getId().toString());
		end();
		return null;
	}
	public Void visit(StatPrint sp){
		indent();
		print("print("
		+ sp.getExpression().toString()
		+")");end();
		return null;
	}
	public Void visit(MethodDecl m){
		indent();
		print(m.getType().toString()
		+ " "
		+m.getId().toString()
		+"(");
		Formal f;
		for(int i=0; i<m.getFormal().size();i++){
			f=m.getFormal().get(i);
			f.accept(this);
			if(i<m.getFormal().size()-1){
				print(",");
			}
		}
		print(")");
		m.getBlock().accept(this);
		return null;
	}

	public Void visit(StatReturn s){
		indent();
		print("return "+s.getExpression().toString());
		end();
		return null;
	}

	
	public Void visit(Formal f){
		print(f.getType().toString()
		+" "
		+f.getId().toString());
		return null;
	}
	
	public Void visit(StatList sl){
		List<Statement> l=sl.getStatList();
		for(Statement s : l){
			s.accept(this);
		}

		return null;
	}

	public Void visit(StatIf a){
		Expression e = a.getExpression();
		Block ib = a.getIfBlock();
		Block eb= a.getElseBlock();
		indent();
		print("if("+e.toString()+")");
		ib.accept(this);
		indent();
		print("else");
		eb.accept(this);

		return null;
	}
	public Void visit(StatWhile s){
		Expression e= s.getExpression();
		Block b = s.getBlock();
		indent();
		print("while("+e.toString()+")");
		b.accept(this);
		return null;
	}
	public Void visit(Block b){
		System.out.print("{");indent++;
		b.getStatement().accept(this);
		indent--;indent();System.out.print("}");
		return null;
	}


	public Void visit(TypePrim t){
		print(t.toString());
		return null;
	}
	public Void visit(TypeTab t){
		t.getElementType().accept(this);
		print("[]");
		return null;
	}
	public Void visit(Id i){
		print(i.toString());
		return null;
	}

	public Void visit(ExpBin e){
		Expression eg=e.getLeftExp();
		Expression ed=e.getRightExp();
		BinOp op=e.getOp();
		print("(");eg.accept(this);
		print(" "+op.toString()+" ");
		ed.accept(this);
		print(")");
		return null;
	}
	public Void visit(ExpCallMethod e){
		e.getMethod().accept(this);
		print("(");
		List<Expression> l=e.getArgs();
		for(int i=0;i<l.size();i++){
			l.get(i).accept(this);
			if(i<l.size()-1){
				print(",");
			}
		}

		print(")");
		return null;
	}
	public Void visit(ExpInt e){
		print(String.valueOf(e.getValue()));
		return null;
	}
	public Void visit(ExpRead e){
		print("read()");
		return null;
	}

	public Void visit(ExpId i){
		print(i.getValue());
		return null;
	}
	public Void visit(ExpCons e){
		print(e.getConstant().toString());
		return null;
	}
	public Void visit(ExpTabElt e){
		e.getTab().accept(this);
		print("[");
		e.getIndice().accept(this);
		print("]");
		return null;
	}
	public Void visit(ExpNewTab e){
		print("new ");
		e.getType().accept(this);
		print("[");
		e.getTaille().accept(this);
		print("]");
		return null;
	}
	
	public Void visit(ExpUn e){
		print(e.getOp().toString());
		e.getExp().accept(this);
		return null;
	}
}
