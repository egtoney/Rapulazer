package edu.thunderseethe.rapulazer;

import android.app.Activity;
import android.media.audiofx.Visualizer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends Activity {

    protected GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGLView = (GLSurfaceView)findViewById(R.id.visualizer_view);
        mGLView.setEGLContextClientVersion(3);
        mGLView.setPreserveEGLContextOnPause(true);
        mGLView.setRenderer(new VisualizerGLRenderer());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
}
