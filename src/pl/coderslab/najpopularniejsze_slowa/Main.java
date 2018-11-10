package pl.coderslab.najpopularniejsze_slowa;

import jdk.dynalink.StandardOperation;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        getHeadlinesFromWeb("https://www.interia.pl/", "a.news-a", 3);
        getHeadlinesFromWeb("https://www.wp.pl/", "div.sc-1bp8799-1.gqsna", 3);
        getHeadlinesFromWeb("https://www.onet.pl/", "span.title", 3);
        getHeadlinesFromWeb("https://www.tvn24.pl/", "a", 3);
        getHeadlinesFromWeb("https://tvnwarszawa.tvn24.pl/", "a", 3);
        filterWords("excludeWords.csv");

    }

    static void getHeadlinesFromWeb(String url, String cssQuery, int minLength){

        Connection connect = Jsoup.connect(url);
        try {

            Document document = connect.get();
            System.out.println(document.title());
            StringBuilder words = new StringBuilder();
            ArrayList<String> wordsCleaned = new ArrayList<>();

            Elements linki = document.select(cssQuery);

            wordsCleaned.add("---" + document.title() + "---");

            for (Element el: linki) {

                String tmpStr = el.text();
                String tmpStrNew ="";

                for (int i = 0; i < tmpStr.length(); i++) {

                    char c = tmpStr.charAt(i);

                    if (!Character.isLetterOrDigit(c)){

                        tmpStrNew += " ";
                    } else {

                        tmpStrNew += c;
                    }
                }

                words.append(tmpStrNew + "\n");
            }

            String[] wordsArrayLong = words.toString().split("\\s+");

            for (int i = 0; i < wordsArrayLong.length ; i++) {

                if (wordsArrayLong[i].length() > minLength){

                    wordsCleaned.add(wordsArrayLong[i]);
                }
            }

            wordsCleaned.add("------------------------\n");
            Path path = Paths.get("popular_words.txt");
            Files.write(path, wordsCleaned, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void filterWords(String excludeListFilename){

        ArrayList<String> filterWordsArr = new ArrayList<>();
        ArrayList<String> popularWordsArr = new ArrayList<>();
        ArrayList<String> clearedWordsArr = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(excludeListFilename))) {

            stream.forEach(filterWordsArr::add);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Stream<String> stream = Files.lines(Paths.get("popular_words.txt"))) {

            stream.forEach(popularWordsArr::add);

        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isExcluded = false;
        for (int i = 0; i < popularWordsArr.size(); i++) {

            isExcluded = false;
            for (int j = 0; j < filterWordsArr.size(); j++) {

                if (popularWordsArr.get(i).equalsIgnoreCase(filterWordsArr.get(j))){

                    System.out.print("\nWord: " + popularWordsArr.get(i) + " is excluded.");
                    isExcluded = true;
                }

            }

            if (!isExcluded){

                clearedWordsArr.add(popularWordsArr.get(i));
            }
        }

        try{

            Path path = Paths.get("filtered_words.txt");
            Files.write(path, clearedWordsArr, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
