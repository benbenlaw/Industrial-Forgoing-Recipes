package com.benbenlaw.ifrecipes;

import com.benbenlaw.ifrecipes.config.IFConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(IFRecipes.MOD_ID)
public class IFRecipes {
    public static final String MOD_ID = "ifrecipes";

    public IFRecipes(IEventBus eventBus) {

        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.STARTUP, IFConfig.SPEC, "bbl/ifrecipes-startup.toml");

    }
}