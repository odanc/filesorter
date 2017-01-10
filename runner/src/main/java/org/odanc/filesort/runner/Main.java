package org.odanc.filesort.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Runner for file sorting application. It accepts 2 mandatory
 * input arguments: path to source file which will be sort and
 * maximum heap size jvm can use for this application.
 * 
 */
public class Main {
    private static String CORE_MODULE_FILE_NAME = "filesort-core.jar";
    
    public static void main(String... args) {
        
        // Check that filesort-core module is located in current working directory
        // since there is no point to proceed further if it's not
        if (Files.notExists(Paths.get(CORE_MODULE_FILE_NAME))) {
            terminate("couldn't locate " + CORE_MODULE_FILE_NAME + " in the current directory");
        }
        
        if (args.length != 2) {
            terminate("wrong number of arguments");
        }

        try {
            
            Path fileName = Paths.get(args[0]);
            if (Files.notExists(fileName)) {
                terminate("file doesn't exist");
            }

            String heapSizeValue = args[1];
            
            // Check that max heap size value is valid: 512m, 4G, 32M, 1g
            if (!heapSizeValue.matches("\\d+[mMgG]")) {
                terminate("invalid max heap size, must match to \"\\d[mMgG]\" pattern");
            }
            
            runFileSortProcess(fileName, heapSizeValue);
            
        } catch (InvalidPathException e) {
            terminate("invalid file name");
            
        // Terminate application execution if any exception raises
        } catch (Exception e) {
            terminate(e);
        }
    }
    
    // Spawns a new java process with max heap size limit that will sort the given file
    private static void runFileSortProcess(Path fileName, String heapSizeValue)
            throws IOException, InterruptedException {
        
        // Extracting heap size
        Integer heapSize = Integer.parseInt(heapSizeValue.substring(0, heapSizeValue.length() - 1));
        
        // Extracting heap size measure (megabytes or gigabytes)
        String measure = heapSizeValue.substring(heapSizeValue.length()).toLowerCase();
        
        // Converts heap size to megabytes if neccessary
        if ("g".equals(measure)) {
            heapSize *= 1_024;
        }

        // Start a new java process with given max heap size
        new ProcessBuilder("java", "-Xmx" + heapSizeValue, "-jar",
                CORE_MODULE_FILE_NAME, fileName.toString(), heapSize.toString())
                .inheritIO()
                .start()
                .waitFor();
    }

    private static void terminate(String errorMessage) {
        System.err.println("Error: " + errorMessage);
        System.err.println("Usage: java -jar <runner.jar> <path to source file> <max heap size>");
        System.exit(-1);
    }

    private static void terminate(Throwable e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        System.exit(-1);
    }
    
}
