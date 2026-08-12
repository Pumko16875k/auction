package com.auctionhouse.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.StringTextComponent;

// Importation directe de l'API d'EconomyInc
import com.buuz135.economyinc.api.EconomyIncAPI;

public class AuctionEvents {

    public static void executeTransaction(ServerPlayerEntity acheteur, String vendeur, double prix) {
        if (acheteur != null) {
            // 1. On retire l'argent à l'acheteur directement via le mod EconomyInc
            EconomyIncAPI.removeBalance(acheteur.getUUID(), (decimal) prix);

            // 2. On ajoute l'argent au vendeur s'il est trouvé par son UUID ou nom
            // Si EconomyInc gère les UUIDs :
            EconomyIncAPI.addBalance(acheteur.getServer().getProfileCache().get(vendeur).getId(), (decimal) prix);

            acheteur.sendMessage(new StringTextComponent("§aAchat réussi ! §e" + prix + " $ §aretirés de votre compte."), acheteur.getUUID());
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
