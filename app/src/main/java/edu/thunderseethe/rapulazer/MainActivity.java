package edu.thunderseethe.rapulazer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.media.MediaMetadataRetriever;
import java.util.ArrayList;
import android.database.Cursor;
import android.provider.MediaStore;
import android.net.Uri;
import android.media.AudioManager;

import java.io.File;

import edu.thunderseethe.rapulazer.AudioLib.AudioFeatures;

public class MainActivity extends Activity {

    protected GLSurfaceView mGLView;
    private boolean mBound = false;
    private VisualizerGLRenderer mRenderer;
    private AudioVisualizerService.AudioFeatureBinder mService;
    public static final int BUCKETS = 12;

    protected static String prettyPrintByteArray(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(byte b : bytes) {
            sb.append(" '");
            sb.append(Integer.toBinaryString((int)b));
            sb.append("',");
        }
        sb.append(" ]");
        return sb.toString();
    }

    public static String prettyPrintFloatArray(float[] floats) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(float f : floats) {
            sb.append(" '");
            sb.append(f);
            sb.append("',");
        }
        sb.append(" ]");
        return sb.toString();
    }

    public static String prettyPrintIntArray(int[] ints) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int f : ints) {
            sb.append(" '");
            sb.append(f);
            sb.append("',");
        }
        sb.append(" ]");
        return sb.toString();
    }

    public static String prettyPrintDoubleArray(double[] doubles) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(double f : doubles) {
            sb.append(" '");
            sb.append(f);
            sb.append("',");
        }
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRenderer = new VisualizerGLRenderer(getApplicationContext(), DataRef.<AudioFeatures>empty());

        mGLView = new GLSurfaceView(this);
        setContentView(mGLView);
        mGLView.setEGLContextClientVersion(3);
        mGLView.setPreserveEGLContextOnPause(true);
        mGLView.setRenderer(mRenderer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mBound) {
            bindService(
                    new Intent(this, AudioVisualizerService.class),
                    mConnection,
                    Context.BIND_AUTO_CREATE
            );
        }

        //Whenever we come back start up again
        mGLView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBound) {
            mService.onResume();
            mGLView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBound) {
            mService.onPause();
            mGLView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (AudioVisualizerService.AudioFeatureBinder) service;
            //fuck glview
            mRenderer.updateRef(mService.dataRef());
            mService.setBuckets(BUCKETS+1);

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };
}
