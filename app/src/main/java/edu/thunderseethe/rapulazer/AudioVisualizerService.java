package edu.thunderseethe.rapulazer;

import android.app.IntentService;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import edu.thunderseethe.rapulazer.AudioLib.AudioFeatureExtractor;
import edu.thunderseethe.rapulazer.AudioLib.AudioFeatures;

/**
 * Created by thunderseethe on 4/15/17.
 */

public class AudioVisualizerService extends IntentService {

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static final String MODEL_FILE = "file:///android_asset/frozen_model.pb";

    public class AudioFeatureBinder extends Binder
    {

        public AudioFeatureBinder() {
            super();
            AudioVisualizerService.this.onCreate();
        }

        public DataRef<AudioFeatures> dataRef() {
            return AudioVisualizerService.this.mAudioFeatureRef;
        }

        public void onResume() {
            AudioVisualizerService.this.mVis.setEnabled(true);
        }

        public void onPause() {
            AudioVisualizerService.this.mVis.setEnabled(false);
        }

        public void setBuckets(int buckets) {
            AudioVisualizerService.this.mFeatureExtractor.setBuckets(buckets);
        }
        public int getMaxBucketSize() {
            return AudioVisualizerService.this.mCaptureSize;
        }
    }

    private Visualizer mVis;
    private AudioFeatureExtractor mFeatureExtractor;
    private DataRef<AudioFeatures> mAudioFeatureRef = DataRef.empty();
    private final Binder mBinder = new AudioFeatureBinder();
    private int mCaptureSize = 0;

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
        Log.d("AudioVisualizerService", "Initialization starting");
        mVis = new Visualizer(0);
        mVis.setEnabled(false);
        mVis.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);

        final int SAMPLING_RATE = (int)Math.round(Visualizer.getMaxCaptureRate() / 1.0);

        final Bundle bundle = new Bundle();
        bundle.putByteArray("fft", new byte[]{});
        bundle.putByteArray("waveform", new byte[]{});
        bundle.putInt("samplingRate", SAMPLING_RATE);

        int[] range = Visualizer.getCaptureSizeRange();

        final Visualizer.MeasurementPeakRms measurement = new Visualizer.MeasurementPeakRms();

        mCaptureSize = range[1];
        mVis.setCaptureSize(mCaptureSize);
        mVis.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {}

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                AudioFeatures last = mAudioFeatureRef.data();
                mAudioFeatureRef.update(mFeatureExtractor.getFeatures(fft));

//                Log.d("CATHACKS", MainActivity.prettyPrintDoubleArray(mAudioFeatureRef.data().freq_counts));
                Log.d("CATHACKS", String.format("%s PEAK:%s\tRMS:%s", mAudioFeatureRef.data().toString(), measurement.mPeak, measurement.mRms));
            }
        }, SAMPLING_RATE, false, true);

        //These are all magic values, don't worry about it
        int sampleSizeInBits = 8;
        int channels = 1;
        TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(
                TarsosDSPAudioFormat.Encoding.PCM_UNSIGNED,
                SAMPLING_RATE,
                sampleSizeInBits,
                channels,
                (sampleSizeInBits + 7) / 8 * channels,
                16, //This value doesn't matter as we only use format for the FloatConverter
                true
        );

        mFeatureExtractor = new AudioFeatureExtractor(SAMPLING_RATE, mVis.getCaptureSize(), format, null);
        mVis.setEnabled(true);
        Log.d("AudioVisualizerService", "Finished starting");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AudioVisualizerService", "Destroyed");
        if(mVis != null)
            mVis.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {}
}
