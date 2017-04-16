package edu.thunderseethe.rapulazer;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.Visualizer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.Arrays;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import edu.thunderseethe.rapulazer.AudioLib.AudioFeatureExtractor;
import edu.thunderseethe.rapulazer.AudioLib.AudioFeatures;

/**
 * Created by thunderseethe on 4/15/17.
 */

public class AudioVisualizerService extends IntentService {
    public class AudioFeatureBinder extends Binder
    {

        public DataRef<AudioFeatures> dataRef() {
            return AudioVisualizerService.this.mAudioFeatureRef;
        }
    }

    private Visualizer mVis;
    private AudioFeatureExtractor mFeatureExtractor;
    private LocalBroadcastManager mBManager;
    private DataRef<AudioFeatures> mAudioFeatureRef = DataRef.empty();
    private final Binder mBinder = new AudioFeatureBinder();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AudioVisualizerService(String name) {
        super(name);
    }
    public AudioVisualizerService() {
        this("DefaultAudioVisualizerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CATHACKS", "I was started");
        mVis = new Visualizer(0);
        mVis.setEnabled(false);

        final int SAMPLING_RATE = Visualizer.getMaxCaptureRate();

        final Bundle bundle = new Bundle();
        bundle.putByteArray("fft", new byte[]{});
        bundle.putByteArray("waveform", new byte[]{});
        bundle.putInt("samplingRate", SAMPLING_RATE);

        int[] range = Visualizer.getCaptureSizeRange();
        Log.d("CATHACKS", String.format("range [%d, %d]", range[0], range[1]));
        Log.d("CATHACKS", String.format("maxCapSize %d", Visualizer.getMaxCaptureRate()));

        mVis.setCaptureSize(range[1]);
        mVis.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                Log.d("CATHACKS", MainActivity.prettyPrintByteArray(waveform));
                /*bundle.putByteArray("waveform", Arrays.copyOf(waveform, waveform.length));
                bundle.putInt("samplingRate", samplingRate);
                sendUpdate(bundle);*/
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                Log.d("CATHACKS", MainActivity.prettyPrintByteArray(fft));
                /*Log.d("CATHACKS", "I was reached");
                bundle.putByteArray("fft", Arrays.copyOf(fft, fft.length));
                bundle.putInt("samplingRate", samplingRate);
                sendUpdate(bundle);*/
                mAudioFeatureRef.update(mFeatureExtractor.getFeatures(fft));
            }
        }, SAMPLING_RATE, false, true);

        TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(
            SAMPLING_RATE,
            16,
            TarsosDSPAudioFormat.NOT_SPECIFIED,
            false,
            true
        );

        mFeatureExtractor = new AudioFeatureExtractor(SAMPLING_RATE, mVis.getCaptureSize());
        mBManager = LocalBroadcastManager.getInstance(this);
        mVis.setEnabled(true);
        Log.d("CATHACKS", "I finished starting");
    }

    protected void sendUpdate(Bundle bundle) {
        Intent visualizeData = new Intent(getString(R.string.visualize_data));
        visualizeData.putExtras(bundle);
        mBManager.sendBroadcast(visualizeData);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if(getString(R.string.start_visualize).equals(action)) {
            mVis.setEnabled(true);
        }
        if(getString(R.string.stop_visualize).equals(action)) {
            mVis.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CATHACKS", "I was destroyed");
        if(mVis != null)
            mVis.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("CATHACKS", "fuck I");
        this.onCreate(); //Manually create that shit
        return mBinder;
    }
}
