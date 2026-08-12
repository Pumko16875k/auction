package com.auctionhouse.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

import java.lang.reflect.Method;

public class AuctionEvents {

    /**
     * Tente d'exécuter la transaction financière.
     * @return true si la transaction financière est un succès, false sinon.
     */
    public static boolean executeTransaction(ServerPlayerEntity acheteur, String vendeur, double prix) {
        MinecraftServer server = acheteur.getServer();
        if (server == null) return false;

        String nomAcheteur = acheteur.getName().getString();

        // 1. Essai via Reflection sur les méthodes Java d'EconomyInc
        try {
            Class<?> apiClass = Class.forName("com.buuz135.economyinc.api.EconomyIncAPI");
            
            // Méthodes type : getBalance, removeBalance, addBalance
            Method getBalanceMethod = apiClass.getMethod("getBalance", java.util.UUID.class);
            Method removeBalanceMethod = apiClass.getMethod("removeBalance", java.util.UUID.class, double.class);
            Method addBalanceMethod = apiClass.getMethod("addBalance", java.util.UUID.class, double.class);

            double soldeActuel = (double) getBalanceMethod.invoke(null, acheteur.getUUID());

            if (soldeActuel < prix) {
                acheteur.sendMessage(new StringTextComponent("§cVous n'avez pas assez d'argent ! (Solde: " + soldeActuel + " $, Requis: " + prix + " $)"), acheteur.getUUID());
                return false;
            }

            // Prélèvement et Crédit
            removeBalanceMethod.invoke(null, acheteur.getUUID(), prix);

            // Recherche de l'UUID du vendeur s'il est en ligne
            ServerPlayerEntity vendeurPlayer = server.getPlayerList().getPlayerByName(vendeur);
            if (vendeurPlayer != null) {
                addBalanceMethod.invoke(null, vendeurPlayer.getUUID(), prix);
                vendeurPlayer.sendMessage(new StringTextComponent("§aVous avez vendu un objet pour §e" + (int)prix + " $ §a!"), vendeurPlayer.getUUID());
            }

            acheteur.sendMessage(new StringTextComponent("§aAchat réussi pour §e" + (int)prix + " $ §a!"), acheteur.getUUID());
            return true;

        } catch (Exception e) {
            // 2. Repli de secours : Exécution directe par commande console si l'API Reflection échoue
            int prixInt = (int) prix;
            int resultRemove = server.getCommands().performCommand(
                server.createCommandSourceStack().withPermission(4),
                "balance remove " + nomAcheteur + " " + prixInt
            );

            if (resultRemove == 0) {
                server.getCommands().performCommand(
                    server.createCommandSourceStack().withPermission(4),
                    "balance take " + nomAcheteur + " " + prixInt
                );
            }

            server.getCommands().performCommand(
                server.createCommandSourceStack().withPermission(4),
                "balance add " + vendeur + " " + prixInt
            );

            acheteur.sendMessage(new StringTextComponent("§aAchat effectué pour §e" + prixInt + " $ !"), acheteur.getUUID());
            return true;
        }
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
