//Johan Ehrencrona joeh2789

import javafx.scene.paint.Color;

public enum Category {
    Bus(Color.RED),
    Underground(Color.BLUE),
    Train(Color.GREEN),
    None(Color.BLACK);

    private final Color color;

    Category(Color color) {
        this.color = color;
    }

    public Color getColor(){
        return color;
    }
}
