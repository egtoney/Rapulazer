package edu.thunderseethe.rapulazer.AudioLib;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;

import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.pitch.McLeodPitchMethod;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;

/**
 * Created by Cody on 4/15/2017.
 */

public class AudioFeatureExtractor {

    static {
        System.loadLibrary("tensorflow_inference");
    }

    /*
     * TensorFlow related
     */
    private final TensorFlowInferenceInterface inference_interface;
    private static final String INPUT_NODE = "Input/input:0";
    private static final String[] OUTPUT_NODE = new String[] { "Output/output:0" };
    private static final int NUM_NODES = 400000;
    private static final long[] INPUT_SIZE = {1, NUM_NODES};
    private static final int PAD_MULTIPLIER = 20;
    private float[] node_values;
    private int node_value_index = 0;
    private boolean is_rap = true;
    private boolean performed_MLA = false;

    private SilenceDetector silence_detector;
    private PitchDetector pitch_detector;
    private AndroidComplexOnsetDetector complex_onset_detector;
    private EnvelopeDetector envelope_detector;
    private AndroidPercussionOnsetDetector percussion_onset_detector;

    private int buckets = 5;
    private final float sample_rate;
    private final int buffer_size;

    private TarsosDSPAudioFloatConverter float_converter;

    public AudioFeatureExtractor(float sample_rate, int buffer_size, TarsosDSPAudioFormat audio_format, TensorFlowInferenceInterface inference_interface) {
        this.sample_rate = sample_rate;
        this.buffer_size = buffer_size;
        this.float_converter = TarsosDSPAudioFloatConverter.getConverter(audio_format);

        /*
         * TensorFlow stuff
         */
        this.inference_interface = inference_interface;
        this.node_values = new float[NUM_NODES];

        /*
         * Initialize the SilenceDetector
         */
        this.silence_detector = new SilenceDetector();

        /*
         * Initialize the PitchDetector
         */
        this.pitch_detector = new McLeodPitchMethod(sample_rate, buffer_size);

        /*
         * Initialize the ComplexOnsetDetector
         */
        this.complex_onset_detector = new AndroidComplexOnsetDetector(buffer_size);
        this.percussion_onset_detector = new AndroidPercussionOnsetDetector(sample_rate, buffer_size, 0);

        /*
         * Initialize the EnvelopeDetector
         */
        this.envelope_detector = new EnvelopeDetector(this.sample_rate);
    }

    public AudioFeatures getFeatures(byte[] buffer) {
        AudioFeatures audio_features = new AudioFeatures();
        float[] f_buffer = new float[buffer.length];
        float_converter.toFloatArray(buffer, f_buffer);

        /*
         * Check if rap
         */
        audio_features.is_rap = is_rap;

        /*
         * Check silence detector
         */
        audio_features.is_silence = this.silence_detector.isSilence(f_buffer);

        audio_features.sound_pressure_level = this.silence_detector.currentSPL();

        /*
         * Check pitch detector
         */
        audio_features.pitch_detection_result = this.pitch_detector.getPitch(f_buffer);

        /*
         * Check complex onset detector
         */
        audio_features.is_beat = !audio_features.is_silence && this.complex_onset_detector.isBeat(f_buffer);

        audio_features.is_percussion = this.percussion_onset_detector.process(f_buffer);

        /*
         * Check the envelope detector
         */
        audio_features.average_envelope = envelope_detector.calculateAverageEnvelope(f_buffer);

        audio_features.freq_counts = frequency_counts(buffer, buckets);

        //        int length;
//        if(!performed_MLA) {
//            if (node_value_index + f_buffer.length > NUM_NODES / PAD_MULTIPLIER) {
//                length = (NUM_NODES / PAD_MULTIPLIER) - node_value_index - 1;
//                System.arraycopy(f_buffer, 0, node_values, node_value_index, length);
//
//                node_value_index += length;
//                length = NUM_NODES / PAD_MULTIPLIER;
//                for (int i = 0; i < PAD_MULTIPLIER - 1; i++) {
//                    System.arraycopy(node_values, 0, node_values, node_value_index, length);
//                    node_value_index += length;
//                }
//
//                performed_MLA = true;
//
//                Log.d("CATHACKS", "Machine Learning Time!");
//
//                (new Thread() {
//                    @Override
//                    public void run() {
//                        float[] outputs = new float[8];
//
//                        // Copy the input data into TensorFlow.
//                        Trace.beginSection("feed");
//                        inference_interface.feed(INPUT_NODE, node_values, NUM_NODES);
//                        Trace.endSection();
//
//                        // Run the inference call.
//                        Trace.beginSection("run");
//                        inference_interface.run(OUTPUT_NODE, true);
//                        Trace.endSection();
//
//                        // Copy the output Tensor back into the output array.
//                        Trace.beginSection("fetch");
//                        inference_interface.fetch(OUTPUT_NODE[0], outputs);
//                        Trace.endSection();
//
//                        Log.d("CATHACKS", outputs[0] + " " + outputs[1] + " " + inference_interface.getStatString());
//                    }
//                }).start();
//            } else {
//                length = f_buffer.length;
//                System.arraycopy(f_buffer, 0, node_values, node_value_index, length);
//                node_value_index += length;
//            }
//        }

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
            out[0] = 1;
            return out;
        }

        double bucket_size = (double)range / (n - 1);
        int bucket = 1;
        for(int i = 0; i < buffer.length; i += 1) {
            if((buffer[i] - buffer[0]) > bucket * bucket_size) {
                bucket += 1;
            }
            if(buckets - 1 == counts.length)
                counts[bucket - 2] += 1;
            else
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
            percents[i] = Math.log10(counts[i]) / log_max_count;
        }

        return percents;
    }
}
