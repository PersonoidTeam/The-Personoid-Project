package us.notnotdoddy.personoid;

import me.definedoddy.fluidapi.FluidPlugin;
import me.definedoddy.fluidapi.FluidUtils;
import me.definedoddy.fluidapi.tasks.RepeatingTask;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.apache.commons.io.FileUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NPCHandler {
    public static final NPCRegistry registry = CitizensAPI.getNPCRegistry();
    private static final Map<net.citizensnpcs.api.npc.NPC, NPC> NPCs = new HashMap<>();

    public static Map<net.citizensnpcs.api.npc.NPC, NPC> getNPCs() {
        return NPCs;
    }

    public static NPC create(String name, Location loc) {
        return new NPC(name).spawn(loc);
    }

    public static String getRandomName() {
        InputStream stream = FluidPlugin.getPlugin().getClass().getResourceAsStream("/names.txt");
        File file = new File("names.txt");
        try {
            FileUtils.copyInputStreamToFile(stream, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FluidUtils.randomLineFromFile(file);
    }

    //replace this if you want - this was just for testing purposes
    public static void startNPCTicking() {
        new RepeatingTask(0, 5) {
            @Override
            public void run() {
                for (NPC npc : NPCs.values()) {
                    if (npc.entity.isSpawned() && !npc.paused) {
                        Player closest = getClosestPlayer(npc.entity.getStoredLocation());
                        if (!npc.players.containsKey(closest)) {
                            npc.players.put(closest, new PlayerInfo(closest));
                        }
                        if (npc.players.get(closest).mood == Behavior.Mood.ANGRY) {
                            npc.entity.getNavigator().setTarget(closest, true);
                        } else {
                            Location loc = npc.entity.getStoredLocation();
                            double x = loc.getX() + FluidUtils.random(-5, 5);
                            double z = loc.getZ() + FluidUtils.random(-5, 5);
                            Location to = new Location(npc.entity.getStoredLocation().getWorld(), x, loc.getY(), z);
                            npc.entity.getNavigator().setTarget(to);
                        }
                    }
                }
            }
        };
    }

    public static Player getClosestPlayer(Location loc) {
        Player closestPlayer = null;
        double closestDistance = 0;
        for (Player player : loc.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(loc);
            if (closestPlayer == null || distance < closestDistance && player.getGameMode() != GameMode.SPECTATOR) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }
        return closestPlayer;
    }
    //
}
