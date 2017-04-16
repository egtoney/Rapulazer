package edu.thunderseethe.rapulazer.AudioLib;

import java.util.Locale;

import be.tarsos.dsp.pitch.PitchDetectionResult;

/**
 * Created by Cody on 4/15/2017.
 */

public class AudioFeatures {
    public boolean is_silence;
    public boolean is_beat;
    public boolean is_percussion;

    /**
     * SPL is actually a ratio of the absolute, Sound Pressure and a reference level (usually the Threshold of Hearing,
     * or the lowest intensity sound that can be heard by most people). SPL is measured in decibels (dB), because of
     * the incredibly broad range of intensities we can hear.
     */
    public double sound_pressure_level;

    /**
     * The envelope is essentially just the amplitude
     */
    public float average_envelope;

    /**
     * The sensation of a frequency is commonly referred to as the pitch of a sound. A high pitch sound corresponds to
     * a high frequency sound wave and a low pitch sound corresponds to a low frequency sound wave.
     *
     * This PitchDetectionResult contains the probability it is a pitch, a boolean of whether or not it is a pitch, and
     * the pitch itself.
     */
    public PitchDetectionResult pitch_detection_result;
    public double[] freq_counts;

    public String toString() {
        return String.format(Locale.CANADA, "AudioFeature(%b, %b, %b, %f, %f, %f)", is_silence, is_beat, is_percussion, sound_pressure_level, average_envelope, pitch_detection_result.getPitch());
    }
}
