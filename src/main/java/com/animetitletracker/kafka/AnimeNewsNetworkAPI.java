package com.animetitletracker.kafka;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimeNewsNetworkAPI {

    public static Response getResponse() {
        int TITLES_TO_GET = 50; // can't fetch more than 50
        String ID = "148"; //148 and 155
        String API_URL = "https://www.animenewsnetwork.com/encyclopedia/reports.xml";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(API_URL);
        target = target
                .queryParam("id", ID)
                .queryParam("nlist", TITLES_TO_GET);
        Builder builder = target.request();
        return builder.get();
    }

    public static Set<Element> parseTitleNames(Document doc, Element checkpointElement, String lastDate) {

        Set<Element> newTitleNamesBuffer = new LinkedHashSet<>();

        System.out.println(doc.select("type").toString());
        if (newTitleNamesBuffer.isEmpty() && lastDate.isEmpty() && checkpointElement.toString().isEmpty()) {
            checkpointElement = doc.select("item").first();
            lastDate = doc.select("date_added").first().text();
        }else if(!lastDate.isEmpty() && checkpointElement.toString().isEmpty()){
            checkpointElement = doc.select(String.format("item:added:contains(%s)", lastDate)).first();
        }
        else {
            for (Element element : doc.select("item")) {
                boolean isDateTheLast = element.toString().equals(
                        doc.select(String.format("item:contains(%s)", lastDate)).toString());
                if (isDateTheLast) { break; } else {
                    newTitleNamesBuffer.add(element);
                }
            }
        }
        return newTitleNamesBuffer;
    }

    public static Map<String, Integer> parseTitleTypes(Set<Element> newTitleNamesBuffer) {
        Map<String, Integer> titleTypesBuffer = new HashMap<>();
        for (Element element : newTitleNamesBuffer) {
            String animeName = element.select("anime").text();
            Matcher matcher = Pattern
                    .compile("\\(\\w*\\s*\\w\\)")
                    .matcher(animeName);
            String animeType = matcher.find() ? matcher.group() : "";
            if (!titleTypesBuffer.containsKey(animeType)) {titleTypesBuffer.put(animeType, 0);}
            titleTypesBuffer.put(animeType, titleTypesBuffer.get(animeType) + 1);
        }
        return titleTypesBuffer;
    }

    public static void display(Set<Element> newTitleNamesBuffer, Map<String, Integer> titleTypesBuffer) {
        System.out.println("NEW TITLES: " + newTitleNamesBuffer.size() );
        for (String title : titleTypesBuffer.keySet()) {
            System.out.println(String.format("%s : %s", title, titleTypesBuffer.get(title)));
        }
        System.out.println("----------------");
    }

    public static void main(String[] args) throws InterruptedException {
        Element CHECKPOINT_ELEMENT = new Element("a");
        String LAST_DATE = "2021-07-30 22:26:57";

        while (true) {
            try {
                Response response = AnimeNewsNetworkAPI.getResponse();
                String html = response.readEntity(String.class);
                Document doc = Jsoup.parse(html, "", Parser.xmlParser());

                Set<Element>  newTitleNamesBuffer = parseTitleNames(doc, CHECKPOINT_ELEMENT, LAST_DATE);
                Map<String, Integer> titleTypesBuffer = parseTitleTypes(newTitleNamesBuffer);

                display(newTitleNamesBuffer, titleTypesBuffer);

                titleTypesBuffer.clear();
                newTitleNamesBuffer.clear();
            } catch (Exception e) {
                System.out.println(e);
            }
            Thread.sleep(10000L);
        }
    }
}
