package io.github.badgersmc.advancements.pilot;
import com.fren_gor.ultimateAdvancementAPI.*;
import com.fren_gor.ultimateAdvancementAPI.advancement.*;
import java.lang.reflect.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProjectionBoundaryTest {
    record Fixture(PilotPlugin service, Plugin owner, Player player, RootAdvancement root, BaseAdvancement node, AdvancementTab tab) {}
    private Fixture fixture() throws Exception {
        PilotPlugin service = mock(PilotPlugin.class, CALLS_REAL_METHODS);
        Plugin owner = mock(Plugin.class); Player player = mock(Player.class);
        RootAdvancement root = mock(RootAdvancement.class); BaseAdvancement node = mock(BaseAdvancement.class);
        AdvancementTab tab = mock(AdvancementTab.class);
        UltimateAdvancementAPI api = mock(UltimateAdvancementAPI.class, RETURNS_DEEP_STUBS);
        when(player.isOnline()).thenReturn(true); when(api.isLoaded(player)).thenReturn(true);
        when(api.getTeamProgression(player).getSize()).thenReturn(1);
        Class<?> type=Class.forName(PilotPlugin.class.getName()+"$Tree");
        var constructor=type.getDeclaredConstructors()[0]; constructor.setAccessible(true);
        Object tree=constructor.newInstance(owner,tab,root,Map.of("node",node));
        var trees=PilotPlugin.class.getDeclaredField("trees"); trees.setAccessible(true); trees.set(service,new LinkedHashMap<>(Map.of("test",tree)));
        var apiField=PilotPlugin.class.getDeclaredField("api"); apiField.setAccessible(true); apiField.set(service,api);
        return new Fixture(service,owner,player,root,node,tab);
    }
    private void project(Fixture f, Plugin owner, Map<String,Integer> progress) throws Exception {
        try { ProjectionService.class.getMethod("project",Plugin.class,String.class,Player.class,Map.class).invoke(f.service(),owner,"test",f.player(),progress); }
        catch(InvocationTargetException e) { if(e.getCause() instanceof RuntimeException failure) throw failure; throw e; }
    }
    @Test void nullEntryRejectsEntireMapBeforeAnyDisplayMutation() throws Exception {
        var f=fixture(); var progress=new LinkedHashMap<String,Integer>(); progress.put("node",500); progress.put("bad",null);
        try(var bukkit=mockStatic(Bukkit.class)) { bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            assertThrows(IllegalArgumentException.class,()->project(f,f.owner(),progress));
            verifyNoInteractions(f.root(),f.node(),f.tab());
        }
    }
    @Test void otherProviderCannotProjectOrCelebrate() throws Exception {
        var f=fixture(); Plugin other=mock(Plugin.class);
        try(var bukkit=mockStatic(Bukkit.class)) { bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            assertThrows(IllegalArgumentException.class,()->project(f,other,Map.of("node",1000)));
            var method=ProjectionService.class.getMethod("celebrate",Plugin.class,String.class,Player.class,String.class);
            var failure=assertThrows(InvocationTargetException.class,()->method.invoke(f.service(),other,"test",f.player(),"node"));
            assertInstanceOf(IllegalArgumentException.class,failure.getCause()); verifyNoInteractions(f.root(),f.node(),f.tab());
        }
    }
    @Test void validOwnerRetainsUnavailableSentinelAndProjectsValidValues() throws Exception {
        var f=fixture();
        try(var bukkit=mockStatic(Bukkit.class)) { bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            project(f,f.owner(),Map.of("node",-1)); verify(f.node(),never()).setProgression(any(Player.class),anyInt(),anyBoolean());
            project(f,f.owner(),Map.of("node",999)); verify(f.node()).setProgression(f.player(),99,false);
        }
    }
    @Test void ownerlessCompatibilityMethodsFailClosed() throws Exception {
        var f=fixture();
        assertThrows(UnsupportedOperationException.class,()->f.service().project("test",f.player(),Map.of()));
        assertThrows(UnsupportedOperationException.class,()->f.service().celebrate("test",f.player(),"node"));
    }
}
