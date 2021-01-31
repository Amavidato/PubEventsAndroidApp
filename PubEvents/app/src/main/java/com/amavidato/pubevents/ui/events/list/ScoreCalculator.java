package com.amavidato.pubevents.ui.events.list;

import android.util.Log;

public class ScoreCalculator {
    private static final String TAG = ScoreCalculator.class.getSimpleName();

    public static double computeScore(int rangeMax, int rangeMin, double maxDomainValue, double currentDomainValue){
        double score = ((rangeMax-rangeMin)/maxDomainValue) * currentDomainValue + rangeMin;
        Log.d(TAG, "ComputeScore:max:"+rangeMax+"-min:"+rangeMin+"-maxDomain:"+maxDomainValue+"-currDomain:"+currentDomainValue+"===>Score:"+score);
        return score;
    }

    public static double computeScoreInverse(int rangeMax, int rangeMin, double maxDomainValue, double currentDomainValue){
        double score = rangeMax/computeScore(rangeMax,rangeMin,maxDomainValue,currentDomainValue);//(((rangeMax-rangeMin)/maxDomainValue) * currentDomainValue + rangeMin);
        Log.d(TAG, "ComputeScoreInverse..."+score);//:max:"+rangeMax+"-min:"+rangeMin+"-maxDomain:"+maxDomainValue+"-currDomain:"+currentDomainValue+"===>Score:"+score);
        return score;
    }
}
