package edu.thunderseethe.rapulazer;

import android.app.IntentService;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


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

        public void onResume() {
            AudioVisualizerService.this.mVis.setEnabled(true);
        }

        public void onPause() {
            AudioVisualizerService.this.mVis.setEnabled(false);
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
        Log.d("AudioVisualizerService", "Initialization starting");
        mVis = new Visualizer(0);
        mVis.setEnabled(false);
        //mVis.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);

        final int SAMPLING_RATE = Visualizer.getMaxCaptureRate();

        final Bundle bundle = new Bundle();
        bundle.putByteArray("fft", new byte[]{});
        bundle.putByteArray("waveform", new byte[]{});
        bundle.putInt("samplingRate", SAMPLING_RATE);

        int[] range = Visualizer.getCaptureSizeRange();

        final Visualizer.MeasurementPeakRms measurement = new Visualizer.MeasurementPeakRms();

        mVis.setCaptureSize(range[0]);
        mVis.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                mAudioFeatureRef.update(mFeatureExtractor.getFeatures(waveform));
                Log.d("CATHACKS", mAudioFeatureRef.data().toString());
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                mAudioFeatureRef.update(mFeatureExtractor.getFeatures(fft));
                //visualizer.getMeasurementPeakRms(measurement);
                Log.d("CATHACKS", String.format("%s PEAK: %d\tRMS: %d", mAudioFeatureRef.data().toString(), measurement.mPeak, measurement.mRms));
            }
        }, SAMPLING_RATE, false, true);

        //These are all magic values, don't worry about it
        int sampleSizeInBits = 8;
        int channels = 1;
        TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(
                TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                SAMPLING_RATE,
                sampleSizeInBits,
                channels,
                (sampleSizeInBits + 7) / 8 * channels,
                16, //This value doesn't matter as we only use format for the FloatConverter
                true
        );

        mFeatureExtractor = new AudioFeatureExtractor(SAMPLING_RATE, mVis.getCaptureSize(), format);
        mBManager = LocalBroadcastManager.getInstance(this);
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
        this.onCreate(); //Manually create that shit
        return mBinder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
