//Johan Ehrencrona joeh2789


import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;


public abstract class Place extends Canvas {
    private Position position;
    private String name;
    private Category category;
    private ClickHandler cl = new ClickHandler();
    private boolean marked;
    private Polyline triangle = new Polyline();




    Place(String name, Position position, String category){
        this.name = name;
        this.position = position;
        this.category = Category.valueOf(category);
    }

    public Color getColor() {
        return category.getColor();
    }

    public String getName() {
        return name;
    }

    public Position getPosition(){
        return position;
    }

    public boolean getMarked(){
        return marked;
    }

    public Polyline getTriangle() {
        return triangle;
    }

    public String getCategory() {
        return  String.valueOf(category);
    }

    public void createTriangle(Pane pane){
        triangle.getPoints().addAll(0.0, 0.0, 25.0, 0.0, 12.5, 25.0);
        triangle.setFill(getColor());
        triangle.setStroke(getColor());
        pane.getChildren().add(triangle);
        triangle.setLayoutX(position.getCoordinateX()-12.5);
        triangle.setLayoutY(position.getCoordinateY()-25.0);
        triangle.setOnMouseClicked(cl);
    }

    class ClickHandler implements EventHandler<MouseEvent> {
        @Override public void handle(MouseEvent event) {
            if (event.getButton() == MouseButton.PRIMARY){
                if (marked){
                    setUnMarked();
                }else{setMarked();}
            }
            if (event.getButton() == MouseButton.SECONDARY){
                infoAlert();
            }
        }
    }

    public void infoAlert(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText("Name: " + name +" ["+ position.getCoordinateX() +", "+ position.getCoordinateY()+"]");
        alert.showAndWait();
    }

    public void setMarked(){
        marked = true;
        triangle.setFill(Color.YELLOW);
        triangle.setStroke(Color.YELLOW);
        Main.setMarked(category, this);
    }

    public void setUnMarked(){
        marked = false;
        triangle.setFill(getColor());
        triangle.setStroke(getColor());
    }

    public void setVisible(){
        triangle.setVisible(true);
    }

    public void setInvisible(){
        triangle.setVisible(false);
    }


}
