package com.kk.snakegame.android;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;

import com.badlogic.gdx.Screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Vector;


/**
 * Created by Kaushik Kanetkar on 10/6/2015.
 */
public class Field implements Screen {
    MainScreen mainScreen;
    EndScreen endScreen;
    final SnakeGame sgame;
    OrthographicCamera camera;

    Texture image, apple, keys, banana;
    Array<Rectangle> Snake;
    Rectangle snakeFood, snakeSpecialFood, left, right, top, bottom, exitRect;
    List<Integer> previousScores = Arrays.asList(new Integer[]{0, 0, 0});

    long lastFoodTime, lastSpecialFoodTime, foodDelta, specialFoodDelta;
    float lastx, lasty; // DEBUG
    float width_factor, height_factor;
    int gameEndReason;
    int gameMode;
    int snakeDirection, orientation;  // Horizontal
    int score = 0;
    int chancesRemaining = 4;
    int lengthOfSnake = 0;
    boolean gameOver = false, speedRegulator = false;
    boolean snakeAteFood, snakeAteSpecialFood, isChanged = false;
    boolean scoresUpdated, justGotHighScore;
    boolean enableSpecialFood, generatedSpecialFood;

    public Field(SnakeGame snakeGame, MainScreen main, int mode) {
        mainScreen = main;
        sgame = snakeGame;
        gameMode = mode;
        initGame();
        createSnake();
    }

    @Override
    public void show() {
        endScreen = mainScreen.endScreen;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 1.0f, 1.0f, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        sgame.batch.setProjectionMatrix(camera.combined);
        // Draw
        sgame.batch.begin();
        performDraw();
        sgame.batch.end();

        // Check if Game is over and exit back to main screen
        ExitFieldIfGameOver();

        // Check end of game
        checkEndOfGame();

        updateScoresIfGameOver();

        // Continue playing...
        if (gameOver == false) {
            // Check for input from user
            addressUserInput();

            checkIfSnakeAte();

            updateSnake();

            updateScoresAndFoodLocations();
        }

        isChanged = false;
    }

