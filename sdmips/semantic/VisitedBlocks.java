package semantic;
import java.util.Stack;
import ast.*;

public class VisitedBlocks{

	private Stack<Block> blocks;


	public VisitedBlocks(){
		this.blocks=new Stack<Block>();
	}

	public Stack<Block> getStack(){
		return blocks;
	}

	public void enter(Block b){
		blocks.push(b);
	}

	public void exit(){
		blocks.pop();
	}

	public Block current(){
		return blocks.peek();
	}

}
