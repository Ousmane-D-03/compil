package semantic;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import support.Errors;
import ast.*;

public class SymbolTable {
	private Errors errors;
	public Errors getErrors(){return errors;}
	//pour les fonctions : 
	private Map<String,MethodSig> methods;
	//pour les blocs : chaque bloc
	// est associé à sa table locale.
	private Map<Block,Map<String,Type>> blocks;

	public SymbolTable(){
		methods=new HashMap<>();
		blocks=new HashMap<>();
		errors=new Errors();
	}

	public void addMethod(String nom,MethodSig ms){
		this.methods.put(nom,ms);
	}
	public void addLocalVariable(Block block,String nom,Type type){
		if(blocks.get(block)==null){
			errors.add(block,"erreur variable "+nom+" dans bloc "+block);
		}
		else{
			blocks.get(block).put(nom,type);
		}
	}
	public void localTable(Block sl){
		if(blocks.get(sl)==null){
			Map<String,Type> localT =new HashMap<>();
			blocks.put(sl,localT);
		}
	}
	public MethodSig methodLookup(String name){
		return methods.get(name);
	}
	public Type variableLookup(String name,VisitedBlocks vb){
		Type t=null;
		for(Block b: vb.getStack()){
			//System.out.print("lookup ["+name+"] in "+sl);
			Map<String,Type> table = blocks.get(b);
			if (table==null){
				String s =
					"[Table des symboles] : pas de table locale associée au bloc "+b;
				errors.add(b,s);
			}
			t=table.get(name);
			if(t!=null){
				//System.out.print(" -> oui ("+t.getName()+")\n");
				return t;
			}
			//System.out.print(" -> nope\n");
		}
		return t;
		//t peut être null
	}

	public void print(){
		System.out.println("Table des symboles : ");
		System.out.println("Méthodes : ");
		for (Map.Entry entry : methods.entrySet()){
			System.out.println("clé: "+entry.getKey()
					+ " | valeur: " 
					+ entry.getValue());
		}
		System.out.println("Blocs : ");
		for (Map.Entry entry : blocks.entrySet()){
			System.out.println("clé: "+entry.getKey()
					+ " | valeur: " 
					+ entry.getValue());
		}

	}

}
