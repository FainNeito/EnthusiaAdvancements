package net.badgersmc.ek.api
import java.util.UUID
/** Compile-only mirror of the read-only eKOTH provider, excluded from shadowJar. */
interface KothProgressionV1 { fun progress(player:UUID):Map<String,Int>? }
