package com.kk.snakegame.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Game;

public class SnakeGame extends Game {

	public static final int HORIZONTAL_RIGHT = 1;
	public static final int VERTICAL_DOWN = 2;
	public static final int HORIZONTAL_LEFT = 3;
	public static final int VERTICAL_UP = 4;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int TOP = 3;
	public static final int BOTTOM = 4;
	public static final int snakePieceWidth = 6;
	public static final int snakePieceHeight = 12;
	public static final int foodPieceWidth = 12;
	public static final int foodPieceHeight = 12;
	public static final int FOOD_DELAY = 10000;
	public static final int SPECIAL_FOOD_DELAY = 6000;
	public static final int WIDTH = 1280;
	public static final int HEIGHT = 768;
	public static final int leftPadding = 200;
	public static final int rightPadding = 200;
	public static final int topPadding = 50;
	public static final int bottomPadding = 200;
	public static final int DEBUG = 0;

	public static final int ITSELF = 0;
	public static final int BORDER = 1;
	public static final int CHANCES = 2;

	public static final int FOOD = 0;
	public static final int SPECIAL_FOOD = 1;

	public static final int AVERAGE_SPEED = 1;
	public static final int FAST_SPEED = 2;

	public int GFX_WIDTH;
	public int GFX_HEIGHT;

	SpriteBatch batch;
        BitmapFont font;
	MainScreen mainScreen;
	boolean useServer;
	public int currentTopScore = 0;
	private static Preferences pref;

	@Override
	public void create () {
		batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.0f);
		font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		GFX_WIDTH = Gdx.graphics.getWidth();
		GFX_HEIGHT = Gdx.graphics.getHeight();
		initPrefs();
		mainScreen = new MainScreen(this);
		useServer = false;
		this.setScreen(mainScreen);
	}

	@Override
	public void render () {
		super.render();
	}

    public void dispose() {
        batch.dispose();
        font.dispose();
	}

	public void initPrefs()
	{
		pref = Gdx.app.getPreferences("Snake");
		if (!pref.contains("highScore1"))
		{
			// Set High Score to 0
			pref.putInteger("highScore1", 0);
		}
		if (!pref.contains("highScore2"))
		{
			// Set High Score to 0
			pref.putInteger("highScore2", 0);
		}
		if (!pref.contains("rank1")) {
			pref.putInteger("rank1", 0);
		}
		if (!pref.contains("rank2")) {
			pref.putInteger("rank2", 0);
		}
	}

	public int getHighScore(int mode)
	{
		if (mode == 1) {
			return pref.getInteger("highScore1");
		}
		else if (mode == 2) {
			return pref.getInteger("highScore2");
		}
		return 0;
	}

	public void setHighScore(int score, int mode) {
		if (mode == 1) {
			pref.putInteger("highScore1", score);
		} else {
			pref.putInteger("highScore2", score);
		}
		pref.flush();
	}

	public void resetHighscores() {
		pref.putInteger("highScore1", 0);
		pref.putInteger("highScore2", 0);
		pref.flush();
	}


	public void setRank(int rank, int mode) {
		if (mode == 1) {
			pref.putInteger("rank1", rank);
		} else {
			pref.putInteger("rank2", rank);
		}
		pref.flush();
	}

	public int getRank(int mode) {
		if (mode == 1) {
			return pref.getInteger("rank1");
		} else if (mode == 2) {
			return pref.getInteger("rank2");
		}
		return 0;
	}
}

