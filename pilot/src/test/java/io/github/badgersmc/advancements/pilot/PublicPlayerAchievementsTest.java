package io.github.badgersmc.advancements.pilot;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PublicPlayerAchievementsTest {
    @Test void requestedKnownIndividualsExportAndEmptyIsExplicit() {
        UUID known=UUID.randomUUID(), unknown=UUID.randomUUID();
        var result=PublicPlayerAchievements.build(List.of(known,unknown), id -> id.equals(known)
            ?new PublicPlayerAchievements.Completion("Known",List.of()):null, Instant.EPOCH);
        var rows=(List<?>)result.get("items");assertEquals(1,rows.size());
        var row=(Map<?,?>)rows.getFirst();assertEquals(known.toString(),row.get("id"));
        assertEquals(true,row.get("achievementsKnown"));assertEquals(List.of(),row.get("achievements"));
        assertEquals(Set.of("id","name","achievementsKnown","achievements"),row.keySet());
    }
    @Test void malformedRequestsFailBeforeLookup() {
        UUID id=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,()->PublicPlayerAchievements.build(List.of(id,id), value->{fail();return null;},Instant.EPOCH));
        assertThrows(IllegalArgumentException.class,()->PublicPlayerAchievements.build(List.of(),value->{fail();return null;},Instant.EPOCH));
    }
}
