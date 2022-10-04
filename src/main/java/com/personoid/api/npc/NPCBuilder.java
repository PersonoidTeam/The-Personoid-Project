package com.personoid.api.npc;

import com.personoid.api.npc.injection.CallbackInfo;
import com.personoid.api.npc.injection.Feature;
import com.personoid.api.npc.injection.Hook;
import com.personoid.api.utils.CacheManager;
import com.personoid.api.utils.packet.Packages;
import com.personoid.api.utils.packet.ReflectionUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

public class NPCBuilder {
    private static final CacheManager CACHE = new CacheManager("npc_builder");

    static {
        CACHE.put("entity_player", ReflectionUtils.getClass(Packages.LEVEL, "EntityPlayer"));
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
            DynamicType.Builder<?> builder = new ByteBuddy()
                    .subclass(CACHE.getClass("entity_player"))
                    .method(ElementMatchers.named("tick"))
                    .intercept(MethodCall.invoke(npc.getOverrides().get("tick")));
            for (String method : npc.getOverrides().getMethods()) {
                builder = builder.method(ElementMatchers.named(method))
                        .intercept(MethodCall.invoke(npc.getOverrides().get(method)));
            }
            Object base = builder.make()
                    .load(NPCBuilder.class.getClassLoader())
                    .getLoaded().newInstance();
            npc.getOverrides().setBase(base);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return npc;
    }
}
