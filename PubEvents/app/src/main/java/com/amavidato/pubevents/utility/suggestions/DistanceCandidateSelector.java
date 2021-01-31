package com.amavidato.pubevents.utility.suggestions;

import android.location.Location;
import android.util.Log;

import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.model.ModelObj;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.ui.events.list.EventItem;
import com.amavidato.pubevents.ui.events.list.ScoreCalculator;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class DistanceCandidateSelector implements GeneralCandidateSelector {
    private static final String TAG = DistanceCandidateSelector.class.getSimpleName();

    private final double MAX_DISTANCE = 100000;
    private GeoPoint meanPoint;
    @Override
    public void computeParametersForSelection(List<MyItem> baseList) {
        double avgLat = 0.0, avgLong = 0.0;
        for (MyItem e :
                baseList) {
            ModelObj object = e.object;
            GeoPoint point =  object instanceof Event ? ((Event) e.object).getPub().getGeoLocation() : ((Pub) e.object).getGeoLocation();
            avgLat += point.getLatitude();
            avgLong += point.getLongitude();
        }
        avgLat /= baseList.size();
        avgLong /= baseList.size();
        meanPoint = new GeoPoint(avgLat,avgLong);
    }

    @Override
    public void updateCandidatesScore(List<Candidate> candidates) {
        for (Candidate candidate :
                candidates) {
            ModelObj object = candidate.getItem().object;
            Log.d(TAG,"UpdateCandidateScore:"+object);
            GeoPoint geo = object instanceof Event ? ((Event)object).getPub().getGeoLocation() : ((Pub)object).getGeoLocation();
            Double dist = distance(geo,meanPoint);
            if (dist >= MAX_DISTANCE){
                candidates.remove(candidate);
            }else{
                candidate.setScore(candidate.getScore() + ScoreCalculator.computeScoreInverse(3,1,MAX_DISTANCE,dist));
            }
        }
    }

    private Double distance(GeoPoint geoLocation, GeoPoint meanPoint) {
        Location tmp1 = new Location("");
        tmp1.setLatitude(geoLocation.getLatitude());
        tmp1.setLongitude(geoLocation.getLongitude());

        Location tmp2 = new Location("");
        tmp2.setLatitude(meanPoint.getLatitude());
        tmp2.setLongitude(meanPoint.getLongitude());

        Double dist = ((Float)tmp1.distanceTo(tmp2)).doubleValue();
        Log.d(TAG,"DISTANCE FROM->"+geoLocation+"->TO->"+meanPoint+"="+dist);
        return dist;
    }

}
