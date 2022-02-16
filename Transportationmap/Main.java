//Johan Ehrencrona joeh2789

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.*;
import java.util.*;

public class Main extends Application{

    private static List<Place> placeMark = new ArrayList<>();
    private boolean unsavedChanges;

    private HBox topMenu = new HBox(5);
    private VBox header = new VBox(5);
    private GridPane categories = new GridPane();
    private BorderPane root = new BorderPane();
    private VBox centerBox = new VBox();
    private StackPane imageBox = new StackPane();
    private Pane mapBox = new Pane();
    private ListView<String> categoriesList = new ListView<>();

    private Map<Position, Place> placePos = new HashMap<>();
    private Map<String, List<Place>> nameList = new HashMap<>();
    private Map<Category, List<Place>> categoryListMap = new HashMap<>();


    @Override
    public void start(Stage primaryStage) {
        header.setAlignment(Pos.TOP_CENTER);
        topMenu.setAlignment(Pos.TOP_CENTER);
        categories.setAlignment(Pos.BASELINE_RIGHT);
        BorderPane.setAlignment(categories, Pos.CENTER);
        topMenu.setPadding(new Insets(5, 5, 5, 5));
        centerBox.getChildren().add(topMenu);
        centerBox.getChildren().add(mapBox);
        root.setCenter(centerBox);
        root.setTop(header);
        root.setRight(categories);
        header.prefWidthProperty().bind(primaryStage.widthProperty());
        showHeaderMenu();
        showTopMenu();
        showCategories();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("");
        primaryStage.sizeToScene();

        primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);

    }

    public static void main(String[] args){
        Application.launch(args);
    }

    private void showHeaderMenu(){
        MenuBar menuBar = new MenuBar();
        header.getChildren().add(menuBar);
        Menu fileMenu = new Menu("File");

        menuBar.getMenus().add(fileMenu);

        MenuItem menuLoadMap = new MenuItem("Load Map");
        MenuItem menuLoadPlaces = new MenuItem("Load Places");
        MenuItem menuSave = new MenuItem("Save");
        MenuItem menuExit = new MenuItem("Exit");
        fileMenu.getItems().addAll(menuLoadMap,menuLoadPlaces,menuSave,menuExit);

        menuLoadMap.setOnAction(new EventHandler<ActionEvent>(){
            private boolean imgLoaded;
            public void handle (ActionEvent t){
                if(newImgCheck(imgLoaded)) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Open Map File");
                    File file = fileChooser.showOpenDialog(root.getScene().getWindow());
                    Image map = new Image(file.toURI().toString());
                    ImageView mapImage = new ImageView(map);
                    imageBox.getChildren().add(mapImage);
                    mapBox.getChildren().add(mapImage);
                    imgLoaded = true;
                }
            }
        });

        menuLoadPlaces.setOnAction(new EventHandler<ActionEvent>(){
            private boolean fileLoaded;
            public void handle (ActionEvent t){
                if(newPlacesCheck(fileLoaded)){
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Open Location File");
                    File file = fileChooser.showOpenDialog(root.getScene().getWindow());
                    try {
                        Scanner input = new Scanner(file);
                        input.useDelimiter(",|\n");
                        while (input.hasNext()) {
                            String placeType = input.next();
                            String category = input.next();
                            int coordinateX = input.nextInt();
                            int coordinateY = input.nextInt();
                            String name = input.next().replaceAll("\\s+", "");
                            if (!placePos.containsKey(createPosition(coordinateX, coordinateY))) {
                                Position position = createPosition(coordinateX, coordinateY);
                                if (placeType.equals("Described")) {
                                    String description = input.next();
                                    createDescribedPlace(position, name, category, description);
                                } else {
                                    createNamedPlace(position, name, category);
                                }
                            } else {
                                positionAlert();
                            }
                        }
                        fileLoaded = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        menuSave.setOnAction(new EventHandler<ActionEvent>(){
            public void handle (ActionEvent t){
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Map File");
                File file = fileChooser.showOpenDialog(root.getScene().getWindow());
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    PrintWriter printWriter = new PrintWriter(fileWriter);
                    for (Place place : placePos.values()) {
                        if(place.getClass() == NamedPlace.class) {
                            printWriter.printf("Named" + "," + place.getCategory() + "," + place.getPosition().getCoordinateX() + "," + place.getPosition().getCoordinateY() + "," + place.getName() + "\n");
                        }else{
                            DescribedPlace descPlace = (DescribedPlace) place;
                            printWriter.printf("Described" + "," + place.getCategory() + "," + place.getPosition().getCoordinateX() + "," + place.getPosition().getCoordinateY() + "," + place.getName() +","+ descPlace.getDescription()+ "\n");
                        }
                    }
                    printWriter.close();
                    setSavedChanges();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        menuExit.setOnAction(new EventHandler<ActionEvent>(){
            public void handle (ActionEvent t) {
                if (unsavedChanges) {
                    if (unsavedChangesAlert().get().equals(ButtonType.OK)) {
                        Platform.exit();
                    }
                }else{
                    Platform.exit();
                }
            }
        });
    }

    private void showTopMenu(){
        TextField searchText = new TextField();
        searchText.setPromptText("Search");

        Button searchButton = new Button("Search");
        Button hideButton = new Button("Hide");
        Button removeButton = new Button("Remove");
        Button coordinatesButton = new Button("Coordinates");
        Button newButton = new Button("New");


        VBox locationCheck = new VBox(5);
        final ToggleGroup group = new ToggleGroup();
        RadioButton namedRadioButton = new RadioButton("Named");
        RadioButton describedRadioButton = new RadioButton("Described");
        namedRadioButton.setToggleGroup(group);
        describedRadioButton.setToggleGroup(group);
        namedRadioButton.setSelected(true);

        locationCheck.getChildren().addAll(namedRadioButton,describedRadioButton);
        topMenu.getChildren().addAll(newButton,locationCheck, searchText,searchButton,hideButton,removeButton,coordinatesButton);

        newButton.setOnAction(new EventHandler<ActionEvent>(){
            public void handle (ActionEvent t){
                mapBox.getScene().setCursor(Cursor.CROSSHAIR);
                mapBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent event) {
                        root.getScene().setCursor(Cursor.DEFAULT);
                        if (!placePos.containsKey(createPosition((int)event.getSceneX(), (int)event.getSceneY()))) {
                            if (namedRadioButton.isSelected()) {
                                newNamedPlace(event);
                            }
                            if (describedRadioButton.isSelected()) {
                                newDescribedPlace(event);
                            }
                            mapBox.setOnMouseClicked(null);
                            setUnSavedChanges();
                        }else{
                            positionAlert();
                        }
                    }
                });
            }
        });

        hideButton.setOnAction(new EventHandler<ActionEvent>(){
            public void handle (ActionEvent t){
                ListIterator<Place> places = placeMark.listIterator();
                while (places.hasNext()) {
                    Place place = places.next();
                    place.setInvisible();
                    places.remove();
                }
            }
        });

        removeButton.setOnAction(new EventHandler<ActionEvent>(){
            public void handle (ActionEvent t){
                ListIterator<Place> places = placeMark.listIterator();
                while (places.hasNext()) {
                    Place place = places.next();
                    mapBox.getChildren().remove(place.getTriangle());
                    placePos.remove(place.getPosition());
                    places.remove();
                    categoryListMap.remove(Category.valueOf(place.getCategory()), place);
                    place=null;
                    setUnSavedChanges();
                }
            }
        });

        searchButton.setOnAction(new EventHandler<ActionEvent>(){
            public void handle (ActionEvent t){
                String name = searchText.getText();
                unMarkAll();
                if (nameList.containsKey(name)){
                    ListIterator<Place> places = nameList.get(name).listIterator();
                    while (places.hasNext()) {
                        Place place = places.next();
                        place.setVisible();
                        place.setMarked();
                        placeMark.add(place);
                    }
                }

            }
        });

        coordinatesButton.setOnAction(new EventHandler<ActionEvent>(){
            public void handle (ActionEvent t){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Input Coordinates");
                alert.setHeaderText("");
                GridPane grid = new GridPane();

                Label coordinateLabelX = new Label("x: ");
                TextField textFieldX = new TextField();
                grid.add(coordinateLabelX, 1,1);
                grid.add(textFieldX, 2,1);

                Label coordinateLabelY = new Label("y: ");
                TextField textFieldY = new TextField();
                grid.add(coordinateLabelY, 1,2);
                grid.add(textFieldY, 2,2);

                alert.getDialogPane().setContent(grid);
                Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK && textFieldX.getText().matches("([0-9]*)") && textFieldY.getText().matches("([0-9]*)")) {
                        int x = Integer.parseInt(textFieldX.getText());
                        int y = Integer.parseInt(textFieldY.getText());
                        if (placePos.containsKey(createPosition(x,y))){
                            unMarkAll();
                            placePos.get(createPosition(x,y)).setVisible();
                            placePos.get(createPosition(x,y)).setMarked();
                        }else{
                            noPositionAlert();
                        }
                }
            }
        });


    }

    private void showCategories(){

        Label categoryLabel = new Label("Categories");

        categoriesList.getItems().addAll("Bus","Underground","Train");

        Button hideCategoriesButton = new Button("Hide Categories");

        categories.add(categoryLabel,1,1);
        categories.add(categoriesList,1,2);
        categories.add(hideCategoriesButton,1,3);
        categories.setHalignment(categoryLabel, HPos.CENTER); // To align horizontally in the cell
        categories.setHalignment(hideCategoriesButton, HPos.CENTER); // To align horizontally in the cell
        categories.setMaxSize(150.0,325.0);
        categories.setPadding(new Insets(100, 0, 100, 0));

        hideCategoriesButton.setOnAction(new EventHandler<ActionEvent>(){
            public void handle (ActionEvent t){
                Category category = Category.valueOf(categoryCheck());
                ListIterator<Place> places = categoryListMap.get(category).listIterator();
                while (places.hasNext()) {
                    Place place = places.next();
                    place.setInvisible();
                    placeMark.remove(place);
                }
            }
        });
        categoriesList.setOnMouseClicked(new EventHandler<MouseEvent>(){
                public void handle (MouseEvent t){
                    Category category = Category.valueOf(categoryCheck());
                    ListIterator<Place> places = categoryListMap.get(category).listIterator();
                    while (places.hasNext()) {
                        Place place = places.next();
                        place.setVisible();
                    }
                }
        });
    }

    private void newNamedPlace(MouseEvent event){
        Alert addNew = new Alert(Alert.AlertType.CONFIRMATION);
        addNew.setTitle("New Place");
        addNew.setHeaderText("");
        GridPane grid = new GridPane();

        Label nameLabel = new Label("Name: ");
        nameLabel.setStyle("-fx-font-weight: bold");
        TextField nameTextField = new TextField();
        grid.add(nameLabel, 1,1);
        grid.add(nameTextField, 2,1);

        addNew.getDialogPane().setContent(grid);
        Optional<ButtonType> result = addNew.showAndWait();
        if (result.get() == ButtonType.OK){
            if (!nameTextField.getText().isEmpty() && !placePos.containsKey(createPosition((int)event.getSceneX(), (int)event.getSceneY()))) {
                createNamedPlace(createPosition((int) event.getSceneX(), (int) event.getSceneY() - 75), nameTextField.getText(), categoryCheck());
            }else{
                positionAlert();
            }
        }
    }

    private void newDescribedPlace(MouseEvent event){
        Alert addNew = new Alert(Alert.AlertType.CONFIRMATION);
        addNew.setTitle("New Place");
        addNew.setHeaderText("");
        GridPane grid = new GridPane();

        Label nameLabel = new Label("Name: ");
        nameLabel.setStyle("-fx-font-weight: bold");
        TextField nameTextField = new TextField();
        grid.add(nameLabel, 1,1);
        grid.add(nameTextField, 2,1);

        Label descLabel = new Label("Description: ");
        descLabel.setStyle("-fx-font-weight: bold");
        TextField descTextField = new TextField();
        grid.add(descLabel, 1,2);
        grid.add(descTextField, 2,2);

        addNew.getDialogPane().setContent(grid);
        Optional<ButtonType> result = addNew.showAndWait();
        if (result.get() == ButtonType.OK){
            if (!nameTextField.getText().isEmpty() && !placePos.containsKey(createPosition((int)event.getSceneX(), (int)event.getSceneY()))) {
                createDescribedPlace(createPosition((int) event.getSceneX(), (int) event.getSceneY() - 75), nameTextField.getText(), categoryCheck(), descTextField.getText());
            }else{
                positionAlert();
            }
        }
    }

    private String categoryCheck(){
        String category;
        if (!categoriesList.getSelectionModel().isEmpty()){
            category = categoriesList.getSelectionModel().getSelectedItem();
        }else{
            category = "None";
        }
        return category;
    }

    private void createNamedPlace(Position position, String name, String category){
        Place place = new NamedPlace(name, position, category);
        placePos.put(position, place);
        place.createTriangle(mapBox);
        setNameList(place);
        setCategoryListMap(place);
    }

    private void createDescribedPlace(Position position, String name, String category, String description){
        Place place = new DescribedPlace(name, position, category, description);
        placePos.put(position, place);
        place.createTriangle(mapBox);
        setNameList(place);
        setCategoryListMap(place);
    }

    public static void setMarked(Category category, Place place) {
        placeMark.add(place);
    }


    private void unMarkAll() {
        for (Place place : placeMark) {
            place.setUnMarked();
        }
        placeMark.clear();
    }

    private void setNameList(Place place){
        String name = place.getName();
        List<Place> places = nameList.get(name);
        if (nameList.containsKey(name)){
            places.add(place);
            nameList.put(name, places);
        }else{
            places = new ArrayList<>();
            places.add(place);
            nameList.put(name, places);
        }
    }

    private Position createPosition(int coordinateX, int coordinateY){
        return new Position(coordinateX,coordinateY);
    }

    private void setUnSavedChanges(){
        if (placePos.isEmpty()){
            unsavedChanges = false;
        }
        else{
            unsavedChanges = true;
        }
    }

    private void setSavedChanges(){
        unsavedChanges = false;
    }

    private void closeWindowEvent(WindowEvent t) {
        if (unsavedChanges){
            if(unsavedChangesAlert().get().equals(ButtonType.OK)){
                Platform.exit();
            }else {
                t.consume();
            }
        }
    }


    private Optional<ButtonType> unsavedChangesAlert(){
        Alert error = new Alert(Alert.AlertType.CONFIRMATION);
        error.setTitle("Warning: Unsaved changes");
        error.setHeaderText("");
        error.setContentText("There are unsaved changes to the document, are you sure you want to quit?");
        Optional<ButtonType> result = error.showAndWait();
        return result;
    }

    private void positionAlert(){
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Position not accepted");
        error.setHeaderText("");
        error.setContentText("There is already a place at the entered coordinates");
        error.showAndWait();
    }

    private void noPositionAlert(){
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("No such place");
        error.setHeaderText("");
        error.setContentText("There is no place at the entered coordinates");
        error.showAndWait();
    }

    private void setCategoryListMap(Place place){
        Category category = Category.valueOf(place.getCategory());
        List<Place> places = categoryListMap.get(category);
        if (categoryListMap.containsKey(category)){
            places.add(place);
            categoryListMap.put(category, places);
        }else{
            places = new ArrayList<>();
            places.add(place);
            categoryListMap.put(category, places);
        }
        categoryListMap.entrySet().forEach(entry->{
        });
    }

    private boolean newImgCheck(boolean previousDoc){
        if(previousDoc){
            if (unsavedChanges){
                if (unsavedChangesAlert().get().equals(ButtonType.OK)) {
                    emptyAll();
                    return true;
                }else{
                    return false;
                }
            }else{
                emptyAll();
                return true;
            }
        }
        return true;
    }

    private boolean newPlacesCheck(boolean fileLoaded){
        if(fileLoaded){
            if (unsavedChanges){
                if (unsavedChangesAlert().get().equals(ButtonType.OK)) {
                    emptyPlaces();
                    return true;
                }else{
                    return false;
                }
            }else{
                emptyPlaces();
                return true;
            }
        }
        return true;
    }

    private void emptyAll(){
        mapBox.getChildren().clear();
        emptyLists();
    }

    private void emptyPlaces(){
        Iterator it = placePos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Place place = (Place) pair.getValue();
            mapBox.getChildren().remove(place.getTriangle());
        }
        emptyLists();
    }

    private void emptyLists(){
        placeMark.clear();
        placePos.clear();
        nameList.clear();
        categoryListMap.clear();
        unsavedChanges = false;
    }
}
