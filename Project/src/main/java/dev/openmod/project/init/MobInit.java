package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.custom.CustomMob;
import dev.openmod.project.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

public class MobInit {
    public static final DeferredRegister<EntityType<?>> MOBS = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Project.MODID);
    public static void init(IEventBus bus) {
        Map<String, Map> mobs = Util.readFileAsJSON("../src/data/mobs.json");
        for (Map.Entry<String, Map> mobEntry : mobs.entrySet()) {
            String name = mobEntry.getKey();
            Map data = mobEntry.getValue();
            Item.Properties itemProperties = new Item.Properties();
            FoodProperties.Builder foodProperties = new FoodProperties.Builder();
            int stacksTo = ((Number) data.get("stacksTo")).intValue();
            int minXp = ((Number) data.get("minXp")).intValue();
            int maxXp = ((Number) data.get("maxXp")).intValue();
            float burnTime = ((Number) data.get("burnTime")).floatValue() * 20f;
            float hitboxWidth = ((Number)data.get("hitboxWidth")).floatValue();
            float hitboxHeight = ((Number)data.get("hitboxHeight")).floatValue();
            String tab = (String) data.get("tab");
            String controller = (String) data.get("controller");
            String ridingItem = (String) data.get("ridingItem");
            String foodItem = (String) data.get("foodItem");
            String rarity = (String) data.get("rarity");
            String bgColor = (String) data.get("bgColor");
            String highlightColor = (String) data.get("highlightColor");
            String footstepSound = (String) data.get("footstepSound");
            String ambientSound = (String) data.get("ambientSound");
            String hurtSound = (String) data.get("hurtSound");
            String deathSound = (String) data.get("deathSound");
            boolean rideable = (boolean) data.get("rideable");
            boolean requiresSaddle = (boolean) data.get("requiresSaddle");
            boolean breed = (boolean) data.get("breed");
            boolean dropXp = (boolean) data.get("dropXp");
            boolean fuel = (boolean) data.get("fuel");
            boolean food = (boolean) data.get("food");
            boolean fireResistant = (boolean) data.get("fireResistant");
            if(fireResistant) itemProperties.fireResistant();
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
                itemProperties.food(foodProperties.build());
            }
            if(rarity.equals("common")) itemProperties.rarity(Rarity.COMMON);
            else if(rarity.equals("uncommon")) itemProperties.rarity(Rarity.UNCOMMON);
            else if(rarity.equals("rare")) itemProperties.rarity(Rarity.RARE);
            else if(rarity.equals("epic")) itemProperties.rarity(Rarity.EPIC);
            RegistryObject mobObject = MOBS.register(name, () -> EntityType.Builder.of((entityType, level) -> new CustomMob(name, entityType, level, requiresSaddle, rideable, ridingItem, controller) {
                @org.jetbrains.annotations.Nullable
                @Override
                public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
                    if(!breed) return null;
                    EntityType<CustomMob> offSpring = null;
                    for(RegistryObject<EntityType<?>> mobEntry : MobInit.MOBS.getEntries()) {
                        if(name.equals(mobEntry.getKey().location().getPath())) {
                            offSpring = (EntityType<CustomMob>) mobEntry.get();
                            break;
                        }
                    }
                    return offSpring.create(p_146743_);
                }
                @Override
                public boolean isFood(ItemStack p_27600_) {
                    Item item = null;
                    for(RegistryObject<Item> itemEntry : ItemInit.ITEMS.getEntries()) {
                        if(foodItem.equals(itemEntry.getKey().location().getPath())) {
                            item = itemEntry.get();
                            break;
                        }
                    }
                    if(item == null) item = RegistryObject.create(new ResourceLocation(foodItem), ForgeRegistries.ITEMS).get();
                    return p_27600_.getItem().equals(item);
                }

                @Override
                public int getExperienceReward() {
                    return dropXp ? UniformInt.of(minXp, maxXp).sample(this.level().random) : 0;
                }

                protected void playStepSound(BlockPos pos, BlockState blockIn) {
                    SoundEvent aFootstepSound = null;
                    for(RegistryObject<SoundEvent> soundEntry : SoundInit.SOUNDS.getEntries()) {
                        if(footstepSound.equals(soundEntry.getKey().location().getPath())) {
                            aFootstepSound = soundEntry.get();
                        }
                        if(aFootstepSound != null) break;
                    }
                    this.playSound(aFootstepSound);
                }

                protected SoundEvent getAmbientSound() {
                    SoundEvent aAmbientSound = null;
                    for(RegistryObject<SoundEvent> soundEntry : SoundInit.SOUNDS.getEntries()) {
                        if(ambientSound.equals(soundEntry.getKey().location().getPath())) {
                            aAmbientSound = soundEntry.get();
                        }
                        if(aAmbientSound != null) break;
                    }
                    return aAmbientSound;

                }

                protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
                    SoundEvent aHurtSound = null;
                    for(RegistryObject<SoundEvent> soundEntry : SoundInit.SOUNDS.getEntries()) {
                        if(hurtSound.equals(soundEntry.getKey().location().getPath())) {
                            aHurtSound = soundEntry.get();
                        }
                        if(aHurtSound != null) break;
                    }
                    return aHurtSound;
                }

                protected SoundEvent getDeathSound() {
                    SoundEvent aDeathSound = null;
                    for(RegistryObject<SoundEvent> soundEntry : SoundInit.SOUNDS.getEntries()) {
                        if(deathSound.equals(soundEntry.getKey().location().getPath())) {
                            aDeathSound = soundEntry.get();
                        }
                        if(aDeathSound != null) break;
                    }
                    return aDeathSound;
                }
            }, MobCategory.CREATURE).sized(hitboxWidth, hitboxHeight).build(new ResourceLocation(Project.MODID, name).toString()));
            if(!tab.equals("none"))
                TabInit.tabItems.get(tab).put(name, ItemInit.ITEMS.register(name, () -> fuel ? new ForgeSpawnEggItem(mobObject, Integer.parseInt(bgColor.substring(1), 16), Integer.parseInt(highlightColor.substring(1), 16),itemProperties) {
                    @Override
                    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                        return ((Number) burnTime).intValue();
                    }
                } : new ForgeSpawnEggItem(mobObject, Integer.parseInt(bgColor.substring(1), 16), Integer.parseInt(highlightColor.substring(1), 16),itemProperties)));
            else ItemInit.ITEMS.register(name, () -> fuel ? new ForgeSpawnEggItem(mobObject, Integer.parseInt(bgColor.substring(1), 16), Integer.parseInt(highlightColor.substring(1), 16),itemProperties) {
                @Override
                public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                    return ((Number) burnTime).intValue();
                }
            } : new ForgeSpawnEggItem(mobObject, Integer.parseInt(bgColor.substring(1), 16), Integer.parseInt(highlightColor.substring(1), 16),itemProperties));
        }
        bus.addListener((EntityAttributeCreationEvent event) ->
        {
            for (Map.Entry<String, Map> mobEntry : mobs.entrySet()) {
                String name = mobEntry.getKey();
                Map data = mobEntry.getValue();
                double armor = ((Number)data.get("armor")).doubleValue();
                double armorToughness = ((Number)data.get("armorToughness")).doubleValue();
                double luck = ((Number)data.get("luck")).doubleValue();
                double attackDamage = ((Number)data.get("attackDamage")).doubleValue();
                double attackSpeed = ((Number)data.get("attackSpeed")).doubleValue();
                double attackKnockback = ((Number)data.get("attackKnockback")).doubleValue();
                double flyingSpeed = ((Number)data.get("flyingSpeed")).doubleValue();
                double followRange = ((Number)data.get("followRange")).doubleValue();
                double jumpStrength = ((Number)data.get("jumpStrength")).doubleValue();
                double knockbackResistance = ((Number)data.get("knockbackResistance")).doubleValue();
                double maxHealth = ((Number)data.get("maxHealth")).doubleValue();
                double movementSpeed = ((Number)data.get("movementSpeed")).doubleValue();
                double spawnReinforcementsChance = ((Number)data.get("spawnReinforcementsChance")).doubleValue() / 100;
                EntityType<CustomMob> foundMob = null;
                for(RegistryObject<EntityType<?>> mob : MobInit.MOBS.getEntries()) {
                    if(name.equals(mob.getKey().location().getPath())) {
                        foundMob = (EntityType<CustomMob>) mob.get();
                        break;
                    }
                }
                event.put(foundMob, Animal.createMobAttributes().add(Attributes.ARMOR, armor)
                        .add(Attributes.ARMOR_TOUGHNESS, armorToughness)
                        .add(Attributes.LUCK, luck)
                        .add(Attributes.ATTACK_SPEED, attackSpeed)
                        .add(Attributes.ATTACK_DAMAGE, attackDamage)
                        .add(Attributes.ATTACK_KNOCKBACK, attackKnockback)
                        .add(Attributes.FLYING_SPEED, flyingSpeed)
                        .add(Attributes.FOLLOW_RANGE, followRange)
                        .add(Attributes.JUMP_STRENGTH, jumpStrength)
                        .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                        .add(Attributes.MAX_HEALTH, maxHealth)
                        .add(Attributes.MOVEMENT_SPEED, movementSpeed)
                        .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, spawnReinforcementsChance).build());
            }
        });
    }
}
