package com.benbenlaw.ifrecipes.mixin;

import com.benbenlaw.ifrecipes.IFRecipes;
import com.benbenlaw.ifrecipes.config.IFConfig;
import com.buuz135.industrial.block.core.tile.LatexProcessingUnitTile;
import com.buuz135.industrial.block.generator.mycelial.MagmaGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LatexProcessingUnitTile.class)
public class LatexProcessingUnitMixin {

    @Shadow private static int AMOUNT_LATEX;
    @Shadow private static int AMOUNT_WATER;

    @Inject(method = "<clinit>", at = @At("TAIL"))

    private static void onStaticInit(CallbackInfo ci) {
        AMOUNT_LATEX = IFConfig.LATEX_PROCESSING_UNIT_LATEX_AMOUNT.get();
        AMOUNT_WATER = IFConfig.LATEX_PROCESSING_UNIT_WATER_AMOUNT.get();
    }

}
