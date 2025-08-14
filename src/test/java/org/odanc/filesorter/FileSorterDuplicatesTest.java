package org.odanc.filesorter;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FileSorterDuplicatesTest {
    private static Path outputFile;
    private static boolean isWindows;

    @BeforeClass
    public static void setUp() {
        isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        outputFile = getPathTo("generated_dup.txt");
    }

    @Test
    public void test_duplicates_sort() throws Exception {
        Path sourceFile = getPathTo("unsorted_duplicates.txt");
        Path sortedFile = getPathTo("sorted_duplicates.txt");

        sortFile(sourceFile, outputFile);

        assertEquals("Files sizes are not equal", Files.size(sortedFile), Files.size(outputFile));

        byte[] sortedContents = Files.readAllBytes(sortedFile);
        byte[] outputContents = Files.readAllBytes(outputFile);
        assertArrayEquals("Files contents are not equal", sortedContents, outputContents);
    }

    @After
    public void tearDown() {
        outputFile.toFile().delete();
    }

    private void sortFile(Path sourceFile, Path outputFile) throws Exception {
        Config config = Config.newBuilder(sourceFile, 8)
                              .setOutputFile(outputFile)
                              .build();
        new FileSorter(config).sort();
    }

    private static Path getPathTo(String fileName) {
        String fullFileName = FileSorterDuplicatesTest.class.getResource("/").getFile() + fileName;
        return isWindows ? Paths.get(fullFileName.substring(1)) : Paths.get(fullFileName);
    }
}
