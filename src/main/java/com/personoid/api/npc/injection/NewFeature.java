package com.personoid.api.npc.injection;

public class NewFeature extends Feature {
    @Hook("tick")
    public void tick() {
        // do something
    }
}
