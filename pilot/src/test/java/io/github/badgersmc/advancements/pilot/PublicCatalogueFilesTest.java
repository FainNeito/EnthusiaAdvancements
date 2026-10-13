package io.github.badgersmc.advancements.pilot;
import java.nio.file.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class PublicCatalogueFilesTest {
    @TempDir Path directory;
    @Test void outputIsCompleteJsonAndNoTemporaryFilesRemain() throws Exception {
        Path output=PublicCatalogueFiles.write(directory,Map.of("title","Quoted \"title\""));
        assertEquals("Quoted \"title\"",com.google.gson.JsonParser.parseString(Files.readString(output)).getAsJsonObject().get("title").getAsString());
        try(var files=Files.list(directory)){assertEquals(1,files.count());}
    }
}
