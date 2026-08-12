package com.auctionhouse.events;

import com.auctionhouse.AuctionMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AuctionMod.MOD_ID)
public class AuctionEvents {

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        // Logique de fermeture du menu si nécessaire
    }

    public static void processPurchase(ServerPlayerEntity acheteur, String vendeur, double prix, ItemStack item) {
        MinecraftServer server = acheteur.getServer();

        if (server != null) {
            String nomAcheteur = acheteur.getName().getString();

            // 1. Débiter l'acheteur (syntaxe Forge 1.16.5 exacte)
            server.getCommands().performCommand(
                server.createCommandSourceStack(),
                "balance remove " + nomAcheteur + " " + prix
            );

            // 2. Créditer le vendeur
            server.getCommands().performCommand(
                server.createCommandSourceStack(),
                "balance add " + vendeur + " " + prix
            );

            // 3. Donner l'item et message
            acheteur.addItem(item.copy());
            acheteur.sendMessage(new StringTextComponent("§aAchat effectué avec succès !"), acheteur.getUUID());
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
}
