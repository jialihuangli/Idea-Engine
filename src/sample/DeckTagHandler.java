package sample;

import javafx.embed.swing.SwingNode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.Destination;
import tech.tablesaw.io.json.JsonReadOptions;
import tech.tablesaw.io.json.JsonWriter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DeckTagHandler {
    Table deckTable;
    Table tagTable;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    String newName = "";
    private static Color statColor = new Color(17, 138, 178);
    static String deckPath = "resources/deckInfo.json";
    static String tagPath = "resources/tagInfo.json";


    public void deckHandler() throws IOException {
        this.deckTable = Table.read().usingOptions(JsonReadOptions.builder(deckPath));
        this.tagTable = Table.read().usingOptions(JsonReadOptions.builder(tagPath));
    }

    public void saveDeckTable() {
        JsonWriter jw = new JsonWriter();
        try {
            Destination dest = new Destination(new File(deckPath));
            jw.write(deckTable, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTagTable() {
        JsonWriter jw = new JsonWriter();
        try {
            Destination dest = new Destination(new File(tagPath));
            jw.write(tagTable, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkIfNameUsed(String deckName) {
        boolean isUsed = false;
        List<String> listOfNames = (List<String>) deckTable.column("DeckName").asList();
        for (int i = 0; i < listOfNames.size(); i++) {
            if (listOfNames.get(i).contains(deckName)) {
                isUsed = true;
            }
            ;
        }
        return isUsed;
    }

    public List<String> getIncludeList(String deckName) {
        List<String> includeList;
        if (deckName == null) {
            includeList = new ArrayList();
        } else {
            Table includeTable = deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(deckName));

            String includeString = includeTable.getString(0, "include");

            includeList = new ArrayList(Arrays.asList(includeString.substring(1, includeString.length() - 1).split(",")));
        }
        return includeList;
    }

    public List<String> getExcludeList(String deckName) {
        List<String> excludeList;
        if (deckName == null) {
            excludeList = new ArrayList<>();
        } else {
            Table excludeTable = deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(deckName));

            String excludeString = excludeTable.getString(0, "exclude");

            excludeList = new ArrayList(Arrays.asList(excludeString.substring(1, excludeString.length() - 1).split(",")));
        }
        return excludeList;
    }

    public void editDeckTable(String deckName, String newDeckName, List includeList, List excludeList, String url) {
        Table deckLine = deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(deckName));
        Table remainder = deckTable.where(deckTable.stringColumn("DeckName").isNotEqualTo(deckName));

        StringColumn name = deckLine.stringColumn("DeckName");
        name.set(0, newDeckName);

        StringColumn include = deckLine.stringColumn("include");
        include.set(0, String.valueOf(includeList).replace(", ", ",").replace(" <", "").replace("[,", "[")); ///removes random spaces

        StringColumn exclude = deckLine.stringColumn("exclude");
        exclude.set(0, String.valueOf(excludeList).replace(", ", ",").replace(" <", "").replace("[,", "[")); /// removes random spaces

        StringColumn file = deckLine.stringColumn("background");
        file.set(0, url);

        this.newName = newDeckName;

        this.deckTable = deckLine.append(remainder);
    }

/*  Use history over single recall
    public void changeDeckRating(String deckName, String rating) {
        Table deckLine = deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(deckName));
        Table remainder = deckTable.where(deckTable.stringColumn("DeckName").isNotEqualTo(deckName));

        DoubleColumn deckRating = deckLine.doubleColumn("deckrating");
        deckRating.set(0, Double.parseDouble(rating));

        this.deckTable = deckLine.append(remainder);
    }

    public void changeTagRating(String tagName, String rating) {
        Table tagLine = tagTable.where(tagTable.stringColumn("TagName").isEqualTo(tagName));
        Table remainder = tagTable.where(tagTable.stringColumn("TagName").isNotEqualTo(tagName));
        StringColumn tagRating = tagLine.stringColumn("tagrating");
        tagRating.set(0, rating);

        this.tagTable = tagLine.append(remainder);
    }
*/

    public void newDeckTable(String newDeckName, List includeList, List excludeList, String url, Double deckRating) {

        StringColumn c0 = StringColumn.create("DeckName", newDeckName);
        StringColumn c1 = StringColumn.create("include", includeList.toString());
        StringColumn c2 = StringColumn.create("exclude", excludeList.toString());
        StringColumn c3 = StringColumn.create("background", url);
        StringColumn c4 = StringColumn.create("ratingHistory", "0.00");
        StringColumn c5 = StringColumn.create("timeHistory", "0.00");
        StringColumn c6 = StringColumn.create("ratingByDate", "");
        StringColumn c7 = StringColumn.create("timeByDate", "");
        StringColumn c8 = StringColumn.create("amountByDate", "");
        IntColumn c9 = IntColumn.create("totalTime", 0);
        IntColumn c10 = IntColumn.create("totalAmount", 0);
        StringColumn c11 = StringColumn.create("date", "");


        Table newDeckInfo = Table.create(c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11);

        this.deckTable = deckTable.append(newDeckInfo);
        saveDeckTable();

    }

    public void newTagTable(String newTagName) {

        String newTagNameFixed = String.valueOf(newTagName).replace("<", "").replace(">", "");

        StringColumn c0 = StringColumn.create("TagName", newTagNameFixed);
        StringColumn c1 = StringColumn.create("ratingHistory", "");
        StringColumn c2 = StringColumn.create("timeHistory", "");
        StringColumn c3 = StringColumn.create("ratingByDate", "");
        StringColumn c4 = StringColumn.create("date", "");
        StringColumn c5 = StringColumn.create("timeByDate", "");
        StringColumn c6 = StringColumn.create("amountByDate", "");
        IntColumn c7 = IntColumn.create("totalTime", 0);
        IntColumn c8 = IntColumn.create("totalAmount", 0);

        Table newTagInfo = Table.create(c0, c1, c2, c3, c4, c5, c6, c7, c8);

        if (tagTable.rowCount() == 0) {
            tagTable = newTagInfo;
        } else {
            this.tagTable = tagTable.append(newTagInfo);
        }
        saveTagTable();

    }

    public String getBackgroundPath(String deckName) {
        Table deckInfo = deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(deckName));
        String imgPath = deckInfo.getString(0, "background");
        return imgPath;
    }

    public String getNewName() {

        return this.newName;
    }

    public String getDeckRating(String deckName) {
        Table deckInfo = deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(deckName));
        List list = Arrays.asList(deckInfo.getString(0, "ratingHistory").replace("[", "").replace("]", "").split(","));
        String rating = (String) list.get(list.size() - 1);
        return rating;
    }

    public String getTagRating(String tagName) {
        List<String> rating = new ArrayList(Arrays.asList(
                tagTable.where(tagTable.stringColumn("TagName")
                                .isEqualTo(tagName))
                        .getString(0, "ratingHistory")
                        .split(",")));

        return rating.get(rating.size() - 1);
    }

    public int getDeckTimeTotal(String deckName) {
        return deckTable.where(deckTable.stringColumn("DeckName")
                .isEqualTo(deckName)).intColumn("totalTime").get(0);
    }

    public int getTagTimeTotal(String tagName) {
        return tagTable.where(tagTable.stringColumn("TagName")
                .isEqualTo(tagName)).intColumn("totalTime").get(0);
    }

    public int getDeckStudyTotal(String deckName) {
        return deckTable.where(deckTable.stringColumn("DeckName")
                .isEqualTo(deckName)).intColumn("totalAmount").get(0);
    }

    public int getTagStudyTotal(String tagName) {
        return tagTable.where(tagTable.stringColumn("TagName")
                .isEqualTo(tagName)).intColumn("totalAmount").get(0);
    }

    public void addToHistory(String newName, Double number, boolean isDeck, boolean isRating) {
        String colName;

        if (isRating) {
            colName = "ratingHistory";
        } else {
            colName = "timeHistory";
        }

        String name = String.valueOf(newName).replace("<", "").replace(">", "");

        List list;
        if (isDeck) {
            list = new ArrayList(Arrays.asList(deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(name)).getString(0, colName)));
        } else {
            list = new ArrayList(Arrays.asList(tagTable.where(tagTable.stringColumn("TagName").isEqualTo(name)).getString(0, colName)));
        }
        if (list.size() > 250) {
            list.remove(0);
            list.add(number);
        } else {
            list.add(number);
        }
        Table line;
        Table remainder;
        if (isDeck) {
            line = deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(name));
            remainder = deckTable.where(deckTable.stringColumn("DeckName").isNotEqualTo(name));
        } else {
            line = tagTable.where(tagTable.stringColumn("TagName").isEqualTo(name));
            remainder = tagTable.where(tagTable.stringColumn("TagName").isNotEqualTo(name));
        }
        StringColumn changeLine = line.stringColumn(colName);
        changeLine.set(0, String.valueOf(list)
                .replace("[,", "")
                .replace("[", "")
                .replace("]", "")
                .replace(" ", ""));

        if (isDeck) {
            this.deckTable = line.append(remainder);
        } else {
            this.tagTable = line.append(remainder);
        }
    }

    public void specialTagHistory(String tagName, Double tagRating) { ///gives rating history of sub tag
        List list = new ArrayList(Arrays.asList(
                tagTable.where(tagTable.stringColumn("TagName")
                                .isEqualTo(tagName))
                        .getString(0, "ratingHistory")
                        .split(",")));
        if (!String.valueOf(tagRating).equals(String.valueOf(list.get(list.size() - 1)))) { /// if not the same as previous number, change list
            if (list.size() >= 250) {
                list.remove(0);
                list.add(tagRating);
            } else {
                list.add(tagRating);
            }
            Table line = tagTable.where(tagTable.stringColumn("TagName").containsString(tagName));
            Table remainder = tagTable.dropWhere(tagTable.stringColumn("TagName").containsString(tagName));
            StringColumn changeLine = line.stringColumn("ratingHistory");
            changeLine.set(0, String.valueOf(list)
                    .replace("[,", "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace(" ", ""));
            this.tagTable = line.append(remainder);
        }
    }

    public SwingNode getTagProgress(String name, Boolean isTime) {

        List list;
        Table deckInfo = tagTable.where(tagTable.stringColumn("TagName").isEqualTo(name));

        /// create list of time or rating
        if (isTime) {
            list = Arrays.asList(deckInfo.getString(0, "timeHistory").replace("[", "").replace("]", "").split(","));
        } else {
            list = Arrays.asList(deckInfo.getString(0, "ratingHistory").replace("[", "").replace("]", "").split(","));
        }

        List<Double> doubleList = new ArrayList(list.size());
        List<Integer> index = new ArrayList(list.size());

        for (int i = 0; i < list.size(); ++i) {
            doubleList.add(Double.valueOf(list.get(i).toString()));
            index.add(i + 1);
        }

        XYChart chart = QuickChart.getChart("Rating History", "", "Rating", "y(x)", index, doubleList);

        chart.getStyler().setLegendVisible(false)
                .setToolTipsEnabled(true)
                .setSeriesColors(new Color[]{statColor});
        chart.getStyler().setXAxisTicksVisible(false);

        JPanel chartPanel = new XChartPanel<>(chart);

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        return swingNode;
/*      OLD CHART LIBRARY
        Plot plt = Plot.create();

        plt.plot()
                .add(doubleList)
                .label("label")
                .linestyle("--");
        plt.ylabel("Rating");
        plt.text(0.5, 0.2, "text");
        plt.title("Rating History");

        String locationName = "resources/charts/lineChart.png";
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

    public SwingNode getDeckProgress(String deckName, Boolean isTime) {

        List list;
        Table deckInfo = deckTable.where(deckTable.stringColumn("DeckName").isEqualTo(deckName));

        /// create list of time or rating
        if (isTime) {
            list = Arrays.asList(deckInfo.getString(0, "timeHistory").replace("[", "").replace("]", "").split(","));
        } else {
            list = Arrays.asList(deckInfo.getString(0, "ratingHistory").replace("[", "").replace("]", "").split(","));
        }

        List<Double> doubleList = new ArrayList(list.size());
        List<Integer> index = new ArrayList(list.size());

        for (int i = 0; i < list.size(); ++i) {
            doubleList.add(Double.valueOf(list.get(i).toString()));
            index.add(i + 1);
        }

        XYChart chart = QuickChart.getChart("Rating History", "", "Rating", "y(x)", index, doubleList);

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false)
                .setSeriesColors(new Color[]{statColor})
                .setToolTipsEnabled(true);

        JPanel chartPanel = new XChartPanel<>(chart);
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        return swingNode;
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

    public void updateTagRatingDateTimeAmount(String tagName, int amount, int time) {
        Date date = new Date();
        String dateString = formatter.format(date);

        String tagRating = getTagRating(tagName);

        List dateList = new ArrayList(Arrays.asList(
                tagTable.where(tagTable.stringColumn("TagName")
                                .isEqualTo(tagName))
                        .getString(0, "date")
                        .split(",")));

        List ratingList = new ArrayList(Arrays.asList(
                tagTable.where(tagTable.stringColumn("TagName")
                                .isEqualTo(tagName))
                        .getString(0, "ratingByDate")
                        .split(",")));
        List timeList = new ArrayList(Arrays.asList(
                tagTable.where(tagTable.stringColumn("TagName")
                                .isEqualTo(tagName))
                        .getString(0, "timeByDate")
                        .split(",")));

        List amountList = new ArrayList(Arrays.asList(
                tagTable.where(tagTable.stringColumn("TagName")
                                .isEqualTo(tagName))
                        .getString(0, "amountByDate")
                        .split(",")));


        if (!dateList.contains(dateString)) { /// if it doesn't contain date, add new
            dateList.add(dateString);
            ratingList.add(tagRating);
            timeList.add(time);
            amountList.add(amount);
            if (dateList.size() > 100) {
                dateList.remove(0);
                ratingList.remove(0);
                timeList.remove(0);
                amountList.remove(0);
            }
            changeTagTableLine(tagName, "date", dateList);
            changeTagTableLine(tagName, "ratingByDate", ratingList);
            changeTagTableLine(tagName, "timeByDate", timeList);
            changeTagTableLine(tagName, "amountByDate", amountList);
        } else {  /// if it does contain date, only change most recent rating value

            ratingList.remove(ratingList.size() - 1);
            timeList.remove(timeList.size() - 1);
            amountList.remove(amountList.size() - 1);

            ratingList.add(tagRating);
            timeList.add(time);
            amountList.add(amount);

            changeTagTableLine(tagName, "ratingByDate", ratingList);
            changeTagTableLine(tagName, "amountByDate", amountList);
            changeTagTableLine(tagName, "timeByDate", timeList);
        }

        int totalAmount = tagTable.where(tagTable.stringColumn("TagName")
                .isEqualTo(tagName)).intColumn("totalAmount").get(0) + amount;
        int totalTime = tagTable.where(tagTable.stringColumn("TagName")
                .isEqualTo(tagName)).intColumn("totalTime").get(0) + time;

        changeTagTableLineInt(tagName, "totalTime", totalTime);
        changeTagTableLineInt(tagName, "totalAmount", totalAmount);

    }

    public void updateDeckRatingDateTimeAmount(String deckName, int amount, int time) {
        Date date = new Date();
        String dateString = formatter.format(date);
        String deckRating = getDeckRating(deckName);

        List dateList = new ArrayList(Arrays.asList(
                deckTable.where(deckTable.stringColumn("DeckName")
                                .isEqualTo(deckName))
                        .getString(0, "date")
                        .split(",")));

        List ratingList = new ArrayList(Arrays.asList(
                deckTable.where(deckTable.stringColumn("DeckName")
                                .isEqualTo(deckName))
                        .getString(0, "ratingByDate")
                        .split(",")));

        List timeList = new ArrayList(Arrays.asList(
                deckTable.where(deckTable.stringColumn("DeckName")
                                .isEqualTo(deckName))
                        .getString(0, "timeByDate")
                        .split(",")));

        List amountList = new ArrayList(Arrays.asList(
                deckTable.where(deckTable.stringColumn("DeckName")
                                .isEqualTo(deckName))
                        .getString(0, "amountByDate")
                        .split(",")));

        if (!dateList.contains(dateString)) { /// if it doesn't contain date, add new
            dateList.add(dateString);
            ratingList.add(deckRating);
            timeList.add(time);
            amountList.add(amount);
            if (dateList.size() > 100) {
                dateList.remove(0);
                ratingList.remove(0);
                timeList.remove(0);
                amountList.remove(0);
            }
            changeDeckTableLine(deckName, "date", dateList);
            changeDeckTableLine(deckName, "ratingByDate", ratingList);
            changeDeckTableLine(deckName, "timeByDate", timeList);
            changeDeckTableLine(deckName, "amountByDate", amountList);

        } else {  /// if it does contain date, only change most recent rating value

            ratingList.remove(ratingList.size() - 1);
            timeList.remove(timeList.size() - 1);
            amountList.remove(amountList.size() - 1);

            ratingList.add(deckRating);
            timeList.add(time);
            amountList.add(amount);

            changeDeckTableLine(deckName, "ratingByDate", ratingList);
            changeDeckTableLine(deckName, "amountByDate", amountList);
            changeDeckTableLine(deckName, "timeByDate", timeList);
        }

        int totalAmount = deckTable.where(deckTable.stringColumn("DeckName")
                .isEqualTo(deckName)).intColumn("totalAmount").get(0) + amount;
        int totalTime = deckTable.where(deckTable.stringColumn("DeckName")
                .isEqualTo(deckName)).intColumn("totalTime").get(0) + time;

        changeDeckTableLineInt(deckName, "totalTime", totalTime);
        changeDeckTableLineInt(deckName, "totalAmount", totalAmount);


    }

    public void changeTagTableLine(String tagName, String infoName, List<String> data) {
        Table line = tagTable.where(tagTable.stringColumn("TagName").containsString(tagName));
        Table remainder = tagTable.dropWhere(tagTable.stringColumn("TagName").containsString(tagName));

        StringColumn changeLine = line.stringColumn(infoName);

        changeLine.set(0, String.valueOf(data)
                .replace("[,", "")
                .replace("[", "")
                .replace("]", "")
                .replace(" ", ""));
        this.tagTable = line.append(remainder);

    }

    public void changeDeckTableLine(String deckName, String infoName, List<String> data) {
        Table line = deckTable.where(deckTable.stringColumn("DeckName").containsString(deckName));
        Table remainder = deckTable.dropWhere(deckTable.stringColumn("DeckName").containsString(deckName));

        StringColumn changeLine = line.stringColumn(infoName);

        changeLine.set(0, String.valueOf(data)
                .replace("[,", "")
                .replace("[", "")
                .replace("]", "")
                .replace(" ", ""));

        this.deckTable = line.append(remainder);

    }

    public void changeTagTableLineInt(String tagName, String infoName, int i) {
        Table line = tagTable.where(tagTable.stringColumn("TagName").containsString(tagName));
        Table remainder = tagTable.dropWhere(tagTable.stringColumn("TagName").containsString(tagName));

        IntColumn changeLine = line.intColumn(infoName);

        changeLine.set(0, i);
        this.tagTable = line.append(remainder);
    }

    public void changeDeckTableLineInt(String deckName, String infoName, int i) {
        Table line = deckTable.where(deckTable.stringColumn("DeckName").containsString(deckName));
        Table remainder = deckTable.dropWhere(deckTable.stringColumn("DeckName").containsString(deckName));

        IntColumn changeLine = line.intColumn(infoName);

        changeLine.set(0, i);
        this.deckTable = line.append(remainder);
    }


}


