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

public class AnimeNewsNetworkAPI {

    public static void main(String[] args) throws InterruptedException {
        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target("https://www.animenewsnetwork.com/encyclopedia/reports.xml");
        target = target
                .queryParam("id", "148")
                .queryParam("nlist", "50");
        Invocation.Builder builder = target.request();

        ArrayList<Element> state = new ArrayList<>();
        LinkedHashSet<Element> newTitlesList = new LinkedHashSet<>();

        while (true) {
            try {
                Response response = builder.get();
                String html = response.readEntity(String.class);
                Document doc = Jsoup.parse(html, "", Parser.xmlParser());
                if (newTitlesList.isEmpty()) { newTitlesList.add(doc.select("item").first()); }
                else {
                    for (Element element : doc.select("item")) {
                        if (element.toString().equals( newTitlesList.iterator().next().toString()) ){
                            System.out.println( " status: ok - 200");
                            break;
                        }else{ newTitlesList.add(element); }
                    }
                }
                System.out.println("NEW TITLES: " + (newTitlesList.size() - 1) );
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
            Thread.sleep(10000L);
        }

        // clean up resources

    }
}
