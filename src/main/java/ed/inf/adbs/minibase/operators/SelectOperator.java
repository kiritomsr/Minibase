package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.Catalog;
import ed.inf.adbs.minibase.base.*;

import java.util.List;


/**
 * This class is to create SelectOperator instance to
 * select tuples from its child operator, with given
 * ComparisonAtoms.
 */
public class SelectOperator extends Operator{
//    private Catalog Catalog = Catalog.getInstance();
    private List<ComparisonAtom> comparisonAtoms;
    private Operator childOp;

    public SelectOperator(Operator childOp, List<ComparisonAtom> comparisonAtoms) {
        this.comparisonAtoms = comparisonAtoms;
        this.childOp = childOp;
    }

    public Operator getChildOp() {
        return childOp;
    }

    /**
     * This method is used to get next tuple from child operator,
     * if child operator still has tuple, then get and test it.
     * If tuple passes these conditions then return it, else
     * grasp next tuple from child, until no tuple left.
     */
    @Override
    public Tuple getNextTuple() {
        while (true){
            Tuple tuple = childOp.getNextTuple();
            if(tuple == null) return null;
            if(compareTuple(tuple, comparisonAtoms)){
                return tuple;
            }
        }
    }

    /**
     * This method is used reset this operator, by resetting its child operator.
     */
    @Override
    public void reset() {
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
//            System.out.println("selectOp: "+tuple);
            Catalog.getInstance().writeOutputFile(tuple);
        }
    }

    /**
     * Compare whether a tuple pass given lists of ComparisonAtom.
     * @param tuple tuple to compare.
     * @param comparisonAtoms a list of comparison atoms
     * @return true, if the tuple pass these conditions, else return false.
     */
    public static boolean compareTuple(Tuple tuple, List<ComparisonAtom> comparisonAtoms){
        for(ComparisonAtom ca: comparisonAtoms){
            if(!singleCompareTuple(tuple, ca)){
                return false;
            }
        }
        return true;
    }

    /**
     * Compare whether a tuple pass the given ComparisonAtom.
     * @param tuple tuple to compare.
     * @param ca a comparison atom
     * @return true, if the tuple pass this condition, else return false.
     */
    public static boolean singleCompareTuple(Tuple tuple, ComparisonAtom ca){
        Term term1 = ca.getTerm1();
        Term term2 = ca.getTerm2();
        ComparisonOperator op = ca.getOp();

        // replace variable with value
        if(term1 instanceof Variable){
            Object value1 = tuple.getValue(term1);
            if(value1 instanceof String){
                term1 = new StringConstant(((String) value1).replace("'", ""));
            }else {
                term1 = new IntegerConstant((Integer) value1);
            }
        }

        if(term2 instanceof Variable){
            Object value2 = tuple.getValue(term2);
            if(value2 instanceof String){
                term2 = new StringConstant(((String) value2).replace("'", ""));
            }else {
                term2 = new IntegerConstant((Integer) value2);
            }
        }

        return compareOperate(term1, term2, op);
    }

    /**
     * Compare whether two terms meet the given ComparisonOperator.
     * @param term1 first term to be compared.
     * @param term2 second term to be compared.
     * @param op a Comparison Operator
     * @return true, if this comparison is passed, else return false.
     */
    public static boolean compareOperate(Term term1, Term term2, ComparisonOperator op){
        if(!term1.getClass().equals(term2.getClass())) {
            return false;
        }else if(term1 instanceof IntegerConstant){
            int value1 = ((IntegerConstant) term1).getValue();
            int value2 = ((IntegerConstant) term2).getValue();
            switch (op){
                case EQ: return value1 == value2;
                case NEQ: return value1 != value2;
                case GT: return value1 > value2;
                case GEQ: return value1 >= value2;
                case LT: return value1 < value2;
                case LEQ: return value1 <= value2;
            }
        }else {
            String value1 = ((StringConstant) term1).getValue();
            String value2 = ((StringConstant) term2).getValue();
            switch (op){
                case EQ: return value1.equals(value2);
                case NEQ: return !value1.equals(value2);
                case GT: return value1.compareTo(value2) > 0;
                case GEQ: return value1.compareTo(value2) >= 0;
                case LT: return value1.compareTo(value2) < 0;
                case LEQ: return value1.compareTo(value2) <= 0;
            }
        }
        return false;
    }



}
