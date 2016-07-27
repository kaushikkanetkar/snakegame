package com.kk.snakegame.android;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Game;

public class SnakeGame extends Game {
	SpriteBatch batch;
    BitmapFont font;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        this.setScreen(new MainScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}

    public void dispose() {
        batch.dispose();
        font.dispose();
    }

}

