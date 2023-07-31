package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PaneHandler {
    public BorderPane borderPane = new BorderPane();
    public int studyStyle = 1;
    public ImageView previewer = null;
    public FlowPane tagsBox;

    public FlowPane newTagsBox() {
        this.tagsBox = new FlowPane();
        tagsBox.setAlignment(Pos.TOP_CENTER);
        tagsBox.getStyleClass().add("tags");
        return this.tagsBox;
    }

    public void setTagsBox(FlowPane tagsBox) {
        this.tagsBox = tagsBox;
    }

    public void setPreviewer(ImageView preview) {
        this.previewer = preview;
        this.previewer.setVisible(true);
    }

    public ImageView getPreviewer() {
        return this.previewer;
    }

    public void updateTopPane(CardSettings cs, TableAndStats tas) {
        HBox topBox = new HBox();
        int iconSize = 24;
        Insets insetTop = new Insets(2);
        GaussianBlur blur = new GaussianBlur();
        blur.setRadius(1.7);


        /// get all info
        Text totalCards = new Text(Integer.toString(cs.getTotalCards()));
        Text todayCards = new Text(Integer.toString(tas.getTodayCards()));
        int todayTime = tas.getTodayTime();



        /// totalcards
        InputStream cardsIcon = null;
        InputStream timeIcon = null;
        InputStream studyIcon = null;

        try {
            cardsIcon = Files.newInputStream(Paths.get("resources/imgs/Cards.png"));
            timeIcon = Files.newInputStream(Paths.get("resources/imgs/time.png"));
            studyIcon = Files.newInputStream(Paths.get("resources/imgs/studied.png"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        ///total cards
        HBox totalTextHolder = new HBox();
        Image image = new Image(cardsIcon);
        HBox cardCountHolder = new HBox();
        ImageView imageView = new ImageView(image);

        imageView.setFitHeight(iconSize);
        imageView.setFitWidth(iconSize);
        imageView.setPreserveRatio(true);
        imageView.setEffect(blur);

        totalTextHolder.getChildren().add(totalCards);
        cardCountHolder.getChildren().addAll(imageView, totalTextHolder);

        cardCountHolder.setAlignment(Pos.CENTER_RIGHT);
        cardCountHolder.getStyleClass().add("infoContainer");
        totalTextHolder.getStyleClass().add("topInfo");
        totalCards.getStyleClass().add("topInfo");


        /// todayCards
        HBox cardsBox = new HBox();
        HBox cardsTextHolder = new HBox();
        Image image2 = new Image(studyIcon);
        ImageView imageView2 = new ImageView(image2);
        imageView2.setEffect(blur);


        imageView2.setFitHeight(iconSize);
        imageView2.setFitWidth(iconSize);

        cardsTextHolder.getChildren().add(todayCards);
        cardsBox.getChildren().addAll(imageView2, cardsTextHolder);

        cardsBox.setAlignment(Pos.CENTER);
        todayCards.getStyleClass().add("topInfo");
        cardsTextHolder.getStyleClass().add("topInfo");
        cardsBox.getStyleClass().add("infoContainer");

        /// todayTime
        HBox timeBox = new HBox();
        HBox timeTextHolder = new HBox();
        Image image3 = new Image(timeIcon);
        ImageView imageView3 = new ImageView(image3);
        imageView3.setEffect(blur);


        imageView3.setFitHeight(iconSize);
        imageView3.setFitWidth(iconSize);

        Text time;
        int hours = todayTime / 3600;
        int minutes = (todayTime % 3600) / 60;
        int seconds = (todayTime % 3600) % 60;
        if (hours > 0) {
            time = new Text(hours + ":" + returnTwoDigit(minutes) + ":" + returnTwoDigit(seconds));
        } else if(minutes>0){
            time = new Text(minutes + ":" + returnTwoDigit(seconds));
        } else {
            time = new Text(0+ ":" + returnTwoDigit(seconds));
        }

        VBox spacer1 = new VBox();
        VBox spacer2 = new VBox();
        VBox spacer3 = new VBox();
        spacer1.setPrefWidth(15);
        spacer2.setPrefWidth(15);
        spacer3.setPrefWidth(15);
        timeTextHolder.getChildren().add(time);
        timeBox.getChildren().addAll(spacer1,imageView, totalTextHolder, spacer2,imageView2,cardsTextHolder,spacer3, imageView3, timeTextHolder);

        timeBox.setAlignment(Pos.CENTER);
        timeBox.getStyleClass().add("infoContainer");
        time.getStyleClass().add("topInfo");
        timeTextHolder.getStyleClass().add("topInfo");

        topBox.setPrefHeight(64);
        topBox.getStyleClass().add("infoBar");
        topBox.setSpacing(20);
        topBox.setPadding(insetTop);
        topBox.getStyleClass().add("topPane");

        VBox spacer0 = new VBox();


        topBox.getChildren().addAll(spacer0, timeBox);
        topBox.setHgrow(spacer0, Priority.ALWAYS);

        borderPane.setTop(topBox);
    }

    public String returnTwoDigit(int i) {
        String doubleDig;
        if (i < 10) {
            doubleDig = ("0" + i);
        } else {
            doubleDig = String.valueOf(i);
        }
        return doubleDig;
    }

}
