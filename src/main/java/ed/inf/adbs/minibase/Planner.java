package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.operators.*;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is to create Planner instance to generate query plan for given query,
 * the query plan is a tree structure with operators as its node.
 */
public class Planner {

    private Interpreter it;

    public Planner(Interpreter it){
        this.it = it;
    }

    /**
     * This method is to generate a query plan with the query information from Interpreter.
     * For every relational atoms, scan (must have), select, join, then sumAgg, and project.
     * The operators (except scan) will pass an if-condition test before applied. If applicable,
     * the operator will be added to the root as the parent of child nodes.
     * @return operator root of the query plan
     */
    public Operator generateQueryPlan(){
        List<RelationalAtom> ras = it.getRelationalAtoms();
        List<Term> relatedTerms = new ArrayList<>();
        List<ComparisonAtom> relatedCAS;
        SumAggregate sumAggregate = it.getQuery().getHead().getSumAggregate();
        Operator root = null;
        for(RelationalAtom ra: ras){

            // scan
            Operator scanOperator = new ScanOperator(ra);

            // select
            Operator selectOperator;
            List<Term> raTerms = ra.getTerms();
            relatedTerms.addAll(raTerms);
            relatedCAS = findRelatedCAS(raTerms);
            if(!relatedCAS.isEmpty()){
                selectOperator = new SelectOperator(scanOperator, relatedCAS);
            }else {
                selectOperator = scanOperator;
            }

            // join
            Operator joinOperator;
            if(ras.indexOf(ra) == 0){
                root = selectOperator;
            }else {
                relatedCAS = findRelatedCAS(relatedTerms);
                root = new JoinOperator(root, selectOperator, relatedCAS);
            }
        }
        // end of for
        // sumAgg
        if(sumAggregate != null){
            root = new SumAggOperator(root, sumAggregate);
        }

        // project
        if(!it.getDistVariables().equals(relatedTerms)){
            root = new ProjectOperator(root, it.getDistVariables(), sumAggregate);
        }

        return root;
    }

    /**
     * This method is to find all related ComparisonAtom from the list in Interpreter.
     * Then remove them from the list to avoid repeated select.
     * @param terms a list of related terms
     * @return a list of related ComparisonAtom
     */
    private List<ComparisonAtom> findRelatedCAS(List<Term> terms){
        List<ComparisonAtom> relatedCAS = new ArrayList<>();
        List<ComparisonAtom> cas = it.getComparisonAtoms();
        for (ComparisonAtom ca : cas){
            if(checkComparisonAtomApplicable(ca, terms)){
                relatedCAS.add(ca);
            }
        }
        for (ComparisonAtom ca: relatedCAS) cas.remove(ca);
        return relatedCAS;
    }

    /**
     * This method is to check if the given ComparisonAtom is applicable to
     * the given list of terms. If two constant, return true.
     * If one variable, contains return true.
     * If two variable, both contains return true.
     * @param ca the given ComparisonAtom to be checked
     * @param terms a list of related terms
     * @return boolean flag of check result
     */
    public boolean checkComparisonAtomApplicable(ComparisonAtom ca, List<Term> terms){
        Term caTerm1 = ca.getTerm1();
        Term caTerm2 = ca.getTerm2();
        boolean flag1 = true;
        boolean flag2 = true;

        if(caTerm1 instanceof Variable) flag1 = terms.contains(caTerm1);
        if(caTerm2 instanceof Variable) flag2 = terms.contains(caTerm2);

        return flag1 && flag2;
    }

}
