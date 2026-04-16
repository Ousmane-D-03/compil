package main;

import java.io.*;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import parser.sdmLexer;
import parser.sdmParser;
import ast.*;
import printers.*;
import semantic.*;
import ir.translation.Translate;
import ir.com.*;
import ir.Frame;
import support.*;

import java.nio.file.*;


public class Main{

	private static ParseTree parse(InputStream inputStream) throws IOException{
		CharStream input = CharStreams.fromStream(inputStream);
		sdmLexer lexer = new sdmLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		sdmParser parser = new sdmParser(tokens);
		ParseTree tree = parser.program();
		if(parser.getNumberOfSyntaxErrors()!=0){
			System.out.println("erreur de syntaxe : sortie après analyse syntaxique");
			System.exit(1);
		}
		return tree;
	}

					
	public static void main(String[] args) throws IOException{

		System.out.println("---- Analyse Syntaxique -----");	
		InputStream inputStream = System.in;
		ParseTree tree = parse(inputStream);
		
		System.out.println("---- Construction AST -----");	
		AstBuild astB=new AstBuild();
		Program ast= (Program) tree.accept(astB);

		System.out.println("---- Affichage AST -----");	
		AstPrinter printer = new AstPrinter();
		ast.accept(printer);
		System.out.print("\n");
	


		System.out.println("\n---- Construction Table -----\n");	
		TableBuilder tb= new TableBuilder();
		ast.accept(tb);
		SymbolTable st=tb.getTable();
		System.out.println("\n---- Vérif de Types -----\n");	
		TypeChecker tc=new TypeChecker(st);
		ast.accept(tc);
		tc.check();

		System.out.println("\n--- Traduction en code intermédiaire ---\n");
		
		Pair<Label,List<Pair<Frame,List<Command>>>> irCode 
			= Translate.run (st, ast);
		
		//IrPrinter ip = new IrPrinter();
		//ip.print(irCode);
		

		System.out.println("\n--- Traduction en assembleur ---\n");

		String name="out.asm";
		if(args.length==1){
			name=args[0];
		}
		
		Path path = FileSystems.getDefault().getPath(name);
		compile(path,irCode.getFst(),irCode.getSnd());
	}

	public static void compile (Path path,
			Label mainLabel,
			List<Pair<Frame,List<Command>>> fragments
			)
	{
		Path newPath=FileSystems.getDefault().getPath(
				changeExtension(path, ".sdm", ".asm").getFileName().toString());
		mips.Program.generate(newPath,mainLabel,fragments);
		Errors errs=mips.Program.errors;
		if(errs.hasErrors()){
			System.out.println("Erreur génération MIPS");
			errs.print();
			System.exit(1);
		}
	}

    private static Path changeExtension(Path path, String oldExt, String newExt) {
        PathMatcher pm = FileSystems.getDefault()
          .getPathMatcher("glob:*" + oldExt);
        if (pm.matches(path.getFileName())) {
            String nameWithExtension = path.getFileName().toString();
            int endIndex = nameWithExtension.length() - oldExt.length();
            String name = nameWithExtension.substring(0, endIndex);
            if (path.getParent() != null)
                return path.getParent().resolve(name + newExt);
            else
                return FileSystems.getDefault().getPath(name + newExt);
        }
        return path;
    }


}
