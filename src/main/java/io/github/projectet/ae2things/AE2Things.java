package io.github.projectet.ae2things;

import appeng.api.IAEAddonEntrypoint;
import appeng.api.storage.StorageCells;
import appeng.api.upgrades.Upgrades;
import appeng.block.AEBaseEntityBlock;
import appeng.core.definitions.AEItems;
import io.github.projectet.ae2things.block.BlockAdvancedInscriber;
import io.github.projectet.ae2things.block.BlockCrystalGrowth;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.command.Command;
import io.github.projectet.ae2things.gui.cell.DISKItemCellGuiHandler;
import io.github.projectet.ae2things.item.AETItems;
import io.github.projectet.ae2things.storage.DISKCellHandler;
import io.github.projectet.ae2things.util.StorageManager;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AE2Things implements IAEAddonEntrypoint {

    public static final String MOD_ID = "ae2things";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(id("item_group"), () -> new ItemStack(AETItems.DISK_HOUSING));

    public static StorageManager STORAGE_INSTANCE = new StorageManager();

    public static final Block ADVANCED_INSCRIBER = new BlockAdvancedInscriber(FabricBlockSettings.of(Material.METAL).hardness(4f));
    public static BlockEntityType<BEAdvancedInscriber> ADVANCED_INSCRIBER_BE = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("advanced_inscriber_be"), FabricBlockEntityTypeBuilder.create(BEAdvancedInscriber::new, ADVANCED_INSCRIBER).build());


    public static final Block CRYSTAL_GROWTH = new BlockCrystalGrowth(FabricBlockSettings.of(Material.METAL).hardness(4f));
    public static BlockEntityType<BECrystalGrowth> CRYSTAL_GROWTH_BE = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("crystal_growth_be"), FabricBlockEntityTypeBuilder.create(BECrystalGrowth::new, CRYSTAL_GROWTH).build());

    public static Identifier id(String path) {
        return new Identifier(MOD_ID , path);
    }

    private void registerBlockwithItem(String path, Block block) {
        Registry.register(Registry.BLOCK, id(path), block);
        Registry.register(Registry.ITEM, id(path), new BlockItem(block, new Item.Settings().group(ITEM_GROUP)));
    }

    @Override
    public void onAe2Initialized() {
        AETItems.init();
        Command.init();

        StorageCells.addCellHandler(DISKCellHandler.INSTANCE);
        StorageCells.addCellGuiHandler(new DISKItemCellGuiHandler());

        registerBlockwithItem("advanced_inscriber", ADVANCED_INSCRIBER);
        ((AEBaseEntityBlock<BEAdvancedInscriber>) ADVANCED_INSCRIBER).setBlockEntity(BEAdvancedInscriber.class, ADVANCED_INSCRIBER_BE, null, null);

        registerBlockwithItem("crystal_growth", CRYSTAL_GROWTH);
        ((AEBaseEntityBlock<BECrystalGrowth>) CRYSTAL_GROWTH).setBlockEntity(BECrystalGrowth.class, CRYSTAL_GROWTH_BE, null, null);

        Upgrades.add(AEItems.SPEED_CARD, ADVANCED_INSCRIBER, 5);
        Upgrades.add(AEItems.SPEED_CARD, CRYSTAL_GROWTH, 3);

        ServerTickEvents.START_WORLD_TICK.register((world -> {
            STORAGE_INSTANCE = StorageManager.getInstance(world.getServer());
        }));
    }
}
