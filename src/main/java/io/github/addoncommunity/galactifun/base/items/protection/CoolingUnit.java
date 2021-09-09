package io.github.addoncommunity.galactifun.base.items.protection;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

import io.github.addoncommunity.galactifun.api.items.ProtectingBlock;
import io.github.addoncommunity.galactifun.api.universe.attributes.atmosphere.AtmosphericEffect;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;

public final class CoolingUnit extends ProtectingBlock {

    private final int tier;

    public CoolingUnit(SlimefunItemStack item, ItemStack[] recipe, int tier) {
        super(item, RecipeType.ENHANCED_CRAFTING_TABLE, recipe);
        this.tier = tier;
    }

    @Override
    protected int getEnergyRequirement() {
        return this.tier * 512;
    }

    @Nonnull
    @Override
    protected AtmosphericEffect getEffect() {
        return AtmosphericEffect.HEAT;
    }

    @Override
    public int getProtection() {
        return this.tier * 2;
    }

    @Override
    public int getRange() {
        return (this.tier - 1) * 500 + 1000;
    }

    @Override
    public int getCapacity() {
        return getEnergyRequirement() * 2;
    }

}
