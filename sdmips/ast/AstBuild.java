package ast;

import java.util.List;
import java.util.ArrayList;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.*;
import parser.*;
import parser.sdmParser.*;
import ast.*;



public class AstBuild extends parser.sdmBaseVisitor<Node> implements parser.sdmVisitor<Node> {

	private static Position position(ParserRuleContext ctx){
		return new Position(ctx.start.getLine(),ctx.start.getCharPositionInLine());
	}



	@Override public Node visitProgram(sdmParser.ProgramContext ctx) {
		List<MethodDeclContext> listMetCont = ctx.methodDecl();
		List<MethodDecl> metList=new ArrayList<MethodDecl>();
		for(MethodDeclContext md : listMetCont){
			metList.add((MethodDecl)visit(md));
		}
		metList.add((MethodDecl)visit(ctx.mainMethod()));
		return new Program(position(ctx),metList);
	}
	@Override public Node visitStatAff(sdmParser.StatAffContext ctx){
		Id id=new Id(position(ctx),ctx.Id().getText());
		Expression e = (Expression)visit(ctx.exp());
		return new StatAff(position(ctx),id,e);
	}

	@Override public Node visitStatAffTab(sdmParser.StatAffTabContext ctx){
		Id id = new Id(position(ctx), ctx.Id().getText());
		Expression index = (Expression)visit(ctx.exp(0));
		Expression value = (Expression)visit(ctx.exp(1));
		return new StatAffTab(position(ctx), new ExpTabElt(position(ctx), id, index), value);
	}

	@Override public Node visitStatVarDecl(sdmParser.StatVarDeclContext ctx){
		Type t=(Type) visit(ctx.type());
		Id id=new Id(position(ctx),ctx.Id().getText());
		return new StatVarDecl(position(ctx),id,t);
	}

	@Override public Node visitStatReturn(sdmParser.StatReturnContext ctx){
		Expression e = (Expression)visit(ctx.exp());
		return new StatReturn(position(ctx),e);
	}
	@Override public Node visitStatIncr(sdmParser.StatIncrContext ctx){
		Position pos=position(ctx);
		Id id = new Id(pos,ctx.Id().getText());
		ExpId ei=new ExpId(pos,ctx.Id().getText());
		ExpBin e = new ExpBin(pos,ei,BinOp.ADD,new ExpInt(pos,1));
		return new StatAff(pos,id,e);
	}
	
	@Override public Node visitStatVarDeclAff(sdmParser.StatVarDeclAffContext ctx){
		List<Statement> sl = new ArrayList<>();
		Expression e = (Expression)visit(ctx.exp());
		Id id=new Id(position(ctx),ctx.Id().getText());
		Type type=(Type)visit(ctx.type());
		sl.add(new StatVarDecl(position(ctx),id,type));
		sl.add(new StatAff(position(ctx),id,e));
		return new StatList(position(ctx),sl);
	}
	@Override public Node visitMainMethod(sdmParser.MainMethodContext ctx){
		Position pos=position(ctx);
		Statement i = new StatReturn(pos, new ExpInt(pos,0));
		List<Statement> sl = new ArrayList<>();
		sl.add((Statement)ctx.statement().accept(this)); 
		sl.add(i);
		Statement sls = new StatList(pos,sl);
		Block b = new Block(pos,sls);

		return new MethodDecl(pos,
				new TypePrim(pos,TypePrim.Prim.INT),
				new Id(pos,"main"),
				new ArrayList<>(),
				b);
	}
	@Override public Node visitMethodDecl(sdmParser.MethodDeclContext ctx) { 
		Type type =(Type) visit(ctx.type());
		Id id = new Id(position(ctx),ctx.Id().getText());
		StatementContext statContext =(ctx.statement());
		Statement statement=(Statement)visit(statContext);
		Block block=new Block(position(ctx),statement);
		
		List<FormalContext> formalContextList = ctx.formal();
		List<Formal> formalList = new ArrayList<Formal>();
		for(FormalContext fc : formalContextList){
			Formal f = (Formal)visit(fc);
			formalList.add(f);
		}

		return new MethodDecl(position(ctx),type,id,formalList,block);


	}

	@Override public Node visitFormal(sdmParser.FormalContext ctx) { 
		Type type = (Type)visit(ctx.type());
		Id id = new Id(position(ctx),ctx.Id().getText());

		return new Formal(position(ctx), type, id);
	}

	@Override public Node visitBoolType(sdmParser.BoolTypeContext ctx){
		return new TypePrim(position(ctx),TypePrim.Prim.BOOL);
	}

	@Override public Node visitIntType(sdmParser.IntTypeContext ctx){
		return new TypePrim(position(ctx),TypePrim.Prim.INT);
	}

	@Override public Node visitBoolArrayType(sdmParser.BoolArrayTypeContext ctx){
		return new TypeTab(position(ctx), new TypePrim(position(ctx), TypePrim.Prim.BOOL));
	}

	@Override public Node visitIntArrayType(sdmParser.IntArrayTypeContext ctx){
		return new TypeTab(position(ctx), new TypePrim(position(ctx), TypePrim.Prim.INT));
	}
	
