//Johan Ehrencrona joeh2789

import javafx.scene.control.Alert;

public class DescribedPlace extends Place{
    private String description;


    DescribedPlace(String name, Position position, String category, String description) {
        super(name, position, category);
        this.description = description;
    }
    @Override
    public void infoAlert(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText("Name: " + getName() +"["+ getPosition().getCoordinateX() +", "+ getPosition().getCoordinateY()+"]\nDescription: " + description);
        alert.showAndWait();
    }

    public String getDescription() {
        return description;
    }
}
