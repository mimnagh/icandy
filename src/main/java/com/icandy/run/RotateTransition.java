package com.icandy.run;

/**
 * Rotate transition effect that rotates images during transitions with configurable angle.
 * Supports rotation direction, center point, and combination with scale and fade effects.
 */
public class RotateTransition implements TransitionEffect {
    private ImageState startState;
    private ImageState targetState;
    private float duration;
    private EasingFunction easingFunction;
    private float rotationAngle;
    private boolean clockwise;
    private float rotationCenterX;
    private float rotationCenterY;
    private boolean combineScale;
    private boolean combineFade;
    private boolean isComplete;
    
    /**
     * Creates a new rotate transition with default settings.
     */
    public RotateTransition() {
        this((float) Math.PI / 2, true, EasingFunction.EASE_IN_OUT); // 90 degrees clockwise
    }
    
    /**
     * Creates a new rotate transition with specified angle and direction.
     * 
     * @param rotationAngle The rotation angle in radians
     * @param clockwise Whether to rotate clockwise
     * @param easingFunction The easing function to use
     */
    public RotateTransition(float rotationAngle, boolean clockwise, EasingFunction easingFunction) {
        this.rotationAngle = rotationAngle;
        this.clockwise = clockwise;
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.EASE_IN_OUT;
        this.rotationCenterX = 0.5f; // Center of image (0.0 to 1.0)
        this.rotationCenterY = 0.5f;
        this.combineScale = true;
        this.combineFade = true;
        this.isComplete = false;
    }
    
    /**
     * Sets the rotation center point as a fraction of the image dimensions.
     * 
     * @param centerX X center (0.0 = left edge, 1.0 = right edge)
     * @param centerY Y center (0.0 = top edge, 1.0 = bottom edge)
     */
    public void setRotationCenter(float centerX, float centerY) {
        this.rotationCenterX = Math.max(0.0f, Math.min(1.0f, centerX));
        this.rotationCenterY = Math.max(0.0f, Math.min(1.0f, centerY));
    }
    
    @Override
    public void startTransition(ImageState from, ImageState to, float duration) {
        this.targetState = to != null ? new ImageState(to) : new ImageState();
        this.duration = Math.max(1.0f, duration);
        this.isComplete = false;
        
        // Calculate the starting state with initial rotation
        this.startState = calculateStartState(targetState);
    }
    
    /**
     * Calculates the starting state with initial rotation and effects.
     * 
     * @param targetState The final target state
     * @return The calculated starting state
     */
    private ImageState calculateStartState(ImageState targetState) {
        ImageState startState = new ImageState(targetState);
        
        // Set initial rotation
        float initialRotation = clockwise ? -rotationAngle : rotationAngle;
        startState.setRotation(targetState.getRotation() + initialRotation);
        
        // Combine with scale effect if enabled
        if (combineScale) {
            startState.setScaleX(targetState.getScaleX() * 0.5f);
            startState.setScaleY(targetState.getScaleY() * 0.5f);
        }
        
        // Combine with fade effect if enabled
        if (combineFade) {
            startState.setOpacity(0.0f);
        }
        
        return startState;
    }
    
    @Override
    public ImageState updateTransition(float progress) {
        if (startState == null || targetState == null) {
            return new ImageState();
        }
        
        // Clamp progress to valid range
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        
        // Apply easing function
        float easedProgress = easingFunction.apply(progress);
        
        // Create interpolated state
        ImageState currentState = startState.lerp(targetState, easedProgress);
        
        // Apply rotation with potential overshoot for more dynamic effect
        float rotationProgress = easedProgress;
        if (easedProgress > 0.8f) {
            // Add slight overshoot in the last 20% for more dynamic feel
            float overshootPhase = (easedProgress - 0.8f) / 0.2f;
            float overshoot = (float) Math.sin(overshootPhase * Math.PI) * 0.1f;
            rotationProgress = easedProgress + overshoot;
        }
        
        // Calculate current rotation
        float currentRotation = startState.getRotation() + 
                               (targetState.getRotation() - startState.getRotation()) * rotationProgress;
        currentState.setRotation(currentRotation);
        
        // Adjust position to maintain rotation center point
        adjustPositionForRotationCenter(currentState, targetState, rotationProgress);
        
        // Mark as complete when progress reaches 1.0
        if (progress >= 1.0f) {
            isComplete = true;
        }
        
        return currentState;
    }
    
    /**
     * Adjusts the position to maintain the specified rotation center point.
     * This is a simplified approach - in a full implementation, you'd use proper
     * rotation matrix calculations.
     * 
     * @param currentState The state to adjust
     * @param targetState The target state for reference
     * @param rotationProgress The current rotation progress
     */
    private void adjustPositionForRotationCenter(ImageState currentState, ImageState targetState, float rotationProgress) {
        // For simplicity, we'll just apply a small offset based on rotation
        // In a full implementation, you'd calculate the actual rotated position
        float rotationOffset = (float) Math.sin(currentState.getRotation()) * 5.0f;
        
        // Apply small positional adjustment to simulate rotation around center
        currentState.setX(currentState.getX() + rotationOffset * (0.5f - rotationCenterX));
        currentState.setY(currentState.getY() + rotationOffset * (0.5f - rotationCenterY));
    }
    
    @Override
    public boolean isComplete() {
        return isComplete;
    }
    
    @Override
    public String getName() {
        return "Rotate";
    }
    
    /**
     * Sets the rotation angle in radians.
     * 
     * @param rotationAngle The rotation angle in radians
     */
    public void setRotationAngle(float rotationAngle) {
        this.rotationAngle = rotationAngle;
    }
    
    /**
     * Gets the current rotation angle in radians.
     * 
     * @return The rotation angle in radians
     */
    public float getRotationAngle() {
        return rotationAngle;
    }
    
    /**
     * Sets the rotation direction.
     * 
     * @param clockwise Whether to rotate clockwise
     */
    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }
    
    /**
     * Checks if rotation is clockwise.
     * 
     * @return true if rotating clockwise
     */
    public boolean isClockwise() {
        return clockwise;
    }
    
    /**
     * Sets the easing function for this rotate transition.
     * 
     * @param easingFunction The new easing function
     */
    public void setEasingFunction(EasingFunction easingFunction) {
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.LINEAR;
    }
    
    /**
     * Gets the current easing function.
     * 
     * @return The current easing function
     */
    public EasingFunction getEasingFunction() {
        return easingFunction;
    }
    
    /**
     * Sets whether to combine rotation with scale effect.
     * 
     * @param combineScale Whether to combine with scale
     */
    public void setCombineScale(boolean combineScale) {
        this.combineScale = combineScale;
    }
    
    /**
     * Checks if rotation is combined with scale effect.
     * 
     * @return true if combined with scale
     */
    public boolean isCombineScale() {
        return combineScale;
    }
    
    /**
     * Sets whether to combine rotation with fade effect.
     * 
     * @param combineFade Whether to combine with fade
     */
    public void setCombineFade(boolean combineFade) {
        this.combineFade = combineFade;
    }
    
    /**
     * Checks if rotation is combined with fade effect.
     * 
     * @return true if combined with fade
     */
    public boolean isCombineFade() {
        return combineFade;
    }
    
    @Override
    public String toString() {
        return String.format("RotateTransition{angle=%.2f, clockwise=%s, easing=%s, scale=%s, fade=%s, complete=%s}",
                rotationAngle, clockwise, easingFunction, combineScale, combineFade, isComplete);
    }
}