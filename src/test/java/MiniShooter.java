import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MiniShooter extends JPanel implements ActionListener, KeyListener {

    private static final int WIDTH = 700;
    private static final int HEIGHT = 700;

    private final Timer timer = new Timer(16, this);
    private final Random random = new Random();

    private final ArrayList<Star> stars = new ArrayList<>();
    private final ArrayList<Bullet> bullets = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final ArrayList<PowerUp> powerUps = new ArrayList<>();

    private final SoundSystem sound = new SoundSystem();

    private enum GameState {
        MENU,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    private GameState state = GameState.MENU;

    // 玩家

    private double playerX;
    private final double playerY = HEIGHT - 100;

    private final int playerWidth = 46;
    private final int playerHeight = 42;

    private int hp = 3;
    private int score = 0;
    private int highScore = 0;

    private int shootCooldown = 0;
    private int invincibleTime = 0;

    private boolean shield = false;

    // 射速强化剩余时间
    private int rapidFireTime = 0;

    private boolean left;
    private boolean right;
    private boolean shooting;

    // =========================
    // 游戏
    // =========================

    private int enemySpawnTimer = 0;
    private int frameCount = 0;

    private int shakeTime = 0;

    // 屏幕闪光
    private int screenFlashTime = 0;

    // 道具提示文字
    private String pickupMessage = "";
    private int pickupMessageTime = 0;

    // =========================
    // 构造
    // =========================

    public MiniShooter() {

        setPreferredSize(
                new Dimension(WIDTH, HEIGHT)
        );

        setBackground(Color.BLACK);

        setFocusable(true);
        addKeyListener(this);

        createStars();

        timer.start();
    }

    // =========================
    // 星空
    // =========================

    private void createStars() {

        for (int i = 0; i < 120; i++) {

            double size =
                    0.8 + random.nextDouble() * 2.8;

            double speed =
                    0.4 + size * 0.9;

            stars.add(
                    new Star(
                            random.nextInt(WIDTH),
                            random.nextInt(HEIGHT),
                            speed,
                            size
                    )
            );
        }
    }

    private void updateStars() {

        for (Star star : stars) {

            star.y += star.speed;

            if (star.y > HEIGHT) {

                star.y = -5;

                star.x =
                        random.nextInt(WIDTH);
            }
        }
    }

    // =========================
    // 主循环
    // =========================

    @Override
    public void actionPerformed(ActionEvent e) {

        frameCount++;

        updateStars();

        if (state == GameState.PLAYING) {
            updateGame();
        }

        if (pickupMessageTime > 0) {
            pickupMessageTime--;
        }

        if (screenFlashTime > 0) {
            screenFlashTime--;
        }

        repaint();
    }

    // =========================
    // 游戏更新
    // =========================

    private void updateGame() {

        updatePlayer();
        updateBullets();
        updateEnemies();
        updatePowerUps();
        updateParticles();

        checkCollisions();

        if (invincibleTime > 0) {
            invincibleTime--;
        }

        if (rapidFireTime > 0) {
            rapidFireTime--;
        }

        if (shakeTime > 0) {
            shakeTime--;
        }
    }

    // =========================
    // 玩家
    // =========================

    private void updatePlayer() {

        // 提高移动速度
        double speed = 9;

        if (left) {
            playerX -= speed;
        }

        if (right) {
            playerX += speed;
        }

        playerX = Math.max(
                playerWidth / 2,
                Math.min(
                        WIDTH - playerWidth / 2,
                        playerX
                )
        );

        if (shootCooldown > 0) {
            shootCooldown--;
        }

        if (shooting && shootCooldown <= 0) {

            bullets.add(
                    new Bullet(
                            playerX,
                            playerY - 28
                    )
            );

            // 射速强化
            if (rapidFireTime > 0) {
                shootCooldown = 5;
            } else {
                shootCooldown = 9;
            }

            sound.shoot();

            // 枪口闪光
            screenFlashTime = 2;

            // 枪口粒子
            createMuzzleFlash();
        }
    }

    // =========================
    // 枪口特效
    // =========================

    private void createMuzzleFlash() {

        for (int i = 0; i < 4; i++) {

            particles.add(
                    new Particle(
                            playerX + random.nextGaussian() * 4,
                            playerY - 28,
                            random.nextGaussian() * 0.8,
                            -1 - random.nextDouble() * 2,
                            8 + random.nextInt(6)
                    )
            );
        }
    }

    // =========================
    // 子弹
    // =========================

    private void updateBullets() {

        Iterator<Bullet> iterator =
                bullets.iterator();

        while (iterator.hasNext()) {

            Bullet bullet = iterator.next();

            bullet.y -= 13;

            if (bullet.y < -30) {
                iterator.remove();
            }
        }
    }

    // =========================
    // 敌人
    // =========================

    private void updateEnemies() {

        enemySpawnTimer--;

        int difficulty =
                Math.min(score / 100, 10);

        if (enemySpawnTimer <= 0) {

            int x =
                    45 + random.nextInt(WIDTH - 90);

            double speed =
                    2.5 + difficulty * 0.25;

            enemies.add(
                    new Enemy(
                            x,
                            -40,
                            speed
                    )
            );

            enemySpawnTimer =
                    Math.max(
                            15,
                            45 - difficulty * 3
                    );
        }

        Iterator<Enemy> iterator =
                enemies.iterator();

        while (iterator.hasNext()) {

            Enemy enemy = iterator.next();

            enemy.y += enemy.speed;

            enemy.x +=
                    Math.sin(
                            (frameCount + enemy.offset)
                                    * 0.03
                    ) * 0.7;

            if (enemy.y > HEIGHT + 50) {

                iterator.remove();

                damagePlayer();
            }
        }
    }

    // =========================
    // 道具
    // =========================

    private void updatePowerUps() {

        Iterator<PowerUp> iterator =
                powerUps.iterator();

        Rectangle player =
                getPlayerBounds();

        while (iterator.hasNext()) {

            PowerUp powerUp =
                    iterator.next();

            powerUp.y += 2.5;

            powerUp.angle += 0.06;

            // 轻微横向漂移
            powerUp.x +=
                    Math.sin(
                            (frameCount + powerUp.offset)
                                    * 0.04
                    ) * 0.5;

            if (
                    powerUp.getBounds()
                            .intersects(player)
            ) {

                applyPowerUp(powerUp.type);

                iterator.remove();

                createPickupEffect(
                        powerUp.x,
                        powerUp.y
                );

                sound.powerUp();

                shakeTime = 5;

                continue;
            }

            if (powerUp.y > HEIGHT + 50) {
                iterator.remove();
            }
        }
    }

    // =========================
    // 道具效果
    // =========================

    private void applyPowerUp(
            PowerUp.Type type
    ) {

        switch (type) {

            case HEALTH:

                if (hp < 3) {
                    hp++;
                    pickupMessage = "HP +1";
                } else {
                    score += 20;
                    pickupMessage =
                            "FULL HP  +20 SCORE";
                }

                break;

            case SHIELD:

                shield = true;

                pickupMessage =
                        "SHIELD READY";

                break;

            case RAPID_FIRE:

                rapidFireTime = 600;

                pickupMessage =
                        "RAPID FIRE";

                break;
        }

        pickupMessageTime = 90;
    }

    // =========================
    // 道具生成
    // =========================

    private void maybeDropPowerUp(
            double x,
            double y
    ) {

        // 22% 掉落率
        if (random.nextDouble() > 0.22) {
            return;
        }

        PowerUp.Type type;

        int roll = random.nextInt(100);

        if (roll < 35) {
            type = PowerUp.Type.HEALTH;
        } else if (roll < 65) {
            type = PowerUp.Type.SHIELD;
        } else {
            type = PowerUp.Type.RAPID_FIRE;
        }

        powerUps.add(
                new PowerUp(
                        x,
                        y,
                        type
                )
        );
    }

    // =========================
    // 碰撞
    // =========================

    private void checkCollisions() {

        Rectangle player =
                getPlayerBounds();

        Iterator<Enemy> enemyIterator =
                enemies.iterator();

        while (enemyIterator.hasNext()) {

            Enemy enemy =
                    enemyIterator.next();

            boolean destroyed = false;

            Iterator<Bullet> bulletIterator =
                    bullets.iterator();

            while (bulletIterator.hasNext()) {

                Bullet bullet =
                        bulletIterator.next();

                if (
                        enemy.getBounds()
                                .intersects(
                                        bullet.getBounds()
                                )
                ) {

                    bulletIterator.remove();

                    enemyIterator.remove();

                    score += 10;

                    createExplosion(
                            enemy.x,
                            enemy.y,
                            20
                    );

                    maybeDropPowerUp(
                            enemy.x,
                            enemy.y
                    );

                    sound.explosion();

                    shakeTime = 7;

                    destroyed = true;

                    break;
                }
            }

            if (destroyed) {
                continue;
            }

            if (
                    enemy.getBounds()
                            .intersects(player)
            ) {

                enemyIterator.remove();

                createExplosion(
                        enemy.x,
                        enemy.y,
                        15
                );

                damagePlayer();
            }
        }
    }

    // =========================
    // 玩家受伤
    // =========================

    private void damagePlayer() {

        if (invincibleTime > 0) {
            return;
        }

        // 护盾
        if (shield) {

            shield = false;

            invincibleTime = 60;

            pickupMessage =
                    "SHIELD BROKEN";

            pickupMessageTime = 70;

            createShieldBreakEffect();

            sound.shieldBreak();

            shakeTime = 8;

            return;
        }

        hp--;

        invincibleTime = 100;

        shakeTime = 15;

        screenFlashTime = 15;

        sound.hit();

        createExplosion(
                playerX,
                playerY,
                14
        );

        if (hp <= 0) {

            state = GameState.GAME_OVER;

            if (score > highScore) {
                highScore = score;
            }

            sound.gameOver();
        }
    }

    // =========================
    // 护盾破碎
    // =========================

    private void createShieldBreakEffect() {

        for (int i = 0; i < 30; i++) {

            double angle =
                    random.nextDouble()
                            * Math.PI * 2;

            double speed =
                    2 + random.nextDouble() * 5;

            particles.add(
                    new Particle(
                            playerX,
                            playerY,
                            Math.cos(angle) * speed,
                            Math.sin(angle) * speed,
                            20 + random.nextInt(20)
                    )
            );
        }
    }

    // =========================
    // 道具拾取特效
    // =========================

    private void createPickupEffect(
            double x,
            double y
    ) {

        for (int i = 0; i < 18; i++) {

            double angle =
                    random.nextDouble()
                            * Math.PI * 2;

            double speed =
                    1 + random.nextDouble() * 4;

            particles.add(
                    new Particle(
                            x,
                            y,
                            Math.cos(angle) * speed,
                            Math.sin(angle) * speed,
                            25 + random.nextInt(15)
                    )
            );
        }
    }

    // =========================
    // 爆炸
    // =========================

    private void createExplosion(
            double x,
            double y,
            int amount
    ) {

        for (int i = 0; i < amount; i++) {

            double angle =
                    random.nextDouble()
                            * Math.PI * 2;

            double speed =
                    1 + random.nextDouble() * 5;

            particles.add(
                    new Particle(
                            x,
                            y,
                            Math.cos(angle) * speed,
                            Math.sin(angle) * speed,
                            20 + random.nextInt(25)
                    )
            );
        }
    }

    // =========================
    // 粒子更新
    // =========================

    private void updateParticles() {

        Iterator<Particle> iterator =
                particles.iterator();

        while (iterator.hasNext()) {

            Particle particle =
                    iterator.next();

            particle.x += particle.vx;
            particle.y += particle.vy;

            particle.vx *= 0.97;
            particle.vy *= 0.97;

            particle.life--;

            if (particle.life <= 0) {
                iterator.remove();
            }
        }
    }

    // =========================
    // 玩家碰撞箱
    // =========================

    private Rectangle getPlayerBounds() {

        return new Rectangle(
                (int) playerX
                        - playerWidth / 2
                        + 5,

                (int) playerY
                        - playerHeight / 2
                        + 5,

                playerWidth - 10,
                playerHeight - 10
        );
    }

    // =========================
    // 开始
    // =========================

    private void startGame() {

        playerX = WIDTH / 2;

        hp = 3;

        score = 0;

        shield = false;

        rapidFireTime = 0;

        bullets.clear();
        enemies.clear();
        particles.clear();
        powerUps.clear();

        enemySpawnTimer = 20;

        left = false;
        right = false;
        shooting = false;

        invincibleTime = 0;

        pickupMessage = "";
        pickupMessageTime = 0;

        state = GameState.PLAYING;

        sound.start();
    }

    // =========================
    // 绘制
    // =========================

    @Override
    protected void paintComponent(
            Graphics graphics
    ) {

        super.paintComponent(graphics);

        Graphics2D g =
                (Graphics2D)
                        graphics.create();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // 屏幕震动
        if (shakeTime > 0) {

            int offsetX =
                    random.nextInt(9) - 4;

            int offsetY =
                    random.nextInt(9) - 4;

            g.translate(
                    offsetX,
                    offsetY
            );
        }

        drawBackground(g);

        if (state == GameState.MENU) {

            drawMenu(g);

        } else {

            drawGame(g);

            if (state == GameState.PAUSED) {
                drawPause(g);
            }

            if (state == GameState.GAME_OVER) {
                drawGameOver(g);
            }
        }

        // 红色受伤闪屏
        if (screenFlashTime > 0) {

            int alpha =
                    Math.min(
                            130,
                            screenFlashTime * 8
                    );

            g.setColor(
                    new Color(
                            255,
                            30,
                            50,
                            alpha
                    )
            );

            g.fillRect(
                    0,
                    0,
                    WIDTH,
                    HEIGHT
            );
        }

        g.dispose();
    }

    // =========================
    // 背景
    // =========================

    private void drawBackground(
            Graphics2D g
    ) {

        g.setColor(Color.BLACK);

        g.fillRect(
                0,
                0,
                WIDTH,
                HEIGHT
        );

        for (Star star : stars) {

            int alpha =
                    80 +
                            (int)
                                    (star.size * 50);

            alpha =
                    Math.min(
                            alpha,
                            255
                    );

            g.setColor(
                    new Color(
                            180,
                            220,
                            255,
                            alpha
                    )
            );

            int size =
                    Math.max(
                            1,
                            (int)
                                    star.size
                    );

            g.fillOval(
                    (int) star.x,
                    (int) star.y,
                    size,
                    size
            );
        }

        // 很淡的横向空间线
        g.setColor(
                new Color(
                        40,
                        80,
                        120,
                        25
                )
        );

        for (
                int y = 0;
                y < HEIGHT;
                y += 80
        ) {

            g.drawLine(
                    0,
                    y,
                    WIDTH,
                    y
            );
        }
    }

    // =========================
    // 游戏
    // =========================

    private void drawGame(
            Graphics2D g
    ) {

        drawBullets(g);

        drawPowerUps(g);

        drawEnemies(g);

        drawPlayer(g);

        drawParticles(g);

        drawHUD(g);

        drawPickupMessage(g);
    }

    // =========================
    // 玩家
    // =========================

    private void drawPlayer(
            Graphics2D g
    ) {

        if (
                invincibleTime > 0 &&
                        frameCount % 8 < 4
        ) {
            return;
        }

        int x = (int) playerX;
        int y = (int) playerY;

        // 玩家移动尾焰
        int flameLength =
                24 +
                        (int)
                                (
                                        Math.sin(
                                                frameCount
                                                        * 0.35
                                        ) * 6
                                );

        Polygon flame =
                new Polygon();

        flame.addPoint(
                x - 9,
                y + 13
        );

        flame.addPoint(
                x,
                y + flameLength
        );

        flame.addPoint(
                x + 9,
                y + 13
        );

        g.setColor(
                new Color(
                        255,
                        120,
                        20,
                        180
                )
        );

        g.fillPolygon(flame);

        // 蓝色尾焰
        Polygon innerFlame =
                new Polygon();

        innerFlame.addPoint(
                x - 4,
                y + 12
        );

        innerFlame.addPoint(
                x,
                y + flameLength - 7
        );

        innerFlame.addPoint(
                x + 4,
                y + 12
        );

        g.setColor(
                new Color(
                        120,
                        220,
                        255
                )
        );

        g.fillPolygon(innerFlame);

        // 飞机主体
        Polygon ship =
                new Polygon();

        ship.addPoint(
                x,
                y - 25
        );

        ship.addPoint(
                x - 23,
                y + 18
        );

        ship.addPoint(
                x - 7,
                y + 13
        );

        ship.addPoint(
                x,
                y + 23
        );

        ship.addPoint(
                x + 7,
                y + 13
        );

        ship.addPoint(
                x + 23,
                y + 18
        );

        g.setColor(
                new Color(
                        70,
                        200,
                        255
                )
        );

        g.fillPolygon(ship);

        // 飞机外轮廓
        g.setColor(
                new Color(
                        180,
                        240,
                        255
                )
        );

        g.setStroke(
                new BasicStroke(2)
        );

        g.drawPolygon(ship);

        // 高光
        Polygon highlight =
                new Polygon();

        highlight.addPoint(
                x,
                y - 20
        );

        highlight.addPoint(
                x - 7,
                y + 10
        );

        highlight.addPoint(
                x,
                y + 5
        );

        highlight.addPoint(
                x + 7,
                y + 10
        );

        g.setColor(Color.WHITE);

        g.fillPolygon(highlight);

        // 核心
        g.setColor(
                new Color(
                        80,
                        150,
                        255
                )
        );

        g.fillOval(
                x - 5,
                y - 3,
                10,
                10
        );

        // 护盾
        if (shield) {

            int pulse =
                    (int)
                            (
                                    Math.sin(
                                            frameCount
                                                    * 0.12
                                    ) * 4
                            );

            int radius =
                    36 + pulse;

            g.setColor(
                    new Color(
                            80,
                            180,
                            255,
                            55
                    )
            );

            g.fillOval(
                    x - radius,
                    y - radius,
                    radius * 2,
                    radius * 2
            );

            g.setColor(
                    new Color(
                            100,
                            220,
                            255,
                            180
                    )
            );

            g.setStroke(
                    new BasicStroke(2)
            );

            g.drawOval(
                    x - radius,
                    y - radius,
                    radius * 2,
                    radius * 2
            );
        }
    }

    // =========================
    // 子弹
    // =========================

    private void drawBullets(
            Graphics2D g
    ) {

        for (Bullet bullet : bullets) {

            // 发光层
            g.setColor(
                    new Color(
                            255,
                            240,
                            80,
                            50
                    )
            );

            g.fillOval(
                    (int) bullet.x - 8,
                    (int) bullet.y - 5,
                    16,
                    25
            );

            // 核心
            g.setColor(
                    Color.YELLOW
            );

            g.fillRoundRect(
                    (int) bullet.x - 2,
                    (int) bullet.y,
                    4,
                    17,
                    4,
                    4
            );
        }
    }

    // =========================
    // 敌机
    // =========================

    private void drawEnemies(
            Graphics2D g
    ) {

        for (Enemy enemy : enemies) {

            int x = (int) enemy.x;
            int y = (int) enemy.y;

            // 敌机红色光晕
            g.setColor(
                    new Color(
                            255,
                            40,
                            80,
                            25
                    )
            );

            g.fillOval(
                    x - 30,
                    y - 30,
                    60,
                    60
            );

            Polygon ship =
                    new Polygon();

            ship.addPoint(
                    x,
                    y + 25
            );

            ship.addPoint(
                    x - 22,
                    y - 15
            );

            ship.addPoint(
                    x - 8,
                    y - 10
            );

            ship.addPoint(
                    x,
                    y - 20
            );

            ship.addPoint(
                    x + 8,
                    y - 10
            );

            ship.addPoint(
                    x + 22,
                    y - 15
            );

            g.setColor(
                    new Color(
                            240,
                            70,
                            90
                    )
            );

            g.fillPolygon(ship);

            g.setColor(
                    new Color(
                            255,
                            140,
                            150
                    )
            );

            g.setStroke(
                    new BasicStroke(2)
            );

            g.drawPolygon(ship);

            // 核心
            g.setColor(
                    new Color(
                            255,
                            220,
                            100
                    )
            );

            g.fillOval(
                    x - 5,
                    y - 2,
                    10,
                    10
            );

            // 翼部
            g.setColor(
                    new Color(
                            180,
                            40,
                            70
                    )
            );

            g.fillRect(
                    x - 24,
                    y - 5,
                    10,
                    18
            );

            g.fillRect(
                    x + 14,
                    y - 5,
                    10,
                    18
            );
        }
    }

    // =========================
    // 道具绘制
    // =========================

    private void drawPowerUps(
            Graphics2D g
    ) {

        for (PowerUp powerUp : powerUps) {

            int x = (int) powerUp.x;
            int y = (int) powerUp.y;

            int pulse =
                    (int)
                            (
                                    Math.sin(
                                            frameCount
                                                    * 0.15
                                    ) * 4
                            );

            int radius =
                    20 + pulse;

            // 光晕
            g.setColor(
                    new Color(
                            100,
                            220,
                            255,
                            25
                    )
            );

            g.fillOval(
                    x - radius - 6,
                    y - radius - 6,
                    (radius + 6) * 2,
                    (radius + 6) * 2
            );

            // 外圈
            g.setColor(
                    powerUp.getColor(
                            100
                    )
            );

            g.fillOval(
                    x - radius,
                    y - radius,
                    radius * 2,
                    radius * 2
            );

            g.setColor(
                    powerUp.getColor(
                            220
                    )
            );

            g.setStroke(
                    new BasicStroke(2)
            );

            g.drawOval(
                    x - radius,
                    y - radius,
                    radius * 2,
                    radius * 2
            );

            // 图标
            g.setColor(Color.WHITE);

            g.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            20
                    )
            );

            String icon =
                    powerUp.getIcon();

            FontMetrics fm =
                    g.getFontMetrics();

            g.drawString(
                    icon,
                    x - fm.stringWidth(icon) / 2,
                    y + 7
            );
        }
    }

    // =========================
    // 粒子
    // =========================

    private void drawParticles(
            Graphics2D g
    ) {

        for (Particle particle :
                particles) {

            int alpha =
                    Math.min(
                            255,
                            particle.life * 10
                    );

            int size =
                    Math.max(
                            2,
                            particle.life / 5
                    );

            g.setColor(
                    new Color(
                            255,
                            180,
                            50,
                            alpha
                    )
            );

            g.fillOval(
                    (int) particle.x
                            - size / 2,

                    (int) particle.y
                            - size / 2,

                    size,
                    size
            );
        }
    }

    // =========================
    // HUD
    // =========================

    private void drawHUD(
            Graphics2D g
    ) {

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        g.setColor(Color.WHITE);

        g.drawString(
                "SCORE  " + score,
                20,
                32
        );

        g.drawString(
                "HIGH  " + highScore,
                20,
                58
        );

        // HP
        g.drawString(
                "HP",
                WIDTH - 130,
                32
        );

        for (int i = 0; i < 3; i++) {

            if (i < hp) {

                g.setColor(
                        new Color(
                                80,
                                220,
                                255
                        )
                );

            } else {

                g.setColor(
                        new Color(
                                60,
                                60,
                                70
                        )
                );
            }

            g.fillRect(
                    WIDTH - 90 + i * 22,
                    18,
                    16,
                    16
            );
        }

        // 护盾状态
        if (shield) {

            g.setColor(
                    new Color(
                            100,
                            220,
                            255
                    )
            );

            g.drawString(
                    "SHIELD",
                    WIDTH - 110,
                    60
            );
        }

        // 射速强化
        if (rapidFireTime > 0) {

            g.setColor(
                    new Color(
                            255,
                            220,
                            70
                    )
            );

            int seconds =
                    rapidFireTime / 60;

            g.drawString(
                    "RAPID " + seconds,
                    WIDTH - 130,
                    85
            );
        }
    }

    // =========================
    // 道具提示
    // =========================

    private void drawPickupMessage(
            Graphics2D g
    ) {

        if (pickupMessageTime <= 0) {
            return;
        }

        int alpha =
                Math.min(
                        255,
                        pickupMessageTime * 4
                );

        g.setColor(
                new Color(
                        255,
                        255,
                        255,
                        alpha
                )
        );

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        FontMetrics fm =
                g.getFontMetrics();

        g.drawString(
                pickupMessage,
                (WIDTH -
                        fm.stringWidth(
                                pickupMessage
                        )) / 2,
                HEIGHT - 60
        );
    }

    // =========================
    // 菜单
    // =========================

    private void drawMenu(
            Graphics2D g
    ) {

        g.setColor(Color.WHITE);

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        52
                )
        );

        String title =
                "MINI SHOOTER";

        FontMetrics fm =
                g.getFontMetrics();

        g.drawString(
                title,
                (WIDTH -
                        fm.stringWidth(title))
                        / 2,
                230
        );

        g.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        22
                )
        );

        String start =
                "PRESS SPACE TO START";

        fm = g.getFontMetrics();

        g.drawString(
                start,
                (WIDTH -
                        fm.stringWidth(start))
                        / 2,
                330
        );

        g.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        16
                )
        );

        String controls =
                "A / D or LEFT / RIGHT    MOVE";

        fm = g.getFontMetrics();

        g.drawString(
                controls,
                (WIDTH -
                        fm.stringWidth(controls))
                        / 2,
                380
        );

        g.drawString(
                "SPACE    SHOOT",
                280,
                410
        );

        g.drawString(
                "ESC    PAUSE",
                295,
                440
        );

        g.setColor(
                new Color(
                        100,
                        220,
                        255
                )
        );

        g.drawString(
                "Destroy enemies and collect power-ups!",
                215,
                500
        );
    }

    // =========================
    // 暂停
    // =========================

    private void drawPause(
            Graphics2D g
    ) {

        g.setColor(
                new Color(
                        0,
                        0,
                        0,
                        170
                )
        );

        g.fillRect(
                0,
                0,
                WIDTH,
                HEIGHT
        );

        g.setColor(Color.WHITE);

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        50
                )
        );

        String text = "PAUSED";

        FontMetrics fm =
                g.getFontMetrics();

        g.drawString(
                text,
                (WIDTH -
                        fm.stringWidth(text))
                        / 2,
                HEIGHT / 2
        );
    }

    // =========================
    // Game Over
    // =========================

    private void drawGameOver(
            Graphics2D g
    ) {

        g.setColor(
                new Color(
                        0,
                        0,
                        0,
                        190
                )
        );

        g.fillRect(
                0,
                0,
                WIDTH,
                HEIGHT
        );

        g.setColor(Color.WHITE);

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        50
                )
        );

        String text =
                "GAME OVER";

        FontMetrics fm =
                g.getFontMetrics();

        g.drawString(
                text,
                (WIDTH -
                        fm.stringWidth(text))
                        / 2,
                280
        );

        g.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        24
                )
        );

        String scoreText =
                "SCORE  " + score;

        fm =
                g.getFontMetrics();

        g.drawString(
                scoreText,
                (WIDTH -
                        fm.stringWidth(
                                scoreText
                        )) / 2,
                330
        );

        String restart =
                "PRESS R TO RESTART";

        fm =
                g.getFontMetrics();

        g.drawString(
                restart,
                (WIDTH -
                        fm.stringWidth(
                                restart
                        )) / 2,
                390
        );
    }

    // =========================
    // 键盘
    // =========================

    @Override
    public void keyPressed(
            KeyEvent e
    ) {

        int key =
                e.getKeyCode();

        if (state == GameState.MENU) {

            if (
                    key ==
                            KeyEvent.VK_SPACE
            ) {
                startGame();
            }

            return;
        }

        if (state == GameState.GAME_OVER) {

            if (
                    key ==
                            KeyEvent.VK_R
            ) {
                startGame();
            }

            return;
        }

        if (
                key ==
                        KeyEvent.VK_A ||
                        key ==
                                KeyEvent.VK_LEFT
        ) {
            left = true;
        }

        if (
                key ==
                        KeyEvent.VK_D ||
                        key ==
                                KeyEvent.VK_RIGHT
        ) {
            right = true;
        }

        if (
                key ==
                        KeyEvent.VK_SPACE
        ) {
            shooting = true;
        }

        if (
                key ==
                        KeyEvent.VK_ESCAPE
        ) {

            if (
                    state ==
                            GameState.PLAYING
            ) {
                state =
                        GameState.PAUSED;

            } else if (
                    state ==
                            GameState.PAUSED
            ) {
                state =
                        GameState.PLAYING;
            }
        }
    }

    @Override
    public void keyReleased(
            KeyEvent e
    ) {

        int key =
                e.getKeyCode();

        if (
                key ==
                        KeyEvent.VK_A ||
                        key ==
                                KeyEvent.VK_LEFT
        ) {
            left = false;
        }

        if (
                key ==
                        KeyEvent.VK_D ||
                        key ==
                                KeyEvent.VK_RIGHT
        ) {
            right = false;
        }

        if (
                key ==
                        KeyEvent.VK_SPACE
        ) {
            shooting = false;
        }
    }

    @Override
    public void keyTyped(
            KeyEvent e
    ) {
    }

    // =========================
    // Star
    // =========================

    private static class Star {

        double x;
        double y;
        double speed;
        double size;

        Star(
                double x,
                double y,
                double speed,
                double size
        ) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
        }
    }

    // =========================
    // Bullet
    // =========================

    private static class Bullet {

        double x;
        double y;

        Bullet(
                double x,
                double y
        ) {
            this.x = x;
            this.y = y;
        }

        Rectangle getBounds() {

            return new Rectangle(
                    (int) x - 3,
                    (int) y,
                    6,
                    17
            );
        }
    }

    // =========================
    // Enemy
    // =========================

    private static class Enemy {

        double x;
        double y;
        double speed;

        int offset;

        Enemy(
                double x,
                double y,
                double speed
        ) {

            this.x = x;
            this.y = y;
            this.speed = speed;

            offset =
                    (int)
                            (
                                    Math.random()
                                            * 1000
                            );
        }

        Rectangle getBounds() {

            return new Rectangle(
                    (int) x - 20,
                    (int) y - 18,
                    40,
                    40
            );
        }
    }

    // =========================
    // PowerUp
    // =========================

    private static class PowerUp {

        enum Type {
            HEALTH,
            SHIELD,
            RAPID_FIRE
        }

        double x;
        double y;

        double angle;

        int offset;

        Type type;

        PowerUp(
                double x,
                double y,
                Type type
        ) {

            this.x = x;
            this.y = y;

            this.type = type;

            this.angle =
                    Math.random()
                            * Math.PI * 2;

            this.offset =
                    (int)
                            (
                                    Math.random()
                                            * 1000
                            );
        }

        Rectangle getBounds() {

            return new Rectangle(
                    (int) x - 18,
                    (int) y - 18,
                    36,
                    36
            );
        }

        String getIcon() {

            return switch (type) {

                case HEALTH ->
                        "+";

                case SHIELD ->
                        "S";

                case RAPID_FIRE ->
                        "R";
            };
        }

        Color getColor(
                int alpha
        ) {

            return switch (type) {

                case HEALTH ->
                        new Color(
                                80,
                                230,
                                120,
                                alpha
                        );

                case SHIELD ->
                        new Color(
                                80,
                                190,
                                255,
                                alpha
                        );

                case RAPID_FIRE ->
                        new Color(
                                255,
                                190,
                                70,
                                alpha
                        );
            };
        }
    }

    // =========================
    // Particle
    // =========================

    private static class Particle {

        double x;
        double y;

        double vx;
        double vy;

        int life;

        Particle(
                double x,
                double y,
                double vx,
                double vy,
                int life
        ) {

            this.x = x;
            this.y = y;

            this.vx = vx;
            this.vy = vy;

            this.life = life;
        }
    }

    // =========================
    // 声音系统
    // =========================

    private static class SoundSystem {

        private void play(
                double frequency,
                int duration,
                double volume
        ) {

            new Thread(() -> {

                try {

                    float sampleRate =
                            44100;

                    AudioFormat format =
                            new AudioFormat(
                                    sampleRate,
                                    8,
                                    1,
                                    true,
                                    false
                            );

                    SourceDataLine line =
                            AudioSystem
                                    .getSourceDataLine(
                                            format
                                    );

                    line.open(format);

                    line.start();

                    byte[] buffer =
                            new byte[1024];

                    int samples =
                            (int)
                                    (
                                            sampleRate
                                                    * duration
                                                    / 1000
                                    );

                    for (
                            int i = 0;
                            i < samples;
                            i++
                    ) {

                        double angle =
                                2 *
                                        Math.PI *
                                        frequency *
                                        i /
                                        sampleRate;

                        double wave =
                                Math.sin(
                                        angle
                                );

                        double fade =
                                1.0 -
                                        (double) i /
                                                samples;

                        buffer[
                                i %
                                        buffer.length
                                ] =
                                (byte)
                                        (
                                                wave *
                                                        127 *
                                                        volume *
                                                        fade
                                        );

                        if (
                                i %
                                        buffer.length
                                        ==
                                        buffer.length - 1
                        ) {

                            line.write(
                                    buffer,
                                    0,
                                    buffer.length
                            );
                        }
                    }

                    line.drain();

                    line.stop();

                    line.close();

                } catch (
                        Exception ignored
                ) {
                }

            }).start();
        }

        void shoot() {
            play(
                    900,
                    45,
                    0.25
            );
        }

        void hit() {
            play(
                    150,
                    120,
                    0.5
            );
        }

        void explosion() {
            play(
                    80,
                    180,
                    0.7
            );
        }

        void start() {
            play(
                    500,
                    100,
                    0.25
            );
        }

        void gameOver() {
            play(
                    100,
                    400,
                    0.5
            );
        }

        void powerUp() {
            play(
                    1000,
                    100,
                    0.3
            );
        }

        void shieldBreak() {
            play(
                    220,
                    180,
                    0.45
            );
        }
    }

    // =========================
    // Main
    // =========================

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame =
                    new JFrame(
                            "Mini Shooter 2.1"
                    );

            MiniShooter game =
                    new MiniShooter();

            frame.setContentPane(game);

            frame.pack();

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setLocationRelativeTo(
                    null
            );

            frame.setResizable(false);

            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }
}