package ru.spbhse.pocketmagic;

import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;

import java.util.ArrayList;

public class GestureListener implements GestureOverlayView.OnGesturePerformedListener {

    private GestureLibrary gestureLibrary;
    private GameActivity.Caster caster;

    private static final int SENSITIVITY = 2;

    GestureListener(GestureLibrary gestureLibrary, GameActivity.Caster caster) {
        this.gestureLibrary = gestureLibrary;
        this.caster = caster;
    }

    @Override
    public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
        ArrayList<Prediction> predictionList = gestureLibrary.recognize(gesture);

        if (predictionList.size() > 0) {
            Prediction firstPrediction = predictionList.get(0);

            if (firstPrediction.score > SENSITIVITY) {
                caster.cast(firstPrediction.name);
            }
        }
    }
}
