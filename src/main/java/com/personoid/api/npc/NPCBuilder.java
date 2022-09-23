package com.personoid.api.npc;

import com.personoid.api.npc.injection.*;
import com.personoid.api.utils.CacheManager;
import com.personoid.api.utils.packet.ReflectionUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Random;

public class NPCBuilder {
    private static final CacheManager CACHE = new CacheManager("npc_builder");

    static {
        CACHE.put("entity_player", ReflectionUtils.getNMSClass(null, "level", "EntityPlayer", 17));
    }

    public static NPC create(GameProfile profile) {
        NPC npc = new NPC(profile);
        npc.addFeature(new Feature() {
            @Hook("tick")
            public void tick() {
                // tick injection
            }

            @Hook("damage")
            public void damageWithRandom(float damage, CallbackInfo ci) {
                if (damage > 10) {
                    ci.setReturnValue(new Random().nextDouble() * 10);
                }
            }
        });
        try {
            Object base = new ByteBuddy()
                    .subclass(CACHE.getClass("entity_player"))
                    .method(ElementMatchers.named("tick"))
                    .intercept(MethodCall.invoke(npc.getFields().getClass().getMethod("tick")))
                    .make()
                    .load(NPCBuilder.class.getClassLoader())
                    .getLoaded().newInstance();
            npc.getFields().setBase(base);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return npc;
    }
}
