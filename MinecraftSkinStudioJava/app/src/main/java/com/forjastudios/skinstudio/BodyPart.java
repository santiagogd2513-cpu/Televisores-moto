package com.forjastudios.skinstudio;
public enum BodyPart {
    HEAD("Cabeza", "HEAD"), HAIR("Pelo", "HEAD"), FACE("Cara", "HEAD"),
    CHEST("Pecho", "TORSO"), ARMS("Brazos", "ARMS"), HANDS("Manos", "ARMS"),
    LEGS("Piernas", "LEGS"), SHOES("Zapatos", "LEGS");
    private final String label, group;
    BodyPart(String label, String group) { this.label = label; this.group = group; }
    public String getLabel() { return label; }
    public String getGroup() { return group; }
}
