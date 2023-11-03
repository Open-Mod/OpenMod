package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.util.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TabInit {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Project.MODID);
    public static final Map<String, Map<String, Supplier<? extends ItemLike>>> tabItems = new HashMap<String, Map<String, Supplier<? extends ItemLike>>>();
    public static void init() {
        Map<String, Map> tabs = Util.readFileAsJSON("../src/data/tabs.json");
        for (Map.Entry<String, Map> tabEntry : tabs.entrySet()) {
            Map items = new HashMap<String, Supplier<? extends ItemLike>>();
            String name = tabEntry.getKey();
            Map data = tabEntry.getValue();
            CreativeModeTab.Builder properties = CreativeModeTab.builder();
            String title = (String) data.get("title");
            String titleColor = (String) data.get("titleColor");
            String icon = (String) data.get("icon");
            boolean withSearchbar = (boolean) data.get("withSearchbar");
            boolean alignedRight = (boolean) data.get("alignedRight");
            boolean hideTitle = (boolean) data.get("hideTitle");
            boolean noScrollbar = (boolean) data.get("noScrollbar");
            properties.title(Component.literal(title));
            if(withSearchbar) properties.withSearchBar();
            if(alignedRight) properties.alignedRight();
            if(hideTitle) properties.hideTitle();
            if(noScrollbar) properties.noScrollBar();
            properties.withLabelColor(Integer.parseInt(titleColor.substring(1), 16));
            properties.icon(() -> {
                Item item = null;
                for(Object objectItemEntry : items.entrySet()) {
                    Map.Entry<String, Supplier<? extends ItemLike>> itemEntry = (Map.Entry<String, Supplier<? extends ItemLike>>) objectItemEntry;
                    if(icon.equals(Project.MODID + ":" + itemEntry.getKey())) {
                        item = itemEntry.getValue().get().asItem();
                        break;
                    }
                }
                if(item == null) item = RegistryObject.create(new ResourceLocation(icon), ForgeRegistries.ITEMS).get();
                return new ItemStack(item.asItem());
            });
            properties.displayItems((displayParams, output) -> {
                for(Object objectItemEntry : items.entrySet()) {
                    Map.Entry<String, Supplier<? extends ItemLike>> itemEntry = (Map.Entry<String, Supplier<? extends ItemLike>>) objectItemEntry;
                    output.accept(itemEntry.getValue().get());
                }
            });
            tabItems.put(name, items);
            TABS.register(name, () -> properties.build());
        }
    }
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        for(RegistryObject<CreativeModeTab> tabEntry : TABS.getEntries()) {
            if(event.getTab() == tabEntry.get()) {
                Map<String, Supplier<? extends ItemLike>> items = tabItems.get(tabEntry.getKey());
                for(Object objectItemEntry : items.entrySet()) {
                    Map.Entry<String, Supplier<? extends ItemLike>> itemEntry = (Map.Entry<String, Supplier<? extends ItemLike>>) objectItemEntry;
                    event.accept(itemEntry.getValue().get());
                }
            }
        }
    }
}
