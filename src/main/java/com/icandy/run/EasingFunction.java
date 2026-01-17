package com.icandy.run;

/**
 * Enumeration of easing functions for smooth transitions.
 * Each function provides a different animation curve for transitions.
 */
public enum EasingFunction {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    BOUNCE,
    ELASTIC;
    
    /**
     * Applies the easing function to a linear progress value.
     * 
     * @param t The linear progress from 0.0 to 1.0
     * @return The eased progress value
     */
    public float apply(float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        
        switch (this) {
            case LINEAR:
                return t;
                
            case EASE_IN:
                return t * t;
                
            case EASE_OUT:
                return 1.0f - (1.0f - t) * (1.0f - t);
                
            case EASE_IN_OUT:
                if (t < 0.5f) {
                    return 2.0f * t * t;
                } else {
                    return 1.0f - 2.0f * (1.0f - t) * (1.0f - t);
                }
                
            case BOUNCE:
                return bounce(t);
                
            case ELASTIC:
                return elastic(t);
                
            default:
                return t;
        }
    }
    
    /**
     * Bounce easing function with bouncing effect at the end.
     */
    private float bounce(float t) {
        if (t < 1.0f / 2.75f) {
            return 7.5625f * t * t;
        } else if (t < 2.0f / 2.75f) {
            t -= 1.5f / 2.75f;
            return 7.5625f * t * t + 0.75f;
        } else if (t < 2.5f / 2.75f) {
            t -= 2.25f / 2.75f;
            return 7.5625f * t * t + 0.9375f;
        } else {
            t -= 2.625f / 2.75f;
            return 7.5625f * t * t + 0.984375f;
        }
    }
    
    /**
     * Elastic easing function with spring-like effect.
     */
    private float elastic(float t) {
        if (t == 0.0f) return 0.0f;
        if (t == 1.0f) return 1.0f;
        
        float p = 0.3f;
        float s = p / 4.0f;
        
        return (float) (Math.pow(2.0, -10.0 * t) * Math.sin((t - s) * (2.0 * Math.PI) / p) + 1.0);
    }
}