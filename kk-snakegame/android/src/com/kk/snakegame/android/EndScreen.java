package com.kk.snakegame.android;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by kaushik on 7/30/16.
 */
public class EndScreen implements Screen {
    final SnakeGame sgame;
    OrthographicCamera camera;
    BitmapFont fontScore, fontTopScore, fontEndGame, fontServer, fontPlayAgain;
    Field field;
    Rectangle playAgainRect;

    public static final int DONT_ADD = 1;
    public static final int ADD = 2;
    public static final int GET_TOP_SCORE = 3;

    public static final int MODE_OPEN = 1;
    public static final int MODE_BOUNDARY = 2;

    float width_factor,height_factor;

    public EndScreen(SnakeGame snakeGame) {
        sgame = snakeGame;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        initializeFonts();
        field = sgame.mainScreen.getField();
        initRects();
        width_factor = (float) sgame.WIDTH / (float) sgame.GFX_WIDTH;
        height_factor = (float) sgame.HEIGHT / (float) sgame.GFX_HEIGHT;
    }

    private void initRects() {
        playAgainRect = new Rectangle(sgame.WIDTH - 600, 0, 600, 100);
    }

    private void initializeFonts() {
        fontScore = new BitmapFont();
        fontScore.getData().setScale(3.0f);
        fontScore.setColor(Color.GREEN);
        fontScore.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontTopScore = new BitmapFont();
        fontTopScore.getData().setScale(3.0f);
        fontTopScore.setColor(Color.CYAN);
        fontTopScore.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontEndGame = new BitmapFont();
        fontEndGame.getData().setScale(3.0f);
        fontEndGame.setColor(Color.RED);
        fontEndGame.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontServer = new BitmapFont();
        fontServer.getData().setScale(2.5f);
        fontServer.setColor(Color.NAVY);
        fontServer.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontPlayAgain = new BitmapFont();
        fontPlayAgain.getData().setScale(2.5f);
        fontPlayAgain.setColor(Color.WHITE);
        fontPlayAgain.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void show() {
        /*
        if (sgame.useServer == true) {
            SnakeAsyncTask addScore, getTopScore;
            addScore = new SnakeAsyncTask();
            addScore.execute(field.score, field.gameMode, ADD);
            while (addScore.getStatus() != AsyncTask.Status.FINISHED) {
            }
            if (field.justGotHighScore == true) {
                sgame.setRank(addScore.rank, field.gameMode);
            }

            getTopScore = new SnakeAsyncTask();
            getTopScore.execute(field.score, field.gameMode, GET_TOP_SCORE);
            while (getTopScore.getStatus() != AsyncTask.Status.FINISHED) {

            }
            sgame.currentTopScore = getTopScore.rank;
        }
        */
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        sgame.batch.setProjectionMatrix(camera.combined);
/*
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 1f, 1);
        shapeRenderer.rect((1280-200)/width_factor, 0/width_factor, 200/width_factor, 50/height_factor);
        shapeRenderer.end();
*/
        sgame.batch.begin();

        if (field.gameEndReason == sgame.ITSELF) {
            fontEndGame.draw(sgame.batch, "Ah, the snake ran into itself!", sgame.WIDTH / 4, 5 * sgame.HEIGHT / 6);
        } else if (field.gameEndReason == sgame.BORDER) {
            fontEndGame.draw(sgame.batch, "Damn, the snake hit the border", sgame.WIDTH / 4, 5 * sgame.HEIGHT / 6);
        } else if (field.gameEndReason == sgame.CHANCES) {
            fontEndGame.draw(sgame.batch, "Game over - The snake missed food 3 times...", sgame.WIDTH / 4, 5 * sgame.HEIGHT / 6);
        }

        fontScore.draw(sgame.batch, "Your score: " + field.score, sgame.WIDTH/4, 400);
        fontTopScore.draw(sgame.batch, "High Score: " + sgame.getHighScore(field.gameMode), 750, 400);

        if (sgame.useServer == true) {
            fontTopScore.draw(sgame.batch, "Rank: " + sgame.getRank(field.gameMode), 750, 350);
            fontTopScore.draw(sgame.batch, "Current Top Score: " + sgame.currentTopScore, 750, 250);
        }

        fontPlayAgain.draw(sgame.batch, "Click here to play again", sgame.WIDTH-470, 60);
        sgame.batch.end();

        if (Gdx.input.isTouched()) {
            if (playAgainRect.contains(width_factor * Gdx.input.getX(), sgame.HEIGHT - (height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(20);
                sgame.setScreen(sgame.mainScreen);
            }
        }

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        disposeFonts();
    }

    private void disposeFonts() {
        fontPlayAgain.dispose();
        fontServer.dispose();
        fontEndGame.dispose();
        fontScore.dispose();
        fontTopScore.dispose();
    }
}
