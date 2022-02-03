package io.github.projectet.ae2things.block;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BECrystalGrowth;
import io.github.projectet.ae2things.gui.crystalGrowth.CrystalGrowthMenu;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCrystalGrowth extends AEBaseEntityBlock<BECrystalGrowth> {

    public BlockCrystalGrowth(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return AE2Things.CRYSTAL_GROWTH_BE.instantiate(pos, state);
    }

    @Override
    public ActionResult onActivated(final World level, final BlockPos pos, final PlayerEntity p,
                                    final Hand hand,
                                    final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final BECrystalGrowth cg = (BECrystalGrowth) level.getBlockEntity(pos);
            if (cg != null) {
                if (!level.isClient()) {
                    hit.getSide();
                    MenuOpener.open(CrystalGrowthMenu.CRYSTAL_GROWTH_SHT, p,
                            MenuLocators.forBlockEntity(cg));
                }
                return ActionResult.success(level.isClient());
            }
        }
        return ActionResult.PASS;
    }
}
