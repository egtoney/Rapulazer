package edu.thunderseethe.rapulazer;

import android.app.IntentService;
import android.content.Intent;
import android.media.audiofx.Visualizer;

/**
 * Created by thunderseethe on 4/15/17.
 */

public class AudioVisualizerService extends IntentService {
    final Visualizer mVis;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AudioVisualizerService(String name, int audioID) {
        super(name);
        mVis = new Visualizer(audioID);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
