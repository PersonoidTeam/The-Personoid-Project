package us.notnotdoddy.personoid.npc.resourceGathering;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import us.notnotdoddy.personoid.npc.PersonoidNPCInventory;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.resourceGathering.actions.GatherAction;
import us.notnotdoddy.personoid.utils.DebugMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ResourceManager {

    private final PersonoidNPC personoidNPC;
    private final PersonoidNPCInventory baseInventory;
    // Unrealistic for bots to resource gather forever with 0 breaks, even robots need to rest sometimes.
    private int restTime = 0;
    public boolean isDoingSomething = false;
    public boolean isPaused = false;
    private List<GatherStage> gatherStages = new ArrayList<>();
    private GatherStage activeGatherStage = null;


    public ResourceManager(PersonoidNPC personoidNPC) {
        this.personoidNPC = personoidNPC;
        this.baseInventory = personoidNPC.data.inventory;
    }

    public void tick(){
        if (!isPaused){
            if (restTime > 0){
                restTime--;
            }
            else {
                selectGatherStage();
                if (activeGatherStage != null){
                    activeGatherStage.tick();
                }
            }
        }
    }

    private void selectGatherStage(){
        if (!gatherStages.isEmpty()){
            isDoingSomething = true;
            if (activeGatherStage == null) {
                activeGatherStage = gatherStages.get(gatherStages.size() - 1);
            }
            if (activeGatherStage.stageCompleted){
                gatherStages.remove(activeGatherStage);
                activeGatherStage = null;
            }
        }
        else {
            isDoingSomething = false;
        }
    }

    public boolean attemptCraft(Material itemStack){
        for (Iterator<Recipe> it = Bukkit.getServer().recipeIterator(); it.hasNext(); ) {
            Recipe recipe = it.next();
            if (recipe.getResult().getType().equals(itemStack)) {
                DebugMessage.attemptMessage("Found recipe");
                if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                    attemptCraftFromMaterialList(itemStack, recipe.getResult(), new ArrayList<>(shapelessRecipe.getIngredientList()));
                } else if (recipe instanceof ShapedRecipe shapedRecipe) {
                    attemptCraftFromMaterialList(itemStack, recipe.getResult(), new ArrayList<>(shapedRecipe.getIngredientMap().values()));
                }
            }
        }
        return false;
    }

    private boolean attemptCraftFromMaterialList(Material itemStack, ItemStack result, List<ItemStack> requiredList){
        HashMap<Material, Integer> requiredItems = new HashMap<>();
        DebugMessage.attemptMessage("Recipe has " + requiredItems.size() + " ingredients.");
        for (ItemStack required : requiredList) {
            if (required != null) {
                requiredItems.putIfAbsent(required.getType(), 0);
                requiredItems.put(required.getType(), requiredItems.get(required.getType()) + required.getAmount());
            }
        }
        int successChecks = 0;
        DebugMessage.attemptMessage("This recipe requires: " + requiredItems.size() + " different items");
        List<Material> unsuccessfulMaterials = new ArrayList<>();
        for (Material finalPassItem : requiredItems.keySet()) {
            DebugMessage.attemptMessage(finalPassItem.toString());
            DebugMessage.attemptMessage("NPC has: " + baseInventory.getAmountOf(finalPassItem) + ", requires " + requiredItems.get(finalPassItem));
            if (baseInventory.getAmountOf(finalPassItem) >= requiredItems.get(finalPassItem)) {
                successChecks++;
            } else {
                unsuccessfulMaterials.add(finalPassItem);
            }
        }
        if (successChecks == requiredItems.size()) {
            for (Material removalMaterial : requiredItems.keySet()) {
                baseInventory.removeMaterialCount(removalMaterial, requiredItems.get(removalMaterial));
            }
            //baseInventory.addItem(recipe.getResult());
            personoidNPC.setMainHandItem(result);
            DebugMessage.attemptMessage("NPC has successfully crafted the item!");
            return true;
        }
        else {
            DebugMessage.attemptMessage("NPC was unable to craft item");
            List<Material> ableToBeCraftedMaterials = new ArrayList<>();
            for (Material testCraft : unsuccessfulMaterials) {
                if (!ResourceTypes.SMELTED_ORE.contains(testCraft)){
                    DebugMessage.attemptMessage("Testing if NPC can craft sub-item: " + testCraft.toString());
                    // RECURSION OMG OMG OMG OMG OMG OMG
                    DebugMessage.attemptMessage("Beginning craft for: " + testCraft.toString());
                    if (attemptCraft(testCraft)) {
                        ableToBeCraftedMaterials.add(testCraft);
                    }
                    DebugMessage.attemptMessage("Ending craft for: " + testCraft.toString());
                }
            }
            for (Material craftable : ableToBeCraftedMaterials) {
                unsuccessfulMaterials.remove(craftable);
            }
            if (!unsuccessfulMaterials.isEmpty()) {
                DebugMessage.attemptMessage("NPC was unable to clear items required.");
                List<GatherAction> gatherActions = new ArrayList<>();
                for (Material neededMaterials : unsuccessfulMaterials) {
                    GatherAction gatherAction = new GatherAction(getProperResourceType(neededMaterials), personoidNPC, requiredItems.get(neededMaterials));
                    gatherActions.add(gatherAction);
                    DebugMessage.attemptMessage("Made a gather action for: " + getProperResourceType(neededMaterials).toString());
                }
                GatherStage gatherStage = new GatherStage();
                for (GatherAction gatherAction : gatherActions) {
                    gatherStage.addGatherAction(gatherAction);
                }
                gatherStage.setCompletionCraft(itemStack, personoidNPC);
                gatherStages.add(gatherStage);
                return false;
            } else {
                return attemptCraft(itemStack);
            }
        }
    }

    private ResourceTypes getProperResourceType(Material material){
        if (ResourceTypes.PLANKS.contains(material)){
            return ResourceTypes.LOG;
        }
        if (material.equals(Material.IRON_INGOT) || material.equals(Material.IRON_BLOCK)){
            return ResourceTypes.IRON_ORE;
        }
        if (material.equals(Material.REDSTONE)){
            return ResourceTypes.REDSTONE_ORE;
        }
        if (material.equals(Material.LAPIS_ORE)){
            return ResourceTypes.LAPIS_ORE;
        }
        if (material.equals(Material.GOLD_INGOT)){
            return ResourceTypes.GOLD_ORE;
        }
        if (material.equals(Material.DIAMOND)){
            return ResourceTypes.DIAMOND_ORE;
        }
        if (ResourceTypes.STONE.contains(material)){
            return ResourceTypes.STONE;
        }
        return ResourceTypes.LOG;
    }
}
