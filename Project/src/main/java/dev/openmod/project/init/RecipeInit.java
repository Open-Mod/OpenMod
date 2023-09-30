package dev.openmod.project.init;

import dev.openmod.project.custom.CustomBrewingRecipe;
import dev.openmod.project.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class RecipeInit {
    public static void init(IEventBus bus) {
        Map<String, Map> recipes = Util.readFileAsJSON("../src/data/recipes.json");
        bus.addListener((FMLCommonSetupEvent event) ->
        {
            event.enqueueWork(() ->
            {
                for (Map.Entry<String, Map> recipeEntry : recipes.entrySet()) {
                    Map data = recipeEntry.getValue();
                    String type = (String) data.get("type");
                    if(!type.equals("brewing")) continue;
                    String ingredient = (String) data.get("secondItem");
                    String input = (String) data.get("firstItem");
                    String output = (String) data.get("resultItem");
                    Item aIngredient = null;
                    Potion aInput = null;
                    Potion aOutput = null;
                    for(RegistryObject<Item> itemEntry : ItemInit.ITEMS.getEntries()) {
                        if(ingredient.equals(itemEntry.getKey().location().getPath())) {
                            aIngredient = itemEntry.get();
                        }
                        if(aIngredient != null) break;
                    }
                    if(aIngredient == null) aIngredient = RegistryObject.create(new ResourceLocation(ingredient), ForgeRegistries.ITEMS).get();
                    for(RegistryObject<Potion> potionEntry : PotionInit.POTIONS.getEntries()) {
                        if(input.equals(potionEntry.getKey().location().getPath())) {
                            aInput = potionEntry.get();
                        }
                        if(output.equals(potionEntry.getKey().location().getPath())) {
                            aOutput = potionEntry.get();
                        }
                        if(aInput != null && aOutput != null) break;
                    }
                    if(aInput == null) aInput = RegistryObject.create(new ResourceLocation(input), ForgeRegistries.POTIONS).get();
                    if(aOutput == null) aOutput = RegistryObject.create(new ResourceLocation(output), ForgeRegistries.POTIONS).get();
                    BrewingRecipeRegistry.addRecipe(new CustomBrewingRecipe(aInput, aIngredient, aOutput));
                }
            });
        });
    }
}
