package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.util.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Map;

public class PotionInit {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Project.MODID);
    public static void init() {
        Map<String, Map> potions = Util.readFileAsJSON("../src/data/potions.json");
        for (Map.Entry<String, Map> potionEntry : potions.entrySet()) {
            String name = potionEntry.getKey();
            Map data = potionEntry.getValue();
            String effectName = (String) data.get("effect");
            float duration = ((Number) data.get("duration")).floatValue() * 20f;
            int amplifier = ((Number) data.get("amplifier")).intValue() - 1;
            MobEffectInstance effect = null;
            if(effectName.equals("absorption")) effect = new MobEffectInstance(MobEffects.ABSORPTION, (int) duration, amplifier);
            else if(effectName.equals("bad_omen")) effect = new MobEffectInstance(MobEffects.BAD_OMEN, (int) duration, amplifier);
            else if(effectName.equals("blindness")) effect = new MobEffectInstance(MobEffects.BLINDNESS, (int) duration, amplifier);
            else if(effectName.equals("confusion")) effect = new MobEffectInstance(MobEffects.CONFUSION, (int) duration, amplifier);
            else if(effectName.equals("conduit_power")) effect = new MobEffectInstance(MobEffects.CONDUIT_POWER, (int) duration, amplifier);
            else if(effectName.equals("damage_boost")) effect = new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) duration, amplifier);
            else if(effectName.equals("damage_resistance")) effect = new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int) duration, amplifier);
            else if(effectName.equals("darkness")) effect = new MobEffectInstance(MobEffects.DARKNESS, (int) duration, amplifier);
            else if(effectName.equals("dig_slowdown")) effect = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, (int) duration, amplifier);
            else if(effectName.equals("dig_speed")) effect = new MobEffectInstance(MobEffects.DIG_SPEED, (int) duration, amplifier);
            else if(effectName.equals("dolphins_grace")) effect = new MobEffectInstance(MobEffects.DOLPHINS_GRACE, (int) duration, amplifier);
            else if(effectName.equals("fire_resistance")) effect = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) duration, amplifier);
            else if(effectName.equals("glowing")) effect = new MobEffectInstance(MobEffects.GLOWING, (int) duration, amplifier);
            else if(effectName.equals("harm")) effect = new MobEffectInstance(MobEffects.HARM, (int) duration, amplifier);
            else if(effectName.equals("heal")) effect = new MobEffectInstance(MobEffects.HEAL, (int) duration, amplifier);
            else if(effectName.equals("health_boost")) effect = new MobEffectInstance(MobEffects.HEALTH_BOOST, (int) duration, amplifier);
            else if(effectName.equals("hero_of_the_village")) effect = new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, (int) duration, amplifier);
            else if(effectName.equals("hunger")) effect = new MobEffectInstance(MobEffects.HUNGER, (int) duration, amplifier);
            else if(effectName.equals("invisibility")) effect = new MobEffectInstance(MobEffects.INVISIBILITY, (int) duration, amplifier);
            else if(effectName.equals("jump")) effect = new MobEffectInstance(MobEffects.JUMP, (int) duration, amplifier);
            else if(effectName.equals("levitation")) effect = new MobEffectInstance(MobEffects.LEVITATION, (int) duration, amplifier);
            else if(effectName.equals("luck")) effect = new MobEffectInstance(MobEffects.LUCK, (int) duration, amplifier);
            else if(effectName.equals("movement_slowdown")) effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) duration, amplifier);
            else if(effectName.equals("movement_speed")) effect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int) duration, amplifier);
            else if(effectName.equals("night_vision")) effect = new MobEffectInstance(MobEffects.NIGHT_VISION, (int) duration, amplifier);
            else if(effectName.equals("poison")) effect = new MobEffectInstance(MobEffects.POISON, (int) duration, amplifier);
            else if(effectName.equals("regeneration")) effect = new MobEffectInstance(MobEffects.REGENERATION, (int) duration, amplifier);
            else if(effectName.equals("saturation")) effect = new MobEffectInstance(MobEffects.SATURATION, (int) duration, amplifier);
            else if(effectName.equals("slow_falling")) effect = new MobEffectInstance(MobEffects.SLOW_FALLING, (int) duration, amplifier);
            else if(effectName.equals("unluck")) effect = new MobEffectInstance(MobEffects.UNLUCK, (int) duration, amplifier);
            else if(effectName.equals("water_breathing")) effect = new MobEffectInstance(MobEffects.WATER_BREATHING, (int) duration, amplifier);
            else if(effectName.equals("weakness")) effect = new MobEffectInstance(MobEffects.WEAKNESS, (int) duration, amplifier);
            else if(effectName.equals("wither")) effect = new MobEffectInstance(MobEffects.WITHER, (int) duration, amplifier);
            MobEffectInstance finalEffect = effect;
            POTIONS.register(name, () -> new Potion(finalEffect));
        }
    }
}
