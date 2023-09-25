package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.Catalog;
import ed.inf.adbs.minibase.Interpreter;
import ed.inf.adbs.minibase.base.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is to create SumAggOperator instance to
 * get tuples from its child operator. It would calculate
 * the sum value of aggregated variable, for each distinct
 * group-by variables group.
 */
public class SumAggOperator extends Operator{
    private Interpreter it;
    private Operator childOp;
    private List<Tuple> childTuples;
    private SumAggregate sumAggregate;
    private List<Variable> distVariables;
    private Map<List<Object>, List<Tuple>> groupMap;
    private Map<List<Object>, Integer> groupSums;


    public SumAggOperator(Operator childOp, SumAggregate sumAggregate){
        this.it = Interpreter.getInstance();
        this.childOp = childOp;
        this.childTuples = new ArrayList<>();
        this.sumAggregate = sumAggregate;
        this.distVariables = it.getDistVariables();
        this.groupMap = new HashMap<>();
        this.groupSums = new HashMap<>();
        generateSumAgg();
    }

    /**
     * This method is used to get all tuples from child operator.
     * divide tuples into groups, calculate the sum of each group,
     * and add a SumAgg term to the end of every singleTuple.
     * (value:groupSum, term:sumAgg, type:int)
     * Then, add this updated tuple to the tuple list.
     */
    private void generateSumAgg(){
        // get all child tuples
        childOp.reset();
        while (true){
            Tuple singleTuple = childOp.getNextTuple();
            if(singleTuple == null) break;
            if(!childTuples.contains(singleTuple)) childTuples.add(singleTuple);
        }

        if(distVariables.size() == 0){
            int sum = 0;
            for (Tuple singleTuple: childTuples){
                sum += getSingleContribute(singleTuple);
            }
            for (Tuple singleTuple: childTuples){
                singleTuple.getValues().add(sum);
                singleTuple.getSchema().getTypes().add("int");
                singleTuple.getTerms().add(sumAggregate);
            }

        }else {
            // divide tuples into groups
            for (Tuple singleTuple: childTuples){
                List<Object> distValues = new ArrayList<>();
                for (Variable distVar: distVariables){
                    distValues.add(singleTuple.getValue(distVar));
                }
                if(groupMap.containsKey(distValues)){
                    groupMap.get(distValues).add(singleTuple);
                    groupSums.replace(distValues, groupSums.get(distValues)+getSingleContribute(singleTuple));
                }else {
                    List<Tuple> newGroup = new ArrayList<>();
                    newGroup.add(singleTuple);
                    groupMap.put(distValues, newGroup);
                    groupSums.put(distValues, getSingleContribute(singleTuple));
                }
            }

            // update singleTuple from each group, add value:groupSum, term:sumAgg, type:int
            for (List<Object> distValues: groupMap.keySet()){
                List<Tuple> group = groupMap.get(distValues);
                for (Tuple singleTuple: group){
                    singleTuple.getValues().add(groupSums.get(distValues));
                    singleTuple.getSchema().getTypes().add("int");
                    singleTuple.getTerms().add(sumAggregate);
                }
            }
        }
    }

    /**
     * This method calculate the contribution of the given tuple
     * to the group sum by given product terms.
     * @param tuple given tuple to be calculated
     * @return int value of the product
     */
    private int getSingleContribute(Tuple tuple){
        List<Term> productTerms = sumAggregate.getProductTerms();
        int contribute = 1;
        for (Term productTerm: productTerms){
            if(productTerm instanceof Variable){
                contribute = contribute * ((int) tuple.getValue(productTerm));
            }else if(productTerm instanceof IntegerConstant){
                contribute = contribute * ((IntegerConstant) productTerm).getValue();
            }
        }
        return contribute;
    }

    /**
     * This method is used to return a tuple from its tuple list,
     * remove the returned one from list, until empty the list.
     */
    @Override
    public Tuple getNextTuple() {
        if(childTuples.size() == 0) return null;
        Tuple tuple = childTuples.get(0);
        childTuples.remove(tuple);
//        System.out.println("sumAggOp: "+tuple);
        return tuple;
    }

    /**
     * This method is used reset this operator, by resetting its child operator.
     */
    @Override
    public void reset() {
        childOp.reset();
    }

    /**
     * This method is used dump all tuples in the list,
     * meanwhile write the tuple back to output file.
     * @see Catalog#writeOutputFile(Tuple)
     */
    @Override
    public void dump() {
        reset();
        for(Tuple tuple: childTuples){
            Catalog.getInstance().writeOutputFile(tuple);
//            System.out.println("sumaggOp: "+tuple);
        }
    }
}
