package mips;

import ir.Register;
import support.Errors;
import support.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Frame {

    private final Errors errorReporter;
    public Frame(Errors errorReporter) {
        this.errorReporter = errorReporter;
    }

    List<Register> allFromFrame(ir.Frame frame) {
	    //Cette fonction récupère tous les registres temporaires du cadre,
	    //plus celui pour le retour de la fonction.
        List<Register> registers = new LinkedList<>(frame.getParameters());
        registers.addAll(frame.getLocals());
        if (frame.getResult()==null){
		errorReporter.add("frame sans résultat");
	}
	else registers.add(frame.getResult());
        return registers;
    }
    private void updateRegAlloc(ir.Frame frame, Map<Register, Integer> regAlloc) {
	    //Cette fonction prend tous les registres locaux du cadre, et leur
	    //associe un décalage dans la map regAlloc. Les deux premières cases
	    //sont réservées pour l’ancien ra et l’ancien fp, puis les registres
	    //sont placés sous ces deux mots.

        List<Register> registers = allFromFrame(frame);
        int offset = -Program.DEFAULT_SIZE * 3;
        for (Register register : registers) {
            regAlloc.put(register, offset);
            offset -= Program.DEFAULT_SIZE;
        }
	//La taille du cadre correspond donc à une case par donnée temporaire,
	//plus les deux cases pour stocker l’adresse de retour et le pointeur de
	//cadre.
	
        frame.setSize(Program.DEFAULT_SIZE * (registers.size() + 2));
    }
    private List<String> generateBody(Map<Register, Integer> regAlloc,
                                      List<ir.com.Command> fragment) {
	    //Pour générer le corps de la fonction, facile.
	    //On crée un visiteur de commandes (class mips.Command)
	    //et on renvoie la liste des instructions assembleur.
        List<String> asmCode = new LinkedList<>();
        Command commandVisitor = new Command(errorReporter, regAlloc);
        for (ir.com.Command command : fragment) {
            asmCode.addAll(command.accept(commandVisitor));
        }
        return asmCode;
    }
    private List<String> generatePrologue(Map<Register, Integer> regAlloc,
                                          ir.Frame frame) {
	    //Que se passe-t-il à l’entrée dans une fonction ?
	    //- On met un nouveau cadre sur la pile. 	    
	    //Le nouveau pointeur de cadre (frame pointer = fp)
	    // correspondra au sommet de la pile (stack pointer = sp).
	    // Schématiquement : 
	    //		----------fp		…
	    //		…		-> 	…
	    //		…			…
	    //		----------sp		---------fp
	    //					 ^
	    //					 |
	    //					 | (taille du cadre) 
	    //					 |
	    //					 v
	    //					----------sp
	    //
	    //(il faut connaître la taille du nouveau cadre, pour savoir où
	    //mettre le pointeur de pile)
	    //Bien sûr, il faudra se rappeler de là où était le pointeur de cadre 
	    //avant qu’on le mette à jour.
	    //
	    //Algo :
	    //-> on enregistre l’adresse de retour ainsi que le pointeur de
	    //      cadre dans des registres (t0, t1)
	    //-> on met à jour fp et sp
	    //-> on stocke t0 et t1 sur la pile (à 4($sp) et 8($sp)
	    //    respectivement)
	    //-> on empile aussi les paramètres de la fonction
	    //  + on les lit dans les registres a0…a3,
	    //  + chacun est stocké en offset($fp)$, où offset est 
	    //    la valeur associé au registre dans la map regAlloc.
	    //c’est tout, on devrait donc avoir ça en sortie :
	    //
	    //	…
	    //	…
	    //	--------------fp (ancien sp)
	    //	…
	    //	[contenu des ai (si utilisés)]
	    //	…
	    //
	    //	…
	    //	[ancien ra]
	    //  [ancien sp]
	    //  --------------sp 
        List<String> asmCode = new LinkedList<>();
        asmCode.add(Asm.label(frame.getEntryPoint().toString()));
        asmCode.add(Asm.command("move $t0, $ra"));
        asmCode.add(Asm.command("move $t1, $fp"));
        asmCode.add(Asm.command("addi $sp, $sp, -" + frame.getSize()));
        asmCode.add(Asm.command("sw $t0, 4($sp)"));
        asmCode.add(Asm.command("sw $t1, 8($sp)"));
        asmCode.add(Asm.command("addi $fp, $sp, " + frame.getSize()));
        for (int i = 0; i < frame.getParameters().size(); i++) {
            Register reg = frame.getParameters().get(i);
            int offset = regAlloc.get(reg);
            asmCode.add(Asm.command("sw $a" + i + ", " + offset + "($fp)"));
        }
        return asmCode;
    }
    private List<String> generateEpilogue(Map<Register, Integer> regAlloc,
                                          ir.Frame frame) {
	//Que se passe-t-il à la sortie d’une fonction ?
	//
	//On doit récupérer l’adresse de retour et le pointeur de cadre qu’il y
	//avait avant d’entrer dans la fonction, pour les restaurer.
	//
        List<String> asmCode = new LinkedList<>();
        asmCode.add(Asm.label(frame.getExitPoint().toString()));
        int resultOffset = regAlloc.get(frame.getResult());
        asmCode.add(Asm.command("lw $v0, " + resultOffset + "($fp)"));
        asmCode.add(Asm.command("addi $sp, $fp, -" + frame.getSize()));
        asmCode.add(Asm.command("lw $ra, 4($sp)"));
        asmCode.add(Asm.command("lw $fp, 8($sp)"));
        asmCode.add(Asm.command("jr $ra"));
	return asmCode;
    }

	
    List<String> generate(Pair<ir.Frame, List<ir.com.Command>> fragment) {
	    //Pour la génération du code associé à une fonction : 
	    //- on récupère le frame 
	    //- on s’assure que la fonction n’ait pas plus de 4 arguments, sinon
	    //on envoie une erreur.
	    //- on crée une nouvelle map regAlloc pour l’allocation des registres sur la pile.
	    //- on la met à jour avec les données du frame (implémenté plus haut)
	    //- on renvoie le code composé du prologue, du corps (qui utilise la
	    //liste de commandes du fragment), et de
	    // l’épilogue.

	    ir.Frame frame = fragment.getFst();
	    if (frame.getParameters().size() > 4) {
	        errorReporter.add("Fonction avec plus de 4 arguments non supportée");
	        return new LinkedList<>();
	    }
	    Map<Register, Integer> regAlloc = new HashMap<>();
	    updateRegAlloc(frame, regAlloc);
	    List<String> asmCode = new LinkedList<>();
	    asmCode.addAll(generatePrologue(regAlloc, frame));
	    asmCode.addAll(generateBody(regAlloc, fragment.getSnd()));
	    asmCode.addAll(generateEpilogue(regAlloc, frame));
	    return asmCode;
	}
}
