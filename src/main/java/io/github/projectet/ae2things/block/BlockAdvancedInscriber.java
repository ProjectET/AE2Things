package io.github.projectet.ae2things.block;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberMenu;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockAdvancedInscriber extends AEBaseEntityBlock<BEAdvancedInscriber> {

    public BlockAdvancedInscriber(Properties settings) {
        super(settings);
        settings.requiresCorrectToolForDrops();
        this.registerDefaultState(this.defaultBlockState().setValue(WORKING, false));
        setBlockEntity(BEAdvancedInscriber.class, AE2Things.ADVANCED_INSCRIBER_BE, null, null);
    }

    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, BEAdvancedInscriber be) {
        return currentState.setValue(WORKING, be.isWorking());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AE2Things.ADVANCED_INSCRIBER_BE.create(pos, state);
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
                                    final InteractionHand hand,
                                    final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final BEAdvancedInscriber ai = (BEAdvancedInscriber) level.getBlockEntity(pos);
            if (ai != null) {
                if (!level.isClientSide()) {
                    hit.getDirection();
                    MenuOpener.open(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, p,
                            MenuLocators.forBlockEntity(ai));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

}
