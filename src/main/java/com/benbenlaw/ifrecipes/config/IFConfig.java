package com.benbenlaw.ifrecipes.config;

import com.buuz135.industrial.config.machine.core.LatexProcessingUnitConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class IFConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MAGMA_GENERATOR_FUELS;
    public static final ModConfigSpec.ConfigValue<Integer> LATEX_PROCESSING_UNIT_LATEX_AMOUNT;
    public static final ModConfigSpec.ConfigValue<Integer> LATEX_PROCESSING_UNIT_WATER_AMOUNT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Industrial Foregoing Fuel Config");

        MAGMA_GENERATOR_FUELS = builder.comment("\"minecraft:glowstone_dust, minecraft:water, 250, 500, 100\", energy per tick, duration")
                        .defineList("magma_generator_fuels", List.of(), obj -> obj instanceof String);

        builder.pop();

        builder.push("Latex Processing Unit Config");

        LATEX_PROCESSING_UNIT_LATEX_AMOUNT = builder.comment("Amount of latex consumed per operation, default 750")
                        .defineInRange("latex_amount", 750, 1, LatexProcessingUnitConfig.maxLatexTankSize);

        LATEX_PROCESSING_UNIT_WATER_AMOUNT = builder.comment("Amount of water consumed per operation, default 500")
                        .defineInRange("water_amount", 500, 1, LatexProcessingUnitConfig.maxWaterTankSize);

        builder.pop();


        SPEC = builder.build();
    }
}
