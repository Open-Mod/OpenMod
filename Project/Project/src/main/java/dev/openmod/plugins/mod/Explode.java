package dev.openmod.plugins.mod;

import dev.openmod.project.util.Node;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class Explode {
    public static void execute(IEventBus bus, Node node, RegistryObject<Item> itemObj) {
        Map properties = (Map) node.data.get("properties");
        Level level = (Level) node.getInputData("Level <Level>");
        if(level.isClientSide()) return;
        double X = ((Number) node.getInputData("X <Double>")).doubleValue();
        double Y = ((Number) node.getInputData("Y <Double>")).doubleValue();
        double Z = ((Number) node.getInputData("Z <Double>")).doubleValue();
        float radius = ((Number) node.getInputData("Radius <Float>")).floatValue();
        boolean withFire = (boolean) properties.get("With Fire?");
        boolean destroyBlocks = (boolean) properties.get("Destroy Blocks?");
        if(destroyBlocks) level.explode(null, X, Y, Z, radius, withFire, Level.ExplosionInteraction.TNT);
        else level.explode(null,X, Y, Z, radius, withFire, Level.ExplosionInteraction.NONE);
        node.TriggerNext("connector");
    }
}
