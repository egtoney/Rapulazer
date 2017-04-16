package edu.thunderseethe.rapulazer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import edu.thunderseethe.rapulazer.AudioLib.AudioFeatures;

public class MainActivity extends Activity {

    protected GLSurfaceView mGLView;
    //private BroadcastReceiver mReceiver;
    //private LocalBroadcastManager mBManager;
    //private DataRef<AudioFeatures> mAudioFeaturesRef;
    private boolean mBound = false;
    private VisualizerGLRenderer mRenderer = new VisualizerGLRenderer(DataRef.<AudioFeatures>empty());

    protected static String prettyPrintByteArray(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(byte b : bytes) {
            sb.append(" '");
            sb.append((int)b);
            sb.append("',");
        }
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView fftText = (TextView)findViewById(R.id.fft_text);
        final TextView waveformText = (TextView)findViewById(R.id.waveform_text);


        /*mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                mDataRef.update(bundle);

                String fftString = prettyPrintByteArray(bundle.getByteArray("fft"));
                String waveformString = prettyPrintByteArray(bundle.getByteArray("waveform"));

                fftText.setText(prettyPrintByteArray(bundle.getByteArray("fft")));
                waveformText.setText(prettyPrintByteArray(bundle.getByteArray("waveform")));

                Log.i("CATHACKS", fftString);
                Log.i("CATHACKS", waveformString);
            }
        };*/

        /*mBManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.visualize_data));
        mBManager.registerReceiver(mReceiver, intentFilter);*/

        mGLView = (GLSurfaceView)findViewById(R.id.visualizer_view);
        mGLView.setEGLContextClientVersion(3);
        mGLView.setPreserveEGLContextOnPause(true);
        mGLView.setRenderer(mRenderer);

        //startService(new Intent(this, AudioVisualizerService.class));
        bindService(
            new Intent(this, AudioVisualizerService.class),
            mConnection,
            Context.BIND_AUTO_CREATE
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBound) {
            mGLView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBound) {
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
            AudioVisualizerService.AudioFeatureBinder binder = (AudioVisualizerService.AudioFeatureBinder) service;
            //fuck glview
            mRenderer.updateRef(binder.dataRef());
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };
}
