package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.Catalog;
import ed.inf.adbs.minibase.base.*;

import java.util.ArrayList;
import java.util.List;

import static ed.inf.adbs.minibase.operators.SelectOperator.compareTuple;

/**
 * This class is to create JoinOperator instance to
 * merge tuples from two child operators, with given
 * ComparisonAtoms, based on simple nested loop join.
 */
public class JoinOperator extends Operator{

    private Operator childOp1;
    private Operator childOp2;
    private List<ComparisonAtom> cas;
    private Tuple tuple1;
    private Tuple tuple2;

    public JoinOperator(Operator childOp1, Operator childOp2, List<ComparisonAtom> cas) {
        this.childOp1 = childOp1;
        this.childOp2 = childOp2;
        this.cas = cas;
        this.tuple1 = null;
        this.tuple2 = null;
    }

    /**
     * This method is used to get next tuple.
     * It follows the simple nested loop join algorithm.
     * The left child is in outer and the right child is in inner.
     * Each time this method is called, it would grasp tuples from both the left and the right operator.
     * If there are no join conditions, it would directly return the merged tuple. Otherwise, it
     * would call compareTuple method to check whether the merged tuple satisfy it.
     * Whenever the inner loop arrive its end, reset it and advance the left operator.
     * When the left operator touch its end, return null.
     * @see ed.inf.adbs.minibase.operators.SelectOperator#compareTuple(Tuple, List)
     */
    @Override
    public Tuple getNextTuple() {

        while (true){
            if(tuple2 == null){
                tuple1 = childOp1.getNextTuple();
                if(tuple1 == null) return null;
                childOp2.reset();
                tuple2 = childOp2.getNextTuple();
            }
            Tuple tuple = mergeTuple(tuple1, tuple2);
            tuple2 = childOp2.getNextTuple();
            if(cas.size()!=0){
                if (compareTuple(tuple, cas)) {
                    return tuple;
                }
            }else return tuple;
        }
    }

    /**
     * A method that used to create a new tuple based on the merge of given two tuples
     * @param tuple1 First tuple to merge.
     * @param tuple2 Second tuple to merge.
     * @return Merged tuple.
     */
    public static Tuple mergeTuple(Tuple tuple1, Tuple tuple2){
        List<Object> newValues = new ArrayList<>();
        List<String> newTypes = new ArrayList<>();
        List<Term> newTerms = new ArrayList<>();

        for(Tuple t: new Tuple[]{tuple1, tuple2}){
            for (int i=0; i<t.getValues().size(); i++){
                if(!newTerms.contains(t.getTerm(i))){
                    newValues.add(t.getValue(i));
                    newTerms.add(t.getTerm(i));
                    newTypes.add(t.getSchema().getType(i));
                }
            }
        }
        return new Tuple(newValues, newTerms, new Schema(newTypes));
    }

    /**
     * This method is used reset this operator, by resetting its child operators.
     */
    @Override
    public void reset() {
        childOp1.reset();
        childOp2.reset();
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
            Catalog.getInstance().writeOutputFile(tuple);
//            System.out.println("joinOp: "+tuple);
        }
    }
}
