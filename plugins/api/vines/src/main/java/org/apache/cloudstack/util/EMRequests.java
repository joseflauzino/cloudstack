package org.apache.cloudstack.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class EMRequests {

    public String sendRequest(String vnfIp, String function, String httpMethod) {
        // String urlStr = "http://"+vnfIp+":8000/click_plugin/"+function;
        String urlStr = "http://192.168.122.10:9000/em/" + function;
        StringBuffer response = new StringBuffer();
        try {
            URL urlObj = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            con.setRequestMethod(httpMethod);
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");

            con.setDoOutput(true);
            String urlParameters = "{\"vnf_ip\":\"" + vnfIp + "\"}";
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("Sending " + httpMethod + " request to URL: " + urlStr);
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();

            System.out.println("RESPONSE: " + response.toString());
        } catch (MalformedURLException e) {
            // erro ao criar urlObj
            e.printStackTrace();
        } catch (IOException e) {
            // erro ao criar con
            e.printStackTrace();
        }

        return response.toString();
    }
}