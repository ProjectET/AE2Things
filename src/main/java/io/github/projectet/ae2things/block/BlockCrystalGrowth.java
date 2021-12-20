package io.github.projectet.ae2things.block;

import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BlockCrystalGrowth extends BlockWithEntity {

    public BlockCrystalGrowth(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BECrystalGrowth(pos, state);
    }
}
