package com.badlogic.androidgames.ch04androidbasics.framework;

import com.badlogic.androidgames.ch04androidbasics.framework.Graphics.PixmapFormat;

public interface Pixmap {
    public int getWidth();

    public int getHeight();

    public PixmapFormat getFormat();

    public void dispose();
}
