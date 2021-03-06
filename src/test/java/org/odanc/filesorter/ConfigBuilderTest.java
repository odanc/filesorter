package org.odanc.filesorter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.odanc.filesorter.Config.ConfigBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.odanc.filesorter.Config.DEFAULT_OUTPUT_FILE;
import static org.odanc.filesorter.Config.MAX_BUFFER_SIZE;
import static org.odanc.filesorter.Config.MIN_BUFFER_SIZE;

public class ConfigBuilderTest {
    private static Path sourceFile;
    private static Path outputFile;

    @BeforeClass
    public static void setUp() {
        String userHomeDir = System.getProperty("user.home");
        sourceFile = Paths.get(userHomeDir);
        outputFile = Paths.get("output.txt");
    }

    @Test
    public void test_default_config_builder() {
        ConfigBuilder builder = Config.newBuilder(sourceFile, 0);
        
        Config config = builder.build();
        assertMinBufferSize(config);
        assertEquals("source files paths are not equal", sourceFile, config.getSourceFile());
        assertNull("word delimiter is set", config.getDelimiter());
        assertEquals("output files paths are not equal", DEFAULT_OUTPUT_FILE, config.getOutputFile());
        
        builder = Config.newBuilder(sourceFile, -1);
        assertMinBufferSize(builder.build());
        
        builder = Config.newBuilder(sourceFile, 1_024);
        assertMaxBufferSize(builder.build());
    }
    
    @Test
    public void test_custom_word_size_config_builder() {
        ConfigBuilder builder = Config.newBuilder(sourceFile, 0);
        assertMinBufferSize(builder.build());
        assertMinBufferSize(builder.setMaxWordSize(-1).build());
        assertMinBufferSize(builder.setMaxWordSize(0).build());
        assertMinBufferSize(builder.setMaxWordSize(1_000).build());
        
        builder = Config.newBuilder(sourceFile, 2_048);
        assertMaxBufferSize(builder.build());
        assertMaxBufferSize(builder.setMaxWordSize(-1).build());
        assertMaxBufferSize(builder.setMaxWordSize(0).build());
        assertMaxBufferSize(builder.setMaxWordSize(1_000).build());
    }
    
    @Test
    public void test_custom_dest_delimiter_config_builder() {
        ConfigBuilder builder = Config.newBuilder(sourceFile, 0);

        Config config = builder.setOutputFile(outputFile).build();
        assertEquals("output files paths are not equal", outputFile, config.getOutputFile());

        config = builder.setWordDelimiter("\\.").build();
        assertNotNull("delimiter is not set", config.getDelimiter());
    }
    
    private void assertMinBufferSize(Config config) {
        assertEquals("buffer sizes are not equal", MIN_BUFFER_SIZE, config.getBufferSize());
    }
    
    private void assertMaxBufferSize(Config config) {
        assertEquals("buffer sizes are not equal", MAX_BUFFER_SIZE, config.getBufferSize());
    }

}