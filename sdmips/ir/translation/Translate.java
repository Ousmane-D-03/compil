package ir.translation;

import ast.*;
import ast.Expression;
import ir.Frame;
import ir.Register;
import ir.com.*;
import ir.expr.*;
import semantic.*;
import support.*;

import java.util.*;
import java.util.stream.Collectors;

public class Translate {
	private static final TypeConverter typeConverter = new TypeConverter();

	private static ir.Type ofType(Type type) {
		return type.accept(typeConverter);
	}

	public static Pair<Label, List<Pair<Frame, List<Command>>>> 
	  run(SymbolTable symbolTable, Program program) {
		TranslationVisitor translator = new TranslationVisitor(symbolTable);
		program.accept(translator);
		Errors e = translator.errors;
		if(translator.mainLabel==null){
			e.add("pas de fonction main.");
		}
		if(e.hasErrors()){
			System.out.println("Erreurs de traduction en code intermédiaire :");
			e.print();
			System.out.println("Sortie avant production assembleur");
			System.exit(1);
		  }
		  return new Pair<>(translator.mainLabel, translator.fragments);
	}
	private static class TypeConverter extends ast.BaseVisitor<ir.Type> {
	//pour convertir les types de l’AST en types de l’IR
	public TypeConverter(){
		super(null);
	}
	@Override
	public ir.Type visit(TypePrim type) {
		switch (type.getPrim()) {
		case INT:
			return ir.Type.INT;
		case BOOL:
			return ir.Type.BYTE;
		}
		throw new Error("Traduction IR :type primitif non géré : "+type.getPrim().toString());
	}
	@Override
	public ir.Type visit(TypeTab type) {
		return ir.Type.INT;
	}
}

private static class TranslationVisitor extends ast.BaseVisitor<Result>{
	public Errors errors;
	//La table des symboles et le typeChecker, utiles pour au moins deux
	//éléments :
	//- Savoir à quel bloc se réfère un nom de variable (pile des blocs)
	//- Savoir quel est le type pour les registres associés aux variables.
	private final SymbolTable symbolTable;
	private final TypeChecker typeChecker;
	
	//Chaque nom de méthode est associé à un frame (construit par le
	//FramesBuilder)
	private final Map<String, Frame> frames;

	//Dans chaque bloc, chaque variable est associée à un registre.
	private final Map<Block,Map<String, Register>> varToReg;

	//Chaque méthode sera enregistrée dans une liste de paires, avec son
	//frame et son code.
	private final List<Pair<Frame, List<Command>>> fragments;

	//Le frame courant, auquel on ajoute en particulier les registres
	//temporaires nécessaires au calcul de la fonction (qui seront empilés
	//lors de l’appel).
	private Frame currentFrame;
	//Un label spécifique, correspondant au point d’entrée de la fonction
	//main.
	private Label mainLabel;

	public TranslationVisitor(SymbolTable symbolTable) {
		super(null);
		this.symbolTable = symbolTable;
		this.varToReg = new HashMap<>();
		this.typeChecker = new TypeChecker(symbolTable);
		this.fragments = new LinkedList<>();
		this.frames = new HashMap<>();
		this.currentFrame = null;
		this.mainLabel = null;
		this.errors = new Errors();
	}

//----REGISTRES-------
	private Register registerLookup(String name) {
		Register reg;
		for (Block block : typeChecker.getVisitedBlocks().getStack()) {
			Map<String,Register> map = varToReg.get(block);
	
			reg=map.get(name);
			//System.out.println("lookup "+block+": "+name);
			if (reg != null) return reg;
		}
		support.Maps.print(varToReg);
		errors.add("Erreur interne, pas de registre associé à la variable " + name);
		return null;
	}

//---EXPRESSIONS------
	@Override
	public Result visit(ExpCons exp) {
		int value = exp.getConstant() == Constant.TRUE ? 1 : 0;
		return new Result(new ir.expr.Byte((byte) value), new LinkedList<>());
	}
	@Override
	public Result visit(ExpInt exp) {
		return new Result(new Int(exp.getValue()), new LinkedList<>());
	}
	@Override
	public Result visit(ExpId exp){
		Register reg = registerLookup(exp.getValue());
		return new Result(new ReadReg(reg), new LinkedList<>());
	}
	public Result visit(ExpRead e){
	//création d’un frame pour la lecture, avec un nom qui sera rajouté
	//explicitement dans le code assembleur.
		Frame frame = new Frame(Label.named("entryReadInt"),
			Label.named("exitReadInt"),
			new LinkedList<>(),
			new Register(ir.Type.INT)
			);
		Type type = new TypePrim(null,TypePrim.Prim.INT);
		List<Command> code = new LinkedList<>();
		List<ir.expr.Expression> args = new LinkedList<>();
		Result result = makeFunCall(type,frame,args,code);
		return result;
	}
	@Override
	public Result visit(ExpBin exp) {
		Result left = exp.getLeftExp().accept(this);
		Result right = exp.getRightExp().accept(this);
		List<Command> code = new LinkedList<>();
		code.addAll(left.getCode());
		code.addAll(right.getCode());
		return new Result(new Binary(left.getExp(), right.getExp(), exp.getOp()), code);
	}
	
