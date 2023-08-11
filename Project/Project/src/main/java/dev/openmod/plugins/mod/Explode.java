package dev.openmod.plugins.mod;

import dev.openmod.project.util.Node;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;

public class Explode {
    public static <T> void execute(IEventBus bus, Node node, T item) {
        Level level = (Level) node.getInputData("Level <Level>");
        double X = ((Number) node.getInputData("X <Double>")).doubleValue();
        double Y = ((Number) node.getInputData("Y <Double>")).doubleValue();
        double Z = ((Number) node.getInputData("Z <Double>")).doubleValue();
        float radius = ((Number) node.getInputData("Radius <Float>")).floatValue();
        level.explode(null, X, Y, Z, radius, Level.ExplosionInteraction.BLOCK);
        node.TriggerNext("connector");
    }
}
