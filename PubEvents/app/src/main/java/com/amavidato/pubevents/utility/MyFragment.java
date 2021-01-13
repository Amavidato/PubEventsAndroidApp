package com.amavidato.pubevents.utility;

import androidx.fragment.app.Fragment;

public abstract class MyFragment extends Fragment implements OnBackPressedInterface {
    @Override
    public abstract void onBackPressed();
}
