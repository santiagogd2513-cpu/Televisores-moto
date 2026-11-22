package com.forjastudios.skinstudio;
public enum SkinType {
    CLASSIC("Clásica (4px brazos)", "Java / Bedrock", 4),
    SLIM("Slim / Brazos corticos (3px)", "Java / Bedrock", 3);
    private final String displayName, compatibility;
    private final int armWidthPx;
    SkinType(String displayName, String compatibility, int armWidthPx) {
        this.displayName = displayName; this.compatibility = compatibility; this.armWidthPx = armWidthPx;
    }
    public String getDisplayName() { return displayName; }
    public String getCompatibility() { return compatibility; }
    public int getArmWidthPx() { return armWidthPx; }
}
