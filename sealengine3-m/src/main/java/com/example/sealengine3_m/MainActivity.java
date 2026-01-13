package com.example.sealengine3_m;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.sealengine3_m.databinding.ActivityMainBinding;
import com.nikitos.Engine;
import com.nikitos.MainRenderer;
import com.seal.gl_engine.AndroidLauncher;
import com.seal.gl_engine.AndroidLauncherParams;

public class MainActivity extends AppCompatActivity {
    Engine engine;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(uiOptions);
        AndroidLauncherParams androidLauncherParams = new AndroidLauncherParams(getApplicationContext())
                .setDebug(true)
                .setLandscape(true)
                .setStartPage(unused -> new MainRenderer())
                .setMSAA(true);

        AndroidLauncher androidLauncher = new AndroidLauncher(androidLauncherParams);
        engine = androidLauncher.getEngine();
        setContentView(androidLauncher.launch());
    }


    @Override
    protected void onPause() {
        super.onPause();
        engine.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        engine.onResume();
    }
}