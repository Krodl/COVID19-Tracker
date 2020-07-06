package dev.kordell.corona.services;

import com.sun.net.httpserver.Headers;
import dev.kordell.corona.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_US.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    // @PostConstruct tells Spring to fire this method on startup.
    // When the entire app starts up, the @Service will tell Spring to create an instance of the CoronaVirusDataService
    // Once that's done, it'll see the @PostConstruct, and fire this method.
    @PostConstruct
    @Scheduled(cron = "* * 1 * * *") // Tells Spring to wrap this method in a proxy to run this method every second
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>(); // New arraylist so that if a user is trying to access the data while it's being updated, they won't receive an error.
        // Object for the http client. Clients send requests.
        // More info: https://openjdk.java.net/groups/net/httpclient/intro.html
        HttpClient client = HttpClient.newHttpClient();

        //Object to make the HTTP request to get the data. We give it a Uri and use defaults.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader csvBodyReader = new StringReader(httpResponse.body());

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        List<CSVRecord> sortedRecords = new ArrayList<>();

        // Convert iterable into a List so it can be sorted
        for(CSVRecord record : records){
            sortedRecords.add(record);
        }

        // Sort by province
        // This must be done because there is a split in the data between known counties
        // and unknown counties.
        Collections.sort(sortedRecords, Comparator.comparing(o -> o.get("Province_State")));

        String lastState = "";
        int countForState = 0;
        for (CSVRecord record : sortedRecords) {
            if(!lastState.equals(record.get("Province_State")) && !lastState.equals("")){
                //Add the last state and it's data to the newArrayList
                LocationStats locationStats = new LocationStats();
                locationStats.setState(lastState);
                locationStats.setLatestReportedCases(countForState);
                locationStats.setDiffFromPrevDay(countForState - prevDayTotal(sortedRecords, lastState));
                newStats.add(locationStats);

                //Sets countForState to the first value found from the new record.
                countForState = Integer.parseInt(record.get(record.size() - 1));
            } else {
                countForState += Integer.parseInt(record.get(record.size() - 1));
            }
            lastState = record.get("Province_State");
        }

        this.allStats = newStats;

    }

    public int prevDayTotal(List<CSVRecord> records, String state){
        int count = 0;
        for(CSVRecord record : records){
            if(record.get("Province_State").equals(state)){
                count += Integer.parseInt(record.get(record.size() - 2));
            }
        }
        System.out.println("State: " + state + " had " + count + " cases the day before.");
        return count;
    }

}
