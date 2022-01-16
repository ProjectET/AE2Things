package io.github.projectet.ae2things.block;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.InscriberMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberMenu;
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
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final BEAdvancedInscriber ai = (BEAdvancedInscriber) level.getBlockEntity(pos);
            if (ai != null) {
                if (!level.isClient()) {
                    hit.getSide();
                    ai.getGridNode().getConnections().forEach(o -> {
                        System.out.println("----------");
                        System.out.println(o.a().getOwner().getClass().getSimpleName());
                        System.out.println(o.b().getOwner().getClass().getSimpleName());
                    });
                    MenuOpener.open(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, p,
                            MenuLocators.forBlockEntity(ai));
                }
                return ActionResult.success(level.isClient());
            }
        }
        return ActionResult.PASS;
    }

}
