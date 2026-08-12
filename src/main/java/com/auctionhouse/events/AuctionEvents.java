package com.auctionhouse.events;

import com.auctionhouse.data.AuctionManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "auctionhouse")
public class AuctionEvents {

    public static void processBuy(ServerPlayerEntity buyer, int slot) {
        if (slot < 0 || slot >= AuctionManager.activeListings.size()) return;

        AuctionManager.Listing listing = AuctionManager.activeListings.get(slot);

        // Donner l'item au joueur
        ItemStack itemToGive = listing.item.copy();
        buyer.inventory.add(itemToGive);
        buyer.sendMessage(new StringTextComponent("§aVous avez acheté l'item pour " + listing.price + "$ !"), buyer.getUUID());

        // Notifier le vendeur
        if (listing.seller != null) {
            listing.seller.sendMessage(new StringTextComponent("§aUn de vos items a été vendu pour " + listing.price + "$ !"), listing.seller.getUUID());
        }

        // Retirer la vente
        AuctionManager.activeListings.remove(slot);
        buyer.closeContainer();
    }
}
