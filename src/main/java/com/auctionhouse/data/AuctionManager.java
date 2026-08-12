package com.auctionhouse.data;

import com.auctionhouse.events.AuctionEvents;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class AuctionManager {

    public static class Listing {
        public ServerPlayerEntity seller;
        public String sellerName;
        public ItemStack item;
        public double price;
        public int days;

        public Listing(ServerPlayerEntity seller, ItemStack item, double price, int days) {
            this.seller = seller;
            this.sellerName = seller.getName().getString();
            this.item = item;
            this.price = price;
            this.days = days;
        }
    }

    public static final List<Listing> activeListings = new ArrayList<>();

    public static void addListing(ServerPlayerEntity seller, ItemStack item, double price, int days) {
        activeListings.add(new Listing(seller, item, price, days));
    }

    public static void buyItem(ServerPlayerEntity buyer, int slot) {
        if (slot < 0 || slot >= activeListings.size()) {
            return;
        }

        Listing listing = activeListings.get(slot);

        // 1. Exécution du transfert d'argent via EconomyInc
        AuctionEvents.executeTransaction(buyer, listing.sellerName, listing.price);

        // 2. Nettoyage du Lore et don de l'item propre à l'acheteur
        ItemStack cleanItem = AuctionEvents.cleanItemFromAH(listing.item);
        buyer.inventory.add(cleanItem);

        // 3. Retrait de la liste
        activeListings.remove(slot);

        // 4. Mise à jour du menu
        openGUI(buyer, 0);
    }

    public static void openGUI(ServerPlayerEntity player, int page) {
        Inventory chestInventory = new Inventory(54);

        for (int i = 0; i < Math.min(activeListings.size(), 45); i++) {
            chestInventory.setItem(i, activeListings.get(i).item);
        }

        player.openMenu(new SimpleNamedContainerProvider((id, playerInv, p) -> 
            ChestContainer.sixRows(id, playerInv, chestInventory),
            new StringTextComponent("Hôtel de Ventes - Page " + (page + 1))
        ));
    }
}
