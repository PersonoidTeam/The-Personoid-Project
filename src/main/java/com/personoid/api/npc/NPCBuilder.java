package com.personoid.api.npc;

import com.personoid.api.utils.cache.Cache;
import com.personoid.nms.mappings.NMSClass;
import com.personoid.nms.packet.Package;
import com.personoid.nms.packet.Packages;
import com.personoid.nms.packet.NMSReflection;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NPCBuilder {
    private static final Cache CACHE = new Cache("npc_builder");
    private static DynamicType.Builder<?> builder;

    private static final String KEY_SERVER_PLAYER = "server_player";
    private static final String KEY_MINECRAFT_SERVER = "minecraft_server";
    private static final String KEY_WORLD_SERVER = "world_server";
    private static final String KEY_GAME_PROFILE = "game_profile";
    private static final String KEY_PROFILE_PUBLIC_KEY = "profile_public_key";

    static {
        CACHE.put(KEY_SERVER_PLAYER, Package.SERVER_PLAYER_CLASS.getMappedClass());
        CACHE.put(KEY_MINECRAFT_SERVER, NMSReflection.findClass(Packages.SERVER, "MinecraftServer"));
        CACHE.put(KEY_WORLD_SERVER, NMSReflection.findClass(Packages.SERVER_LEVEL, "WorldServer"));
        CACHE.put(KEY_GAME_PROFILE, NMSReflection.findClass(Packages.AUTH_LIB, "GameProfile"));
        CACHE.put(KEY_PROFILE_PUBLIC_KEY, NMSReflection.findClass(Packages.WORLD.plus("entity.player"), "ProfilePublicKey"));
    }

    public static NPC create(GameProfile profile) {
        NPC npc = new NPC(profile);
        if (builder == null) {
            builder = new ByteBuddy().subclass(
                    ((NMSClass) CACHE.get(KEY_SERVER_PLAYER)).getRawClass(),
                    ConstructorStrategy.Default.IMITATE_SUPER_CLASS_PUBLIC
            );
        }
        try {
            String methodNameTick = NPCOverrides.METHOD_TICK;
            Method tickMethod = NPCOverrides.class.getMethod("tick");

            builder = builder.method(ElementMatchers.isMethod()
                    .and(ElementMatchers.named(methodNameTick))
                    .and(ElementMatchers.returns(TypeDescription.VOID))
                    .and(ElementMatchers.takesNoArguments())
            ).intercept(MethodCall.invokeSuper().andThen(MethodCall.invoke(tickMethod).on(npc.getOverrides())));

            Class<?> loaded = builder.make().load(NPCBuilder.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
            Object gameProfile = NMSReflection.construct(CACHE.getClass(KEY_GAME_PROFILE), profile.getId(), profile.getName());
            Object base;
            if (NMSReflection.getVersionInt() == 19 && NMSReflection.getSubVersionInt() >= 0 && NMSReflection.getSubVersionInt() <= 2) {
                base = loaded.getConstructor(
                                CACHE.getClass(KEY_MINECRAFT_SERVER),
                                CACHE.getClass(KEY_WORLD_SERVER),
                                CACHE.getClass(KEY_GAME_PROFILE),
                                CACHE.getClass(KEY_PROFILE_PUBLIC_KEY))
                        .newInstance(
                                NMSReflection.invoke(Bukkit.getServer(), "getServer"),
                                NMSReflection.getHandle(Bukkit.getWorlds().get(0)),
                                gameProfile, null
                        );
            } else {
                base = loaded.getConstructor(
                                CACHE.getClass(KEY_MINECRAFT_SERVER),
                                CACHE.getClass(KEY_WORLD_SERVER),
                                CACHE.getClass(KEY_GAME_PROFILE))
                        .newInstance(
                                NMSReflection.invoke(Bukkit.getServer(), "getServer"),
                                NMSReflection.getHandle(Bukkit.getWorlds().get(0)),
                                gameProfile
                        );
            }
            npc.getOverrides().setBase(base);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return npc;
    }
}
