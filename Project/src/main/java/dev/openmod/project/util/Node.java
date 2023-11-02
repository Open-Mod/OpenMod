package dev.openmod.project.util;

import dev.openmod.project.init.ItemInit;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Node {
    public Map data;
    private Map nodeData;
    private ArrayList nodes;
    private IEventBus bus;

    private RegistryObject item;
    public Node(Map n, Map nodeData, ArrayList nodes, IEventBus bus, RegistryObject item) {
        this.data = n;
        this.nodeData = nodeData;
        this.nodes = nodes;
        this.bus = bus;
        this.item = item;
    }
    public void TriggerNext(String outputName) {
        IEventBus bus = this.bus;
        ArrayList outputs = (ArrayList) this.data.get("outputs");
        for(Object nodeEntry : this.nodes) {
            Map node = (Map)nodeEntry;
            ArrayList inputs = (ArrayList) node.get("inputs");
            ArrayList foundOutputs = (ArrayList) node.get("outputs");
            boolean isEvent = true;
            for(Object inputEntry : inputs) {
                Map input = (Map) inputEntry;
                if(input.get("type") == null) continue;
                boolean isConnector = ((String) input.get("type")).equals("connector");
                if(isConnector) {
                    isEvent = false;
                    break;
                }
            }
            for(Object outputEntry : foundOutputs) {
                Map output = (Map) outputEntry;
                if(output.get("type") == null) continue;
                boolean isConnector = ((String) output.get("type")).equals("connector");
                if(isConnector) {
                    isEvent = false;
                    break;
                }
            }
            if(isEvent) {
                String plugin = (String) node.get("plugin");
                Node n = new Node(node, this.nodeData, this.nodes, bus, this.item);
                //${plugins}
            }
        }
        for(Object outputEntry : outputs) {
            Map output = (Map)outputEntry;
            boolean matched = ((String) output.get("name")).equals(outputName);
            if(matched) {
                ArrayList<Number> links = (ArrayList) output.get("links");
                if(links == null) break;
                for(Number link : links) {
                    for(Object foundNodeEntry : this.nodes) {
                        Map foundNode = (Map)foundNodeEntry;
                        ArrayList foundInputs = (ArrayList) foundNode.get("inputs");
                        for(Object foundInputEntry : foundInputs) {
                            Map foundInput = (Map)foundInputEntry;
                            if(foundInput.get("link") != null) {
                                boolean found = ((Number) foundInput.get("link")).intValue() == link.intValue();
                                if (found) {
                                    String plugin = (String) foundNode.get("plugin");
                                    RegistryObject data = this.item;
                                    Node n = new Node(foundNode, this.nodeData, this.nodes, bus, data);
                                    //${plugins}
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    public Object getInputData(String inputName) {
        String id = ((Number) this.data.get("id")).toString();
        return this.nodeData.get(id +":" + inputName);
    }
    public void setOutputData(String outputName, Object value) {
        ArrayList outputs = (ArrayList) this.data.get("outputs");
        for(Object outputEntry : outputs) {
            Map output = (Map)outputEntry;
            boolean matched = ((String) output.get("name")).equals(outputName);
            if (matched) {
                ArrayList<Number> links = (ArrayList) output.get("links");
                if(links == null) break;
                for(Number link : links) {
                    for(Object foundNodeEntry : this.nodes) {
                        Map foundNode = (Map)foundNodeEntry;
                        ArrayList foundInputs = (ArrayList) foundNode.get("inputs");
                        for(Object foundInputEntry : foundInputs) {
                            Map foundInput = (Map)foundInputEntry;
                            if(foundInput.get("link") != null) {
                                boolean found = ((Number) foundInput.get("link")).intValue() == link.intValue();
                                if (found) {
                                    String id = ((Number) foundNode.get("id")).toString();
                                    String inputName = (String) foundInput.get("name");
                                    this.nodeData.put(id + ":" + inputName, value);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
