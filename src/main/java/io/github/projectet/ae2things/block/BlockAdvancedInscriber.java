package io.github.projectet.ae2things.block;

import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AE2Things.ADVANCED_INSCRIBER_BE, BEAdvancedInscriber::tick);
    }
}
