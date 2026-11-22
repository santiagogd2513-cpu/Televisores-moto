package com.forjastudios.skinstudio;
import android.graphics.Bitmap;
import android.graphics.Color;
public class SkinCanvasModel {
    public static final int SKIN_WIDTH=64, SKIN_HEIGHT=64;
    private final Bitmap skinBitmap;
    private SkinType currentSkinType;
    private BodyPart selectedPart;
    private int activeColor;
    private boolean overlayLayerEnabled, eraserMode;
    public SkinCanvasModel() {
        skinBitmap=Bitmap.createBitmap(SKIN_WIDTH,SKIN_HEIGHT,Bitmap.Config.ARGB_8888);
        currentSkinType=SkinType.CLASSIC; selectedPart=BodyPart.HEAD;
        activeColor=Color.parseColor("#29B6F6"); overlayLayerEnabled=true; eraserMode=false;
        initDefaultTemplate();
    }
    public void initDefaultTemplate() {
        skinBitmap.eraseColor(Color.TRANSPARENT);
        int baseBlue=Color.parseColor("#1565C0"), accentCyan=Color.parseColor("#4FC3F7");
        int torsoNavy=Color.parseColor("#0D47A1"), limbSlate=Color.parseColor("#1E3A5F");
        fillRect(8,0,16,8,accentCyan); fillRect(0,8,32,16,baseBlue);
        fillRect(20,16,28,20,baseBlue); fillRect(16,20,40,32,torsoNavy);
        fillRect(4,16,8,20,baseBlue); fillRect(0,20,16,32,limbSlate);
        fillRect(44,16,48,20,accentCyan); fillRect(40,20,56,32,baseBlue);
        fillRect(20,48,24,52,baseBlue); fillRect(16,52,32,64,limbSlate);
        fillRect(36,48,40,52,accentCyan); fillRect(32,52,48,64,baseBlue);
    }
    private void fillRect(int x0,int y0,int x1,int y1,int color) {
        for(int y=Math.max(0,y0);y<Math.min(SKIN_HEIGHT,y1);y++)
            for(int x=Math.max(0,x0);x<Math.min(SKIN_WIDTH,x1);x++) skinBitmap.setPixel(x,y,color);
    }
    public boolean paintPixel(int x,int y) {
        if(x<0||x>=SKIN_WIDTH||y<0||y>=SKIN_HEIGHT) return false;
        skinBitmap.setPixel(x,y,eraserMode?Color.TRANSPARENT:activeColor); return true;
    }
    public Bitmap getSkinBitmap(){return skinBitmap;}
    public SkinType getCurrentSkinType(){return currentSkinType;}
    public void setCurrentSkinType(SkinType v){if(v!=null)currentSkinType=v;}
    public BodyPart getSelectedPart(){return selectedPart;}
    public void setSelectedPart(BodyPart v){if(v!=null)selectedPart=v;}
    public int getActiveColor(){return activeColor;}
    public void setActiveColor(int v){activeColor=v;eraserMode=false;}
    public boolean isOverlayLayerEnabled(){return overlayLayerEnabled;}
    public void setOverlayLayerEnabled(boolean v){overlayLayerEnabled=v;}
    public boolean isEraserMode(){return eraserMode;}
    public void setEraserMode(boolean v){eraserMode=v;}
}
