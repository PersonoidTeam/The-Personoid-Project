package com.personoid.api.npc;

import com.personoid.nms.packet.Packets;
import org.apache.commons.lang.Validate;

import java.util.UUID;

public class GameProfile {
    private NPC npc;
    private final UUID id;
    private String name;
    private Skin skin;

    private boolean visibleInTab;

    public GameProfile(UUID id, String name, Skin skin) {
        Validate.notNull(id, "id cannot be null");
        this.id = id;
        this.name = name;
        this.skin = skin;
        visibleInTab = true;
    }

    public GameProfile(UUID id, String name) {
        Validate.notNull(id, "id cannot be null");
        this.id = id;
        this.name = name;
        this.skin = Skin.randomDefault();
        visibleInTab = true;
    }

    // region GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
/*        npc.getEntity().setDisplayName(name);
        ((CraftPlayer)npc.getEntity()).getHandle().displayName = name;
        ((CraftPlayer)npc.getEntity()).getHandle().listName = Component.literal(name);
        Packets.removePlayer(npc.getEntity()).send();
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(GameProfile.class), () -> {
            Packets.addPlayer(npc.getEntity(), true).send();
        }, 1);*/
    }

    public Skin getSkin() {
        return skin;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
        npc.getOverrides().updateSkin();
    }

    public UUID getId() {
        return id;
    }

    public void setTabVisibility(boolean visible) {
        if (!npc.isSpawned()) return;
        Packets.showPlayer(npc.getEntity(), visible).send();
        this.visibleInTab = visible;
    }

    public boolean isVisibleInTab() {
        return visibleInTab;
    }

    void setNPC(NPC npc) {
        this.npc = npc;
    }

    // endregion
}
