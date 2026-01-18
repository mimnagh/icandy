package com.icandy.run;

/**
 * Represents a single particle in a particle system.
 * Particles have position, velocity, size, color, and lifetime properties.
 */
public class Particle {
    
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float size;
    private int particleColor;
    private float lifetime; // Total lifetime in milliseconds
    private float age;      // Current age in milliseconds
    private float opacity;
    private VisualEffectsConfig.ParticleType type;
    
    // Physics properties
    private float gravity = 0.0f;
    private float wind = 0.0f;
    private float friction = 0.98f;
    
    /**
     * Creates a new particle with specified properties.
     * 
     * @param x Initial x position
     * @param y Initial y position
     * @param velocityX Initial x velocity
     * @param velocityY Initial y velocity
     * @param size Particle size
     * @param particleColor Particle color
     * @param lifetime Total lifetime in milliseconds
     * @param type Particle type
     */
    public Particle(float x, float y, float velocityX, float velocityY, float size, int particleColor, float lifetime, VisualEffectsConfig.ParticleType type) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.size = size;
        this.particleColor = particleColor;
        this.lifetime = lifetime;
        this.age = 0.0f;
        this.opacity = 1.0f;
        this.type = type;
        
        // Set physics properties based on particle type
        switch (type) {
            case SMOKE:
                this.gravity = -0.1f; // Smoke rises
                this.friction = 0.95f;
                break;
            case FIRE:
                this.gravity = -0.2f; // Fire rises faster
                this.friction = 0.92f;
                break;
            case SNOW:
                this.gravity = 0.05f; // Snow falls slowly
                this.wind = 0.02f;
                this.friction = 0.99f;
                break;
            case SPARKLES:
                this.gravity = 0.1f; // Sparkles fall
                this.friction = 0.96f;
                break;
            case STARS:
                this.gravity = 0.0f; // Stars don't fall
                this.friction = 1.0f; // No friction
                break;
            case BUBBLES:
                this.gravity = -0.05f; // Bubbles rise slowly
                this.friction = 0.98f;
                break;
        }
    }
    
    /**
     * Updates the particle's position, velocity, and age.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    public void update(float deltaTime) {
        // Update age
        age += deltaTime;
        
        // Update velocity with physics
        velocityY += gravity * deltaTime;
        velocityX += wind * deltaTime;
        
        // Apply friction
        velocityX *= friction;
        velocityY *= friction;
        
        // Update position
        x += velocityX * deltaTime / 16.67f; // Normalize to ~60fps
        y += velocityY * deltaTime / 16.67f;
        
        // Update opacity based on age (fade out over time)
        float ageRatio = age / lifetime;
        if (ageRatio < 0.8f) {
            opacity = 1.0f;
        } else {
            // Fade out in the last 20% of lifetime
            opacity = 1.0f - ((ageRatio - 0.8f) / 0.2f);
        }
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        
        // Update size based on type and age
        switch (type) {
            case SMOKE:
                // Smoke grows larger over time
                size = size * (1.0f + ageRatio * 0.5f);
                break;
            case FIRE:
                // Fire shrinks over time
                size = size * (1.0f - ageRatio * 0.3f);
                break;
            case BUBBLES:
                // Bubbles grow slightly
                size = size * (1.0f + ageRatio * 0.2f);
                break;
            case SPARKLES:
                // Sparkles maintain constant size
                break;
            case STARS:
                // Stars maintain constant size
                break;
            case SNOW:
                // Snow maintains constant size
                break;
        }
    }
    
    /**
     * Checks if the particle is dead (exceeded its lifetime).
     * 
     * @return true if the particle is dead, false otherwise
     */
    public boolean isDead() {
        return age >= lifetime;
    }
    
    /**
     * Gets the current color with opacity applied.
     * 
     * @return The color with alpha channel adjusted for current opacity
     */
    public int getCurrentColor() {
        int alpha = (int) (opacity * 255);
        return (particleColor & 0x00FFFFFF) | (alpha << 24);
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getVelocityX() { return velocityX; }
    public float getVelocityY() { return velocityY; }
    public float getSize() { return size; }
    public int getColor() { return particleColor; }
    public float getLifetime() { return lifetime; }
    public float getAge() { return age; }
    public float getOpacity() { return opacity; }
    public VisualEffectsConfig.ParticleType getType() { return type; }
    public float getGravity() { return gravity; }
    public float getWind() { return wind; }
    public float getFriction() { return friction; }
    
    // Setters
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void setVelocity(float velocityX, float velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }
    
    public void setSize(float size) {
        this.size = Math.max(0.1f, size);
    }
    
    public void setColor(int particleColor) {
        this.particleColor = particleColor;
    }
    
    public void setPhysics(float gravity, float wind, float friction) {
        this.gravity = gravity;
        this.wind = wind;
        this.friction = Math.max(0.0f, Math.min(1.0f, friction));
    }
    
    /**
     * Resets the particle's age to 0, effectively restarting its lifetime.
     */
    public void reset() {
        this.age = 0.0f;
        this.opacity = 1.0f;
    }
    
    @Override
    public String toString() {
        return String.format("Particle{type=%s, pos=(%.1f,%.1f), vel=(%.1f,%.1f), size=%.1f, age=%.0f/%.0f, opacity=%.2f}",
                type, x, y, velocityX, velocityY, size, age, lifetime, opacity);
    }
}