package org.odanc.filesort.core;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration with all required setting for sorting files.
 * It stores settings like path to the source file, path to 
 * save sorted output file, words count to load from the source
 * file at once and the delimiter used to separate words in
 * the source file.
 */
class Config {
    private Path sourceFile;
    private Path outputFile;
    private int bufferSize;
    private int maxWordSize;
    private String delimiter;
    
    // Default name of the sorted output file
    static final Path DEFAULT_OUTPUT_FILE = Paths.get(
            System.getProperty("user.dir") + File.separator + "sorted.txt");
    
    // Default maximum single word size used to calculate words buffer size
    private static final int DEFAULT_MAX_WORD_SIZE = 100;
    
    // Minimum and maximum number of words to load from file at once,
    // sort them and store in a temporary file
    static final int MIN_BUFFER_SIZE = 200_000;
    static final int MAX_BUFFER_SIZE = 1_000_000;
    
    // Heap size in bytes not meant to be available for words buffer
    // It doesn't really mean that it won't be available for this
    // Used to calculate words buffer size
    private static final int RESERVED_HEAP_SIZE = 2_097_152;
    
    private Config() { }

    /**
     * Creates a configuration builder
     * 
     * @param sourceFile path to source file
     * @param heapSize heap size in megabytes
     */
    static ConfigBuilder newBuilder(Path sourceFile, Integer heapSize) {
        return new Config().new ConfigBuilder(sourceFile, heapSize);
    }

    Path getSourceFile() {
        return sourceFile;
    }

    Path getOutputFile() {
        return outputFile;
    }

    int getBufferSize() {
        return bufferSize;
    }

    String getDelimiter() {
        return delimiter;
    }
    
    
    
    
    
    /**
     * Configuration builder used for flexible tuning configuration.
     * It creates configuration with stores path to given source file
     * and desired maximum heap size. Optionally it is possible to set
     * the maximum size of a single word, the delimiter used to separate
     * words in the source file and a path to save the sorted output file
     */
    class ConfigBuilder {
        private int maxHeapSize;

        private ConfigBuilder(Path sourceFile, int heapSize) {
            Config.this.sourceFile = sourceFile;
            maxHeapSize = heapSize;
        }

        /**
         * Sets the path to save the sorted output file
         * 
         * @param outputFile path to save the output file
         * @return configuration builder
         */
        ConfigBuilder setOutputFile(Path outputFile) {
            Config.this.outputFile = outputFile;
            return this;
        }

        /**
         * Sets the maximum single word size in the source file.
         * Used to calculate the words buffer size
         * 
         * @param wordSize maximum single word size
         * @return configuration builder
         */
        ConfigBuilder setMaxWordSize(int wordSize) {
            maxWordSize = wordSize;
            return this;
        }

        /**
         * Sets the word delimiter in configuration
         * 
         * @param regex regex-pattern as a delimiter
         * @return configuration builder
         */
        ConfigBuilder setWordDelimiter(String regex) {
            delimiter = regex;
            return this;
        }

        /**
         * Creates a configuration with all required settings
         * 
         * @return a configuration used for file sorting
         */
        Config build() {
            Config config = new Config();
            
            if (maxHeapSize > 0) {

                // Approximate minimum memory usage in bytes for one word of MAX_WORD_SIZE length
                // based on http://stackoverflow.com/a/31207050
                int wordSize = 8 * (((maxWordSize * 2) + 45) / 8) - 8;

                // Sets the appropriate words buffer size based on heap size,
                // single word size and reserved heap size. Ensures that heap size
                // value is between MIN_BUFFER_SIZE and MAX_BUFFER_SIZE
                int bufferSize = (maxHeapSize * 1024 * 1024 - RESERVED_HEAP_SIZE) / wordSize;
                config.bufferSize = Math.min(Math.max(bufferSize, MIN_BUFFER_SIZE), MAX_BUFFER_SIZE);

            } else {
                config.bufferSize = MIN_BUFFER_SIZE;
            }
            
            // Do not permit negative single word size
            config.maxWordSize = maxWordSize > 0
                    ? maxWordSize
                    : DEFAULT_MAX_WORD_SIZE;
            
            // If the output file is not set use the current working directory
            config.outputFile = outputFile != null
                    ? outputFile
                    : DEFAULT_OUTPUT_FILE;
            
            config.sourceFile = sourceFile;
            config.delimiter = delimiter;
            
            return config;
        }
    }
}
