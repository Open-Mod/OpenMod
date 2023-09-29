package dev.openmod.project;

import dev.openmod.project.init.*;
import dev.openmod.project.custom.CustomBlockRenderer;
import dev.openmod.project.custom.CustomBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(Project.MODID)
public class Project {
    public static final String MODID = "${project}";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public Project() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        TabInit.init();
        SoundInit.init();
        BiomeInit.init(bus);
        ItemInit.init();
        BlockInit.init();
        TreeInit.init();
        MaterialInit.init();
        TierInit.init();
        ArmorInit.init();
        ToolInit.init();
        EventInit.init();
        CustomBlockEntities.init();
        ItemInit.ITEMS.register(bus);
        BlockInit.BLOCKS.register(bus);
        TabInit.TABS.register(bus);
        SoundInit.SOUNDS.register(bus);
        CustomBlockEntities.BLOCK_ENTITIES.register(bus);
        GeckoLib.initialize();
    }
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            for(RegistryObject<BlockEntityType<?>> blockEntry : CustomBlockEntities.BLOCK_ENTITIES.getEntries()) {
                BlockEntityRenderers.register((BlockEntityType) blockEntry.get(), (context) -> new CustomBlockRenderer(blockEntry.getKey().location().getPath()));
            }
        }
    }
}
