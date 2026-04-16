package mips;

import ir.Register;
import ir.com.*;
import support.Errors;
import support.ListTools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static mips.Asm.sizeOf;

public class Command implements ir.com.Visitor<List<String>> {
    final private Expression exprVisitor;
    final private Map<Register, Integer> regAlloc;
    public Command(Errors errorReporter, Map<Register, Integer> regAlloc) {
        this.regAlloc = regAlloc;
        this.exprVisitor = new Expression(regAlloc);
    }
    @Override
    public List<String> visit(Label com) {
        return ListTools.mklist(Asm.label(com.toString()));
    }
    @Override
    public List<String> visit(WriteReg com) {
	    //On compile l’expression à écrire. Puis on dépile dans un registre.
	    //On récupère le décalage du registre à écrire, puis on écrit  à
	    //cette adresse la valeur dépilée.
        List<String> asmCode = new LinkedList<>();
        asmCode.addAll(com.getExp().accept(exprVisitor));
        asmCode.addAll(Asm.pop("$t0"));
        int offset = regAlloc.get(com.getReg());
        asmCode.add(Asm.command("sw $t0, " + offset + "($fp)"));
        return asmCode;
    }
    @Override
    public List<String> visit(CJump com) {
	    //Saut conditionnel : si l’expression s’évalue à zero, on saute au
	    //label "faux", sinon au label "vrai".
	    //(Les noms des labels sont ceux de l’ir).
        List<String> asmCode = com.getCondition().accept(exprVisitor);
        asmCode.addAll(Asm.pop("$t0"));
        asmCode.add(Asm.command("beq $t0, $zero, " + com.getFalseLabel()));
        asmCode.add(Asm.command("j " + com.getTrueLabel()));
	return asmCode;
    }
    @Override
    public List<String> visit(Jump com) {
	    //Saut non conditionnel au Label adapté.
        return ListTools.mklist(Asm.command("j " + com.getGotoLabel()));
    }
    private List<String> passArguments(List<ir.expr.Expression> exps) {
	//Cette fonction prend une liste d’expressions (on supposera qu’il n’y
	//en a pas plus de 4 par construction), puis les compile avec le
	//visiteur exprVisitor (mips.Expression) en une suite d’instructions. 
	//On suppose que le code généré par la visite de chaque expression
	//stocke le résultat sur le sommet de la pile (dans un TP précédent, on
	//faisait le même type d’hypothèse avec v0 par exemple).

	//Pour chacune de ces expressions, on fait en sorte que son résultat
	//soit stocké le registre adapté (après avoir compilé la première, on
	//dépile dans a0, etc…).
	
	//n.b: Asm.pop(reg) stocke le sommet de sp dans reg.
	
        List<String> asmCode = new LinkedList<>();
        List<String> popAndCopy = new LinkedList<>();
        int counter = exps.size() - 1;
        for (ir.expr.Expression exp : exps) {
            asmCode.addAll(exp.accept(exprVisitor));
            popAndCopy.addAll(Asm.pop("$a" + counter));
            counter -= 1;
        }
        asmCode.addAll(popAndCopy);
        return asmCode;
    }
    @Override
    public List<String> visit(FunCall com) {
	    //On fait le passage d’arguments.
	    //Ensuite on saute au point d’entrée de la fonction en sauvegardant
	    //l’adresse de l’instruction suivante dans $ra.
	    //
	    //On récupère le décalage associé a registre de l’instruction com.
	    //On stocke v0 à cette adresse (Pour rappel, en MIPS, c’est le registre dédié
	    //au retour des fonctions)
        List<String> asmCode = passArguments(com.getArguments());
        asmCode.add(Asm.command("jal " + com.getFrame().getEntryPoint()));
        int offset = regAlloc.get(com.getRegister());
        asmCode.add(Asm.command("sw $v0, " + offset + "($fp)"));
        return asmCode;
    }

    @Override
    public List<String> visit(WriteMem com) {
        List<String> asmCode = new LinkedList<>();
        asmCode.addAll(com.getBase().accept(exprVisitor));
        asmCode.addAll(com.getIndex().accept(exprVisitor));
        asmCode.addAll(com.getValue().accept(exprVisitor));
        asmCode.addAll(Asm.pop("$t0"));
        asmCode.addAll(Asm.pop("$t1"));
        asmCode.addAll(Asm.pop("$t2"));
        asmCode.add(Asm.command("sll $t1, $t1, 2"));
        asmCode.add(Asm.command("addu $t2, $t2, $t1"));
        asmCode.add(Asm.command("sw $t0, 0($t2)"));
        return asmCode;
    }
}
