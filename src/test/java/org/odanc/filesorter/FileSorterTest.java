package org.odanc.filesorter;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FileSorterTest {
    private static Path outputFile;
    private static boolean isWindows;

    @BeforeClass
    public static void setUp() {
        isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        outputFile = getPathTo("generated.txt");
    }

    @Test
    public void test_small_file_sort() throws Exception {
        Path sourceFile = getPathTo("unsorted_small.txt");
        Path sortedFile = getPathTo("sorted_small.txt");
        
        sortFile(sourceFile, outputFile);

        assertEquals("Files sizes are not equal", Files.size(sortedFile), Files.size(outputFile));
        
        byte[] sortedContents = Files.readAllBytes(sortedFile);
        byte[] outputContents = Files.readAllBytes(outputFile);
        assertArrayEquals("Files contents are not equal", sortedContents, outputContents);
    }
    
    @Test
    public void test_big_file_sort() throws IOException {
        Path sourceFile = getPathTo("unsorted_big.txt");
        Path sortedFile = getPathTo("sorted_big.txt");
        
        sortFile(sourceFile, outputFile);
        
        assertEquals("Files sizes are not equal", Files.size(sortedFile), Files.size(outputFile));
        
        // For comparing big files for content equality it might be more efficient
        // to compare their memory-mapped projections.
        // See http://codereview.stackexchange.com/a/90152
        long fileSize = Files.size(sortedFile);
        
        // Size of memory-mapped file buffer in kb
        long bufferSize = 512 * 1_024;
        
        try (FileChannel sortedChannel = FileChannel.open(sortedFile);
             FileChannel outputChannel = FileChannel.open(outputFile)) {
            
            for (long position = 0; position < Files.size(sortedFile); position += bufferSize) {
                MappedByteBuffer sortedBuffer = mapFileToBuffer(sortedChannel, position, fileSize, bufferSize);
                MappedByteBuffer outputBuffer = mapFileToBuffer(outputChannel, position, fileSize, bufferSize);
                assertEquals("Files contents are not equal", sortedBuffer, outputBuffer);
            }
        }
    }

    @After
    public void tearDown() {
        // On Windows Files.delete throws AccessDeniedException
        // Files.delete(outputFile);

        outputFile.toFile().delete();
    }
    
    private void sortFile(Path sourceFile, Path outputFile) throws IOException {
        Config config = Config.newBuilder(sourceFile, 8)
                              .setOutputFile(outputFile)
                              .build();
        new FileSorter(config).sort();
    }

    // Gets OS-dependent path to file
    private static Path getPathTo(String fileName) {
        String fullFileName = FileSorterTest.class.getResource("/").getFile() + fileName;
        return isWindows ? Paths.get(fullFileName.substring(1)) : Paths.get(fullFileName);
    }
    
    // Maps content of the file to memory-mapped buffer of the given size
    private MappedByteBuffer mapFileToBuffer(FileChannel channel, long position, long fileSize, long mapSize)
            throws IOException {
        long size = Math.min(fileSize, position + mapSize) - position;
        return channel.map(READ_ONLY, position, size);
    }

}