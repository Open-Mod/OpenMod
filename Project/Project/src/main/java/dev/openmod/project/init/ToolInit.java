package dev.openmod.project.init;

import dev.openmod.project.Project;
import dev.openmod.project.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;

import java.util.Map;

public class ToolInit {
        public static void init() {
        Map<String, Map> tools = Util.readFileAsJSON("../src/data/tools.json");
        for (Map.Entry<String, Map> itemEntry : tools.entrySet()) {
            String name = itemEntry.getKey();
            Map data = itemEntry.getValue();
            Item.Properties properties = new Item.Properties();
            int stacksTo = ((Number) data.get("stacksTo")).intValue();
            int attackDamage = ((Number) data.get("attackDamage")).intValue();
            float attackSpeed = ((Number) data.get("attackSpeed")).floatValue() / 100f * 1.6f;
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
                if(type.equals("sword")) item = new SwordItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                else if(type.equals("pickaxe")) item = new PickaxeItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                else if(type.equals("axe")) item = new AxeItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                else if(type.equals("shovel")) item = new ShovelItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                else if(type.equals("hoe")) item = new HoeItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                return item;
            }));
            else ItemInit.ITEMS.register(name, () -> {
                Item item = null;
                if(type.equals("sword")) item = new SwordItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                else if(type.equals("pickaxe")) item = new PickaxeItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                else if(type.equals("axe")) item = new AxeItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                else if(type.equals("shovel")) item = new ShovelItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                else if(type.equals("hoe")) item = new HoeItem(TierInit.tierItems.get(tier), attackDamage, attackSpeed, properties);
                return item;
            });
        }
    }
}
