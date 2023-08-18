package dev.openmod.project;

import dev.openmod.project.init.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Project.MODID)
public class Project {
    public static final String MODID = "${project}";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public Project() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        TabInit.init();
        SoundInit.init();
        ItemInit.init();
        BlockInit.init();
        MaterialInit.init();
        TierInit.init();
        ArmorInit.init();
        ToolInit.init();
        EventInit.init(eventBus);
        ItemInit.ITEMS.register(bus);
        BlockInit.BLOCKS.register(bus);
        TabInit.TABS.register(bus);
        SoundInit.SOUNDS.register(bus);
    }
}
