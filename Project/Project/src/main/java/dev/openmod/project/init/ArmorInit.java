package dev.openmod.project.init;

import dev.openmod.project.util.CustomArmor;
import dev.openmod.project.util.Util;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

public class ArmorInit {
        public static void init() {
        Map<String, Map> armors = Util.readFileAsJSON("../src/data/armors.json");
        for (Map.Entry<String, Map> itemEntry : armors.entrySet()) {
            String name = itemEntry.getKey();
            Map data = itemEntry.getValue();
            Item.Properties properties = new Item.Properties();
            int stacksTo = ((Number) data.get("stacksTo")).intValue();
            float burnTime = ((Number) data.get("burnTime")).floatValue() * 20f;
            String tab = (String) data.get("tab");
            String type = (String) data.get("type");
            String material = (String) data.get("material");
            String rarity = (String) data.get("rarity");
            String modelType = (String) data.get("modelType");
            boolean fuel = (boolean) data.get("fuel");
            boolean fireResistant = (boolean) data.get("fireResistant");
            boolean setRepair = (boolean) data.get("setRepair");
            properties.stacksTo(stacksTo);
            if(rarity.equals("common")) properties.rarity(Rarity.COMMON);
            else if(rarity.equals("uncommon")) properties.rarity(Rarity.UNCOMMON);
            else if(rarity.equals("rare")) properties.rarity(Rarity.RARE);
            else if(rarity.equals("epic")) properties.rarity(Rarity.EPIC);
            if(fireResistant) properties.fireResistant();
            if(!setRepair) properties.setNoRepair();
            if(!tab.equals("none")) TabInit.tabItems.get(tab).put(name, ItemInit.ITEMS.register(name, () -> {
                Item item = null;
                    if(type.equals("helmet")) item = fuel ? new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.HELMET, properties) {
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.HELMET, properties);
                    else if(type.equals("boots")) item = fuel ? new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.BOOTS, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.BOOTS, properties);
                    else if(type.equals("chestplate")) item = fuel ? new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.CHESTPLATE, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.CHESTPLATE, properties);
                    else if(type.equals("leggings")) item = fuel ? new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.LEGGINGS, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.LEGGINGS, properties);
                return item;
            }));
            else ItemInit.ITEMS.register(name, () -> {
                Item item = null;
                if(modelType.equals("default"))
                {
                    if(type.equals("helmet")) item = fuel ? new ArmorItem(MaterialInit.materialItems.get(material), ArmorItem.Type.HELMET, properties) {
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new ArmorItem(MaterialInit.materialItems.get(material), ArmorItem.Type.HELMET, properties);
                    else if(type.equals("boots")) item = fuel ? new ArmorItem(MaterialInit.materialItems.get(material), ArmorItem.Type.BOOTS, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new ArmorItem(MaterialInit.materialItems.get(material), ArmorItem.Type.BOOTS, properties);
                    else if(type.equals("chestplate")) item = fuel ? new ArmorItem(MaterialInit.materialItems.get(material), ArmorItem.Type.CHESTPLATE, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new ArmorItem(MaterialInit.materialItems.get(material), ArmorItem.Type.CHESTPLATE, properties);
                    else if(type.equals("leggings")) item = fuel ? new ArmorItem(MaterialInit.materialItems.get(material), ArmorItem.Type.LEGGINGS, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new ArmorItem(MaterialInit.materialItems.get(material), ArmorItem.Type.LEGGINGS, properties);
                } else if(modelType.equals("blockbench")) {
                    if(type.equals("helmet")) item = fuel ? new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.HELMET, properties) {
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.HELMET, properties);
                    else if(type.equals("boots")) item = fuel ? new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.BOOTS, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.BOOTS, properties);
                    else if(type.equals("chestplate")) item = fuel ? new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.CHESTPLATE, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.CHESTPLATE, properties);
                    else if(type.equals("leggings")) item = fuel ? new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.LEGGINGS, properties){
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomArmor(name, modelType, MaterialInit.materialItems.get(material), ArmorItem.Type.LEGGINGS, properties);
                }
                return item;
            });
        }
    }
}
