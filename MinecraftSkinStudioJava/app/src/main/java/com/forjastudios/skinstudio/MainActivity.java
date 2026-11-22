package com.forjastudios.skinstudio;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    private SkinCanvasModel skinModel;
    private SkinViewport3DView viewport3D;
    private ColorCatalog colorCatalog;
    private TextView tvSelectedPart, tvSkinFormatBadge, tvColorInfo;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main);
        skinModel=new SkinCanvasModel(); colorCatalog=new ColorCatalog();
        viewport3D=findViewById(R.id.viewport3D); tvSelectedPart=findViewById(R.id.tvSelectedPart);
        tvSkinFormatBadge=findViewById(R.id.tvSkinFormatBadge); tvColorInfo=findViewById(R.id.tvColorInfo);
        if(viewport3D!=null) viewport3D.bindModel(skinModel);
        setupSkinTypeSelector(); setupPartSelector(); setupHundredsColorPalette();
        setupUniversalWheelButton(); setupTools(); refreshUiLabels(); updateColorLabel("Azul Cielo");
    }
    private void setupSkinTypeSelector() {
        RadioGroup rg=findViewById(R.id.rgSkinType); RadioButton classic=findViewById(R.id.rbClassic);
        if(classic!=null) classic.setChecked(true);
        if(rg!=null) rg.setOnCheckedChangeListener((group,id)->{
            skinModel.setCurrentSkinType(id==R.id.rbSlim?SkinType.SLIM:SkinType.CLASSIC);
            refreshUiLabels(); if(viewport3D!=null) viewport3D.invalidate();
        });
    }
    private void setupPartSelector() {
        bindPartButton(R.id.btnPartHead,BodyPart.HEAD); bindPartButton(R.id.btnPartHair,BodyPart.HAIR);
        bindPartButton(R.id.btnPartFace,BodyPart.FACE); bindPartButton(R.id.btnPartChest,BodyPart.CHEST);
        bindPartButton(R.id.btnPartArms,BodyPart.ARMS); bindPartButton(R.id.btnPartHands,BodyPart.HANDS);
        bindPartButton(R.id.btnPartLegs,BodyPart.LEGS); bindPartButton(R.id.btnPartShoes,BodyPart.SHOES);
    }
    private void bindPartButton(int id,BodyPart part) {
        Button btn=findViewById(id); if(btn!=null) btn.setOnClickListener(v->{
            skinModel.setSelectedPart(part); refreshUiLabels(); if(viewport3D!=null) viewport3D.invalidate();
        });
    }
    private void setupHundredsColorPalette() {
        HorizontalScrollView scroll=findViewById(R.id.hsvPaletteScroll); if(scroll==null)return;
        LinearLayout container=new LinearLayout(this); container.setOrientation(LinearLayout.HORIZONTAL);
        int size=(int)(34*getResources().getDisplayMetrics().density);
        int margin=(int)(3*getResources().getDisplayMetrics().density);
        for(ColorCatalog.Swatch swatch:colorCatalog.getSwatches()) {
            View chip=new View(this); LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(size,size);
            params.setMargins(margin,0,margin,0); chip.setLayoutParams(params); chip.setBackgroundColor(swatch.getColor());
            chip.setOnClickListener(v->{skinModel.setActiveColor(swatch.getColor());updateColorLabel(swatch.getName());
                if(viewport3D!=null)viewport3D.invalidate();}); container.addView(chip);
        }
        scroll.removeAllViews(); scroll.addView(container);
    }
    private void setupUniversalWheelButton() {
        Button btn=findViewById(R.id.btnUniversalWheel); if(btn!=null)btn.setOnClickListener(v->{
            UniversalColorWheelDialog dialog=new UniversalColorWheelDialog();
            dialog.show(this,skinModel.getActiveColor(),(picked,desc)->{
                skinModel.setActiveColor(picked);updateColorLabel(desc);
                if(viewport3D!=null)viewport3D.invalidate();
                Toast.makeText(this,"Color aplicado ("+desc+")",Toast.LENGTH_SHORT).show();
            });
        });
    }
    private void setupTools() {
        Button eraser=findViewById(R.id.btnEraser), reset=findViewById(R.id.btnResetCam);
        if(eraser!=null)eraser.setOnClickListener(v->{skinModel.setEraserMode(true);
            Toast.makeText(this,"Borrador activo",Toast.LENGTH_SHORT).show();});
        if(reset!=null)reset.setOnClickListener(v->{if(viewport3D!=null)viewport3D.resetCamera();});
    }
    private void updateColorLabel(String label) {
        if(tvColorInfo!=null){String hex=String.format("#%06X",(0xFFFFFF&skinModel.getActiveColor()));
            tvColorInfo.setText("Paleta ("+colorCatalog.count()+" tonos) • "+label+" "+hex);}
    }
    private void refreshUiLabels() {
        if(tvSelectedPart!=null)tvSelectedPart.setText("Parte activa: "+skinModel.getSelectedPart().getLabel());
        if(tvSkinFormatBadge!=null){SkinType type=skinModel.getCurrentSkinType();
            tvSkinFormatBadge.setText(type.getDisplayName()+" • "+type.getCompatibility());}
    }
}
