package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Project.MODID);
    public static void init() {
        Map<String, Map> blocks = Util.readFileAsJSON("../src/data/blocks.json");
        for (Map.Entry<String, Map> blockEntry : blocks.entrySet()) {
            String name = blockEntry.getKey();
            Map data = blockEntry.getValue();
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();
            Item.Properties itemProperties = new Item.Properties();
            FoodProperties.Builder foodProperties = new FoodProperties.Builder();
            float resistance = ((Number) data.get("resistance")).floatValue() / 100f;
            float explosion_resistance = ((Number) data.get("explosion_resistance")).floatValue() / 100f;
            int fire_resistance = 300 - ((Number) data.get("fire_resistance")).intValue() * 3;
            int lightLevel = ((Number) data.get("lightLevel")).intValue();
            int stacksTo = ((Number) data.get("stacksTo")).intValue();
            float burnTime = ((Number) data.get("burnTime")).floatValue() * 20f;
            int minXp = ((Number) data.get("minXp")).intValue();
            int maxXp = ((Number) data.get("maxXp")).intValue();
            float friction = ((Number) data.get("friction")).floatValue() / 100f;
            float jumpFactor = ((Number) data.get("jumpFactor")).floatValue() / 100f;
            float speedFactor = ((Number) data.get("speedFactor")).floatValue() / 100f;
            String tab = (String) data.get("tab");
            String mapColor = (String) data.get("mapColor");
            String instrument = (String) data.get("instrument");
            String breakSound = (String) data.get("breakSound");
            String walkSound = (String) data.get("walkSound");
            String placeSound = (String) data.get("placeSound");
            String hitSound = (String) data.get("hitSound");
            String pushReaction = (String) data.get("pushReaction");
            String minedBy = (String) data.get("minedBy");
            String rarity = (String) data.get("rarity");
            boolean dropXp = (boolean) data.get("dropXp");
            boolean dropItem = (boolean) data.get("dropItem");
            boolean ignitedByLava = (boolean) data.get("ignitedByLava");
            boolean isCollidable = (boolean) data.get("isCollidable");
            boolean fuel = (boolean) data.get("fuel");
            boolean food = (boolean) data.get("food");
            boolean fireResistant = (boolean) data.get("fireResistant");
            properties.noOcclusion();
            properties.strength(resistance, explosion_resistance);
            properties.lightLevel(state -> lightLevel);
            properties.friction(1f - friction);
            properties.jumpFactor(jumpFactor);
            properties.speedFactor(speedFactor);
            itemProperties.stacksTo(stacksTo);
            if(ignitedByLava) properties.ignitedByLava();
            if(!isCollidable) properties.noCollission();
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
            if (mapColor.equals("none")) properties.mapColor(MapColor.NONE);
            else if (mapColor.equals("grass")) properties.mapColor(MapColor.GRASS);
            else if (mapColor.equals("sand")) properties.mapColor(MapColor.SAND);
            else if (mapColor.equals("wool")) properties.mapColor(MapColor.WOOL);
            else if (mapColor.equals("fire")) properties.mapColor(MapColor.FIRE);
            else if (mapColor.equals("ice")) properties.mapColor(MapColor.ICE);
            else if (mapColor.equals("metal")) properties.mapColor(MapColor.METAL);
            else if (mapColor.equals("plant")) properties.mapColor(MapColor.PLANT);
            else if (mapColor.equals("snow")) properties.mapColor(MapColor.SNOW);
            else if (mapColor.equals("clay")) properties.mapColor(MapColor.CLAY);
            else if (mapColor.equals("dirt")) properties.mapColor(MapColor.DIRT);
            else if (mapColor.equals("stone")) properties.mapColor(MapColor.STONE);
            else if (mapColor.equals("water")) properties.mapColor(MapColor.WATER);
            else if (mapColor.equals("wood")) properties.mapColor(MapColor.WOOD);
            else if (mapColor.equals("quartz")) properties.mapColor(MapColor.QUARTZ);
            else if (mapColor.equals("color_orange")) properties.mapColor(MapColor.COLOR_ORANGE);
            else if (mapColor.equals("color_magenta")) properties.mapColor(MapColor.COLOR_MAGENTA);
            else if (mapColor.equals("color_light_blue")) properties.mapColor(MapColor.COLOR_LIGHT_BLUE);
            else if (mapColor.equals("color_yellow")) properties.mapColor(MapColor.COLOR_YELLOW);
            else if (mapColor.equals("color_light_green")) properties.mapColor(MapColor.COLOR_LIGHT_GREEN);
            else if (mapColor.equals("color_pink")) properties.mapColor(MapColor.COLOR_PINK);
            else if (mapColor.equals("color_gray")) properties.mapColor(MapColor.COLOR_GRAY);
            else if (mapColor.equals("color_light_gray")) properties.mapColor(MapColor.COLOR_LIGHT_GRAY);
            else if (mapColor.equals("color_cyan")) properties.mapColor(MapColor.COLOR_CYAN);
            else if (mapColor.equals("color_purple")) properties.mapColor(MapColor.COLOR_PURPLE);
            else if (mapColor.equals("color_blue")) properties.mapColor(MapColor.COLOR_BLUE);
            else if (mapColor.equals("color_brown")) properties.mapColor(MapColor.COLOR_BROWN);
            else if (mapColor.equals("color_green")) properties.mapColor(MapColor.COLOR_GREEN);
            else if (mapColor.equals("color_red")) properties.mapColor(MapColor.COLOR_RED);
            else if (mapColor.equals("color_black")) properties.mapColor(MapColor.COLOR_BLACK);
            else if (mapColor.equals("gold")) properties.mapColor(MapColor.GOLD);
            else if (mapColor.equals("diamond")) properties.mapColor(MapColor.DIAMOND);
            else if (mapColor.equals("lapis")) properties.mapColor(MapColor.LAPIS);
            else if (mapColor.equals("emerald")) properties.mapColor(MapColor.EMERALD);
            else if (mapColor.equals("podzol")) properties.mapColor(MapColor.PODZOL);
            else if (mapColor.equals("nether")) properties.mapColor(MapColor.NETHER);
            else if (mapColor.equals("terracotta_white")) properties.mapColor(MapColor.TERRACOTTA_WHITE);
            else if (mapColor.equals("terracotta_orange")) properties.mapColor(MapColor.TERRACOTTA_ORANGE);
            else if (mapColor.equals("terracotta_magenta")) properties.mapColor(MapColor.TERRACOTTA_MAGENTA);
            else if (mapColor.equals("terracotta_light_blue")) properties.mapColor(MapColor.TERRACOTTA_LIGHT_BLUE);
            else if (mapColor.equals("terracotta_yellow")) properties.mapColor(MapColor.TERRACOTTA_YELLOW);
            else if (mapColor.equals("terracotta_light_green")) properties.mapColor(MapColor.TERRACOTTA_LIGHT_GREEN);
            else if (mapColor.equals("terracotta_pink")) properties.mapColor(MapColor.TERRACOTTA_PINK);
            else if (mapColor.equals("terracotta_gray")) properties.mapColor(MapColor.TERRACOTTA_GRAY);
            else if (mapColor.equals("terracotta_light_gray")) properties.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY);
            else if (mapColor.equals("terracotta_cyan")) properties.mapColor(MapColor.TERRACOTTA_CYAN);
            else if (mapColor.equals("terracotta_purple")) properties.mapColor(MapColor.TERRACOTTA_PURPLE);
            else if (mapColor.equals("terracotta_blue")) properties.mapColor(MapColor.TERRACOTTA_BLUE);
            else if (mapColor.equals("terracotta_brown")) properties.mapColor(MapColor.TERRACOTTA_BROWN);
            else if (mapColor.equals("terracotta_green")) properties.mapColor(MapColor.TERRACOTTA_GREEN);
            else if (mapColor.equals("terracotta_red")) properties.mapColor(MapColor.TERRACOTTA_RED);
            else if (mapColor.equals("terracotta_black")) properties.mapColor(MapColor.TERRACOTTA_BLACK);
            else if (mapColor.equals("crimson_nylium")) properties.mapColor(MapColor.CRIMSON_NYLIUM);
            else if (mapColor.equals("crimson_stem")) properties.mapColor(MapColor.CRIMSON_STEM);
            else if (mapColor.equals("crimson_hyphae")) properties.mapColor(MapColor.CRIMSON_HYPHAE);
            else if (mapColor.equals("warped_nylium")) properties.mapColor(MapColor.WARPED_NYLIUM);
            else if (mapColor.equals("warped_stem")) properties.mapColor(MapColor.WARPED_STEM);
            else if (mapColor.equals("warped_hyphae")) properties.mapColor(MapColor.WARPED_HYPHAE);
            else if (mapColor.equals("warped_wart_block")) properties.mapColor(MapColor.WARPED_WART_BLOCK);
            else if (mapColor.equals("deepslate")) properties.mapColor(MapColor.DEEPSLATE);
            else if (mapColor.equals("raw_iron")) properties.mapColor(MapColor.RAW_IRON);
            else if (mapColor.equals("glow_lichen")) properties.mapColor(MapColor.GLOW_LICHEN);
            Supplier<SoundEvent> aBreakSound = null;
            Supplier<SoundEvent> aWalkSound = null;
            Supplier<SoundEvent> aPlaceSound = null;
            Supplier<SoundEvent> aHitSound = null;
            for(RegistryObject<SoundEvent> soundEntry : SoundInit.SOUNDS.getEntries()) {
                if(soundEntry.getKey().equals(aBreakSound)) {
                    aBreakSound = () -> soundEntry.get();
                    break;
                }
                if(soundEntry.getKey().equals(aWalkSound)) {
                    aWalkSound = () -> soundEntry.get();
                    break;
                }
                if(soundEntry.getKey().equals(aPlaceSound)) {
                    aPlaceSound = () -> soundEntry.get();
                    break;
                }
                if(soundEntry.getKey().equals(aHitSound)) {
                    aHitSound = () -> soundEntry.get();
                    break;
                }
            }
            properties.sound(new ForgeSoundType(1f, 1f, aBreakSound, aWalkSound, aPlaceSound, aHitSound, aWalkSound ));
           // properties.instrument(NoteBlockInstrument(instrument, aBreakSound, null));
            if(pushReaction.equals("ignore")) properties.pushReaction(PushReaction.IGNORE);
            else if(pushReaction.equals("block")) properties.pushReaction(PushReaction.BLOCK);
            else if(pushReaction.equals("push_only")) properties.pushReaction(PushReaction.PUSH_ONLY);
            else if(pushReaction.equals("destroy")) properties.pushReaction(PushReaction.DESTROY);
            else if(pushReaction.equals("normal")) properties.pushReaction(PushReaction.NORMAL);
            if(dropItem)
                if(!minedBy.equals("anything")) properties.requiresCorrectToolForDrops();
            if(!tab.equals("none"))
                TabInit.tabItems.get(tab).put(name, BLOCKS.register(name, () ->  {
                    if(dropXp) {
                        DropExperienceBlock block = new DropExperienceBlock(properties, UniformInt.of(minXp, maxXp)) {
                            @Override
                            public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                                if(!ignitedByLava && fire_resistance == 0) {
                                    return super.getFlammability(state, level, pos, direction);
                                } else return fire_resistance;
                            }
                        };
                        ItemInit.ITEMS.register(name, () -> fuel ? new BlockItem(block, itemProperties){
                            @Override
                            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                                return ((Number) burnTime).intValue();
                            }
                        } : new BlockItem(block, itemProperties));
                        return block;
                    } else {
                        Block block = new Block(properties) {
                            @Override
                            public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                                if(!ignitedByLava && fire_resistance == 0) {
                                    return super.getFlammability(state, level, pos, direction);
                                } else return fire_resistance;
                            }
                        };
                        ItemInit.ITEMS.register(name, () -> fuel ? new BlockItem(block, itemProperties){
                            @Override
                            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                                return ((Number) burnTime).intValue();
                            }
                        } : new BlockItem(block, itemProperties));
                        return block;
                    }
                }));
            else
                BLOCKS.register(name, () ->  {
                    if(dropXp) {
                        DropExperienceBlock block = new DropExperienceBlock(properties, UniformInt.of(minXp, maxXp)) {
                            @Override
                            public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                                if(!ignitedByLava && fire_resistance == 0) {
                                    return super.getFlammability(state, level, pos, direction);
                                } else return fire_resistance;
                            }
                        };
                        ItemInit.ITEMS.register(name, () -> fuel ? new BlockItem(block, itemProperties){
                            @Override
                            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                                return ((Number) burnTime).intValue();
                            }
                        } : new BlockItem(block, itemProperties));
                        return block;
                    } else {
                        Block block = new Block(properties) {
                            @Override
                            public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                                if(!ignitedByLava && fire_resistance == 0) {
                                    return super.getFlammability(state, level, pos, direction);
                                } else return fire_resistance;
                            }
                        };
                        ItemInit.ITEMS.register(name, () -> fuel ? new BlockItem(block, itemProperties){
                            @Override
                            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                                return ((Number) burnTime).intValue();
                            }
                        } : new BlockItem(block, itemProperties));
                        return block;
                    }
                });

        }
    }
}
