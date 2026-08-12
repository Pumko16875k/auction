package com.auctionhouse.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "auctionhouse")
public class AuctionEvents {

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof ChestContainer && event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity acheteur = (ServerPlayerEntity) event.getPlayer();
            MinecraftServer server = acheteur.getServer();

            if (server != null) {
                // Événement à la fermeture
            }
        }
    }

    public static void executeTransaction(ServerPlayerEntity acheteur, String vendeur, double prix) {
        MinecraftServer server = acheteur.getServer();
        if (server != null) {
            String nomAcheteur = acheteur.getName().getString();

            // Retrait de l'argent de l'acheteur
            server.getCommands().performCommand(
                server.createCommandSourceStack(),
                "balance remove " + nomAcheteur + " " + prix
            );

            // Crédit de l'argent au vendeur
            server.getCommands().performCommand(
                server.createCommandSourceStack(),
                "balance add " + vendeur + " " + prix
            );

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
