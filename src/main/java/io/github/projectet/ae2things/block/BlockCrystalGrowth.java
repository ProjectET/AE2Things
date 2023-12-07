package io.github.projectet.ae2things.block;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.gui.crystalGrowth.CrystalGrowthMenu;
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
import org.jetbrains.annotations.Nullable;

public class BlockCrystalGrowth extends AEBaseEntityBlock<BECrystalGrowth> {

    public BlockCrystalGrowth(Properties settings) {
        super(settings);
        settings.requiresCorrectToolForDrops();
        this.registerDefaultState(this.defaultBlockState().setValue(WORKING, false));
        setBlockEntity(BECrystalGrowth.class, AE2Things.CRYSTAL_GROWTH_BE, null, null);
    }

    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, BECrystalGrowth be) {
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
        return AE2Things.CRYSTAL_GROWTH_BE.create(pos, state);
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
                                    final InteractionHand hand,
                                    final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final BECrystalGrowth cg = (BECrystalGrowth) level.getBlockEntity(pos);
            if (cg != null) {
                if (!level.isClientSide()) {
                    hit.getDirection();
                    MenuOpener.open(CrystalGrowthMenu.CRYSTAL_GROWTH_SHT, p,
                            MenuLocators.forBlockEntity(cg));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }
}
