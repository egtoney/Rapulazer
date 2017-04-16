package edu.thunderseethe.rapulazer.AudioLib;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.tarsos.dsp.util.fft.FFT;

/**
 * Created by thunderseethe on 4/16/17.
 */

public class AndroidPercussionOnsetDetector {
    public static final double DEFAULT_THRESHOLD = 8;

    public static final double DEFAULT_SENSITIVITY = 20;

    private final FFT fft;

    private final float[] priorMagnitudes;
    private final float[] currentMagnitudes;

    private float dfMinus1, dfMinus2;

    private final float sampleRate;
    private long processedSamples;

    private final double sensitivity;

    private final double threshold;

    public AndroidPercussionOnsetDetector(float sampleRate, int bufferSize, int bufferOverlap) {
        this(sampleRate, bufferSize, DEFAULT_SENSITIVITY, DEFAULT_THRESHOLD);
    }

    public AndroidPercussionOnsetDetector(float sampleRate, int bufferSize, double sensitivity, double threshold) {
        fft = new FFT(bufferSize / 2);
        this.threshold = threshold;
        this.sensitivity = sensitivity;
        priorMagnitudes = new float[bufferSize / 2];
        currentMagnitudes = new float[bufferSize / 2];
        this.sampleRate = sampleRate;
    }

    public boolean process(float[] audioFloatBuffer) {
        this.processedSamples += audioFloatBuffer.length;

        fft.forwardTransform(audioFloatBuffer);
        fft.modulus(audioFloatBuffer, currentMagnitudes);

        int binsOverThreshold = 0;
        for(int i = 0; i < currentMagnitudes.length; i += 1) {
            if(priorMagnitudes[i] > 0.f) {
                double diff = 10 * Math.log10(currentMagnitudes[i] / priorMagnitudes[i]);
                if(diff >= threshold) {
                    binsOverThreshold += 1;
                }
            }
            priorMagnitudes[i] = currentMagnitudes[i];
        }

        if(dfMinus2 < dfMinus1
                && dfMinus1 >= binsOverThreshold
                && dfMinus1 > ((100 - sensitivity) * audioFloatBuffer.length) / 200) {
            float timeStamp = processedSamples / sampleRate;
            return true;
        }

        dfMinus2 = dfMinus1;
        dfMinus1 = binsOverThreshold;

        return false;
    }

}
