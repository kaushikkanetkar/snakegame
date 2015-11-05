package com.kk.snakegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;


/**
 * Created by Kaushik Kanetkar on 10/6/2015.
 */

public class MainScreen implements Screen{
    final SnakeGame sgame;
    OrthographicCamera camera;
    Texture image;
    Texture mainlogo;
    Rectangle mode1, mode2;
    Preferences pref;
    BitmapFont font;
    public static final int GFX_WIDTH = Gdx.graphics.getWidth();
    public static final int GFX_HEIGHT = Gdx.graphics.getHeight();
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 768;
    float width_factor = (float)WIDTH/(float)GFX_WIDTH, height_factor = (float)HEIGHT/(float)GFX_HEIGHT;
    static boolean showingLogo = true;
    long firstTime;

    public MainScreen(SnakeGame snakeGame)
    {
        sgame = snakeGame;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 500, 300);
        image = new Texture(Gdx.files.internal("mainscreen.jpg"));
        mainlogo = new Texture((Gdx.files.internal("logo_main.jpg")));
        mode1 = new Rectangle(0, 200, WIDTH/2, 270);
        mode2 = new Rectangle(WIDTH/2 + 1, 200, WIDTH/2, 270);
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        font.setColor(Color.RED);
        firstTime = TimeUtils.millis();
        initPrefs();
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if ((showingLogo == true) &&
            (TimeUtils.millis() - firstTime > 3000))
        {
            // Stop showing logo if its been 3 seconds
            showingLogo = false;
        }

        if (showingLogo == false)
        {
            camera.setToOrtho(false, WIDTH, HEIGHT);
        }

        camera.update();
        sgame.batch.setProjectionMatrix(camera.combined);

        sgame.batch.begin();

        if (showingLogo == true)
        {
            sgame.batch.draw(mainlogo, 0, 0, 500, 300);
        }
        else
        {
            sgame.batch.draw(image, 0, 0, 1280, 768);
            font.draw(sgame.batch, "High Score: " + getHighScore(1), 215, HEIGHT - 540);
            font.draw(sgame.batch, "High Score: " + getHighScore(2), 860, HEIGHT - 540);
        }

        sgame.batch.end();

        if (showingLogo == false)
        {
            if (Gdx.input.isTouched())
            {
                if (mode1.contains(width_factor * Gdx.input.getX(), HEIGHT - (height_factor * Gdx.input.getY())))
                {
                    Gdx.input.vibrate(20);
                    sgame.setScreen(new Field(sgame, this, 1));
                }
                else if (mode2.contains(width_factor * Gdx.input.getX(), HEIGHT - (height_factor * Gdx.input.getY())))
                {
                    Gdx.input.vibrate(20);
                    sgame.setScreen(new Field(sgame, this, 2));
                }
            }
        }
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
        image.dispose();
    }
}
