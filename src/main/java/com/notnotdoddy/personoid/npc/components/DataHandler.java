package com.notnotdoddy.personoid.npc.components;

import com.notnotdoddy.personoid.misc.DataContainer;

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
