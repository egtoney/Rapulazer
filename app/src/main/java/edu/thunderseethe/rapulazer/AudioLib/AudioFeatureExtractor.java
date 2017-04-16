package edu.thunderseethe.rapulazer.AudioLib;

import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.FastYin;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

/**
 * Created by Cody on 4/15/2017.
 */

public class AudioFeatureExtractor {

    private SilenceDetector silence_detector;
    private PitchDetector pitch_detector;

    public AudioFeatureExtractor(float sample_rate, int buffer_size) {
        /**
         * Initialize the SilenceDetector
         */
        silence_detector = new SilenceDetector();

        /**
         * Initialize the PitchDetector
         */
        PitchEstimationAlgorithm algo = PitchEstimationAlgorithm.FFT_YIN;
        pitch_detector = new FastYin(sample_rate, buffer_size);
    }

    public AudioFeatures getFeatures(byte[] buffer) {
        AudioFeatures audio_features = new AudioFeatures();

        /**
         * Check for silence
         */
        audio_features.is_silence = silence_detector.isSilence(AudioUtil.toFloatArray(buffer));

        /**
         * Check the pitch
         */
        PitchDetectionResult result = new PitchDetectionResult();

        return audio_features;
    }
}
