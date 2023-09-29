package dev.openmod.project.custom;

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
}
