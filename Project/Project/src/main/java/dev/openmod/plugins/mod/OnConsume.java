package dev.openmod.plugins.mod;

import dev.openmod.project.util.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

public class OnConsume {
    public static void execute(IEventBus bus, Node node, RegistryObject<Item> itemObj) {
        bus.addListener((event) ->{
            if(event instanceof LivingEntityUseItemEvent.Finish) {
                Item item = itemObj.get();
                if(!((LivingEntityUseItemEvent) event).getItem().getItem().equals(item)) return;
                Entity player = ((LivingEntityUseItemEvent) event).getEntity();
                node.setOutputData("Item <Item>", item);
                node.setOutputData("Level <Level>", player.getCommandSenderWorld());
                node.setOutputData("Player <Entity>", player);
                node.TriggerNext("connector");
            }
        });
    }
}