	@Override
	public Result visit(ExpUn exp) {
		Result result = exp.getExp().accept(this);
		List<Command> code = new LinkedList<>(result.getCode());
		return new Result(new Unary(result.getExp(), exp.getOp()), code);
	}

	@Override
	public Result visit(ExpTabElt exp) {
		Result tab = exp.getTab().accept(this);
		Result index = exp.getIndice().accept(this);
		Type tabType = exp.getTab().accept(typeChecker);
		ir.Type elementType = ir.Type.INT;
		if (tabType instanceof TypeTab) {
			elementType = ofType(((TypeTab) tabType).getElementType());
		}
		List<Command> code = new LinkedList<>();
		code.addAll(tab.getCode());
		code.addAll(index.getCode());
		return new Result(new ReadMem(tab.getExp(), index.getExp(), elementType), code);
	}

	@Override
	public Result visit(ExpNewTab exp) {
		Result size = exp.getTaille().accept(this);
		List<Command> code = new LinkedList<>(size.getCode());
		return new Result(new NewArray(size.getExp()), code);
	}

	@Override
	public Result visit(ExpCallMethod exp){
		String functionName = exp.getMethod().getName();
	
		Pair<List<ir.expr.Expression>, List<Command>> translation = translateExpressions(exp.getArgs());
		List<Command> code = translation.getSnd();
		List<ir.expr.Expression> arguments = translation.getFst();
		MethodSig signature = symbolTable.methodLookup(functionName);
		if(signature==null) errors.add("Erreur interne, " + functionName+" pas dans la table");
	
		List<Type> argumentTypes = getArgumentTypes(exp.getArgs());
		Type returnType = signature.getReturnType();
	
		Frame frame = frames.get(functionName);
		Result result= makeFunCall(returnType,frame,arguments,code);
		return result;
	}

	
	
	
//-------INSTRUCTIONS--------

	@Override
	public Result visit(StatVarDecl stm) {
		Register reg = new Register(ofType(stm.getType()));
		if (currentFrame == null) {
			errors.add("Erreur interne : déclaration de variable hors d'un frame");
			return new Result(new LinkedList<>());
		}
		currentFrame.addLocal(reg);
		Block currentBlock = typeChecker.getVisitedBlocks().current();
		Map<String, Register> map = varToReg.get(currentBlock);
		if (map == null) {
			map = new HashMap<>();
			varToReg.put(currentBlock, map);
		}
		map.put(stm.getId().getName(), reg);
		return new Result(new LinkedList<>());
	}

	@Override
	public Result visit(StatAff s) {
		Result result = s.getExpression().accept(this);
		List<Command> code = new LinkedList<>(result.getCode());
		Register reg = registerLookup(s.getId().getName());
		code.add(new WriteReg(reg, result.getExp()));
		return new Result(code);
	}

	@Override
	public Result visit(StatAffTab s) {
		Result tab = s.getLhs().getTab().accept(this);
		Result index = s.getLhs().getIndice().accept(this);
		Result value = s.getExpression().accept(this);
		List<Command> code = new LinkedList<>();
		code.addAll(tab.getCode());
		code.addAll(index.getCode());
		code.addAll(value.getCode());
		code.add(new WriteMem(tab.getExp(), index.getExp(), value.getExp()));
		return new Result(code);
	}
	@Override 
	public Result visit(StatPrint s){
		Expression e=s.getExpression();
		Frame frame = new Frame(Label.named("entryPrintInt"),
				Label.named("exitPrintInt"),
				ListTools.mklist(new Register(ir.Type.INT)),
				new Register(ir.Type.INT)
				);
		Result r = e.accept(this);
	
		List<Command> code = r.getCode();
		List<ir.expr.Expression> args = ListTools.mklist(r.getExp());
		//on laisse un int en retour par défaut, car toutes les fonctions
		//doivent retourne qqchose. 
		Type type = new TypePrim(null,TypePrim.Prim.INT);
		Result result = makeFunCall(type,frame,args,code);
		
	    	return result;
	}

  
	@Override
	public Result visit(StatReturn stm) {
		Result result = stm.getExpression().accept(this);
		List<Command> code = new LinkedList<>(result.getCode());
		code.add(new WriteReg(currentFrame.getResult(), result.getExp()));
		code.add(new Jump(currentFrame.getExitPoint()));
		return new Result(code);
	}
	
	//Pour compiler une liste d’expressions dans une seule paire (exp,code)
	private	 Pair<List<ir.expr.Expression>,List<Command>> translateExpressions(List<Expression> exps) {
		List<ir.expr.Expression> expressions = new LinkedList<>();
		List<Command> code = new LinkedList<>();
		for (Expression exp : exps) {
			Result result = exp.accept(this);
			code.addAll(result.getCode());
			expressions.add(result.getExp());
		}
		return new Pair<>(expressions, code);
	}

