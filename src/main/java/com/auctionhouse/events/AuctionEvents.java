package com.auctionhouse.events;

import com.auctionhouse.AuctionMod;
import com.auctionhouse.world.AuctionItem;
import com.auctionhouse.world.AuctionSaveData;
import com.auctionhouse.inventory.AuctionMenu;
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
        if (event.getContainer() instanceof AuctionMenu) {
            AuctionMenu menu = (AuctionMenu) event.getContainer();
            if (menu.isBuyClick() && event.getPlayer() instanceof ServerPlayerEntity) {
                ServerPlayerEntity acheteur = (ServerPlayerEntity) event.getPlayer();
                AuctionItem item = menu.getSelectedBuyItem();
                MinecraftServer server = acheteur.getServer();

                if (item != null && server != null) {
                    String nomAcheteur = acheteur.getName().getString();
                    String nomVendeur = item.getSellerName();
                    double prix = item.getPrice();

                    // 1. Retire l'argent à l'acheteur avec ta vraie commande
                    server.getCommands().performCommand(
                        server.createCommandSourceStack(),
                        "balance remove " + nomAcheteur + " " + prix
                    );

                    // 2. Donne l'argent au vendeur avec ta vraie commande
                    server.getCommands().performCommand(
                        server.createCommandSourceStack(),
                        "balance add " + nomVendeur + " " + prix
                    );

                    // 3. Donne l'objet à l'acheteur
                    acheteur.addItem(item.getStack().copy());

                    // 4. Supprime l'objet de l'hôtel de vente
                    AuctionSaveData.get(server).removeItem(item.getId());

                    acheteur.sendMessage(new StringTextComponent("§aAchat effectué avec succès !"), acheteur.getUUID());
                }
            }
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
