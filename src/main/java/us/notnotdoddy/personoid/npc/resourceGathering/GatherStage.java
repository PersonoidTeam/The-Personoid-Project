package us.notnotdoddy.personoid.npc.resourceGathering;

import me.definedoddy.fluidapi.utils.JavaUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.resourceGathering.actions.GatherAction;
import us.notnotdoddy.personoid.types.Types;
import us.notnotdoddy.personoid.utils.DebugMessage;
import us.notnotdoddy.personoid.utils.DelayedAction;

import java.util.ArrayList;
import java.util.List;

public class GatherStage {

    List<GatherAction> gatherActions = new ArrayList<>();
    public GatherAction activeAction = null;
    public boolean stageCompleted = false;
    PersonoidNPC npc = null;
    Material craftMaterial = null;

    public void setCompletionCraft(Material material, PersonoidNPC personoidNPC){
        craftMaterial = material;
        this.npc = personoidNPC;
    }

    public void addGatherAction(GatherAction gatherAction){
        gatherActions.add(gatherAction);
    }

    public void tick(){
        if (!gatherActions.isEmpty()){
            if (activeAction == null) {
                activeAction = gatherActions.get(gatherActions.size() - 1);
                activeAction.init();
            }
            activeAction.tick();
            if (activeAction.isCompleted){
                gatherActions.remove(activeAction);
                activeAction = null;
            }
        }
        else {
            DebugMessage.log("resource", "Stage completed!");
            stageCompleted = true;
            if (craftMaterial != null){
                npc.attemptCraft(craftMaterial);
            }
            Material type = npc.getInventory().getItemInMainHand().getType();
            if (Types.isArmor(type)) {
                new DelayedAction(npc, JavaUtils.random(20, 60)) {
                    @Override
                    public void run() {
                        npc.getInventory().setItem(Types.getArmorSlotType(type), new ItemStack(type));
                        npc.playSound(Types.getArmorEquipSound(type), 1F, 1F);
                        npc.setItemInMainHand(null);
                    }
                };
            }
        }
    }
}
