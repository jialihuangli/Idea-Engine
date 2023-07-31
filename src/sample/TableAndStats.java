package sample;

import javafx.embed.swing.SwingNode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.ArrayUtils;
import org.knowm.xchart.*;
import org.knowm.xchart.style.BoxStyler;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.Destination;
import tech.tablesaw.io.json.JsonReadOptions;
import tech.tablesaw.io.json.JsonWriter;
import tech.tablesaw.selection.Selection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TableAndStats {

    /// Aggregate tables
    Table allCards;
    Table historyTable;

    /// derived tables
    Table studyingCards;
    Table studyingStack;
    Table remainingCards;
    Table remainingStack;
    Table unusedCards;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private static final DecimalFormat decimal2 = new DecimalFormat("0.00");

    public int cardNumber = 0;

    private static Color statColor = new Color(17, 138, 178);


    String cardPath = "resources/cards.json";
    String historyPath = "resources/history.json";


    public void tableAndStats() throws IOException {


        allCards = Table.read().usingOptions(JsonReadOptions.builder(cardPath));
        historyTable = Table.read().usingOptions(JsonReadOptions.builder(historyPath));
        checkForNew();


    }

    public void saveAllCardsAny(Boolean updateStudyNum) {

        if (updateStudyNum) {
            updateStudied(studyingCards, cardNumber + 1);
            if (allCards.columnNames().contains("sortColumn")) {
                allCards.removeColumns("SortColumn");
            }
        }

        allCards = allCards.dropWhere(allCards.intColumn("#").isGreaterThan(0)); ///make empty table to add cards, prevents double saving

        allCards = allCards.append(remainingCards);
        allCards = allCards.append(studyingCards);

        /// save
        JsonWriter jw = new JsonWriter();
        try {
            Destination dest = new Destination(new File(cardPath));
            jw.write(allCards, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void updateStudied(Table studying, int currentCard) {

        IntColumn amountStudiedColumn = studying.intColumn("AmountStudied");
        IntColumn studyDate = studying.intColumn("StudyDate");
        int studiedToday = (int) (new Date().getTime() / 1000);
        for (int i = 0; i < currentCard; i++) {
            amountStudiedColumn.set(i, amountStudiedColumn.getInt(i) + 1);
            studyDate.set(i, studiedToday);
        }
    }

    public void shuffle() {

        ///takes table, randomly orders

        ///Variables
        int variance = 75;
        int deviation = 75;
        double trainingWeight = 1.5;

        Table table = studyingStack;
        int todayInt = (int) (new Date().getTime() / 60000);  /// change to minutes
        int[] sortArray = new int[0];
        Random rand = new Random();

        ///randomizing alg
        if (Main.cardSettings.trainingOn) {
            for (int i = 0; i < table.rowCount(); i++) {
                sortArray = ArrayUtils.add(
                        sortArray,
                        (int) (((Math.round(Math.pow((todayInt - (Integer.parseInt(table.getString(i, "StudyDate")) / 60)), .5) + 5)) ///get sqrt(minutes) since last studied
                                * Integer.parseInt(table.getString(i, "Importance")) /// multiply by importance
                                / (Math.round(Math.pow(Integer.parseInt(table.getString(i, "AmountStudied")), .5)) + 1) /// divide by sqrt(times studied)
                                * (rand.nextInt(variance) * rand.nextInt(deviation) + 1) /// multiply by a random integer
                                * Math.pow(trainingWeight, 5 - Integer.parseInt(table.getString(i, "TrainingNumber"))) /// divided by training number.
                                * 100)
                        )

                );

            }
        } else {
            for (int i = 0; i < table.rowCount(); i++) {
                sortArray = ArrayUtils.add(
                        sortArray,
                        (int) ((((Math.round((todayInt - Integer.parseInt(table.getString(i, "StudyDate")) / 60)) + 10)) ///get sqrt(minutes) since last studied
                                * Integer.parseInt(table.getString(i, "Importance")) /// multiply by importance
                                / (Math.round(Math.sqrt(Integer.parseInt(table.getString(i, "AmountStudied")))) + 1) /// divide by sqrt(times studied)
                                * (rand.nextInt(variance) * rand.nextInt(deviation) + 1) /// multiply by a random integer
                                * 10)
                        )
                );
            }

        }

        IntColumn column = IntColumn.create("SortColumn", sortArray);
        table.replaceColumn("SortColumn", column);
        studyingCards = table.sortDescendingOn("SortColumn");

        ///check if you have fewer cards than trying to study
        int deckLimiter = Math.min(Main.cardSettings.getDeckSize(), studyingStack.rowCount());

        ///drops and saves cards not getting used, picks cards, adds unused to remaining cards
        unusedCards = studyingCards.dropRange(deckLimiter);
        studyingCards = studyingCards.first(deckLimiter);

        remainingCards = unusedCards.dropWhere(allCards.intColumn("#").isGreaterThan(0));
        remainingCards.append(unusedCards);
        remainingCards.append(remainingStack);

    }

    public void addSortColumn() {
        /// Create Sort List

        if (!allCards.columnNames().contains("SortColumn")) {
            int[] sortArray = new int[0];
            for (int i = 0; i < allCards.rowCount(); i++) {
                sortArray = ArrayUtils.add(
                        sortArray, 0
                );
            }
            IntColumn column = IntColumn.create("SortColumn", sortArray);
            allCards.addColumns(column);
        }
    }

    public void nextNumber() {
        this.cardNumber = cardNumber + 1;
    }

    public void previousNumber() {
        this.cardNumber = cardNumber - 1;
    }

    public void resetNumber() {
        this.cardNumber = 0;
    }

    public void addTimeAndCard(int time) {

        checkForNew();

        Date date = new Date();
        String dateString = formatter.format(date);

        Table today = historyTable.where(historyTable.stringColumn("date").isEqualTo(dateString));

        Table remainder = historyTable.where(historyTable.stringColumn("date").isNotEqualTo(dateString));

        IntColumn cardsStudied = today.intColumn("cardsStudied");
        cardsStudied.set(0, Integer.parseInt(today.getString(0, "cardsStudied")) + (cardNumber + 1)); ///removes random spaces

        IntColumn secondsStudied = today.intColumn("secondsStudied");
        secondsStudied.set(0, Integer.parseInt(today.getString(0, "secondsStudied")) + time); /// removes random spaces

        this.historyTable = today.append(remainder);

        saveHistory();

    }

    public int getTodayTime() {
        Date date = new Date();
        String dateString = formatter.format(date);
        int todayTime = Integer.parseInt(historyTable.where(historyTable.stringColumn("date").isEqualTo(dateString)).getString(0, "secondsStudied"));
        return todayTime;
    }

    public int getTodayCards() {
        Date date = new Date();
        String dateString = formatter.format(date);
        int todayTime = Integer.parseInt(historyTable.where(historyTable.stringColumn("date").isEqualTo(dateString)).getString(0, "cardsStudied"));
        return todayTime;
    }

    public void saveHistory() {
        JsonWriter jw = new JsonWriter();
        try {
            Destination dest = new Destination(new File(historyPath));
            jw.write(historyTable, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Main.paneHandler.updateTopPane(Main.cardSettings, Main.tableAndStats);
    }

    public void calculateOverDue() {  /// determines how many cards need to be studied

    }

    public void checkForNew() {
        Date date = new Date();
        String dateString = formatter.format(date);

        if (historyTable.rowCount() == 0) { /// if new table
            StringColumn c0 = StringColumn.create("date", dateString);
            IntColumn c1 = IntColumn.create("cardsStudied", 1);
            c1.append(0);
            IntColumn c2 = IntColumn.create("secondsStudied", 1);
            c2.append(0);

            Table newDeckInfo = Table.create(c0, c1, c2);

            historyTable = newDeckInfo;
        } else if (!historyTable.stringColumn("date").contains(dateString)) { /// if it doesn't contain date, add new
            StringColumn c0 = StringColumn.create("date", dateString);
            IntColumn c1 = IntColumn.create("cardsStudied", 1);
            c1.set(0, 0);
            IntColumn c2 = IntColumn.create("secondsStudied", 1);
            c2.set(0, 0);


            Table newDeckInfo = Table.create(c0, c1, c2);

            historyTable = historyTable.append(newDeckInfo);

        }
        saveHistory();
    }

    public double getTagRating(String tagName) {
        String tagNameSp = "<" + tagName + ">";
        double avgRating = allCards.where(allCards.stringColumn("Tags").containsString(tagNameSp)).intColumn("trainingNumber").mean();
        if (Double.isNaN(avgRating)) {
            avgRating = 0;
        } else {
            avgRating = Double.parseDouble(decimal2.format(avgRating));
        }
        return avgRating;
    }

    public double getFullTagRating(String tagName){

        ///checks for tag in format:   <tag name (sub name)>

        String tagNameSp = "<" + tagName + " (";

        double avgRating = allCards.where(allCards.stringColumn("Tags").containsString(tagNameSp)).intColumn("trainingNumber").mean();
        if (Double.isNaN(avgRating)) {
            avgRating = 0;
        } else {
            avgRating = Double.parseDouble(decimal2.format(avgRating));
        }

        return avgRating;
    }

    public SwingNode getTagRatingHistogram(String name) {

        Column list;

        String spName = "<" + name + ">";

        list = allCards.where(allCards.stringColumn("Tags").containsString(spName))
                .column("trainingNumber");

        List<Double> doubleList = new ArrayList(list.size());

        for (int i = 0; i < list.size(); ++i) {
            doubleList.add(Double.valueOf(list.get(i).toString()));
        }

        CategoryChart chart = new CategoryChartBuilder()
                .title("Ratings")
                .xAxisTitle("Rating")
                .yAxisTitle("Count")
                .build();
        Histogram histogram = new Histogram(doubleList, 11, -.5, 10.5);
        chart.addSeries("histogram", histogram.getxAxisData(), histogram.getyAxisData());

        chart.getStyler().setLegendVisible(false)
                .setSeriesColors(new Color[]{statColor})
                .setToolTipsEnabled(true);

        JPanel chartPanel = new XChartPanel<>(chart);
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        return swingNode;

/*      OLD CHART LIBRARY

        String locationName = "resources/charts/histogram.png";
        Plot plt = Plot.create();

        plt.hist()
                .add(doubleList)
                .bins(10)
                .stacked(true)
                .color("#66DD66");
        plt.xlim(0, 10);
        plt.title("Ratings");
        plt.savefig(locationName).dpi(100);
        try {
            plt.executeSilently();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PythonExecutionException e) {
            throw new RuntimeException(e);
        return getPng(locationName, 350);
        }*/
    }

    public SwingNode getDeckRatingHistogram(String deckName) {

        java.util.List<String> include = Main.deckTagHandler.getIncludeList(deckName);
        java.util.List<String> exclude = Main.deckTagHandler.getExcludeList(deckName);

        Selection selection = null;
        int i = 0;

        ///create a selection to include all included tags

        if (include.size() == 0) {
            AlertBox.display("Error", "No tags included in deck.");
        }
        while (i < include.size()) {
            String tagName = include.get(i);
            if (selection == null) { /// initiates table
                selection = allCards.stringColumn("Tags").containsString(tagName);
                i++;
            } else {
                selection = allCards.stringColumn("Tags").containsString(tagName).or(selection);
                i++;

            }
        }

        if (exclude.size() > 0) {
            int j = 0;
            ///create a selection to exclude all excluded tags
            while (j < exclude.size()) {
                if (!exclude.get(j).isEmpty()) {
                    String tagName = exclude.get(j);
                    selection = selection.andNot(allCards.stringColumn("Tags").containsString(tagName));
                }
                j++;
            }
        }

        Column list;

        list = allCards.where(selection)
                .column("trainingNumber");

        List<Double> doubleList = new ArrayList(list.size());

        for (int j = 0; j < list.size(); ++j) {
            doubleList.add(Double.valueOf(list.get(j).toString()));
        }

        CategoryChart chart = new CategoryChartBuilder()
                .title("Ratings")
                .xAxisTitle("Rating")
                .yAxisTitle("Count")
                .build();
        Histogram histogram = new Histogram(doubleList, 11, -.5, 10.5);
        chart.addSeries("histogram", histogram.getxAxisData(), histogram.getyAxisData());

        chart.getStyler().setLegendVisible(false)
                .setSeriesColors(new Color[]{statColor})
                .setToolTipsEnabled(true);

        JPanel chartPanel = new XChartPanel<>(chart);
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        return swingNode;

        /*  Old library

        Plot plt = Plot.create();

        plt.hist()
                .add(doubleList)
                .bins(10)
                .stacked(true)
                .color("#66DD66");
        plt.xlim(0, 10);
        plt.title("Ratings");
        String locationName = "resources/charts/histogram.png";
        plt.savefig(locationName).dpi(100);
        try {
            plt.executeSilently();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PythonExecutionException e) {
            throw new RuntimeException(e);
        }

        return getPng(locationName, 350);*/

    }

    public SwingNode numberRatingChart() {

        Column rating;

        BoxChart chart = new BoxChartBuilder().title("Rating by Times Studied").build();
        chart.getStyler().setBoxplotCalCulationMethod(BoxStyler.BoxplotCalCulationMethod.N_LESS_1_PLUS_1);
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setSeriesColors(new Color[]{statColor});

        for (int i = 0; i < 11; ++i) {
            rating = allCards.where(allCards.intColumn("trainingNumber").isEqualTo(i)).column("AmountStudied");
            List<Integer> studiedList = new ArrayList(rating.size());
            for (int j = 0; j < rating.size(); ++j) {
                studiedList.add(Integer.valueOf(rating.get(j).toString()));
            }
            chart.addSeries(Integer.toString(i), studiedList);
        }

        JPanel chartPanel = new XChartPanel<>(chart);
        SwingNode swingNode = new SwingNode();

        swingNode.setContent(chartPanel);

        return swingNode;

/*      OLD CHART LIBRARY

        String locationName = "resources/charts/studiedRatingChart.png";
        Plot plt = Plot.create();

        plt.plot().add(studiedList, ratingList, "o");
        plt.xlabel("Amount Studied");
        plt.ylabel("Rating");
        plt.savefig(locationName).dpi(200);
        try {
            plt.executeSilently();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PythonExecutionException e) {
            throw new RuntimeException(e);
        return getPng(locationName, 600);
        }*/

    }

    public void getRatingProgress() {

    }

    public ImageView getPng(String url, int size) {

        InputStream inputStream;
        try {
            inputStream = Files.newInputStream(Paths.get(url));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Image image = new Image(inputStream);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size * 1.5);
        return imageView;

    }

    public void getDeck(List<String> include, List<String> exclude) {

        Selection selection = null;
        int i = 0;

        ///create a selection to include all included tags

        if (include.size() == 0) {
            AlertBox.display("Error", "No tags included in deck.");
        }
        while (i < include.size()) {
            String tagName = include.get(i);
            if (selection == null) { /// initiates table
                selection = allCards.stringColumn("Tags").containsString(tagName);
                i++;
            } else {
                selection = allCards.stringColumn("Tags").containsString(tagName).or(selection);
                i++;

            }
        }

        if (exclude.size() > 0) {
            int j = 0;
            ///create a selection to exclude all excluded tags
            while (j < exclude.size()) {
                if (!exclude.get(j).isEmpty()) {
                    String tagName = exclude.get(j);
                    selection = selection.andNot(allCards.stringColumn("Tags").containsString(tagName));
                }
                j++;
            }
        }

        addSortColumn();

        ///separate all cards to ones you study and not studying
        studyingStack = allCards.where(selection);
        remainingStack = allCards.dropWhere(selection);

    }

    public Table getDeckForStats(List<String> include, List<String> exclude) {
        Table table;

        Selection selection = null;
        int i = 0;

        ///create a selection to include all included tags

        if (include.size() == 0) {
            AlertBox.display("Error", "No tags included in deck.");
        }
        while (i < include.size()) {
            String tagName = include.get(i);
            if (selection == null) { /// initiates table
                selection = allCards.stringColumn("Tags").containsString(tagName);
                i++;
            } else {
                selection = allCards.stringColumn("Tags").containsString(tagName).or(selection);
                i++;

            }
        }

        if (exclude.size() > 0) {
            int j = 0;
            ///create a selection to exclude all excluded tags
            while (j < exclude.size()) {
                if (!exclude.get(j).isEmpty()) {
                    String tagName = exclude.get(j);
                    selection = selection.andNot(allCards.stringColumn("Tags").containsString(tagName));
                }
                j++;
            }
        }

        table = allCards.where(selection);

        return table;
    }

    public int getDeckNotificationNumber(List<String> include, List<String> exclude) {
        Table table = getDeckForStats(include, exclude);

        int notifNumber = 0;
        int todayInt = (int) (new Date().getTime() / 1000);
        for (int i = 0; i < table.rowCount(); i++) {
            int days = (todayInt - table.intColumn("StudyDate").get(i)) / 24 / 60 / 60;
            if (days > Math.pow(table.intColumn("trainingNumber").get(i),1.5) + 1) {
                notifNumber++;
            }
        }
        return notifNumber;
    }

    public int countNumberTagsInStudying(String tagName){
        return studyingCards.where(studyingCards.stringColumn("Tags").containsString(tagName)).rowCount();
    }

}
