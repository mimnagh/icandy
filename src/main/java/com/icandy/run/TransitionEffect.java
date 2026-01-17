package com.icandy.run;

/**
 * Interface for transition effects that animate changes between image states.
 * Transition effects handle the visual animation when images are swapped or repositioned.
 */
public interface TransitionEffect {
    
    /**
     * Starts a transition from one image state to another.
     * 
     * @param from The starting image state
     * @param to The target image state
     * @param duration The duration of the transition in milliseconds
     */
    void startTransition(ImageState from, ImageState to, float duration);
    
    /**
     * Updates the transition and returns the current interpolated state.
     * 
     * @param progress The transition progress from 0.0 (start) to 1.0 (complete)
     * @return The current interpolated image state
     */
    ImageState updateTransition(float progress);
    
    /**
     * Checks if the transition is complete.
     * 
     * @return true if the transition has finished, false otherwise
     */
    boolean isComplete();
    
    /**
     * Gets the name of this transition effect.
     * 
     * @return The effect name for identification
     */
    String getName();
}