	private Result makeFunCall(Type type,Frame frame,List<ir.expr.Expression> args,List<Command> code){
		Register reg = new Register(ofType(type));
		currentFrame.addLocal(reg);
		Command call = new FunCall(reg, frame, args);
		code.add(call);
		return new Result(new ReadReg(reg), code);
	}
	//Pour récupérer la liste des types de la liste des paramètres
	List<Type> getArgumentTypes(List<Expression> args) {
		return args.stream()
			.map((a) -> a.accept(typeChecker))
			.collect(Collectors.toList());
	}
	
	@Override
	public Result visit(StatIf stm) {
		Result cond = stm.getExpression().accept(this);
		Label thenLabel = Label.fresh();
		Label elseLabel = Label.fresh();
		Label endLabel = Label.fresh();
		List<Command> code = new LinkedList<>();
		code.addAll(cond.getCode());
		code.add(new CJump(cond.getExp(), thenLabel, elseLabel));
		code.add(thenLabel);
		code.addAll(stm.getIfBlock().accept(this).getCode());
		code.add(new Jump(endLabel));
		code.add(elseLabel);
		code.addAll(stm.getElseBlock().accept(this).getCode());
		code.add(endLabel);
		return new Result(code);
	}
	
	@Override
	public Result visit(StatWhile stm) {
		Label startLabel = Label.fresh();
		Label bodyLabel = Label.fresh();
		Label endLabel = Label.fresh();
		List<Command> code = new LinkedList<>();
		code.add(startLabel);
		Result cond = stm.getExpression().accept(this);
		code.addAll(cond.getCode());
		code.add(new CJump(cond.getExp(), bodyLabel, endLabel));
		code.add(bodyLabel);
		code.addAll(stm.getBlock().accept(this).getCode());
		code.add(new Jump(startLabel));
		code.add(endLabel);
		return new Result(code);
	}
	
	@Override
	public Result visit(Block b) {
		if(varToReg.get(b)==null){
			varToReg.put(b,new HashMap<>());
		}
		typeChecker.getVisitedBlocks().enter(b);
	
		List<Command> code = new LinkedList<>();
		Statement stm = b.getStatement();
		Result result = stm.accept(this);
		code.addAll(result.getCode());
	
	
		typeChecker.getVisitedBlocks().exit();
	      
		return new Result(code);
	}
	@Override
	public Result visit(StatList sl) {
		List<Command> code = new LinkedList<>();
		for (Statement stm : sl.getStatList()){
			Result result = stm.accept(this);
			code.addAll(result.getCode());
		}
	return new Result(code);
	}

	@Override
	public Result visit(MethodDecl md) {
		Frame frame = frames.get(md.getId().getName());
		if (frame == null) {
			errors.add("Erreur interne : frame manquant pour la méthode " + md.getId().getName());
			return new Result(new LinkedList<>());
		}
		currentFrame = frame;
		Result bodyResult = md.getBlock().accept(this);
		List<Command> code = new LinkedList<>(bodyResult.getCode());
		code.add(frame.getExitPoint());
		fragments.add(new Pair<>(frame, code));
		currentFrame = null;
		return new Result(code);
	}

	@Override
	public Result visit(Program program) {
		program.accept(typeChecker);
		FramesBuilder framesBuilder = new FramesBuilder();
		program.accept(framesBuilder);
		for (MethodDecl fun : program.getListMethodDecl())
			fun.accept(this);
		return null;
	}

//Classe interne : visiteur pour la constructions des Frames
private class FramesBuilder extends ast.BaseVisitor<Void> {
	FramesBuilder(){super(null);}

	@Override
	public Void visit(MethodDecl function) {
		List<Register> parameters = new LinkedList<>();
		Block b = function.getBlock();
		//on ne visite pas le bloc, alors le rajouter ici dans la Map
		varToReg.put(b,new HashMap<>());
		for (Formal argument : function.getFormal()) {
			Type argType = argument.getType();
			String argName = argument.getId().getName();
			Register register = new Register(ofType(argType));
			Map<String,Register> map = varToReg.get(b);
			//System.out.println("put "+argName+"->"+register+" dans "+b);
			map.put(argName, register);
			parameters.add(register);
		}
		Frame frame;
		Type type = function.getType();
		ir.Type irType = ofType(type);
		frame = new Frame(Label.fresh(), Label.fresh(), parameters, new Register(irType));
	
		frames.put(function.getId().getName(), frame);
		if (function.getId().getName().equals("main")){
			mainLabel = frame.getEntryPoint();
		}
		//support.Maps.print(varToReg); 
		return null;
	}
	@Override
	public Void visit(Program program) {
		for (MethodDecl function : program.getListMethodDecl()) {
			function.accept(this);
		}
		return null;
	}
}
}
}
