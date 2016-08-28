package com.kk.snakegame.android;

import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.TimeUtils;


/**
 * Created by Kaushik Kanetkar on 10/6/2015.
 */

public class MainScreen implements Screen{
    final SnakeGame sgame;
    EndScreen endScreen;
    Field fieldModeOne;
    Field fieldModeTwo;
    OrthographicCamera camera;
    Texture image;
    Texture mainlogo;
    Rectangle mode1, mode2, averageSpeed, fastSpeed;
    Preferences pref;
    BitmapFont font, fontLogo, fontAverageSpeed, fontFastSpeed;
    int gameSpeed;

    public static final int DONT_ADD = 1;
    public static final int ADD = 2;

    float width_factor, height_factor;
    static boolean showingLogo = true;
    long firstTime;


    public MainScreen(SnakeGame snakeGame)
    {
        sgame = snakeGame;
        camera = new OrthographicCamera();
        image = new Texture(Gdx.files.internal("main.jpg"));
        mainlogo = new Texture((Gdx.files.internal("logo_main.jpeg")));
        camera.setToOrtho(false, mainlogo.getWidth(), mainlogo.getHeight());
        mode1 = new Rectangle(0, 200, sgame.WIDTH/2, 270);
        mode2 = new Rectangle(sgame.WIDTH/2 + 1, 200, sgame.WIDTH/2, 270);
        averageSpeed = new Rectangle(1000, sgame.HEIGHT-90, 280, 90);
        fastSpeed = new Rectangle(1000, sgame.HEIGHT-170, 280, 79);
        initFonts();
        width_factor = (float)sgame.WIDTH/(float)sgame.GFX_WIDTH;
        height_factor = (float)sgame.HEIGHT/(float)sgame.GFX_HEIGHT;
        gameSpeed = sgame.AVERAGE_SPEED;
        firstTime = TimeUtils.millis();
    }

    private void initFonts() {
        fontLogo = new BitmapFont();
        fontLogo.getData().setScale(2f);
        fontLogo.setColor(Color.BLACK);
        fontLogo.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        font.setColor(Color.RED);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontAverageSpeed = new BitmapFont();
        fontAverageSpeed.getData().setScale(2.5f);
        fontAverageSpeed.setColor(Color.CYAN);
        fontAverageSpeed.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontFastSpeed = new BitmapFont();
        fontFastSpeed.getData().setScale(2.5f);
        fontFastSpeed.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void show() {
        if (fieldModeOne != null) {
            fieldModeOne.dispose();
            fieldModeOne = null;
        }
        if (fieldModeTwo != null) {
            fieldModeTwo.dispose();
            fieldModeTwo = null;
        }

        /*
        if (sgame.useServer == true) {
            if (fieldModeOne == null) {
                SnakeAsyncTask async;
                async = new SnakeAsyncTask();
                async.execute(sgame.getHighScore(1), 1, DONT_ADD);
                while (async.getStatus() != AsyncTask.Status.FINISHED) {
                }
                sgame.setRank(async.rank, 1);
            }

            if (fieldModeTwo == null) {
                SnakeAsyncTask async;
                async = new SnakeAsyncTask();
                async.execute(sgame.getHighScore(2), 2, DONT_ADD);
                while (async.getStatus() != AsyncTask.Status.FINISHED) {
                }
                sgame.setRank(async.rank, 2);
            }
        }
        */

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if ((showingLogo == true) &&
            (TimeUtils.millis() - firstTime > 2000))
        {
            // Stop showing logo if its been 3 seconds
            showingLogo = false;
        }

        if (showingLogo == false)
        {
            camera.setToOrtho(false, sgame.WIDTH, sgame.HEIGHT);
        }

        camera.update();
        sgame.batch.setProjectionMatrix(camera.combined);

        sgame.batch.begin();

        if (showingLogo == true)
        {
            sgame.batch.draw(mainlogo, 0, 0, mainlogo.getWidth(), mainlogo.getHeight());
            fontLogo.draw(sgame.batch, "Created by Kaushik Kanetkar", mainlogo.getWidth() - 500, 40);
        }
        else
        {
            sgame.batch.draw(image, 0, 0, 1280, 768);
            font.draw(sgame.batch, "High Score: " + sgame.getHighScore(1), 240, 200);

            font.draw(sgame.batch, "High Score: " + sgame.getHighScore(2), 750, 200);

            fontAverageSpeed.draw(sgame.batch, "Average", 1030, sgame.HEIGHT-75);
            fontFastSpeed.draw(sgame.batch, "Fast", 1040, sgame.HEIGHT-135);

        }

        sgame.batch.end();

        if (showingLogo == false)
        {
            if (Gdx.input.isTouched())
            {
                if (mode1.contains(width_factor * Gdx.input.getX(), sgame.HEIGHT - (height_factor * Gdx.input.getY())))
                {
                    Gdx.input.vibrate(20);
                    fieldModeOne = new Field(sgame, this, 1);
                    endScreen = new EndScreen(sgame);
                    sgame.setScreen(fieldModeOne);
                }
                else if (mode2.contains(width_factor * Gdx.input.getX(), sgame.HEIGHT - (height_factor * Gdx.input.getY())))
                {
                    Gdx.input.vibrate(20);
                    fieldModeTwo = new Field(sgame, this, 2);
                    endScreen = new EndScreen(sgame);
                    sgame.setScreen(fieldModeTwo);
                }

                if (averageSpeed.contains(width_factor * Gdx.input.getX(), sgame.HEIGHT - (height_factor * Gdx.input.getY())))
                {
                    Gdx.input.vibrate(10);
                    fontAverageSpeed.setColor(Color.CYAN);
                    fontFastSpeed.setColor(Color.WHITE);
                    gameSpeed = sgame.AVERAGE_SPEED;
                }
                else if (fastSpeed.contains(width_factor * Gdx.input.getX(), sgame.HEIGHT - (height_factor * Gdx.input.getY())))
                {
                    Gdx.input.vibrate(10);
                    fontFastSpeed.setColor(Color.CYAN);
                    fontAverageSpeed.setColor(Color.WHITE);
                    gameSpeed = sgame.FAST_SPEED;
                }
            }
        }
    }

    public Field getField()
    {
        if (fieldModeOne != null) {
            return fieldModeOne;
        } else {
            return fieldModeTwo;
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
        disposeTextures();
    }

    private void disposeTextures() {
        mainlogo.dispose();
        image.dispose();
    }

    private void disposeFonts() {
        font.dispose();
        fontFastSpeed.dispose();
        fontAverageSpeed.dispose();
        fontLogo.dispose();
    }
}
