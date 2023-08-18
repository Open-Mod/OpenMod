package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class SoundInit {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Project.MODID);
    public static void init() {
        Map<String, Map> sounds = Util.readFileAsJSON("../src/data/sounds.json");
        for (Map.Entry<String, Map> soundEntry : sounds.entrySet()) {
            String name = soundEntry.getKey();
            SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Project.MODID, name)));
        }
    }
}
