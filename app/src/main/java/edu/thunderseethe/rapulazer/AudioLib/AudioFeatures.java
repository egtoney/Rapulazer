package edu.thunderseethe.rapulazer.AudioLib;

import android.util.Pair;

import java.util.ArrayList;

import be.tarsos.dsp.pitch.PitchDetectionResult;

/**
 * Created by Cody on 4/15/2017.
 */

public class AudioFeatures {
    public boolean is_silence;
    public boolean is_beat;
    public double sound_pressure_level;
    public PitchDetectionResult pitch_detection_result;
}
