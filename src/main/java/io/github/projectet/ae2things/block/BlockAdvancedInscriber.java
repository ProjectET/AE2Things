package io.github.projectet.ae2things.block;

import appeng.block.AEBaseEntityBlock;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAdvancedInscriber extends AEBaseEntityBlock<BEAdvancedInscriber> {

    public BlockAdvancedInscriber(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return AE2Things.ADVANCED_INSCRIBER_BE.instantiate(pos, state);
    }

    @Override
    public ActionResult onActivated(final World level, final BlockPos pos, final PlayerEntity p,
                                    final Hand hand,
                                    final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if(!level.isClient) {
            NamedScreenHandlerFactory factory = level.getBlockState(pos).createScreenHandlerFactory(level, pos);
            if(factory != null) {
                p.openHandledScreen(factory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory) be : null;
    }

}
