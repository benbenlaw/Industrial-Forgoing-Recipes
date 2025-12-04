package com.benbenlaw.ifrecipes.mixin;

import com.benbenlaw.ifrecipes.config.IFConfig;
import com.buuz135.industrial.block.generator.mycelial.MagmaGeneratorType;
import com.buuz135.industrial.plugin.jei.generator.MycelialGeneratorRecipe;
import com.hrznstudio.titanium.component.fluid.SidedFluidTankComponent;
import com.hrznstudio.titanium.component.inventory.SidedInventoryComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Mixin(MagmaGeneratorType.class)
public class MagmaGeneratorMixin {

    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)

    private void addCustomMagmaRecipesJEI(CallbackInfoReturnable<List<MycelialGeneratorRecipe>> cir) {
        List<MycelialGeneratorRecipe> recipes = cir.getReturnValue();

        for (String line : IFConfig.MAGMA_GENERATOR_FUELS.get()) {

            try {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String itemString = parts[0].trim();
                String fluidString = parts[1].trim();
                String fluidAmount = parts[2].trim();
                String power = parts[3].trim();
                String ticks = parts[4].trim();

                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemString));
                Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidString));

                MycelialGeneratorRecipe recipe = new MycelialGeneratorRecipe(
                        Arrays.asList(
                                new ArrayList(),
                                List.of(Ingredient.of(item))
                        ),
                        Arrays.asList(
                                List.of(new FluidStack(fluid, Integer.parseInt(fluidAmount))),
                                new ArrayList<>()
                        ),
                        Integer.parseInt(ticks),
                        Integer.parseInt(power)
                );

                MycelialGeneratorRecipe recipeNoItem = new MycelialGeneratorRecipe(
                        Arrays.asList(
                                new ArrayList(),
                                new ArrayList()
                        ),
                        Arrays.asList(
                                List.of(new FluidStack(fluid, Integer.parseInt(fluidAmount))),
                                new ArrayList<>()
                        ),
                        Integer.parseInt(ticks),
                        (Integer.parseInt(power) / 2)
                );

                recipes.add(recipe);
                recipes.add(recipeNoItem);

            } catch (Exception e) {
                System.out.println("Error parsing magma generator fuel config line: " + line);
            }

        }

        cir.setReturnValue(recipes);

    }

    @Inject(method = "getTankInputPredicates", at = @At("RETURN"), cancellable = true)

    private void addCustomMagmaRecipesFluids(CallbackInfoReturnable<List<Predicate<FluidStack>>> cir) {

        List<Predicate<FluidStack>> originalPredicates = cir.getReturnValue();
        if (originalPredicates == null || originalPredicates.isEmpty()) {
            System.out.println("No original predicates found — skipping magma mixin.");
            return;
        }

        List<Predicate<FluidStack>> predicates = new ArrayList<>(originalPredicates);

        List<Fluid> allowedFluids = new ArrayList<>();
        for (String line : IFConfig.MAGMA_GENERATOR_FUELS.get()) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String fluidString = parts[1].trim();
                Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidString));

                if (fluid != Fluids.EMPTY && !allowedFluids.contains(fluid)) {
                    System.out.println("Allowing extra fluid: " + fluidString);
                    allowedFluids.add(fluid);
                }
            } catch (Exception e) {
                System.out.println("Error parsing magma generator fuel config line: " + line);
                e.printStackTrace();
            }
        }

        Predicate<FluidStack> originalLavaCheck = predicates.getFirst();

        Predicate<FluidStack> combinedPredicate = (fluidStack) -> {
            if (originalLavaCheck != null && originalLavaCheck.test(fluidStack)) return true;
            for (Fluid extra : allowedFluids) {
                if (fluidStack.getFluid().isSame(extra)) return true;
            }
            return false;
        };

        predicates.set(0, combinedPredicate);
        cir.setReturnValue(predicates);
    }


    @Inject(method = "getSlotInputPredicates", at = @At("RETURN"), cancellable = true)
    private void addCustomMagmaRecipesItems(CallbackInfoReturnable<List<BiPredicate<ItemStack, Integer>>> cir) {

        List<BiPredicate<ItemStack, Integer>> originalPredicates = cir.getReturnValue();
        if (originalPredicates == null || originalPredicates.size() < 2) {
            System.out.println("Original predicates invalid or too small — skipping magma mixin.");
            return;
        }

        List<BiPredicate<ItemStack, Integer>> predicates = new ArrayList<>(originalPredicates);

        List<Item> allowedItems = new ArrayList<>();
        for (String line : IFConfig.MAGMA_GENERATOR_FUELS.get()) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String itemString = parts[0].trim();
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemString));

                if (!item.getDefaultInstance().is(ItemStack.EMPTY.getItem()) && !allowedItems.contains(item)) {
                    System.out.println("Allowing extra items: " + itemString);
                    allowedItems.add(item);
                }
            } catch (Exception e) {
                System.out.println("Error parsing magma generator fuel config line: " + line);
                e.printStackTrace();
            }
        }

        final BiPredicate<ItemStack, Integer> originalRedstoneCheck = predicates.get(1);

        BiPredicate<ItemStack, Integer> combinedPredicate = (itemStack, slotIndex) -> {

            if (originalRedstoneCheck != null && originalRedstoneCheck.test(itemStack, slotIndex)) {
                return true;
            }

            for (Item extra : allowedItems) {
                if (itemStack.is(extra)) {
                    return true;
                }
            }
            return false;
        };

        predicates.set(1, combinedPredicate);
        cir.setReturnValue(predicates);
    }


    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void customCanStartCheck(INBTSerializable<CompoundTag>[] inputs, CallbackInfoReturnable<Boolean> cir) {
        if (inputs.length < 2 || !(inputs[0] instanceof SidedFluidTankComponent fluidTank) || !(inputs[1] instanceof SidedInventoryComponent inventory)) {
            return;
        }

        for (String line : IFConfig.MAGMA_GENERATOR_FUELS.get()) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String fluidString = parts[1].trim();
                int fluidAmount = Integer.parseInt(parts[2].trim());

                int baseFluidAmount = fluidAmount / 2;

                Fluid requiredFluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidString));

                FluidStack tankFluid = fluidTank.getFluidInTank(0);
                if (tankFluid.getFluid().isSame(requiredFluid) && tankFluid.getAmount() >= baseFluidAmount) {

                    cir.setReturnValue(true);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error parsing magma generator fuel config line in canStart: " + line);
            }
        }
    }

    @Inject(method = "getTimeAndPowerGeneration", at = @At("HEAD"), cancellable = true)
    private void customTimeAndPowerGeneration(INBTSerializable<CompoundTag>[] inputs, CallbackInfoReturnable<Pair<Integer, Integer>> cir) {
        if (inputs.length < 2 || !(inputs[0] instanceof SidedFluidTankComponent fluidTank) || !(inputs[1] instanceof SidedInventoryComponent inventory)) {
            return;
        }

        FluidStack tankFluid = fluidTank.getFluidInTank(0);

        for (String line : IFConfig.MAGMA_GENERATOR_FUELS.get()) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String itemString = parts[0].trim();
                String fluidString = parts[1].trim();
                int fluidAmount = Integer.parseInt(parts[2].trim());
                int configuredPower = Integer.parseInt(parts[3].trim());
                int configuredTicks = Integer.parseInt(parts[4].trim());

                Item requiredItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemString));
                Fluid requiredFluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidString));

                int baseFluidAmount = fluidAmount / 2;

                if (tankFluid.getFluid().isSame(requiredFluid) && tankFluid.getAmount() >= baseFluidAmount) {

                    if (inventory.getStackInSlot(0).is(requiredItem) && inventory.getStackInSlot(0).getCount() > 0) {

                        if (tankFluid.getAmount() >= fluidAmount) {

                            fluidTank.drainForced(fluidAmount, IFluidHandler.FluidAction.EXECUTE);
                            inventory.getStackInSlot(0).shrink(1);

                            cir.setReturnValue(Pair.of(configuredTicks, configuredPower));
                            cir.cancel();
                            return;
                        }
                    }

                    else {
                        fluidTank.drainForced(baseFluidAmount, IFluidHandler.FluidAction.EXECUTE);

                        int halfPower = configuredPower / 2;

                        cir.setReturnValue(Pair.of(configuredTicks, halfPower));
                        cir.cancel();
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing magma generator fuel config line in getTimeAndPowerGeneration: " + line);
            }
        }
    }
}
