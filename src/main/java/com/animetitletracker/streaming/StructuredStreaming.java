package com.animetitletracker.streaming;

import com.animetitletracker.common.AppConfig;
import com.animetitletracker.kafka.ATTSchema;
import com.google.gson.Gson;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQueryException;

import static org.apache.spark.sql.functions.trim;

public class StructuredStreaming {
    public static void main(String[] args) throws StreamingQueryException {
        AppConfig appConfig = AppConfig.getInstance(args);
        SparkConf conf = new SparkConf()
                .set("spark.master", "local");
//                .set("spark.driver.host", appConfig.getSparkDriverHost())
//                .set("spark.driver.bindAddress", appConfig.getSparkBindAddress());

        SparkSession spark = SparkSession
                .builder()
                .config("spark.sql.warehouse.dir", "file:///C:/temp")
                .config(conf)
                .getOrCreate()
                ;

        Dataset<Row> df = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", appConfig.getKafkaBootstrapServers())
                .option("subscribe", appConfig.getTopic())
                .option("startingOffset", "latest")
                .option("fetchOffset.retryIntervalMs", 10001)
                .load();

        Dataset<String> dfs = df.selectExpr("md5(cast(key as string))", "CAST(value AS STRING)")
                .select("value")
                .as(Encoders.STRING());
        // step 1 - decode value
        Dataset<ATTSchema> ANNSchemaDataset = dfs.map((MapFunction<String, ATTSchema>) record -> {
            Gson g = new Gson();
            return g.fromJson(record, ATTSchema.class);
        }, Encoders.bean(ATTSchema.class));
        Dataset<Row> openSky = ANNSchemaDataset.toDF();
        openSky.writeStream()
        .outputMode("append")
        .format("console")
        .start()
        .awaitTermination();

//        openSky.show();

//        //transform openSky data to ES format
//        openSky = openSky
//                .withColumn("position",
//                        getGeoPointColumn(openSky,
//                                "longitude",
//                                "latitude"))
//                .drop("geoAltitude",
//                        "heading",
//                        "icao24",
//                        "lastContact",
//                        "longitude",
//                        "latitude",
//                        "number");
//
//        openSky.show();

//        Dataset<Row> routesAirportsData = RoutesAirports.getRoutesAirports(spark, appConfig)
//                .withColumnRenamed("callsign", "callsign_air");
//
//
//        routesAirportsData.show();
//
//        Dataset<Row> resultDF = openSky
//                .join(routesAirportsData,
//                        trim(openSky.col("callsign"))
//                                .equalTo(trim(routesAirportsData.col("callsign_air"))),
//                        "inner")
//                .drop("callsign_air");
//
//        //add date column to resultDS
//        String datePattern = "yyyy-MM-dd HH:mm:ssXXX";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
//        resultDF = resultDF.withColumn("date",
//                functions.lit(simpleDateFormat.format(new Date())));
//
//        Dataset<ESStreamingSchema> resultDS = resultDF.as(Encoders.bean(ESStreamingSchema.class));
//
//        //sending data to ElasticSearch
//        resultDS.writeStream()
//                .outputMode("append")
//                .queryName("writing_to_es")
//                .format("org.elasticsearch.spark.sql")
//                .option("fetchOffset.retryIntervalMs", 10001)
//                .option("es.resource", appConfig.getEsSource())
//                .option("checkpointLocation", appConfig.getCheckpointLocation())
//                .option("es.nodes", appConfig.getESNodes())
//                .option("es.port", appConfig.getESPort())
//                .start()
//                .awaitTermination();
    }
}
