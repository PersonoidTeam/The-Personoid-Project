package us.notnotdoddy.personoid.npc;

import net.citizensnpcs.api.ai.flocking.Flocker;
import net.citizensnpcs.api.ai.flocking.RadiusNPCFlock;
import net.citizensnpcs.api.ai.flocking.SeparationBehavior;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import us.notnotdoddy.personoid.goals.NPCGoal;
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceManager;
import us.notnotdoddy.personoid.status.Behavior;
import us.notnotdoddy.personoid.player.PlayerInfo;
import us.notnotdoddy.personoid.status.RemovalReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NPCData {
    private final PersonoidNPC npc;
    public final HashMap<UUID, PlayerInfo> players = new HashMap<>();
    public final NPCInventory inventory;
    public ResourceManager resourceManager;
    public final Flocker flocker;

    public final Behavior behavior = new Behavior(Behavior.Type.BUILDER);
    public final List<NPCGoal> goals = new ArrayList<>();
    public NPCTarget target;

    public boolean paused;
    public boolean hibernating;
    public UUID closestPlayer;
    public NPCGoal currentGoal;
    public Location spawnPoint;
    public boolean stuck;
    public RemovalReason removalReason;
    public UUID lastDamager;
    public int hibernationTicks;
    public boolean playerLoaded;
    public boolean updatedLocationThisTick;
    public Location lastLocation;
    public Location originalLastLocation;
    public int cooldownTicks;

    public NPCData(PersonoidNPC npc) {
        this.npc = npc;
        inventory = new NPCInventory(npc);
        resourceManager = new ResourceManager(npc);
        flocker = new Flocker(npc.citizen, new RadiusNPCFlock(4.0D, 0), new SeparationBehavior(1.0D));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!players.containsKey(player.getUniqueId())) {
                players.put(player.getUniqueId(), new PlayerInfo());
            }
        }
    }
}
