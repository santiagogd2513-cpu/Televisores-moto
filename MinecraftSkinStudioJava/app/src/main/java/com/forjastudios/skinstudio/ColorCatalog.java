package com.forjastudios.skinstudio;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;
public class ColorCatalog {
    public static class Swatch {
        private final int color; private final String name;
        public Swatch(int color, String name) { this.color = color; this.name = name; }
        public int getColor() { return color; }
        public String getName() { return name; }
    }
    private final List<Swatch> swatches = new ArrayList<>();
    public ColorCatalog() { buildHundredsOfShades(); }
    private void buildHundredsOfShades() {
        float[] saturations = {1.0f,0.85f,0.65f,0.45f,0.95f,0.75f,0.55f,0.30f};
        float[] values = {1.0f,0.90f,0.80f,0.95f,0.65f,0.50f,0.35f,0.85f};
        int idx = 1;
        for (int deg=0; deg<360; deg+=10) for (int i=0;i<saturations.length;i++) {
            int c=Color.HSVToColor(new float[]{deg,saturations[i],values[i]});
            swatches.add(new Swatch(c,"Tono #"+idx+" ("+deg+"°)")); idx++;
        }
        int extraIdx=1;
        for (int v=5;v<=100;v+=3) {
            int c=Color.HSVToColor(new float[]{210f,0.15f,v/100f});
            swatches.add(new Swatch(c,"Escala Azul/Gris #"+extraIdx)); extraIdx++;
        }
    }
    public List<Swatch> getSwatches() { return swatches; }
    public int count() { return swatches.size(); }
}
