package printers;
import java.util.List;
import ir.expr.*;
import ir.com.*;
import ir.translation.Result;
import support.Pair;
import ir.Frame;

public class IrPrinter{
	public void print(Pair<Label,List<Pair<Frame,List<Command>>>> code ){
		Label label=code.getFst();
		List<Pair<Frame,List<Command>>> list = code.getSnd();
		System.out.println("Label "+label.toString());
		for(Pair<Frame,List<Command>> l : list){
			System.out.println(l.getFst().toString());
			for(Command c : l.getSnd()){
				System.out.println(c);
			}
		}
	}

}
