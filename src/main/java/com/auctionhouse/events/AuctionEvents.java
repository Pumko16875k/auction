package com.auctionhouse.events;

import com.auctionhouse.data.AuctionManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
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
            // Événement à la fermeture si besoin
        }
    }

    public static void executeTransaction(ServerPlayerEntity acheteur, String vendeur, double prix) {
        MinecraftServer server = acheteur.getServer();
        if (server != null) {
            String nomAcheteur = acheteur.getName().getString();

            // Retrait de l'argent de l'acheteur via EconomyInc
            server.getCommands().performCommand(
                server.createCommandSourceStack(),
                "balance remove " + nomAcheteur + " " + prix
            );

            // Ajout de l'argent au vendeur via EconomyInc
            server.getCommands().performCommand(
                server.createCommandSourceStack(),
                "balance add " + vendeur + " " + prix
            );

            acheteur.sendMessage(new StringTextComponent("§aTransaction effectuée : " + prix + " $ payés à " + vendeur), acheteur.getUUID());
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
