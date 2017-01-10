package org.odanc.filesort.core;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    
    public static void main(String... args) {
        
        // All args should be already checked at this point.
        // This class is meant to be run from filesorter-runner.jar
        if (args.length != 2) {
            terminate("wrong number of arguments");
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
            terminate("file " + sourceFile.toString() + " doesn't exist");
            
        // All exceptions of file sorting routine are printed to output stream.
        // Application execution is then aborted since there is nothing else to do.
        } catch (Exception e) {
            terminate(e);
        }
    }
    
    private static void terminate(String errorMessage) {
        System.err.println("Error: " + errorMessage);
        System.exit(-1);
    }
    
    private static void terminate(Throwable e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        System.exit(-1);
    }
    
}