package us.notnotdoddy.personoid.npc.resourceGathering.actions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import us.notnotdoddy.personoid.npc.NPCTarget;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceTypes;
import us.notnotdoddy.personoid.utils.DebugMessage;
import us.notnotdoddy.personoid.utils.LocationUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GatherAction {

    Random random = new Random();

    public boolean isCompleted = false;
    private final ResourceTypes gatherType;
    private final PersonoidNPC npc;
    private Block targetBlock = null;
    private Material materialWhenStarted = null;
    private Material pickedUpMaterial = null;
    private int targetAmount;
    public boolean wasBreaking = false;
    private int currentAmount = 0;
    private int failSafeTicks = 0;
    private int successChecks = 0;
    private Block furnaceBlock = null;
    private int probableCoalCount = 0;
    private int currentCoalCount = 0;
    private boolean wasLooking = false;

    private int itemsTakenFromFurnace = 0;

    private boolean hasTransferedToFurnace = false;

    public GatherAction(ResourceTypes gatherType, PersonoidNPC npc, int targetAmount) {
        this.gatherType = gatherType;
        this.npc = npc;
        this.targetAmount = targetAmount;
        if (gatherType.shouldBeSmelted){
            probableCoalCount = 4;
        }
        DebugMessage.attemptMessage("resource", "Made gather action: "+gatherType.toString());
    }

    public void init(){
        lookForNewBlock();
    }

    private boolean shouldGetThatCoal(Material block){
        return gatherType.shouldBeSmelted && ResourceTypes.COAL.contains(block);
    }

    private void lookForNewBlock(){
        boolean foundBlock = false;
        for (Block block : getBlocksInSphere(npc.getEntity().getLocation(), 10)){
            if (gatherType.contains(block.getType()) || shouldGetThatCoal(block.getType())){
                targetBlock = block;
                materialWhenStarted = targetBlock.getType();
                if (wasLooking){
                    npc.forgetTarget();
                    wasLooking = false;
                }
                npc.target(new NPCTarget(targetBlock.getLocation().getBlock(), NPCTarget.BlockTargetType.BREAK));
                foundBlock = true;
                DebugMessage.attemptMessage("resource", "Found block");
            }
        }
        if (!foundBlock){
            wasLooking = true;
            DebugMessage.attemptMessage("resource", "Wandering because didnt find block");
            failSafeTicks++;
            if (!npc.hasTarget()){
                Location location = LocationUtilities.getRandomLoc(npc);
                npc.target(new NPCTarget(LocationUtilities.getNearestValidLocation(location)));
            }
            if (failSafeTicks == 40 || npc.getLocationTarget().distance(npc.getEntity().getLocation()) < 3){
                npc.target(new NPCTarget(LocationUtilities.getRandomLoc(npc)));
                failSafeTicks = 0;
            }
        }
    }

    public void tick(){
        if (targetAmount <= currentAmount && currentCoalCount >= probableCoalCount){
            DebugMessage.attemptMessage("resource", "DONE!");
            if (!gatherType.shouldBeSmelted){
                isCompleted = true;
            }
            else {
                if (hasPlacedFurnace() && furnaceBlock.getType() == Material.FURNACE){
                    Furnace furnace = (Furnace) furnaceBlock.getState();
                    if (npc.getEntity().getLocation().distance(furnaceBlock.getLocation()) >= 6){
                        npc.target(new NPCTarget(furnaceBlock.getRelative(BlockFace.UP)));
                    }
                    if (!hasTransferedToFurnace){
                        hasTransferedToFurnace = true;
                        furnace.getInventory().setFuel(new ItemStack(Material.COAL, probableCoalCount));
                        npc.data.inventory.removeMaterialCount(Material.COAL, probableCoalCount);

                        furnace.getInventory().setSmelting(new ItemStack(pickedUpMaterial, targetAmount));
                        npc.data.inventory.removeMaterialCount(pickedUpMaterial, targetAmount);
                    }
                    if (furnace.getInventory().getResult() != null){
                        if (furnace.getInventory().getResult().getAmount() >= targetAmount){
                            if (furnace.getInventory().getFuel() != null){
                                npc.data.inventory.addItem(furnace.getInventory().getFuel().clone());
                                furnace.getInventory().setFuel(null);
                            }
                            npc.data.inventory.addItem(furnace.getInventory().getResult().clone());
                            furnace.getInventory().setResult(null);
                            npc.resume();

                            isCompleted = true;
                        }
                    }
                }
                else {
                    DebugMessage.attemptMessage("resource", "Placed furnace");
                    npc.setItemInMainHand(new ItemStack(Material.FURNACE));
                    Location location = npc.getEntity().getLocation().clone().getBlock().getRelative(npc.getPlayer().getFacing()).getLocation();
                    location.subtract(0,1,0);
                    furnaceBlock = location.getBlock();
                    furnaceBlock.setType(Material.FURNACE);
                    Directional dir = ((Directional) furnaceBlock.getBlockData());
                    dir.setFacing(npc.getPlayer().getFacing().getOppositeFace());
                    furnaceBlock.setBlockData(dir);
                }
            }
        }
        else {
            if (!npc.isHibernating()){
                if (targetBlock != null){
                    if (!targetBlock.getType().equals(materialWhenStarted)){
                        DebugMessage.attemptMessage("resource", "Target block is no longer correct.");
                        if (targetBlock != null){
                            npc.forgetTarget();
                            targetBlock = null;
                            materialWhenStarted = null;
                            if (targetBlock.getRelative(BlockFace.UP).getType().equals(Material.BARRIER)){
                                targetBlock.getRelative(BlockFace.UP).setType(Material.AIR);
                            }
                        }
                        if (wasBreaking){
                            DebugMessage.attemptMessage("resource", "was breaking!");
                            for (Entity entity : npc.getEntity().getNearbyEntities(4, 4, 4)){
                                if (entity instanceof Item item){
                                    ItemStack itemStack = item.getItemStack();
                                    DebugMessage.attemptMessage("resource", "itemstack");
                                    if (gatherType.contains(itemStack.getType()) || shouldGetThatCoal(itemStack.getType())){
                                        npc.data.inventory.addItem(itemStack);
                                        item.remove();
                                        if (gatherType.contains(itemStack.getType())){
                                            if (pickedUpMaterial == null){
                                                pickedUpMaterial = itemStack.getType();
                                            }
                                            currentAmount++;
                                        }
                                        else {
                                            if (itemStack.getType().equals(Material.COAL)){
                                                currentCoalCount += itemStack.getAmount();
                                            }
                                        }
                                        DebugMessage.attemptMessage("resource", currentAmount+" blocks collected");
                                        DebugMessage.attemptMessage("resource", "Target amount: "+targetAmount);
                                        DebugMessage.attemptMessage("resource", currentCoalCount+" coal collected");
                                        DebugMessage.attemptMessage("resource", "Target coal: "+probableCoalCount);

                                    }
                                }
                            }
                            wasBreaking = false;
                        }
                        npc.resume();
                        lookForNewBlock();
                    }
                }
                else {
                    lookForNewBlock();
                }
            }
            else {
                DebugMessage.attemptMessage("resource", "Im hibernating");
                // Haha, now we can do the lame stuff. Funny how this is literally all code to DISPLAY them getting the resources in the case that someone is watching
                // Which is probably not even going to be that common.
                if (random.nextDouble() <= npc.data.behavior.type().resourceGatheringSkill){
                    successChecks++;
                }
                if (successChecks == 100){
                    for (Material material : Material.values()){
                        if (gatherType.contains(material)){
                            npc.data.inventory.addItem(new ItemStack(material));
                            break;
                        }
                    }
                    currentAmount++;
                    successChecks = 0;
                }
            }
        }
    }

    private Material getFirstMatchedMaterial(){
        for (Material material : Material.values()){
            if (gatherType.contains(material)){
                return material;
            }
        }
        return null;
    }

    private boolean hasPlacedFurnace(){
        return furnaceBlock != null;
    }

    private List<Block> getBlocksInSphere(Location center, double radius) {
        List<Block> ret = new ArrayList<>();
        for (double dx = -radius; dx <= radius; dx++) {
            for (double dy = -radius; dy <= radius; dy++) {
                for (double dz = -radius; dz <= radius; dz++) {
                    double distance2 = (dx * dx) + (dy * dy) + (dz * dz);
                    if (distance2 > radius * radius) continue;
                    Block block = (center.clone().add(dx, dy, dz)).getBlock();
                    if (!block.getType().isAir()) {
                        ret.add(block);
                    }
                }
            }
        }
        return ret;
    }

}
