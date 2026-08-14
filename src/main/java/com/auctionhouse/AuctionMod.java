package com.auctionhouse;

import com.auctionhouse.commands.AHCommand;
import com.auctionhouse.data.AuctionManager;
import com.auctionhouse.events.AuctionEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.FMLServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(AuctionMod.MOD_ID)
public class AuctionMod {
    public static final String MOD_ID = "auctionhouse";

    public AuctionMod() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(AuctionEvents.class);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AHCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        AuctionManager.loadListings();
    }
}
