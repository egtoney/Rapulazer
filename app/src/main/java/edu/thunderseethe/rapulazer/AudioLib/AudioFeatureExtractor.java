package edu.thunderseethe.rapulazer.AudioLib;

import android.media.audiofx.Visualizer;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;

import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.pitch.McLeodPitchMethod;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import edu.thunderseethe.rapulazer.MainActivity;

/**
 * Created by Cody on 4/15/2017.
 */

public class AudioFeatureExtractor {

    private SilenceDetector silence_detector;
    private PitchDetector pitch_detector;
    private AndroidComplexOnsetDetector complex_onset_detector;
    private EnvelopeDetector envelope_detector;
    private AndroidPercussionOnsetDetector percussion_onset_detector;

    private final float sample_rate;
    private final int buffer_size;
    private int bytes_processed = 0;
    private int buckets = 5;

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
        this.pitch_detector = new McLeodPitchMethod(sample_rate, buffer_size);

        /**
         * Initialize the ComplexOnsetDetector
         */
        this.complex_onset_detector = new AndroidComplexOnsetDetector(buffer_size);
        this.percussion_onset_detector = new AndroidPercussionOnsetDetector(sample_rate, buffer_size, 0);

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
            audio_features.is_beat = this.complex_onset_detector.isBeat(f_buffer, sample_rate, this.buffer_size);
        else
            audio_features.is_beat = false;

        audio_features.is_percussion = this.percussion_onset_detector.process(f_buffer);

        /**
         * Check the envelope detector
         */
        audio_features.average_envelope = envelope_detector.calculateAverageEnvelope(f_buffer);

        audio_features.freq_counts = frequency_counts(buffer, buckets);


        return audio_features;
    }

    public void setBuckets(int buckets) {
        this.buckets = buckets;
    }

    private double[] frequency_counts(byte[] buffer, int n) {
        int[] counts = new int[n];
        Arrays.sort(buffer);
        int range = buffer[buffer.length - 1] - buffer[0];
        if(range == 0) {
            double[] out = new double[n];
            for(int i = 0; i < out.length; i += 1) {
                out[i] = 1/n;
            }
            return out;
        }


        /* dicks dicks dicks dicks
         dicks dicks dicks dicks
         dicks dicks dicks dicks
        dicks dicks dicks dicks */

        double bucket_size = (double)range / (n - 1);
        int bucket = 1;
        for(int i = 0; i < buffer.length; i += 1) {
            if((buffer[i] - buffer[0]) > bucket * bucket_size) {
                bucket += 1;
            }
            counts[bucket - 1] += 1;
        }
        int max_count = counts[0];
        for(int i : counts) {
           if(i > max_count)
               max_count = i;
        }

        double log_max_count = Math.log10(max_count);
        double[] percents = new double[n];
        for(int i = 0; i < counts.length; i += 1){
            percents[i] =
                counts[i] != 0
                    ? Math.log10(counts[i]) / log_max_count
                    : 0;
        }

        return percents;
    }

    private double getTimestamp() {
        return (float)this.bytes_processed / (float)this.buffer_size / this.sample_rate;
    }
}
