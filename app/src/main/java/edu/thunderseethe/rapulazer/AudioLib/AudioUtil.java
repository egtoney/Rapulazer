package edu.thunderseethe.rapulazer.AudioLib;

/**
 * Created by Cody on 4/15/2017.
 */

public class AudioUtil {
    public static float[] toFloatArray(byte[] data) {
        if (data == null || data.length % 4 != 0) return null;
        // ----------
        float[] flts = new float[data.length / 4];
        for (int i = 0; i < flts.length; i++) {
            flts[i] = toFloat( new byte[] {
                    data[(i*4)],
                    data[(i*4)+1],
                    data[(i*4)+2],
                    data[(i*4)+3],
            } );
        }
        return flts;
    }

    public static float toFloat(byte[] data) {
        if (data == null || data.length != 4) return 0x0;
        // ---------- simple:
        return Float.intBitsToFloat(toInt(data));
    }

    public static int toInt(byte[] data) {
        if (data == null || data.length != 4) return 0x0;
        // ----------
        return (int)( // NOTE: type cast not necessary for int
                (0xff & data[0]) << 24  |
                        (0xff & data[1]) << 16  |
                        (0xff & data[2]) << 8   |
                        (0xff & data[3]) << 0
        );
    }
}
