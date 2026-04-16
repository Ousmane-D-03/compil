package semantic;

import ast.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ast.TypePrim;
import ast.Formal;
import ast.Type;

public class  Signatures{
	public static MethodSig buildUnaryVoid(TypePrim.Prim tp){
		Type t=new TypePrim(null,tp);
		List<Formal> lf=new ArrayList<>();
		lf.add(new Formal(null,t,null));
		return new MethodSig(lf,null);
	}


  public static final MethodSig printInt = buildUnaryVoid(TypePrim.Prim.INT);
  public static final MethodSig unaryBoolRTVoid = buildUnaryVoid(TypePrim.Prim.BOOL);

  //int à remplacer par string
  public static final MethodSig inputMethodSig = new MethodSig(new ArrayList<Formal>(), new TypePrim(null,TypePrim.Prim.INT));
  

  public static void addPredefinedSignature(SymbolTable symbolTable){
    symbolTable.addMethod("print", printInt);
    symbolTable.addMethod("print", unaryBoolRTVoid);
    symbolTable.addMethod("input", inputMethodSig);
  }
}
