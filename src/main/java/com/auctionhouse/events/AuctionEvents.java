package com.auctionhouse.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class AuctionEvents {

    public static void executeTransaction(ServerPlayerEntity acheteur, String vendeur, double prix) {
        MinecraftServer server = acheteur.getServer();
        if (server != null) {
            String nomAcheteur = acheteur.getName().getString();

            // Convertit le prix en entier si ton plugin d'économie n'aime pas les doubles (ex: 10.0 -> 10)
            long prixEntier = (long) prix;

            System.out.println("[AH-DEBUG] Tentative de retrait : /balance remove " + nomAcheteur + " " + prixEntier);

            // Exécution directe via les commandes du serveur avec privilèges ROOT (Op Level 4)
            server.getCommands().performCommand(
                server.createCommandSourceStack().withPermission(4),
                "balance remove " + nomAcheteur + " " + prixEntier
            );

            System.out.println("[AH-DEBUG] Tentative d'ajout : /balance add " + vendeur + " " + prixEntier);

            server.getCommands().performCommand(
                server.createCommandSourceStack().withPermission(4),
                "balance add " + vendeur + " " + prixEntier
            );

            acheteur.sendMessage(new StringTextComponent("§aAchat réussi ! §e" + prixEntier + " $ §aont été retirés."), acheteur.getUUID());
        }
    }

    public static ItemStack formatItemForAH(ItemStack originalStack, double prix, String vendeur) {
        ItemStack stack = originalStack.copy();
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT lore = display.contains("Lore") ? display.getList("Lore", 8) : new ListNBT();

        lore.add(StringNBT.valueOf("{\"text\":\"§7Prix: §e" + (long)prix + " $\",\"italic\":false}"));
        lore.add(StringNBT.valueOf("{\"text\":\"§7Vendeur: §b" + vendeur + "\",\"italic\":false}"));

        display.put("Lore", lore);
        tag.put("display", display);
        return stack;
    }

    public static ItemStack cleanItemFromAH(ItemStack item) {
        ItemStack cleanStack = item.copy();
        CompoundNBT tag = cleanStack.getTag();
        if (tag != null && tag.contains("display")) {
            CompoundNBT display = tag.getCompound("display");
            if (display.contains("Lore")) {
                ListNBT lore = display.getList("Lore", 8);
                if (lore.size() >= 2) {
                    lore.remove(lore.size() - 1);
                    lore.remove(lore.size() - 1);
                }
                if (lore.isEmpty()) {
                    display.remove("Lore");
                }
            }
        }
        return cleanStack;
    }
}
