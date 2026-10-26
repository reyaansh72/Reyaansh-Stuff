package com.reyaansh72.reyaanshsstuff;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceShooterActivity extends AppCompatActivity {

    private ShooterView shooterV;
    private TextView tvScore;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_shooter);

        findViewById(R.id.shooterBack).setOnClickListener(v -> finish());
        tvScore = findViewById(R.id.tvShooterScore);

        LinearLayout container = findViewById(R.id.shooterContainer);
        shooterV = new ShooterView(this);
        container.addView(shooterV);

        MaterialButton btnLeft = findViewById(R.id.btnMoveLeft);
        MaterialButton btnRight = findViewById(R.id.btnMoveRight);
        MaterialButton btnShoot = findViewById(R.id.btnShootLaser);

        btnLeft.setOnClickListener(v -> shooterV.moveLeft());
        btnRight.setOnClickListener(v -> shooterV.moveRight());
        btnShoot.setOnClickListener(v -> shooterV.shoot());
    }

    public void updateScore(int s) {
        score = s;
        runOnUiThread(() -> tvScore.setText("Score: " + score));
    }

    class ShooterView extends View {
        private float playerX = 400;
        private final List<Bullet> bullets = new ArrayList<>();
        private final List<Meteor> meteors = new ArrayList<>();
        private boolean isRunning = true;
        private final Handler handler = new Handler();
        private Runnable runnable;
        private final Paint paint = new Paint();
        private final Random random = new Random();
        private int frameCount = 0;

        public ShooterView(Context context) {
            super(context);
            initGame();
        }

        private void initGame() {
            bullets.clear();
            meteors.clear();
            score = 0;
            updateScore(0);
            frameCount = 0;
            playerX = 400;

            runnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        update();
                        invalidate();
                        handler.postDelayed(this, 30);
                    }
                }
            };
            handler.postDelayed(runnable, 30);
        }

        public void moveLeft() {
            playerX = Math.max(50, playerX - 50);
        }

        public void moveRight() {
            playerX = Math.min(getWidth() - 50, playerX + 50);
        }

        public void shoot() {
            bullets.add(new Bullet(playerX, getHeight() - 180));
        }

        private void update() {
            int width = getWidth();
            int height = getHeight();
            if (width == 0 || height == 0) return;

            frameCount++;
            if (frameCount % 40 == 0) {
                meteors.add(new Meteor(random.nextInt(width - 80) + 40, -50, random.nextInt(6) + 4));
            }

            // Update bullets
            List<Bullet> deadBullets = new ArrayList<>();
            for (Bullet b : bullets) {
                b.y -= 15;
                if (b.y < 0) deadBullets.add(b);
            }
            bullets.removeAll(deadBullets);

            // Update meteors
            List<Meteor> deadMeteors = new ArrayList<>();
            for (Meteor m : meteors) {
                m.y += m.speed;

                // Player collision
                if (m.y > height - 150 && Math.abs(m.x - playerX) < 50) {
                    isRunning = false;
                }

                // Bullet collision
                for (Bullet b : bullets) {
                    if (Math.abs(b.x - m.x) < 40 && Math.abs(b.y - m.y) < 40) {
                        deadMeteors.add(m);
                        deadBullets.add(b);
                        score += 15;
                        updateScore(score);
                    }
                }

                if (m.y > height) {
                    deadMeteors.add(m);
                }
            }
            meteors.removeAll(deadMeteors);
            bullets.removeAll(deadBullets);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();

            // Background
            paint.setColor(Color.parseColor("#121815"));
            canvas.drawRect(0, 0, width, height, paint);

            // Draw player spaceship
            paint.setColor(Color.parseColor("#52B788"));
            canvas.drawRect(playerX - 40, height - 150, playerX + 40, height - 100, paint);
            canvas.drawRect(playerX - 15, height - 180, playerX + 15, height - 150, paint);

            // Draw bullets
            paint.setColor(Color.parseColor("#FF758F"));
            for (Bullet b : bullets) {
                canvas.drawRect(b.x - 6, b.y - 20, b.x + 6, b.y, paint);
            }

            // Draw meteors
            paint.setColor(Color.parseColor("#E0AAFF"));
            for (Meteor m : meteors) {
                canvas.drawCircle(m.x, m.y, 35, paint);
            }

            if (!isRunning) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(50);
                canvas.drawText("Game Over! Tap to Restart", 40, height / 2f, paint);
            }
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!isRunning) {
                    isRunning = true;
                    initGame();
                    performClick();
                    return true;
                }
                playerX = event.getX();
                shoot();
                performClick();
            }
            return true;
        }
    }

    static class Bullet {
        float x, y;
        Bullet(float x, float y) { this.x = x; this.y = y; }
    }

    static class Meteor {
        float x, y, speed;
        Meteor(float x, float y, float speed) { this.x = x; this.y = y; this.speed = speed; }
    }
}