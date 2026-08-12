package com.auctionhouse.events;

import com.auctionhouse.data.AuctionManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "auctionhouse")
public class AuctionEvents {

    public static void processBuy(ServerPlayerEntity buyer, int slot) {
        if (slot < 0 || slot >= AuctionManager.activeListings.size()) return;

        AuctionManager.Listing listing = AuctionManager.activeListings.get(slot);

        // 1. On retire l'argent du compte de l'acheteur via Economy Inc.
        String commandTake = "balance remove " + buyer.getName().getString() + " " + listing.price;
        buyer.getServer().getCommands().performCommand(buyer.getServer().createCommandSourceStack(), commandTake);

        // 2. On ajoute l'argent sur le compte du vendeur via Economy Inc.
        if (listing.seller != null) {
            String commandGive = "balance add " + listing.seller.getName().getString() + " " + listing.price;
            buyer.getServer().getCommands().performCommand(buyer.getServer().createCommandSourceStack(), commandGive);
        }

        // 3. On donne l'item à l'acheteur
        ItemStack itemToGive = listing.item.copy();
        buyer.inventory.add(itemToGive);
        buyer.sendMessage(new StringTextComponent("§aAchat réussi pour " + listing.price + "$ !"), buyer.getUUID());

        // 4. On retire la vente de l'Hôtel de Ventes
        AuctionManager.activeListings.remove(slot);
        buyer.closeContainer();
    }
}
