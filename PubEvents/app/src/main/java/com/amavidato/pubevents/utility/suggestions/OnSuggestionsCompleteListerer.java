package com.amavidato.pubevents.utility.suggestions;

import java.util.List;

public interface OnSuggestionsCompleteListerer {

    public void onComplete(List<Candidate> candidates,SuggestionsManager.TypeOfSuggestion tos);
}
