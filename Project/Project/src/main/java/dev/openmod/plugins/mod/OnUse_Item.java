package dev.openmod.plugins.mod;

import dev.openmod.project.util.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

public class OnUse_Item {
    public static <T> void execute(IEventBus bus, Node node, T itemObj) {
        bus.addListener((event) ->{
            Item item = ((RegistryObject<Item>) itemObj).get();
            if(event instanceof LivingEntityUseItemEvent.Finish && ((LivingEntityUseItemEvent) event).getItem().getItem().equals(item)) {
                node.setOutputData("Item <Item>", item);
                node.setOutputData("Level <Level>", ((LivingEntityUseItemEvent) event).getEntity().getCommandSenderWorld());
                node.TriggerNext("connector");
            }
        });
    }
}
