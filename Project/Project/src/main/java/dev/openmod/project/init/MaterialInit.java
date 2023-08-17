package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.util.ModArmorMaterial;
import dev.openmod.project.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class MaterialInit {
    public static final Map<String, ModArmorMaterial> materialItems = new HashMap<String, ModArmorMaterial>();
    public static void init() {
        Map<String, Map> materials = Util.readFileAsJSON("../src/data/materials.json");
        for (Map.Entry<String, Map> materialEntry : materials.entrySet()) {
            String name = materialEntry.getKey();
            Map data = materialEntry.getValue();
            int durabilityForHelmet = ((Number) data.get("durabilityForHelmet")).intValue();
            int durabilityForBoots = ((Number) data.get("durabilityForBoots")).intValue();
            int durabilityForChestplate = ((Number) data.get("durabilityForChestplate")).intValue();
            int durabilityForLeggings = ((Number) data.get("durabilityForLeggings")).intValue();
            int protectionForHelmet = ((Number) data.get("protectionForHelmet")).intValue();
            int protectionForBoots = ((Number) data.get("protectionForBoots")).intValue();
            int protectionForChestplate = ((Number) data.get("protectionForChestplate")).intValue();
            int protectionForLeggings = ((Number) data.get("protectionForLeggings")).intValue();
            int enchantmentValue = ((Number) data.get("enchantmentValue")).intValue();
            float toughness = ((Number) data.get("toughness")).floatValue();
            float knockbackResistance = ((Number) data.get("knockbackResistance")).floatValue();
            String repairIngredient = (String) data.get("repairIngredient");
            materialItems.put(name, new ModArmorMaterial(new int[] {durabilityForHelmet, durabilityForBoots, durabilityForChestplate, durabilityForLeggings}, new int[] {protectionForHelmet, protectionForBoots, protectionForChestplate, protectionForLeggings}, enchantmentValue, SoundEvents.ARMOR_EQUIP_GENERIC, () -> {
                Item item = null;
                for(RegistryObject<Item> itemEntry : ItemInit.ITEMS.getEntries()) {
                    if(itemEntry.getKey().equals(repairIngredient)) {
                        item = itemEntry.get();
                        break;
                    }
                }
                if(item == null) item = RegistryObject.create(new ResourceLocation(repairIngredient), ForgeRegistries.ITEMS).get();
                return Ingredient.of(item);
            }, name, toughness, knockbackResistance));
        }
    }
}
