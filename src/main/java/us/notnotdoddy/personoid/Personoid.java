package us.notnotdoddy.personoid;

import me.definedoddy.fluidapi.FluidCommand;
import me.definedoddy.fluidapi.FluidMessage;
import me.definedoddy.fluidapi.FluidPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import us.notnotdoddy.personoid.npc.NPCEvents;
import us.notnotdoddy.personoid.utils.ChatMessage;

public final class Personoid extends JavaPlugin {
    //public static String colour = "#FF00AA";

    @Override
    public void onEnable() {
        FluidPlugin.register(this, false);
        FluidMessage.setDefaultPrefix("&b[Personoid] ");
        initListeners();
        ChatMessage.init();
        NPCEvents.init();
        FluidCommand.register(new Commands());
        new FluidMessage("&aPlugin enabled!").usePrefix().send();
    }

    @Override
    public void onDisable() {
        //disabled
    }

    private void initListeners() {
/*        new FluidListener() {
            public void pluginDisable(PluginDisableEvent e) {
                for (PersonoidNPC npc : NPCHandler.getNPCs().values()) {
                    npc.remove();
                }
                new FluidMessage("&cPlugin disabled!").usePrefix().send();
            }
        };*/
        getServer().getPluginManager().registerEvents(new GameEvents(), this);
    }
}
