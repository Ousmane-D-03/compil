package semantic;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import ast.*;
import semantic.SymbolTable;
import support.Errors;


public class TypeChecker extends ast.BaseVisitor<Type>{
	private SymbolTable table;
	private String currentMethod;
	private VisitedBlocks visitedBlocks;

	public VisitedBlocks getVisitedBlocks(){
		return visitedBlocks;
	}

	private boolean aRetourner; //Est-ce qu’on est dans 
				    //un bloc qui attend une valeur de retour ?
				    //(i.e. une def de fonction ?)
				    //Peut être à faux si on autorise des blocs
				    //d’instructions hors des déclarations de
				    //méthodes, ou si on est après un "return".
	private Type typeRetour;//type retourné par un bloc 
				//(par toutes les branches des if !)

	public void checkReach(Node n){//si inst après un return
		if(aRetourner && typeRetour!=null){
			addWarning(n,"Instruction inatteignable");
		}
	}

	private Errors errors;
	private Errors warnings;
	
	private final static Type statType = new TypePrim(null, TypePrim.Prim.STAT);
	private final static Type errType = new TypePrim(null, TypePrim.Prim.IND);

	public TypeChecker(SymbolTable t){
		super(statType);
		visitedBlocks=new VisitedBlocks();
		this.table=t;
		this.errors=new Errors();
		this.warnings=new Errors();
		
	}

	private void addWarning(Node n, String msg){
		warnings.add(n, msg);
	}

	public Errors getWarnings(){
		return warnings;
	}

	//TODO ajouter warnings : les afficher mais continuer la compilation
	public void check(){
		if(warnings.hasErrors()){
			System.out.println("Warnings de typage : ");
			warnings.print();
		}
		if(errors.hasErrors()){
			System.out.println("Erreurs de typage : ");
			errors.print();
			System.out.println("Sortie après analyse de types.");
			System.exit(1);
		}
	}
	
	//Feuilles
	

	public Type visit (Id i){
		Type t = table.variableLookup(i.getName(),visitedBlocks);
		if(t==null){
			errors.add(i,
					"Impossible de trouver le type de la variable "
					+i.getName()
				  );
			return errType;
		}
		return t;


	}
	public Type visit(ExpId e){
		Type t = table.variableLookup(e.getValue(),visitedBlocks);
		if(t==null){
			errors.add(e,
					"Impossible de trouver le type de la variable "
					+e.getValue()
				  );
			return errType;
		}

		return t;
	}
	public Type visit(ExpInt e){
		return new TypePrim(e.getPosition(),TypePrim.Prim.INT);
	}
	public Type visit(ExpRead e){
		return new TypePrim(e.getPosition(),TypePrim.Prim.INT);
	}
	public Type visit(ExpTabElt e){
		Type tabType = e.getTab().accept(this);
		Type idxType = e.getIndice().accept(this);
		if (!(tabType instanceof TypeTab)) {
			errors.add(e, "Indexation sur une valeur non-tableau (trouvé : " + tabType.toString() + ")");
			return errType;
		}
		if (!idxType.toString().equals("int")) {
			errors.add(e, "Indice de tableau doit être de type int (trouvé : " + idxType.toString() + ")");
			return errType;
		}
		return ((TypeTab) tabType).getElementType();
	}
	public Type visit(ExpNewTab e){
		Type sizeType = e.getTaille().accept(this);
		if (!sizeType.toString().equals("int")) {
			errors.add(e, "Taille de tableau doit être de type int (trouvé : " + sizeType.toString() + ")");
			return errType;
		}
		return new TypeTab(e.getPosition(), e.getType());
	}
	public Type visit(ExpCons e){
		Type t=null;
		switch(e.getConstant()){
			case TRUE :
		       		t=new TypePrim(e.getPosition(),TypePrim.Prim.BOOL);
				break;
			case FALSE :
			       	t=new TypePrim(e.getPosition(),TypePrim.Prim.BOOL);
				break;
			default :
				break;
		}
		return t;
	}

