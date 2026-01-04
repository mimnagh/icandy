package com.icandy.unit;

import com.icandy.build.AssociationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AssociationManager.
 */
class AssociationManagerTest {
    
    private AssociationManager manager;
    
    @BeforeEach
    void setUp() {
        manager = new AssociationManager();
    }
    
    @Test
    void testAddAssociation_SingleWord() {
        String[] images = {"image1.jpg", "image2.jpg"};
        manager.addAssociation("hello", images);
        
        String[] retrieved = manager.getImagesForWord("hello");
        assertArrayEquals(images, retrieved);
    }
    
    @Test
    void testAddAssociation_CaseInsensitive() {
        String[] images = {"image1.jpg"};
        manager.addAssociation("Hello", images);
        
        String[] retrieved = manager.getImagesForWord("hello");
        assertArrayEquals(images, retrieved);
        
        retrieved = manager.getImagesForWord("HELLO");
        assertArrayEquals(images, retrieved);
    }
    
    @Test
    void testAddAssociation_MultipleWords() {
        manager.addAssociation("hello", new String[]{"hello1.jpg", "hello2.jpg"});
        manager.addAssociation("world", new String[]{"world1.jpg"});
        
        assertEquals(2, manager.getImagesForWord("hello").length);
        assertEquals(1, manager.getImagesForWord("world").length);
    }
    
    @Test
    void testAddAssociation_AppendToExisting() {
        manager.addAssociation("test", new String[]{"image1.jpg"});
        manager.addAssociation("test", new String[]{"image2.jpg"});
        
        String[] retrieved = manager.getImagesForWord("test");
        assertEquals(2, retrieved.length);
    }
    
    @Test
    void testAddAssociation_NullWord() {
        manager.addAssociation(null, new String[]{"image.jpg"});
        assertEquals(0, manager.getWordCount());
    }
    
    @Test
    void testAddAssociation_EmptyWord() {
        manager.addAssociation("", new String[]{"image.jpg"});
        assertEquals(0, manager.getWordCount());
    }
    
    @Test
    void testAddAssociation_NullImagePaths() {
        manager.addAssociation("test", null);
        assertEquals(0, manager.getWordCount());
    }
    
    @Test
    void testAddAssociation_EmptyImagePaths() {
        manager.addAssociation("test", new String[0]);
        assertEquals(0, manager.getWordCount());
    }
    
    @Test
    void testAddAssociation_AllNullImagePaths() {
        manager.addAssociation("test", new String[]{null, null});
        assertEquals(0, manager.getWordCount());
        assertFalse(manager.hasWord("test"));
    }
    
    @Test
    void testAddAssociation_AllEmptyStringImagePaths() {
        manager.addAssociation("test", new String[]{"", "  "});
        assertEquals(0, manager.getWordCount());
        assertFalse(manager.hasWord("test"));
    }
    
    @Test
    void testGetImagesForWord_NonExistent() {
        String[] retrieved = manager.getImagesForWord("nonexistent");
        assertEquals(0, retrieved.length);
    }
    
    @Test
    void testGetImagesForWord_NullWord() {
        String[] retrieved = manager.getImagesForWord(null);
        assertEquals(0, retrieved.length);
    }
    
    @Test
    void testSaveToFile_AndLoadFromFile(@TempDir Path tempDir) throws IOException {
        // Add some associations
        manager.addAssociation("hello", new String[]{"hello1.jpg", "hello2.jpg"});
        manager.addAssociation("world", new String[]{"world1.jpg"});
        
        // Save to file
        Path filepath = tempDir.resolve("associations.json");
        manager.saveToFile(filepath.toString());
        
        // Verify file exists
        assertTrue(Files.exists(filepath));
        
        // Load into new manager
        AssociationManager newManager = new AssociationManager();
        newManager.loadFromFile(filepath.toString());
        
        // Verify associations were loaded correctly
        assertArrayEquals(
            manager.getImagesForWord("hello"),
            newManager.getImagesForWord("hello")
        );
        assertArrayEquals(
            manager.getImagesForWord("world"),
            newManager.getImagesForWord("world")
        );
        assertEquals(manager.getWordCount(), newManager.getWordCount());
    }
    
    @Test
    void testSaveToFile_CreatesParentDirectories(@TempDir Path tempDir) throws IOException {
        Path filepath = tempDir.resolve("subdir/associations.json");
        
        manager.addAssociation("test", new String[]{"test.jpg"});
        manager.saveToFile(filepath.toString());
        
        assertTrue(Files.exists(filepath));
    }
    
