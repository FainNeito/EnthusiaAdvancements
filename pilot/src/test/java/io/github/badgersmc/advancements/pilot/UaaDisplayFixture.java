package io.github.badgersmc.advancements.pilot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Server;

/** Only the NMS frame adapter is stubbed; tests construct real UAA displays. */
final class UaaDisplayFixture {

    private static boolean initialized;

    private UaaDisplayFixture() {}

    static synchronized void initialize() {
        if (initialized) return;
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getMinecraftVersion).thenReturn("26.2");
            bukkit.when(Bukkit::getServer).thenReturn(mock(Server.class));
            try (var reflection = mockStatic(ReflectionUtil.class)) {
                reflection
                    .when(() ->
                        ReflectionUtil.getWrapperClass(
                            AdvancementFrameTypeWrapper.class
                        )
                    )
                    .thenReturn(TestFrameWrapper.class);
                AdvancementFrameType.values();
            }
        }
        initialized = true;
    }

    public static final class TestFrameWrapper
        extends AdvancementFrameTypeWrapper
    {

        private final FrameType type;

        public TestFrameWrapper(FrameType type) {
            this.type = type;
        }

        @Override
        public FrameType getFrameType() {
            return type;
        }

        @Override
        public Object toNMS() {
            throw new UnsupportedOperationException(
                "NMS is not exercised by unit tests"
            );
        }
    }
}
