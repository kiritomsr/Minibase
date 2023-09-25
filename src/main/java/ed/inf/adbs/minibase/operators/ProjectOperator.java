package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.Catalog;
import ed.inf.adbs.minibase.base.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is to create ProjectOperator instance to
 * get tuples from its child operator, project them on
 * the given Variables. It maintains a list of tuples
 * that has been returned to eliminate duplicates.
 */
public class ProjectOperator extends Operator{
    private Operator childOp;
    private List<Variable> dvs;
    private SumAggregate sumAggregate;
    private List<Tuple> buffer = new ArrayList<>();

    public ProjectOperator(Operator childOp, List<Variable> dvs, SumAggregate sumAggregate){
        this.childOp = childOp;
        this.dvs = dvs;
        this.sumAggregate = sumAggregate;
    }

    public Operator getChildOp() {
        return childOp;
    }

    /**
     * This method is used to get the projected tuple.
     * @param t the tuple provided to project on.
     * @return the tuple after projection
     */
    private Tuple project(Tuple t){
        List<Object> newValues = new ArrayList<>();
        List<String> newTypes = new ArrayList<>();
        List<Term> newTerms = new ArrayList<>(dvs);

        for (Variable dv: dvs){
            newValues.add(t.getValue(dv));
            if(t.getValue(dv) instanceof Integer) newTypes.add("int");
            if(t.getValue(dv) instanceof String) newTypes.add("string");
        }
        if(sumAggregate != null){
//            System.out.println("sumAggregate value: "+t.getValue(sumAggregate));
            newValues.add(t.getValue(t.getValues().size()-1));
            newTypes.add("int");
            newTerms.add(sumAggregate);
        }

        return new Tuple(newValues, newTerms, new Schema(newTypes));
    }

    /**
     * This method is used to get next tuple from child operator,
     * if child operator still has tuple, then get and project it.
     * If tuple has been returned then grasp next tuple from child,
     * until no tuple left.
     */
    @Override
    public Tuple getNextTuple() {

        while (true){
            Tuple tuple = childOp.getNextTuple();
            if(tuple == null) return null;
            tuple = project(tuple);
//            System.out.println(tuple);
            if(!buffer.contains(tuple)){
                buffer.add(tuple);
                return tuple;
            }
        }

    }

    /**
     * This method is used reset this operator,
     * by resetting its child operator and buffer.
     */
    @Override
    public void reset() {
        buffer = new ArrayList<>();
        childOp.reset();
    }

    /**
     * This method is used dump the tuple, keep calling this.getNextTuple()
     * until it returns null, meanwhile write the tuple back to output file.
     * @see Catalog#writeOutputFile(Tuple)
     */
    @Override
    public void dump() {
        reset();
        while (true){
            Tuple tuple = this.getNextTuple();
            if(tuple == null) break;
            if(!buffer.contains(tuple)){
                buffer.add(tuple);
            }
        }
        for(Tuple tuple: buffer){
            Catalog.getInstance().writeOutputFile(tuple);
//            System.out.println("projOp: "+tuple);
        }
    }

}
