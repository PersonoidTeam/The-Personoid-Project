package us.notnotdoddy.personoid.npc.resourceGathering;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.resourceGathering.actions.GatherAction;
import us.notnotdoddy.personoid.utils.DebugMessage;

import java.util.ArrayList;
import java.util.List;

public class GatherStage {

    List<GatherAction> gatherActions = new ArrayList<>();
    GatherAction activeAction = null;
    public boolean stageCompleted = false;
    PersonoidNPC personoidNPC = null;
    Material craftMaterial = null;

    public void setCompletionCraft(Material material, PersonoidNPC personoidNPC){
        craftMaterial = material;
        this.personoidNPC = personoidNPC;
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
            DebugMessage.attemptMessage("Stage completed!");
            stageCompleted = true;
            if (craftMaterial != null){
                personoidNPC.resourceManager.attemptCraft(craftMaterial);
            }
        }
    }
}
