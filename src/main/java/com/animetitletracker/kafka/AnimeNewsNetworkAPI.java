package com.animetitletracker.kafka;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;

public class AnimeNewsNetworkAPI {
    public static void main(String[] args) {
        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target("https://www.animenewsnetwork.com/encyclopedia/reports.xml");
        target = target
                .queryParam("id", "148")
                .queryParam("nlist", "50");

        Invocation.Builder builder = target.request();

        Response response = builder.get();

        String html = response.readEntity(String.class);

        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        for (Element e : doc.select("item")) {
            System.out.println(e);
        }
        // clean up resources

    }
}
