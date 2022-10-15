package com.personoid.api.npc;

import com.personoid.api.npc.injection.CallbackInfo;
import com.personoid.api.npc.injection.Feature;
import com.personoid.api.npc.injection.Hook;
import com.personoid.api.utils.CacheManager;
import com.personoid.api.utils.packet.Packages;
import com.personoid.api.utils.packet.ReflectionUtils;
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
import java.util.UUID;

public class NPCBuilder {
    private static final CacheManager CACHE = new CacheManager("npc_builder");

    static {
        CACHE.put("entity_player", ReflectionUtils.findClass(Packages.SERVER_LEVEL, "EntityPlayer"));
        CACHE.put("minecraft_server", ReflectionUtils.findClass(Packages.SERVER, "MinecraftServer"));
        CACHE.put("world_server", ReflectionUtils.findClass(Packages.SERVER_LEVEL, "WorldServer"));
        CACHE.put("game_profile", ReflectionUtils.findClass(Packages.AUTH_LIB, "GameProfile"));
        CACHE.put("profile_public_key", ReflectionUtils.findClass(Packages.WORLD.plus("entity.player"), "ProfilePublicKey"));
    }

    public static NPC create(GameProfile profile) {
        NPC npc = new NPC(profile);
        // add feature example
        npc.addFeature(new Feature() {
            @Hook("tick")
            public void tick() {
                // tick injection
            }

            @Hook("damage")
            public void constantDamage(float damage, CallbackInfo ci) {
                if (damage > 10) {
                    ci.setReturnValue(2);
                }
            }
        });
        try {
/*            new AgentBuilder.Default()
                    .disableClassFormatChanges()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .ignore(ElementMatchers.none())
                    .type(ElementMatchers.is(CACHE.getClass("entity_player")))
                    .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
                        for (String method : npc.getOverrides().getMethods()) {
                            builder.visit(Advice.to(NPCOverrides.class).on(ElementMatchers.named(method)));
                        }
                        return builder;
                    }).installOnByteBuddyAgent();*/
            DynamicType.Builder<?> builder = new ByteBuddy().subclass(CACHE.getClass("entity_player"),
                    ConstructorStrategy.Default.IMITATE_SUPER_CLASS_PUBLIC);
/*            if (ReflectionUtils.getVersionInt() >= 19) {
                builder = builder.defineConstructor(Visibility.PUBLIC).withParameters(
                        CACHE.getClass("dedicated_server"),
                        CACHE.getClass("world_server"),
                        CACHE.getClass("game_profile"),
                        CACHE.getClass("profile_public_key")
                ).intercept(MethodCall.invoke(CACHE.getClass("entity_player").getConstructor(
                        CACHE.getClass("dedicated_server"),
                        CACHE.getClass("world_server"),
                        CACHE.getClass("game_profile"),
                        CACHE.getClass("profile_public_key")
                )).onSuper().withAllArguments());
            } else {
                builder = builder.defineConstructor(Visibility.PUBLIC).withParameters(
                        CACHE.getClass("dedicated_server"),
                        CACHE.getClass("world_server"),
                        CACHE.getClass("game_profile")
                ).intercept(MethodDelegation.toConstructor(CACHE.getClass("entity_player")));
            }*/
            for (String method : npc.getOverrides().getMethods()) {
                builder = builder.method(ElementMatchers.isMethod()
                                .and(ElementMatchers.named(method))
                                .and(ElementMatchers.returns(TypeDescription.VOID))
                        .and(ElementMatchers.takesNoArguments())
                ).intercept(MethodCall.invokeSuper().andThen(MethodCall.invoke(NPCOverrides.class.getMethod(method)).on(npc.getOverrides())));
            }
            Class<?> loaded = builder.make().load(NPCBuilder.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
            Object base;
            if (ReflectionUtils.getVersionInt() >= 19) {
                base = loaded.getConstructor(
                        CACHE.getClass("minecraft_server"),
                        CACHE.getClass("world_server"),
                        CACHE.getClass("game_profile"),
                        CACHE.getClass("profile_public_key"))
                        .newInstance(
                                ReflectionUtils.invoke(Bukkit.getServer(), "getServer"),
                                ReflectionUtils.invoke(Bukkit.getWorlds().get(0), "getHandle"),
                                new com.mojang.authlib.GameProfile(UUID.randomUUID(), profile.getName()), null
                        );
            } else {
                base = loaded.getConstructor(
                                CACHE.getClass("minecraft_server"),
                                CACHE.getClass("world_server"),
                                CACHE.getClass("game_profile"))
                        .newInstance(
                                ReflectionUtils.invoke(Bukkit.getServer(), "getServer"),
                                ReflectionUtils.invoke(Bukkit.getWorlds().get(0), "getHandle"),
                                new com.mojang.authlib.GameProfile(UUID.randomUUID(), profile.getName())
                        );
            }
            for (Method method : base.getClass().getDeclaredMethods()) {
                Bukkit.broadcastMessage(method.getName());
            }
            npc.getOverrides().setBase(base);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return npc;
    }
}
