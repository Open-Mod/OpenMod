package dev.openmod.plugins.mod;

import dev.openmod.project.util.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class Float {
    public static void execute(IEventBus bus, Node node, RegistryObject<Item> itemObj) {
        Map properties = (Map) node.data.get("properties");
        Number num = (Number) properties.get("Number");
        float number = (float) num.floatValue();
        node.setOutputData("Number <Float>", number);
    }
}
