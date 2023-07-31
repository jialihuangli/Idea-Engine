package sample;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Entry {

    public String title;
    public String Major;
    public String secondaryInfo;
    public String secondaryText;
    public List<String> tagList;
    public String source;
    public List<String> stacksList;
    public int importance;
    public String cardType;

    public DeckTagHandler deckTagHandler = Main.deckTagHandler;
    private final int studyAmount = 0;
    private final int dateCreate = (int) (new Date().getTime() / 1000);

    public void save(CardSettings cardSettings) {

        /// Data to savable Object

        JSONParser jsonParser = new JSONParser();
        try {
            /// Save Card
            Object obj = jsonParser.parse(new FileReader("resources/cards.json"));
            JSONArray jsonArray = (JSONArray) obj;

            JSONObject cardItem = new JSONObject();

            cardItem.put("info1", this.title);
            cardItem.put("info2", this.Major);
            cardItem.put("info3", this.secondaryInfo);
            cardItem.put("info3text", this.secondaryText);
            cardItem.put("CreationDate", dateCreate);
            cardItem.put("EditDate", dateCreate);
            cardItem.put("StudyDate", dateCreate);
            cardItem.put("#", Main.cardSettings.nextCardNumber());
            cardItem.put("Tags", tagList.toString());
            cardItem.put("AmountStudied", studyAmount);
            cardItem.put("Importance", importance);
            cardItem.put("cardType", cardType);
            cardItem.put("Source", this.source);
            cardItem.put("trainingNumber", 3);
            jsonArray.add(cardItem);

            FileWriter file = new FileWriter("resources/cards.json");
            file.write(jsonArray.toJSONString());
            file.flush();

            /// Add new tags to backend
                //remove <> from tag names
            List<String> fixedTagList = new ArrayList<>();
            for(int i = 0; i < tagList.size(); i++){
                fixedTagList.add(tagList.get(i).substring(1, tagList.get(i).length()-1));
            }

            /// Add new tags to tagInfo
            for(String tag: tagList) {
                if(!deckTagHandler.tagTable.columnNames().contains(tag)){
                    deckTagHandler.newTagTable(tag);
                };
            }

            Path path = Paths.get("resources/backendInfo.txt");
            List<String> oldList = Arrays.asList(Files.readAllLines(path).get(2).split(","));
            Set<String> combined = new TreeSet<>();
            combined.addAll(oldList);
            combined.addAll(fixedTagList);

            cardSettings.tagList = (List<String>) new ArrayList<String>(combined);
            cardSettings.saveSettings();

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMajor(String major) {
        this.Major = major;
    }

    public void setMinor(String HTML) {
        this.secondaryInfo = HTML;
        Document document = Jsoup.parse(HTML);
        String text = document.select("body").text();
        this.secondaryText = text;
    }

    public void setTags(List tagList) {
        this.tagList = tagList;
    }

    public void setImportance(int i) {
        this.importance = i;
    }

    public void setCardType(String str) {
        this.cardType = str;
    }
}
