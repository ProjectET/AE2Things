package io.github.projectet.ae2things.block;

import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BlockAdvancedInscriber extends BlockWithEntity {

    public BlockAdvancedInscriber(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BEAdvancedInscriber(pos, state);
    }
}
