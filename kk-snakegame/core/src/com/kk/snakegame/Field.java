package com.kk.snakegame;

import com.badlogic.gdx.Screen;
import java.util.Iterator;
import java.util.Timer;
import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import sun.rmi.runtime.Log;

/**
 * Created by Kaushik Kanetkar on 10/6/2015.
 */
public class Field implements Screen {
    public static final int HORIZONTAL_RIGHT = 1;
    public static final int VERTICAL_DOWN = 2;
    public static final int HORIZONTAL_LEFT = 3;
    public static final int VERTICAL_UP = 4;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int TOP = 3;
    public static final int BOTTOM = 4;
    public static final int snakePieceWidth = 5;
    public static final int snakePieceHeight = 10;
    public static final int foodPieceWidth = 10;
    public static final int foodPieceHeight = 10;
    public static final int FOOD_DELAY = 10000;
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 768;
    public static final int leftPadding = 200;
    public static final int rightPadding = 200;
    public static final int topPadding = 50;
    public static final int bottomPadding = 200;
    public static final int GFX_WIDTH = Gdx.graphics.getWidth();
    public static final int GFX_HEIGHT = Gdx.graphics.getHeight();
    public static final int DEBUG = 0;

    private static Preferences pref;

    MainScreen mainScreen;
    final SnakeGame sgame;
    OrthographicCamera camera;
    Texture image, apple, keys;
    Array<Rectangle> Snake;
    Rectangle snakeFood, left, right, top, bottom, exitRect;
    long lastFoodTime;
    float lastx, lasty;
    int snakeDirection, orientation = HORIZONTAL_RIGHT;  // Horizontal
    boolean snakeAte, isChanged = false;
    float width_factor = (float)WIDTH/(float)GFX_WIDTH, height_factor = (float)HEIGHT/(float)GFX_HEIGHT;
    int score = 0;
    int lengthOfSnake = 0;
    boolean gameOver = false;
    int gameMode;

