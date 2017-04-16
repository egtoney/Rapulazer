package edu.thunderseethe.rapulazer.AudioLib;

/**
 * Created by Cody on 4/16/2017.
 */

public class EnvelopeDetector {
    /**
     * Defines how fast the envelope raises, defined in seconds.
     */
    private static final double DEFAULT_ATTACK_TIME =  0.0002;//in seconds
    /**
     * Defines how fast the envelope goes down, defined in seconds.
     */
    private static final double DEFAULT_RELEASE_TIME =  0.0004;//in seconds

    float gainAttack ;
    float gainRelease;
    float envelopeOut = 0.0f;

    /**
     * Create a new envelope follower, with a certain sample rate.
     * @param sampleRate The sample rate of the audio signal.
     */
    public EnvelopeDetector(double sampleRate){
        this(sampleRate,DEFAULT_ATTACK_TIME,DEFAULT_RELEASE_TIME);
    }

    /**
     * Create a new envelope follower, with a certain sample rate.
     * @param sampleRate The sample rate of the audio signal.
     * @param attackTime Defines how fast the envelope raises, defined in seconds.
     * @param releaseTime Defines how fast the envelope goes down, defined in seconds.
     */
    public EnvelopeDetector(double sampleRate, double attackTime,double releaseTime){
        gainAttack = (float) Math.exp(-1.0/(sampleRate*attackTime));
        gainRelease = (float) Math.exp(-1.0/(sampleRate*releaseTime));
    }

    public float calculateAverageEnvelope(float[] buffer){
        float total = 0;
        for(int i = 0 ; i < buffer.length ; i++){
            float envelopeIn = Math.abs(buffer[i]);
            if(envelopeOut < envelopeIn){
                envelopeOut = envelopeIn + gainAttack * (envelopeOut - envelopeIn);
            } else {
                envelopeOut = envelopeIn + gainRelease * (envelopeOut - envelopeIn);
            }
            total += envelopeOut;
        }

        return total / (float)buffer.length;
    }
}
