package com.amavidato.pubevents.utility.suggestions;

import android.util.Log;

import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeCandidateSelection implements GeneralCandidateSelector {
    private static final String TAG = TypeCandidateSelection.class.getSimpleName();

    private HashMap<Event.EventType,Double> typeScores;

    @Override
    public void computeParametersForSelection(List<MyItem> baseList) {
        typeScores = new HashMap<>();
        for (MyItem e :
                baseList) {
            Event ev = ((Event)e.object);
            Double tmp = typeScores.get(ev.getType());
            if(tmp == null){
                tmp = 0.0;
            }
            tmp += 1.0;
            typeScores.put(ev.getType(),tmp);
        }
        double maxOccurrences = Collections.max(typeScores.entrySet(), new Comparator<Map.Entry<Event.EventType, Double>>() {
            @Override
            public int compare(Map.Entry<Event.EventType, Double> e1, Map.Entry<Event.EventType, Double> e2) {
                return e1.getValue().compareTo(e2.getValue());
            }
        }).getValue();
        Log.d(TAG,typeScores.toString());
        for (Event.EventType t :
                typeScores.keySet()) {
            typeScores.put(t, ScoreCalculator.computeScore(2,1,maxOccurrences,typeScores.get(t)));
        }
    }

    @Override
    public void updateCandidatesScore(List<Candidate> candidates) {
        for (Candidate candidate :
                candidates) {
            Event e = (Event) candidate.getItem().object;
            candidate.setScore(candidate.getScore() + typeScores.get(e.getType()));
        }
    }
}