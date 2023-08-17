package dev.openmod.project.init;

import dev.openmod.project.util.Util;
import net.minecraft.world.item.*;

import java.util.Map;

public class ArmorInit {
        public static void init() {
        Map<String, Map> armors = Util.readFileAsJSON("../src/data/armors.json");
        for (Map.Entry<String, Map> itemEntry : armors.entrySet()) {
            String name = itemEntry.getKey();
            Map data = itemEntry.getValue();
            Item.Properties properties = new Item.Properties();
            int stacksTo = ((Number) data.get("stacksTo")).intValue();
            String tab = (String) data.get("tab");
            String type = (String) data.get("type");
            String tier = (String) data.get("tier");
            String rarity = (String) data.get("rarity");
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
                if(type.equals("helmet")) item = new ArmorItem(null, ArmorItem.Type.HELMET, properties);
                else if(type.equals("boots")) item = new ArmorItem(null, ArmorItem.Type.BOOTS, properties);
                else if(type.equals("chestplate")) item = new ArmorItem(null, ArmorItem.Type.CHESTPLATE, properties);
                else if(type.equals("leggings")) item = new ArmorItem(null, ArmorItem.Type.LEGGINGS, properties);
                return item;
            }));
            else ItemInit.ITEMS.register(name, () -> {
                Item item = null;
                if(type.equals("helmet")) item = new ArmorItem(null, ArmorItem.Type.HELMET, properties);
                else if(type.equals("boots")) item = new ArmorItem(null, ArmorItem.Type.BOOTS, properties);
                else if(type.equals("chestplate")) item = new ArmorItem(null, ArmorItem.Type.CHESTPLATE, properties);
                else if(type.equals("leggings")) item = new ArmorItem(null, ArmorItem.Type.LEGGINGS, properties);
                return item;
            });
        }
    }
}
