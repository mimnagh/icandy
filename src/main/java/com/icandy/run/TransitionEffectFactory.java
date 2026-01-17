package com.icandy.run;

import com.icandy.common.ConfigurationManager;
import com.icandy.run.TransitionConfig.SlideDirection;
import com.icandy.run.TransitionConfig.ZoomMode;
import com.icandy.run.TransitionConfig.BlendMode;

/**
 * Factory class for creating transition effects based on configuration.
 * Handles the instantiation and configuration of different transition types.
 */
public class TransitionEffectFactory {
    
    /**
     * Creates a transition effect based on the effect name.
     * 
     * @param effectName The name of the effect ("fade", "slide", "zoom", "rotate", "morph")
     * @return The created transition effect, or null if invalid name
     */
    public static TransitionEffect createEffect(String effectName) {
        if (effectName == null) {
            return null;
        }
        
        switch (effectName.toLowerCase()) {
            case "fade":
                return new FadeTransition();
            case "slide":
                return new SlideTransition();
            case "zoom":
                return new ZoomTransition();
            case "rotate":
                return new RotateTransition();
            case "morph":
                return new MorphTransition();
            default:
                return null;
        }
    }
    
    /**
     * Creates a transition effect based on configuration manager settings.
     * 
     * @param config The configuration manager containing transition settings
     * @return The configured transition effect
     */
    public static TransitionEffect createEffect(ConfigurationManager config) {
        if (config == null) {
            return new FadeTransition(); // Default fallback
        }
        
        String effectName = config.getCurrentTransitionEffect();
        TransitionEffect effect = createEffect(effectName);
        
        if (effect == null) {
            effect = new FadeTransition(); // Default fallback
        }
        
        // Configure the effect based on its type
        configureEffect(effect, config);
        
        return effect;
    }
    
    /**
     * Creates a transition configuration based on configuration manager settings.
     * 
     * @param config The configuration manager containing transition settings
     * @return The configured transition config
     */
    public static TransitionConfig createConfig(ConfigurationManager config) {
        if (config == null) {
            return new TransitionConfig(); // Default config
        }
        
        TransitionConfig transitionConfig = new TransitionConfig();
        
        // Set basic parameters
        transitionConfig.setDuration(config.getTransitionDuration());
        transitionConfig.setEasingFunction(parseEasingFunction(config.getTransitionEasing()));
        transitionConfig.setEnableStagger(config.isStaggerEnabled());
        transitionConfig.setStaggerDelay(config.getStaggerDelay());
        
        // Set effect-specific parameters
        transitionConfig.setSlideDirection(parseSlideDirection(config.getSlideDirection()));
        transitionConfig.setZoomMode(parseZoomMode(config.getZoomMode()));
        transitionConfig.setRotationAngle(config.getRotationAngle());
        transitionConfig.setBlendMode(parseBlendMode(config.getBlendMode()));
        
        return transitionConfig;
    }
    
    /**
     * Configures a transition effect based on configuration manager settings.
     * 
     * @param effect The effect to configure
     * @param config The configuration manager
     */
    private static void configureEffect(TransitionEffect effect, ConfigurationManager config) {
        EasingFunction easing = parseEasingFunction(config.getTransitionEasing());
        
        if (effect instanceof FadeTransition) {
            FadeTransition fade = (FadeTransition) effect;
            fade.setEasingFunction(easing);
        } else if (effect instanceof SlideTransition) {
            SlideTransition slide = (SlideTransition) effect;
            slide.setEasingFunction(easing);
            slide.setSlideDirection(parseSlideDirection(config.getSlideDirection()));
        } else if (effect instanceof ZoomTransition) {
            ZoomTransition zoom = (ZoomTransition) effect;
            zoom.setEasingFunction(easing);
            zoom.setZoomMode(parseZoomMode(config.getZoomMode()));
        } else if (effect instanceof RotateTransition) {
            RotateTransition rotate = (RotateTransition) effect;
            rotate.setEasingFunction(easing);
            rotate.setRotationAngle(config.getRotationAngle());
        } else if (effect instanceof MorphTransition) {
            MorphTransition morph = (MorphTransition) effect;
            morph.setEasingFunction(easing);
            morph.setBlendMode(parseBlendMode(config.getBlendMode()));
            morph.setMorphIntensity(config.getMorphIntensity());
        }
    }
    
    /**
     * Parses an easing function string to enum.
     * 
     * @param easingString The easing function string
     * @return The corresponding EasingFunction enum
     */
    private static EasingFunction parseEasingFunction(String easingString) {
        if (easingString == null) {
            return EasingFunction.LINEAR;
        }
        
        switch (easingString.toLowerCase()) {
            case "linear":
                return EasingFunction.LINEAR;
            case "ease_in":
                return EasingFunction.EASE_IN;
            case "ease_out":
                return EasingFunction.EASE_OUT;
            case "ease_in_out":
                return EasingFunction.EASE_IN_OUT;
            case "bounce":
                return EasingFunction.BOUNCE;
            case "elastic":
                return EasingFunction.ELASTIC;
            default:
                return EasingFunction.LINEAR;
        }
    }
    
    /**
     * Parses a slide direction string to enum.
     * 
     * @param directionString The slide direction string
     * @return The corresponding SlideDirection enum
     */
    private static SlideDirection parseSlideDirection(String directionString) {
        if (directionString == null) {
            return SlideDirection.LEFT;
        }
        
        switch (directionString.toLowerCase()) {
            case "up":
                return SlideDirection.UP;
            case "down":
                return SlideDirection.DOWN;
            case "left":
                return SlideDirection.LEFT;
            case "right":
                return SlideDirection.RIGHT;
            case "up_left":
                return SlideDirection.UP_LEFT;
            case "up_right":
                return SlideDirection.UP_RIGHT;
            case "down_left":
                return SlideDirection.DOWN_LEFT;
            case "down_right":
                return SlideDirection.DOWN_RIGHT;
            default:
                return SlideDirection.LEFT;
        }
    }
    
    /**
     * Parses a zoom mode string to enum.
     * 
     * @param modeString The zoom mode string
     * @return The corresponding ZoomMode enum
     */
    private static ZoomMode parseZoomMode(String modeString) {
        if (modeString == null) {
            return ZoomMode.ZOOM_IN;
        }
        
        switch (modeString.toLowerCase()) {
            case "zoom_in":
                return ZoomMode.ZOOM_IN;
            case "zoom_out":
                return ZoomMode.ZOOM_OUT;
            case "zoom_both":
                return ZoomMode.ZOOM_BOTH;
            default:
                return ZoomMode.ZOOM_IN;
        }
    }
    
    /**
     * Parses a blend mode string to enum.
     * 
     * @param modeString The blend mode string
     * @return The corresponding BlendMode enum
     */
    private static BlendMode parseBlendMode(String modeString) {
        if (modeString == null) {
            return BlendMode.NORMAL;
        }
        
        switch (modeString.toLowerCase()) {
            case "normal":
                return BlendMode.NORMAL;
            case "multiply":
                return BlendMode.MULTIPLY;
            case "screen":
                return BlendMode.SCREEN;
            case "overlay":
                return BlendMode.OVERLAY;
            case "soft_light":
                return BlendMode.SOFT_LIGHT;
            case "hard_light":
                return BlendMode.HARD_LIGHT;
            case "color_dodge":
                return BlendMode.COLOR_DODGE;
            case "color_burn":
                return BlendMode.COLOR_BURN;
            default:
                return BlendMode.NORMAL;
        }
    }
}