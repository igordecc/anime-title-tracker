package com.animetitletracker.common;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import java.util.NoSuchElementException;

public class AppConfig {
    private final Options options;
    private final CommandLineParser parser;
    private final CommandLine commandLine;

    private static AppConfig instance = null;

    {
        options = new Options();
        parser = new DefaultParser();

        options.addOption(
                "n",
                "es.nodes",
                true,
                "List of Elasticsearch nodes to connect to");
        options.addOption(
                "p",
                "es.port",
                true,
                "This setting is applied to the nodes in es.nodes " +
                        "that do not have any port specified.\n");
        options.addOption(
                "a",
                "airports_files_path",
                true,
                "The path to the source data about airports");
        options.addOption(
                "f",
                "flights_files_path",
                true,
                "The path to the source data about flights");
        options.addOption(
                "r",
                "routes_files_path",
                true,
                "The path to the source data about routes");
        options.addOption(
                "i",
                "es_index",
                true,
                "The elasticsearch index name");

        options.addOption(
                "l",
                "os_login",
                true,
                "The opensky login");
        options.addOption(
                "p",
                "os_password",
                true,
                "The opensky password");
        options.addOption(
                "t",
                "topic",
                true,
                "The Kafka Topic name");
        options.addOption(
                "fn",
                "flightNumberVotes_files_path",
                true,
                "File path for FlightNumberVotes");
        options.addOption(
                "sh",
                "spark_driver_host",
                true,
                "Spark Driver Host address");
        options.addOption(
                "sb",
                "spark_driver_bind_address",
                true,
                "spark_driver_bind_address");
        options.addOption(
                "k",
                "kafka_bootstrap_servers",
                true,
                "Kafka Boostrap Servers addresses");
        options.addOption(
                "o",
                "output_file_path",
                true,
                "Output file path");
        options.addOption(
                "es",
                "es_source",
                true,
                "ElasticSearch source");
        options.addOption(
                "cp",
                "checkpoint_location",
                true,
                "Checkpoint Location");

    }

    private AppConfig(String[] params) {
        try {
            commandLine = parser.parse( options, params);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Couldn't parse config parameters", e);
        }
    }

    public static AppConfig getInstance(String[] params) {
        if (instance == null) {
            instance = new AppConfig(params);
        }
        return instance;
    }

    public String getESNodes() {
        return getSetting("es.nodes");
    }

    public String getESPort() {
        return getSetting("es.port");
    }

    public String getAirportsFilesPath() {
        return getSetting("airports_files_path");
    }

    public String getFlightsFilesPath() {
        return getSetting("flights_files_path");
    }

    public String getRoutesFilesPath() { return getSetting("routes_files_path"); }

    public String getFlightNumberVotes() { return getSetting("flightNumberVotes_files_path"); }

    public String getOutputFilePath() { return getSetting("output_file_path"); }

    public String getESIndex() {
        return getSetting("es_index");
    }

    public String getOpenSkyLogin() {
        return getSetting("os_login");
    }

    public String getOpenSkyPass() {
        return getSetting("os_password");
    }

    public String getTopic() {
        return getSetting("topic");
    }

    public String getSparkDriverHost() {
        return getSetting("spark_driver_host");
    }

    public String getSparkBindAddress() {
        return getSetting("spark_driver_bind_address");
    }

    public String getKafkaBootstrapServers() {
        return getSetting("kafka_bootstrap_servers");
    }

    public String getEsSource() {
        return getSetting("es_source");
    }

    public String getCheckpointLocation() {
        return getSetting("checkpoint_location");
    }

    private String getSetting(String key) {
        if (!commandLine.hasOption(key)) {
            throw new NoSuchElementException("Couldn't find property with \"" + key + "\" key");
        }

        return commandLine.getOptionValue(key);
    }
}