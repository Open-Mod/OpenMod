package dev.openmod.project.init;


import dev.openmod.project.util.Node;
import dev.openmod.project.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventInit {
    public static void init() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        Map itemNodeData = new HashMap();
        Map<String, Map> items = Util.readFileAsJSON("../src/data/items.json");
        for (Map.Entry<String, Map> itemEntry : items.entrySet()) {
            Map data = itemEntry.getValue();
            Map node_data = (Map) data.get("node_data");
            ArrayList connected_nodes = (ArrayList) node_data.get("connected_nodes");
            for(Object nodeEntry : connected_nodes) {
                Map node = (Map)nodeEntry;
                ArrayList inputs = (ArrayList) node.get("inputs");
                boolean isEvent = true;
                for(Object inputEntry : inputs) {
                    Map input = (Map) inputEntry;
                    boolean isConnector = ((String) input.get("type")).equals("connector");
                    if(isConnector) {
                        isEvent = false;
                        break;
                    }
                }
                if(isEvent) {
                    String plugin = (String) node.get("plugin");
                    RegistryObject item = null;
                    for(RegistryObject<Item> entry : ItemInit.ITEMS.getEntries()) {
                        if(itemEntry.getKey().equals(entry.getKey().location().getPath())) {
                            item = entry;
                            break;
                        }
                    }
                    Node n = new Node(node, itemNodeData, connected_nodes, bus, item);
                    //${plugins}
                }
            }
        }
        Map blockNodeData = new HashMap();
        Map<String, Map> blocks = Util.readFileAsJSON("../src/data/blocks.json");
        for (Map.Entry<String, Map> blockEntry : blocks.entrySet()) {
            Map data = blockEntry.getValue();
            Map node_data = (Map) data.get("node_data");
            ArrayList connected_nodes = (ArrayList) node_data.get("connected_nodes");
            for(Object nodeEntry : connected_nodes) {
                Map node = (Map)nodeEntry;
                ArrayList inputs = (ArrayList) node.get("inputs");
                boolean isEvent = true;
                for(Object inputEntry : inputs) {
                    Map input = (Map) inputEntry;
                    boolean isConnector = ((String) input.get("type")).equals("connector");
                    if(isConnector) {
                        isEvent = false;
                        break;
                    }
                }
                if(isEvent) {
                    String plugin = (String) node.get("plugin");
                    RegistryObject item = null;
                    for(RegistryObject<Block> entry : BlockInit.BLOCKS.getEntries()) {
                        if(blockEntry.getKey().equals(entry.getKey().location().getPath())) {
                            item = entry;
                            break;
                        }
                    }
                    Node n = new Node(node, blockNodeData, connected_nodes, bus, item);
                    //${plugins}
                }
            }
        }
        Map armorNodeData = new HashMap();
        Map<String, Map> armors = Util.readFileAsJSON("../src/data/armors.json");
        for (Map.Entry<String, Map> armorEntry : armors.entrySet()) {
            Map data = armorEntry.getValue();
            Map node_data = (Map) data.get("node_data");
            ArrayList connected_nodes = (ArrayList) node_data.get("connected_nodes");
            for(Object nodeEntry : connected_nodes) {
                Map node = (Map)nodeEntry;
                ArrayList inputs = (ArrayList) node.get("inputs");
                boolean isEvent = true;
                for(Object inputEntry : inputs) {
                    Map input = (Map) inputEntry;
                    boolean isConnector = ((String) input.get("type")).equals("connector");
                    if(isConnector) {
                        isEvent = false;
                        break;
                    }
                }
                if(isEvent) {
                    String plugin = (String) node.get("plugin");
                    RegistryObject item = null;
                    for(RegistryObject<Item> entry : ItemInit.ITEMS.getEntries()) {
                        if(armorEntry.getKey().equals(entry.getKey().location().getPath())) {
                            item = entry;
                            break;
                        }
                    }
                    Node n = new Node(node, armorNodeData, connected_nodes, bus, item);
                    //${plugins}
                }
            }
        }
        Map toolNodeData = new HashMap();
        Map<String, Map> tools = Util.readFileAsJSON("../src/data/tools.json");
        for (Map.Entry<String, Map> toolEntry : tools.entrySet()) {
            Map data = toolEntry.getValue();
            Map node_data = (Map) data.get("node_data");
            ArrayList connected_nodes = (ArrayList) node_data.get("connected_nodes");
            for(Object nodeEntry : connected_nodes) {
                Map node = (Map)nodeEntry;
                ArrayList inputs = (ArrayList) node.get("inputs");
                boolean isEvent = true;
                for(Object inputEntry : inputs) {
                    Map input = (Map) inputEntry;
                    boolean isConnector = ((String) input.get("type")).equals("connector");
                    if(isConnector) {
                        isEvent = false;
                        break;
                    }
                }
                if(isEvent) {
                    String plugin = (String) node.get("plugin");
                    RegistryObject item = null;
                    for(RegistryObject<Item> entry : ItemInit.ITEMS.getEntries()) {
                        if(toolEntry.getKey().equals(entry.getKey().location().getPath())) {
                            item = entry;
                            break;
                        }
                    }
                    Node n = new Node(node, toolNodeData, connected_nodes, bus, item);
                    //${plugins}
                }
            }
        }
        Map potionNodeData = new HashMap();
        Map<String, Map> potions = Util.readFileAsJSON("../src/data/potions.json");
        for (Map.Entry<String, Map> potionEntry : potions.entrySet()) {
            Map data = potionEntry.getValue();
            Map node_data = (Map) data.get("node_data");
            ArrayList connected_nodes = (ArrayList) node_data.get("connected_nodes");
            for(Object nodeEntry : connected_nodes) {
                Map node = (Map)nodeEntry;
                ArrayList inputs = (ArrayList) node.get("inputs");
                boolean isEvent = true;
                for(Object inputEntry : inputs) {
                    Map input = (Map) inputEntry;
                    boolean isConnector = ((String) input.get("type")).equals("connector");
                    if(isConnector) {
                        isEvent = false;
                        break;
                    }
                }
                if(isEvent) {
                    String plugin = (String) node.get("plugin");
                    RegistryObject item = null;
                    for(RegistryObject<Item> entry : ItemInit.ITEMS.getEntries()) {
                        if(potionEntry.getKey().equals(entry.getKey().location().getPath())) {
                            item = entry;
                            break;
                        }
                    }
                    Node n = new Node(node, potionNodeData, connected_nodes, bus, item);
                    //${plugins}
                }
            }
        }
        Map mobNodeData = new HashMap();
        Map<String, Map> mobs = Util.readFileAsJSON("../src/data/mobs.json");
        for (Map.Entry<String, Map> mobEntry : mobs.entrySet()) {
            Map data = mobEntry.getValue();
            Map node_data = (Map) data.get("node_data");
            ArrayList connected_nodes = (ArrayList) node_data.get("connected_nodes");
            for(Object nodeEntry : connected_nodes) {
                Map node = (Map)nodeEntry;
                ArrayList inputs = (ArrayList) node.get("inputs");
                boolean isEvent = true;
                for(Object inputEntry : inputs) {
                    Map input = (Map) inputEntry;
                    boolean isConnector = ((String) input.get("type")).equals("connector");
                    if(isConnector) {
                        isEvent = false;
                        break;
                    }
                }
                if(isEvent) {
                    String plugin = (String) node.get("plugin");
                    RegistryObject item = null;
                    for(RegistryObject<EntityType<?>> entry : MobInit.MOBS.getEntries()) {
                        if(mobEntry.getKey().equals(entry.getKey().location().getPath())) {
                            item = entry;
                            break;
                        }
                    }
                    Node n = new Node(node, mobNodeData, connected_nodes, bus, item);
                    //${plugins}
                }
            }
        }
        Map playerNodeData = new HashMap();
        Map<String, Map> players = Util.readFileAsJSON("../src/data/players.json");
        for (Map.Entry<String, Map> playerEntry : players.entrySet()) {
            Map data = playerEntry.getValue();
            Map node_data = (Map) data.get("node_data");
            ArrayList connected_nodes = (ArrayList) node_data.get("connected_nodes");
            for(Object nodeEntry : connected_nodes) {
                Map node = (Map)nodeEntry;
                ArrayList inputs = (ArrayList) node.get("inputs");
                boolean isEvent = true;
                for(Object inputEntry : inputs) {
                    Map input = (Map) inputEntry;
                    boolean isConnector = ((String) input.get("type")).equals("connector");
                    if(isConnector) {
                        isEvent = false;
                        break;
                    }
                }
                if(isEvent) {
                    String plugin = (String) node.get("plugin");
                    RegistryObject item = null;
                    Node n = new Node(node, playerNodeData, connected_nodes, bus, item);
                    //${plugins}
                }
            }
        }
    }
}
