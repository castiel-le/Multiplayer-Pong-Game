package com.mycompany.multiplayer_pong;

import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import static java.lang.Math.abs;
import static java.lang.Math.signum;
import javafx.geometry.Point2D;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class BallComponent extends Component{
    private PhysicsComponent physics;

    @Override
    public void onUpdate(double tpf) {
        limitVelocity();
        checkOffscreen();
    }

    private void limitVelocity() {
        // we don't want the ball to move too slow in X direction
        if (abs(physics.getVelocityX()) < 5 * 60) {
            physics.setVelocityX(signum(physics.getVelocityX()) * 5 * 60);
        }

        // we don't want the ball to move too fast in Y direction
        if (abs(physics.getVelocityY()) > 5 * 60 * 2) {
            physics.setVelocityY(signum(physics.getVelocityY()) * 5 * 60);
        }
    }

    // this is a hack:
    // we use a physics engine, so it is possible to push the ball through a wall to outside of the screen
    private void checkOffscreen() {
        if (getEntity().getBoundingBoxComponent().isOutside(getGameScene().getViewport().getVisibleArea())) {
            physics.overwritePosition(new Point2D(
                    getAppWidth() / 2,
                    getAppHeight() / 2
            ));
        }
    }
}
