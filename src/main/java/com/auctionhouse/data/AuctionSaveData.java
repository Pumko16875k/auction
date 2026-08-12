package com.auctionhouse.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class AuctionSaveData extends WorldSavedData {
    private static final String DATA_NAME = "auctionhouse_data";

    public AuctionSaveData() {
        super(DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {
        ListNBT list = nbt.getList("listings", Constants.NBT.TAG_COMPOUND);
        AuctionManager.activeListings.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT itemTag = list.getCompound(i);
            // Reconstitution des articles sauvegardés au démarrage
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        // Sauvegarde de la liste des ventes sur le disque
        compound.put("listings", list);
        return compound;
    }
}
