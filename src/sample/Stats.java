package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Stats {

    BorderPane statsPane = new BorderPane();

    public Stats() throws IOException {

        statsPane.getStyleClass().add("tabsVBox");


    }

    public static class AutoCompleteComboBox<T> implements EventHandler<KeyEvent> {


        private ComboBox comboBox;
        private List list;
        private final ObservableList<T> data;
        private boolean moveCaretToPos = false;
        private int caretPos;

        public AutoCompleteComboBox(final ComboBox comboBox, List list) {
            this.comboBox = comboBox;
            this.list = list;
            data = comboBox.getItems();
            comboBox.getStyleClass().add("comboBox");
            this.comboBox.setEditable(true);


            this.comboBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    comboBox.hide();
                }
            });
            this.comboBox.setOnKeyReleased(AutoCompleteComboBox.this);
        }

        @Override
        public void handle(KeyEvent event) {

            if (event.getCode() == KeyCode.UP) {
                caretPos = -1;
                moveCaret(comboBox.getEditor().getText().length());
                return;
            } else if (event.getCode() == KeyCode.DOWN) {
                if (!comboBox.isShowing()) {
                    comboBox.show();
                }
                caretPos = -1;
                moveCaret(comboBox.getEditor().getText().length());
                return;
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                moveCaretToPos = true;
                caretPos = comboBox.getEditor().getCaretPosition();
            } else if (event.getCode() == KeyCode.DELETE) {
                moveCaretToPos = true;
                caretPos = comboBox.getEditor().getCaretPosition();
            } else if (event.getCode() == KeyCode.ENTER) {
                moveCaretToPos = true;
                caretPos = comboBox.getEditor().getCaretPosition();
            }

            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                    || event.isControlDown() || event.getCode() == KeyCode.HOME
                    || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
                return;
            }

            ObservableList list = FXCollections.observableArrayList();
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).toString().toLowerCase().startsWith(
                        AutoCompleteComboBox.this.comboBox
                                .getEditor().getText().toLowerCase())) {
                    list.add(data.get(i));
                }
            }
            String t = comboBox.getEditor().getText();

            comboBox.setItems(list);
            comboBox.getEditor().setText(t);
            if (!moveCaretToPos) {
                caretPos = -1;
            }
            moveCaret(t.length());
            if (!list.isEmpty()) {
                comboBox.show();
            }
        }

        private void moveCaret(int textLength) {
            if (caretPos == -1) {
                comboBox.getEditor().positionCaret(textLength);
            } else {
                comboBox.getEditor().positionCaret(caretPos);
            }
            moveCaretToPos = false;
        }

    }

    public void set0AllStats() {
        this.statsPane.getChildren().removeAll();

        ///top panel: Chart Type
        HBox chartOptions = new HBox();
        Button studyHistory = new Button("Progress");
        Button boxChart = new Button("Rating Distribution");

        ///left panel:

        SwingNode numberRatingChart = Main.tableAndStats.numberRatingChart();


        statsPane.setCenter(createChartAnchor(numberRatingChart));
    }

    public void set1DeckStats() {
        this.statsPane.getChildren().removeAll();
        this.statsPane.setCenter(null);
        VBox deckButtonList = new VBox();

        deckButtonList.getStyleClass().add("statsLeftBox");

        List list = Main.deckTagHandler.deckTable.column("DeckName").asList();
        Collections.sort(list);

        for (int i = 0; i < list.size(); i++) {
            String deckName = (String) list.get(i);

            Button deckNameButton = new Button(deckName);

            deckNameButton.getStyleClass().add("statMainSelectButtons");
            deckNameButton.setMaxWidth(Double.MAX_VALUE);

            deckNameButton.setOnAction(e -> {

                ///selected css, deselect others
                for (Node button : deckButtonList.getChildren()) {
                    button.getStyleClass().remove("statMainSelectButtonsSelected");
                }
                deckNameButton.getStyleClass().add("statMainSelectButtonsSelected");


                VBox chartBox = new VBox();

                SwingNode histogram = Main.tableAndStats.getDeckRatingHistogram(deckName);
                SwingNode ratingChart = Main.deckTagHandler.getDeckProgress(deckName, false);

                GridPane gridPane = new GridPane();
                gridPane.setPadding(new Insets(10));

                ColumnConstraints col1 = new ColumnConstraints();
                ColumnConstraints col2 = new ColumnConstraints();
                col1.setPercentWidth(50);
                col2.setPercentWidth(50);

                RowConstraints row1 = new RowConstraints();
                RowConstraints row2 = new RowConstraints();
                row1.setPercentHeight(50);
                row2.setPercentHeight(50);

                gridPane.getColumnConstraints().addAll(col1, col2);
                gridPane.getRowConstraints().addAll(row1, row2);

                gridPane.add(createChartAnchor(ratingChart), 0, 0, 2, 1);
                gridPane.add(createChartAnchor(histogram), 0, 1, 1, 1);
                ///gridPane.add(timeBox, 1,1,1,1);

                chartBox.getChildren().addAll(gridPane);

                statsPane.setCenter(chartBox);

                /// STATS
                statsPane.setRight(createStatBox(deckName, false));

            });

            deckButtonList.getChildren().add(deckNameButton);
        }

        statsPane.setLeft(deckButtonList);
    }

    public void set2TagStats() {

        int buttonHeight = 30;

        this.statsPane.getChildren().removeAll();

        VBox tagButtonList = new VBox();

        VBox subTagButtonList = new VBox();
        subTagButtonList.setVisible(false);
        subTagButtonList.setManaged(false);

        tagButtonList.getStyleClass().add("statsLeftBox");
        subTagButtonList.getStyleClass().add("statsLeftBox2");

        String[] list = Main.cardSettings.getMainTags();

        ///iterate over main tag names and create a button for each
        for (int i = 0; i < list.length; i++) {

            String tagName = list[i];

            Button tagNameButton = new Button(tagName);
            tagNameButton.setPrefHeight(buttonHeight);

            ///if it has subtags, open additional column to show subtags
            if (Main.cardSettings.checkIfSubTags(tagName)) {

                tagNameButton.setOnAction(e -> { ///make each subtag button create chart

                    for (Node button : tagButtonList.getChildren()) {
                        button.getStyleClass().remove("statMainSelectButtonsSelected");
                    }
                    tagNameButton.getStyleClass().add("statMainSelectButtonsSelected");

                    subTagButtonList.getChildren().clear(); ///clears previous subtags

                    for (String subTagName : Main.cardSettings.getSubTags(tagName)) {

                        String subTagNameOnly = StringUtils.substringBetween(subTagName, "(", ")");
                        Button subTagNameButton = new Button(subTagNameOnly);

                        subTagNameButton.getStyleClass().add("statMainSelectButtons");
                        subTagNameButton.setMaxWidth(Double.MAX_VALUE);
                        subTagNameButton.setPrefHeight(buttonHeight);

                        subTagNameButton.setOnAction(f -> {
                            /// TABLE AND CHARTS

                            for (Node button : subTagButtonList.getChildren()) {
                                button.getStyleClass().remove("statMainSelectButtonsSelected");
                            }
                            subTagNameButton.getStyleClass().add("statMainSelectButtonsSelected");

                            VBox chartBox = new VBox();

                            SwingNode histogram = Main.tableAndStats.getTagRatingHistogram(subTagName);
                            SwingNode ratingChart = Main.deckTagHandler.getTagProgress(subTagName, false);

                            GridPane gridPane = new GridPane();
                            gridPane.setPadding(new Insets(10));

                            ColumnConstraints col1 = new ColumnConstraints();
                            ColumnConstraints col2 = new ColumnConstraints();
                            col1.setPercentWidth(50);
                            col2.setPercentWidth(50);

                            RowConstraints row1 = new RowConstraints();
                            RowConstraints row2 = new RowConstraints();
                            row1.setPercentHeight(50);
                            row2.setPercentHeight(50);

                            gridPane.getColumnConstraints().addAll(col1, col2);
                            gridPane.getRowConstraints().addAll(row1, row2);

                            gridPane.add(createChartAnchor(ratingChart), 0, 0, 2, 1);
                            gridPane.add(createChartAnchor(histogram), 0, 1, 1, 1);

                            chartBox.getChildren().addAll(gridPane);

                            statsPane.setCenter(chartBox);

                            /// STATS
                            statsPane.setRight(createStatBox(subTagName, true));


                        });

                        subTagButtonList.getChildren().add(subTagNameButton);

                    }

                    subTagButtonList.setVisible(true); ///clicking on something with sub tags shows subtag panel
                    subTagButtonList.setManaged(true);

                });

            } else {
                tagNameButton.setOnAction(e -> {

                    for (Node button : tagButtonList.getChildren()) {
                        button.getStyleClass().remove("statMainSelectButtonsSelected");
                    }
                    tagNameButton.getStyleClass().add("statMainSelectButtonsSelected");

                    subTagButtonList.setVisible(false); ///clicking on something without subtags clears that panel
                    subTagButtonList.setManaged(false);

                    VBox chartBox = new VBox();

                    SwingNode histogram = Main.tableAndStats.getTagRatingHistogram(tagName);
                    SwingNode ratingChart = Main.deckTagHandler.getTagProgress(tagName, false);
                    ///SwingNode timeChart = Main.deckTagHandler.getTagProgress(tagName, true);

                    GridPane gridPane = new GridPane();
                    gridPane.setPadding(new Insets(10));

                    ColumnConstraints col1 = new ColumnConstraints();
                    ColumnConstraints col2 = new ColumnConstraints();
                    col1.setPercentWidth(50);
                    col2.setPercentWidth(50);

                    RowConstraints row1 = new RowConstraints();
                    RowConstraints row2 = new RowConstraints();
                    row1.setPercentHeight(50);
                    row2.setPercentHeight(50);

                    gridPane.getColumnConstraints().addAll(col1, col2);
                    gridPane.getRowConstraints().addAll(row1, row2);

                    gridPane.add(createChartAnchor(ratingChart), 0, 0, 2, 1);
                    gridPane.add(createChartAnchor(histogram), 0, 1, 1, 1);
                    ///gridPane.add(timeBox, 1,1,1,1);

                    chartBox.getChildren().addAll(gridPane);

                    statsPane.setCenter(chartBox);

                    /// STATS
                    statsPane.setRight(createStatBox(tagName, true));
                });
            }

            tagNameButton.setMaxWidth(Double.MAX_VALUE);
            tagNameButton.getStyleClass().add("statMainSelectButtons");
            tagButtonList.getChildren().add(tagNameButton);
        }

        HBox fullLeftPanel = new HBox();
        fullLeftPanel.getChildren().addAll(tagButtonList, subTagButtonList);
        fullLeftPanel.getStyleClass().add("statsLeftContainer");

        statsPane.setLeft(fullLeftPanel);
    }

    public AnchorPane createChartAnchor(SwingNode chart) {

        AnchorPane anchorPane = new AnchorPane();

        AnchorPane.setBottomAnchor(chart, 10.0);
        AnchorPane.setTopAnchor(chart, 10.0);
        AnchorPane.setLeftAnchor(chart, 10.0);
        AnchorPane.setRightAnchor(chart, 10.0);

        anchorPane.getStyleClass().add("statsChartBox");

        anchorPane.getChildren().add(chart);

        return anchorPane;
    }

    public VBox createStatBox(String tagDeckName, Boolean isTag){

        System.out.println(tagDeckName);

        VBox statBox = new VBox();
        Text rating;
        int timeInSeconds;
        int studyAmount;

        if(isTag) {
            rating = new Text(Main.deckTagHandler.getTagRating(tagDeckName));
            timeInSeconds = Main.deckTagHandler.getTagTimeTotal(tagDeckName);
            studyAmount = Main.deckTagHandler.getTagStudyTotal(tagDeckName);
        } else {
            rating = new Text(Main.deckTagHandler.getDeckRating(tagDeckName));
            timeInSeconds = Main.deckTagHandler.getDeckTimeTotal(tagDeckName);
            studyAmount = Main.deckTagHandler.getDeckStudyTotal(tagDeckName);
        }

        String timeString;

        if (timeInSeconds >= 86400) {
            int days = timeInSeconds / 86400;
            int remainder = timeInSeconds % 86400;
            String dayPlurality = " day ";
            String hourPlurality = " hour ";

            if (days > 1) {
                dayPlurality = " days ";
            }

            if (remainder >= 3600) { /// if greater than 1 day, show days and hours studied
                int hours = remainder / 3600;

                if (hours > 1) {
                    hourPlurality = " hours ";
                }

                timeString = days + dayPlurality + hours + hourPlurality + "studied";
            } else {
                timeString = days + dayPlurality + "studied";
            }
        } else if (timeInSeconds >= 3600) { /// if greater than 1 hour, show hours and minutes studied
            int hours = timeInSeconds / 3600;
            int remainder = timeInSeconds % 3600;
            String hourPlurality = " hour ";
            String minutePlurality = " minute ";

            if (hours > 1) {
                hourPlurality = " hours ";
            }

            if (remainder >= 60) { /// if greater than 1 day, show days and hours studied
                int minutes = remainder / 60;
                if (minutes > 1) {
                    minutePlurality = " minutes ";
                }
                timeString = hours + hourPlurality + hours + minutePlurality + "studied";
            } else {
                timeString = hours + hourPlurality + "studied";
            }
        } else {
            int minutes = timeInSeconds / 60;
            if(minutes == 1){
                timeString = "1 minute studied";
            }
            else {
                timeString = minutes + " minutes studied";
            }
        }

        Text totalTimeStudied = new Text(timeString);
        Text totalAmountStudied = new Text(studyAmount + " cards studied");

        rating.getStyleClass().add("blueTextLarge");
        totalTimeStudied.getStyleClass().add("blueTextMedium");
        totalAmountStudied.getStyleClass().add("blueTextMedium");

        statBox.getChildren().addAll(rating, totalTimeStudied, totalAmountStudied);

        return  statBox;
    }


}
