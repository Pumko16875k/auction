package com.auctionhouse;

import com.auctionhouse.commands.AHCommand;
import com.auctionhouse.events.AuctionEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("auctionhouse")
public class AuctionMod {

    public AuctionMod() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(AuctionEvents.class);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AHCommand.register(event.getDispatcher());
    }
}
