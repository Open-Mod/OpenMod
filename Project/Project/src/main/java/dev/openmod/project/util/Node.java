package dev.openmod.project.util;

import net.minecraftforge.eventbus.api.IEventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Node <T>{
    public Map data;
    private Map nodeData;
    private ArrayList nodes;
    private IEventBus bus;
    private T item;
    public Node(Map n, Map nodeData, ArrayList nodes, IEventBus bus, T item) {
        this.data = n;
        this.nodeData = nodeData;
        this.nodes = nodes;
        this.bus = bus;
        this.item = item;
    }
    public void TriggerNext(String outputName) {
        IEventBus bus = this.bus;
        ArrayList outputs = (ArrayList) this.data.get("outputs");
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
                            boolean found = ((Number) foundInput.get("link")).intValue() == link.intValue();
                            if(found) {
                                String plugin = (String) foundNode.get("plugin");
                                T data = this.item;
                                Node n = new Node<>(foundNode, this.nodeData, this.nodes, bus, data);
                                //${plugins}
                                break;
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
                            boolean found = ((Number) foundInput.get("link")).intValue() == link.intValue();
                            if(found) {
                                String id = ((Number) foundNode.get("id")).toString();
                                String inputName = (String) foundInput.get("name");
                                this.nodeData.put(id+":"+inputName, value);
                            }
                        }
                    }
                }
            }
        }
    }
}