    @Test
    void testSaveToFile_NullFilepath() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.saveToFile(null);
        });
    }
    
    @Test
    void testSaveToFile_EmptyFilepath() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.saveToFile("");
        });
    }
    
    @Test
    void testLoadFromFile_NonExistentFile() {
        assertThrows(IOException.class, () -> {
            manager.loadFromFile("nonexistent.json");
        });
    }
    
    @Test
    void testLoadFromFile_NullFilepath() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.loadFromFile(null);
        });
    }
    
    @Test
    void testLoadFromFile_InvalidJson(@TempDir Path tempDir) throws IOException {
        Path filepath = tempDir.resolve("invalid.json");
        Files.writeString(filepath, "not valid json");
        
        assertThrows(IOException.class, () -> {
            manager.loadFromFile(filepath.toString());
        });
    }
    
    @Test
    void testLoadFromFile_MissingAssociationsKey(@TempDir Path tempDir) throws IOException {
        Path filepath = tempDir.resolve("missing_key.json");
        Files.writeString(filepath, "{\"metadata\": {}}");
        
        assertThrows(IOException.class, () -> {
            manager.loadFromFile(filepath.toString());
        });
    }
    
    @Test
    void testVerifyImageFiles_AllExist(@TempDir Path tempDir) throws IOException {
        // Create actual image files
        Path image1 = tempDir.resolve("image1.jpg");
        Path image2 = tempDir.resolve("image2.jpg");
        Files.createFile(image1);
        Files.createFile(image2);
        
        manager.addAssociation("test", new String[]{
            image1.toString(),
            image2.toString()
        });
        
        assertTrue(manager.verifyImageFiles());
    }
    
    @Test
    void testVerifyImageFiles_SomeMissing(@TempDir Path tempDir) throws IOException {
        Path image1 = tempDir.resolve("image1.jpg");
        Files.createFile(image1);
        
        manager.addAssociation("test", new String[]{
            image1.toString(),
            tempDir.resolve("missing.jpg").toString()
        });
        
        assertFalse(manager.verifyImageFiles());
    }
    
    @Test
    void testVerifyImageFiles_EmptyAssociations() {
        assertTrue(manager.verifyImageFiles());
    }
    
    @Test
    void testGetMissingImageFiles(@TempDir Path tempDir) throws IOException {
        Path image1 = tempDir.resolve("image1.jpg");
        Files.createFile(image1);
        
        String missingPath = tempDir.resolve("missing.jpg").toString();
        
        manager.addAssociation("test", new String[]{
            image1.toString(),
            missingPath
        });
        
        List<String> missing = manager.getMissingImageFiles();
        assertEquals(1, missing.size());
        assertEquals(missingPath, missing.get(0));
    }
    
    @Test
    void testGetWordCount() {
        assertEquals(0, manager.getWordCount());
        
        manager.addAssociation("hello", new String[]{"hello.jpg"});
        assertEquals(1, manager.getWordCount());
        
        manager.addAssociation("world", new String[]{"world.jpg"});
        assertEquals(2, manager.getWordCount());
    }
    
    @Test
    void testGetImageCount() {
        assertEquals(0, manager.getImageCount());
        
        manager.addAssociation("hello", new String[]{"hello1.jpg", "hello2.jpg"});
        assertEquals(2, manager.getImageCount());
        
        manager.addAssociation("world", new String[]{"world1.jpg"});
        assertEquals(3, manager.getImageCount());
    }
    
    @Test
    void testGetAllWords() {
        manager.addAssociation("hello", new String[]{"hello.jpg"});
        manager.addAssociation("world", new String[]{"world.jpg"});
        
        Set<String> words = manager.getAllWords();
        assertEquals(2, words.size());
        assertTrue(words.contains("hello"));
        assertTrue(words.contains("world"));
    }
    
    @Test
    void testHasWord_Exists() {
        manager.addAssociation("hello", new String[]{"hello.jpg"});
        assertTrue(manager.hasWord("hello"));
        assertTrue(manager.hasWord("HELLO")); // Case insensitive
    }
    
    @Test
    void testHasWord_DoesNotExist() {
        assertFalse(manager.hasWord("nonexistent"));
    }
    
    @Test
    void testHasWord_NullWord() {
        assertFalse(manager.hasWord(null));
    }
    
    @Test
    void testHasWord_EmptyWord() {
        assertFalse(manager.hasWord(""));
    }
    
    @Test
    void testClear() {
        manager.addAssociation("hello", new String[]{"hello.jpg"});
        manager.addAssociation("world", new String[]{"world.jpg"});
        
        assertEquals(2, manager.getWordCount());
        
        manager.clear();
        
        assertEquals(0, manager.getWordCount());
        assertEquals(0, manager.getImageCount());
    }
}
