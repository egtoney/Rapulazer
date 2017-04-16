package edu.thunderseethe.rapulazer.AudioLib;

import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.PeakPicker;
import be.tarsos.dsp.util.fft.HannWindow;

/**
 * Created by Cody on 4/15/2017.
 */

public class AndroidComplexOnsetDetector {
    /**
     * The threshold to define silence, in dbSPL.
     */
    private final double silenceThreshold;

    /**
     * The minimum IOI (inter onset interval), in seconds.
     */
    private final double minimumInterOnsetInterval;

    /**
     * The last detected onset, in seconds.
     */
    private double lastOnset;

    /**
     * The last detected onset value.
     */
    private double lastOnsetValue;

    private final PeakPicker peakPicker;

    /**
     * To calculate the FFT.
     */
    private final FFT fft;

    /**
     * Previous phase vector, one frame behind
     */
    private final float[] theta1;
    /**
     * Previous phase vector, two frames behind
     */
    private final float[] theta2;

    /**
     * Previous norm (power, magnitude) vector
     */
    private final float[] oldmag;

    /**
     * Current onset detection measure vector
     */
    private final float[] dev1;

    /**
     *
     * @param fftSize The size of the fft to take (e.g. 512)
     * @param peakThreshold A threshold used for peak picking. Values between 0.1 and 0.8. Default is 0.3, if too many onsets are detected adjust to 0.4 or 0.5.
     * @param silenceThreshold The threshold that defines when a buffer is silent. Default is -70dBSPL. -90 is also used.
     * @param minimumInterOnsetInterval The minimum inter-onset-interval in seconds. When two onsets are detected within this interval the last one does not count. Default is 0.004 seconds.
     */
    public AndroidComplexOnsetDetector(int fftSize,double peakThreshold,double minimumInterOnsetInterval,double silenceThreshold){
        fft = new FFT(fftSize,new HannWindow());
        this.silenceThreshold = silenceThreshold;
        this.minimumInterOnsetInterval = minimumInterOnsetInterval;

        peakPicker = new PeakPicker(peakThreshold);

        int rsize = fftSize/2+1;
        oldmag = new float[rsize];
        dev1 = new float[rsize];
        theta1 = new float[rsize];
        theta2 = new float[rsize];
    }

    public AndroidComplexOnsetDetector(int fftSize){
        this(fftSize,0.3);
    }

    public AndroidComplexOnsetDetector(int fftSize,double peakThreshold){
        this(fftSize,peakThreshold,0.03);
    }

    public AndroidComplexOnsetDetector(int fftSize,double peakThreshold,double minimumInterOnsetInterval){
        this(fftSize,peakThreshold,minimumInterOnsetInterval,-70.0);
    }

    public boolean isBeat(float[] buffer, float sample_rate, int overlap, double time_stamp) {
        //calculate the complex fft (the magnitude and phase)
        float[] data = buffer.clone();
        float[] power = new float[data.length / 2];
        float[] phase = new float[data.length / 2];
        fft.powerPhaseFFT(data, power, phase);

        float onsetValue = 0;

        for (int j = 0; j < power.length; j++) {
            //int imgIndex = (power.length - 1) * 2 - j;

            // compute the predicted phase
            dev1[j] = 2.f * theta1[j] - theta2[j];

            // compute the euclidean distance in the complex domain
            // sqrt ( r_1^2 + r_2^2 - 2 * r_1 * r_2 * \cos ( \phi_1 - \phi_2 ) )
            onsetValue += Math.sqrt(Math.abs(Math.pow(oldmag[j], 2) + Math.pow(power[j], 2) - 2. * oldmag[j] * power[j] * Math.cos(dev1[j] - phase[j])));

			/* swap old phase data (need to remember 2 frames behind)*/
            theta2[j] = theta1[j];
            theta1[j] = phase[j];

			/* swap old magnitude data (1 frame is enough) */
            oldmag[j] = power[j];
        }

        lastOnsetValue = onsetValue;


        boolean isOnset = peakPicker.pickPeak(onsetValue);
        if (isOnset) {
            double delay = ((overlap * 4.3)) / sample_rate;
            double onsetTime = time_stamp - delay;
            if (onsetTime - lastOnset > minimumInterOnsetInterval) {
                lastOnset = onsetTime;
                return true;
            }
        }

        return false;
    }
}
