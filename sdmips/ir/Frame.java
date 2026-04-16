package ir;

import ir.com.Label;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Frame
{
    final private Label entryPoint;
    final private Label exitPoint;
    final private List<Register> parameters;
    final private Register result;
    final private List<Register> locals;
    private int size;

    public Label getEntryPoint() {
        return entryPoint;
    }

    public Label getExitPoint() {
        return exitPoint;
    }

    public List<Register> getParameters() {
        return parameters;
    }


    public Register getResult() {
        return result;
    }

    public List<Register> getLocals() {
        return locals;
    }

    public void addLocal(Register register) {
        this.locals.add(register);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "\n  entryPoint=" + entryPoint +
                ",\n  exitPoint=" + exitPoint +
                ",\n  parameters=" + parameters +
                //",\n  passedByRef=" + passedByRef +
                ",\n  result=" + result +
                ",\n  locals=" + locals +
                ",\n  size=" + size +
                "\n}";
    }

    private Frame(Label entryPoint, 
		    Label exitPoint, 
		    List<Register> parameters,
		    Register result, 
		    int size) {
        this.entryPoint = entryPoint;
        this.exitPoint = exitPoint;
        this.parameters = parameters;
        this.result = result;
        this.size = size;
        this.locals = new LinkedList<>();
    }

    public Frame(Label entryPoint, 
		    Label exitPoint, 
		    List<Register> parameters, 
		    Register result) {
        this(entryPoint, exitPoint, parameters, result, 0);
    }
    

}
