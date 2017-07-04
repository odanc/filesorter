package org.odanc.filesorter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.WRITE;

class FileSorter {
    private Config config;
    
    // Used for storing all the temporary file names
    private List<Path> tempFiles;

    /**
     * File Sorter which sorts file from the given configuration
     * 
     * @param configuration configuration with all required settings
     */
    FileSorter(Config configuration) {
        config = configuration;
    }

    /**
     * Sorts content of the source file from the configuration
     * and outputs the result to the new destination file which is
     * set in the configuration.
     * Sorting strategy is based on split-and-merge basis.
     * The source file is splitted on numerous most likely smaller files
     * with contents of source file already sorted. After that all intermediate
     * files are merged into one bigger file with all content sorted.
     * 
     * @throws IOException I/O error happened while processing file
     */
    void sort() throws IOException {
        tempFiles = new ArrayList<>();
        Path outputFile = config.getOutputFile();
        
        splitFile(config.getSourceFile());
        mergeFiles(tempFiles, outputFile);
    }
    
    // Reads the words from given file and outputs them
    // to numerous temporary files in sorted order
    private void splitFile(Path sourceFile) throws IOException {
        
        // Sets the maximum words for one temporary file
        int wordsPerFile = config.getBufferSize();
        List<String> words = new ArrayList<>(wordsPerFile);

        try (Scanner scanner = new Scanner(sourceFile, UTF_8.name())) {
            String delimiter = config.getDelimiter();
            if (delimiter != null) {
                scanner.useDelimiter(delimiter);
            }
            
            int currentSize = 0;

            // Reads word by word from file until the threshold is reached.
            // Then begins to process the words buffer.
            // After processing is done, clears the buffer size and
            // repeats reading the words.
            while (scanner.hasNext()) {
                words.add(scanner.next());
                currentSize += 1;

                if (currentSize == wordsPerFile) {
                    currentSize = 0;
                    processWords(words);
                    words.clear();
                }
            }
            
            // Last words from file containing in the words buffer.
            if (!words.isEmpty()) {
                processWords(words);
            }
            
            if (scanner.ioException() != null) {
                throw scanner.ioException();
            }
        }
    }

    // Merges contents of the files from list and outputs them to single file
    private void mergeFiles(List<Path> files, Path outputFile) throws IOException {
        
        // Words buffer with mappings to files the words came from.
        // Words are stored in sorted order.
        SortedMap<WordWrapper, Scanner> wordToFileMap = new TreeMap<>();
        
        if (!outputFile.toFile().exists()) {
            Files.createFile(outputFile);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, UTF_8, WRITE)) {
            
            // Reads the first word from each temporary file, maps it
            // to the file it was read from and stores it in the buffer.
            for (Path file : files) {
                Scanner scanner = new Scanner(file, UTF_8.name());
                if (scanner.hasNext()) {
                    WordWrapper wrapper = new WordWrapper(scanner.next());
                    wordToFileMap.put(wrapper, scanner);
                }

                checkForReadErrors(scanner);
            }

            // Writes the first word from the buffer to the output file,
            // removes it from buffer, reads the next word from the file
            // the written word was from, repeates.
            // The loop continues until all temporary files are entirely read.
            while (!wordToFileMap.isEmpty()) {
                WordWrapper wrapper = wordToFileMap.firstKey();
                Scanner scanner = wordToFileMap.remove(wrapper);
                writer.write(wrapper.getWord());
                writer.newLine();
                
                if (scanner.hasNext()) {
                    wrapper = new WordWrapper(scanner.next());
                    wordToFileMap.put(wrapper, scanner);
                } else {
                    scanner.close();
                }

                checkForReadErrors(scanner);
            }
        }
    }

    // Sorts words in the word buffer and sends the buffer
    // to writing temporary file procedure
    private void processWords(List<String> words) throws IOException {

        // Lambdas required more heap space than anonymous comparator-object
        // words.sort(String::compareToIgnoreCase);

        //noinspection Convert2Lambda,Anonymous2MethodRef
        words.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        writeFile(words);
    }

    // Writes words from the words buffer to a temporary file
    // and stores the temporary file name
    private void writeFile(List<String> words) throws IOException {
        Path tempFile = Files.createTempFile(null, null);
        Files.write(tempFile, words, UTF_8, WRITE);
        
        tempFiles.add(tempFile);
    }
    
    private void checkForReadErrors(Scanner scanner) throws IOException {
        if (scanner.ioException() != null) {
            throw scanner.ioException();
        }
    }
    
    
    
    
    
    /**
     * A wrapper to String class since strings need to be keys in a HashMap
     * and there is a need for duplicate string keys. Keys come from files so it is
     * fine to have repeateable strings
     */
    private class WordWrapper implements Comparable<WordWrapper> {
        private String word;

        WordWrapper(String wrappedString) {
            this.word = wrappedString;
        }

        String getWord() {
            return word;
        }

        @Override
        public int compareTo(WordWrapper other) {
            return word.compareTo(other.getWord());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != WordWrapper.class) {
                return false;
            } else {
                WordWrapper other = (WordWrapper) obj;
                String word = other.getWord();
                return word != null && word.equals(this.getWord());
            }
        }
    }
}
