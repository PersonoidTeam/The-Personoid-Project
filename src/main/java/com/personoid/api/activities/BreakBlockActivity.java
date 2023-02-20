package com.personoid.api.activities;

import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

public class BreakBlockActivity extends Activity {
    private static final int PICKUP_ITEM_TIME = 20;

    private final Block block;
    private final Material originalMaterial;
    private final boolean collectDrops;
    private int pickupItemTimer;
    private boolean brokeBlock;
    private boolean goneToDroppedItem;

    public BreakBlockActivity(Block block) {
        this(block, true);
    }

    public BreakBlockActivity(Block block, boolean collectDrops) {
        super(ActivityType.INTERACTION);
        this.block = block;
        originalMaterial = block.getType();
        this.collectDrops = collectDrops;
    }

    @Override
    public void onStart(StartType startType) {
        getNPC().getBlockBreaker().start(block);
    }

    @Override
    public void onUpdate() {
        if (!LocationUtils.canReach(block.getLocation(), getNPC().getLocation())) {
            Profiler.ACTIVITIES.push("Block too far away, distance: " + getNPC().getLocation().distance(block.getLocation()));
            markAsFinished(new Result<>(Result.Type.FAILURE));
        } else if (block.getType() != originalMaterial) {
            Profiler.ACTIVITIES.push("Broke " + originalMaterial.name().toLowerCase().replace("_", ""));
            if (!collectDrops) {
                markAsFinished(new Result<>(Result.Type.SUCCESS));
            } else {
                if (!brokeBlock) {
                    brokeBlock = true;
                    addPickupListener();
                    pickupItemTimer = PICKUP_ITEM_TIME;
                } else {
                    if (pickupItemTimer > 0) {
                        pickupItemTimer--;
                    } else if (goneToDroppedItem) {
                        markAsFinished(new Result<>(Result.Type.FAILURE));
                    } else {
                        Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), this::goToPickUpItem, 20);
                    }
                }
            }
        }
    }

    private void goToPickUpItem() {
        getNPC().getWorld().getNearbyEntities(block.getLocation(), 5, 3, 5).forEach((entity) -> {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                if (item.getItemStack().getType().equals(originalMaterial)) {
                    GoToLocationActivity goTo = new GoToLocationActivity(item.getLocation(), GoToLocationActivity.MovementType.SPRINT);
                    goTo.onFinished((result) -> {
                        if (result.getType() == Result.Type.SUCCESS) {
                            addPickupListener();
                            goneToDroppedItem = true;
                            pickupItemTimer = PICKUP_ITEM_TIME;
                        } else {
                            markAsFinished(new Result<>(Result.Type.FAILURE));
                        }
                    });
                    goTo.getOptions().setStoppingDistance(0.5F);
                    goTo.getOptions().setFaceLocation(true, Priority.NORMAL);
                    run(goTo);
                }
            }
        });
    }

    private void addPickupListener() {
        getNPC().getInventory().addPickupListener("block_breaker", (item) -> {
            if (item.getType().equals(originalMaterial)) {
                markAsFinished(new Result<>(Result.Type.SUCCESS));
            }
            getNPC().getInventory().removePickupListener("block_breaker");
        });
    }

    @Override
    public void onStop(StopType stopType) {
        getNPC().getBlockBreaker().stop();
    }

    @Override
    public boolean canStart(StartType startType) {
        return LocationUtils.canReach(block.getLocation(), getNPC().getLocation());
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }
}
