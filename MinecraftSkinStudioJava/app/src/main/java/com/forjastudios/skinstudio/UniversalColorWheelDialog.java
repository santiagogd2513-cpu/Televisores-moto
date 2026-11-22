package com.forjastudios.skinstudio;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
public class UniversalColorWheelDialog {
    public interface OnUniversalColorSelectedListener { void onColorPicked(int color,String description); }
    private float currentHue=200f,currentSat=.85f,currentVal=.95f;
    private String blendModeLabel="Directo";
    public void show(Context context,int initialColor,OnUniversalColorSelectedListener listener){
        float[] hsv=new float[3];Color.colorToHSV(initialColor,hsv);
        currentHue=hsv[0];currentSat=Math.max(.05f,hsv[1]);currentVal=Math.max(.10f,hsv[2]);blendModeLabel="Rueda Universal";
        LinearLayout root=new LinearLayout(context);root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#060D17"));
        int pad=(int)(14*context.getResources().getDisplayMetrics().density);root.setPadding(pad,pad,pad,pad);
        TextView title=new TextView(context);title.setText("Rueda Universal & Combinaciones");
        title.setTextColor(Color.parseColor("#90CAF9"));title.setTextSize(16f);root.addView(title);
        TextView preview=new TextView(context);preview.setTextColor(Color.parseColor("#E3F2FD"));
        preview.setTextSize(12f);preview.setPadding(0,pad/2,0,pad/2);
        ColorWheelView wheel=new ColorWheelView(context,(h,s)->{currentHue=h;currentSat=s;blendModeLabel="Rueda HSV";updatePreview(preview,initialColor);});
        int wheelSize=(int)(220*context.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(wheelSize,wheelSize);
        wp.gravity=android.view.Gravity.CENTER_HORIZONTAL;wheel.setLayoutParams(wp);root.addView(wheel);
        TextView valLabel=new TextView(context);valLabel.setText("Brillo / Luminosidad");
        valLabel.setTextColor(Color.parseColor("#81D4FA"));valLabel.setTextSize(12f);root.addView(valLabel);
        SeekBar value=new SeekBar(context);value.setMax(100);value.setProgress((int)(currentVal*100f));
        value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean fromUser){
                currentVal=Math.max(.05f,progress/100f);wheel.setValue(currentVal);updatePreview(preview,initialColor);
            }
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){}
        });root.addView(value);root.addView(preview);
        LinearLayout row1=new LinearLayout(context);row1.setOrientation(LinearLayout.HORIZONTAL);
        Button complementary=makeComboButton(context,"Complemento");
        complementary.setOnClickListener(v->{currentHue=(currentHue+180f)%360f;blendModeLabel="Complementario";wheel.setHueSat(currentHue,currentSat);updatePreview(preview,initialColor);});
        Button triadic=makeComboButton(context,"Triádico");
        triadic.setOnClickListener(v->{currentHue=(currentHue+120f)%360f;blendModeLabel="Triádico";wheel.setHueSat(currentHue,currentSat);updatePreview(preview,initialColor);});
        Button analogous=makeComboButton(context,"Análogo");
        analogous.setOnClickListener(v->{currentHue=(currentHue+30f)%360f;blendModeLabel="Análogo";wheel.setHueSat(currentHue,currentSat);updatePreview(preview,initialColor);});
        row1.addView(complementary);row1.addView(triadic);row1.addView(analogous);root.addView(row1);
        LinearLayout row2=new LinearLayout(context);row2.setOrientation(LinearLayout.HORIZONTAL);row2.setPadding(0,pad/2,0,0);
        Button mix=makeComboButton(context,"Mezclar 50/50");
        mix.setOnClickListener(v->{int mixed=blendColors(getComposedColor(),initialColor,.5f);float[] m=new float[3];Color.colorToHSV(mixed,m);
            currentHue=m[0];currentSat=m[1];currentVal=m[2];blendModeLabel="Mezcla 50/50";wheel.setValue(currentVal);wheel.setHueSat(currentHue,currentSat);updatePreview(preview,initialColor);});
        Button invert=makeComboButton(context,"Invertir RGB");
        invert.setOnClickListener(v->{int c=getComposedColor();int inv=Color.rgb(255-Color.red(c),255-Color.green(c),255-Color.blue(c));
            float[] a=new float[3];Color.colorToHSV(inv,a);currentHue=a[0];currentSat=a[1];currentVal=a[2];
            blendModeLabel="Invertido";wheel.setValue(currentVal);wheel.setHueSat(currentHue,currentSat);updatePreview(preview,initialColor);});
        row2.addView(mix);row2.addView(invert);root.addView(row2);
        wheel.setValue(currentVal);wheel.setHueSat(currentHue,currentSat);updatePreview(preview,initialColor);
        new AlertDialog.Builder(context,android.R.style.Theme_DeviceDefault_Dialog_Alert).setView(root)
            .setPositiveButton("Usar Color",(d,w)->{if(listener!=null)listener.onColorPicked(getComposedColor(),blendModeLabel);})
            .setNegativeButton("Cancelar",null).show();
    }
    private Button makeComboButton(Context c,String text){
        Button b=new Button(c);b.setText(text);b.setTextSize(10f);b.setTextColor(Color.parseColor("#E3F2FD"));
        b.setBackgroundColor(Color.parseColor("#1565C0"));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f);
        p.setMargins(3,0,3,0);b.setLayoutParams(p);return b;
    }
    private int getComposedColor(){return Color.HSVToColor(new float[]{currentHue,currentSat,currentVal});}
    private int blendColors(int a,int b,float ratio){
        float inv=1f-ratio;int r=(int)(Color.red(a)*ratio+Color.red(b)*inv);
        int g=(int)(Color.green(a)*ratio+Color.green(b)*inv),bl=(int)(Color.blue(a)*ratio+Color.blue(b)*inv);
        return Color.rgb(Math.max(0,Math.min(255,r)),Math.max(0,Math.min(255,g)),Math.max(0,Math.min(255,bl)));
    }
    private void updatePreview(TextView tv,int initial){
        int c=getComposedColor();String hex=String.format("#%06X",0xFFFFFF&c);tv.setTextColor(c);
        tv.setText("Modo: "+blendModeLabel+" | HEX: "+hex+" | H:"+Math.round(currentHue)+"° S:"+Math.round(currentSat*100)+"% V:"+Math.round(currentVal*100)+"%");
    }
    private static class ColorWheelView extends View {
        interface OnWheelPick { void onPick(float hue,float sat); }
        private final OnWheelPick callback;private final Paint markerPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private Bitmap wheelBitmap;private float val=.95f,selectedHue=200f,selectedSat=.85f;
        ColorWheelView(Context c,OnWheelPick cb){super(c);callback=cb;markerPaint.setStyle(Paint.Style.STROKE);markerPaint.setStrokeWidth(4f);markerPaint.setColor(Color.WHITE);}
        void setValue(float v){val=v;rebuildBitmap();invalidate();}
        void setHueSat(float h,float s){selectedHue=h;selectedSat=s;invalidate();}
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);rebuildBitmap();}
        private void rebuildBitmap(){
            int size=Math.min(getWidth(),getHeight());if(size<=4)return;
            Bitmap bmp=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);float radius=size/2f,cx=radius,cy=radius;
            for(int y=0;y<size;y++)for(int x=0;x<size;x++){
                float dx=x-cx,dy=y-cy,dist=(float)Math.sqrt(dx*dx+dy*dy);
                if(dist<=radius){float sat=Math.min(1f,dist/radius);float angle=(float)Math.toDegrees(Math.atan2(dy,dx));
                    float hue=(angle+360f)%360f;bmp.setPixel(x,y,Color.HSVToColor(new float[]{hue,sat,val}));}
                else bmp.setPixel(x,y,Color.TRANSPARENT);
            }wheelBitmap=bmp;
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);if(wheelBitmap!=null)c.drawBitmap(wheelBitmap,0f,0f,null);
            int size=Math.min(getWidth(),getHeight());if(size>4){float radius=size/2f,cx=radius,cy=radius;
                double rad=Math.toRadians(selectedHue);float mx=cx+(float)(Math.cos(rad)*selectedSat*radius);
                float my=cy+(float)(Math.sin(rad)*selectedSat*radius);c.drawCircle(mx,my,10f,markerPaint);}
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e==null)return false;int size=Math.min(getWidth(),getHeight());if(size<=4)return false;
            float radius=size/2f,dx=e.getX()-radius,dy=e.getY()-radius;
            float dist=(float)Math.sqrt(dx*dx+dy*dy),sat=Math.min(1f,dist/radius);
            float angle=(float)Math.toDegrees(Math.atan2(dy,dx)),hue=(angle+360f)%360f;
            selectedHue=hue;selectedSat=sat;invalidate();if(callback!=null)callback.onPick(hue,sat);return true;
        }
    }
}
