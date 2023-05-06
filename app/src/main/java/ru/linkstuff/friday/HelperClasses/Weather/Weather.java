package ru.linkstuff.friday.HelperClasses.Weather;

/**
 * Created by alexander on 21.10.17.
 */

public class Weather {
    String city;
    String summary;
    String temperature;
    String hourlySummary;

    public Weather(String city, String summary, double temperature, String hourlySummary){
        this.city = city;
        this.summary = summary;
        this.hourlySummary = hourlySummary;

        this.temperature = Math.round((temperature - 32) * 5 / 9) + "°C";
    }

    public String getCity() {
        return city;
    }

    public String getSummary() {
        return summary;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHourlySummary() {
        return hourlySummary;
    }
}