	public Type visit (StatAff sa){
		checkReach(sa);
		Type t1=sa.getId().accept(this);
		Expression e = sa.getExpression();
		Type t2=e.accept(this);
		if(t1==null||t2==null){
			errors.add(sa,"Erreur de typage pour l’affectation");
		}
		if(!t1.equals(t2)){
			errors.add(sa,"Impossible d’affecter l’expression "+e.toString()+" de type "
				       +t2.toString()
				       +" à la variable "
				       +sa.getId().getName()
				       +" de type "
				       +t1.toString()
				       );
		}
		return statType;
	}

	public Type visit(StatAffTab sa){
		checkReach(sa);
		Type elementType = sa.getLhs().accept(this);
		Type valueType = sa.getExpression().accept(this);
		if(!elementType.equals(valueType)){
			errors.add(sa,"Impossible d’affecter l’expression "+sa.getExpression().toString()+" de type "
			       +valueType.toString()
			       +" à l’élément de tableau de type "
			       +elementType.toString()
			       );
		}
		return statType;
	}
	
	public Type visit(StatList sl){
		checkReach(sl);
		return super.visit(sl); 
	}
	public Type visit(MethodDecl md){
		//on initialise la recherche du type retourné.
		//Il sera modifié si le visiteur rencontre un "return"
		//dans toutes les branches du flot de contrôle.
		this.typeRetour=null;
		Type ret=md.getType();
		this.aRetourner=true;
		
		Block b=md.getBlock();

		Type t = b.accept(this);

		if( typeRetour==null){
			errors.add(md,"Pas de valeur de retour.");
		}

		else if (!typeRetour.equals(ret)){
			errors.add(md, "Type de retour attendu : "
					+ret.toString()
					+". Trouvé : "
					+typeRetour.toString()
					);
		}
		this.aRetourner=false;
		return t;
	}


	public Type visit(ExpBin e){
		Type tg=e.getLeftExp().accept(this);
		Type td=e.getRightExp().accept(this);
		if(tg==null || td ==null){
			return errType;
		}
		BinOp op=e.getOp();
	
		if(!tg.equals(td)){
			errors.add(e,
					"Les expressions doivent être de même type"
					+" pour l’opération "
					+op.toString()
					+" (trouvé : "
					+tg.toString()
					+" et "
					+td.toString()
					+")."
				  );
			return errType;
		}
		Type t = null;
		switch(op){
			case ADD,MIN,MULT,DIV :
				if(tg.toString().equals("int")){
					t=new TypePrim(e.getPosition(),
							TypePrim.Prim.INT);
				}
				else{
					errors.add(e,
							"expressions de type int"
							+" attendues pour l’opération "
							+op.toString()
							+" (trouvé : "
							+tg.toString()+")."
						  );
					t=errType;
				}
				break;
			case AND,OR :
				if(tg.toString().equals("bool")){
					t=new TypePrim(e.getPosition(),
							TypePrim.Prim.BOOL);
				}
				else{
					errors.add(e,
							"expressions de type bool"
							+" attendues pour l’opération "
							+op.toString()
							+" (trouvé : "
							+tg.toString()+")."

						  );
					t=errType;
				}
				break;
			case EQ,LT,NEQ,GT :
				t=new TypePrim(e.getPosition(),
						TypePrim.Prim.BOOL);
				break;
		}
		return t;
	}
	
	public Type visit(ExpUn exp){
		Type t=exp.getExp().accept(this);
		Position pos=exp.getPosition();
		if(t==null || t.equals(errType)){
			return errType;
		}
		String name=t.toString();
		UnOp op=exp.getOp();
		switch(op){
			case NOT :
				if(name.equals("bool")){
					t=new TypePrim(pos,TypePrim.Prim.BOOL);
				}
				else{
					errors.add(exp,"exp. de type bool attendue pour l’opération "
							+op.toString()
							+" (trouvé : "
							+t.toString()
							+")"
						  );
					t=errType;
				}
							
				break;
		}

		return t;
	}

