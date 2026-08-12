package com.auctionhouse.commands;

import com.auctionhouse.data.AuctionManager;
import com.auctionhouse.events.AuctionEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class AHCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("ah")
            .executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayerOrException();
                AuctionManager.openGUI(player, 0);
                return 1;
            })
            .then(Commands.literal("sell")
                .then(Commands.argument("price", DoubleArgumentType.doubleArg(0.01))
                    .executes(context -> {
                        double price = DoubleArgumentType.getDouble(context, "price");
                        return processSell(context.getSource().getPlayerOrException(), price, 7);
                    })
                    .then(Commands.argument("days", IntegerArgumentType.integer(1, 100))
                        .executes(context -> {
                            double price = DoubleArgumentType.getDouble(context, "price");
                            int days = IntegerArgumentType.getInteger(context, "days");
                            return processSell(context.getSource().getPlayerOrException(), price, days);
                        })
                    )
                )
            )
        );
    }

    private static int processSell(ServerPlayerEntity player, double price, int days) {
        ItemStack heldItem = player.getMainHandItem();
        
        if (heldItem.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cVous devez tenir un item en main !"), player.getUUID());
            return 0;
        }

        String pseudoVendeur = player.getName().getString();
        
        // 1. On applique le formatage (prix + nom du vendeur dans le Lore)
        ItemStack itemFormatted = AuctionEvents.formatItemForAH(heldItem.copy(), price, pseudoVendeur);
        
        // 2. On retire l'item de la main du joueur
        heldItem.shrink(heldItem.getCount()); 

        // 3. On ajoute l'item formaté dans l'Hôtel de Vente
        AuctionManager.addListing(player, itemFormatted, price, days);
        
        player.sendMessage(new StringTextComponent("§aItem mis en vente pour " + price + "$ pendant " + days + " jours !"), player.getUUID());
        return 1;
    }
}
