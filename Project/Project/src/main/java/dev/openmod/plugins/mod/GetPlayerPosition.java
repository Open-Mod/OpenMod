package dev.openmod.plugins.mod;

import dev.openmod.project.util.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;

public class GetPlayerPosition {
    public static <T> void execute(IEventBus bus, Node node, T item) {
        Map properties = (Map) node.data.get("properties");
        Player player = Minecraft.getInstance().player;
        Vec3 pos = player.position();
        node.setOutputData("X <Double>", pos.x);
        node.setOutputData("Y <Double>", pos.y);
        node.setOutputData("Z <Double>", pos.z);
        node.TriggerNext("connector");
    }
}