    public Field(SnakeGame snakeGame, MainScreen main, int mode)
    {
        mainScreen = main;
        sgame = snakeGame;
        gameMode = mode;
        initGame();
        createSnake();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta)
    {
        Gdx.gl.glClearColor(0, 1.0f, 1.0f, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        sgame.batch.setProjectionMatrix(camera.combined);
        // Draw
        sgame.batch.begin();
        performDraw();
        sgame.batch.end();

        if ((gameOver == true) &&
             (Gdx.input.justTouched()))
        {
            if (exitRect.contains((float)(width_factor * Gdx.input.getX()), (float)(HEIGHT - height_factor * Gdx.input.getY())))
            {
                this.dispose();
                sgame.setScreen(mainScreen);
            }
        }

        // Does the snake overlap ?
        checkEndOfGame();
        if (gameOver == true)
        {
            if (score > getHighScore())
            {
                setHighScore(score);
            }
        }

        if (gameOver == false)
        {
            // Check for input from user
            addressUserInput();
            snakeAte = ifSnakeAteFood();
            updateSnake();

            if ((TimeUtils.millis() - lastFoodTime > FOOD_DELAY) ||
                (snakeAte == true))
            {
                getFoodLocation();
            }
        }

        isChanged = false;
    }

    private void performDraw()
    {
        sgame.batch.draw(keys, 0, 0, WIDTH, HEIGHT);
        for (Rectangle piece : Snake)
        {
            sgame.batch.draw(image, piece.x, piece.y, piece.width, piece.height);
            if (DEBUG == 1) {
                lastx = piece.x;
                lasty = piece.y;
            }
        }

        sgame.font.draw(sgame.batch, "Score: " + "" + score, 954, HEIGHT - 22);
        sgame.font.draw(sgame.batch, "High Score: " + getHighScore(), 200, HEIGHT - 22);

        if (gameOver == false)
        {
            sgame.font.draw(sgame.batch, "Time left: " + "" + getTimeLeft(), 550, HEIGHT - 22);
        }
        else
        {
            sgame.font.draw(sgame.batch, "- Game Over - ", 2, (3*HEIGHT/4) + 40);
            sgame.font.draw(sgame.batch, "  Tap here  ", 6, (3*HEIGHT/4) + 10);
        }
        if (DEBUG == 1)
        {
            sgame.font.draw(sgame.batch, "Length: " + (lastx - Snake.first().x) + " " + (lasty - Snake.first().y) + "    " + Snake.first().x, 200, 130);
        }

        sgame.batch.draw(apple, snakeFood.x, snakeFood.y, snakeFood.width, snakeFood.height);
    }

    public void addressUserInput()
    {
        if (Gdx.input.justTouched()) {
            if (left.contains((float) (width_factor * Gdx.input.getX()), (float) (HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(10);
                snakeDirection = LEFT;
                if ((orientation != HORIZONTAL_LEFT) && (orientation != HORIZONTAL_RIGHT)) {
                    isChanged = true;
                }
            } else if (right.contains((float) (width_factor * Gdx.input.getX()), (float) (HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(10);
                snakeDirection = RIGHT;
                if ((orientation != HORIZONTAL_LEFT) && (orientation != HORIZONTAL_RIGHT)) {
                    isChanged = true;
                }
            } else if (top.contains((float) (width_factor * Gdx.input.getX()), (float) (HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(10);
                snakeDirection = TOP;
                if ((orientation != VERTICAL_UP) && (orientation != VERTICAL_DOWN)) {
                    isChanged = true;
                }
            } else if (bottom.contains((float) (width_factor * Gdx.input.getX()), (float) (HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(10);
                snakeDirection = BOTTOM;
                if ((orientation != VERTICAL_UP) && (orientation != VERTICAL_DOWN)) {
                    isChanged = true;
                }
            }
        }
    }

    private void initGame()
    {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WIDTH, HEIGHT);
        image = new Texture(Gdx.files.internal("green.jpg"));
        apple = new Texture(Gdx.files.internal("apple.jpg"));
        keys = new Texture((gameMode == 1) ? Gdx.files.internal("layout.jpg") : Gdx.files.internal("layout2.jpg"));
        Snake = new <Rectangle>Array();
        snakeFood = new Rectangle();

        //Too much hard coding - improve it
        left = new Rectangle(leftPadding, 0, WIDTH/2 - leftPadding - 101, bottomPadding);
        right = new Rectangle(WIDTH/2 + 101, 0, WIDTH/2 - 99, bottomPadding);
        top = new Rectangle(WIDTH/2 - 100, 100 + 1, 200, 100);
        bottom = new Rectangle(WIDTH/2 - 100, 0, 200, 100 - 1);
        exitRect = new Rectangle(0, 3*HEIGHT/4, 200, HEIGHT/4);
        initPrefs();
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

    void createSnake()
    {
        lengthOfSnake = 60;
        float head_x = (MathUtils.random(leftPadding + 2 + (lengthOfSnake * snakePieceWidth), WIDTH - rightPadding) / snakePieceWidth) * snakePieceWidth;
        float head_y = (MathUtils.random(bottomPadding + 2, HEIGHT - topPadding) / snakePieceWidth) * snakePieceWidth;

        if (gameMode == 2)
        {
            head_x = leftPadding + (lengthOfSnake * snakePieceWidth) + 2;
        }

        for (int pieces = 0; pieces < lengthOfSnake ; pieces++)
        {
            Rectangle part = new Rectangle(head_x - (pieces * snakePieceWidth),
                                           head_y,
                                           snakePieceWidth,
                                           snakePieceHeight);
            Snake.add(part);
        }
    }

    public boolean ifSnakeAteFood()
    {
        Rectangle head = new Rectangle(Snake.first());

        if (head.overlaps(snakeFood))
        {
            score+= getTimeLeft();
            Gdx.input.vibrate(30);
            return true;
        }
        return false;
    }

    public int getTimeLeft()
    {
        return (int) (MathUtils.ceil((float)(((FOOD_DELAY) - (TimeUtils.millis() - lastFoodTime)) / 1000)));
    }

    public void setHighScore(int score)
    {
        if (gameMode == 1) {
            pref.putInteger("highScore1", score);
        }
        else {
            pref.putInteger("highScore2", score);
        }
        pref.flush();
    }

    public int getHighScore()
    {
        if (gameMode == 1) {
            return pref.getInteger("highScore1");
        }
        else if (gameMode == 2) {
            return pref.getInteger("highScore2");
        }
        return 0;
    }

    public void getFoodLocation()
    {
        snakeFood.width = 2 * foodPieceWidth;
        snakeFood.height = 2 * foodPieceHeight;
        snakeFood.x = (MathUtils.random(leftPadding, (WIDTH - rightPadding) - snakeFood.width - 1) / snakePieceWidth) * snakePieceWidth;
        snakeFood.y = (MathUtils.random(bottomPadding, (HEIGHT - topPadding) - snakeFood.height - 1) / snakePieceWidth) * snakePieceWidth;
        lastFoodTime = TimeUtils.millis();
    }

    public void checkEndOfGame()
    {
        switch (gameMode)
        {
            case 1:
            {
                int snakeParser = 0;
                Rectangle head = new Rectangle(Snake.first());
                for (Rectangle rect : Snake)
                {
                    if (snakeParser > 0)
                    {
                        if (head.overlaps(rect))
                        {
                            gameOver = true;
                        }
                    }
                    snakeParser++;
                }
            }
            break;
            case 2:
            {
                Rectangle head = new Rectangle(Snake.first());
                if ((head.x < leftPadding) ||
                    (head.x > (WIDTH - rightPadding)) ||
                    (head.y > (HEIGHT - topPadding)) ||
                    (head.y < bottomPadding))
                {
                    gameOver = true;
                }
            }
            break;
        }
    }

    void updateSnake()
    {
        Rectangle copyHead = new Rectangle(Snake.first());

        if (isChanged == true)
        {
            Rectangle rect = Snake.first();
            updateSnakeOrientation(rect);
        }

        // Update the head of the snake if no action from user
        if (isChanged == false)
        {
            Rectangle head = Snake.first();
            updateSnakeLocation(head);
        }

        updateRemainderOfSnake(copyHead);

    }

    public void updateSnakeLocation(Rectangle head)
    {
        if (orientation == HORIZONTAL_RIGHT) {
            head.x += snakePieceWidth;
        }
        else if (orientation == HORIZONTAL_LEFT) {
            head.x -= snakePieceWidth;
        }
        else if (orientation == VERTICAL_UP) {
            head.y += snakePieceWidth;
        }
        else if (orientation == VERTICAL_DOWN) {
            head.y -= snakePieceWidth;
        }
    }

    public void updateRemainderOfSnake(Rectangle copyHead)
    {
        // Shift 1->last parts one by one
        int snakeParser = 0;
        Rectangle last = new Rectangle();
        for (Rectangle rect : Snake)
        {
            copyRect(last, rect);
            if (snakeParser > 0)
            {
                Rectangle temp = new Rectangle();
                copyRect(temp, rect);
                copyRect(rect, copyHead);
                copyRect(copyHead, temp);
            }

            // Align with respect to boundary
            if (gameMode == 1)
            {
                alignSnakePiece(rect);
            }
            snakeParser++;
        }

        if (snakeAte == true) {
            Snake.add(last);
            lengthOfSnake++;
            AddOneMoreToSnake(last);
        }
    }

    public void alignSnakePiece(Rectangle rect)
    {
        if (rect.x < leftPadding)
        {
            rect.x = (((WIDTH - rightPadding) - (leftPadding - rect.x)) % WIDTH);
        }
        else if (((rect.x + snakePieceWidth) > (WIDTH - rightPadding)))
        {
            rect.x = ((rect.x + snakePieceWidth) % WIDTH) + leftPadding;
        }

        if (rect.y < bottomPadding)
        {
            rect.y = (((HEIGHT - topPadding) - (bottomPadding - rect.y)) % HEIGHT);
        }
        else if ((((rect.y + snakePieceWidth) > (HEIGHT - topPadding))))
        {
            rect.y = ((rect.y + snakePieceWidth) % (HEIGHT - topPadding)) + bottomPadding;
        }
    }

    public void AddOneMoreToSnake(Rectangle last)
    {
        Rectangle secondLast = Snake.get(lengthOfSnake-2);
        Rectangle tail = new Rectangle(); // new piece
        copyRect(tail, last);
        if (secondLast.x != last.x)
        {
            if (last.x < secondLast.x)
            {
                tail.x = last.x - snakePieceWidth;
            }
            else
            {
                tail.x = last.x + snakePieceWidth;
            }
        }
        else if (secondLast.y != last.y)
        {
            if (last.y < secondLast.y)
            {
                tail.y = last.y - snakePieceHeight;
            }
            else
            {
                tail.y = last.y + snakePieceHeight;
            }
        }
        Snake.add(tail);
        lengthOfSnake++;
    }

    public void updateSnakeOrientation(Rectangle rect)
    {
        if (orientation == HORIZONTAL_RIGHT)
        {
            rect.x = rect.x + (snakePieceWidth - snakePieceHeight);
            if (snakeDirection == BOTTOM)
            {
                rect.y = rect.y - snakePieceWidth;
                orientation = VERTICAL_DOWN;
            }
            else if (snakeDirection == TOP)
            {
                rect.y = rect.y + snakePieceHeight;
                orientation = VERTICAL_UP;
            }
        }
        else if (orientation == VERTICAL_DOWN)
        {
            if (snakeDirection == LEFT)
            {
                rect.x = rect.x - snakePieceWidth;
                orientation = HORIZONTAL_LEFT;
            }
            else if (snakeDirection == RIGHT)
            {
                rect.x = rect.x + snakePieceHeight;
                orientation = HORIZONTAL_RIGHT;
            }
        }
        else if (orientation == HORIZONTAL_LEFT)
        {
            if (snakeDirection == TOP)
            {
                rect.y = rect.y + snakePieceHeight;
                orientation = VERTICAL_UP;
            }
            else if (snakeDirection == BOTTOM)
            {
                rect.y = rect.y - snakePieceWidth;
                orientation = VERTICAL_DOWN;
            }
        }
        else if (orientation == VERTICAL_UP)
        {
            rect.y = rect.y + (snakePieceWidth - snakePieceHeight);
            if (snakeDirection == RIGHT)
            {
                rect.x = rect.x + snakePieceHeight;
                orientation = HORIZONTAL_RIGHT;
            }
            else if (snakeDirection == LEFT)
            {
                rect.x = rect.x - snakePieceWidth;
                orientation = HORIZONTAL_LEFT;
            }
        }
        float temp = rect.width;
        rect.width = rect.height;
        rect.height = temp;
    }

    public void copyRect(Rectangle dst, Rectangle src)
    {
        dst.x = src.x;
        dst.y = src.y;
        dst.width = src.width;
        dst.height = src.height;
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
        apple.dispose();
        keys.dispose();
        image.dispose();
    }
}