	public Type visit(ExpCallMethod ec){
		Position pos=ec.getPosition();
		String id=ec.getMethod().getName();
		List<Expression> le = ec.getArgs();

		MethodSig sig=table.methodLookup(id);
		if(sig==null){
			errors.add(ec,
					"Méthode "+id+" non trouvée"
				  );
			return errType;
		}
		List<Formal> lf=sig.getParams();
		Type tReturn=sig.getReturnType();
		if(lf.size()!=le.size()){
			errors.add(ec,
					"La méthode "+id+" doit recevoir "
					+lf.size()
					+ " arguments. (trouvés : "
					+le.size()+")"
				  );
			return errType;
		}
		Type tParam=null;
		Type tExp=null;
		for(int i =0; i<lf.size(); i++){
			tParam=lf.get(i).getType();
			tExp=le.get(i).accept(this);
			if(!tParam.equals(tExp)){
				errors.add(ec,
						"Argument de "+id
						+" en position "+(i+1)
						+" : attendu exp. de type "
						+tParam.toString()
						+" (trouvé : "
						+tExp.toString()+")"
					  );
				return errType;
			}
		}
		return tReturn;
	}
	public Type visit(StatReturn sr){
		checkReach(sr);
		Type t = sr.getExpression().accept(this);
		this.typeRetour=t;
		return statType;
	}
	public Type visit(StatIf s){
		checkReach(s);
		Type te = s.getExpression().accept(this);
		Type ret1=null;//type de retour dans le "then"
		Type ret2=null;//type de retour dans le "else"
		Type t1=s.getIfBlock().accept(this);
		if(aRetourner){
			if(typeRetour==null){
				ret1=null;
			}
			else{
				ret1=typeRetour.copy();
				//on réinitialise le type retourné à null, car il doit
				//être trouvé dans chacune des deux branches.
				typeRetour=null;
			}
		}
		Type t2=s.getElseBlock().accept(this);
		if(aRetourner){
			if(typeRetour==null){
				ret2=null;
			}
			else{
				ret2=typeRetour.copy();
			}
		}
		if(ret1!=null && ret2 !=null && !ret1.equals(ret2)){
			errors.add(s,"Les types de valeurs de retour diffèrent. Trouvé "
					+ret1.toString()
					+" (branche \"then\") et "
					+ret2.toString()
					+" (branche \"else\")."
				  );
			typeRetour=null;
		}
		if((ret1==null || ret2==null)&&typeRetour==null){
			//s’il n’y avait pas de valeur de retour, et
			//si l’une des branches n’a pas de valeur de retour, 
			//on considère qu’il n’y en a pas à la sortie du bloc.
			typeRetour=null;
		}
//		À la fin, le seul cas où typeRetour n’est pas à null, 
//		c’est quand les deux blocs ont ont une valeur de retour, 
//		et que c’est la même. typeRetour reste alors du type de 
//		la val de retour de ret2.

		return statType;
	}


	public Type visit(StatPrint sp){
		checkReach(sp);
		Type t = sp.getExpression().accept(this);
		if(t.toString()!="int"){
			errors.add(sp,"L’Impression ne supporte que les entiers. (trouvé "
					+t.toString()+")");
		};
		return statType;
	}
	public Type visit(StatWhile s){
		checkReach(s);
		Type t = s.getExpression().accept(this);
		if(t.toString()!="bool"){
			errors.add(s,"La condition du while n’est pas du type bool. (trouvé "
				+t.toString() +")"
				  );
		}
		return s.getBlock().accept(this);
	}
	public Type visit(StatVarDecl s){
		checkReach(s);
		return super.visit(s);
	}

	public Type visit(Block b){
		checkReach(b);
		visitedBlocks.enter(b);
		Type t = super.visit(b);
		visitedBlocks.exit();
		return t;
	}





}
	

