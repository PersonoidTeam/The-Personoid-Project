package us.notnotdoddy.personoid.npc.resourceGathering.actions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.TargetHandler;
import us.notnotdoddy.personoid.npc.resourceGathering.ResourceTypes;
import us.notnotdoddy.personoid.utils.LocationUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GatherAction {

    Random random = new Random();

    public boolean isCompleted = false;
    private final ResourceTypes gatherType;
    private final PersonoidNPC personoidNPC;
    private Block targetBlock = null;
    private int targetAmount;
    private boolean wasBreaking = false;
    private int currentAmount = 0;
    private int failSafeTicks = 0;
    private int successChecks = 0;
    private Block furnaceBlock = null;
    private int probableCoalCount = 0;
    private int currentCoalCount = 0;
    private boolean wasLooking = false;

    private int itemsTakenFromFurnace = 0;

    private boolean hasTransferedToFurnace = false;

    public GatherAction(ResourceTypes gatherType, PersonoidNPC personoidNPC, int targetAmount) {
        this.gatherType = gatherType;
        this.personoidNPC = personoidNPC;
        this.targetAmount = targetAmount;
        if (gatherType.shouldBeSmelted){
            probableCoalCount = 4;
        }
        Bukkit.broadcastMessage("Made gather action: "+gatherType.toString());
    }

    public void init(){
        lookForNewBlock();
    }

    private void lookForNewBlock(){
        boolean foundBlock = false;
        for (Block block : getBlocksInSphere(personoidNPC.getLivingEntity().getLocation(), 10)){
            if (gatherType.contains(block.getType()) || (gatherType.shouldBeSmelted && ResourceTypes.COAL.contains(block.getType()))){
                targetBlock = block;
                if (wasLooking){
                    personoidNPC.forgetCurrentTarget();
                    wasLooking = false;
                }
                TargetHandler.setBlockTarget(personoidNPC, targetBlock, true);
                foundBlock = true;
                Bukkit.broadcastMessage("Found block");
            }
        }
        if (!foundBlock){
            wasLooking = true;
            Bukkit.broadcastMessage("Wandering because didnt find block");
            failSafeTicks++;
            if (failSafeTicks == 40){
                TargetHandler.setNothingTarget(personoidNPC, LocationUtilities.getRandomLoc(personoidNPC));
                failSafeTicks = 0;
            }
            if (personoidNPC.getCurrentTargetLocation().distance(personoidNPC.getLivingEntity().getLocation()) < 3) {
                TargetHandler.setNothingTarget(personoidNPC, LocationUtilities.getRandomLoc(personoidNPC));
                failSafeTicks = 0;
            }
        }
    }

    public void tick(){
        if (targetAmount == currentAmount && currentCoalCount >= probableCoalCount){
            if (!gatherType.shouldBeSmelted){
                isCompleted = true;
            }
            else {
                if (hasPlacedFurnace()){
                    Furnace furnace = (Furnace) furnaceBlock;
                    if (personoidNPC.getLivingEntity().getLocation().distance(furnaceBlock.getLocation()) >= 6){
                        TargetHandler.setBlockTarget(personoidNPC, furnaceBlock.getRelative(BlockFace.UP), false);
                    }
                    if (!hasTransferedToFurnace){
                        furnace.getInventory().setFuel(new ItemStack(Material.COAL, probableCoalCount));
                        personoidNPC.inventory.removeMaterialCount(Material.COAL, probableCoalCount);

                        furnace.getInventory().setSmelting(new ItemStack(getFirstMatchedMaterial(), targetAmount));
                        personoidNPC.inventory.removeMaterialCount(getFirstMatchedMaterial(), targetAmount);

                        hasTransferedToFurnace = true;
                    }
                    if (furnace.getInventory().getResult().getAmount() >= targetAmount){
                        if (furnace.getInventory().getFuel() != null){
                            personoidNPC.inventory.addItem(furnace.getInventory().getFuel().clone());
                            furnace.getInventory().setFuel(null);
                        }
                        personoidNPC.inventory.addItem(furnace.getInventory().getResult().clone());
                        furnace.getInventory().setResult(null);
                    }
                }
                else {
                    personoidNPC.setMainHandItem(new ItemStack(Material.FURNACE));
                    personoidNPC.getLivingEntity().getLocation().clone().add(1,0,0).getBlock().setType(Material.FURNACE);
                    furnaceBlock = personoidNPC.getLivingEntity().getLocation().clone().add(1,0,0).getBlock();
                }
            }
        }
        else {
            if (!personoidNPC.isInHibernationState()){
                if (targetBlock != null){
                    if (!gatherType.contains(targetBlock.getType())){
                        Bukkit.broadcastMessage("Target block is no longer correct.");
                        if (targetBlock != null){
                            personoidNPC.forgetCurrentTarget();
                            targetBlock = null;
                        }
                        if (wasBreaking){
                            Bukkit.broadcastMessage("was breaking!");
                            for (Entity entity : personoidNPC.getLivingEntity().getNearbyEntities(0.5, 0.5, 0.5)){
                                if (entity instanceof Item item){
                                    ItemStack itemStack = item.getItemStack();
                                    Bukkit.broadcastMessage("itemstack");
                                    if (gatherType.contains(itemStack.getType()) || (gatherType.shouldBeSmelted && ResourceTypes.COAL.contains(itemStack.getType()))){
                                        personoidNPC.inventory.addItem(itemStack);
                                        item.remove();
                                        currentAmount++;
                                        if (itemStack.getType().equals(Material.COAL)){
                                            currentCoalCount += itemStack.getAmount();
                                        }
                                    }
                                }
                            }
                            wasBreaking = false;
                        }
                        lookForNewBlock();
                    }
                    else {
                        if (personoidNPC.getLivingEntity().getLocation().distance(targetBlock.getLocation()) <= 3 && !personoidNPC.paused){
                            wasBreaking = true;
                            personoidNPC.breakBlock(targetBlock.getLocation());
                        }
                    }
                }
                else {
                    lookForNewBlock();
                }
            }
            else {
                Bukkit.broadcastMessage("Im hibernating");
                // Haha, now we can do the lame stuff. Funny how this is literally all code to DISPLAY them getting the resources in the case that someone is watching
                // Which is probably not even going to be that common.
                if (random.nextDouble() <= personoidNPC.behaviourType.resourceGatheringSkill){
                    successChecks++;
                }
                if (successChecks == 100){
                    for (Material material : Material.values()){
                        if (gatherType.contains(material)){
                            personoidNPC.inventory.addItem(new ItemStack(material));
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
