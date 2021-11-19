package com.mycompany.multiplayer_pong;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getip;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.beans.binding.Bindings;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Castiel Le
 */
public class MultiplayerPongFactory implements EntityFactory{
    
    /**
     *
     * @param data
     * @return
     */
    @Spawns("player")
    public Entity newPlayer(SpawnData data){
        boolean isServer = data.get("isServer");
        if(isServer){
            PhysicsComponent physics = new PhysicsComponent();
            physics.setBodyType(BodyType.KINEMATIC);
        }
        
        return entityBuilder(data)
                .type(EntityType.PLAYER)
                .viewWithBBox(new Rectangle(20, 70, Color.WHITE))
                .with(new CollidableComponent(true))
                .with(new NetworkComponent())
                .build();
    }
    
    /**
     *
     * @param data
     * @return
     */
    @Spawns("ball")
    public Entity newBall(SpawnData data){
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().density(0.3f).restitution(0.1f));
        physics.setOnPhysicsInitialized(() -> physics.setLinearVelocity(5 * 60, -5 * 60));
        
        var endGame = getip("player1score").isEqualTo(10).or(getip("player2score").isEqualTo(10));

        ParticleEmitter emitter = ParticleEmitters.newFireEmitter();
        emitter.startColorProperty().bind(
                Bindings.when(endGame)
                        .then(Color.LIGHTYELLOW)
                        .otherwise(Color.LIGHTYELLOW)
        );

        emitter.endColorProperty().bind(
                Bindings.when(endGame)
                        .then(Color.RED)
                        .otherwise(Color.LIGHTBLUE)
        );

        emitter.setBlendMode(BlendMode.SRC_OVER);
        emitter.setSize(5, 10);
        emitter.setEmissionRate(1);
        
        return entityBuilder(data)
                .type(EntityType.BALL)
                .bbox(new HitBox(BoundingShape.circle(5)))
                .with(new CollidableComponent(true))
                .with(new NetworkComponent())
                .build();
    }
}