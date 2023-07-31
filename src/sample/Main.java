package sample;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.SegmentedButton;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class Main extends Application {

    public static Stage window;

    public static focusTimer focusTimer = new focusTimer();
    Scene mainMenu;
    Insets insetLeft = new Insets(4, 5, 4, 4);
    String cssPath = "file:resources/styleSheets/mainMenu.css";

    public static PaneHandler paneHandler = new PaneHandler();
    BorderPane borderPane = paneHandler.borderPane;

    public static TableAndStats tableAndStats = new TableAndStats();
    public static CardSettings cardSettings = new CardSettings();
    public static DeckTagHandler deckTagHandler = new DeckTagHandler();

    public Stats stats = null;

    int currentCardType = 1;

    /// RIGHT PANE SAVER
    Boolean resetTags = true;
    VBox rightPane = new VBox();
    TextField source = new TextField();

    private List currentTagsList = new ArrayList();

    private static int miniDeckHeight = 240;
    private static int miniDeckWidth = (int) (miniDeckHeight * .75);
    private static int miniCardHeight = 200;
    private static int miniCardWidth = (int) (miniCardHeight * .75);


    private static int miniDeckRadius = 40;
    private static int miniStackRadius = 20;


    public static void main(String[] args) {
        launch(args);
    }

    /// Tags
    public static void comboBoxAddTag(FlowPane pane, List theList, ComboBox theBox) {
        String tag = (String) theBox.getValue();
        tag = "<" + tag.trim() + ">";
        if (!theList.contains(tag) && !tag.equals("<>")) {
            Button button = new Button(tag.substring(1, tag.length() - 1));
            button.setFocusTraversable(false);
            theList.add(tag);
            pane.getChildren().add(button);
            button.getStyleClass().add("tagsButton");
            button.setOnAction(e -> {
                comboBoxRemoveTag(button, pane, theList);
            });
        }
        theBox.setValue("");
    }

    public static void comboBoxRemoveTag(Button button, FlowPane box, List list) {
        list.remove("<" + button.getText() + ">");
        box.getChildren().remove(button);
    }

    public static VBox cardInput(TextArea title, TextArea major, HTMLEditor minor, SegmentedButton seg, TextField source, HBox saveExit, Insets inset) {
        VBox inputBox = new VBox();
        inputBox.getStyleClass().add("centerPane");
        inputBox.getChildren().addAll(title,
                major,
                minor,
                seg,
                source,
                saveExit);
        inputBox.setPadding(inset);

        return inputBox;
    }

    public static VBox cardInputLanguage(TextArea title, TextArea major, TextArea minor, HBox saveExit, Insets inset) {
        VBox inputBox = new VBox();
        inputBox.getStyleClass().add("centerPane");
        inputBox.getChildren().addAll(title,
                major,
                minor,
                saveExit);
        inputBox.setPadding(inset);

        return inputBox;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        cardSettings.cardSettings();
        deckTagHandler.deckHandler();
        tableAndStats.tableAndStats();
        window = primaryStage;
        focusTimer.setUpTimer(window);
        //primaryStage.setMaximized(true);
        primaryStage.setHeight(1000);
        primaryStage.setWidth(1600);

        primaryStage.setMinHeight(1000);
        primaryStage.setMinWidth(1600);


        // Top Menu

        paneHandler.updateTopPane(cardSettings, tableAndStats);
        // Left Menu
        VBox leftMenu = new VBox();

        ///create icons
        InputStream inputImage1 = Files.newInputStream(Paths.get("resources/imgs/addCard.png"));
        Image streamImage1 = new Image(inputImage1);
        ImageView image1 = new ImageView(streamImage1);
        image1.setFitWidth(32);
        image1.setFitHeight(32);

        InputStream inputImage2 = Files.newInputStream(Paths.get("resources/imgs/decks.png"));
        Image streamImage2 = new Image(inputImage2);
        ImageView image2 = new ImageView(streamImage2);
        image2.setFitWidth(32);
        image2.setFitHeight(32);

        InputStream inputImage4 = Files.newInputStream(Paths.get("resources/imgs/stats.png"));
        Image streamImage4 = new Image(inputImage4);
        ImageView image4 = new ImageView(streamImage4);
        image4.setFitWidth(32);
        image4.setFitHeight(32);

        InputStream inputImage5 = Files.newInputStream(Paths.get("resources/imgs/settingsicon.png"));
        Image streamImage5 = new Image(inputImage5);
        ImageView image5 = new ImageView(streamImage5);
        image5.setFitWidth(32);
        image5.setFitHeight(32);

        ///format icon images
        image5.setPreserveRatio(true);
        image5.setFitWidth(32);
        image5.setFitHeight(32);

        ///make buttons with images
        Button button1 = new Button("New Idea", image1);
        Button button2 = new Button("Study", image2);
        Button button3 = new Button("Synergize");
        Button button4 = new Button("Statistics", image4);
        Button button5 = new Button("Settings", image5);

        int buttonWidth = 170;
        int buttonHeight = 60;

        button1.setPrefSize(buttonWidth, buttonHeight);
        button2.setPrefSize(buttonWidth, buttonHeight);
        button3.setPrefSize(buttonWidth, buttonHeight);
        button4.setPrefSize(buttonWidth, buttonHeight);
        button5.setPrefSize(buttonWidth, buttonHeight);

        button1.setFocusTraversable(false);
        button2.setFocusTraversable(false);
        button3.setFocusTraversable(false);
        button4.setFocusTraversable(false);
        button5.setFocusTraversable(false);

        button1.getStyleClass().add("button1");
        button2.getStyleClass().add("button2");
        button3.getStyleClass().add("button3");
        button4.getStyleClass().add("button4");
        button5.getStyleClass().add("button5");

        VBox button1ExpansionBox = new VBox();
        button1ExpansionBox.setVisible(false);
        button1ExpansionBox.setManaged(false);

        VBox button2ExpansionBox = new VBox();
        VBox button3ExpansionBox = new VBox();
        VBox button4ExpansionBox = new VBox();
        VBox button5ExpansionBox = new VBox();

        button1ExpansionBox.getStyleClass().add("button1ExpansionBox");
        button2ExpansionBox.getStyleClass().add("button2ExpansionBox");
        button3ExpansionBox.getStyleClass().add("button3ExpansionBox");
        button4ExpansionBox.getStyleClass().add("button4ExpansionBox");
        button5ExpansionBox.getStyleClass().add("button5ExpansionBox");

        VBox button1BaseBox = new VBox();
        VBox button2BaseBox = new VBox();
        VBox button3BaseBox = new VBox();
        VBox button4BaseBox = new VBox();
        VBox button5BaseBox = new VBox();

        button1BaseBox.getStyleClass().add("button1ExpansionBox");
        button2BaseBox.getStyleClass().add("button2ExpansionBox");
        button3BaseBox.getStyleClass().add("button3ExpansionBox");
        button4BaseBox.getStyleClass().add("button4ExpansionBox");
        button5BaseBox.getStyleClass().add("button5ExpansionBox");


        /// Button 1 Addons
        Button generalCard = new Button("        ‣   General");
        Button datedCard = new Button("        ‣   Dated");
        Button languageCard = new Button("        ‣   Language");
        Button otherCard = new Button("        ‣   Other");
        Button importCard = new Button("        ‣   Import");

        generalCard.getStyleClass().add("button1Selection");
        datedCard.getStyleClass().add("button1Selection");
        languageCard.getStyleClass().add("button1Selection");
        otherCard.getStyleClass().add("button1Selection");
        importCard.getStyleClass().add("button1Selection");

        generalCard.setPrefSize(buttonWidth, buttonHeight * .75);
        datedCard.setPrefSize(buttonWidth, buttonHeight * .75);
        languageCard.setPrefSize(buttonWidth, buttonHeight * .75);
        otherCard.setPrefSize(buttonWidth, buttonHeight * .75);
        importCard.setPrefSize(buttonWidth, buttonHeight * .75);


        generalCard.setOnAction(f -> {
            try {
                borderPane.setCenter(createGeneralEntry());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            this.currentCardType = 1;
        });

        languageCard.setOnAction(f -> {
            try {
                borderPane.setCenter(createLanguageEntry());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        datedCard.setOnAction(f -> {
        });

        otherCard.setOnAction(f -> {
        });

        importCard.setOnAction(f -> {
            try {
                borderPane.setCenter(importCards());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        VBox padding = new VBox();
        padding.setPrefSize(10, 10);

        button1ExpansionBox.getChildren().addAll(generalCard, languageCard, importCard, padding);
        button1BaseBox.getChildren().addAll(button1, button1ExpansionBox);

        /// Button 2 Addons
        RadioButton rb1 = new RadioButton("Front");
        RadioButton rb2 = new RadioButton("Back");
        rb1.setMinWidth(75);
        rb2.setMinWidth(75);

        rb1.getStyleClass().remove("radio-button");
        rb2.getStyleClass().remove("radio-button");
        rb1.getStyleClass().add("toggle-button");
        rb2.getStyleClass().add("toggle-button");
        rb1.getStyleClass().add("styleToggles");
        rb2.getStyleClass().add("styleToggles");

        if (cardSettings.styleNum == 1) {
            rb1.selectedProperty().set(true);
        } else if (cardSettings.styleNum == 2) {
            rb2.selectedProperty().set(true);
        }

        ToggleGroup toggleGroup = new ToggleGroup();

        rb1.setToggleGroup(toggleGroup);
        rb2.setToggleGroup(toggleGroup);
        rb1.setFocusTraversable(false);
        rb2.setFocusTraversable(false);

        rb1.setOnAction(e -> {
            cardSettings.styleNum = 1;
            cardSettings.saveSettings();
        });
        rb2.setOnAction(e -> {
            cardSettings.styleNum = 2;
            cardSettings.saveSettings();
        });

        SegmentedButton seg = new SegmentedButton();
        seg.getButtons().addAll(rb1, rb2);

        seg.setFocusTraversable(false);

        VBox padding2 = new VBox();
        padding2.setMinHeight(7);

        RadioButton st1 = new RadioButton("20");
        RadioButton st2 = new RadioButton("50");
        RadioButton st3 = new RadioButton("100");
        st1.setMinWidth(50);
        st2.setMinWidth(50);
        st3.setMinWidth(50);

        st1.getStyleClass().remove("radio-button");
        st2.getStyleClass().remove("radio-button");
        st3.getStyleClass().remove("radio-button");
        st1.getStyleClass().add("toggle-button");
        st2.getStyleClass().add("toggle-button");
        st3.getStyleClass().add("toggle-button");
        st1.getStyleClass().add("styleToggles");
        st2.getStyleClass().add("styleToggles");
        st3.getStyleClass().add("styleToggles");

        ///set default selection
        switch (cardSettings.deckSize) {
            case 20:
                st1.setSelected(true);
                break;
            case 50:
                st2.setSelected(true);
                break;
            case 100:
                st3.setSelected(true);
                break;
        }

        ToggleGroup toggleGroup1 = new ToggleGroup();

        st1.setToggleGroup(toggleGroup1);
        st2.setToggleGroup(toggleGroup1);
        st3.setToggleGroup(toggleGroup1);

        st1.setOnAction(e -> {
            cardSettings.setDeckSize(20);
            cardSettings.saveSettings();
        });
        st2.setOnAction(e -> {
            cardSettings.setDeckSize(50);
            cardSettings.saveSettings();
        });
        st3.setOnAction(e -> {
            cardSettings.setDeckSize(100);
            cardSettings.saveSettings();
        });

        SegmentedButton seg1 = new SegmentedButton();
        seg1.getButtons().addAll(st1, st2, st3);

        st1.setFocusTraversable(false);
        st2.setFocusTraversable(false);
        st3.setFocusTraversable(false);
        seg1.setFocusTraversable(false);

        button2ExpansionBox.getChildren().addAll(seg1, seg, padding2);
        button2BaseBox.getChildren().addAll(button2, button2ExpansionBox);

        /// Button 3 Addons

        button3ExpansionBox.getChildren().addAll();
        button3BaseBox.getChildren().addAll(button3, button3ExpansionBox);

        /// Button 4 Addons

        Button allStats = new Button("        ‣   All");
        Button deckStats = new Button("        ‣   Deck");
        Button tagStats = new Button("        ‣   Tag");

        allStats.getStyleClass().add("button4Selection");
        deckStats.getStyleClass().add("button4Selection");
        tagStats.getStyleClass().add("button4Selection");

        allStats.setPrefSize(buttonWidth, buttonHeight * .75);
        deckStats.setPrefSize(buttonWidth, buttonHeight * .75);
        tagStats.setPrefSize(buttonWidth, buttonHeight * .75);

        allStats.setOnAction(e -> {
            stats.set0AllStats();
        });

        deckStats.setOnAction(e -> {
            stats.set1DeckStats();
        });

        tagStats.setOnAction(e -> {
            stats.set2TagStats();
        });

        button4ExpansionBox.getChildren().addAll(allStats, deckStats, tagStats);
        button4BaseBox.getChildren().addAll(button4, button4ExpansionBox);

        button4ExpansionBox.setVisible(false);
        button4ExpansionBox.setManaged(false);

        /// Button 5 Addons

        button5ExpansionBox.getChildren().addAll();
        button5BaseBox.getChildren().addAll(button5, button5ExpansionBox);

        leftMenu.setSpacing(4);
        leftMenu.setPadding(insetLeft);
        leftMenu.getStyleClass().add("leftPane");

        leftMenu.getChildren().addAll(button1BaseBox, button2BaseBox, button3BaseBox, button4BaseBox, button5BaseBox);


        // Borderpane
        borderPane.setLeft(leftMenu);
        borderPane.getStyleClass().add("borderPane");

        ///Sidebar Buttons
        button1.setOnAction(e -> {

            collapseAllOtherButtons(button2ExpansionBox, button3ExpansionBox, button4ExpansionBox, button5ExpansionBox);
            expandButton(button1ExpansionBox);

            ///clears right pane
            paneHandler.borderPane.setRight(null);

            resetTags = true;


            try {
                borderPane.setCenter(createGeneralEntry());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });

        button2.setOnAction(e -> {
                    collapseAllOtherButtons(button1ExpansionBox, button3ExpansionBox, button4ExpansionBox, button5ExpansionBox);
                    expandButton(button2ExpansionBox);
                    try {
                        borderPane.setRight(null);
                        borderPane.setCenter(studyPane());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        );

        button3.setOnAction(e -> {
            collapseAllOtherButtons(button1ExpansionBox, button2ExpansionBox, button4ExpansionBox, button5ExpansionBox);
            expandButton(button3ExpansionBox);
            borderPane.setRight(null);
        });

        button4.setOnAction(e -> {
            collapseAllOtherButtons(button1ExpansionBox, button2ExpansionBox, button3ExpansionBox, button5ExpansionBox);
            expandButton(button4ExpansionBox);
            borderPane.setRight(null);
            try {
                this.stats = new Stats();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            stats.set0AllStats();
            borderPane.setCenter(stats.statsPane);
        });

        Button closeButton = new Button("Close Request");
        closeButton.setOnAction(e -> closeProgram());

        mainMenu = new Scene(borderPane);
        mainMenu.getStylesheets().add(cssPath);

        borderPane.setCenter(studyPane());

        window.setScene(mainMenu);
        window.setMinWidth(1700);
        window.setTitle("Idea Engine");

        window.getIcons().add(new Image("file:resources/imgs/iconmaybe.png"));
        window.show();

        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });

    }

    private void closeProgram() {
        Boolean answer = ConfirmBox.display("Close Program", "Are you sure?");
        if (answer) {
            System.exit(0);
            window.close();
        }
    }

    public void changeCenterMenu(Button button, Node node, BorderPane borderScene) {
        button.setOnAction(e -> borderScene.setCenter(node));
    }

    /// New Idea TAB

    public HBox createGeneralEntry() throws IOException {

        this.currentCardType = 1;

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
        frontInput.getStyleClass().add("entryVBox");

        VBox backInput = new VBox();
        backInput.setPrefSize(cardWidth, cardHeight);
        backInput.setMinSize(cardWidth, cardHeight);
        backInput.setMaxSize(cardWidth, cardHeight);
        backInput.setAlignment(Pos.CENTER);
        backInput.getStyleClass().add("entryVBox");
        backInput.setSpacing(20);

        AtomicInteger priorityNum = new AtomicInteger(1);

        ///Title
        TextArea titleField = new TextArea();
        titleField.getStyleClass().add("textTitle");
        titleField.setPromptText("Idea");
        titleField.setPrefHeight(150);
        titleField.setWrapText(true);

        ///Main Idea
        TextArea majorDetail = new TextArea();
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


        ///Create Layout

        frontInput.getChildren().add(titleField);
        backInput.getChildren().addAll(majorDetail, minorDetail, seg);

        sideBySide.getChildren().addAll(frontInput, backInput);

        sideBySide.setSpacing(5);

        TextArea minorDetailText = new TextArea();
        minorDetailText.setText(minorDetail.getHtmlText());

        borderPane.setRight(tagSavePane(titleField, majorDetail, minorDetailText, priorityNum, borderPane));
        return (sideBySide);

    }

    public HBox createLanguageEntry() throws IOException {

        this.currentCardType = 3;

        HBox sideBySide = new HBox();
        sideBySide.setAlignment(Pos.CENTER);
        int cardWidth = 600;
        int cardHeight = 800;

        VBox frontInput = new VBox();
        frontInput.setPrefSize(cardWidth, cardHeight);
        frontInput.setMinSize(cardWidth, cardHeight);
        frontInput.setMaxSize(cardWidth, cardHeight);
        frontInput.setAlignment(Pos.CENTER);
        frontInput.getStyleClass().add("entryVBox");

        VBox backInput = new VBox();
        backInput.setPrefSize(cardWidth, cardHeight);
        backInput.setMinSize(cardWidth, cardHeight);
        backInput.setMaxSize(cardWidth, cardHeight);
        backInput.setAlignment(Pos.CENTER);
        backInput.getStyleClass().add("entryVBox");
        backInput.setSpacing(20);

        AtomicInteger priorityNum = new AtomicInteger(1);

        ///Term
        TextArea titleField = new TextArea();
        titleField.setMinHeight(200);
        titleField.getStyleClass().add("textTitle");
        titleField.setPromptText("Term");

        ///Pronunciation
        TextArea majorDetail = new TextArea();
        titleField.setMinHeight(50);
        majorDetail.getStyleClass().add("textDetail");
        majorDetail.setPromptText("Pronunciation");


        ///Details
        TextArea minorDetail = new TextArea();
        minorDetail.setMinHeight(50);
        minorDetail.getStyleClass().add("textDetail");
        minorDetail.setPromptText("Definition");

        ///Create Layout

        frontInput.getChildren().add(titleField);
        backInput.getChildren().addAll(majorDetail, minorDetail);

        sideBySide.getChildren().addAll(frontInput, backInput);

        sideBySide.setSpacing(5);

        borderPane.setRight(tagSavePane(titleField, majorDetail, minorDetail, priorityNum, borderPane));
        return (sideBySide);

    }

    public HBox importCards() throws IOException {

        AtomicReference<Boolean> isText = new AtomicReference<>(true);

        HBox importDeckBox = new HBox();
        importDeckBox.setAlignment(Pos.CENTER);
        importDeckBox.getStyleClass().add("importCardEditor");

        /// create layout
        VBox dataBox = new VBox();
        dataBox.setAlignment(Pos.CENTER);
        VBox optionBox = new VBox();
        optionBox.setAlignment(Pos.CENTER);

        dataBox.setPrefWidth(500);
        optionBox.setPrefWidth(500);

        /// box for data
        VBox dataSourceBox = new VBox();
        dataSourceBox.setPrefWidth(400);
        dataSourceBox.getStyleClass().add("separationBox");

        /// import type toggle
        RadioButton rb1 = new RadioButton("Import From Text");
        RadioButton rb2 = new RadioButton("Import From CSV");
        rb1.setMinWidth(70);
        rb2.setMinWidth(70);

        rb1.getStyleClass().remove("radio-button");
        rb2.getStyleClass().remove("radio-button");
        rb1.getStyleClass().add("toggle-button");
        rb2.getStyleClass().add("toggle-button");
        rb1.getStyleClass().add("priorityToggles");
        rb2.getStyleClass().add("priorityToggles");

        rb1.selectedProperty().set(true);

        ToggleGroup toggleGroup = new ToggleGroup();

        rb1.setToggleGroup(toggleGroup);
        rb2.setToggleGroup(toggleGroup);


        SegmentedButton seg = new SegmentedButton();
        seg.getButtons().addAll(rb1, rb2);

        /// Import as Text
        VBox importAsTextBox = new VBox();
        Text deckInputTextTitle = new Text("Import Data");
        TextArea deckInputText = new TextArea();
        deckInputText.setWrapText(true);

        deckInputText.setPrefSize(400, 800);
        deckInputTextTitle.getStyleClass().add("redTextMedium");
        deckInputText.getStyleClass().add("dataText");

        importAsTextBox.getChildren().addAll(deckInputTextTitle, deckInputText);
        importAsTextBox.managedProperty().set(true);
        importAsTextBox.setVisible(true);

        /// Import as CSV
        VBox importAsCSVBox = new VBox();
        HBox urlButtonContainer = new HBox();
        Text pictureInputText = new Text("CSV File");
        TextField urlFileChooser = new TextField();

        pictureInputText.getStyleClass().add("redTextMedium");
        urlFileChooser.getStyleClass().add("textDetail");

        importAsCSVBox.getChildren().add(urlButtonContainer);
        importAsCSVBox.managedProperty().set(false);
        importAsCSVBox.setVisible(false);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload File Path");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        Button addCSVButton = new Button("+");
        addCSVButton.getStyleClass().add("plusButton");
        addCSVButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(paneHandler.borderPane.getScene().getWindow());
            urlFileChooser.setText(file.getPath());
            paneHandler.setPreviewer(createImage(file.getPath()));

            HBox previewContainer = new HBox();
            previewContainer.getStyleClass().add("inputBox");
            previewContainer.getChildren().add(paneHandler.getPreviewer());
        });

        urlButtonContainer.getChildren().addAll(urlFileChooser, addCSVButton);
        urlButtonContainer.setAlignment(Pos.CENTER_LEFT);


        /// Options for text data

        VBox textOptions = new VBox();
        textOptions.setSpacing(10);
        TextArea infoSeparator1 = new TextArea();
        TextArea infoSeparator2 = new TextArea();
        TextArea dataSeparator = new TextArea();
        VBox sep0Box = new VBox();
        VBox sep1Box = new VBox();
        VBox sep2Box = new VBox();

        HBox buttonBox0 = new HBox();
        Button newLine0 = new Button("New Line");
        Button tab0 = new Button("Tab");
        Text separator0Text = new Text("Data Separator");

        newLine0.setOnAction(e -> {
            dataSeparator.setText(dataSeparator.getText() + "\\n");
        });
        tab0.setOnAction(e -> {
            dataSeparator.setText(dataSeparator.getText() + "\\t");
        });

        buttonBox0.getChildren().addAll(newLine0, tab0);

        HBox buttonBox1 = new HBox();
        Button newLine1 = new Button("New Line");
        Button tab1 = new Button("Tab");
        Text separator1Text = new Text("Info Separator 1");

        newLine1.setOnAction(e -> {
            infoSeparator1.setText(infoSeparator1.getText() + "\\n");
        });
        tab1.setOnAction(e -> {
            infoSeparator1.setText(infoSeparator1.getText() + "\\t");
        });

        buttonBox1.getChildren().addAll(newLine1, tab1);

        HBox buttonBox2 = new HBox();
        Button newLine2 = new Button("New Line");
        Button tab2 = new Button("Tab");
        Text separator2Text = new Text("Info Separator 2");

        newLine2.setOnAction(e -> {
            infoSeparator2.setText(infoSeparator2.getText() + "\\n");
        });
        tab2.setOnAction(e -> {
            infoSeparator2.setText(infoSeparator2.getText() + "\\t");
        });

        buttonBox2.getChildren().addAll(newLine2, tab2);


        /// Styling
        separator0Text.getStyleClass().add("redTextSmall");
        separator1Text.getStyleClass().add("redTextSmall");
        separator2Text.getStyleClass().add("redTextSmall");
        tab0.getStyleClass().add("saveExit");
        tab1.getStyleClass().add("saveExit");
        tab2.getStyleClass().add("saveExit");
        newLine0.getStyleClass().add("saveExit");
        newLine1.getStyleClass().add("saveExit");
        newLine2.getStyleClass().add("saveExit");
        buttonBox0.setSpacing(5);
        buttonBox1.setSpacing(5);
        buttonBox2.setSpacing(5);
        sep0Box.getStyleClass().addAll("optionNodes");
        sep1Box.getStyleClass().addAll("optionNodes");
        sep2Box.getStyleClass().addAll("optionNodes");
        sep0Box.setSpacing(5);
        sep1Box.setSpacing(5);
        sep2Box.setSpacing(5);
        infoSeparator1.getStyleClass().add("separatorTextField");
        infoSeparator2.getStyleClass().add("separatorTextField");
        dataSeparator.getStyleClass().add("separatorTextField");

        RadioButton rb5 = new RadioButton("General");
        RadioButton rb6 = new RadioButton("Language");
        rb5.setMinWidth(100);
        rb6.setMinWidth(100);

        rb5.getStyleClass().remove("radio-button");
        rb6.getStyleClass().remove("radio-button");
        rb5.getStyleClass().add("toggle-button");
        rb6.getStyleClass().add("toggle-button");
        rb5.getStyleClass().add("priorityToggles");
        rb6.getStyleClass().add("priorityToggles");

        rb5.selectedProperty().set(true);

        ToggleGroup toggleGroup3 = new ToggleGroup();

        rb5.setToggleGroup(toggleGroup3);
        rb6.setToggleGroup(toggleGroup3);

        SegmentedButton seg3 = new SegmentedButton();
        seg3.getButtons().addAll(rb5, rb6);

        rb5.setOnAction(e -> {

        });
        rb6.setOnAction(e -> {

        });

        sep0Box.getChildren().addAll(separator0Text, buttonBox0, dataSeparator);
        sep1Box.getChildren().addAll(separator1Text, buttonBox1, infoSeparator1);
        sep2Box.getChildren().addAll(separator2Text, buttonBox2, infoSeparator2);

        textOptions.getChildren().addAll(seg3, sep0Box, sep1Box, sep2Box);

        textOptions.visibleProperty().set(true);
        textOptions.setManaged(true);

        /// Options for CSV Data
        VBox CSVOptions = new VBox();
        CSVOptions.getStyleClass().add("scrollNodes");

        /// OPTIONS

        RadioButton rb3 = new RadioButton("General");
        RadioButton rb4 = new RadioButton("Language");
        rb3.setMinWidth(70);
        rb4.setMinWidth(70);

        rb3.getStyleClass().remove("radio-button");
        rb4.getStyleClass().remove("radio-button");
        rb3.getStyleClass().add("toggle-button");
        rb4.getStyleClass().add("toggle-button");
        rb3.getStyleClass().add("priorityToggles");
        rb4.getStyleClass().add("priorityToggles");

        rb3.selectedProperty().set(true);

        ToggleGroup toggleGroup2 = new ToggleGroup();

        rb3.setToggleGroup(toggleGroup2);
        rb4.setToggleGroup(toggleGroup2);

        SegmentedButton seg2 = new SegmentedButton();
        seg2.getButtons().addAll(rb3, rb4);

        rb3.setOnAction(e -> {

        });
        rb4.setOnAction(e -> {

        });

        CSVOptions.getChildren().addAll(seg2);

        CSVOptions.setManaged(false);
        CSVOptions.visibleProperty().set(false);


        /// Set button actions

        rb1.setOnAction(e -> {
            importAsTextBox.managedProperty().set(true);
            importAsTextBox.setVisible(true);
            importAsCSVBox.managedProperty().set(false);
            importAsCSVBox.setVisible(false);

            textOptions.managedProperty().set(true);
            textOptions.setVisible(true);
            CSVOptions.managedProperty().set(false);
            CSVOptions.setVisible(false);

            isText.set(true);

        });
        rb2.setOnAction(e -> {
            importAsTextBox.managedProperty().set(false);
            importAsTextBox.setVisible(false);
            importAsCSVBox.managedProperty().set(true);
            importAsCSVBox.setVisible(true);

            textOptions.managedProperty().set(false);
            textOptions.setVisible(false);
            CSVOptions.managedProperty().set(true);
            CSVOptions.setVisible(true);

            isText.set(false);

        });

        HBox saveCancelBox = new HBox();
        Button previewButton = new Button("Preview");
        Button cancelButton = new Button("Cancel");

        previewButton.getStyleClass().add("saveExit");
        cancelButton.getStyleClass().add("saveExit");
        saveCancelBox.setSpacing(10);
        saveCancelBox.setAlignment(Pos.CENTER);

        saveCancelBox.getChildren().addAll(previewButton, cancelButton);

        /// Preview Pane
        VBox previewBox = new VBox();
        Button saveButton = new Button("Save");


        previewBox.setManaged(false);
        previewBox.setVisible(false);

        previewButton.setOnAction(e -> {
            previewBox.getChildren().clear();

            VBox cardFront = new VBox();
            VBox cardBack = new VBox();
            String fullText = deckInputText.getText();

            String ds = dataSeparator.getText().replace("\\t", "\t").replace("\\n", "\n");
            String is1 = infoSeparator1.getText().replace("\\t", "\t").replace("\\n", "\n");
            String is2 = infoSeparator2.getText().replace("\\t", "\t").replace("\\n", "\n");

            Table dataTable = textToTable(fullText, ds, is1, is2);

            String splitted[] = fullText.split(ds);

            String info1 = StringUtils.substringBefore(splitted[0], is1).trim();
            String info2 = StringUtils.substringBetween(splitted[0], is1, is2).trim();
            String info3 = StringUtils.substringAfter(splitted[0], is2).trim();

            Text info1Text = new Text(info1);
            Text info2Text = new Text(info2);
            Text info3Text = new Text(info3);

            cardFront.getChildren().addAll(info1Text);
            cardBack.getChildren().addAll(info2Text, info3Text);

            previewBox.getChildren().addAll(cardFront, cardBack);

            previewBox.setVisible(true);
            previewBox.setManaged(true);
        });
        cancelButton.setOnAction(e -> {
            try {
                borderPane.setCenter(studyPane());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        ///Create Layout


        dataBox.getChildren().addAll(seg, dataSourceBox, importAsCSVBox, importAsTextBox);
        dataBox.getStyleClass().add("inputBox");

        optionBox.getChildren().addAll(textOptions, CSVOptions, saveCancelBox);
        optionBox.getStyleClass().add("inputBox");

        importDeckBox.getChildren().addAll(dataBox, optionBox, previewBox);
        importDeckBox.setSpacing(5);

        return importDeckBox;

    }


    public VBox tagSavePane(TextArea titleField, TextArea majorDetail, TextArea minorDetail, AtomicInteger priorityNum, BorderPane borderPane) throws IOException {
        ///Tags

        String[] tagsList = Files.readAllLines(Paths.get("resources/backendInfo.txt")).get(2).split(",");
        ComboBox tags = new ComboBox(FXCollections.observableArrayList(tagsList));

        rightPane = new VBox();
        rightPane.setAlignment(Pos.CENTER);

        if (resetTags) {

            this.currentTagsList = new ArrayList<>();

            this.source = new TextField();
            this.source.getStyleClass().add("textDetail");
            this.source.setPromptText("Source");


            new AutoCompleteComboBox(tags, paneHandler.newTagsBox(), currentTagsList);

        }

        tags.isEditable();
        tags.setEditable(true);
        tags.setPromptText("Tags");

        ///Save Cancel
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        TextField finalSource = this.source;
        saveButton.setOnAction(e -> {
            resetTags = false;
            Entry newEntry = new Entry();
            newEntry.setTitle(titleField.getText());
            newEntry.setMajor(majorDetail.getText());
            newEntry.setMinor(minorDetail.getText());
            newEntry.setImportance(priorityNum.get());
            newEntry.setSource(finalSource.getText());
            newEntry.setTags(currentTagsList);
            switch (currentCardType) {
                case 1:
                    newEntry.setCardType("general");
                    break;
                case 2:
                    newEntry.setCardType("date");
                    break;
                case 3:
                    newEntry.setCardType("language");
                    break;
                case 4:
                    newEntry.setCardType("other");
                    break;
            }

            newEntry.save(cardSettings);
            Notifications.create()
                    .title("Idea Engine")
                    .text("Card Saved")
                    .hideAfter(Duration.seconds(1.75))
                    .darkStyle()
                    .show();
            /// Clear and refresh entry inputs
            switch (currentCardType) {
                case 1:
                    try {
                        borderPane.setCenter(createGeneralEntry());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case 2:
                    try {
                        borderPane.setCenter(createGeneralEntry());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case 3:
                    try {
                        borderPane.setCenter(createLanguageEntry());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case 4:
                    try {
                        borderPane.setCenter(createGeneralEntry());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
            }
        });
        saveButton.getStyleClass().add("saveExit");
        cancelButton.getStyleClass().add("saveExit");

        /// import button
        Button importButton = new Button("Import from csv");
        importButton.getStyleClass().add("import");
        importButton.setOnAction(e -> {
            File imported = getFile();
        });

        HBox saveExit = new HBox();
        saveExit.setSpacing(6);
        saveExit.setAlignment(Pos.CENTER);
        saveExit.setPadding(insetLeft);
        saveExit.getChildren().addAll(saveButton, cancelButton);

        HBox paddingBox = new HBox();
        paddingBox.setPrefHeight(5000);

        HBox importBox = new HBox();
        importBox.setPadding(insetLeft);
        importBox.getChildren().addAll(importButton);
        importBox.setAlignment(Pos.CENTER);

        if (currentCardType == 1) {
            rightPane.getChildren().addAll(this.source);
        }


        if (resetTags) {
            rightPane.getStyleClass().add("tags");
        }

        HBox spacer = new HBox();
        spacer.setPrefHeight(10);
        spacer.setMinHeight(10);

        rightPane.getChildren().addAll(spacer, tags, paneHandler.tagsBox, paddingBox);
        rightPane.getChildren().addAll(saveExit, importBox);

        rightPane.setPrefWidth(300);

        return rightPane;
    }


    /// Stacks TAB
    public static VBox studyPane() throws IOException {

        tableAndStats.resetNumber(); /// resets number incase clicked out while studying.

        BorderPane borderPane = paneHandler.borderPane;

        ///Study toggle

        HBox toggleBox = new HBox();
        toggleBox.setAlignment(Pos.CENTER_RIGHT);
        toggleBox.getStyleClass().add("pageNodes");

        Text deckSize = new Text("Study Length:");
        deckSize.getStyleClass().add("deckSize");


        /// Smart Study 20/50/100; unlocks at 0/200/1000

        Button smart = new Button("Quick");
        Button button20 = new Button("Study 20");
        Button button50 = new Button();
        Button button100 = new Button();

        smart.setPrefSize(150, 50);
        button20.setPrefSize(150, 50);
        button50.setPrefSize(150, 50);
        button100.setPrefSize(150, 50);

        smart.setOnAction(e -> {
            Stacks newStacks = new Stacks();
            newStacks.smartStackAll();
            borderPane.setCenter(newStacks.cardPane);
        });

        button20.setOnAction(e -> {
            Stacks newStacks = new Stacks();
            newStacks.numberedStack(20);
            borderPane.setCenter(newStacks.cardPane);
        });

        smart.getStyleClass().add("smartStudyButtons");
        button20.getStyleClass().add("smartStudyButtons");
        button50.getStyleClass().add("smartStudyButtons");
        button100.getStyleClass().add("smartStudyButtons");

        HBox quickStacks = new HBox();
        quickStacks.setPadding(new Insets(5, 5, 5, 5));

        quickStacks.getChildren().addAll(smart, button20, button50, button100);

        VBox spacer = new VBox();

        toggleBox.getChildren().addAll(quickStacks, spacer, deckSize);
        toggleBox.setHgrow(spacer, Priority.ALWAYS);


        /// Recent tags & All tags
        VBox recentAllStacks = new VBox();

        Text tagsText = new Text("Tags");
        tagsText.getStyleClass().add("stackType");

        RadioButton rb1 = new RadioButton("Recent");
        RadioButton rb2 = new RadioButton("All");

        rb1.getStyleClass().remove("radio-button");
        rb2.getStyleClass().remove("radio-button");
        rb1.getStyleClass().add("toggle-button");
        rb2.getStyleClass().add("toggle-button");
        rb1.getStyleClass().add("recentAll");
        rb2.getStyleClass().add("recentAll");

        rb1.selectedProperty().set(true);

        ToggleGroup toggleGroup = new ToggleGroup();

        rb1.setToggleGroup(toggleGroup);
        rb2.setToggleGroup(toggleGroup);


        SegmentedButton seg = new SegmentedButton();
        seg.getButtons().addAll(rb1, rb2);
        VBox spacer2 = new VBox();
        VBox padding2 = new VBox();

        Button backButton = new Button();
        backButton.getStyleClass().add("subTagBackButton");

        backButton.setVisible(false);

        HBox titleAndButtonBoxTags = new HBox();
        titleAndButtonBoxTags.getStyleClass().add("titleAndButtonBox");
        titleAndButtonBoxTags.setHgrow(spacer2, Priority.ALWAYS);
        titleAndButtonBoxTags.getChildren().addAll(tagsText, backButton, spacer2, seg);

        /// Recents
        HBox tagScrollPaneContainer = new HBox();

        HBox recents = new HBox();
        recents.setPrefHeight(miniCardHeight + 50);
        String[] recentTagList = cardSettings.recentTagList.toArray(new String[0]);
        for (String tagName : recentTagList) {
            miniStack(recents, tagName, borderPane, tagScrollPaneContainer, backButton);
        }
        ScrollPane recentScrollPane = new ScrollPane();
        recentScrollPane.setPrefHeight(miniCardHeight + 55);
        recentScrollPane.setContent(recents);
        recentScrollPane.setFitToHeight(true);
        recentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        recents.getStyleClass().add("recentAllScroll");
        recentScrollPane.getStyleClass().add("recentAllScroll");

        /// All tags
        HBox all = new HBox();
        all.setPrefHeight(miniCardHeight + 50);
        String[] allTagList = cardSettings.getMainTags();
        for (String tagName : allTagList) {
            miniStack(all, tagName, borderPane, tagScrollPaneContainer, backButton);
        }

        deckTagHandler.saveTagTable();

        ScrollPane allScrollPane = new ScrollPane();
        allScrollPane.setContent(all);
        allScrollPane.setPrefHeight(miniCardHeight + 55);
        allScrollPane.setFitToHeight(true);
        allScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        all.getStyleClass().add("recentAllScroll");
        allScrollPane.getStyleClass().add("recentAllScroll");

        recentAllStacks.getStyleClass().add("stackNodes");

        tagScrollPaneContainer.getChildren().add(recentScrollPane);
        recentAllStacks.getChildren().addAll(titleAndButtonBoxTags, tagScrollPaneContainer);

        /// button toggles between recent and all decks
        AtomicReference<Boolean> recentBool = new AtomicReference<>(true);
        rb1.setOnAction(e -> {
            tagScrollPaneContainer.getChildren().clear();
            tagScrollPaneContainer.getChildren().add(recentScrollPane);
            recentBool.set(true);
        });
        rb2.setOnAction(e -> {
            tagScrollPaneContainer.getChildren().clear();
            tagScrollPaneContainer.getChildren().add(allScrollPane);
            recentBool.set(false);
        });

        backButton.setOnAction(e -> {
            ///makes button invisible again
            backButton.setVisible(false);
            /// go back to recent or all tags
            if (recentBool.get() == true) {
                tagScrollPaneContainer.getChildren().clear();
                tagScrollPaneContainer.getChildren().add(recentScrollPane);
            } else {
                tagScrollPaneContainer.getChildren().clear();
                tagScrollPaneContainer.getChildren().add(allScrollPane);
            }
        });

        recentScrollPane.setOnScroll(event -> {
            if (event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                recentScrollPane.setHvalue(recentScrollPane.getHvalue() - event.getDeltaY() / 150);
            }
        });

        allScrollPane.setOnScroll(event -> {
            if (event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                allScrollPane.setHvalue(allScrollPane.getHvalue() - event.getDeltaY() / 150);
            }
        });


        /// Recent DECK and ALL DECKS
        VBox recentAllDecks = new VBox();

        Text decksText = new Text("Decks");
        decksText.getStyleClass().add("stackType");

        HBox titleAndButtonBox = new HBox();

        titleAndButtonBox.setAlignment(Pos.CENTER_LEFT);

        RadioButton recentDecksrb1 = new RadioButton("Recent");
        RadioButton recentDecksrb2 = new RadioButton("All");
        Button addButton = new Button("+");
        addButton.setPrefSize(10, 10);
        addButton.getStyleClass().add("plusButton");

        recentDecksrb1.getStyleClass().remove("radio-button");
        recentDecksrb2.getStyleClass().remove("radio-button");
        recentDecksrb1.getStyleClass().add("toggle-button");
        recentDecksrb2.getStyleClass().add("toggle-button");
        recentDecksrb1.getStyleClass().add("recentAll");
        recentDecksrb2.getStyleClass().add("recentAll");

        recentDecksrb1.selectedProperty().set(true);

        ToggleGroup recentDecksToggleGroup = new ToggleGroup();

        recentDecksrb1.setToggleGroup(recentDecksToggleGroup);
        recentDecksrb2.setToggleGroup(recentDecksToggleGroup);


        SegmentedButton recentDecksSeg = new SegmentedButton();
        recentDecksSeg.getButtons().addAll(recentDecksrb1, recentDecksrb2);
        VBox spacer1 = new VBox();
        VBox padding1 = new VBox();
        padding1.setMinWidth(10);

        titleAndButtonBox.setHgrow(spacer1, Priority.ALWAYS);
        titleAndButtonBox.getChildren().addAll(decksText, padding1, addButton, spacer1, recentDecksSeg);
        titleAndButtonBox.getStyleClass().add("titleAndButtonBox");

        /// Recents
        HBox recentDecksHBox = new HBox();
        recentDecksHBox.setPrefHeight(miniDeckHeight + 50);
        Table deckTable = deckTagHandler.deckTable;

        for (String deckName : deckTable.stringColumn("DeckName")) {
            if (!deckName.equals("FORMATTER")) {
                deckStack(recentDecksHBox, deckName, borderPane);
            }
        }
        ScrollPane recentDeckScrollPane = new ScrollPane();
        recentDeckScrollPane.setContent(recentDecksHBox);
        recentDeckScrollPane.setFitToHeight(true);
        recentDeckScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        recentDecksHBox.getStyleClass().add("recentAllScroll");
        recentDeckScrollPane.getStyleClass().add("recentAllScroll");


        /// All Decks
        HBox allDecks = new HBox();
        allDecks.setPrefHeight(miniDeckHeight + 50);
        String[] allDecksTagList = cardSettings.recentDeckList.toArray(new String[0]);
        for (String tagName : allDecksTagList) {
            deckStack(allDecks, tagName, borderPane);
        }

        ScrollPane allDecksScrollPane = new ScrollPane();
        allDecksScrollPane.setContent(allDecks);
        allDecksScrollPane.setFitToHeight(true);
        allDecksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        allDecks.getStyleClass().add("recentAllScroll");
        allDecksScrollPane.getStyleClass().add("recentAllScroll");

        recentAllDecks.getStyleClass().add("stackNodes");
        recentAllDecks.getChildren().addAll(titleAndButtonBox, recentDeckScrollPane);

        /// allows horizontal scrolling
        recentDeckScrollPane.setOnScroll(event -> {
            if (event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                recentDeckScrollPane.setHvalue(recentDeckScrollPane.getHvalue() - event.getDeltaY() / 150);
            }
        });

        allDecksScrollPane.setOnScroll(event -> {
            if (event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                allDecksScrollPane.setHvalue(allDecksScrollPane.getHvalue() - event.getDeltaY() / 150);
            }
        });

        /// button toggles between recent and all decks
        recentDecksrb1.setOnAction(e -> {
            recentAllDecks.getChildren().remove(allDecksScrollPane);
            recentAllDecks.getChildren().add(recentDeckScrollPane);
        });
        recentDecksrb2.setOnAction(e -> {
            recentAllDecks.getChildren().remove(recentDeckScrollPane);
            recentAllDecks.getChildren().add(allDecksScrollPane);
        });

        addButton.setOnAction(e -> {
            paneHandler.borderPane.setCenter(editDeck(null));
        });


        /// Advanced Search
        FlowPane tagsBox = new FlowPane();
        List<String> currentTagsList = new ArrayList<>();

        String[] tagsList = Files.readAllLines(Paths.get("resources/backendInfo.txt")).get(2).split(",");
        ComboBox tags = new ComboBox(FXCollections.observableArrayList(tagsList));
        new AutoCompleteComboBox(tags, tagsBox, currentTagsList);

        tags.isEditable();
        tags.setEditable(true);
        tags.setPromptText("Tags");
        tags.getStyleClass().add("tagPickerCombo");

        tagsBox.getChildren().addAll();
        tagsBox.getStyleClass().add("tagPickerTags");

        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("newEntryButton");
        submitButton.setOnAction(e -> {
            Stacks newStacks = new Stacks();
            newStacks.taggedStack("Economics");
            borderPane.setCenter(newStacks.cardPane);
        });


        VBox menu = new VBox();
        menu.setAlignment(Pos.TOP_CENTER);
        menu.getChildren().addAll(recentAllDecks, recentAllStacks);
        menu.getStyleClass().add("tagPicker");
        return (menu);
    }

    /// Create Mini Stacks
    public static void miniStack(HBox hbox, String tagName, BorderPane borderPane, HBox tagScrollPaneContainer, Button backButton) {

        StackPane pane = new StackPane();
        pane.setPrefSize(115, 140);
        pane.getStyleClass().add("miniStackPane");

        int cardShift = 4;

        String cardText;
        if (tagName.contains("(") && tagName.contains(")")) {
            cardText = StringUtils.substringBetween(tagName, "(", ")");
        } else {
            cardText = tagName;
        }

        Text text = new Text(cardText);
        text.setX(0);
        text.setTranslateY(0);
        text.getStyleClass().add("miniStackText");
        text.setWrappingWidth(miniCardWidth - 8);

        Rectangle front = new javafx.scene.shape.Rectangle();
        front.setTranslateX(0);
        front.setTranslateY(0);
        front.setHeight(miniCardHeight);
        front.setWidth(miniCardWidth);
        front.setArcWidth(miniStackRadius);
        front.setArcHeight(miniStackRadius);
        front.getStyleClass().add("miniStackFront");

        Rectangle middle = new javafx.scene.shape.Rectangle();
        middle.setTranslateX(cardShift);
        middle.setTranslateY(cardShift);
        middle.setHeight(miniCardHeight);
        middle.setWidth(miniCardWidth);
        middle.setArcWidth(miniStackRadius);
        middle.setArcHeight(miniStackRadius);
        middle.getStyleClass().add("miniStackMiddle");

        Rectangle bottom = new javafx.scene.shape.Rectangle();
        bottom.setTranslateX(cardShift * 2);
        bottom.setTranslateY(cardShift * 2);
        bottom.setHeight(miniCardHeight);
        bottom.setWidth(miniCardWidth);
        bottom.setArcWidth(miniStackRadius);
        bottom.setArcHeight(miniStackRadius);
        bottom.getStyleClass().add("miniStackBottom");

        /// Stack Button and animation
        Button button = new Button();
        button.setPrefSize(miniCardWidth, miniCardHeight);

        double tagRating;

        /// Check if bring to study or other stacks
        if (cardSettings.checkIfSubTags(tagName)) {

            ///calculate tagrating

            tagRating = tableAndStats.getFullTagRating(tagName);


            button.setOnAction(e -> {
                backButton.setVisible(true);
                backButton.setText("/" + tagName);

                HBox subTagBox = new HBox();
                subTagBox.setPrefHeight(miniCardHeight + 50);
                String[] subTagList = cardSettings.getSubTags(tagName);

                for (String name : subTagList) {

                    miniStack(subTagBox, name, borderPane, null, null);

                }

                deckTagHandler.saveTagTable();

                ScrollPane subTagScrollPane = new ScrollPane();
                subTagScrollPane.setContent(subTagBox);
                subTagScrollPane.setFitToHeight(true);
                subTagScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                subTagBox.getStyleClass().add("recentAllScroll");
                subTagScrollPane.getStyleClass().add("recentAllScroll");


                tagScrollPaneContainer.getChildren().clear();
                tagScrollPaneContainer.getChildren().add(subTagScrollPane);

                /// allows horizontal scrolling
                subTagScrollPane.setOnScroll(event -> {
                    if (event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                        subTagScrollPane.setHvalue(subTagScrollPane.getHvalue() - event.getDeltaY() / 150);
                    }
                });
            });

        } else {
            tagRating = tableAndStats.getTagRating(tagName);
            button.setOnAction(e -> {
                Stacks stack = new Stacks();
                stack.studyStyle = paneHandler.studyStyle;
                stack.taggedStack(tagName);
                borderPane.setCenter(stack.cardPane);
            });
            deckTagHandler.specialTagHistory(tagName, tagRating);

        }
        /// SubTag Pane


        /// Deck rating popup
        int barHeight = 5;

        /// deck rating text

        Rectangle progressTotal = new Rectangle();
        Rectangle currentProgress = new Rectangle();

        progressTotal.setHeight(barHeight);
        progressTotal.setTranslateY(miniCardHeight / 3 + barHeight);
        progressTotal.setArcWidth(10);
        progressTotal.setArcHeight(10);

        currentProgress.setHeight(barHeight);
        currentProgress.setTranslateY(miniCardHeight / 3 + barHeight);
        currentProgress.setTranslateX(miniCardWidth * (-.4 + .8 * tagRating / 20));
        currentProgress.setArcWidth(10);
        currentProgress.setArcHeight(10);

        progressTotal.setWidth(miniCardWidth * .8);
        currentProgress.setWidth(miniCardWidth * .8 * tagRating / 10);

        progressTotal.getStyleClass().add("progressBox");
        currentProgress.getStyleClass().add("progressBar");

        currentProgress.setDisable(true);
        progressTotal.setDisable(true);

        button.setStyle(String.format("-fx-background-radius: %s;", miniStackRadius));
        button.setOnMouseEntered(e -> hoverOverStack(middle, bottom, cardShift, null, null, null, null, false, false));
        button.setOnMouseExited(e -> hoverOutStack(middle, bottom, cardShift, null, null, null, null, false, false));

        pane.getChildren().addAll(bottom, middle, front, text, button, progressTotal, currentProgress);
        hbox.getChildren().addAll(pane);
    }

    public static void deckStack(HBox hbox, String deckName, BorderPane borderPane) {

        StackPane pane = new StackPane();
        pane.getStyleClass().add("deckStackPane");

        int cardShift = 5;

        Text text = new Text(deckName);
        text.setX(0);
        text.setTranslateY(0);
        text.setWrappingWidth(miniDeckWidth - 8);

        String imgPath = deckTagHandler.getBackgroundPath(deckName);

        ImageView imageView = null;
        if (imgPath != "") {
            imageView = createImage(imgPath);
            text.getStyleClass().add("deckStackTextWithShadow");
        } else {
            text.getStyleClass().add("deckStackText");
        }

        Rectangle front = new javafx.scene.shape.Rectangle();
        front.setTranslateX(0);
        front.setTranslateY(0);
        front.setHeight(miniDeckHeight);
        front.setWidth(miniDeckWidth);
        front.setArcWidth(miniDeckRadius);
        front.setArcHeight(miniDeckRadius);
        front.getStyleClass().add("miniStackFront");


        Rectangle middle = new javafx.scene.shape.Rectangle();
        middle.setTranslateX(cardShift);
        middle.setTranslateY(cardShift);
        middle.setHeight(miniDeckHeight);
        middle.setWidth(miniDeckWidth);
        middle.setArcWidth(miniDeckRadius);
        middle.setArcHeight(miniDeckRadius);
        middle.getStyleClass().add("miniStackMiddle");

        Rectangle bottom = new javafx.scene.shape.Rectangle();
        bottom.setTranslateX(cardShift * 2);
        bottom.setTranslateY(cardShift * 2);
        bottom.setHeight(miniDeckHeight);
        bottom.setWidth(miniDeckWidth);
        bottom.setArcWidth(miniDeckRadius);
        bottom.setArcHeight(miniDeckRadius);
        bottom.getStyleClass().add("miniStackBottom");

        /// Stack Button and animation
        Button button = new Button();
        button.setPrefSize(miniDeckWidth, miniDeckHeight);
        button.setOnAction(e -> {
            Stacks stack = new Stacks();
            stack.studyStyle = paneHandler.studyStyle;
            stack.deckSelection(deckName);
            borderPane.setCenter(stack.cardPane);
        });

        int popupHeight = 20;

        /// Deck rating popupbox
        Rectangle ratingPopupBox = new Rectangle();
        ratingPopupBox.setScaleY(0);
        ratingPopupBox.getStyleClass().add("popupTextBox");
        ratingPopupBox.setTranslateY((miniDeckWidth / 2) + popupHeight / 4);

        ratingPopupBox.setArcWidth(20);
        ratingPopupBox.setArcHeight(20);
        ratingPopupBox.setHeight(popupHeight);
        ratingPopupBox.setWidth(40);

        /// deck rating text
        Text ratingPopup = new Text(deckTagHandler.getDeckRating(deckName));
        ratingPopup.setScaleY(0);
        ratingPopup.getStyleClass().add("popupText");
        ratingPopup.setTranslateY((miniDeckWidth / 2) + popupHeight / 4);

        /// notify whether or not to study deck

        int circleRadius = 14;

        int notificationTranslateY = -miniDeckHeight / 2;
        int notificationTranslateX = miniDeckWidth / 2;

        Circle studyNotificationBox = new Circle();
        int notificationNumber = tableAndStats.getDeckNotificationNumber(deckTagHandler.getIncludeList(deckName), deckTagHandler.getExcludeList(deckName));
        studyNotificationBox.setScaleX(0);
        studyNotificationBox.getStyleClass().add("notificationTextBox");
        studyNotificationBox.setTranslateY(notificationTranslateY);
        studyNotificationBox.setTranslateX(notificationTranslateX);

        studyNotificationBox.setRadius(circleRadius);

        /// popup to show number
        Text notificationPopup = new Text(Integer.toString(notificationNumber));
        notificationPopup.setScaleX(0);
        notificationPopup.getStyleClass().add("notificationText");
        notificationPopup.setTranslateY(notificationTranslateY);
        notificationPopup.setTranslateX(notificationTranslateX);

        Text notifyMark;
        Boolean notifyStudy;

        if (notificationNumber == 0) {
            notifyStudy = false;
            notifyMark = new Text("");
        } else {
            notifyStudy = true;
            notifyMark = new Text("!");
        }
        notifyMark.setTranslateY(notificationTranslateY + 1);
        notifyMark.setTranslateX(notificationTranslateX);
        notifyMark.getStyleClass().add("notifyMark");


        button.setStyle(String.format("-fx-background-radius: %s;", miniDeckRadius / 2));
        button.setOnMouseEntered(e -> hoverOverStack(middle, bottom, cardShift, ratingPopupBox, ratingPopup, studyNotificationBox, notificationPopup, true, notifyStudy));
        button.setOnMouseExited(e -> hoverOutStack(middle, bottom, cardShift, ratingPopupBox, ratingPopup, studyNotificationBox, notificationPopup, true, notifyStudy));

        Button editButton = new Button();
        editButton.getStyleClass().add("editButton");
        ///BoxBlur blur = new BoxBlur();
        ///blur.setWidth(1);
        ///blur.setHeight(1);
        GaussianBlur blur = new GaussianBlur();
        blur.setRadius(1.5);

        editButton.setEffect(blur);
        int buttonSize = 32;
        editButton.setPrefSize(buttonSize, buttonSize);
        editButton.setTranslateX(-((miniDeckWidth / 2) - buttonSize / 2 - 5));
        editButton.setTranslateY(((miniDeckHeight / 2) - buttonSize / 2) - 5);

        editButton.setOnAction(e -> paneHandler.borderPane.setCenter(editDeck(deckName)));

        ///Makes it so you dont hover over element instead of button
        text.setDisable(true);
        ratingPopupBox.setDisable(true);
        ratingPopup.setDisable(true);
        notificationPopup.setDisable(true);
        notifyMark.setDisable(true);
        studyNotificationBox.setDisable(true);

        if (imgPath != "") {
            pane.getChildren().addAll(bottom, middle, imageView, button, text, editButton, ratingPopupBox, ratingPopup, notifyMark, studyNotificationBox, notificationPopup);
        } else {
            pane.getChildren().addAll(bottom, middle, front, text, button, editButton, ratingPopupBox, ratingPopup, notifyMark, studyNotificationBox, notificationPopup);
        }
        hbox.getChildren().addAll(pane);
    }


    /// Stack Animations
    public static void hoverOutStack(Rectangle middle, Rectangle bottom, int cardShift, Rectangle popupBox, Text popup, Circle notifBox, Text notifNum, boolean pop, boolean studyNotif) {

        Duration duration = Duration.millis(200);

        ScaleTransition st = new ScaleTransition(duration, popupBox);
        st.setFromY(1);
        st.setToY(0);

        ScaleTransition st2 = new ScaleTransition(duration, popup);
        st2.setFromY(1);
        st2.setToY(0);

        ScaleTransition st3 = new ScaleTransition(duration, notifBox);
        st3.setFromX(1);
        st3.setToX(0);

        ScaleTransition st4 = new ScaleTransition(duration, notifNum);
        st4.setFromX(1);
        st4.setToX(0);

        TranslateTransition ttMiddle = new TranslateTransition(duration, middle);
        ttMiddle.setFromX(cardShift * 1.5);
        ttMiddle.setFromY(cardShift * 1.5);
        ttMiddle.setToX(cardShift);
        ttMiddle.setToY(cardShift);

        TranslateTransition ttBottom = new TranslateTransition(duration, bottom);
        ttBottom.setFromX(cardShift * 3);
        ttBottom.setFromY(cardShift * 3);
        ttBottom.setToX(cardShift * 2);
        ttBottom.setToY(cardShift * 2);


        if (pop) {
            st.play();
            st2.play();
        }

        if (studyNotif) {
            st3.play();
            st4.play();
        }

        ttBottom.play();
        ttMiddle.play();
    }

    public static void hoverOverStack(Rectangle middle, Rectangle bottom, int cardShift, Rectangle popupBox, Text popup, Circle notifBox, Text notifNum, boolean pop, boolean studyNotif) {

        Duration duration = Duration.millis(200);

        ScaleTransition st = new ScaleTransition(duration, popupBox);
        st.setFromY(0);
        st.setToY(1);

        ScaleTransition st2 = new ScaleTransition(duration, popup);
        st2.setFromY(0);
        st2.setToY(1);

        ScaleTransition st3 = new ScaleTransition(duration, notifBox);
        st3.setFromX(0);
        st3.setToX(1);

        ScaleTransition st4 = new ScaleTransition(duration, notifNum);
        st4.setFromX(0);
        st4.setToX(1);

        TranslateTransition ttMiddle = new TranslateTransition(duration, middle);
        ttMiddle.setFromX(cardShift);
        ttMiddle.setFromY(cardShift);
        ttMiddle.setToX(cardShift * 1.5);
        ttMiddle.setToY(cardShift * 1.5);

        TranslateTransition ttBottom = new TranslateTransition(duration, bottom);
        ttBottom.setFromX(cardShift * 2);
        ttBottom.setFromY(cardShift * 2);
        ttBottom.setToX(cardShift * 3);
        ttBottom.setToY(cardShift * 3);


        if (pop) {
            st.play();
            st2.play();
        }

        if (studyNotif) {
            st3.play();
            st4.play();
        }

        ttBottom.play();
        ttMiddle.play();
    }

    /// ComboBox Auto Complete
    public static class AutoCompleteComboBox<T> implements EventHandler<KeyEvent> {

        private ComboBox comboBox;
        private final FlowPane tagsBox;
        private List list;
        private final ObservableList<T> data;
        private FlowPane pane;
        private boolean moveCaretToPos = false;
        private int caretPos;

        public AutoCompleteComboBox(final ComboBox comboBox, FlowPane tagsBox, List list) {
            this.comboBox = comboBox;
            this.tagsBox = tagsBox;
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
            this.comboBox.setOnAction(e -> comboBoxAddTag(tagsBox, list, comboBox));
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
                comboBoxAddTag(tagsBox, list, comboBox);
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

        private Button createAddButton(ComboBox comboBox, FlowPane pane, List<String> list) {
            Button addTagButton = new Button("+");
            this.comboBox = comboBox;
            this.list = list;
            this.pane = pane;
            comboBoxAddTag(pane, list, comboBox);
            addTagButton.setOnAction(e -> {
                moveCaretToPos = true;
                caretPos = comboBox.getEditor().getCaretPosition();
            });

            return addTagButton;

        }

    }

    private File getFile() {
        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showOpenDialog(null);

        LookAndFeel previousLF = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(previousLF);
        } catch (UnsupportedLookAndFeelException e) {
        }

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }

        return file;
    }

    public static HBox editDeck(String deckName) {

        /// initiators
        List<String> includeList = deckTagHandler.getIncludeList(deckName);
        List<String> excludeList = deckTagHandler.getExcludeList(deckName);

        HBox createDeckBox = new HBox();
        createDeckBox.setAlignment(Pos.CENTER);
        createDeckBox.getStyleClass().add("deckCardEditor");

        String imgPath = null;
        if (deckName != null && !deckName.equals("")) {
            imgPath = deckTagHandler.getBackgroundPath(deckName);
        }

        boolean editDeck = true;
        if (deckName == null || deckName.equals("")) {
            editDeck = false;
        }

        /// create layout
        VBox inputBox = new VBox();
        inputBox.setAlignment(Pos.CENTER);


        ///DeckName
        VBox deckInputBox = new VBox();
        Text deckInputText = new Text("Deck Name:");
        TextField deckInput = new TextField();
        if (editDeck) {
            deckInput.setText(deckName);
        }

        deckInputBox.setPrefWidth(500);
        deckInputBox.getStyleClass().add("separationBox");
        deckInputText.getStyleClass().add("yellowTextMedium");
        deckInput.getStyleClass().add("textTitle");

        deckInputBox.getChildren().addAll(deckInputText, deckInput);

        ///Background picture
        VBox pictureInputBox = new VBox();
        HBox urlButtonContainer = new HBox();
        Text pictureInputText = new Text("Image File:");
        TextField urlFileChooser = new TextField(imgPath);

        if (imgPath != null && !imgPath.equals("")) {
            urlFileChooser.setText(imgPath);
        }

        pictureInputBox.setPrefWidth(500);
        pictureInputBox.getStyleClass().add("separationBox");
        pictureInputText.getStyleClass().add("yellowTextMedium");
        urlFileChooser.getStyleClass().add("textDetail");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload File Path");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("IMAGE FILES", "*.jpg", "*.png", "*.gif")
        );

        Button addPictureButton = new Button("+");
        addPictureButton.getStyleClass().add("plusButton");
        addPictureButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(paneHandler.borderPane.getScene().getWindow());
            urlFileChooser.setText(file.getPath());
            paneHandler.setPreviewer(createImage(file.getPath()));
            pictureInputBox.getChildren().clear();

            HBox previewContainer = new HBox();
            previewContainer.getStyleClass().add("inputBox");
            previewContainer.getChildren().add(paneHandler.getPreviewer());
            pictureInputBox.getChildren().addAll(pictureInputText, urlButtonContainer, previewContainer);
        });

        if (imgPath != "" && imgPath != null) {
            paneHandler.setPreviewer(createImage(imgPath));
        }

        urlButtonContainer.getChildren().addAll(urlFileChooser, addPictureButton);
        urlButtonContainer.setAlignment(Pos.CENTER_LEFT);
        if (paneHandler.getPreviewer() != null) {
            HBox previewContainer = new HBox();
            previewContainer.getStyleClass().add("inputBox");
            previewContainer.getChildren().add(paneHandler.getPreviewer());
            pictureInputBox.getChildren().addAll(pictureInputText, urlButtonContainer, previewContainer);
        } else {
            pictureInputBox.getChildren().addAll(pictureInputText, urlButtonContainer);
        }


        ///create tagPicker
        VBox tagPicker = new VBox();

        List<String> tagsList = cardSettings.tagList;
        for (String tag : tagsList) {
            HBox line = new HBox();

            RadioButton st1 = new RadioButton("exclude");
            RadioButton st2 = new RadioButton("none");
            RadioButton st3 = new RadioButton("include");
            st1.setMinWidth(70);
            st2.setMinWidth(70);
            st3.setMinWidth(70);

            String spTag = "<" + tag + ">";
            if (includeList.contains(spTag)) {
                st3.setSelected(true);
            } else if (excludeList.contains(spTag)) {
                st1.setSelected(true);
            } else {
                st2.setSelected(true);
            }
            ToggleGroup toggleGroup1 = new ToggleGroup();

            st1.setToggleGroup(toggleGroup1);
            st2.setToggleGroup(toggleGroup1);
            st3.setToggleGroup(toggleGroup1);

            st1.getStyleClass().remove("radio-button");
            st2.getStyleClass().remove("radio-button");
            st3.getStyleClass().remove("radio-button");
            st1.getStyleClass().add("toggle-button");
            st2.getStyleClass().add("toggle-button");
            st3.getStyleClass().add("toggle-button");
            st1.getStyleClass().add("inclusionToggle");
            st2.getStyleClass().add("inclusionToggle");
            st3.getStyleClass().add("inclusionToggle");

            ///add to exclude list, remove from include list
            st1.setOnAction(e -> {
                excludeList.add(spTag);
                includeList.remove(spTag);
            });

            ///remove from all lists
            st2.setOnAction(e -> {
                excludeList.remove(spTag);
                includeList.remove(spTag);
            });

            ///add to include list, remove from exclude list
            st3.setOnAction(e -> {
                includeList.add(spTag);
                excludeList.remove(spTag);
            });


            SegmentedButton seg = new SegmentedButton();
            seg.getButtons().addAll(st1, st2, st3);
            Text tagNameText = new Text(tag);
            tagNameText.setTextAlignment(TextAlignment.LEFT);
            tagNameText.getStyleClass().add("toggleName");
            VBox spacer = new VBox();
            VBox spacer2 = new VBox();
            VBox spacer3 = new VBox();
            spacer2.setMinWidth(20);
            spacer3.setMinWidth(30);
            line.setHgrow(spacer, Priority.ALWAYS);


            line.getChildren().addAll(tagNameText, spacer, spacer2, seg, spacer3);


            line.setAlignment(Pos.CENTER_RIGHT);
            tagPicker.getChildren().add(line);
        }

        ScrollPane tagsScrollPane = new ScrollPane();
        tagsScrollPane.setContent(tagPicker);
        tagsScrollPane.setFitToWidth(true);
        tagsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        tagPicker.getStyleClass().add("inclusionToggleScroll");
        tagsScrollPane.getStyleClass().add("inclusionToggleScroll");


        VBox scrollHolder = new VBox();
        scrollHolder.getStyleClass().add("scrollNodes");
        scrollHolder.getChildren().add(tagsScrollPane);

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        boolean finalEditDeck = editDeck;
        saveButton.setOnAction(e -> {
            if (finalEditDeck) {
                deckTagHandler.editDeckTable(deckName, deckInput.getText(), includeList, excludeList, urlFileChooser.getText());
                cardSettings.updateNamesInDeckList(deckName, deckTagHandler.getNewName());

            } else {
                deckTagHandler.newDeckTable(deckInput.getText(), includeList, excludeList, urlFileChooser.getText(), 0.00);
            }
            deckTagHandler.saveDeckTable();
            try {
                paneHandler.borderPane.setCenter(studyPane());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        cancelButton.setOnAction(e -> {
            try {
                paneHandler.borderPane.setCenter(studyPane());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        ///Create Layout

        inputBox.getChildren().addAll(deckInputBox, pictureInputBox, saveButton, cancelButton);
        inputBox.getStyleClass().add("inputBox");

        createDeckBox.getChildren().addAll(inputBox, scrollHolder);

        createDeckBox.setSpacing(5);

        return createDeckBox;
    }

    public static ImageView createImage(String url) {
        InputStream stream = null;
        try {
            stream = Files.newInputStream(Paths.get(url));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Image image = new Image(stream);
        ImageView imgView = new ImageView(image);
        imgView.setFitHeight(miniDeckHeight);
        imgView.setFitWidth(miniDeckWidth);

        Rectangle clip = new Rectangle(
                imgView.getFitWidth(), imgView.getFitHeight()
        );
        clip.setArcWidth(miniDeckRadius);
        clip.setArcHeight(miniDeckRadius);
        imgView.setClip(clip);

        // snapshot the rounded image.
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage image1 = imgView.snapshot(parameters, null);

        // remove the rounding clip so that our effect can show through.
        imgView.setClip(null);

        // apply a shadow effect.
        imgView.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.web("FFD166"), 2, .5, 0, 0));

        // store the rounded image in the imageView.
        imgView.setImage(image1);

        return imgView;
    }

    public void expandButton(VBox box) {
        box.setVisible(true);
        box.setManaged(true);
    }

    public void collapseAllOtherButtons(VBox box, VBox box2, VBox box3, VBox box4) {
        box.setVisible(false);
        box.setManaged(false);

        box2.setVisible(false);
        box2.setManaged(false);

        box3.setVisible(false);
        box3.setManaged(false);

        box4.setVisible(false);
        box4.setManaged(false);
    }

    public Table textToTable(String textData, String cardSeparator, String infoSeparator1, String infoSeparator2) {
        /// Assuming all separators are different

        List<String> info1List = new ArrayList();
        List<String> info2List = new ArrayList();
        List<String> info3List = new ArrayList();

        String splitted[] = textData.split(cardSeparator);

        for (String line : splitted) {
            String info1 = StringUtils.substringBefore(line, infoSeparator1).trim();
            String info2 = StringUtils.substringBetween(line, infoSeparator1, infoSeparator2).trim();
            String info3 = StringUtils.substringAfter(line, infoSeparator2).trim();

            info1List.add(info1);
            info2List.add(info2);
            info3List.add(info3);
        }

        Table dataTable =
                Table.create("Data Table")
                        .addColumns(
                                StringColumn.create("info1", info1List),
                                StringColumn.create("info2", info2List),
                                StringColumn.create("info3", info3List));

        return dataTable;

    }

    public void saveTableAsCards(Table table) {

    }

}


