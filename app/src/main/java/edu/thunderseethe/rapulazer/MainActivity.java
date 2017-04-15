package edu.thunderseethe.rapulazer;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GLSurfaceView mGLView = (GLSurfaceView)findViewById(R.id.visualizer_view);
        mGLView.setEGLContextClientVersion(3);
        mGLView.setPreserveEGLContextOnPause(true);
        mGLView.setRenderer(new VisualizerGLRenderer());
    }


}