	@Override public Node visitStatList(sdmParser.StatListContext ctx) { 
		List<StatementContext> listStat = ctx.statement();
		List<Statement> statList= new ArrayList<Statement>();
		for(StatementContext stat : listStat){
			statList.add((Statement)visit(stat));
		}
		return new StatList(position(ctx), statList);
				
	}

	@Override public Node visitStatIf(sdmParser.StatIfContext ctx) { 
		Expression exp = (Expression)visit(ctx.exp());
		Statement test = (Statement)visit(ctx.statement(0));
		Block bTest=new Block(position(ctx),test);
		Statement statElse = (Statement)visit(ctx.statement(1));
		Block bElse=new Block(position(ctx),statElse);
		
		return new StatIf(position(ctx),exp,bTest,bElse);	
	}

		
	@Override public Node visitStatWhile(sdmParser.StatWhileContext ctx) { 
		Expression exp =(Expression) visit(ctx.exp());
		Statement stat = (Statement)visit (ctx.statement());
		Block b=new Block(position(ctx),stat);
		return new StatWhile(position(ctx),exp,b);
	}
	@Override public Node visitStatFor(sdmParser.StatForContext ctx){
		Position pos=position(ctx);
		List<Statement> sList=new ArrayList<>();

		Expression cond=(Expression)visit(ctx.exp());
		Statement init = (Statement)visit(ctx.statement(0));
		Statement action = (Statement)visit(ctx.statement(1));
		Statement loop = (Statement)visit(ctx.statement(2));
		List<Statement> whileBody=new ArrayList<>(); 
		whileBody.add(loop);whileBody.add(action);
		
		Block block=new Block(pos,new StatList(pos,whileBody));
		StatWhile sw = new StatWhile(pos,cond,block);
		sList.add(init);
		sList.add(sw);
		return new StatList(position(ctx),sList);
	}


	@Override public Node visitStatPrint(sdmParser.StatPrintContext ctx) { 
		Expression exp = (Expression)visit(ctx.exp());
		return new StatPrint(position(ctx),exp);
	}

	/**
	 * EXPRESSIONS
	 * */
	@Override public Node visitExId(sdmParser.ExIdContext ctx) { 
		return new ExpId(position(ctx),ctx.Id().getText());
	}
	@Override public Node visitExFalse(sdmParser.ExFalseContext ctx) { 
		return new ExpCons(position(ctx),Constant.FALSE);
	}
	@Override public Node visitExTrue(sdmParser.ExTrueContext ctx) { 
		return new ExpCons(position(ctx),Constant.TRUE);
	}

	@Override public Node visitExParenthesis(sdmParser.ExParenthesisContext ctx) { 
		return visit(ctx.exp());
       	}
	@Override public Node visitExCall(sdmParser.ExCallContext ctx) {
		Id method = new Id(position(ctx),ctx.Id().getText());
		List<Expression> args = new ArrayList<>();
		for(ExpContext ec : ctx.exp()){
			args.add((Expression)visit(ec));
		}

		return new ExpCallMethod(position(ctx),method,args); 
	}
	@Override public Node visitExTabElt(sdmParser.ExTabEltContext ctx) {
		Expression tab = (Expression)visit(ctx.exp(0));
		Expression index = (Expression)visit(ctx.exp(1));
		return new ExpTabElt(position(ctx), tab, index);
	}
	@Override public Node visitExNewTab(sdmParser.ExNewTabContext ctx) {
		Type type = (Type)visit(ctx.type());
		Expression taille = (Expression)visit(ctx.exp());
		return new ExpNewTab(position(ctx), type, taille);
	}
	@Override public Node visitExInt(sdmParser.ExIntContext ctx) {
		return new ExpInt(position(ctx), Integer.parseInt(ctx.Int().getText())); 
	}

	@Override public Node visitExRead(sdmParser.ExReadContext ctx){
		return new ExpRead(position(ctx));
	}


	@Override public Node visitExBinop(sdmParser.ExBinopContext ctx) {
		Expression exp1=(Expression)visit(ctx.exp(0));
		Expression exp2=(Expression)visit(ctx.exp(1));
		String binop = ctx.op.getText();
		BinOp op = switch(binop){
			case "+" -> BinOp.ADD;
			case "-" -> BinOp.MIN;
			case "*" -> BinOp.MULT;
			case "<" -> BinOp.LT;
			case "<=" -> BinOp.LEQ;
			case ">=" -> BinOp.GEQ;
			case ">" -> BinOp.GT;
			case "&&" -> BinOp.AND;
			case "==" -> BinOp.EQ;
			case "!=" -> BinOp.NEQ;
			case "||" -> BinOp.OR;
			case "/" -> BinOp.DIV;
			default -> throw new IllegalStateException("Unexpected value");
		};
		return new ExpBin(position(ctx), exp1,op,exp2);
	}
	@Override public Node visitExUnop(sdmParser.ExUnopContext ctx) { 
		Expression exp=(Expression)visit(ctx.exp());
		String unop=ctx.op.getText();
		UnOp op = switch(unop){
			case "!" -> UnOp.NOT;
			case "-" -> UnOp.MIN;
			default -> throw new IllegalStateException("Unexpected value");
		};
		return new ExpUn(position(ctx), exp, op);
	}




}
