package net.kore.managers;

import net.kore.Kore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class LicenseManager {
    private boolean isPremium = false;
    public LicenseManager() {
        if(Kore.mc.thePlayer != null && this.checkLicense(Kore.mc.thePlayer.getUniqueID().toString())) {
            Kore.sendMessageWithPrefix("You successfully authenticated to Kore (Premium)");
            isPremium = true;
        } else {
            Kore.sendMessageWithPrefix("Looks like you are not premium. You should consider upgrading to premium for the best features.");
        }

        Kore.configManager.reloadConfig(isPremium);
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void disconnect() {
        this.isPremium = false;
    }

    public boolean checkLicense(String uuid) {
        try {
            URL url = new URL("https://kore.valekatoz.com/api/checkLicense.php?key="+ Base64.getEncoder().encodeToString(uuid.getBytes()));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Kore/"+ Kore.VERSION); // custom UserAgent to skip cloudflare managed challenge
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            if(Kore.clientSettings.debug.isEnabled()) {
                System.out.println("Response Code: " + connection.getResponseCode());
                System.out.println("Response Data: " + response.toString());
            }

            connection.disconnect();

            return parseJsonResponse(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean parseJsonResponse(String jsonResponse) {
        try {
            int statusIndex = jsonResponse.indexOf("\"status\":\"");

            if (statusIndex != -1) {
                int start = statusIndex + "\"status\":\"".length();
                int end = jsonResponse.indexOf("\"", start);
                String status = jsonResponse.substring(start, end);

                return "success".equalsIgnoreCase(status);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
