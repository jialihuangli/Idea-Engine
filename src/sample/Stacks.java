package sample;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.apache.commons.lang3.ArrayUtils;
import org.controlsfx.control.SegmentedButton;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Stacks {


    ///initialized variables
    public String tagName;
    public VBox cardFront, cardDetails, cardBack, finalCard;
    public BorderPane cardPane = new BorderPane();

    public Boolean enlarged, finalCardBool = false;
    public boolean isStarred = false;
    public int studyStyle = Main.paneHandler.studyStyle;
    public boolean front = true;
    public boolean initiation = true;
    public boolean boolDetails;

    public focusTimer focusTimer = Main.focusTimer;

    /// card stats
    float startStackTraining, endStackTraining, startAverageAge, endAverageAge, startDeckTagTraining, endDeckTagTraining;

    private static final DecimalFormat decimal2 = new DecimalFormat("0.00");
    private static final DecimalFormat decimal1 = new DecimalFormat("0.0");

    public TableAndStats tableAndStats = Main.tableAndStats;
    /// all tables
    Table setAsideCards;
    int stackTotal;
    int deckType; /// 0=deck, 1=tag
    DeckTagHandler deckTagHandler = Main.deckTagHandler;
    String deckName;

    /// PROGRESS BAR
    GridPane progressBox = new GridPane();
    HBox bottomBox = new HBox();
    ColumnConstraints column1 = new ColumnConstraints();

    String cssPath = "file:resources/styleSheets/card.css";

    public Stacks() {

    }

    public void resetVariables() {
        startStackTraining = (float) (tableAndStats.studyingCards.intColumn("trainingNumber").mean());
        startDeckTagTraining = (float) (tableAndStats.studyingStack.intColumn("trainingNumber").mean());
        startAverageAge = (float) ((new Date().getTime() / 1000) - tableAndStats.studyingStack.intColumn("StudyDate").mean());
        focusTimer.resetTime();
        tableAndStats.resetNumber();
    }

    /// Create stack using tag
    public void taggedStack(String tagSelected) {
        deckType = 1;
        tagName = tagSelected;
        String stackNameSp = "<" + this.tagName + ">";

        tableAndStats.addSortColumn();

        ///separate all cards to ones you study and not studying
        tableAndStats.studyingStack = tableAndStats.allCards.where(tableAndStats.allCards.stringColumn("Tags").containsString(stackNameSp));
        tableAndStats.remainingStack = tableAndStats.allCards.dropWhere(tableAndStats.allCards.stringColumn("Tags").containsString(stackNameSp));

        ///shuffle and order studying deck
        tableAndStats.shuffle();

        ///Update recently opened stacks
        Main.cardSettings.updateRecentStacks(tagSelected);

        stackTotal = tableAndStats.studyingCards.rowCount();

        createCard();

        if (initiation) { /// only generate the first time.
            genStack();

            this.initiation = false;
        }
        column1.setPercentWidth(((tableAndStats.cardNumber + 1) * 100 / stackTotal));

    }

    /// Create stack with a certain number
    public void numberedStack(int stackCount) {
        tableAndStats.studyingCards = tableAndStats.allCards.first(stackCount);
        tableAndStats.addSortColumn();
        stackTotal = tableAndStats.studyingCards.rowCount();
        createCard();
        genStack();
    }

    /// Create stack based on old cards and randomly study a percentage of total
    public void smartStackAll() {
        int todayInt = (int) (new Date().getTime() / 1000);
        int days = 30;
        double percentage = .1;
        int epochTime = days * 60 * 60 * 24;

        Table mustStudy = tableAndStats.allCards.where(tableAndStats.allCards.intColumn("StudyDate").isLessThan(todayInt - epochTime));

        Table filteredStudy = tableAndStats.allCards.where(tableAndStats.allCards.intColumn("StudyDate").isGreaterThanOrEqualTo(todayInt - epochTime));
        int stackCount = (int) Math.max((filteredStudy.rowCount() * percentage), 10);
        filteredStudy = filteredStudy.first(stackCount);

        Table conjoinedTables = mustStudy.append(filteredStudy);
        tableAndStats.studyingCards = conjoinedTables;

        tableAndStats.shuffle();
        createCard();
        genStack();
    }

    public void smartStackTagged(String stackName) {
        int todayInt = (int) (new Date().getTime() / 1000);
        int days = 30;
        double percentage = .1;
        int epochTime = days * 60 * 60 * 24;

        Table filteredCards = tableAndStats.allCards.where(tableAndStats.allCards.stringColumn("Tags").containsString(stackName));

        Table mustStudy = filteredCards.where(filteredCards.intColumn("StudyDate").isLessThan(todayInt - epochTime));

        Table filteredStudy = filteredCards.where(filteredCards.intColumn("StudyDate").isGreaterThanOrEqualTo(todayInt - epochTime));
        int stackCount = (int) Math.max((filteredStudy.rowCount() * percentage), 10);
        filteredStudy = filteredStudy.first(stackCount);

        Table conjoinedTables = mustStudy.append(filteredStudy);
        tableAndStats.studyingCards = conjoinedTables;

        tableAndStats.shuffle();
        createCard();
        genStack();
    }


    public void genStack() {

        resetVariables();

        if (Main.cardSettings.styleNum == 1) {
            cardPane.setCenter(cardFront);
        } else if (Main.cardSettings.styleNum == 2) {
            cardPane.setCenter(cardBack);
        }
        cardPane.getStylesheets().add(cssPath);

        VBox rightBox = new VBox();
        VBox leftBox = new VBox();

        /// progress bar initiation

        VBox bar = new VBox();
        bar.setPrefHeight(5);
        bar.getStyleClass().add("progressBar");
        bar.setFillWidth(true);
        progressBox.setPrefWidth(10000);
        column1.setPercentWidth(((tableAndStats.cardNumber + 1) * 100 / stackTotal));
        column1.getHgrow();
        progressBox.getColumnConstraints().addAll(this.column1);
        progressBox.setRowIndex(bar, 0);
        progressBox.setColumnIndex(bar, 0);
        progressBox.getChildren().addAll(bar);


        ///Buttons
        Button saveAndExit = new Button("Save & Exit");
        Button editButton = new Button();
        editButton.setPrefSize(32, 32);
        editButton.setMinSize(32, 32);
        editButton.setMaxSize(32, 32);
        editButton.setOnAction(e -> {
            switch (tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "cardType")) {
                case "general":
                    Main.paneHandler.borderPane.setCenter(editGeneralEntry());
                    break;
                case "date":
                    break;
                case "language":
                    Main.paneHandler.borderPane.setCenter(editLanguageEntry());
                    break;
                case "other":
                    break;
            }
        });
        editButton.getStyleClass().add("editButton");

        ///PROGRESS  BAR
        bottomBox.getChildren().addAll(progressBox);
        bottomBox.setAlignment(Pos.BOTTOM_LEFT);

        ///INFO BAR
        rightBox.getChildren().addAll(saveAndExit);

        ///EDIT BAR
        leftBox.getChildren().addAll(editButton);
        leftBox.setAlignment(Pos.TOP_LEFT);
        leftBox.setPadding(new Insets(10, 10, 10, 10));

        bottomBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bottomBox.setAlignment(Pos.CENTER);

        rightBox.setAlignment(Pos.CENTER);

        cardPane.setBottom(bottomBox);
        cardPane.setRight(rightBox);
        cardPane.setLeft(leftBox);

        cardPane.setOnKeyPressed(event -> {

            /// Standard controls
            if (event.getCode().equals(KeyCode.DOWN)) {
                if (finalCardBool) {
                } else if (front) {
                    flipCard();
                } else if (boolDetails) {
                    flipCard();
                } else {
                    flipCard();
                }
                event.consume();
            } else if (event.getCode().equals(KeyCode.A)) { /// decrease know, minimum at 0, set to 5 if high value
                tableAndStats.studyingCards.intColumn("trainingNumber").set(tableAndStats.cardNumber, (
                        Math.max(0, Math.min(5, Integer.parseInt(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "trainingNumber")) - 2)))
                );
                /// set aside to study again
                swapCard(true);
                event.consume();
            } else if (event.getCode().equals(KeyCode.D)) { /// increase know, max at 10
                tableAndStats.studyingCards.intColumn("trainingNumber").set(tableAndStats.cardNumber, (
                        Math.min(10, Integer.parseInt(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "trainingNumber")) + 1))
                );
                swapCard(true);
                event.consume();
            } else if (event.getCode().equals(KeyCode.UP)) {
                event.consume();
            } else if (event.getCode().equals(KeyCode.LEFT)) {
                swapCard(false);
                event.consume();
            } else if (event.getCode().equals(KeyCode.RIGHT)) {
                swapCard(true);
                event.consume();
            }
            /// Special controls
            else if (event.getCode().equals(KeyCode.S)) { /// star card
                changeCardStar();
                event.consume();
            }

        });


        saveAndExit.setOnAction(e -> {
                    endCard();
                }
        );

    }


    public BorderPane getPane() {
        return cardPane;
    }

    public void flipCard() {
        if (front) {
            rotateCard(cardFront, cardBack);
            front = false;
        } else {
            rotateCard(cardBack, cardFront);
            front = true;
        }
    }

    public void swapCard(boolean next) {
        ///next card
        if (next) {
            if (finalCardBool) {
                jamCard(finalCard);
            } else if (tableAndStats.cardNumber + 1 < stackTotal) { ///if next card is not last card
                tableAndStats.nextNumber();
                if (front) {
                    transitionNextCard(cardFront);
                } else {
                    transitionNextCard(cardBack);
                }
            } else { ///if last card, shuffle, then display
                if (front) {
                    transitionFinalCard(cardFront);
                } else {
                    transitionFinalCard(cardBack);
                }
                this.finalCardBool = true;
            }
        }
        if (!next) {
            if (finalCardBool == true) {
                jamCard(finalCard);
            } else if (tableAndStats.cardNumber == 0) {
                if (front) {
                    jamCard(cardFront);
                } else {
                    jamCard(cardBack);
                }
            } else {
                tableAndStats.previousNumber();
                if (front) {
                    transitionPreviousCard(cardFront);
                } else {
                    transitionPreviousCard(cardBack);
                }
                front = true;
            }
        }
    }

    public void createCard() {

        int cardWidth = 600;
        int cardHeight = 800;

        String cardType = tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "cardType");
        isStarred = Main.cardSettings.starredList.contains(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "#"));

        HBox topBox = new HBox();

        HBox spacer = new HBox();
        Button starButton = new Button();

        spacer.setPrefWidth(20);

        topBox.setPadding(new Insets(5, 5, 5, 5));
        topBox.getChildren().addAll(starButton);

        /// GENERIC CARD
        if (cardType.equals("general")) {


            ///Create Card Front
            Text title = new Text(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info1"));
            title.getStyleClass().add("frontText");
            this.cardFront = new VBox(title);
            cardFront.setMaxSize(cardWidth, cardHeight);
            cardFront.getStyleClass().add("card");
            title.setWrappingWidth(cardWidth * .95);
            title.setTextAlignment(TextAlignment.CENTER);

            ///Create Card Back
            this.boolDetails = tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info3text").isEmpty();

            Text details = new Text(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info2"));
            VBox detailsBox = new VBox(details);
            details.getStyleClass().add("detailText");
            details.setTextAlignment(TextAlignment.CENTER);
            this.cardBack = new VBox(topBox);
            cardBack.setMaxSize(cardWidth, cardHeight);
            cardBack.getStyleClass().add("card");
            details.setWrappingWidth(cardWidth * .95);
            detailsBox.setAlignment(Pos.CENTER);
            detailsBox.getStyleClass().add("detailText");


            ///If there are details: add only big major
            if (boolDetails) {
                detailsBox.setPrefHeight(5000); /// fills height
                cardBack.getChildren().addAll(detailsBox);
            } else {
                String content = tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info3");
                detailsBox.setPrefHeight(5000); /// fills height
                WebView webView = new WebView();
                webView.getEngine().loadContent(content);
                webView.setMinSize(cardWidth * .925, cardHeight * .42);
                webView.setMaxSize(cardWidth * .925, cardHeight * .42);
                Scale scale = new Scale(1, 1);
                scale.setPivotX(cardWidth * .4625);
                scale.setPivotY(cardHeight * .21);
                webView.getTransforms().setAll(scale);
                this.cardDetails = new VBox();
                cardDetails.setAlignment(Pos.CENTER);
                cardDetails.setMinSize(cardWidth * .975, cardHeight * .46);
                cardDetails.setMaxSize(cardWidth * .975, cardHeight * .46);
                cardDetails.getStyleClass().add("cardDetails");
                cardDetails.getChildren().add(webView);
                cardBack.getChildren().addAll(detailsBox, cardDetails);

            }

            ///Info Box and Source Box

/*            int importance = Integer.parseInt(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "Importance"));
            Text symbol = new Text("⬤");
            switch (importance) {
                case 1:
                    symbol = new Text("⬤");
                    symbol.getStyleClass().add("symbol1");
                    break;
                case 2:
                    symbol = new Text("▲");
                    symbol.getStyleClass().add("symbol2");
                    break;
                case 3:
                    symbol = new Text("✨");
                    symbol.getStyleClass().add("symbol3");
                    break;
            }*/

            cardBack.getChildren().add(getInfoBox(cardWidth,cardHeight));

            VBox sourceBox = new VBox();
            sourceBox.setMinSize(385, 30);
            sourceBox.setMaxSize(385, 30);
            sourceBox.setAlignment(Pos.BOTTOM_CENTER);
            createSourceLinkOrText(sourceBox);
            cardBack.getChildren().add(sourceBox);

            if (Main.cardSettings.styleNum == 1) {
                front = true;
                cardPane.setCenter(cardFront);
            } else if (Main.cardSettings.styleNum == 2) {
                front = false;
                cardPane.setCenter(cardBack);
            }
        }

        /// LANGUAGE CARD
        else if (cardType.equals("language")) {

            Text location1;
            Text location2;
            Text location3 = new Text();

            ///Create Card Front
            Text info1 = new Text(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info1"));
            Text info2 = new Text(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info2"));
            Text info3 = new Text(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info3"));

            info1.getStyleClass().add("langTermText");
            info2.getStyleClass().add("langPronText");
            info3.getStyleClass().add("langDefText");


            if (studyStyle == 1) { ///Term on front
                location1 = info1;
                if (info2.getText() == ("")) { ///no pronunciation
                    location2 = info3;
                    boolDetails = true;
                } else if (info3.getText() == ("")) { ///no definition
                    location2 = info2;
                    boolDetails = true;
                } else {   ///has all STANDARD
                    location2 = info2;
                    location3 = info3;
                    boolDetails = false;
                }
            } else {  /// Reverse flash card
                if (info3.getText() == ("")) { /// show pronunciation on front
                    location1 = info2;
                    location2 = info1;
                    boolDetails = true;
                } else if (info2.getText() == ("")) { /// show definition on front
                    location1 = info3;
                    location1.getStyleClass().add("langTermText");
                    location2 = info1;
                    boolDetails = true;
                } else { ///has everything STANDARD
                    location1 = info3;
                    location2 = info1;
                    location3 = info2;
                    boolDetails = false;
                }
            }


            this.cardFront = new VBox(location1);
            cardFront.setMaxSize(cardWidth, cardHeight);
            cardFront.getStyleClass().add("card");

            ///Create Card Back

            VBox box2 = new VBox(location2);
            location2.getStyleClass().add("detailText");
            location2.setTextAlignment(TextAlignment.CENTER);
            location2.setWrappingWidth(cardWidth * .95);
            box2.setAlignment(Pos.CENTER);
            box2.getStyleClass().add("detailText");

            this.cardBack = new VBox(topBox);
            cardBack.setMaxSize(cardWidth, cardHeight);
            cardBack.getStyleClass().add("card");

            if (boolDetails) {
                box2.setPrefHeight(3000);
                cardBack.getChildren().addAll(box2);
            } else {
                box2.setMinHeight(cardHeight * .45);
                box2.setAlignment(Pos.BOTTOM_CENTER);
                box2.getStyleClass().add("box2");

                VBox box3 = new VBox(location3);
                location3.getStyleClass().add("detailText");
                location3.setTextAlignment(TextAlignment.CENTER);
                location3.setWrappingWidth(cardWidth * .95);
                box3.setAlignment(Pos.TOP_CENTER);
                box3.getStyleClass().add("detailText");
                box3.setPrefHeight(3000);

                cardBack.getChildren().addAll(box2, box3);
            }

            cardBack.getChildren().add(getInfoBox(cardWidth, cardHeight));

            if (Main.cardSettings.styleNum == 1) {
                front = true;
                cardPane.setCenter(cardFront);
            } else if (Main.cardSettings.styleNum == 2) {
                front = false;
                cardPane.setCenter(cardBack);
            }
        }

    }

    public void endCard() {

        int cardWidth = 600;
        int cardHeight = 800;

        int column1Width = 200;
        int column234Width = 120;
        int rowHeight = 40;
        int time = focusTimer.getTimeElapsed();

        ///Update rating for each tag in deck, update date ratings,

        if (deckType == 0) {
            for (String tag : deckTagHandler.getIncludeList(deckName)) {
                Double tagRating = tableAndStats.getTagRating(tag.replace("<", "").replace(">", ""));
                deckTagHandler.specialTagHistory(tag.replace("<", "").replace(">", ""), tagRating);

                int amountForTag = tableAndStats.countNumberTagsInStudying(tag);
                int timeInSecondsStudied;
                if(amountForTag == 0){
                    timeInSecondsStudied = 0;
                } else {
                    timeInSecondsStudied = time * amountForTag / (tableAndStats.cardNumber+1);
                }
                deckTagHandler.updateTagRatingDateTimeAmount(tag.replace("<", "").replace(">", ""), amountForTag, timeInSecondsStudied);

            }
            deckTagHandler.updateDeckRatingDateTimeAmount(deckName, tableAndStats.cardNumber+1, time);
            deckTagHandler.saveTagTable();
            deckTagHandler.saveDeckTable();
        } else {

            deckTagHandler.updateTagRatingDateTimeAmount(tagName.replace("<", "").replace(">", ""), tableAndStats.cardNumber, time);
            deckTagHandler.saveTagTable();
        }

        // Finish Text
        Text secondText;
        Text firstText = new Text("Congratulations!");
        if ((tableAndStats.cardNumber + 1) > 1) { /// check for plurality
            secondText = new Text("You've finished studying " + (tableAndStats.cardNumber + 1) + " cards!");
        } else {
            secondText = new Text("You've finished studying 1 card!");
        }

        firstText.getStyleClass().add("frontText");
        secondText.getStyleClass().add("detailText");

        /// TIME SPENT
        Text tsText = new Text("Study Session Length: ");
        tsText.prefWidth(column1Width);

        tableAndStats.addTimeAndCard(time);
        int min = (time / 60);
        int seconds = time % 60;
        String minString, secondsString;
        if (min < 10) {
            minString = ("0" + min);
        } else {
            minString = String.valueOf(min);
        }
        if (seconds < 10) {
            secondsString = ("0" + seconds);
        } else {
            secondsString = String.valueOf(seconds);
        }
        Text ts2Text = new Text((minString + ":" + secondsString));

        Text ts3Text = new Text(decimal1.format(((float) time / (float) (tableAndStats.cardNumber + 1))) + " seconds/card");

        tsText.getStyleClass().add("boldedText");
        ts2Text.getStyleClass().add("standardText");
        ts3Text.getStyleClass().add("standardText");


        GridPane grouped = new GridPane();

        /// AVERAGE RATING OF STACK (USE STUDYINGCARDS)
        Text aRText = new Text("Stack Rating: ");
        endStackTraining = (float) tableAndStats.studyingCards.intColumn("trainingNumber").mean();
        Text aR1 = new Text(decimal2.format(startStackTraining));
        aR1.setTextAlignment(TextAlignment.RIGHT);
        float diffAverageTraining = endStackTraining - startStackTraining;
        Text aR2;
        if (diffAverageTraining < 0) {
            aR2 = new Text(decimal2.format(diffAverageTraining));
            aR2.getStyleClass().add("negativeNumber");
        } else if (diffAverageTraining == 0) {
            aR2 = new Text("+" + decimal2.format(diffAverageTraining));
            aR2.getStyleClass().add("standardText");
        } else {
            aR2 = new Text("+" + decimal2.format(diffAverageTraining));
            aR2.getStyleClass().add("positiveNumber");
        }
        Text aR3 = new Text(decimal2.format(endStackTraining));
        aRText.getStyleClass().add("boldedText");
        aR1.getStyleClass().add("standardText");
        aR3.getStyleClass().add("standardText");

        /// must save before selection
        tableAndStats.saveAllCardsAny(true);


        /// AVERAGE RATING OF DECK/TAG (USE STUDYING STACK)

        Table tempTable = tableAndStats.unusedCards.append(tableAndStats.studyingCards);


        endDeckTagTraining = (float) tempTable.intColumn("trainingNumber").mean();
        Text deckTagRatingStart = new Text(decimal2.format(startDeckTagTraining));
        deckTagRatingStart.setTextAlignment(TextAlignment.RIGHT);
        float deckTagRatingDiffNum = endDeckTagTraining - startDeckTagTraining;
        Text deckTagRatingDiff;
        if (deckTagRatingDiffNum < 0) {
            deckTagRatingDiff = new Text(decimal2.format(deckTagRatingDiffNum));
            deckTagRatingDiff.getStyleClass().add("negativeNumber");
        } else if (deckTagRatingDiffNum == 0) {
            deckTagRatingDiff = new Text("+" + decimal2.format(deckTagRatingDiffNum));
            deckTagRatingDiff.getStyleClass().add("standardText");
        } else {
            deckTagRatingDiff = new Text("+" + decimal2.format(deckTagRatingDiffNum));
            deckTagRatingDiff.getStyleClass().add("positiveNumber");
        }
        Text deckTagRatingEnd = new Text(decimal2.format(endDeckTagTraining));

        deckTagRatingStart.getStyleClass().add("standardText");
        deckTagRatingEnd.getStyleClass().add("standardText");

        if (deckType == 0) {
            String toSave;
            if (deckTagRatingEnd.getText() == "10.00") {
                toSave = "10";
            } else {
                toSave = deckTagRatingEnd.getText();
            }
            deckTagHandler.saveDeckTable();
        } else if (deckType == 1) {
            String toSave;
            if (deckTagRatingEnd.getText() == "10.00") {
                toSave = "10";
            } else {
                toSave = deckTagRatingEnd.getText();
            }
            deckTagHandler.saveTagTable();
        }


        /// AVERAGE AGE
        Text aAText = new Text("Average Age: ");
        endAverageAge = (float) ((new Date().getTime() / 1000) - tempTable.intColumn("StudyDate").mean());
        Text aA1 = new Text((decimal2.format(startAverageAge / 86400) + " days"));
        float diffAverageAge = (endAverageAge - startAverageAge) / 86400;
        Text aA2;
        if (diffAverageAge < 0) {
            aA2 = new Text(decimal2.format(diffAverageAge) + " days");
            aA2.getStyleClass().add("positiveNumber");
        } else if (diffAverageAge == 0) {
            aA2 = new Text("+" + decimal2.format(diffAverageAge) + " days");
            aA2.getStyleClass().add("standardText");
        } else {
            aA2 = new Text("+" + decimal2.format(diffAverageAge) + " days");
            aA2.getStyleClass().add("negativeNumber");
        }
        Text aA3 = new Text(decimal2.format(endAverageAge / 86400) + " days");

        aAText.getStyleClass().add("boldedText");
        aA1.getStyleClass().add("standardText");
        aA3.getStyleClass().add("standardText");

        Text averageRatingDeckTagText;
        if (deckType == 0) {
            averageRatingDeckTagText = new Text("Deck Rating: ");
            deckTagHandler.addToHistory(deckName, Double.parseDouble(decimal2.format(endDeckTagTraining)), true, true);
            deckTagHandler.addToHistory(deckName, Double.valueOf(decimal1.format(((float) time / (float) (tableAndStats.cardNumber + 1)))), true, false);
            deckTagHandler.saveDeckTable();

        } else {
            averageRatingDeckTagText = new Text("Tag Rating: ");
            deckTagHandler.addToHistory(tagName, Double.parseDouble(decimal2.format(endDeckTagTraining)), false, true);
            deckTagHandler.addToHistory(tagName, Double.valueOf(decimal1.format(((float) time / (float) (tableAndStats.cardNumber + 1)))), false, false);
            deckTagHandler.saveTagTable();
        }


        averageRatingDeckTagText.getStyleClass().add("boldedText");

        // format all stat buttons
        grouped.getColumnConstraints().add(new ColumnConstraints(column1Width));
        grouped.getColumnConstraints().add(new ColumnConstraints(column234Width));
        grouped.getColumnConstraints().add(new ColumnConstraints(column234Width));
        grouped.getColumnConstraints().add(new ColumnConstraints(column234Width));
        grouped.getRowConstraints().add(new RowConstraints(rowHeight));
        grouped.getRowConstraints().add(new RowConstraints(rowHeight));
        grouped.getRowConstraints().add(new RowConstraints(rowHeight));
        grouped.getRowConstraints().add(new RowConstraints(rowHeight));


        grouped.add(tsText, 0, 0);
        grouped.add(ts2Text, 1, 0);
        grouped.add(ts3Text, 2, 0);
        grouped.add(aRText, 0, 1);
        grouped.add(aR1, 1, 1);
        grouped.add(aR2, 2, 1);
        grouped.add(aR3, 3, 1);
        grouped.add(averageRatingDeckTagText, 0, 2);
        grouped.add(deckTagRatingStart, 1, 2);
        grouped.add(deckTagRatingDiff, 2, 2);
        grouped.add(deckTagRatingEnd, 3, 2);
        grouped.add(aAText, 0, 3);
        grouped.add(aA1, 1, 3);
        grouped.add(aA2, 2, 3);
        grouped.add(aA3, 3, 3);

        grouped.getStyleClass().add("endCardStats");
        grouped.setAlignment(Pos.CENTER);

        // Buttons

        Button studyAgain = new Button("Study Again");
        studyAgain.setOnAction(e -> {
            this.finalCardBool = false;
            tableAndStats.resetNumber();
            switch (deckType) {
                case 0:
                    deckSelection(this.deckName);
                    break;
                case 1:
                    taggedStack(tagName);
                    break;
            }
            tableAndStats.shuffle();
            resetVariables();
            createCard();
        });

        studyAgain.getStyleClass().add("cardButton");

        Button returnToDecks = new Button("Return");
        returnToDecks.setOnAction(e -> {
            try {
                Main.paneHandler.borderPane.setCenter(Main.studyPane());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            resetVariables();
        });

        returnToDecks.getStyleClass().add("cardButton");

        // Create Card

        VBox endCard = new VBox();
        HBox buttonBox = new HBox(studyAgain, returnToDecks);
        buttonBox.setAlignment(Pos.CENTER);

        firstText.prefHeight(cardHeight * .15);
        secondText.prefHeight(cardHeight * .15);
        grouped.setPrefHeight(cardHeight * .4);
        buttonBox.setPrefHeight(cardHeight * .2);

        endCard.getChildren().addAll(firstText, secondText, grouped, buttonBox);

        endCard.setPrefSize(cardWidth, cardHeight);
        endCard.setMaxSize(cardWidth, cardHeight);
        endCard.getStyleClass().add("card");


        this.finalCard = endCard;
        cardPane.setCenter(finalCard);
    }

    ///DATA FUNCTIONS


    public Table fullRandom(Table table) {


        return table;
    }

    public Table reshuffle(Table table) {

        tableAndStats.updateStudied(table, tableAndStats.cardNumber);

        int todayInt = (int) (new Date().getTime() / 1000);
        int[] sortArray = {};
        Random rand = new Random();
        for (int i = 0; i < table.rowCount(); i++) {
            sortArray = ArrayUtils.add(
                    sortArray,
                    (todayInt
                            - Integer.parseInt(table.getString(i, "StudyDate")))
                            * Integer.parseInt(table.getString(i, "Importance"))
                            * (rand.nextInt(100) + 100)
            );
        }
        stackTotal = sortArray.length;
        IntColumn column = IntColumn.create("SortColumn", sortArray);
        table.replaceColumn("SortColumn", column);
        /// Order by
        table.sortDescendingOn("SortColumn");
        return table;

    }


    ///ANIMATIONS
    public void transitionNextCard(Node node) {

        int percentage = Math.min(100, ((tableAndStats.cardNumber + 1) * 100 / stackTotal));
        this.column1.setPercentWidth(percentage);

        Duration duration = Duration.millis(100);
        ScaleTransition st = new ScaleTransition(duration, node);
        TranslateTransition tt = new TranslateTransition(duration, node);

        st.setFromX(1);
        st.setFromY(1);
        st.setToX(.5);
        st.setToY(.5);
        tt.setFromX(0);
        tt.setToX(-400);

        ParallelTransition pt = new ParallelTransition(st, tt);

        pt.play();

        pt.setOnFinished(t -> {
            createCard();
            if (Main.cardSettings.styleNum == 1) {
                ScaleTransition st2 = new ScaleTransition(duration, cardFront);
                TranslateTransition tt2 = new TranslateTransition(duration, cardFront);
                RotateTransition rt2 = new RotateTransition(duration, cardFront);
                rt2.setAxis(Rotate.Y_AXIS);
                rt2.setFromAngle(60);
                rt2.setToAngle(0);
                st2.setFromX(.5);
                st2.setFromY(.5);
                st2.setToX(1);
                st2.setToY(1);
                tt2.setFromX(400);
                tt2.setToX(0);
                ParallelTransition pt2 = new ParallelTransition(st2, tt2, rt2);
                pt2.play();
            } else if (Main.cardSettings.styleNum == 2) {
                ScaleTransition st2 = new ScaleTransition(duration, cardBack);
                TranslateTransition tt2 = new TranslateTransition(duration, cardBack);
                RotateTransition rt2 = new RotateTransition(duration, cardBack);
                rt2.setAxis(Rotate.Y_AXIS);
                rt2.setFromAngle(60);
                rt2.setToAngle(0);
                st2.setFromX(.5);
                st2.setFromY(.5);
                st2.setToX(1);
                st2.setToY(1);
                tt2.setFromX(400);
                tt2.setToX(0);
                ParallelTransition pt2 = new ParallelTransition(st2, tt2, rt2);
                pt2.play();
            }
        });
        enlarged = false;

    }

    public void transitionFinalCard(Node node) {
        ///update progress bar

        int percentage = 100;
        this.column1.setPercentWidth(percentage);

        Duration duration = Duration.millis(100);
        ScaleTransition st = new ScaleTransition(duration, node);
        TranslateTransition tt = new TranslateTransition(duration, node);

        st.setFromX(1);
        st.setFromY(1);
        st.setToX(.5);
        st.setToY(.5);
        tt.setFromX(0);
        tt.setToX(-400);

        ParallelTransition pt = new ParallelTransition(st, tt);

        pt.play();

        pt.setOnFinished(t -> {
            endCard();
            ScaleTransition st2 = new ScaleTransition(duration, finalCard);
            TranslateTransition tt2 = new TranslateTransition(duration, finalCard);
            RotateTransition rt2 = new RotateTransition(duration, finalCard);
            rt2.setAxis(Rotate.Y_AXIS);
            rt2.setFromAngle(60);
            rt2.setToAngle(0);
            st2.setFromX(.5);
            st2.setFromY(.5);
            st2.setToX(1);
            st2.setToY(1);
            tt2.setFromX(400);
            tt2.setToX(0);
            ParallelTransition pt2 = new ParallelTransition(st2, tt2, rt2);
            pt2.play();
        });
        enlarged = false;

    }

    public void createSourceLinkOrText(VBox box) {
        /// Source link or text:
        String sourceText = tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "Source");
        VBox box2 = new VBox();
        box2.setPrefSize(390, 20);
        box2.setAlignment(Pos.CENTER);
        if (sourceText.contains("https://")) {
            Hyperlink sauce = new Hyperlink();
            sauce.setText(sourceText);
            sauce.getStyleClass().add("sourceText");
            sauce.setAlignment(Pos.CENTER);
            box2.getChildren().add(sauce);
            sauce.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        Desktop.getDesktop().browse(new URL(sourceText).toURI());
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Text sauce = new Text(sourceText);
            sauce.getStyleClass().add("sourceText");
            box2.getChildren().add(sauce);
        }
        box.getChildren().addAll(box2);
    }

    public void transitionPreviousCard(Node node) {
        ///PROGRESS BAR UPDATE
        int percentage = ((tableAndStats.cardNumber + 1) * 100 / stackTotal);
        this.column1.setPercentWidth(percentage);

        Duration duration = Duration.millis(100);
        ScaleTransition st = new ScaleTransition(duration, node);
        TranslateTransition tt = new TranslateTransition(duration, node);

        st.setFromX(1);
        st.setFromY(1);
        st.setToX(.5);
        st.setToY(.5);
        tt.setFromX(0);
        tt.setToX(400);

        ParallelTransition pt = new ParallelTransition(st, tt);

        pt.play();

        pt.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                createCard();
                if (Main.cardSettings.styleNum == 1) {
                    ScaleTransition st2 = new ScaleTransition(duration, cardFront);
                    TranslateTransition tt2 = new TranslateTransition(duration, cardFront);
                    RotateTransition rt2 = new RotateTransition(duration, cardFront);
                    rt2.setAxis(Rotate.Y_AXIS);
                    rt2.setFromAngle(60);
                    rt2.setToAngle(0);
                    st2.setFromX(.5);
                    st2.setFromY(.5);
                    st2.setToX(1);
                    st2.setToY(1);
                    tt2.setFromX(-400);
                    tt2.setToX(0);
                    ParallelTransition pt2 = new ParallelTransition(st2, tt2, rt2);
                    pt2.play();
                } else if (Main.cardSettings.styleNum == 2) {
                    ScaleTransition st2 = new ScaleTransition(duration, cardBack);
                    TranslateTransition tt2 = new TranslateTransition(duration, cardBack);
                    RotateTransition rt2 = new RotateTransition(duration, cardBack);
                    rt2.setAxis(Rotate.Y_AXIS);
                    rt2.setFromAngle(60);
                    rt2.setToAngle(0);
                    st2.setFromX(.5);
                    st2.setFromY(.5);
                    st2.setToX(1);
                    st2.setToY(1);
                    tt2.setFromX(-400);
                    tt2.setToX(0);
                    ParallelTransition pt2 = new ParallelTransition(st2, tt2, rt2);
                    pt2.play();
                }

            }
        });
        enlarged = false;

    }

    private void rotateCard(Node card, Node card2) {
        Duration duration = Duration.millis(100);
        RotateTransition rotator = new RotateTransition(duration, card);
        RotateTransition rotator2 = new RotateTransition(duration, card2);
        rotator.setAxis(Rotate.Y_AXIS);
        rotator.setFromAngle(0);
        rotator.setToAngle(-90);
        rotator.setInterpolator(Interpolator.LINEAR);

        rotator2.setAxis(Rotate.Y_AXIS);
        rotator2.setFromAngle(90);
        rotator2.setToAngle(0);
        rotator2.setInterpolator(Interpolator.LINEAR);

        ParallelTransition pt = new ParallelTransition(rotator2);

        rotator.play();
        rotator.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                pt.play();
                cardPane.setCenter(card2);
            }
        });
        enlarged = false;
    }

    private void jamCard(Node card) {
        Duration duration = Duration.millis(75);
        RotateTransition rotator = new RotateTransition(duration, card);
        RotateTransition rotator2 = new RotateTransition(duration, card);
        rotator.setAxis(Rotate.Y_AXIS);
        rotator.setFromAngle(0);
        rotator.setToAngle(30);
        rotator.setInterpolator(Interpolator.LINEAR);
        rotator2.setAxis(Rotate.Y_AXIS);
        rotator2.setFromAngle(30);
        rotator2.setToAngle(0);
        rotator2.setInterpolator(Interpolator.LINEAR);
        SequentialTransition st = new SequentialTransition(rotator, rotator2);
        st.play();
    }

    private void enlargeDetail() {
        Duration duration = Duration.millis(100);
        ScaleTransition st = new ScaleTransition(duration, cardBack);
        TranslateTransition tt = new TranslateTransition(duration, cardBack);
        if (enlarged) {
            st.setFromX(2);
            st.setFromY(2);
            st.setToX(1);
            st.setToY(1);
            tt.setFromY(-210);
            tt.setToY(0);
            ParallelTransition pt = new ParallelTransition(st, tt);
            pt.play();
            enlarged = false;
        } else {
            st.setToX(2);
            st.setToY(2);
            st.setFromX(1);
            st.setFromY(1);
            tt.setFromY(0);
            tt.setToY(-210);
            ParallelTransition pt = new ParallelTransition(st, tt);
            pt.play();
            enlarged = true;
        }
    }

    public HBox editGeneralEntry() {

        HBox sideBySide = new HBox();
        sideBySide.setAlignment(Pos.CENTER);
        int cardWidth = 600;
        int cardHeight = 800;

        Insets padding = new Insets(5, 5, 5, 5);

        VBox frontInput = new VBox();
        frontInput.setPrefSize(cardWidth, cardHeight);
        frontInput.setMinSize(cardWidth, cardHeight);
        frontInput.setMaxSize(cardWidth, cardHeight);
        frontInput.setAlignment(Pos.CENTER);
        frontInput.getStyleClass().add("editVBox");

        VBox backInput = new VBox();
        backInput.setPrefSize(cardWidth, cardHeight);
        backInput.setMinSize(cardWidth, cardHeight);
        backInput.setMaxSize(cardWidth, cardHeight);
        backInput.setAlignment(Pos.CENTER);
        backInput.getStyleClass().add("editVBox");
        backInput.setSpacing(20);

        AtomicInteger priorityNum = new AtomicInteger(1);

        ///Title
        javafx.scene.control.TextArea titleField = new javafx.scene.control.TextArea();
        titleField.getStyleClass().add("textTitle");
        titleField.setPromptText("Idea");
        titleField.setPrefHeight(150);
        titleField.setWrapText(true);

        ///Main Idea
        javafx.scene.control.TextArea majorDetail = new javafx.scene.control.TextArea();
        majorDetail.getStyleClass().add("textDetail");
        majorDetail.setPadding(padding);
        majorDetail.setPromptText("Definition");
        majorDetail.setWrapText(true);

        ///Details
        HTMLEditor minorDetail = new HTMLEditor();
        minorDetail.lookup(".bottom-toolbar").setManaged(false);
        minorDetail.lookup(".bottom-toolbar").setVisible(false);
        minorDetail.setPrefHeight(350);

        minorDetail.setHtmlText("<body style=\"background-color:#ebebeb; font-size: 14pt\"><font size = \"14pt\" face=\"Segoe UI\"></font face></body></html>");

        ///Priority
        RadioButton rb1 = new RadioButton("★");
        RadioButton rb2 = new RadioButton("★★");
        RadioButton rb3 = new RadioButton("★★★");
        rb1.setMinWidth(70);
        rb2.setMinWidth(70);
        rb3.setMinWidth(70);

        rb1.getStyleClass().remove("radio-button");
        rb2.getStyleClass().remove("radio-button");
        rb3.getStyleClass().remove("radio-button");
        rb1.getStyleClass().add("toggle-button");
        rb2.getStyleClass().add("toggle-button");
        rb3.getStyleClass().add("toggle-button");
        rb1.getStyleClass().add("priorityToggles");
        rb2.getStyleClass().add("priorityToggles");
        rb3.getStyleClass().add("priorityToggles");

        rb1.selectedProperty().set(true);

        ToggleGroup toggleGroup = new ToggleGroup();

        rb1.setToggleGroup(toggleGroup);
        rb2.setToggleGroup(toggleGroup);
        rb3.setToggleGroup(toggleGroup);

        rb1.setOnAction(e -> priorityNum.set(1));
        rb2.setOnAction(e -> priorityNum.set(2));
        rb3.setOnAction(e -> priorityNum.set(3));


        SegmentedButton seg = new SegmentedButton();
        seg.getButtons().addAll(rb1, rb2, rb3);

        //USE MODEL


        ///Source

        ComboBox comboBox = new ComboBox();
        javafx.scene.control.TextField source = new javafx.scene.control.TextField();
        source.getStyleClass().add("textDetail");
        source.setPromptText("Source");

        ///Create Layout

        frontInput.getChildren().add(titleField);
        backInput.getChildren().addAll(majorDetail, minorDetail, seg, source);

        sideBySide.getChildren().addAll(frontInput, backInput);

        sideBySide.setSpacing(5);

        javafx.scene.control.TextArea minorDetailText = new javafx.scene.control.TextArea();
        minorDetailText.setText(minorDetail.getHtmlText());

        return (sideBySide);

    }

    public HBox editLanguageEntry() {

        HBox sideBySide = new HBox();
        sideBySide.setAlignment(Pos.CENTER);
        int cardWidth = 600;
        int cardHeight = 800;

        VBox frontInput = new VBox();
        frontInput.setPrefSize(cardWidth, cardHeight);
        frontInput.setMinSize(cardWidth, cardHeight);
        frontInput.setMaxSize(cardWidth, cardHeight);
        frontInput.setAlignment(Pos.CENTER);
        frontInput.getStyleClass().add("editVBox");

        VBox backInput = new VBox();
        backInput.setPrefSize(cardWidth, cardHeight);
        backInput.setMinSize(cardWidth, cardHeight);
        backInput.setMaxSize(cardWidth, cardHeight);
        backInput.setAlignment(Pos.CENTER);
        backInput.getStyleClass().add("editVBox");
        backInput.setSpacing(20);

        ///Term
        TextArea term = new TextArea(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info1"));
        term.setMinHeight(200);
        term.getStyleClass().add("textTitle");
        term.setPromptText("Term");

        ///Pronunciation
        TextArea pronunciation = new TextArea(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info2"));
        term.setMinHeight(50);
        pronunciation.getStyleClass().add("textDetail");
        pronunciation.setPromptText("Pronunciation");


        ///Definition
        TextArea definition = new TextArea(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "info3"));
        definition.setMinHeight(50);
        definition.getStyleClass().add("textDetail");
        definition.setPromptText("Definition");

        ///SaveCancel
        HBox saveCancelBox = new HBox();
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.getStyleClass().add("saveExit");
        cancelButton.getStyleClass().add("saveExit");

        saveButton.setOnAction(e -> {
            saveLanguage(term, pronunciation, definition);
            tableAndStats.saveAllCardsAny(false);
            createCard();
            Main.paneHandler.borderPane.setCenter(cardPane);
        });
        cancelButton.setOnAction(e -> {
            Main.paneHandler.borderPane.setCenter(cardPane);
        });

        saveCancelBox.setSpacing(6);
        saveCancelBox.getChildren().addAll(saveButton, cancelButton);


        ///Create Layout

        frontInput.getChildren().add(term);
        backInput.getChildren().addAll(pronunciation, definition, saveCancelBox);

        sideBySide.getChildren().addAll(frontInput, backInput);

        sideBySide.setSpacing(5);

        return (sideBySide);
    }

    public void saveLanguage(TextArea info1, TextArea info2, TextArea info3) {
        tableAndStats.studyingCards.stringColumn("info1").set(tableAndStats.cardNumber, info1.getText());
        tableAndStats.studyingCards.stringColumn("info2").set(tableAndStats.cardNumber, info2.getText());
        tableAndStats.studyingCards.stringColumn("info3").set(tableAndStats.cardNumber, info3.getText());
        tableAndStats.studyingCards.intColumn("EditDate").set(tableAndStats.cardNumber, (int) (new Date().getTime() / 1000));
    }

    public void changeCardStar() {
        if (this.isStarred) { ///if already starred, unstar it
            this.isStarred = false;
            Main.cardSettings.starredList.remove(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "#"));
        } else {  ///if no starred
            this.isStarred = true;
            Main.cardSettings.starredList.add(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "#"));
        }

        Main.cardSettings.saveSettings();

    }

    public void setCardAside() {
        setAsideCards.append(tableAndStats.studyingCards.row(tableAndStats.cardNumber));
    }

    public void deckSelection(String stringDeckName) {
        this.deckType = 0;
        this.deckName = stringDeckName;
        java.util.List<String> include = this.deckTagHandler.getIncludeList(deckName);
        java.util.List<String> exclude = this.deckTagHandler.getExcludeList(deckName);

        tableAndStats.getDeck(include, exclude);

        ///shuffle and order studying deck
        tableAndStats.shuffle();

        ///Update recently opened stacks
        Main.cardSettings.updateRecentDecks(deckName);

        stackTotal = tableAndStats.studyingCards.rowCount();

        if (stackTotal == 0) {
            AlertBox.display("Error", "No tags included in deck.");
            try {
                Main.paneHandler.borderPane.setCenter(Main.studyPane());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            createCard();
        }
        if (initiation && stackTotal != 0) { /// only generate the first time.
            genStack();
            this.initiation = false;
        }
        column1.setPercentWidth(((tableAndStats.cardNumber + 1) * 100 / stackTotal));

    }

    public String getTimeSinceLastStudied() {

        String firstText = "Last Studied: ";
        String fullText;

        /// current time in minutes
        int todayInt = (int) (new Date().getTime() / 60000);
        /// last study time in minutes
        int studyDate = Integer.parseInt(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "StudyDate"));
        /// time since last studied in minutes
        int timeLastStudied = todayInt - (studyDate/60);

        if (timeLastStudied == 1) {
            fullText = firstText + "1 minute ago";
        } else if (timeLastStudied < 60) { /// less than an hour, tell how many minutes ago
            fullText = firstText + String.format("%d minutes ago", timeLastStudied);
        } else if (timeLastStudied < 120) { /// less than 2 hours, write "hour"
            fullText = firstText + "1 hour ago";
        } else if (timeLastStudied < 1440) { /// less than 1 day, show how many hours ago
            fullText = firstText + String.format("%d hours ago", timeLastStudied / 60);
        } else if (timeLastStudied < 2880) {
            fullText = firstText + "1 day ago";
        } else {
            fullText = firstText + String.format("%d days ago", timeLastStudied / 1440);
        }

        return fullText;
    }

    public GridPane getInfoBox(int cardWidth, int cardHeight){

        GridPane gridPane = new GridPane();

        /// GridPane formatting
        gridPane.getStyleClass().add("infoGrid");

        gridPane.setMinSize(cardWidth * .95, cardHeight * .1);
        gridPane.setPrefSize(cardWidth * .95, cardHeight * .1);
        gridPane.setMaxSize(cardWidth * .95, cardHeight * .1);

        gridPane.setHgap(10);
        gridPane.setVgap(10);

        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        RowConstraints r1 = new RowConstraints();
        RowConstraints r2 = new RowConstraints();

        c1.setPercentWidth(50);
        c2.setPercentWidth(50);
        r1.setPercentHeight(50);
        r2.setPercentHeight(50);

        gridPane.getColumnConstraints().addAll(c1,c2);
        gridPane.getRowConstraints().addAll(r1,r2);

        /// Show card star rating
        int rating = Integer.parseInt(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "trainingNumber"));

        String ratingString = "";

        int evenRating = rating/2;
        int oddRating = rating%2;

        for(int i = 0; i<evenRating;i++){
            ratingString = ratingString + "★";
        }
        if(oddRating == 1){
            ratingString = ratingString + "☆";
        }

        Text cardRating = new Text(ratingString);
        cardRating.getStyleClass().add("starText");
        HBox cardRatingBox = new HBox(cardRating);
        cardRatingBox.getStyleClass().add("infoGridBox");

        /// Show amount studied
        Text amountStudied = new Text("Studied: " + tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "AmountStudied"));
        amountStudied.getStyleClass().add("extraText");
        HBox amountStudiedBox = new HBox(amountStudied);
        amountStudiedBox.getStyleClass().add("infoGridBox");

        /// Show time since last studied
        Text lastStudied = new Text(getTimeSinceLastStudied());
        lastStudied.getStyleClass().add("extraText");
        HBox lastStudiedBox = new HBox(lastStudied);
        lastStudiedBox.getStyleClass().add("infoGridBox");


        gridPane.add(cardRatingBox, 0,0);
        gridPane.add(lastStudiedBox, 0,1);
        gridPane.add(amountStudiedBox, 1,1);

        gridPane.setPadding(new Insets(10, 0, 10, 0));

        return gridPane;
    }

    public void genPreview(Table dataTable) {

        resetVariables();

        if (Main.cardSettings.styleNum == 1) {
            cardPane.setCenter(cardFront);
        } else if (Main.cardSettings.styleNum == 2) {
            cardPane.setCenter(cardBack);
        }
        cardPane.getStylesheets().add(cssPath);

        VBox rightBox = new VBox();
        VBox leftBox = new VBox();

        /// progress bar initiation

        VBox bar = new VBox();
        bar.setPrefHeight(5);
        bar.getStyleClass().add("progressBar");
        bar.setFillWidth(true);
        progressBox.setPrefWidth(10000);
        column1.setPercentWidth(((tableAndStats.cardNumber + 1) * 100 / stackTotal));
        column1.getHgrow();
        progressBox.getColumnConstraints().addAll(this.column1);
        progressBox.setRowIndex(bar, 0);
        progressBox.setColumnIndex(bar, 0);
        progressBox.getChildren().addAll(bar);


        ///Buttons
        Button saveAndExit = new Button("Save & Exit");
        Button editButton = new Button();
        editButton.setPrefSize(32, 32);
        editButton.setMinSize(32, 32);
        editButton.setMaxSize(32, 32);
        editButton.setOnAction(e -> {
            switch (dataTable.getString(tableAndStats.cardNumber, "cardType")) {
                case "general":
                    Main.paneHandler.borderPane.setCenter(editGeneralEntry());
                    break;
                case "date":
                    break;
                case "language":
                    Main.paneHandler.borderPane.setCenter(editLanguageEntry());
                    break;
                case "other":
                    break;
            }
        });
        editButton.getStyleClass().add("editButton");

        ///PROGRESS  BAR
        bottomBox.getChildren().addAll(progressBox);
        bottomBox.setAlignment(Pos.BOTTOM_LEFT);

        ///INFO BAR
        rightBox.getChildren().addAll(saveAndExit);

        ///EDIT BAR
        leftBox.getChildren().addAll(editButton);
        leftBox.setAlignment(Pos.TOP_LEFT);
        leftBox.setPadding(new Insets(10, 10, 10, 10));

        bottomBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bottomBox.setAlignment(Pos.CENTER);

        rightBox.setAlignment(Pos.CENTER);

        cardPane.setBottom(bottomBox);
        cardPane.setRight(rightBox);
        cardPane.setLeft(leftBox);

        cardPane.setOnKeyPressed(event -> {

            /// Standard controls
            if (event.getCode().equals(KeyCode.DOWN)) {
                if (finalCardBool) {
                } else if (front) {
                    flipCard();
                } else if (boolDetails) {
                    flipCard();
                } else {
                    flipCard();
                }
                event.consume();
            } else if (event.getCode().equals(KeyCode.A)) { /// decrease know, minimum at 0, set to 5 if high value
                tableAndStats.studyingCards.intColumn("trainingNumber").set(tableAndStats.cardNumber, (
                        Math.max(0, Math.min(5, Integer.parseInt(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "trainingNumber")) - 2)))
                );
                /// set aside to study again
                swapCard(true);
                event.consume();
            } else if (event.getCode().equals(KeyCode.D)) { /// increase know, max at 10
                tableAndStats.studyingCards.intColumn("trainingNumber").set(tableAndStats.cardNumber, (
                        Math.min(10, Integer.parseInt(tableAndStats.studyingCards.getString(tableAndStats.cardNumber, "trainingNumber")) + 1))
                );
                swapCard(true);
                event.consume();
            } else if (event.getCode().equals(KeyCode.UP)) {
                event.consume();
            } else if (event.getCode().equals(KeyCode.LEFT)) {
                swapCard(false);
                event.consume();
            } else if (event.getCode().equals(KeyCode.RIGHT)) {
                swapCard(true);
                event.consume();
            }
            /// Special controls
            else if (event.getCode().equals(KeyCode.S)) { /// star card
                changeCardStar();
                event.consume();
            }

        });


        saveAndExit.setOnAction(e -> {
                    endCard();
                }
        );

    }

    public void previewCard(Table table, String cardType){

        int cardWidth = 600;
        int cardHeight = 800;

        HBox topBox = new HBox();
        HBox spacer = new HBox();

        Button starButton = new Button();

        spacer.setPrefWidth(20);

        topBox.setPadding(new Insets(5, 5, 5, 5));
        topBox.getChildren().addAll(starButton);

        /// GENERIC CARD
        if (cardType.equals("general")) {

            ///Create Card Front
            Text title = new Text(table.getString(tableAndStats.cardNumber, "info1"));
            title.getStyleClass().add("frontText");
            this.cardFront = new VBox(title);
            cardFront.setMaxSize(cardWidth, cardHeight);
            cardFront.getStyleClass().add("card");
            title.setWrappingWidth(cardWidth * .95);
            title.setTextAlignment(TextAlignment.CENTER);

            ///Create Card Back
            this.boolDetails = table.getString(tableAndStats.cardNumber, "info3text").isEmpty();

            Text details = new Text(table.getString(tableAndStats.cardNumber, "info2"));
            VBox detailsBox = new VBox(details);
            details.getStyleClass().add("detailText");
            details.setTextAlignment(TextAlignment.CENTER);
            this.cardBack = new VBox(topBox);
            cardBack.setMaxSize(cardWidth, cardHeight);
            cardBack.getStyleClass().add("card");
            details.setWrappingWidth(cardWidth * .95);
            detailsBox.setAlignment(Pos.CENTER);
            detailsBox.getStyleClass().add("detailText");


            ///If there are details: add only big major
            if (boolDetails) {
                detailsBox.setPrefHeight(5000); /// fills height
                cardBack.getChildren().addAll(detailsBox);
            } else {
                String content = table.getString(tableAndStats.cardNumber, "info3");
                detailsBox.setPrefHeight(5000); /// fills height
                WebView webView = new WebView();
                webView.getEngine().loadContent(content);
                webView.setMinSize(cardWidth * .925, cardHeight * .42);
                webView.setMaxSize(cardWidth * .925, cardHeight * .42);
                Scale scale = new Scale(1, 1);
                scale.setPivotX(cardWidth * .4625);
                scale.setPivotY(cardHeight * .21);
                webView.getTransforms().setAll(scale);
                this.cardDetails = new VBox();
                cardDetails.setAlignment(Pos.CENTER);
                cardDetails.setMinSize(cardWidth * .975, cardHeight * .46);
                cardDetails.setMaxSize(cardWidth * .975, cardHeight * .46);
                cardDetails.getStyleClass().add("cardDetails");
                cardDetails.getChildren().add(webView);
                cardBack.getChildren().addAll(detailsBox, cardDetails);

            }

            cardBack.getChildren().add(getInfoBox(cardWidth,cardHeight));
            front = true;
            cardPane.setCenter(cardFront);
        }

        /// LANGUAGE CARD
        else if (cardType.equals("language")) {

            Text location1;
            Text location2;
            Text location3 = new Text();

            ///Create Card Front
            Text info1 = new Text(table.getString(tableAndStats.cardNumber, "info1"));
            Text info2 = new Text(table.getString(tableAndStats.cardNumber, "info2"));
            Text info3 = new Text(table.getString(tableAndStats.cardNumber, "info3"));

            info1.getStyleClass().add("langTermText");
            info2.getStyleClass().add("langPronText");
            info3.getStyleClass().add("langDefText");

            if (studyStyle == 1) { ///Term on front
                location1 = info1;
                if (info2.getText() == ("")) { ///no pronunciation
                    location2 = info3;
                    boolDetails = true;
                } else if (info3.getText() == ("")) { ///no definition
                    location2 = info2;
                    boolDetails = true;
                } else {   ///has all STANDARD
                    location2 = info2;
                    location3 = info3;
                    boolDetails = false;
                }
            } else {  /// Reverse flash card
                if (info3.getText() == ("")) { /// show pronunciation on front
                    location1 = info2;
                    location2 = info1;
                    boolDetails = true;
                } else if (info2.getText() == ("")) { /// show definition on front
                    location1 = info3;
                    location1.getStyleClass().add("langTermText");
                    location2 = info1;
                    boolDetails = true;
                } else { ///has everything STANDARD
                    location1 = info3;
                    location2 = info1;
                    location3 = info2;
                    boolDetails = false;
                }
            }

            this.cardFront = new VBox(location1);
            cardFront.setMaxSize(cardWidth, cardHeight);
            cardFront.getStyleClass().add("card");

            ///Create Card Back

            VBox box2 = new VBox(location2);
            location2.getStyleClass().add("detailText");
            location2.setTextAlignment(TextAlignment.CENTER);
            location2.setWrappingWidth(cardWidth * .95);
            box2.setAlignment(Pos.CENTER);
            box2.getStyleClass().add("detailText");

            this.cardBack = new VBox(topBox);
            cardBack.setMaxSize(cardWidth, cardHeight);
            cardBack.getStyleClass().add("card");

            if (boolDetails) {
                box2.setPrefHeight(3000);
                cardBack.getChildren().addAll(box2);
            } else {
                box2.setMinHeight(cardHeight * .45);
                box2.setAlignment(Pos.BOTTOM_CENTER);
                box2.getStyleClass().add("box2");

                VBox box3 = new VBox(location3);
                location3.getStyleClass().add("detailText");
                location3.setTextAlignment(TextAlignment.CENTER);
                location3.setWrappingWidth(cardWidth * .95);
                box3.setAlignment(Pos.TOP_CENTER);
                box3.getStyleClass().add("detailText");
                box3.setPrefHeight(3000);

                cardBack.getChildren().addAll(box2, box3);
            }

            cardBack.getChildren().add(getInfoBox(cardWidth, cardHeight));

            front = true;
            cardPane.setCenter(cardFront);

        }

    }
}
