package io.github.projectet.ae2things.block.entity;

import appeng.blockentity.misc.InscriberBlockEntity;
import io.github.projectet.ae2things.AE2Things;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BEAdvancedInscriber extends InscriberBlockEntity {

    public BEAdvancedInscriber(BlockPos pos, BlockState state) {
        super(AE2Things.ADVANCED_INSCRIBER_BE, pos, state);
    }

}
