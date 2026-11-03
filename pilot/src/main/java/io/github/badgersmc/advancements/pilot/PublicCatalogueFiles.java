package io.github.badgersmc.advancements.pilot;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
/** Local restore file output only. No provider or player operations. */
final class PublicCatalogueFiles {
    static Path write(Path directory, Map<String,Object> snapshot) throws IOException {
        Files.createDirectories(directory);
        Path output=directory.resolve("registered-achievement-catalogue.json");
        Path temporary=Files.createTempFile(directory,"catalogue-",".tmp");
        try {
            Files.writeString(temporary,new com.google.gson.Gson().toJson(snapshot));
            Files.move(temporary,output,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        } finally {Files.deleteIfExists(temporary);}
        return output;
    }
}
