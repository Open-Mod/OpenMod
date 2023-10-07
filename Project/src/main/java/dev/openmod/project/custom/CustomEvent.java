package dev.openmod.project.custom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraftforge.eventbus.api.Event;
import software.bernie.geckolib.core.animation.AnimationState;

public class CustomEvent extends Event {
    public static class AnimationInit extends CustomEvent
    {
        private AnimationState animationState;
        private String name;
        public AnimationInit(AnimationState animationState, String name)
        {
            super();
            this.animationState = animationState;
            this.name = name;
        }

        public AnimationState getAnimationState() {
            return animationState;
        }

        public String getName() {
            return name;
        }
    }

    public static class MobGoalsInit extends CustomEvent
    {
        private Mob mob;
        private String name;
        public MobGoalsInit(Mob mob, String name)
        {
            super();
            this.mob = mob;
            this.name = name;
        }

        public Mob getMob() {
            return mob;
        }


        public String getName() {
            return name;
        }
    }
}
