package com.ai_gui_mspgrm71;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("ai_gui_mspgrm71")
public class Aiguimspgrm71 {
    private static final Logger LOGGER = LogManager.getLogger();

    public Aiguimspgrm71() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        LOGGER.info("AI Gui Mod loaded!");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // TODO: Register items, blocks, etc.
    }
}
