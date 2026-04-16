package semantic;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

import ast.*;
import support.Errors;

public class TableBuilder extends ast.BaseVisitor<Void>{
	private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
		"int", "boolean", "if", "else", "while", "for", "print",
		"return", "read", "main", "true", "false"
	));

	private void checkReservedWord(String name, Node n){
		if (RESERVED_WORDS.contains(name)){
			errors.add(n, "identificateur réservé : " + name);
		}
	}
	
	
	private final SymbolTable symbolTable;
	private final VisitedBlocks visitedBlocks;
	private final Errors errors;
	public TableBuilder(){
		super(null);
		errors=new Errors();
		visitedBlocks=new VisitedBlocks();
		symbolTable=new SymbolTable();
	}

	public SymbolTable getTable(){
		if(symbolTable.getErrors().hasErrors()){
			System.out.println("erreurs dans table");
			symbolTable.getErrors().print();
			System.out.println("Sortie après construction de la table des symboles.");
			System.exit(1);
		}
		if(errors.hasErrors()){
			System.out.println("erreurs dans construction table");
			errors.print();
			System.out.println("Sortie après construction de la table des symboles.");
			System.exit(1);
		}
		return symbolTable;
	}

	@Override
	public Void visit(Block b){
		//enregistrer le bloc dans la table :
		symbolTable.localTable(b);

		visitedBlocks.enter(b);
		super.visit(b);
		visitedBlocks.exit();
		return null;
	}
	@Override
	public Void visit(StatVarDecl vd){
		String id=vd.getId().getName();
		checkReservedWord(id, vd);
		Type type=vd.getType();
		Type t = symbolTable.variableLookup(id,visitedBlocks);
		
		if(t!=null){
			errors.add(vd,
				" : variable "
				+id
				+" déjà déclarée en "
				+ t.getPosition());
		}
		//Si on est dans un bloc :
		if(visitedBlocks.getStack().isEmpty()){
			errors.add(vd,"erreur : pile des blocs vide");
		}
		symbolTable.addLocalVariable
			(visitedBlocks.current(),id,type);

		return null;
	}

	@Override
	public Void visit(MethodDecl md){
		String methodName = md.getId().getName();
		if (!"main".equals(methodName)) {
			checkReservedWord(methodName, md);
		}
		Block b=md.getBlock();
		List<Formal> lf=md.getFormal();
		String name = md.getType().toString();
		
		MethodSig ms=MethodSig.signatureOf(md);
		symbolTable.addMethod(methodName,ms);

		symbolTable.localTable(b);
		
		visitedBlocks.enter(b);

		for(Formal f : lf){
			checkReservedWord(f.getId().getName(), f);
			symbolTable.addLocalVariable(
					visitedBlocks.current(),
					f.getId().getName(),
					f.getType()
					);
		}
		super.visit(b);
		visitedBlocks.exit();
		return null;
	}
}
