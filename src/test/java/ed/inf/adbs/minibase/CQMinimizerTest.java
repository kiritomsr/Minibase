package ed.inf.adbs.minibase;

import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;

public class CQMinimizerTest {
    @Test
    public void test() throws IOException {
        int[] range = new int[]{1, 17};
        for(int i=range[0];i<=range[1];i++){
            CQMinimizer.minimizeCQ("data/minimization/input/query"+i+".txt", "data/minimization/output/query"+i+".txt");
            String input = (new BufferedReader(new FileReader(new File("data/minimization/input/query"+i+".txt")))).readLine();
            String output = (new BufferedReader(new FileReader(new File("data/minimization/output/query"+i+".txt")))).readLine();

            if(true){
                try {
                    String expect = (new BufferedReader(new FileReader(new File("data/minimization/expected_output/query"+i+".txt")))).readLine();
                    System.out.println(i+": -----------------------");
                    System.out.println("match: "+output.equals(expect));
                    if(!output.equals(expect)){

                        System.out.println("input: " + input);
                        System.out.println("output: " + output);
                        System.out.println("expect: " + expect);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

        }
    }
}