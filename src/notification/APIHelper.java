package notification;

import java.io.*;
import java.net.*;

public class APIHelper {
    // Thay bằng API key của bạn
    private static final String WEATHER_API_KEY = "";
    private static final String NEWS_API_KEY = "";

    private static final String WEATHER_URL =
        "http://api.openweathermap.org/data/2.5/weather?q=Hanoi&appid=" + WEATHER_API_KEY + "&units=metric";

    private static final String NEWS_URL =
        "https://newsapi.org/v2/top-headlines?country=us&apiKey=" + NEWS_API_KEY;

    // Lấy dữ liệu thời tiết
    public static String getWeatherData() throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = URI.create(WEATHER_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        
        // Parse JSON đơn giản (chỉ lấy temp và description)
        try {
            String temp = result.toString().split("\"temp\":")[1].split(",")[0];
            String description = result.toString().split("\"description\":\"")[1].split("\"")[0];
            return "Hà Nội: " + Math.round(Double.parseDouble(temp)) + "°C, " + description;
        } catch (Exception e) {
            return "Không thể lấy dữ liệu thời tiết";
        }
    }

    // Lấy dữ liệu tin tức
    public static String getNewsData() throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = URI.create(NEWS_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        
        // Parse JSON đơn giản (lấy title đầu tiên)
        try {
            String title = result.toString().split("\"title\":\"")[1].split("\"")[0];
            return title.length() > 100 ? title.substring(0, 97) + "..." : title;
        } catch (Exception e) {
            return "Không thể lấy tin tức";
        }
    }
}