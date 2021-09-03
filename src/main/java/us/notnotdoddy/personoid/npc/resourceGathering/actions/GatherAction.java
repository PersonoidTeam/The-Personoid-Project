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
import us.notnotdoddy.personoid.utils.DebugMessage;
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
    private Material materialWhenStarted = null;
    private Material pickedUpMaterial = null;
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
        DebugMessage.attemptMessage("Made gather action: "+gatherType.toString());
    }

    public void init(){
        lookForNewBlock();
    }

    private boolean shouldGetThatCoal(Material block){
        return gatherType.shouldBeSmelted && ResourceTypes.COAL.contains(block);
    }

    private void lookForNewBlock(){
        boolean foundBlock = false;
        for (Block block : getBlocksInSphere(personoidNPC.getLivingEntity().getLocation(), 10)){
            if (gatherType.contains(block.getType()) || shouldGetThatCoal(block.getType())){
                targetBlock = block;
                materialWhenStarted = targetBlock.getType();
                if (wasLooking){
                    personoidNPC.forgetCurrentTarget();
                    wasLooking = false;
                }
                TargetHandler.setBlockTarget(personoidNPC, LocationUtilities.getNearestStandableLocation(targetBlock.getLocation()).getBlock(), true);
                foundBlock = true;
                DebugMessage.attemptMessage("Found block");
            }
        }
        if (!foundBlock){
            wasLooking = true;
            DebugMessage.attemptMessage("Wandering because didnt find block");
            failSafeTicks++;
            if (personoidNPC.getCurrentTargetLocation() == null){
                Location location = LocationUtilities.getRandomLoc(personoidNPC);
                TargetHandler.setNothingTarget(personoidNPC, LocationUtilities.getNearestStandableLocation(location));
            }
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
        if (targetAmount <= currentAmount && currentCoalCount >= probableCoalCount){
            DebugMessage.attemptMessage("DONE!");
            if (!gatherType.shouldBeSmelted){
                isCompleted = true;
            }
            else {
                if (hasPlacedFurnace()){
                    Furnace furnace = (Furnace) furnaceBlock.getState();
                    if (personoidNPC.getLivingEntity().getLocation().distance(furnaceBlock.getLocation()) >= 6){
                        TargetHandler.setBlockTarget(personoidNPC, furnaceBlock.getRelative(BlockFace.UP), false);
                    }
                    if (!hasTransferedToFurnace){
                        hasTransferedToFurnace = true;
                        furnace.getInventory().setFuel(new ItemStack(Material.COAL, probableCoalCount));
                        personoidNPC.inventory.removeMaterialCount(Material.COAL, probableCoalCount);

                        furnace.getInventory().setSmelting(new ItemStack(pickedUpMaterial, targetAmount));
                        personoidNPC.inventory.removeMaterialCount(pickedUpMaterial, targetAmount);
                    }
                    if (furnace.getInventory().getResult() != null){
                        if (furnace.getInventory().getResult().getAmount() >= targetAmount){
                            if (furnace.getInventory().getFuel() != null){
                                personoidNPC.inventory.addItem(furnace.getInventory().getFuel().clone());
                                furnace.getInventory().setFuel(null);
                            }
                            personoidNPC.inventory.addItem(furnace.getInventory().getResult().clone());
                            furnace.getInventory().setResult(null);
                            personoidNPC.resume();

                            isCompleted = true;
                        }
                    }
                }
                else {
                    DebugMessage.attemptMessage("Placed furnace");
                    personoidNPC.setMainHandItem(new ItemStack(Material.FURNACE));
                    personoidNPC.getLivingEntity().getLocation().clone().add(1,0,0).getBlock().setType(Material.FURNACE);
                    furnaceBlock = personoidNPC.getLivingEntity().getLocation().clone().add(1,0,0).getBlock();
                }
            }
        }
        else {
            if (!personoidNPC.isInHibernationState()){
                if (targetBlock != null){
                    if (!targetBlock.getType().equals(materialWhenStarted)){
                        DebugMessage.attemptMessage("Target block is no longer correct.");
                        if (targetBlock != null){
                            personoidNPC.forgetCurrentTarget();
                            targetBlock = null;
                            materialWhenStarted = null;
                        }
                        if (wasBreaking){
                            DebugMessage.attemptMessage("was breaking!");
                            for (Entity entity : personoidNPC.getLivingEntity().getNearbyEntities(4, 4, 4)){
                                if (entity instanceof Item item){
                                    ItemStack itemStack = item.getItemStack();
                                    DebugMessage.attemptMessage("itemstack");
                                    if (gatherType.contains(itemStack.getType()) || shouldGetThatCoal(itemStack.getType())){
                                        personoidNPC.inventory.addItem(itemStack);
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
                                        DebugMessage.attemptMessage(currentAmount+" blocks collected");
                                        DebugMessage.attemptMessage("Target amount: "+targetAmount);
                                        DebugMessage.attemptMessage(currentCoalCount+" coal collected");
                                        DebugMessage.attemptMessage("Target coal: "+probableCoalCount);

                                    }
                                }
                            }
                            wasBreaking = false;
                        }
                        personoidNPC.resume();
                        lookForNewBlock();
                    }
                    else {
                        if (personoidNPC.getLivingEntity().getLocation().distance(targetBlock.getLocation()) <= 3 && !personoidNPC.paused){
                            wasBreaking = true;
                            personoidNPC.getProperMiningTool(targetBlock);
                            personoidNPC.breakBlock(targetBlock.getLocation());
                        }
                    }
                }
                else {
                    lookForNewBlock();
                }
            }
            else {
                DebugMessage.attemptMessage("Im hibernating");
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
