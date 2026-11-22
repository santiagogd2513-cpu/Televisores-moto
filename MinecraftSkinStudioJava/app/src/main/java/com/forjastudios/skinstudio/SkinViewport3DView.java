package com.forjastudios.skinstudio;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
public class SkinViewport3DView extends View {
    private final Paint gridPaint=new Paint(Paint.ANTI_ALIAS_FLAG), outlinePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint=new Paint(Paint.ANTI_ALIAS_FLAG), facePaint=new Paint();
    private SkinCanvasModel model;
    private float yawDeg=32f,pitchDeg=16f,zoom=1f,lastTouchX,lastTouchY;
    public SkinViewport3DView(Context c){super(c);init();}
    public SkinViewport3DView(Context c,AttributeSet a){super(c,a);init();}
    public SkinViewport3DView(Context c,AttributeSet a,int d){super(c,a,d);init();}
    private void init(){
        setClickable(true);gridPaint.setColor(Color.parseColor("#173459"));gridPaint.setStrokeWidth(1.5f);gridPaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(Color.parseColor("#64B5F6"));outlinePaint.setStrokeWidth(2.5f);outlinePaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setColor(Color.parseColor("#29B6F6"));highlightPaint.setStrokeWidth(3.5f);highlightPaint.setStyle(Paint.Style.STROKE);
        facePaint.setAntiAlias(false);facePaint.setFilterBitmap(false);
    }
    public void bindModel(SkinCanvasModel m){model=m;invalidate();}
    public void resetCamera(){yawDeg=32f;pitchDeg=16f;zoom=1f;invalidate();}
    public void adjustZoom(float delta){zoom=Math.max(.65f,Math.min(1.65f,zoom+delta));invalidate();}
    @Override public boolean onTouchEvent(MotionEvent e){
        if(e==null)return false;
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:lastTouchX=e.getX();lastTouchY=e.getY();return true;
            case MotionEvent.ACTION_MOVE:
                float dx=e.getX()-lastTouchX,dy=e.getY()-lastTouchY;
                yawDeg+=dx*.55f;pitchDeg=Math.max(-65f,Math.min(65f,pitchDeg+dy*.45f));
                lastTouchX=e.getX();lastTouchY=e.getY();invalidate();return true;
            default:return super.onTouchEvent(e);
        }
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);canvas.drawColor(Color.parseColor("#07111F"));
        int w=getWidth(),h=getHeight();if(w<=0||h<=0)return;drawFloorGrid(canvas,w,h);
        float scale=Math.min(w,h)/42f*zoom,cx=w*.5f,cy=h*.52f;
        boolean slim=model!=null&&model.getCurrentSkinType()==SkinType.SLIM;
        float armW=slim?3f:4f;BodyPart selected=model!=null?model.getSelectedPart():BodyPart.HEAD;
        String group=selected!=null?selected.getGroup():"HEAD";Bitmap bmp=model!=null?model.getSkinBitmap():null;
        drawBox3D(canvas,cx,cy,scale,-4,4,12,20,-4,4,"HEAD".equals(group),bmp,8,8,8,0);
        drawBox3D(canvas,cx,cy,scale,-4,4,0,12,-2,2,"TORSO".equals(group),bmp,8,12,4,16);
        drawBox3D(canvas,cx,cy,scale,-4-armW,-4,0,12,-2,2,"ARMS".equals(group),bmp,(int)armW,12,4,40);
        drawBox3D(canvas,cx,cy,scale,4,4+armW,0,12,-2,2,"ARMS".equals(group),bmp,(int)armW,12,4,32);
        drawBox3D(canvas,cx,cy,scale,-4,0,-12,0,-2,2,"LEGS".equals(group),bmp,4,12,4,0);
        drawBox3D(canvas,cx,cy,scale,0,4,-12,0,-2,2,"LEGS".equals(group),bmp,4,12,4,16);
    }
    private void drawFloorGrid(Canvas c,int w,int h){
        float step=36f;for(float x=0;x<=w;x+=step)c.drawLine(x,h*.45f,x,h,gridPaint);
        for(float y=h*.45f;y<=h;y+=step)c.drawLine(0,y,w,y,gridPaint);
    }
    private void drawBox3D(Canvas canvas,float cx,float cy,float scale,float x0,float x1,float y0,float y1,float z0,float z1,
                           boolean selected,Bitmap bmp,int uvW,int uvH,int uvD,int originX){
        float[][] corners={{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},{x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}};
        float[][] proj=new float[8][3];double yaw=Math.toRadians(yawDeg),pitch=Math.toRadians(pitchDeg);
        double cyaw=Math.cos(yaw),syaw=Math.sin(yaw),cp=Math.cos(pitch),sp=Math.sin(pitch);
        for(int i=0;i<8;i++){
            float x=corners[i][0],y=corners[i][1],z=corners[i][2];
            float rx=(float)(x*cyaw-z*syaw),rz=(float)(x*syaw+z*cyaw);
            float ry=(float)(y*cp-rz*sp),fz=(float)(y*sp+rz*cp);
            proj[i][0]=cx+rx*scale;proj[i][1]=cy-ry*scale;proj[i][2]=fz;
        }
        int[][] faces={{4,5,6,7},{1,2,6,5},{3,2,6,7},{0,3,7,4}};
        for(int f=0;f<faces.length;f++){
            int[] idx=faces[f];Path path=new Path();path.moveTo(proj[idx[0]][0],proj[idx[0]][1]);
            path.lineTo(proj[idx[1]][0],proj[idx[1]][1]);path.lineTo(proj[idx[2]][0],proj[idx[2]][1]);
            path.lineTo(proj[idx[3]][0],proj[idx[3]][1]);path.close();
            facePaint.setStyle(Paint.Style.FILL);facePaint.setColor(sampleFaceColor(bmp,originX,uvW,uvH,uvD,f));
            canvas.drawPath(path,facePaint);canvas.drawPath(path,selected?highlightPaint:outlinePaint);
        }
    }
    private int sampleFaceColor(Bitmap bmp,int originX,int uvW,int uvH,int uvD,int face){
        if(bmp==null)return Color.parseColor("#1E88E5");
        int u=originX+uvD+uvW/2,v=uvD+uvH/2;
        if(face==1)u=originX+uvD*2+uvW+uvW/2;
        else if(face==2){u=originX+uvD+uvW/2;v=uvD/2;}
        else if(face==3)u=originX+uvD/2;
        int x=Math.max(0,Math.min(bmp.getWidth()-1,u)),y=Math.max(0,Math.min(bmp.getHeight()-1,v));
        int c=bmp.getPixel(x,y);if(Color.alpha(c)<20)return Color.parseColor("#1565C0");
        return Color.argb(255,Color.red(c),Color.green(c),Color.blue(c));
    }
}
