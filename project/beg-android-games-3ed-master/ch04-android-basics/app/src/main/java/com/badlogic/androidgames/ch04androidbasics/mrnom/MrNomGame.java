package com.badlogic.androidgames.ch04androidbasics.mrnom;

import com.badlogic.androidgames.ch04androidbasics.framework.Screen;
import com.badlogic.androidgames.ch04androidbasics.framework.impl.AndroidGame;


public class MrNomGame extends AndroidGame {

    public Screen getStartScreen() {
        return new LoadingScreen(this);
    }

}
