package com.mycompany.multiplayer_pong;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Castiel Le
 */
public class MultiplayerPongFactory implements EntityFactory{
    
    @Spawns("player")
    public Entity newPlayer(SpawnData data){
        return entityBuilder(data)
                .type(EntityType.PLAYER)
                .viewWithBBox(new Rectangle(20, 70, Color.WHITE))
                .with(new CollidableComponent(true))
                .with(new NetworkComponent())
                .build();
    }
    
    @Spawns("ball")
    public Entity newBall(SpawnData data){
        return entityBuilder(data)
                .type(EntityType.BALL)
                .bbox(new HitBox(BoundingShape.circle(5)))
                .with(new CollidableComponent(true))
                .with(new NetworkComponent())
                .build();
    }
}
