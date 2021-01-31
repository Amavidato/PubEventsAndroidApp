package com.amavidato.pubevents.utility.suggestions;

import com.amavidato.pubevents.ui.events.list.EventItem;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;

import java.util.HashMap;
import java.util.List;

public interface GeneralCandidateSelector {

    void computeParametersForSelection(List<MyItem> baseList);

    void updateCandidatesScore(List<Candidate> candidates);

}
