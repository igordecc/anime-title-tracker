package com.animetitletracker.kafka;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimeNewsNetworkAPI {

    public static void main(String[] args) throws InterruptedException {
        int titlesToGet = 50;
        int NEW_TITLES = 0;
        HashMap<String, Integer> titleTypes = new HashMap<>();
        Element checkpointElement = new Element("a");
        String startFromDate = "2021-07-30 22:26:57";
        String lastDate = startFromDate;
        LinkedHashSet<Element> newTitlesList = new LinkedHashSet<>();

        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target("https://www.animenewsnetwork.com/encyclopedia/reports.xml");
        target = target
                .queryParam("id", "148") //148 and 155
                .queryParam("nlist", titlesToGet);
        Invocation.Builder builder = target.request();

        while (true) {
            try {
                Response response = builder.get();
                String html = response.readEntity(String.class);
                Document doc = Jsoup.parse(html, "", Parser.xmlParser());
                System.out.println(doc.select("type").toString());
                if (newTitlesList.isEmpty() && lastDate.isEmpty() && checkpointElement.toString().isEmpty()) {
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
                            newTitlesList.add(element);
                        }
                    }
                }
                NEW_TITLES = newTitlesList.size() ;
                for (Element element : newTitlesList) {
                    String animeName = element.select("anime").text();
                    Matcher matcher = Pattern
                            .compile("\\(\\w*\\s*\\w\\)")
                            .matcher(animeName);
                    String animeType = matcher.find() ? matcher.group() : "";
                    if (!titleTypes.containsKey(animeType)) {titleTypes.put(animeType, 0);}
                    titleTypes.put(animeType, titleTypes.get(animeType) + 1);
                }
                System.out.println("NEW TITLES: " + NEW_TITLES );
                for (String title : titleTypes.keySet()) {
                    System.out.println(String.format("%s : %s", title, titleTypes.get(title)));
                }
                titleTypes = new HashMap<>();
                newTitlesList = new LinkedHashSet<>();
                System.out.println("----------------");
            } catch (Exception e) {
                System.out.println(e);
            }
            Thread.sleep(10000L);
        }
    }
}
