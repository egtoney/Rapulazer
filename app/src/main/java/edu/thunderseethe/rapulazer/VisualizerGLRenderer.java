package edu.thunderseethe.rapulazer;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.thunderseethe.rapulazer.AudioLib.AudioFeatures;

/**
 * Created by thunderseethe on 4/15/17.
 */

public class VisualizerGLRenderer implements GLSurfaceView.Renderer {
    private DataRef<AudioFeatures> mDataRef;

    public VisualizerGLRenderer(DataRef<AudioFeatures> mDataRef) {
        this.mDataRef = mDataRef;
    }

    public void updateRef(DataRef<AudioFeatures> _dataRef) {
        mDataRef = _dataRef;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
    }
}