package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.custom.CustomArmorMaterial;
import dev.openmod.project.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class MaterialInit {
    public static final Map<String, CustomArmorMaterial> materialItems = new HashMap<String, CustomArmorMaterial>();
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
            String modelType = (String) data.get("modelType");
            String equipSound = (String) data.get("equipSound");
            String repairIngredient = (String) data.get("repairIngredient");
            SoundEvent aEquipSound = SoundEvents.ARMOR_EQUIP_GENERIC;
            for(RegistryObject<SoundEvent> soundEntry : SoundInit.SOUNDS.getEntries()) {
                if(equipSound.equals(soundEntry.getKey().location().getPath())) {
                    aEquipSound = soundEntry.get();
                    break;
                }
            }
            if(aEquipSound == null) aEquipSound = RegistryObject.create(new ResourceLocation(equipSound), ForgeRegistries.SOUND_EVENTS).get();
            materialItems.put(name, new CustomArmorMaterial(modelType, new int[] {durabilityForHelmet, durabilityForBoots, durabilityForChestplate, durabilityForLeggings}, new int[] {protectionForHelmet, protectionForBoots, protectionForChestplate, protectionForLeggings}, enchantmentValue, aEquipSound, () -> {
                Item item = null;
                for(RegistryObject<Item> itemEntry : ItemInit.ITEMS.getEntries()) {
                    if(repairIngredient.equals(Project.MODID + ":" + itemEntry.getKey().location().getPath())) {
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
