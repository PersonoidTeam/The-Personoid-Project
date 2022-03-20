package com.personoid.commands;

import com.personoid.handlers.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestLocationCommand extends CommandHandler.Command {
    private Location firstLoc;

    public TestLocationCommand() {
        super("npc", "testloc", CommonRequirements.player);
    }

    @Override
    public boolean onCommand(@NotNull Player sender, String[] args) {
        if (firstLoc == null) {
            firstLoc = sender.getLocation();
            Bukkit.broadcastMessage("Set first location to " + firstLoc.getBlockX() + ", " + firstLoc.getBlockY() + ", " + firstLoc.getBlockZ());
        } else {
            boolean valid = valid(firstLoc, sender.getLocation());
            Bukkit.broadcastMessage("Valid: " + valid);
            firstLoc = null;
        }
        return true;
    }

    public boolean valid(Location from, Location to) {
        Block fromBlock = from.getBlock();
        Block toBlock = to.getBlock();
        boolean validHeight = toBlock.getType().isAir() && toBlock.getRelative(BlockFace.UP).getType().isAir(); // checks if is player height
        boolean validGround = toBlock.getRelative(BlockFace.DOWN).getType().isSolid(); // is there a block underneath that they can stand on?
        boolean validFromPrev = toBlock.getLocation().subtract(fromBlock.getLocation()).getY() <= 1; // is it max one block higher than the last one?

        // is this one causing issues?
        Location fromLocDist = from.clone();
        Location toLocDist = to.clone();
        toLocDist.setY(fromLocDist.getY());
        boolean validDistance = fromLocDist.distance(toLocDist) <= 1;

        return validHeight && validGround && validFromPrev;
    }
}
