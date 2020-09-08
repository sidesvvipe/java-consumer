package com.v.consumer;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
public class ConsumerController {

    public String PROVIDER_HOST = "localhost";
    public String PROVIDER_PORT = "8082";

    public ConsumerController() {
    }

    @RequestMapping(method = RequestMethod.GET,
            value = "check-input")
    public String checkInput() {
        String provider2Call = String.format("http://%s:%s/accounts/%s", PROVIDER_HOST, "8089", "1");
        // redundant call but lets assume we need it
        JSONObject p2 = new JSONObject(getUrl(provider2Call));
        String url = String.format("http://%s:%s/accounts/num/%s", PROVIDER_HOST, PROVIDER_PORT, p2.getString("account_number"));
        JSONObject myResponse = new JSONObject(getUrl(url));
        return String.format("%s's account is %sactive", myResponse.getString("name"), myResponse.getBoolean("active") ? "" : "not ");
    }

    String getUrl(String url) {
        URL obj = null;
        try {
            obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //print in String
            System.out.println(response.toString());
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}

