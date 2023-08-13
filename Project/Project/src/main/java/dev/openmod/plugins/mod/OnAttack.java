package dev.openmod.plugins.mod;

import dev.openmod.project.util.Node;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

public class OnAttack {
    public static void execute(IEventBus bus, Node node, RegistryObject<Item> itemObj) {
        bus.addListener((event) ->{
            if(event instanceof AttackEntityEvent) {
                Item item = itemObj.get();
                Player player = ((AttackEntityEvent) event).getEntity();
                if(!player.getItemInHand(player.getUsedItemHand()).getItem().equals(item)) return;
                Entity entity = ((AttackEntityEvent) event).getTarget();
                node.setOutputData("Tool <Item>", item);
                node.setOutputData("Level <Level>", player.getCommandSenderWorld());
                node.setOutputData("Player <Entity>", player);
                node.setOutputData("Entity <Entity>", entity);
                node.TriggerNext("connector");
            }
        });
    }
}
