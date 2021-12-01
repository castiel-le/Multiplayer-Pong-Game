package com.mycompany.multiplayer_pong;

import com.almasb.fxgl.dsl.FXGL;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 *
 * @author Castiel Le
 */
public class MultiplayerPongGameBtn extends StackPane{
        private static final Color SELECTED = Color.BLACK;
    private static final Color NOT_SELECTED = Color.GRAY;

    private String name;
    private Runnable action;

    private Text text;
    
    public MultiplayerPongGameBtn(String name, Runnable action){
        this.name = name;
        this.action = action;

        text = FXGL.getUIFactoryService().newText(name, Color.BLACK, 20.0);

        text.fillProperty().bind(Bindings.when(focusedProperty()).then(SELECTED).otherwise(NOT_SELECTED));

        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);

        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER){
                action.run();
            }
        });
        getChildren().addAll(text);
    }
}
