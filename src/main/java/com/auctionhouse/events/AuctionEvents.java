package com.auctionhouse.events;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class AuctionEvents {

    public static boolean executeTransaction(ServerPlayerEntity acheteur, String vendeur, double prix) {
        MinecraftServer server = acheteur.getServer();
        if (server == null) return false;

        // En mode cracké, on prend le nom brut du joueur tel qu'il apparaît en jeu
        String nomAcheteur = acheteur.getScoreboardName();
        int prixInt = (int) prix;

        // On crée la source DEPUIS LE JOUEUR (pour qu'EconomyInc reconnaisse son compte)
        // mais avec la permission Level 4 (Console) temporaire pour contourner les perms
        CommandSource sourceJoueur = acheteur.createCommandSourceStack()
                .withPermission(4)
                .withSuppressedOutput();

        CommandSource sourceConsole = server.createCommandSourceStack()
                .withPermission(4)
                .withSuppressedOutput();

        // 1. Exécution du retrait via le joueur boosté en OP
        server.getCommands().performCommand(sourceJoueur, "balance remove " + nomAcheteur + " " + prixInt);

        // 2. Crédit au vendeur via la console
        server.getCommands().performCommand(sourceConsole, "balance add " + vendeur + " " + prixInt);

        acheteur.sendMessage(new StringTextComponent("§aAchat réussi ! §e" + prixInt + " $ §aont été prélevés."), acheteur.getUUID());
        return true;
    }

    public static ItemStack formatItemForAH(ItemStack originalStack, double prix, String vendeur) {
        ItemStack stack = originalStack.copy();
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT lore = display.contains("Lore") ? display.getList("Lore", 8) : new ListNBT();

        lore.add(StringNBT.valueOf("{\"text\":\"§7Prix: §e" + (int)prix + " $\",\"italic\":false}"));
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
