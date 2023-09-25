package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.operators.Operator;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static ed.inf.adbs.minibase.operators.SelectOperator.compareOperate;

/**
 * This class takes the responsibility of interpreting schema and query from
 * the given filepath and get an operator root from query planner, designed
 * in singleton pattern.
 */
public class Interpreter {
    private static Interpreter instance;

    // a mapping from relational name to schema
    private Map<String, Schema> schemas;
    // a query object parsing from input
    private Query query;
    // a list of RelationalAtoms
    private List<RelationalAtom> relationalAtoms;
    // a list of ComparisonAtoms
    private List<ComparisonAtom> comparisonAtoms;
    // a list of distinguished variables appear in head
    private List<Variable> distVariables;
    // a list of non-distinguished variables appear in body but not in head
    private List<Variable> nonDistVariables;
    // a query planner to generate operator root
    private Planner planner;

    /**
     * This constructor is to interpret schema and query from the given filepath,
     * translate implicit conditions both for single and join relations,
     * remove constant comparison atoms,
     * and create a query planner.
     */
    public Interpreter(){
        instance = this;
        initSchemas(Catalog.getInstance().getDbRoot());
        initQuery(Catalog.getInstance().getInputFile());

        selectExplicit();
        joinSelectExplicit();

        optimizeComparisonAtoms();

        planner = new Planner(this);
    }

    public static Interpreter getInstance(){
        if(instance == null) instance = new Interpreter();
        return instance;
    }

    public void dump(){
        Operator root = planner.generateQueryPlan();
        root.dump();
    }

    public Query getQuery() {
        return query;
    }

    public Schema getScheme(String name){
        return schemas.get(name);
    }

    public List<RelationalAtom> getRelationalAtoms() {
        return relationalAtoms;
    }

    public List<ComparisonAtom> getComparisonAtoms() {
        return comparisonAtoms;
    }

    public List<Variable> getDistVariables() {
        return distVariables;
    }

    public List<Variable> getNonDistVariables() {
        return nonDistVariables;
    }

