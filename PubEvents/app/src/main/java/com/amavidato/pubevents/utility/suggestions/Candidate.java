package com.amavidato.pubevents.utility.suggestions;

import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;

public class Candidate {
    private MyItem item;
    private Double score;

    public Candidate(MyItem item, Double score) {
        this.item = item;
        this.score = score;
    }

    public MyItem getItem() {
        return item;
    }

    public void setItem(MyItem item) {
        this.item = item;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "item=" + item +
                ", score=" + score +
                '}';
    }
}
