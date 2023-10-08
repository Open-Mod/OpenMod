package dev.openmod.project.custom;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.eventbus.api.Event;
import software.bernie.geckolib.core.animation.AnimationState;

public class CustomEvent extends Event {
    public static class AnimationInit extends CustomEvent
    {
        private AnimationState animationState;
        private Object item;
        public AnimationInit(AnimationState animationState, Object item)
        {
            super();
            this.animationState = animationState;
            this.item = item;
        }

        public AnimationState getAnimationState() {
            return animationState;
        }

        public Object get() {
            return item;
        }
    }

    public static class MobGoalsInit extends CustomEvent
    {
        private Mob mob;
        public MobGoalsInit(Mob mob)
        {
            super();
            this.mob = mob;
        }

        public Mob get() {
            return mob;
        }
    }

    public static class MobServerAiStep extends CustomEvent
    {
        private Mob mob;
        public MobServerAiStep(Mob mob)
        {
            super();
            this.mob = mob;
        }

        public Mob get() {
            return mob;
        }
    }

    public static class MobTick extends CustomEvent
    {
        private Mob mob;
        public MobTick(Mob mob)
        {
            super();
            this.mob = mob;
        }

        public Mob get() {
            return mob;
        }

    }
}
