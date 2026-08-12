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

            // Source de commande de niveau CONSOLE / SERVEUR (Bypasse tous les soucis de permissions)
            net.minecraft.command.CommandSource consoleSource = server.createCommandSourceStack().withSuppressedOutput();

            // 1. Retrait de l'argent de l'acheteur
            server.getCommands().performCommand(
                consoleSource,
                "balance remove " + nomAcheteur + " " + prix
            );

            // 2. Crédit de l'argent au vendeur
            server.getCommands().performCommand(
                consoleSource,
                "balance add " + vendeur + " " + prix
            );

            acheteur.sendMessage(new StringTextComponent("§aAchat réussi ! §e" + prix + " $ §aont été prélevés."), acheteur.getUUID());
        }
    }

    public static ItemStack formatItemForAH(ItemStack originalStack, double prix, String vendeur) {
        ItemStack stack = originalStack.copy();
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT lore = display.contains("Lore") ? display.getList("Lore", 8) : new ListNBT();

        lore.add(StringNBT.valueOf("{\"text\":\"§7Prix: §e" + prix + " $\",\"italic\":false}"));
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
