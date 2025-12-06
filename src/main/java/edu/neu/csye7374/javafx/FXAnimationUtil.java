package edu.neu.csye7374.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class FXAnimationUtil {

    // -------------------------------------------------
    // Safe Resource Loader
    // -------------------------------------------------
    private static Image load(String path) {
        return new Image(FXAnimationUtil.class.getResourceAsStream(path));
    }

    // Key in ImageView properties used to store the active Timeline
    private static final String ANIM_KEY = "fx_anim_timeline";

    private static void stopExisting(ImageView view) {
        Object existing = view.getProperties().get(ANIM_KEY);
        if (existing instanceof Timeline t) {
            t.stop();
        }
    }

    private static void storeTimeline(ImageView view, Timeline t) {
        view.getProperties().put(ANIM_KEY, t);
    }

    // ===================================================================
    // CHARACTER SELECT: WARRIOR IDLE (CENTERED BOX, UPSCALED)
    // ===================================================================
    public static void playWarriorIdle(ImageView view, String spriteSheetPath, int speedMs) {

        Image sheet = load(spriteSheetPath);
        view.setImage(sheet);

        // Upscale a bit in the character-select card
        view.setScaleX(2.4);
        view.setScaleY(2.4);

        final int boxW = 96;
        final int boxH = 128;
        final int frames = 7;

        final int warriorActualHeight = 96;
        final int verticalOffset = (boxH - warriorActualHeight) / 2;

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        for (int i = 0; i < frames; i++) {
            int x = i * 96;

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(speedMs * i), e ->
                            view.setViewport(new Rectangle2D(
                                    x,
                                    -verticalOffset,
                                    boxW,
                                    boxH
                            ))
                    )
            );
        }

        stopExisting(view);
        storeTimeline(view, timeline);
        timeline.play();
    }

    // ===================================================================
    // CHARACTER SELECT: MAGE IDLE (CENTERED)
    // ===================================================================
    public static void playMageIdle(ImageView view, String spriteSheetPath, int speedMs) {

        Image sheet = load(spriteSheetPath);
        view.setImage(sheet);

        view.setScaleX(1.35);
        view.setScaleY(1.35);

        final int boxW = 96;
        final int boxH = 128;

        int[] xs     = { 36, 164, 289, 417, 545, 673, 804 };
        int[] widths = { 28,  28,  31,  33,  31,  31,  28 };

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        for (int i = 0; i < xs.length; i++) {

            int idx = i;
            int cropW = widths[idx];
            int leftOffset = xs[idx];

            int horizontalOffset = (boxW - cropW) / 2;

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(speedMs * i), e ->
                            view.setViewport(new Rectangle2D(
                                    leftOffset - horizontalOffset,
                                    0,
                                    boxW,
                                    boxH
                            ))
                    )
            );
        }

        stopExisting(view);
        storeTimeline(view, timeline);
        timeline.play();
    }

    // ===================================================================
    // SIMPLE SWAP EFFECTS (USED IN TITLE/OLDER CODE)
    // ===================================================================
    public static void playAttack(ImageView view, String spritePath) {

        Image attack = load(spritePath);
        Image idle = view.getImage();

        Platform.runLater(() -> view.setImage(attack));

        Timeline t = new Timeline(new KeyFrame(Duration.millis(300), e ->
                Platform.runLater(() -> view.setImage(idle))
        ));
        stopExisting(view);
        storeTimeline(view, t);
        t.play();
    }

    public static void playFireball(ImageView view, String spritePath) {

        Image fireball = load(spritePath);
        Image idle = view.getImage();

        Platform.runLater(() -> view.setImage(fireball));

        Timeline t = new Timeline(new KeyFrame(Duration.millis(550), e ->
                Platform.runLater(() -> view.setImage(idle))
        ));
        stopExisting(view);
        storeTimeline(view, t);
        t.play();
    }

    public static void playHeal(ImageView view, String spritePath) {

        Image heal = load(spritePath);
        Image idle = view.getImage();

        Platform.runLater(() -> view.setImage(heal));

        Timeline t = new Timeline(new KeyFrame(Duration.millis(600), e ->
                Platform.runLater(() -> view.setImage(idle))
        ));
        stopExisting(view);
        storeTimeline(view, t);
        t.play();
    }

    // ===================================================================
    // GENERIC HORIZONTAL SHEET PLAYER (BATTLE)
    // ===================================================================
    private static Timeline playSheet(
            ImageView view,
            String spritePath,
            int frameWidth,
            int frameHeight,
            int yOffset,
            int frames,
            int fps,
            boolean loop,
            Runnable onFinished
    ) {
        Image sheet = load(spritePath);
        view.setImage(sheet);

        Timeline timeline = new Timeline();
        for (int i = 0; i < frames; i++) {
            final int idx = i;
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(1000.0 / fps * i), e -> {
                        view.setViewport(new Rectangle2D(
                                idx * frameWidth,
                                yOffset,
                                frameWidth,
                                frameHeight
                        ));
                    })
            );
        }

        timeline.setCycleCount(loop ? Timeline.INDEFINITE : 1);
        if (!loop && onFinished != null) {
            timeline.setOnFinished(e -> onFinished.run());
        }

        stopExisting(view);
        storeTimeline(view, timeline);
        timeline.play();
        return timeline;
    }

    // ===================================================================
    // GOBLIN (BATTLE)
    // Sheets: 2048 × 256, 8 frames of 256 × 256
    // ===================================================================
    public static void playGoblinIdle(ImageView view, String spritePath, int speedMs) {
        int fps = Math.max(4, 1000 / speedMs); // around 7–8 fps for 130 ms
        playSheet(view, spritePath, 256, 256, 0, 8, fps, true, null);
    }

    public static void playGoblinAttack(ImageView view, String attackSheet, String idleSheet) {
        playSheet(view, attackSheet, 256, 256, 0, 8, 12, false,
                () -> playGoblinIdle(view, idleSheet, 130));
    }

    public static void playGoblinHurt(ImageView view, String hurtSheet, String idleSheet) {
        playSheet(view, hurtSheet, 256, 256, 0, 8, 10, false,
                () -> playGoblinIdle(view, idleSheet, 130));
    }

    public static void playGoblinDeath(ImageView view, String deathSheet) {
        // Stop on last frame
        playSheet(view, deathSheet, 256, 256, 0, 8, 10, false, null);
    }

    // ===================================================================
    // MAGE (BATTLE)
    // mage_staff_attack : 512 × 128 (4 frames)
    // mage_fireball_cast: 1792 × 128 (14 frames)
    // mage_hurt         : 384 × 128 (3 frames)
    // mage_dead         : 768 × 256 (6 frames -> 128×256 each)
    // ===================================================================
    public static void playMageStaffAttack(ImageView view, String staffSheet, String idleSheet) {
        // 4 frames of 128×128
        playSheet(view, staffSheet, 128, 128, 0, 4, 12, false,
                () -> playMageIdle(view, idleSheet, 130));
    }

    public static void playMageFireballAttack(ImageView view, String fireSheet, String idleSheet) {
        playSheet(view, fireSheet, 128, 128, 0, 14, 18, false,
                () -> playMageIdle(view, idleSheet, 130));
    }

    public static void playMageHurt(ImageView view, String hurtSheet, String idleSheet) {
        // tiny crouch effect while hurt
        double originalY = view.getTranslateY();
        view.setTranslateY(originalY + 6);

        playSheet(view, hurtSheet, 128, 128, 0, 3, 10, false, () -> {
            view.setTranslateY(originalY);
            playMageIdle(view, idleSheet, 130);
        });
    }

    public static void playMageDeath(ImageView view, String deathSheet) {
        // Keep last frame (falls down)
        playSheet(view, deathSheet, 128, 256, 0, 6, 10, false, null);
    }

    // ===================================================================
    // WARRIOR (BATTLE)
    // warrior_hurt: 384 × 84 (4 frames, 96×84)
    // For death we reuse hurt sheet and just stop at last frame.
    // ===================================================================
    public static void playWarriorAttack(ImageView view, String attackSheet, String idleSheet) {
        // attack sheet assumed 4 frames of 96×96
        playSheet(view, attackSheet, 96, 96, 0, 4, 12, false,
                () -> playWarriorIdle(view, idleSheet, 130));
    }

    public static void playWarriorHurt(ImageView view, String hurtSheet, String idleSheet) {

        double originalY = view.getTranslateY();
        view.setTranslateY(originalY + 6);

        // 4 frames of 96×84, vertically offset so feet line up
        int offsetY = 0;
        playSheet(view, hurtSheet, 96, 84, offsetY, 4, 10, false, () -> {
            view.setTranslateY(originalY);
            playWarriorIdle(view, idleSheet, 130);
        });
    }

    public static void playWarriorDeath(ImageView view, String hurtSheet) {
        // reuse hurt sheet and stay on last frame
        playSheet(view, hurtSheet, 96, 84, 0, 4, 10, false, null);
    }

    // ===================================================================
    // HEAL OVERLAY (BATTLE)
    // ===================================================================
    public static void playHealOverlay(ImageView heroView,
                                       ImageView overlayView,
                                       String auraPath) {

        if (overlayView == null) {
            // Fallback to old behaviour if overlay not wired
            playHeal(heroView, auraPath);
            return;
        }

        Image aura = load(auraPath);
        overlayView.setImage(aura);

        // Match hero size / position
        overlayView.setVisible(true);
        overlayView.setOpacity(0.0);
        overlayView.setFitWidth(heroView.getFitWidth());
        overlayView.setPreserveRatio(true);
        overlayView.setLayoutX(heroView.getLayoutX());

        // Place the aura roughly at the hero's feet instead of above the head
        // offset ~ +45px from hero top; adjust if you want it lower/higher
        overlayView.setLayoutY(heroView.getLayoutY() + 45);

        Timeline t = new Timeline(
                new KeyFrame(Duration.millis(0),
                        e -> overlayView.setOpacity(0.0)),
                new KeyFrame(Duration.millis(150),
                        e -> overlayView.setOpacity(0.9)),
                new KeyFrame(Duration.millis(550),
                        e -> overlayView.setOpacity(0.0))
        );
        t.setOnFinished(e -> overlayView.setVisible(false));

        stopExisting(overlayView);
        storeTimeline(overlayView, t);
        t.play();
    }
}
