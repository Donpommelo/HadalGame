package com.mygdx.hadal.map;

import com.mygdx.hadal.states.PlayState;

public class ToggleHub extends ModeSetting {

    @Override
    public void loadSettingMisc(PlayState state, GameMode mode) {
        state.setHub(true);
    }
}
