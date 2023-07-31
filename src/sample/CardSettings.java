package sample;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CardSettings {
    Path path = Paths.get("resources/backendInfo.txt");

    public int currentCardNumber;
    public List<String> recentTagList = null;
    public List<String> tagList = null;
    public List<String> starredList = null;
    public boolean trainingOn = false;
    public List<String> recentDeckList = null;
    public int deckSize = 20;

    public int styleNum = 1;


    public void cardSettings() {
        try {
            this.currentCardNumber = Integer.parseInt(Files.readAllLines(path).get(1));
            this.tagList = Arrays.asList(Files.readAllLines(path).get(2).split(","));
            this.recentTagList = Arrays.asList(Files.readAllLines(path).get(3).split(","));
            this.starredList = new ArrayList<>(Arrays.asList(Files.readAllLines(path).get(4).split(",")));
            this.deckSize = Integer.parseInt(Files.readAllLines(path).get(5));
            this.trainingOn = Boolean.parseBoolean((Files.readAllLines(path).get(6)));
            this.recentDeckList = Arrays.asList(Files.readAllLines(path).get(7).split(","));
            this.styleNum = Integer.parseInt(Files.readAllLines(path).get(8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public int nextCardNumber() throws IOException {
        int cn = currentCardNumber;
        this.currentCardNumber = currentCardNumber + 1;

        saveSettings();

        return cn;
    }

    public void saveSettings() {

        List<String> lines = null;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lines.set(1, String.valueOf(this.currentCardNumber));
        lines.set(2, trimSpacing(String.join(",", this.tagList)));
        lines.set(3, trimSpacing(String.join(",", this.recentTagList)));
        lines.set(4, trimSpacing(String.join(",", this.starredList)));
        lines.set(5, String.valueOf(this.deckSize));
        lines.set(6, String.valueOf(this.trainingOn));
        lines.set(7, trimSpacing(String.join(",", this.recentDeckList)));
        lines.set(8, String.valueOf(this.styleNum));

        try {
            Files.write(path, lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cardSettings();
    }

    public void updateRecentStacks(String tagName) {

        String tagNameTrimmed = tagName.replaceAll("\\(([^()]+)\\)", "").trim();

        Deque<String> dequeList = new ArrayDeque<>(this.recentTagList);

        dequeList.remove(tagNameTrimmed);
        dequeList.addFirst(tagNameTrimmed);

        this.recentTagList = Arrays.asList(String.join(",", dequeList.stream().limit(10).collect(Collectors.toList())));

        saveSettings();
    }

    public void updateRecentDecks(String deckName) {

        Deque<String> dequeList = new ArrayDeque<>(this.recentDeckList);

        dequeList.remove(deckName);
        dequeList.addFirst(deckName);

        this.recentDeckList = Arrays.asList(String.join(",", dequeList));

        saveSettings();
    }

    public void setDeckSize(int i) {
        this.deckSize = i;
    }

    public int getDeckSize() {
        return this.deckSize;
    }

    public void updateNamesInDeckList(String oldName, String newName) {
        Deque<String> dequeList = new ArrayDeque<>(this.recentDeckList);

        dequeList.remove(oldName);
        dequeList.addFirst(newName);

        this.recentDeckList = Arrays.asList(String.join(",", dequeList));

        saveSettings();
    }

    public String trimSpacing(String untrimmed) {
        String trimmed = untrimmed
                .trim()
                .replace(", ", "")
                .replace(" ,", "");
        return trimmed;
    }

    public int getTotalCards() {
        return (currentCardNumber - 1);
    }


    public String[] getMainTags() {

        String[] allTagList = tagList.toArray(new String[0]);

        List<String> mainTagList = new ArrayList<>();

        for (int i = 0; i < allTagList.length; i++) {
            String mainTag = allTagList[i].replaceAll("\\(([^()]+)\\)", "").trim();
            mainTagList.add(mainTag);
        }

        return mainTagList.stream().distinct().collect(Collectors.toList()).toArray(new String[0]);
    }

    public String[] getSubTags(String mainTagName) {

        String[] allTagList = tagList.toArray(new String[0]);
        List<String> subTagList = new ArrayList<>();
        for (int i = 0; i < allTagList.length; i++) {
            String mainTag = allTagList[i];
            String headTag = StringUtils.substringBefore(mainTag,"(").trim();
            if (headTag.equals(mainTagName)) {
                subTagList.add(mainTag);
            }
        }

        return subTagList.stream().distinct().collect(Collectors.toList()).toArray(new String[0]);

    }

    public Boolean checkIfSubTags(String tagName) {

        Boolean subTagBool = false;

        for (String str : tagList) {
            if (str.matches(tagName + " " + "\\(([^()]+)\\)")) {
                subTagBool = true;
            }
        }

        return subTagBool;
    }

}


