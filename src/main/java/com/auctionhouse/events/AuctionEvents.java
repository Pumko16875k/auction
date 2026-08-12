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

    @SubscribeEvent
    public static void onInventoryClick(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
        // Événement réservé aux interactions d'inventaire
    }

    public static void processBuy(ServerPlayerEntity buyer, int slot) {
        if (slot < 0 || slot >= AuctionManager.activeListings.size()) return;

        AuctionManager.Listing listing = AuctionManager.activeListings.get(slot);

        // Don d'item à l'acheteur
        buyer.inventory.add(listing.item);
        buyer.sendMessage(new StringTextComponent("§aVous avez acheté l'item pour " + listing.price + "$ !"), buyer.getUUID());

        // Message au vendeur
        if (listing.seller != null && listing.seller.isAlive()) {
            listing.seller.sendMessage(new StringTextComponent("§aVotre item a été vendu pour " + listing.price + "$ !"), listing.seller.getUUID());
        }

        // Retrait de la liste
        AuctionManager.activeListings.remove(slot);
        buyer.closeContainer();
    }
}
