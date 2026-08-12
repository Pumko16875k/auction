package com.auctionhouse.data;

import com.auctionhouse.events.AuctionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
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

    public static void openGUI(ServerPlayerEntity player, int page) {
        Inventory chestInventory = new Inventory(54);

        for (int i = 0; i < Math.min(activeListings.size(), 45); i++) {
            chestInventory.setItem(i, activeListings.get(i).item);
        }

        player.openMenu(new SimpleNamedContainerProvider((id, playerInv, p) -> 
            new AHContainer(id, playerInv, chestInventory),
            new StringTextComponent("Hôtel de Ventes")
        ));
    }

    public static class AHContainer extends ChestContainer {
        public AHContainer(int id, PlayerInventory playerInv, Inventory inventory) {
            super(ContainerType.GENERIC_9x6, id, playerInv, inventory, 6);
        }

        @Override
        public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
            // Seuls les slots de l'Hôtel de Ventes (0 à 44) déclenchent l'achat
            if (slotId >= 0 && slotId < activeListings.size() && player instanceof ServerPlayerEntity) {
                ServerPlayerEntity buyer = (ServerPlayerEntity) player;
                Listing listing = activeListings.get(slotId);

                // 1. Exécution forcée de la transaction en mode Console
                AuctionEvents.executeTransaction(buyer, listing.sellerName, listing.price);

                // 2. Nettoyage du lore et don de l'item à l'acheteur
                ItemStack cleanItem = AuctionEvents.cleanItemFromAH(listing.item);
                buyer.inventory.add(cleanItem);

                // 3. Retrait du marché
                activeListings.remove(slotId);

                // 4. Fermeture propre du menu
                buyer.closeContainer();
                return ItemStack.EMPTY;
            }
            
            // Empêche la prise d'items hors-transaction
            return ItemStack.EMPTY;
        }
    }
}
