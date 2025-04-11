package personal.carl.thronson.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class HttpUtils {

    public static JsonNode get(URL url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
//        con.setDoOutput(true);
        con.setReadTimeout(100000);
        ObjectMapper mapper = new ObjectMapper();
//        OutputStream out = new ByteArrayOutputStream();
//        System.out.println(jsonString);
//        try (OutputStream os = con.getOutputStream()) {
//            byte[] input = jsonString.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
//            System.out.println(response.toString());
            ObjectReader rdr = mapper.reader();
            JsonNode tree = rdr.readTree(response.toString());
//            JsonNode id = tree.get("id");
//            return id.asLong();
            return tree;
        }
    }

    public static JsonNode post(String jsonString, URL url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setReadTimeout(10000);
        ObjectMapper mapper = new ObjectMapper();
//        OutputStream out = new ByteArrayOutputStream();
        System.out.println(jsonString);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
//            System.out.println(response.toString());
            ObjectReader rdr = mapper.reader();
            JsonNode tree = rdr.readTree(response.toString());
            return tree;
        }
    }

    public static URL connect(URI uri) throws IOException, MalformedURLException {
        URL obj = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setReadTimeout(60000);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent", "Mozilla");
        conn.addRequestProperty("Referer", "google.com");

        System.out.println("Request URL ... " + obj);

        boolean redirect = false;

        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }

        if (status == 429) {
            System.out.println("Response Code ... " + status);
            for (Entry entry: conn.getHeaderFields().entrySet()) {
                System.out.println(entry);
            }
        }

        if (redirect) {

            // get redirect url from "location" header field
            String newUrl = conn.getHeaderField("Location");

            // get the cookie if need, for login
            String cookies = conn.getHeaderField("Set-Cookie");

            // open the new connnection again
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", cookies);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");

//            System.out.println("Redirect to URL : " + newUrl);

            obj = new URL(newUrl);
        }
        return obj;
    }

    public static String delete(URL url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
//        con.setDoOutput(true);
        con.setReadTimeout(10000);
        ObjectMapper mapper = new ObjectMapper();
//        OutputStream out = new ByteArrayOutputStream();
//        System.out.println(jsonString);
//        try (OutputStream os = con.getOutputStream()) {
//            byte[] input = jsonString.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
//            System.out.println(response.toString());
            return response.toString();
//            ObjectReader rdr = mapper.reader();
//            JsonNode tree = rdr.readTree(response.toString());
//            JsonNode id = tree.get("id");
//            return id.asLong();
//            return tree;
        }
    }

    public static JsonNode put(String jsonString, URL url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setReadTimeout(10000);
        ObjectMapper mapper = new ObjectMapper();
//        OutputStream out = new ByteArrayOutputStream();
//        System.out.println(jsonString);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
//            System.out.println(response.toString());
            ObjectReader rdr = mapper.reader();
            JsonNode tree = rdr.readTree(response.toString());
            return tree;
        }
    }
}
