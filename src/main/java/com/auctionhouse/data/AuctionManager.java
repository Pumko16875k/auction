package com.auctionhouse.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {

    public static class Listing {
        public String sellerName;
        public ItemStack item;
        public double price;
        public int days;

        public Listing(String sellerName, ItemStack item, double price, int days) {
            this.sellerName = sellerName;
            this.item = item;
            this.price = price;
            this.days = days;
        }
    }

    public static final List<Listing> activeListings = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void addListing(ServerPlayerEntity seller, ItemStack item, double price, int days) {
        activeListings.add(new Listing(seller.getScoreboardName(), item, price, days));
        saveListings(); // Sauvegarde automatique à chaque mise en vente
    }

    public static void saveListings() {
        try {
            File dir = FMLPaths.GAMEDIR.get().resolve("config").toFile();
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "auctionhouse_data.json");

            JsonArray jsonArray = new JsonArray();
            for (Listing listing : activeListings) {
                JsonObject obj = new JsonObject();
                obj.addProperty("seller", listing.sellerName);
                obj.addProperty("price", listing.price);
                obj.addProperty("days", listing.days);
                obj.addProperty("itemNBT", listing.item.save(new CompoundNBT()).toString());
                jsonArray.add(obj);
            }

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(jsonArray, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadListings() {
        try {
            File file = FMLPaths.GAMEDIR.get().resolve("config/auctionhouse_data.json").toFile();
            if (!file.exists()) return;

            activeListings.clear();
            try (FileReader reader = new FileReader(file)) {
                JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject obj = jsonArray.get(i).getAsJsonObject();
                    String seller = obj.get("seller").getAsString();
                    double price = obj.get("price").getAsDouble();
                    int days = obj.get("days").getAsInt();
                    CompoundNBT nbt = JsonToNBT.parseTag(obj.get("itemNBT").getAsString());
                    ItemStack item = ItemStack.of(nbt);

                    activeListings.add(new Listing(seller, item, price, days));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (slotId >= 0 && slotId < activeListings.size() && player instanceof ServerPlayerEntity) {
                ServerPlayerEntity buyer = (ServerPlayerEntity) player;
                Listing listing = activeListings.get(slotId);

                if (buyer.getScoreboardName().equalsIgnoreCase(listing.sellerName)) {
                    buyer.sendMessage(new StringTextComponent("§cVous ne pouvez pas acheter votre propre objet !"), buyer.getUUID());
                    return ItemStack.EMPTY;
                }

                boolean paiementReussi = AuctionEvents.executeTransaction(buyer, listing.sellerName, listing.price);

                if (paiementReussi) {
                    ItemStack cleanItem = AuctionEvents.cleanItemFromAH(listing.item);
                    buyer.inventory.add(cleanItem);
                    activeListings.remove(slotId);
                    saveListings(); // Sauvegarde automatique après achat pour retirer l'item du fichier JSON
                    buyer.closeContainer();
                }

                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
    }
}
