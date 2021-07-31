package com.animetitletracker.kafka;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Map;

public class AnimeNewsNetworkAPI {
    public static void main(String[] args) {
        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target("https://www.animenewsnetwork.com/encyclopedia/reports.xml");
        target = target
                .queryParam("id", "148")
                .queryParam("nlist", "50");

        Invocation.Builder builder = target.request();
        Response response = builder.get();
        if  (response != null && response.getHeaders() != null ) {
            Map<String, ?> some = response.getHeaders();
            for (String k : some.keySet()) {
                System.out.println(k);
            }
        }
//        Title title = builder.get(Title.class);
    }
}
