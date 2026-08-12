package com.pumko.auction;

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
        if (event.getContainer() instanceof AuctionMenu) {
            AuctionMenu menu = (AuctionMenu) event.getContainer();
            if (menu.isBuyClick()) {
                PlayerEntity player = event.getPlayer();
                AuctionItem item = menu.getSelectedBuyItem();

                if (item != null && player instanceof ServerPlayerEntity) {
                    ServerPlayerEntity acheteur = (ServerPlayerEntity) player;
                    MinecraftServer server = acheteur.getServer();

                    if (server != null) {
                        String nomAcheteur = acheteur.getName().getString();
                        String nomVendeur = item.getSellerName();
                        double prix = item.getPrice();

                        // 1. On retire l'argent à l'acheteur
                        server.getCommandManager().handleCommand(
                            server.getCommandSource(),
                            "balance remove " + nomAcheteur + " " + prix
                        );

                        // 2. On donne l'argent au vendeur
                        server.getCommandManager().handleCommand(
                            server.getCommandSource(),
                            "balance add " + nomVendeur + " " + prix
                        );

                        // 3. Donner l'objet à l'acheteur
                        acheteur.addItemStackToInventory(item.getStack().copy());

                        // 4. Retirer l'objet du /ah
                        AuctionSaveData.get(server).removeItem(item.getId());

                        acheteur.sendMessage(new StringTextComponent("§aAchat effectué avec succès !"), acheteur.getUniqueID());
                    }
                }
            }
        }
    }

    // Ajouter le prix dans la description de l'item (Lore)
    public static ItemStack formatItemForAH(AuctionItem auctionItem) {
        ItemStack stack = auctionItem.getStack().copy();
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT lore = display.contains("Lore") ? display.getList("Lore", 8) : new ListNBT();

        lore.add(StringNBT.valueOf("{\"text\":\"§7Prix: §e" + auctionItem.getPrice() + " $\",\"italic\":false}"));
        lore.add(StringNBT.valueOf("{\"text\":\"§7Vendeur: §b" + auctionItem.getSellerName() + "\",\"italic\":false}"));

        display.put("Lore", lore);
        tag.put("display", display);
        return stack;
    }
}
