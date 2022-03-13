package com.notnotdoddy.personoid.listeners;

import com.notnotdoddy.personoid.handlers.NPCHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                if (packet instanceof ClientboundAddPlayerPacket addPlayerPacket) {
                    NPCHandler.renderNPC(player, addPlayerPacket);
                }
                super.write(channelHandlerContext, packet, channelPromise);
            }
        };
        ChannelPipeline pipeline = ((CraftPlayer)player).getHandle().connection.connection.channel.pipeline();
        try {
            pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
        } catch (IllegalArgumentException ignored) { }
    }
}
