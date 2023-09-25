package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Minibase;
import ed.inf.adbs.minibase.Planner;
import ed.inf.adbs.minibase.operators.Operator;
import ed.inf.adbs.minibase.operators.ProjectOperator;
import ed.inf.adbs.minibase.operators.ScanOperator;
import ed.inf.adbs.minibase.operators.SelectOperator;
import ed.inf.adbs.minibase.parser.QueryParser;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CatalogTest extends TestCase {
    @Test
    public void test() throws IOException {

        //catalog
//        String input = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\input\\query" + index + ".txt";
//        String output = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\output\\query" + index + ".csv";
//        new Catalog("D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\db\\", input, output);
//        RelationalAtom ra = (RelationalAtom) Catalog.getQuery().getBody().get(0);
//        //schema
//        Schema schema = Catalog.getScheme("R");
//        System.out.println(schema.toString());
//
//        for (Schema schema1: Catalog.getSchemas().values()){
//            System.out.println(schema1.toString());
//        }
//
//        Tuple tuple = new Tuple("1, 9, 'adbs'", ra.getTerms(), schema);
//        System.out.println(((String) tuple.getValue(2)).charAt(0));
//
//        Schema schema1 = new Schema(" int int string");
//        System.out.println(schema1.equals(schema));
//        //tuple
//        Tuple tuple1 = new Tuple("1, 9, 'adbs'", ra.getTerms(), schema1);
//        System.out.println(tuple1.equals(tuple));
//        Planner.selectExplicit();
        int[] range = new int[]{1, 9};
//        List<Integer> tests = new ArrayList<>();
        for(int i=range[0]; i<=range[1]; i++){
            String db = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\db";
            String inputFile = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\input\\query" + i + ".txt";
            String outputFile = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\output\\query" + i + ".csv";
            String expectFile = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\expected_output\\query" + i + ".csv";
//            String db = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\db3\\";
//            String inputFile = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\input3\\query0" + i + ".txt";
//            String outputFile = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\output3\\query0" + i + ".csv";
//            String expectFile = "D:\\Users\\lenovo\\IdeaProjects\\Minibase\\data\\evaluation\\expected_output3\\query0" + i + ".csv";
            Minibase.evaluateCQ(db, inputFile, outputFile);

            StringBuilder output = new StringBuilder();

            String line;
            BufferedReader br = new BufferedReader(new FileReader(new File(outputFile)));
            while ((line = br.readLine()) != null) output.append(line).append("\n");
//            System.out.println(output);
            if(true){
                StringBuilder expect = new StringBuilder();
                br = new BufferedReader(new FileReader(new File(expectFile)));
                while ((line = br.readLine()) != null) expect.append(line).append("\n");

                boolean match = output.toString().equals(expect.toString());
                System.out.println(i+": "+ match);
                if(!match){
                    System.out.println(output);
                    System.out.println(expect);
                }
            }

        }


//        Query query = QueryParser.parse("Q(x, z, SUM(t*y)) :- R(x, y, z), S(x, w, t), x >= 5");
//        System.out.println(query.getHead().getSumAggregate());
//        System.out.println(query.getHead().getVariables());
//        System.out.println(query.getHead().getSumAggregate().getProductTerms());
//        System.out.println(query.getBody());


//        //scanOp
//        ScanOperator scanOperator = new ScanOperator(Catalog.getRelationalAtoms().get(0));
//        scanOperator.dump();
//
//        //selectOp
//        SelectOperator selectOperator = new SelectOperator(Catalog.getComparisonAtoms(), scanOperator);
//        selectOperator.dump();
//
//        //projOp
//        ProjectOperator projectOperator = new ProjectOperator(Catalog.getDistVariables(), selectOperator);
//        projectOperator.dump();


    }
}

