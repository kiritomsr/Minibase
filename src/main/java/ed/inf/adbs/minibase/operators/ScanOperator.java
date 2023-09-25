package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.Interpreter;
import ed.inf.adbs.minibase.Catalog;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.Schema;
import ed.inf.adbs.minibase.base.Tuple;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class is to create ScanOperator instance to
 * scan records from corresponding database csv files
 * and create tuples, with a given RelationalAtom ra.
 */
public class ScanOperator extends Operator{

    private Catalog catalog;
    private RelationalAtom ra;
    private Scanner scanner = null;
    private Schema schema;

    /**
     * Constructor method, initialize instance variables and get the schema.
     */
    public ScanOperator(RelationalAtom ra){
        this.catalog = Catalog.getInstance();
        this.ra = ra;
        this.schema = Interpreter.getInstance().getScheme(ra.getName());
        reset();
    }

    /**
     * This method is used to get next tuple from scanner.
     * If scanner shows that the file still has a next line,
     * load it and new a tuple and return it.
     * If not, return null as a terminator.
     */
    @Override
    public Tuple getNextTuple() {
        if(scanner.hasNextLine()) {
            return new Tuple(scanner.nextLine(), ra.getTerms(), schema);
        }else {
            return null;
        }
    }

    /**
     * This method is used reset the scanner of this operator,
     * so that the scanner will read from the beginning next time.
     */
    @Override
    public void reset() {
        if(scanner != null) scanner.close();
        try {
            scanner = new Scanner(new File(catalog.getFilePath(ra.getName())));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
            catalog.writeOutputFile(tuple);
//            System.out.println("scanOp: "+tuple);
        }
    }
}
