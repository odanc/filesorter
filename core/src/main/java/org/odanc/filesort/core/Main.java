package org.odanc.filesort.core;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    
    public static void main(String... args) {
        
        // Checking just for sure, all args should be already checked at this point
        // This class is meant to be run from filesort-runner jar
        if (args.length != 2) {
            System.err.println("Wrong number of arguments");
            System.exit(-1);
        }
        
        Path sourceFile = Paths.get(args[0]);
        int maxHeapSize;
        
        try {
            maxHeapSize = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignore) {
            maxHeapSize = 0;
        }
        
        // Creating configuration with mandatory settings for file sorter to use
        Config config = Config.newBuilder(sourceFile, maxHeapSize).build();
        FileSorter fileSorter = new FileSorter(config);
        
        try {
            System.out.println("Sorting file " + sourceFile.toString());
            fileSorter.sort();
            System.out.println("Done!");
            
        // Checking just for sure, shouldn't happen if is run from filesort-runner jar    
        } catch (NoSuchFileException nsfe) {
            terminate("File " + sourceFile.toString() + " doesn't exist");
            
        // All exceptions of file sorting routine are printed to output stream.
        // Application execution is then aborted since there is nothing else to do.
        } catch (Exception e) {
            terminate(e);
        }
    }
    
    private static void terminate(String errorMessage) {
        terminate(errorMessage, null);
    }
    
    private static void terminate(Throwable e) {
        terminate(null, e);
    }
    
    // Print errors and terminate application execution
    private static void terminate(String errorMessage, Throwable e) {
        System.err.println("Error encountered");
        
        if (errorMessage != null) {
            System.err.println(errorMessage);
        }
        
        if (e != null) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        
        System.exit(-1);
    }
    
}
