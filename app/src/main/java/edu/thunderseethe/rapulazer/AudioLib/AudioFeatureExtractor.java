package edu.thunderseethe.rapulazer.AudioLib;

import android.media.audiofx.Visualizer;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.pitch.FastYin;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;

/**
 * Created by Cody on 4/15/2017.
 */

public class AudioFeatureExtractor {

    private SilenceDetector silence_detector;
    private PitchDetector pitch_detector;
    private AndroidComplexOnsetDetector complex_onset_detector;
    private EnvelopeDetector envelope_detector;

    private final float sample_rate;
    private final int buffer_size;
    private int bytes_processed = 0;

    private TarsosDSPAudioFloatConverter float_converter;

    public AudioFeatureExtractor(float sample_rate, int buffer_size, TarsosDSPAudioFormat audio_format) {
        this.sample_rate = sample_rate;
        this.buffer_size = buffer_size;
        this.float_converter = TarsosDSPAudioFloatConverter.getConverter(audio_format);

        /**
         * Initialize the SilenceDetector
         */
        this.silence_detector = new SilenceDetector();

        /**
         * Initialize the PitchDetector
         */
        this.pitch_detector = new FastYin(sample_rate, buffer_size);

        /**
         * Initialize the ComplexOnsetDetector
         */
        this.complex_onset_detector = new AndroidComplexOnsetDetector(buffer_size);

        /**
         * Initialize the EnvelopeDetector
         */
        this.envelope_detector = new EnvelopeDetector(this.sample_rate);
    }

    public AudioFeatures getFeatures(byte[] buffer) {
        AudioFeatures audio_features = new AudioFeatures();
        float[] f_buffer = new float[buffer.length];
        float_converter.toFloatArray(buffer, f_buffer);
        this.bytes_processed += buffer.length;

        /**
         * Check silence detector
         */
        audio_features.is_silence = this.silence_detector.isSilence(f_buffer);
        audio_features.sound_pressure_level = this.silence_detector.currentSPL();

        /**
         * Check pitch detector
         */
        audio_features.pitch_detection_result = this.pitch_detector.getPitch(f_buffer);

        /**
         * Check complex onset detector
         */
        if(!audio_features.is_silence)
            audio_features.is_beat = this.complex_onset_detector.isBeat(f_buffer, sample_rate, 0, getTimestamp());
        else
            audio_features.is_beat = false;

        /**
         * Check the envelope detector
         */
        audio_features.average_envelope = envelope_detector.calculateAverageEnvelope(f_buffer);

        return audio_features;
    }

    private double getTimestamp() {
        return (float)this.bytes_processed / (float)this.buffer_size / this.sample_rate;
    }
}
