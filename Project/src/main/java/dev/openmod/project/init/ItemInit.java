package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.custom.CustomItem;
import dev.openmod.project.util.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Map;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Project.MODID);
    public static void init() {
        Map<String, Map> items = Util.readFileAsJSON("../src/data/items.json");
        for (Map.Entry<String, Map> itemEntry : items.entrySet()) {
            String name = itemEntry.getKey();
            Map data = itemEntry.getValue();
            Item.Properties properties = new Item.Properties();
            FoodProperties.Builder foodProperties = new FoodProperties.Builder();
            int stacksTo = ((Number) data.get("stacksTo")).intValue();
            float burnTime = ((Number) data.get("burnTime")).floatValue() * 20f;
            String modelType = (String) data.get("modelType");
            String tab = (String) data.get("tab");
            String rarity = (String) data.get("rarity");
            boolean fuel = (boolean) data.get("fuel");
            boolean food = (boolean) data.get("food");
            boolean fireResistant = (boolean) data.get("fireResistant");
            properties.stacksTo(stacksTo);
            if(rarity.equals("common")) properties.rarity(Rarity.COMMON);
            else if(rarity.equals("uncommon")) properties.rarity(Rarity.UNCOMMON);
            else if(rarity.equals("rare")) properties.rarity(Rarity.RARE);
            else if(rarity.equals("epic")) properties.rarity(Rarity.EPIC);
            if(fireResistant) properties.fireResistant();
            if(food) {
                boolean alwaysEat = (boolean) data.get("food_alwaysEat");
                boolean fast = (boolean) data.get("food_fast");
                boolean meat = (boolean) data.get("food_meat");
                int nutrition = ((Number) data.get("food_nutrition")).intValue();
                float saturationMod = ((Number) data.get("food_saturationMod")).floatValue() / 100f;
                ArrayList effects = (ArrayList) data.get("effects");
                for(Object effectEntry : effects) {
                    Map effect = (Map)effectEntry;
                    String effectName = (String) effect.get("name");
                    float probability = ((Number) effect.get("probability")).floatValue() / 100f;
                    float duration = ((Number) effect.get("duration")).floatValue() * 20f;
                    int amplifier = ((Number) effect.get("amplifier")).intValue() - 1;
                    boolean ambient = (boolean) effect.get("ambient");
                    boolean visible = (boolean) effect.get("visible");
                    boolean showIcon = (boolean) effect.get("showIcon");
                    if(effectName.equals("absorption")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("bad_omen")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.BAD_OMEN, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("blindness")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("confusion")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.CONFUSION, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("conduit_power")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.CONDUIT_POWER, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("damage_boost")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("damage_resistance")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("darkness")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.DARKNESS, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("dig_slowdown")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("dig_speed")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("dolphins_grace")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.DOLPHINS_GRACE, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("fire_resistance")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("glowing")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.GLOWING, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("harm")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.HARM, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("heal")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.HEAL, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("health_boost")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("hero_of_the_village")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("hunger")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.HUNGER, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("invisibility")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.INVISIBILITY, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("jump")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.JUMP, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("levitation")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.LEVITATION, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("luck")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.LUCK, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("movement_slowdown")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("movement_speed")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("night_vision")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("poison")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.POISON, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("regeneration")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.REGENERATION, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("saturation")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.SATURATION, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("slow_falling")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("unluck")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.UNLUCK, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("water_breathing")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("weakness")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, (int) duration, amplifier, ambient, visible, showIcon), probability);
                    else if(effectName.equals("wither")) foodProperties.effect(() -> new MobEffectInstance(MobEffects.WITHER, (int) duration, amplifier, ambient, visible, showIcon), probability);
                }
                if(alwaysEat) foodProperties.alwaysEat();
                if(fast) foodProperties.fast();
                if(meat) foodProperties.meat();
                foodProperties.nutrition(nutrition * 2);
                foodProperties.saturationMod(saturationMod);
                properties.food(foodProperties.build());
            }
            if(!tab.equals("none")) TabInit.tabItems.get(tab).put(name, ITEMS.register(name, () ->{
                    return fuel ? new CustomItem(name, modelType, properties) {
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomItem(name, modelType, properties);
            }));
            else ITEMS.register(name, () -> {
                    return fuel ? new CustomItem(name, modelType, properties) {
                        @Override
                        public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                            return ((Number) burnTime).intValue();
                        }
                    } : new CustomItem(name, modelType, properties);
            });
        }
    }
}