    /**
     * This method is used to read all schemas from schema.txt, and store
     * them in a map for later access.
     * @param dbRoot The directory root of database files.
     */
    private void initSchemas(String dbRoot){
        try (BufferedReader reader = new BufferedReader(new FileReader(dbRoot+File.separator+"schema.txt"))) {
            this.schemas = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] lineUnits = line.split(" ");
                List<String> types = new ArrayList<>(Arrays.asList(lineUnits).subList(1, lineUnits.length));
                schemas.put(lineUnits[0], new Schema(types));
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * This method is used to read the query from the given file name,
     * extract relational / comparison Atoms from the body,
     * extract distinguished / non-distinguished variables from the head and relational atoms,
     * and store them as instance variables for later access.
     * @param inputFile The filename of input query file.
     */
    private void initQuery(String inputFile){

        try {
            this.query = QueryParser.parse(Paths.get(inputFile));
            this.relationalAtoms = new ArrayList<>();
            this.comparisonAtoms = new ArrayList<>();
            this.distVariables = query.getHead().getVariables();
            this.nonDistVariables = new ArrayList<>();
            for(Atom atom: query.getBody()){
                if(atom instanceof RelationalAtom) {
                    relationalAtoms.add((RelationalAtom) atom);
                    for(Term term: ((RelationalAtom) atom).getTerms()){
                        if(term instanceof Variable && !distVariables.contains(term) && !nonDistVariables.contains(term)){
                            nonDistVariables.add((Variable) term);
                        }
                    }
                }else if(atom instanceof ComparisonAtom){
                    comparisonAtoms.add((ComparisonAtom) atom);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to extract implicit select comparison atoms from the body.
     * For each terms in every RelationalAtom, if it is a Constant, e.g. rewrite
     * R(x, y, 4) to R(x, y, a) by introducing a new variable and add a new comparison
     * atom a = 4.
     */
    private void selectExplicit(){
        for(RelationalAtom ra: relationalAtoms){
            if(atomContainsConstant(ra)){
                for(Term term: ra.getTerms()){
                    if(term instanceof Constant) {
                        Variable newVariable = getNewVariable();
                        ra.getTerms().set(ra.getTerms().indexOf(term), newVariable);
                        comparisonAtoms.add(new ComparisonAtom(newVariable, term, ComparisonOperator.EQ));
                        nonDistVariables.add(newVariable);
                    }
                }
            }
        }
    }

    /**
     * This method is used to extract implicit join comparison atoms from the body.
     * For each variable in every RelationalAtom, if there is another variable with
     * same name appearing in former RelationalAtom, then replace the former variable
     * with a new one, e.g. rewrite R(x, y, z), S(x, w) to R(x, y, z), S(a, w) by
     * introducing a new variable a and add a new comparison atom x = a.
     */
    private void joinSelectExplicit(){
        if(relationalAtoms.size()>1){
            for (int i=0;i<relationalAtoms.size()-1;i++){
                for (int j=i+1; j<relationalAtoms.size();j++){
                    RelationalAtom ra1 = relationalAtoms.get(i);
                    RelationalAtom ra2 = relationalAtoms.get(j);
                    for(Term term1: ra1.getTerms()){
                        for (Term term2: ra2.getTerms()){
                            if(term1.equals(term2)){
                                Variable newVar = getNewVariable();
                                int l = comparisonAtoms.size();
                                for (int k=0; k<l; k++){
                                    ComparisonAtom ca = comparisonAtoms.get(k);
                                    Term newTerm1 = ca.getTerm1();
                                    Term newTerm2 = ca.getTerm2();
                                    if(newTerm1.equals(term1)){
                                        newTerm1 = newVar;
                                    }
                                    if(newTerm2.equals(term1)){
                                        newTerm2 = newVar;
                                    }
                                    ComparisonAtom newCA = new ComparisonAtom(newTerm1, newTerm2, ca.getOp());
                                    if(!comparisonAtoms.contains(newCA)) comparisonAtoms.add(newCA);
                                }

                                ra2.getTerms().set(ra2.getTerms().indexOf(term2), newVar);
                                ComparisonAtom newCA = new ComparisonAtom(term1, newVar, ComparisonOperator.EQ);
                                if(!comparisonAtoms.contains(newCA)) comparisonAtoms.add(newCA);
                                nonDistVariables.add(newVar);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is to check if the given relational atom contains constant terms,
     * @param ra given relational atom to be checked
     * @return boolean flag of check result
     */
    private boolean atomContainsConstant(RelationalAtom ra){
        for(Term term: ra.getTerms()){
            if(term instanceof Constant) return true;
        }
        return false;
    }

    /**
     * This method is to get a new variable which its name has not been used in other places.
     * @return a new variable
     */
    private Variable getNewVariable(){
        for (int i = (int)'a';i<=(int)'z';i++){
            if(checkVariableName(""+(char)i)) return new Variable(""+(char)i);
        }
        for (int i = (int)'a';i<=(int)'z';i++){
            for (int j = (int)'a';j<=(int)'z';j++){
                if(checkVariableName(""+(char)i+(char)j)) return new Variable(""+(char)i+(char)j);
            }
        }
        return null;
    }

    /**
     * This method is to check if the given variable name has been used in other places.
     * @param name given variable name to be checked
     * @return boolean flag of check result
     */
    private boolean checkVariableName(String name){
        Variable newVar = new Variable(name);
        for(Variable dv: distVariables){
            if(newVar.equals(dv)) return false;
        }
        for(Variable ndv: nonDistVariables){
            if(newVar.equals(ndv)) return false;
        }
        return true;
    }

    /**
     * This method is used to optimize the query by removing constant comparison atoms,
     * if true, remove it, if false, simplify the query to get a null output.
     */
    private void optimizeComparisonAtoms(){
        ComparisonAtom falseCA = new ComparisonAtom(new IntegerConstant(1), new IntegerConstant(0), ComparisonOperator.EQ);
        for(ComparisonAtom ca: comparisonAtoms){

            if(ca.getTerm1() instanceof Constant && ca.getTerm2() instanceof Constant){
                if(compareOperate(ca.getTerm1(), ca.getTerm2(), ca.getOp())){
                    comparisonAtoms.remove(ca);
                }else {
                    relationalAtoms = relationalAtoms.subList(0, 1);
                    comparisonAtoms = new ArrayList<>();
                    comparisonAtoms.add(falseCA);
                    return;
                }
            }
        }
    }


}
