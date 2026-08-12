package com.auctionhouse.data;

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
        public ItemStack item;
        public double price;
        public int days;

        public Listing(ServerPlayerEntity seller, ItemStack item, double price, int days) {
            this.seller = seller;
            this.item = item;
            this.price = price;
            this.days = days;
        }
    }

    public static final List<Listing> activeListings = new ArrayList<>();

    public static void addListing(ServerPlayerEntity seller, ItemStack item, double price, int days) {
        activeListings.add(new Listing(seller, item, price, days));
    }

    public static void openGUI(ServerPlayerEntity player, int page) {
        Inventory chestInventory = new Inventory(54); // Interface coffre double (54 slots)

        for (int i = 0; i < Math.min(activeListings.size(), 45); i++) {
            chestInventory.setItem(i, activeListings.get(i).item);
        }

        player.openMenu(new SimpleNamedContainerProvider((id, playerInv, p) -> 
            ChestContainer.sixRows(id, playerInv, chestInventory),
            new StringTextComponent("Hôtel de Ventes - Page " + (page + 1))
        ));
    }
}
