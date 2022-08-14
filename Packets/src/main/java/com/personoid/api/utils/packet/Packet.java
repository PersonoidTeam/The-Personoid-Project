package com.personoid.api.utils.packet;

import org.bukkit.entity.Player;

public interface Packet {
    void send(Player... players);
    void send();
}
