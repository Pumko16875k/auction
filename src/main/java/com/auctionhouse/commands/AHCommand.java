package com.auctionhouse.commands;

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
                player.sendMessage(new StringTextComponent("§aOuverture de l'Hôtel de Ventes..."), player.getUUID());
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

        heldItem.shrink(heldItem.getCount()); 
        player.sendMessage(new StringTextComponent("§aItem mis en vente pour " + price + "$ pendant " + days + " jours !"), player.getUUID());
        return 1;
    }
}
