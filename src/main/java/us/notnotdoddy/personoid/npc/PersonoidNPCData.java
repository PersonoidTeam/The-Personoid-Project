package us.notnotdoddy.personoid.npc;

import net.citizensnpcs.api.ai.flocking.Flocker;
import net.citizensnpcs.api.ai.flocking.RadiusNPCFlock;
import net.citizensnpcs.api.ai.flocking.SeparationBehavior;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import us.notnotdoddy.personoid.goals.PersonoidGoal;
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceManager;
import us.notnotdoddy.personoid.player.NPCTarget;
import us.notnotdoddy.personoid.status.Behavior;
import us.notnotdoddy.personoid.player.PlayerInfo;
import us.notnotdoddy.personoid.status.RemovalReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PersonoidNPCData {
    private final PersonoidNPC npc;
    public final HashMap<UUID, PlayerInfo> players = new HashMap<>();
    public final PersonoidNPCInventory inventory;
    public ResourceManager resourceManager;
    public final Flocker flocker;

    public final Behavior behavior = new Behavior(Behavior.Type.BUILDER);
    public final List<PersonoidGoal> goals = new ArrayList<>();
    public TargetHandler.TargetType targetType = TargetHandler.TargetType.NOTHING;

    public boolean paused;
    public boolean hibernating;
    public NPCTarget target;
    public UUID closestPlayer;
    public PersonoidGoal currentGoal;
    public Location spawnPoint;
    public Location targetLocation;
    public boolean stuck;
    public RemovalReason removalReason;
    public UUID lastDamager;
    public int hibernationTicks;
    public boolean playerLoaded;
    public boolean updatedLocationThisTick;
    public Location lastLocation;
    public Location originalLastLocation;
    public int cooldownTicks;

    public PersonoidNPCData(PersonoidNPC npc) {
        inventory = new PersonoidNPCInventory(npc);
        flocker = new Flocker(npc.citizen, new RadiusNPCFlock(4.0D, 0), new SeparationBehavior(1.0D));
        this.npc = npc;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!players.containsKey(player.getUniqueId())) {
                players.put(player.getUniqueId(), new PlayerInfo());
            }
        }
    }

    public void makeResourceManager(){
        resourceManager = new ResourceManager(npc);
    }

    public Player getClosestPlayer() {
        return Bukkit.getPlayer(closestPlayer);
    }

    public Player getLastDamager() {
        return Bukkit.getPlayer(lastDamager);
    }
}
