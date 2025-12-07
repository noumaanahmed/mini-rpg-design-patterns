package edu.neu.csye7374.javafx;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class FXAnimationUtil {

    private static final String ANIM_KEY = "fx_anim_timeline";

    // You can tweak these if you want global scaling later
    private static final double WARRIOR_SCALE = 2.4;
    private static final double MAGE_SCALE    = 1.35;
    private static final double GOBLIN_SCALE  = 1.8;  // <--- goblin slightly smaller

    // ------------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------------
    private static Image load(String path) {
        return new Image(FXAnimationUtil.class.getResourceAsStream(path));
    }

    private static void stopExisting(ImageView view) {
        Object existing = view.getProperties().get(ANIM_KEY);
        if (existing instanceof Timeline t) {
            t.stop();
        }
    }

    private static void storeTimeline(ImageView view, Timeline t) {
        view.getProperties().put(ANIM_KEY, t);
    }

    // ------------------------------------------------------------------------
    // WARRIOR IDLE
    // ------------------------------------------------------------------------
    public static void playWarriorIdle(ImageView view, String spriteSheetPath, int speedMs) {
        Image sheet = load(spriteSheetPath);
        view.setImage(sheet);

        view.setScaleX(WARRIOR_SCALE);
        view.setScaleY(WARRIOR_SCALE);

        final int boxW = 96;
        final int boxH = 84;
        final int frames = 7;

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        for (int i = 0; i < frames; i++) {
            int x = i * boxW;
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(speedMs * i), e ->
                            view.setViewport(new Rectangle2D(x, 0, boxW, boxH))
                    )
            );
        }

        stopExisting(view);
        storeTimeline(view, timeline);
        timeline.play();
    }

    // ------------------------------------------------------------------------
    // MAGE IDLE
    // ------------------------------------------------------------------------
    public static void playMageIdle(ImageView view, String spriteSheetPath, int speedMs) {
        Image sheet = load(spriteSheetPath);
        view.setImage(sheet);

        view.setScaleX(MAGE_SCALE);
        view.setScaleY(MAGE_SCALE);

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

    // ------------------------------------------------------------------------
    // SIMPLE SWAP HELPERS (fallbacks – used if overlay is null)
    // ------------------------------------------------------------------------
    public static void playAttack(ImageView view, String spritePath) {
        Image attack = load(spritePath);
        Image idle   = view.getImage();

        Platform.runLater(() -> view.setImage(attack));

        Timeline t = new Timeline(
                new KeyFrame(Duration.millis(420), e ->
                        Platform.runLater(() -> view.setImage(idle))
                )
        );
        stopExisting(view);
        storeTimeline(view, t);
        t.play();
    }

    public static void playFireball(ImageView view, String spritePath) {
        Image fireball = load(spritePath);
        Image idle     = view.getImage();

        Platform.runLater(() -> view.setImage(fireball));

        Timeline t = new Timeline(
                new KeyFrame(Duration.millis(650), e ->
                        Platform.runLater(() -> view.setImage(idle))
                )
        );
        stopExisting(view);
        storeTimeline(view, t);
        t.play();
    }

    public static void playHeal(ImageView view, String spritePath) {
        Image heal = load(spritePath);
        Image idle = view.getImage();

        Platform.runLater(() -> view.setImage(heal));

        Timeline t = new Timeline(
                new KeyFrame(Duration.millis(580), e ->
                        Platform.runLater(() -> view.setImage(idle))
                )
        );
        stopExisting(view);
        storeTimeline(view, t);
        t.play();
    }

    // ------------------------------------------------------------------------
    // GENERIC SHEET PLAYER (UNIFORM FRAMES)
    // ------------------------------------------------------------------------
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
                    new KeyFrame(Duration.millis(1000.0 / fps * i), e ->
                            view.setViewport(new Rectangle2D(
                                    idx * frameWidth,
                                    yOffset,
                                    frameWidth,
                                    frameHeight
                            ))
                    )
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

    // ------------------------------------------------------------------------
    // HERO ATTACKS (ANIMATED, FULL CYCLES)
    // ------------------------------------------------------------------------
    public static void playWarriorAttack(ImageView view,
                                         String spritePath,
                                         String idlePath) {
        view.setScaleX(WARRIOR_SCALE);
        view.setScaleY(WARRIOR_SCALE);
        // 6 frames, 96x84
        playSheet(view, spritePath, 96, 84, 0, 6, 11, false,
                () -> playWarriorIdle(view, idlePath, 200));
    }

    public static void playMageStaffAttack(ImageView view,
                                           String spritePath,
                                           String idlePath) {
        view.setScaleX(MAGE_SCALE);
        view.setScaleY(MAGE_SCALE);
        // 4 frames, slightly slower so it's readable
        playSheet(view, spritePath, 128, 128, 0, 4, 7, false,
                () -> playMageIdle(view, idlePath, 200));
    }

    public static void playMageFireball(ImageView view,
                                        String spritePath,
                                        String idlePath) {
        view.setScaleX(MAGE_SCALE);
        view.setScaleY(MAGE_SCALE);
        // 14 frames, full breath cycle
        playSheet(view, spritePath, 128, 128, 0, 14, 14, false,
                () -> playMageIdle(view, idlePath, 200));
    }

    // ------------------------------------------------------------------------
    // GOBLIN (battle) — facing LEFT, slightly smaller
    // ------------------------------------------------------------------------
    public static void playGoblinIdle(ImageView view, String spritePath, int speedIgnored) {
        int fps = 8;
        view.setScaleX(-GOBLIN_SCALE); // negative → flipped left
        view.setScaleY(GOBLIN_SCALE);

        playSheet(view, spritePath, 256, 256, 0, 3, fps, true, null);
    }

    public static void playGoblinAttack(ImageView view, String attackSheet, String idleSheet) {
        view.setScaleX(-GOBLIN_SCALE);
        view.setScaleY(GOBLIN_SCALE);

        playSheet(view, attackSheet, 256, 256, 0, 7, 11, false,
                () -> playGoblinIdle(view, idleSheet, 0));
    }

    public static void playGoblinHurt(ImageView view, String hurtSheet, String idleSheet) {
        view.setScaleX(-GOBLIN_SCALE);
        view.setScaleY(GOBLIN_SCALE);

        playSheet(view, hurtSheet, 256, 256, 0, 7, 8, false,
                () -> playGoblinIdle(view, idleSheet, 0));
    }

    public static void playGoblinDeath(ImageView view, String deathSheet) {
        view.setScaleX(-GOBLIN_SCALE);
        view.setScaleY(GOBLIN_SCALE);

        playSheet(view, deathSheet, 256, 256, 0, 8, 8, false, null);
    }

    // ------------------------------------------------------------------------
    // WARRIOR HURT / DEATH
    // ------------------------------------------------------------------------
    public static void playWarriorHurt(ImageView view, String hurtSheet, String idleSheet) {
        view.setScaleX(WARRIOR_SCALE);
        view.setScaleY(WARRIOR_SCALE);

        double originalY = view.getTranslateY();
        view.setTranslateY(originalY + 6);

        playSheet(view, hurtSheet, 96, 84, 0, 4, 10, false, () -> {
            view.setTranslateY(originalY);
            playWarriorIdle(view, idleSheet, 200);
        });
    }

    public static void playWarriorDeath(ImageView view, String deathSheet) {
        view.setScaleX(WARRIOR_SCALE);
        view.setScaleY(WARRIOR_SCALE);
        playSheet(view, deathSheet, 96, 84, 0, 10, 8, false, null);
    }

    // ------------------------------------------------------------------------
    // MAGE HURT / DEATH
    // ------------------------------------------------------------------------
    public static void playMageHurt(ImageView view, String hurtSheet, String idleSheet) {
        view.setScaleX(MAGE_SCALE);
        view.setScaleY(MAGE_SCALE);

        double originalY = view.getTranslateY();
        view.setTranslateY(originalY + 6);

        // slightly slower than before so the hurt is readable
        playSheet(view, hurtSheet, 128, 128, 0, 3, 8, false, () -> {
            view.setTranslateY(originalY);
            playMageIdle(view, idleSheet, 200);
        });
    }

    public static void playMageDeath(ImageView view, String deathSheet) {
        view.setScaleX(MAGE_SCALE);
        view.setScaleY(MAGE_SCALE);
        playSheet(view, deathSheet, 128, 128, 0, 6, 8, false, null);
    }

    // ------------------------------------------------------------------------
    // HEAL AURA OVERLAY (sprite sheet)
    // ------------------------------------------------------------------------
    public static void playHealOverlay(ImageView heroView,
                                       ImageView overlayView,
                                       String auraPath) {
        playHealingAura(heroView, overlayView, auraPath, null);
    }

    public static void playHealingAura(ImageView heroView,
                                       ImageView overlayView,
                                       String auraSheetPath,
                                       Pane battlePane) {

        if (overlayView == null) {
            playHeal(heroView, auraSheetPath);
            return;
        }

        Image auraSheet = load(auraSheetPath);
        overlayView.setImage(auraSheet);
        overlayView.setVisible(true);
        overlayView.setOpacity(1.0);
        overlayView.setPreserveRatio(false);

        double heroWidth  = heroView.getFitWidth()  > 0 ? heroView.getFitWidth()
                : heroView.getBoundsInParent().getWidth();
        double heroHeight = heroView.getFitHeight() > 0 ? heroView.getFitHeight()
                : heroView.getBoundsInParent().getHeight();

        double auraWidth  = heroWidth * 1.4;
        double auraHeight = heroHeight * 0.45;

        overlayView.setFitWidth(auraWidth);
        overlayView.setFitHeight(auraHeight);

        double heroCenterX = heroView.getLayoutX() + heroWidth / 2.0;
        double heroBottomY = heroView.getLayoutY() + heroHeight;

        overlayView.setLayoutX(heroCenterX - auraWidth / 2.0);
        overlayView.setLayoutY(heroBottomY - auraHeight * 0.6);

        final int frames = 8;
        final int frameW = 72;
        final int frameH = 72;
        final int fps = 10;
        final double frameDuration = 1000.0 / fps;

        Timeline sheetTimeline = new Timeline();
        for (int i = 0; i < frames; i++) {
            final int idx = i;
            sheetTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(frameDuration * i), e ->
                            overlayView.setViewport(new Rectangle2D(
                                    idx * frameW,
                                    0,
                                    frameW,
                                    frameH
                            ))
                    )
            );
        }

        sheetTimeline.setCycleCount(2);
        sheetTimeline.setOnFinished(e -> overlayView.setVisible(false));

        stopExisting(overlayView);
        storeTimeline(overlayView, sheetTimeline);
        sheetTimeline.play();
    }

    // ------------------------------------------------------------------------
    // (These helpers are currently unused but kept for extensibility)
    // ------------------------------------------------------------------------
    private static void createGlowPulse(ImageView heroView,
                                       Pane battlePane,
                                       double auraWidth,
                                       double auraHeight) {

        if (battlePane == null) return;

        double heroWidth  = heroView.getFitWidth()  > 0 ? heroView.getFitWidth()
                : heroView.getBoundsInParent().getWidth();
        double heroHeight = heroView.getFitHeight() > 0 ? heroView.getFitHeight()
                : heroView.getBoundsInParent().getHeight();

        double heroCenterX = heroView.getLayoutX() + heroWidth / 2.0;
        double heroBottomY = heroView.getLayoutY() + heroHeight;

        Rectangle glow = new Rectangle(auraWidth * 1.15, auraHeight * 1.3);
        glow.setArcWidth(18);
        glow.setArcHeight(18);
        glow.setFill(Color.web("#7CFF8A", 0.45));

        glow.setLayoutX(heroCenterX - glow.getWidth() / 2.0);
        glow.setLayoutY(heroBottomY - glow.getHeight());

        int overlayIndex = battlePane.getChildren().indexOf(heroView);
        if (overlayIndex >= 0) {
            battlePane.getChildren().add(overlayIndex, glow);
        } else {
            battlePane.getChildren().add(glow);
        }

        Timeline glowTimeline = new Timeline(
                new KeyFrame(Duration.millis(0),    e -> glow.setOpacity(0.0)),
                new KeyFrame(Duration.millis(120),  e -> glow.setOpacity(0.9)),
                new KeyFrame(Duration.millis(700),  e -> glow.setOpacity(0.0))
        );
        glowTimeline.setOnFinished(e -> battlePane.getChildren().remove(glow));
        glowTimeline.play();
    }

    private static void createFloatingText(ImageView heroView,
                                           Pane battlePane,
                                           String text) {

        if (battlePane == null) return;

        double heroWidth  = heroView.getFitWidth()  > 0 ? heroView.getFitWidth()
                : heroView.getBoundsInParent().getWidth();
        double heroHeight = heroView.getFitHeight() > 0 ? heroView.getFitHeight()
                : heroView.getBoundsInParent().getHeight();

        double heroCenterX = heroView.getLayoutX() + heroWidth / 2.0;
        double heroTopY    = heroView.getLayoutY();

        Label hpLabel = new Label(text);
        hpLabel.setStyle(
                "-fx-text-fill: #99ff99;" +
                "-fx-font-size: 18;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(one-pass-box, black, 4, 0, 0, 0);"
        );

        hpLabel.setLayoutX(heroCenterX - 24);
        hpLabel.setLayoutY(heroTopY - 10);

        battlePane.getChildren().add(hpLabel);

        TranslateTransition moveUp = new TranslateTransition(Duration.millis(700), hpLabel);
        moveUp.setFromY(0);
        moveUp.setToY(-25);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(700), hpLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ParallelTransition combo = new ParallelTransition(moveUp, fadeOut);
        combo.setOnFinished(e -> battlePane.getChildren().remove(hpLabel));
        combo.play();
    }
}
