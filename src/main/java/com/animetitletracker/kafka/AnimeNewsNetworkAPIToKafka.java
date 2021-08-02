package com.animetitletracker.kafka;

import com.animetitletracker.common.AppConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AnimeNewsNetworkAPIToKafka {
    public static void main(String[] args) throws IOException {
        AppConfig appConfig = AppConfig.getInstance(args);
        getInputStream(appConfig, 50, 10000L, 9999);
    }

    public static void getInputStream(AppConfig appConfig, int titlesToGet, long sleepTime, int lifetime) throws IOException {

        Stream<String> some = new ArrayList<String>(1).stream();
        try{
            while (true){
                Client client = ClientBuilder.newBuilder().newClient();
                WebTarget target = client.target("https://www.animenewsnetwork.com/encyclopedia/reports.xml");
                target = target
                        .queryParam("id", "148") //148 and 155
                        .queryParam("nlist", titlesToGet);
                Invocation.Builder builder = target.request();

                Response response = builder.get();
                String html = response.readEntity(String.class);
                Document doc = Jsoup.parse(html, "", Parser.xmlParser());

                ArrayList<Map<String, Object>> outList = new ArrayList<>();
                for (Element element : doc.select("item")) {
                    Map<String, Object> outMap = new HashMap<>();
                    outMap.put("anime", element.select("anime").text());
                    String animeName = element.select("anime").text();
                    Matcher matcher = Pattern
                            .compile("\\(\\w*\\s*\\w\\)")
                            .matcher(animeName);
                    outMap.put("animeType", matcher.find() ? matcher.group() : "");
                    outMap.put("dateAdded", element.select("date_added").text());

                    outList.add(outMap);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String outJson = objectMapper.writeValueAsString(outList);
                KafkaProducerForANN.runProducer(outJson, appConfig);

                Thread.sleep(sleepTime);
            }
        }catch(InterruptedException e){ System.out.println(e); };

        // TODO make json from doc +++ but not tested
        // TODO TEST JSON
        // TODO install kafka on windows - NO!
        // just unfold dockerfile folder on your docker
        // TODO spark read from kafka
        // TODO download and install ES and Kibana
        // TODO spark write to ES index
        // TODO create dashboard in Kibana to display the index

    }
}
