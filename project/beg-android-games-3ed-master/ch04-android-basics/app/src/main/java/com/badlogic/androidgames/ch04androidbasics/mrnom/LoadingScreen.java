package com.badlogic.androidgames.ch04androidbasics.mrnom;


import com.badlogic.androidgames.ch04androidbasics.framework.Game;
import com.badlogic.androidgames.ch04androidbasics.framework.Graphics;
import com.badlogic.androidgames.ch04androidbasics.framework.Screen;
import com.badlogic.androidgames.ch04androidbasics.framework.Graphics.PixmapFormat;

public class LoadingScreen extends Screen {
    public LoadingScreen(Game game) {
        super(game);
    }

    public void update(float deltaTime) {
        Graphics g = game.getGraphics();
        Assets.background = g.newPixmap("mrnom/background.png", PixmapFormat.RGB565);
        Assets.logo = g.newPixmap("mrnom/logo.png", PixmapFormat.ARGB4444);
        Assets.mainMenu = g.newPixmap("mrnom/mainmenu.png", PixmapFormat.ARGB4444);
        Assets.buttons = g.newPixmap("mrnom/buttons.png", PixmapFormat.ARGB4444);
        Assets.help1 = g.newPixmap("mrnom/help1.png", PixmapFormat.ARGB4444);
        Assets.help2 = g.newPixmap("mrnom/help2.png", PixmapFormat.ARGB4444);
        Assets.help3 = g.newPixmap("mrnom/help3.png", PixmapFormat.ARGB4444);
        Assets.numbers = g.newPixmap("mrnom/numbers.png", PixmapFormat.ARGB4444);
        Assets.ready = g.newPixmap("mrnom/ready.png", PixmapFormat.ARGB4444);
        Assets.pause = g.newPixmap("mrnom/pausemenu.png", PixmapFormat.ARGB4444);
        Assets.gameOver = g.newPixmap("mrnom/gameover.png", PixmapFormat.ARGB4444);
        Assets.headUp = g.newPixmap("mrnom/headup.png", PixmapFormat.ARGB4444);
        Assets.headLeft = g.newPixmap("mrnom/headleft.png", PixmapFormat.ARGB4444);
        Assets.headDown = g.newPixmap("mrnom/headdown.png", PixmapFormat.ARGB4444);
        Assets.headRight = g.newPixmap("mrnom/headright.png", PixmapFormat.ARGB4444);
        Assets.tail = g.newPixmap("mrnom/tail.png", PixmapFormat.ARGB4444);
        Assets.stain1 = g.newPixmap("mrnom/stain1.png", PixmapFormat.ARGB4444);
        Assets.stain2 = g.newPixmap("mrnom/stain2.png", PixmapFormat.ARGB4444);
        Assets.stain3 = g.newPixmap("mrnom/stain3.png", PixmapFormat.ARGB4444);
        Assets.click = game.getAudio().newSound("mrnom/click.ogg");
        Assets.eat = game.getAudio().newSound("mrnom/eat.ogg");
        Assets.bitten = game.getAudio().newSound("mrnom/bitten.ogg");
        Settings.load(game.getFileIO());
        game.setScreen(new MainMenuScreen(game));
    }
    public void present(float deltaTime) {

    }

    public void pause() {

    }

    public void resume() {

    }

    public void dispose() {

    }
}
