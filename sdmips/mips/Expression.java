package mips;

import ir.Register;
//import ir.RegisterOffset;
import ir.Type;
import ir.expr.Byte;
import ir.expr.NewArray;
import ir.expr.ReadMem;
import ir.expr.*;
import support.ListTools;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static mips.Asm.sizeOf;

public class Expression implements ir.expr.Visitor<List<String>> {
	//À l’issue de la visite d’une expression, on génère une liste
	//d’instructions assembleur à l’issue de laquelle le résultat du calcul
	//de l’expression est stocké sur le sommet de la pile.

    private final Map<Register, Integer> regAddress;

    public Expression(Map<Register, Integer> regAddress) {
        this.regAddress = regAddress;
    }

    //Les octets et entiers sont d’abord chargés (avec 'li', pour simplifier
    //dans un premier temps)
    //dans un registre (t0), puis
    //empilés.
    @Override
    public List<String> visit(Byte exp) {
        List<String> asmCode =
                ListTools.mklist(Asm.command("li $t0, " + exp.getValue()));
        asmCode.addAll(Asm.push("$t0"));
        return asmCode;
    }

    @Override
    public List<String> visit(Int exp) {
        List<String> asmCode =
                ListTools.mklist(Asm.command("li $t0, " + exp.getValue()));
        asmCode.addAll(Asm.push("$t0"));
        return asmCode;
    }

    @Override
    //on récupère le décalage associé au registre grâce à regAddress.
    //Puis on charge la valeur à cette adresse en T0, que l’on empile.
    public List<String> visit(ReadReg exp) {
        int offset = regAddress.get(exp.getRegister());
        List<String> asmCode = new LinkedList<>();
        asmCode.add(Asm.command("lw $t0, " + offset + "($fp)"));
        asmCode.addAll(Asm.push("$t0"));
        return asmCode;
    }

    @Override
    public List<String> visit(NewArray exp) {
        List<String> code = exp.getSize().accept(this);
        code.addAll(Asm.pop("$t0"));
        code.add(Asm.command("sll $t0, $t0, 2"));
        code.add(Asm.command("li $v0, 9"));
        code.add(Asm.command("move $a0, $t0"));
        code.add(Asm.command("syscall"));
        code.add(Asm.command("move $t0, $v0"));
        code.addAll(Asm.push("$t0"));
        return code;
    }

    @Override
    public List<String> visit(ReadMem exp) {
        List<String> code = exp.getBase().accept(this);
        code.addAll(exp.getIndex().accept(this));
        code.addAll(Asm.pop("$t1"));
        code.addAll(Asm.pop("$t0"));
        code.add(Asm.command("sll $t1, $t1, 2"));
        code.add(Asm.command("addu $t0, $t0, $t1"));
        code.add(Asm.command("lw $t0, 0($t0)"));
        code.addAll(Asm.push("$t0"));
        return code;
    }
    //On compile l’expression, puis on dépile, avant d’empiler le résultat
    //obtenu par l’opération adaptée.
    @Override
    public List<String> visit(Unary exp) {
        List<String> code = exp.getExp().accept(this);
        code.addAll(Asm.pop("$t0"));
        switch (exp.getOp()) {
            case MIN -> code.add(Asm.command("subu $t0, $zero, $t0"));
            case NOT -> {
                code.add(Asm.command("li $t1, 4294967294"));
                code.add(Asm.command("nor $t0, $t1, $t0"));
            }
        }
        code.addAll(Asm.push("$t0"));
        return code;
    }

    @Override
    public List<String> visit(Binary exp) {
	    //on compile l’exp gauche puis l’exp droite,
	    //on a donc un code mettant deux valeurs sur la pile. 
	    //On les dépile dans deux registres, puis on empile le résultat de
	    //l’opération adaptée.



	List<String> codeLeft = exp.getLeft().accept(this);
	List<String> codeRight = exp.getRight().accept(this);
	
	String op=null;
	ast.BinOp eOp=exp.getOp();
    	
	switch(eOp){
        	case ADD -> op="add";
        	case MIN -> op="sub";
		case MULT -> op = "mul";
		case DIV -> op = "div";
		case AND -> op = "and";
		case OR -> op = "or";
		case LT -> op = "slt";
		case LEQ -> op = "sle";
		case GT -> op = "sgt";
		case GEQ -> op = "sge";
		case EQ -> op = "seq";
		case NEQ -> op = "sne";
    	}
List<String> asmCode = new LinkedList<>(codeLeft);
	asmCode.addAll(codeRight);
	asmCode.addAll(Asm.pop("$t1"));
	asmCode.addAll(Asm.pop("$t0"));
	if (op.equals("div")) {
		asmCode.add(Asm.command("div $t0, $t1"));
		asmCode.add(Asm.command("mflo $t0"));
	} else {
		asmCode.add(Asm.command(op + " $t0, $t0, $t1"));
	}
	asmCode.addAll(Asm.push("$t0"));
    	
	return asmCode;
    }
}
