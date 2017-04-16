package edu.thunderseethe.rapulazer;

import android.os.Bundle;

import edu.thunderseethe.rapulazer.AudioLib.AudioFeatures;

/**
 * Created by thunderseethe on 4/15/17.
 */

public class DataRef<A> {
    private A data;

    public DataRef() {
        data = null;
    }
    public DataRef(A _data) {
        this.data = _data;
    }


    public boolean hasData() {
        return data != null;
    }

    public void update(A _data) {
        this.data = _data;
    }

    public A data() {
        return data;
    }

    public static <E> DataRef<E> empty() {
        return new DataRef<>();
    }
}
