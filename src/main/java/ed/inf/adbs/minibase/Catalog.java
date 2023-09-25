package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Tuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Catalog is a collection of static methods for Minibase tasks, designed
 * in singleton pattern. It deals with db/input/output files.
 */
public class Catalog {

    private static Catalog instance;
    private static String dbRoot;
    private static String inputFile;
    private static String outputFile;

    public Catalog(String dbRoot, String inputFile, String outputFile){
        Catalog.dbRoot = dbRoot;
        Catalog.inputFile = inputFile;
        Catalog.outputFile = outputFile;
        try {
            new File(outputFile).delete();
            new File(outputFile).createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        instance = this;
    }

    public static Catalog getInstance() {
        return instance;
    }

    public void checkInit(){
        if(instance == null){
            throw new RuntimeException("Catalog is not initialized!");
        }
    }

    public String getDbRoot() {
        checkInit();
        return dbRoot;
    }

    public String getFilePath(String filename){
        checkInit();
        return dbRoot + File.separator + "files" + File.separator + filename + ".csv";
    }

    public String getInputFile() {
        checkInit();
        return inputFile;
    }

    /**
     * Material the tuple into output file and connect values with ", "
     * @param tuple tuple to be written
     */
    public void writeOutputFile(Tuple tuple){
//        System.out.println(tuple);
        BufferedWriter bw = null;
        try {
            bw= new BufferedWriter(new FileWriter(outputFile, true));
            String str = Utils.join(tuple.getValues(),", ");
            bw.write(str);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bw != null){
                try {
                    bw.close();
                } catch (IOException e) {
                    System.err.println("Exception occurred during writing query back to file");
                    e.printStackTrace();
                }
            }
        }
    }

}
