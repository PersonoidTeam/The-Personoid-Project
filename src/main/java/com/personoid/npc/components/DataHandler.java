package com.personoid.npc.components;

import com.personoid.misc.DataContainer;

public abstract class DataHandler {
    private final DataContainer dataContainer = new DataContainer();
    public DataContainer getDataContainer() {
        return dataContainer;
    }

    public DataHandler() {
        initData();
    }

    protected void initData() {

    }
}