    private void ExitFieldIfGameOver() {
        if ((gameOver == true) && (Gdx.input.justTouched())) {
            if (exitRect.contains((float) (width_factor * Gdx.input.getX()), (float) (sgame.HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(20);
                this.dispose();
                sgame.setScreen(endScreen);
            }
        }
    }

    private void updateScoresIfGameOver() {
        if ((gameOver == true) && (scoresUpdated == false)) {
            if (score > sgame.getHighScore(gameMode)) {
                justGotHighScore = true;
                sgame.setHighScore(score, gameMode);
            }
            scoresUpdated = true;
        }
    }

    private void updateScoresAndFoodLocations() {
        if ((TimeUtils.millis() - lastFoodTime > sgame.FOOD_DELAY) &&
                (snakeAteFood == false)) {
            updatePreviousScores(0);
            chancesRemaining--;
        }

        if ((TimeUtils.millis() - lastFoodTime > sgame.FOOD_DELAY) ||
                (snakeAteFood == true)) {
            // Produce a new food location
            getFoodLocation(sgame.FOOD);
        }

        if ((enableSpecialFood == true) && (generatedSpecialFood == false)) {
            getFoodLocation(sgame.SPECIAL_FOOD);
        }

        if ((enableSpecialFood == true) &&
                ((TimeUtils.millis() - lastSpecialFoodTime > sgame.SPECIAL_FOOD_DELAY) ||
                        (snakeAteSpecialFood == true))) {
            enableSpecialFood = false;
            generatedSpecialFood = false;
        }
    }

    private void checkIfSnakeAte() {
        snakeAteFood = ifSnakeAteFood();

        snakeAteSpecialFood = false;
        if (enableSpecialFood == true) {
            snakeAteSpecialFood = ifSnakeAteSpecialFood();
        }
    }

    private void performDraw() {
        // Draw the layout
        sgame.batch.draw(keys, 0, 0, sgame.WIDTH, sgame.HEIGHT);

        // Draw the food
        sgame.batch.draw(apple, snakeFood.x, snakeFood.y, snakeFood.width, snakeFood.height);

        if (enableSpecialFood == true) {
            sgame.batch.draw(banana, snakeSpecialFood.x, snakeSpecialFood.y, snakeSpecialFood.width, snakeSpecialFood.height);
        }

        // Draw the Snake
        for (Rectangle piece : Snake) {
            sgame.batch.draw(image, piece.x, piece.y, piece.width, piece.height);
            if (sgame.DEBUG == 1) {
                lastx = piece.x;
                lasty = piece.y;
            }
        }

        sgame.font.setColor(Color.CYAN);
        // Render the scores
        sgame.font.getData().setScale(2.25f);
        sgame.font.draw(sgame.batch, "Score: " + "" + score, 575, sgame.HEIGHT - 20);
        sgame.font.setColor(Color.WHITE);
        sgame.font.getData().setScale(2.0f);

        sgame.font.setColor(Color.GOLD);
        sgame.font.draw(sgame.batch, "High Score: " + sgame.getHighScore(gameMode), 200, sgame.HEIGHT - 22);
        sgame.font.setColor(Color.WHITE);

        sgame.font.setColor(Color.GREEN);
        sgame.font.draw(sgame.batch, "Chances: " + chancesRemaining, 1095, sgame.HEIGHT - 75);
        sgame.font.setColor(Color.WHITE);

        if (sgame.DEBUG == 1) {
            sgame.font.draw(sgame.batch, "Fps: " + Gdx.graphics.getFramesPerSecond(), 1100, sgame.HEIGHT - 50);
            sgame.font.draw(sgame.batch, "Length: " + Snake.size, 200, 130);
        }

        // Show time left or game over
        if (gameOver == false) {
            sgame.font.setColor(Color.RED);
            sgame.font.draw(sgame.batch, "Time left: " + "" + getTimeLeft(sgame.FOOD), 930, sgame.HEIGHT - 22);
            sgame.font.setColor(Color.WHITE);

        } else {
            sgame.font.setColor(Color.RED);
            sgame.font.draw(sgame.batch, "Game Over ! ", 2, (1 * sgame.HEIGHT / 2) + 40);
            sgame.font.draw(sgame.batch, "  Tap here  ", 6, (1 * sgame.HEIGHT / 2) + 10);
            sgame.font.setColor(Color.WHITE);
        }

    }

    public void addressUserInput() {
        if (Gdx.input.justTouched()) {
            if (left.contains((float) (width_factor * Gdx.input.getX()), (float) (sgame.HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(10);
                snakeDirection = sgame.LEFT;
                if ((orientation != sgame.HORIZONTAL_LEFT) && (orientation != sgame.HORIZONTAL_RIGHT)) {
                    isChanged = true;
                }
            } else if (right.contains((float) (width_factor * Gdx.input.getX()), (float) (sgame.HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(10);
                snakeDirection = sgame.RIGHT;
                if ((orientation != sgame.HORIZONTAL_LEFT) && (orientation != sgame.HORIZONTAL_RIGHT)) {
                    isChanged = true;
                }
            } else if (top.contains((float) (width_factor * Gdx.input.getX()), (float) (sgame.HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(10);
                snakeDirection = sgame.TOP;
                if ((orientation != sgame.VERTICAL_UP) && (orientation != sgame.VERTICAL_DOWN)) {
                    isChanged = true;
                }
            } else if (bottom.contains((float) (width_factor * Gdx.input.getX()), (float) (sgame.HEIGHT - height_factor * Gdx.input.getY()))) {
                Gdx.input.vibrate(10);
                snakeDirection = sgame.BOTTOM;
                if ((orientation != sgame.VERTICAL_UP) && (orientation != sgame.VERTICAL_DOWN)) {
                    isChanged = true;
                }
            }
        }
    }

    private void initGame() {
        width_factor = (float) sgame.WIDTH / (float) sgame.GFX_WIDTH;
        height_factor = (float) sgame.HEIGHT / (float) sgame.GFX_HEIGHT;

        orientation = sgame.HORIZONTAL_RIGHT;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, sgame.WIDTH, sgame.HEIGHT);

        initTextures();

        Snake = new <Rectangle>Array();
        snakeFood = new Rectangle();
        snakeSpecialFood = new Rectangle();

        initRects();

        scoresUpdated = false;
        justGotHighScore = false;
        sgame.font.getData().setScale(2.0f);

    }

    private void initRects() {
        //Too much hard coding - improve it
        left = new Rectangle(sgame.leftPadding, 0, sgame.WIDTH / 2 - sgame.leftPadding - 101, sgame.bottomPadding);
        right = new Rectangle(sgame.WIDTH / 2 + 101, 0, sgame.WIDTH / 2 - 99, sgame.bottomPadding);
        top = new Rectangle(sgame.WIDTH / 2 - 100, 100 + 1, 200, 100);
        bottom = new Rectangle(sgame.WIDTH / 2 - 100, 0, 200, 100 - 1);
        exitRect = new Rectangle(0, 1 * sgame.HEIGHT / 3, 200, 1 * sgame.HEIGHT / 3);
    }

    private void initTextures() {
        image = new Texture(Gdx.files.internal("green.jpg"));
        apple = new Texture(Gdx.files.internal("apple.jpg"));
        banana = new Texture(Gdx.files.internal("banana.jpg"));
        keys = new Texture((gameMode == 1) ? Gdx.files.internal("layout.jpg") : Gdx.files.internal("layout2.jpg"));
    }

    void createSnake() {
        lengthOfSnake = 60;
        float head_x = (MathUtils.random(sgame.leftPadding + 2 + (lengthOfSnake * sgame.snakePieceWidth),
                sgame.WIDTH - sgame.rightPadding) / sgame.snakePieceWidth) * sgame.snakePieceWidth;
        float head_y = (MathUtils.random(sgame.bottomPadding + 2,
                sgame.HEIGHT - sgame.topPadding) / sgame.snakePieceWidth) * sgame.snakePieceWidth;

        if (gameMode == 2) {
            head_x = sgame.leftPadding + (lengthOfSnake * sgame.snakePieceWidth) + 2;
        }

        for (int pieces = 0; pieces < lengthOfSnake; pieces++) {
            Rectangle part = new Rectangle(head_x - (pieces * sgame.snakePieceWidth),
                    head_y,
                    sgame.snakePieceWidth,
                    sgame.snakePieceHeight);
            Snake.add(part);
        }
    }

    public boolean ifSnakeAteFood() {
        Rectangle head = new Rectangle(Snake.first());

        if (head.overlaps(snakeFood)) {
            int currentScore = getTimeLeft(sgame.FOOD);
            score += currentScore;

            updatePreviousScores(currentScore);

            Gdx.input.vibrate(30);

            return true;
        }
        return false;
    }

    public boolean ifSnakeAteSpecialFood() {
        Rectangle head = new Rectangle(Snake.first());

        if (head.overlaps(snakeSpecialFood)) {
            int currentScore = getTimeLeft(sgame.SPECIAL_FOOD);
            score += ((2 * currentScore) + 5);

            Gdx.input.vibrate(30);

            return true;
        }
        return false;
    }

    private void updatePreviousScores(int currentScore) {
        previousScores.set(2, previousScores.get(1));
        previousScores.set(1, previousScores.get(0));
        previousScores.set(0, currentScore);

        if ((previousScores.get(0).intValue() > 4) &&
                (previousScores.get(1).intValue() > 4) &&
                (previousScores.get(2).intValue() > 4)) {
            enableSpecialFood = true;

            previousScores.set(0, 0);
            previousScores.set(1, 0);
            previousScores.set(2, 0);
        }
    }

    public int getTimeLeft(int foodType) {
        int timeLeft = 0;
        if (foodType == sgame.FOOD) {
            timeLeft = (int) (MathUtils.ceil((float) (((sgame.FOOD_DELAY) - (TimeUtils.millis() - lastFoodTime)) / 1000)));
        } else if (foodType == sgame.SPECIAL_FOOD) {
            timeLeft = (int) (MathUtils.ceil((float) (((sgame.SPECIAL_FOOD_DELAY) - (TimeUtils.millis() - lastSpecialFoodTime)) / 1000)));
        }
        return timeLeft;
    }


    public void getFoodLocation(int foodType) {
        Rectangle food = new Rectangle();

        food.width = 2 * sgame.foodPieceWidth;
        food.height = 2 * sgame.foodPieceHeight;
        food.x = (MathUtils.random(sgame.leftPadding,
                (sgame.WIDTH - sgame.rightPadding) - food.width - 1) / sgame.snakePieceWidth) * sgame.snakePieceWidth;
        food.y = (MathUtils.random(sgame.bottomPadding,
                (sgame.HEIGHT - sgame.topPadding) - food.height - 1) / sgame.snakePieceWidth) * sgame.snakePieceWidth;

        Rectangle foodOnScreen = new Rectangle();
        if (foodType == sgame.FOOD) {
            if (enableSpecialFood == true) {
                foodOnScreen = snakeSpecialFood;
            }
        } else if (foodType == sgame.SPECIAL_FOOD) {
            foodOnScreen = snakeFood;
        }

        while (isFoodOnSnake(food) ||
                food.contains(foodOnScreen)) {
            food.x += food.width;
            if (food.x > (sgame.WIDTH - sgame.rightPadding - food.width - 1)) {
                food.x = sgame.leftPadding + 1;
            }

            food.y += food.height;
            if (food.y > (sgame.HEIGHT - sgame.topPadding - food.height - 1)) {
                food.y = sgame.bottomPadding + 1;
            }
        }

        if (foodType == sgame.FOOD) {
            snakeFood = food;
            lastFoodTime = TimeUtils.millis();
        } else if (foodType == sgame.SPECIAL_FOOD) {
            snakeSpecialFood = food;
            lastSpecialFoodTime = TimeUtils.millis();
            generatedSpecialFood = true;
        }
    }

    public boolean isFoodOnSnake(Rectangle snakeFood) {
        for (Rectangle rect : Snake) {
            if (snakeFood.overlaps(rect)) {
                return true;
            }
        }
        return false;
    }

    public void checkEndOfGame() {
        switch (gameMode) {
            case 1: {
                if (chancesRemaining == 0) {
                    gameOver = true;
                    gameEndReason = sgame.CHANCES;
                } else {
                    int snakeParser = 0;
                    Rectangle head = new Rectangle(Snake.first());
                    for (Rectangle rect : Snake) {
                        if (snakeParser > 0) {
                            if (head.overlaps(rect)) {
                                gameOver = true;
                                gameEndReason = sgame.ITSELF;
                            }
                        }
                        snakeParser++;
                    }
                }
            }
            break;
            case 2: {
                if (chancesRemaining == 0) {
                    gameOver = true;
                    gameEndReason = sgame.CHANCES;
                } else {
                    int snakeParser = 0;
                    Rectangle head = new Rectangle(Snake.first());
                    for (Rectangle rect : Snake) {
                        if (snakeParser > 0) {
                            if ((head.x < sgame.leftPadding) ||
                                    (head.x > (sgame.WIDTH - sgame.rightPadding)) ||
                                    (head.y > (sgame.HEIGHT - sgame.topPadding)) ||
                                    (head.y < sgame.bottomPadding)){
                                gameOver = true;
                                gameEndReason = sgame.BORDER;
                            }
                            else if(head.overlaps(rect)) {
                                gameOver = true;
                                gameEndReason = sgame.ITSELF;
                            }
                        }
                        snakeParser++;
                    }
                }
            }
            break;
        }
    }

    void updateSnake() {
        if (sgame.mainScreen.gameSpeed == sgame.AVERAGE_SPEED) {
            moveSnake();
        } else if (sgame.mainScreen.gameSpeed == sgame.FAST_SPEED) {
            moveSnake();
            if (speedRegulator == true) {
                moveSnake();
                speedRegulator = false;
            } else {
                speedRegulator = true;
            }
        }
        if (gameMode == 1) {
            alignSnake();
        }

    }

    private void moveSnake() {
        Rectangle copyHead = new Rectangle(Snake.first());

        if (isChanged == true) {
            Rectangle rect = Snake.first();
            updateSnakeOrientation(rect);
        }

        // Update the head of the snake if no action from user
        if (isChanged == false) {
            Rectangle head = Snake.first();
            updateSnakeLocation(head);
        }

        Snake.insert(1, copyHead);

        if ((snakeAteFood == false) && (snakeAteSpecialFood == false)){
            Snake.pop();
        } else {
            Rectangle last = Snake.get(Snake.size - 1);
            lengthOfSnake++;
            AddOneMoreToSnake(last);
        }
        isChanged = false;
    }

    private void alignSnake() {
        for (Rectangle rect: Snake) {
            alignSnakePiece(rect);
        }
    }

    public void updateSnakeLocation(Rectangle head) {
        if (orientation == sgame.HORIZONTAL_RIGHT) {
            head.x += sgame.snakePieceWidth;
        } else if (orientation == sgame.HORIZONTAL_LEFT) {
            head.x -= sgame.snakePieceWidth;
        } else if (orientation == sgame.VERTICAL_UP) {
            head.y += sgame.snakePieceWidth;
        } else if (orientation == sgame.VERTICAL_DOWN) {
            head.y -= sgame.snakePieceWidth;
        }
    }

    public void alignSnakePiece(Rectangle rect) {
        if (rect.x < sgame.leftPadding) {
            rect.x = (((sgame.WIDTH - sgame.rightPadding) - (sgame.leftPadding - rect.x)) % sgame.WIDTH);
        } else if (((rect.x + sgame.snakePieceWidth) > (sgame.WIDTH - sgame.rightPadding))) {
            rect.x = ((rect.x + sgame.snakePieceWidth) % sgame.WIDTH) + sgame.leftPadding;
        }

        if (rect.y < sgame.bottomPadding) {
            rect.y = (((sgame.HEIGHT - sgame.topPadding) - (sgame.bottomPadding - rect.y)) % sgame.HEIGHT);
        } else if ((((rect.y + sgame.snakePieceWidth) > (sgame.HEIGHT - sgame.topPadding)))) {
            rect.y = ((rect.y + sgame.snakePieceWidth) % (sgame.HEIGHT - sgame.topPadding)) + sgame.bottomPadding;
        }
    }

    public void AddOneMoreToSnake(Rectangle last) {
        Rectangle secondLast = Snake.get(lengthOfSnake - 2);
        Rectangle tail = new Rectangle(); // new piece
        copyRect(tail, last);
        if (secondLast.x != last.x) {
            if (last.x < secondLast.x) {
                tail.x = last.x - sgame.snakePieceWidth;
            } else {
                tail.x = last.x + sgame.snakePieceWidth;
            }
        } else if (secondLast.y != last.y) {
            if (last.y < secondLast.y) {
                tail.y = last.y - sgame.snakePieceHeight;
            } else {
                tail.y = last.y + sgame.snakePieceHeight;
            }
        }
        Snake.add(tail);
        lengthOfSnake++;
    }

    public void updateSnakeOrientation(Rectangle rect) {
        if (orientation == sgame.HORIZONTAL_RIGHT) {
            rect.x = rect.x + (sgame.snakePieceWidth - sgame.snakePieceHeight);
            if (snakeDirection == sgame.BOTTOM) {
                rect.y = rect.y - sgame.snakePieceWidth;
                orientation = sgame.VERTICAL_DOWN;
            } else if (snakeDirection == sgame.TOP) {
                rect.y = rect.y + sgame.snakePieceHeight;
                orientation = sgame.VERTICAL_UP;
            }
        } else if (orientation == sgame.VERTICAL_DOWN) {
            if (snakeDirection == sgame.LEFT) {
                rect.x = rect.x - sgame.snakePieceWidth;
                orientation = sgame.HORIZONTAL_LEFT;
            } else if (snakeDirection == sgame.RIGHT) {
                rect.x = rect.x + sgame.snakePieceHeight;
                orientation = sgame.HORIZONTAL_RIGHT;
            }
        } else if (orientation == sgame.HORIZONTAL_LEFT) {
            if (snakeDirection == sgame.TOP) {
                rect.y = rect.y + sgame.snakePieceHeight;
                orientation = sgame.VERTICAL_UP;
            } else if (snakeDirection == sgame.BOTTOM) {
                rect.y = rect.y - sgame.snakePieceWidth;
                orientation = sgame.VERTICAL_DOWN;
            }
        } else if (orientation == sgame.VERTICAL_UP) {
            rect.y = rect.y + (sgame.snakePieceWidth - sgame.snakePieceHeight);
            if (snakeDirection == sgame.RIGHT) {
                rect.x = rect.x + sgame.snakePieceHeight;
                orientation = sgame.HORIZONTAL_RIGHT;
            } else if (snakeDirection == sgame.LEFT) {
                rect.x = rect.x - sgame.snakePieceWidth;
                orientation = sgame.HORIZONTAL_LEFT;
            }
        }
        float temp = rect.width;
        rect.width = rect.height;
        rect.height = temp;
    }

    public void copyRect(Rectangle dst, Rectangle src) {
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
        foodDelta = TimeUtils.millis() - lastFoodTime;
        specialFoodDelta = TimeUtils.millis() - lastSpecialFoodTime;
    }

    @Override
    public void resume() {
        lastFoodTime = TimeUtils.millis() - foodDelta;
        lastSpecialFoodTime = TimeUtils.millis() - specialFoodDelta;
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        disposeTextures();
    }

    private void disposeTextures() {
        apple.dispose();
        keys.dispose();
        image.dispose();
        banana.dispose();
    }
}
