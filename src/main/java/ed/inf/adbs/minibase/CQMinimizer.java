package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Minimization of conjunctive queries
 * In each round, the query is tried to be reduced until the minimization.
 * To find a query homomorphism, a qualified atom is selected and tested
 * whether this atom can be mapped into another atom. Add all possible
 * Atom-to-Atom mapping to candidate list. Try to merge single mappings and
 * apply this bigger mapping to all body atoms and get a new body. If all
 * atoms in the new body is contained in the previous body, this query
 * homomorphism is qualified and replace the old body with new one. Repeat.
 * @author Shuren Miao
 *
 */
public class CQMinimizer {

    // a list of distinguished variables appear in head
    private static List<Variable> distVariables;
    // a list of non-distinguished variables appear in body but not in head
    private static List<Variable> nonDistVariables;
    // a list of relational atoms in body
    private static List<RelationalAtom> bodyAtoms;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];
        minimizeCQ(inputFile, outputFile);
    }

    /**
     * CQ minimization procedure
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     * @param inputFile path of input query.txt
     * @param outputFile path of output query.txt
     */
    public static void minimizeCQ(String inputFile, String outputFile) {
        // TODO: add your implementation
        try {
            Query query = QueryParser.parse(Paths.get(inputFile));
            Head head = query.getHead();
            List<Atom> body = query.getBody();

            distVariables = head.getVariables();
            nonDistVariables = new ArrayList<Variable>();
            bodyAtoms = new ArrayList<RelationalAtom>();

            for(Atom atom: body){
                if(atom instanceof RelationalAtom){
                    bodyAtoms.add((RelationalAtom) atom);
                    for(Term term: ((RelationalAtom) atom).getTerms()){
                        if(term instanceof Variable && !distVariables.contains(term) && !nonDistVariables.contains(term)){
                            nonDistVariables.add((Variable) term);
                        }
                    }
                }
            }

            // repeatedly call tryMinimization() until it returns false
            while (tryMinimization()){};

            // write the body atoms with head into output file
            List<Atom> result = new ArrayList<>(bodyAtoms);
            Query output = new Query(query.getHead(), result);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(output.toString());
            writer.close();
        }catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    /**
     * repeated CQ reduce procedure
     * @return boolean flag of reduce success
     */
    public static boolean tryMinimization(){
        List<Map<Variable, Term>> atomMaps = new ArrayList<>();
        for(RelationalAtom rAtom: bodyAtoms){
            List<RelationalAtom> otherAtoms = new ArrayList<>(bodyAtoms);
            otherAtoms.remove(rAtom);
            // if this atom only contains distVariables or constants, skip.
            if (checkNonDistVariable(rAtom)) continue;

            // if this atom contains the last distVariables, skip.
            if(lastDistVariable(rAtom, otherAtoms)) continue;

            // if this atom can be mapped to other atoms, add to the map list
            for(RelationalAtom otherAtom: otherAtoms){
                Map<Variable, Term> atomMap = atomMapping(rAtom, otherAtom);
                if(atomMap!=null) atomMaps.add(atomMap);
            }
        }

        // select candidate map from list, merge them if possible, then test CQ containment
        for (Map<Variable, Term> atomMap: atomMaps){
            List<Map<Variable, Term>> otherMaps = new ArrayList<>(atomMaps);
            otherMaps.remove(atomMap);
            Map<Variable, Term> mergedMap = new HashMap<>(atomMap);
            for (Map<Variable, Term> otherAtomMap: otherMaps){
                Map<Variable, Term> outcome = mergeMap(mergedMap, otherAtomMap);
                if(outcome == null) break;
                mergedMap = outcome;
            }
            // if new body is contained in old body, this mapping is a qualified query homomorphism
            if(checkBodyContain(mergedMap)) {
                bodyAtoms = mappedBody(mergedMap);
                return true;
            }
        }
        return false;
    }

    /**
     * check if the given atom does not contain non-distinguished variables (to be mapped)
     * @param rAtom given atom (to be checked)
     * @return boolean flag of check result
     */
    public static boolean checkNonDistVariable(RelationalAtom rAtom) {
        boolean nonDistVar = false;
        for(Term rTerm: rAtom.getTerms()){
            if(rTerm instanceof Variable && containsThisVariable(nonDistVariables, (Variable) rTerm)){
                nonDistVar = true;
                break;
            }
        }
        return !nonDistVar;
    }

    /**
     * check if the given atom contains the last distinguished variable,
     * which does not appear in any other atoms
     * @param rAtom given atom (to be checked)
     * @param otherAtoms other atoms in body, except the given one
     * @return boolean flag of check result
     */
    public static boolean lastDistVariable(RelationalAtom rAtom, List<RelationalAtom> otherAtoms){
        for(Term rTerm: rAtom.getTerms()){
            // if this term is distinguished variable
            if(rTerm instanceof Variable && containsThisVariable(distVariables, (Variable) rTerm)){
                boolean lastDistVar = true;
                for(RelationalAtom otherAtom: otherAtoms){
                    for (Term oTerm: otherAtom.getTerms()){
                        // if another atom contains this same distinguished variable, check next distVar
                        if(oTerm instanceof Variable && rTerm.equals(oTerm)) {
                            lastDistVar = false;
                            break;
                        }
                    }
                    if (!lastDistVar) break;
                }
                // if all other atoms do not contain this distinguished variable, return true
                if(lastDistVar) return true;
            }
        }
        return false;
    }

    /**
     * check if the given variable is contained in the list
     * @param vars a list of variables
     * @param thisVar given variable (to be checked)
     * @return boolean flag of check result
     */
    public static boolean containsThisVariable(List<Variable> vars, Variable thisVar){
        for (Variable var: vars){
            if(var.equals(thisVar)){
                return true;
            }
        }
        return false;
    }

    /**
     * check if the target atom is contained in the given list
     * @param target target atom (to be checked)
     * @param atoms  given atom list
     * @return boolean flag of check result
     */
    public static boolean checkAtomContain(RelationalAtom target, List<RelationalAtom> atoms){
        for(RelationalAtom atom: atoms){
            if(target.equals(atom)) return true;
        }
        return false;
    }

    /**
     * return the mapped term from the source variable and the map,
     * if hit, return the value in map
     * if not hit, return the original (term) variable
     * @param var source variable (to be mapped)
     * @param atomMap given mapping on terms
     * @return mapped term
     */
    public static Term mappedTerm(Variable var, Map<Variable, Term> atomMap){
        for(Variable key: atomMap.keySet()){
            if(var.equals(key)) return atomMap.get(key);
        }
        return var;
    }

    /**
     * generate a new atom from the source atom and the map
     * @param srcAtom source atom (to be mapped)
     * @param atomMap given mapping on terms
     * @return new mapped atom
     */
    public static RelationalAtom mappedAtom(RelationalAtom srcAtom, Map<Variable, Term> atomMap){
        List<Term> terms = new ArrayList<>();
        for (Term src: srcAtom.getTerms()){
            if(src instanceof Variable && !containsThisVariable(distVariables, (Variable) src)){
                terms.add(mappedTerm((Variable) src, atomMap));
            }else {
                terms.add(src);
            }
        }
        return new RelationalAtom(srcAtom.getName(), terms);
    }

    /**
     * generate new body atoms from the previous body and the given map
     * @param atomMap given mapping on terms
     * @return new body atoms
     */
    public static List<RelationalAtom> mappedBody(Map<Variable, Term> atomMap){
        List<RelationalAtom> newBody = new ArrayList<>();
        for(RelationalAtom before: bodyAtoms){
            RelationalAtom after = mappedAtom(before, atomMap);
            if(!checkAtomContain(after, bodyAtoms)){
                return null;
            }else if(!checkAtomContain(after, newBody)){
                newBody.add(after);
            }
        }
        return newBody;
    }

    /**
     * try to find a map from source to destination relational atom,
     * if not applicable, return null
     * @param srcAtom source relational atom (to be reduced)
     * @param dstAtom destination relational atom
     * @return a map from srcAtom variables to corresponding dstAtom terms
     */
    public static Map<Variable, Term> atomMapping(RelationalAtom srcAtom, RelationalAtom dstAtom){
        // if different schema name, return null
        if(!(srcAtom.getName()).equals(dstAtom.getName())) {
            return null;
        }
        Map<Variable, Term> atomMap = new HashMap<>();
        for(int i=0; i<srcAtom.getTerms().size(); i++){
            Term src = srcAtom.getTerms().get(i);
            Term dst = dstAtom.getTerms().get(i);
            // if src term is a constant, dst term must be the same
            if(src instanceof Constant){
                if(!(dst instanceof Constant)) {
                    return null;
                }else if(!src.equals(dst)){
                    return null;
                }
            }else if(src instanceof Variable){
                // if src term is a distVariables, dst term must be the same
                if(containsThisVariable(distVariables, (Variable) src)){
                    if(dst instanceof Variable){
                        Variable srcVar = (Variable) src;
                        Variable dstVar = (Variable) dst;
                        if(!srcVar.equals(dstVar)) return null;
                        atomMap.put((Variable) src, dst);
                    }
                }else{
                    // if src term is a nonDistVariables, add a map<srcVar, dstTerm>
                    if(!src.equals(dst)) atomMap.put((Variable) src, dst);
                }
            }
        }
        return atomMap;
    }

    /**
     * try to merge two single atom->atom maps together,
     * if contains conflicts, return null
     * @param map1 single atom->atom map to be merged
     * @param map2 single atom->atom map to be merged
     * @return a merged map contains kv pairs from map1 and map2
     */
    public static Map<Variable, Term> mergeMap(Map<Variable, Term> map1, Map<Variable, Term> map2){
        Map<Variable, Term> mergedMap = new HashMap<>(map1);
        for(Variable var2: map2.keySet()){
            if(map1.containsValue(var2)) return null;
            else if(map1.containsKey(var2)){
                if(!map2.get(var2).equals(map1.get(var2))) return null;
            }else {
                mergedMap.put(var2, map2.get(var2));
            }
        }
        return mergedMap;
    }

    /**
     * check if all atoms after mapped is contained in the previous body
     * @param atomMap given mapping on terms
     * @return boolean flag of check result
     */
    public static boolean checkBodyContain(Map<Variable, Term> atomMap){
        for(RelationalAtom before: bodyAtoms){
            RelationalAtom after = mappedAtom(before, atomMap);
            // if an atom after mapping can not be found in previous bodyAtoms, return false
            if(!checkAtomContain(after, bodyAtoms)){
                return false;
            }
        }
        return true;
    }


    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */
    public static void parsingExample(String filename) {

        try {
//            Query query = QueryParser.parse(Paths.get(filename));
            Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w)");
//             Query query = QueryParser.parse("Q(x) :- R(x, 'z'), S(4, z, w)");

            System.out.println("Entire query: " + query);
            Head head = query.getHead();
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            System.out.println("Body: " + body);

            System.out.println("Name: " + head.getName());
            System.out.println("Var: " + head.getVariables());

            Map<Term, Term> atomMap = new HashMap<Term, Term>();

            atomMap.put(((RelationalAtom)body.get(0)).getTerms().get(0), ((RelationalAtom)body.get(0)).getTerms().get(1));

            System.out.println(atomMap.get(((RelationalAtom) body.get(0)).getTerms().get(0)));
            System.out.println(atomMap.get(((RelationalAtom) body.get(0)).getTerms().get(0)));


        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

}
