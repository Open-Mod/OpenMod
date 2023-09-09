package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class TierInit {
    public static final Map<String, ForgeTier> tierItems = new HashMap<String, ForgeTier>();
    public static void init() {
        Map<String, Map> tiers = Util.readFileAsJSON("../src/data/tiers.json");
        for (Map.Entry<String, Map> tierEntry : tiers.entrySet()) {
            String name = tierEntry.getKey();
            Map data = tierEntry.getValue();
            int level = ((Number) data.get("level")).intValue();
            int uses = ((Number) data.get("uses")).intValue();
            int enchantmentValue = ((Number) data.get("enchantmentValue")).intValue();
            float attackSpeed = ((Number) data.get("attackSpeed")).floatValue();
            float attackDamageBonus = ((Number) data.get("attackDamageBonus")).floatValue();
            String repairIngredient = (String) data.get("repairIngredient");
            tierItems.put(name, new ForgeTier(level, uses, attackSpeed, attackDamageBonus, enchantmentValue, BlockTags.create(new ResourceLocation(Project.MODID, "needs_" + name + "_tool")), () -> {
                Item item = null;
                for(RegistryObject<Item> itemEntry : ItemInit.ITEMS.getEntries()) {
                    if(repairIngredient.equals(itemEntry.getKey().location().getPath())) {
                        item = itemEntry.get();
                        break;
                    }
                }
                if(item == null) item = RegistryObject.create(new ResourceLocation(repairIngredient), ForgeRegistries.ITEMS).get();
                return Ingredient.of(item);
            }));
        }
    }
}
