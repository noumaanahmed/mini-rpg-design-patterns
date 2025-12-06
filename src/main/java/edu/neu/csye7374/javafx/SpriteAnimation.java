package edu.neu.csye7374.javafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class SpriteAnimation {

    private int frameCount;
    private int frameWidth;
    private int frameHeight;
    private Timeline timeline;

    public SpriteAnimation(ImageView imageView, Image spritesheet,
                           int frameWidth, int frameHeight, int frameCount,
                           int fps) {

        this.frameCount = frameCount;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;

        imageView.setImage(spritesheet);

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        for (int i = 0; i < frameCount; i++) {
            int index = i;

            KeyFrame frame = new KeyFrame(
                Duration.millis(1000.0 / fps * i),
                e -> imageView.setViewport(
                        new Rectangle2D(index * frameWidth, 0, frameWidth, frameHeight)
                )
            );

            timeline.getKeyFrames().add(frame);
        }
    }

    public void play() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}
