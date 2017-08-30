package com.amf.spotifylocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Spotify {
    
    private static final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    
    private String csrf, oAuth, url;
    
    private Date expiration;
    
    private int port;
    
    public Spotify() throws IOException {
        this("localhost");
    }
    
    public Spotify(String host) throws IOException {
        this(host, findPort(host, 4380, 4391));
    }
    
    public Spotify(String host, int port) throws IOException {
        this.port = port;
        url = "http://" + host + ":" + port;
        refreshOAuth();
        csrf = request("/simplecsrf/token.json?").getString("token");
    }
    
    public Spotify(String host, int portStart, int portEnd) throws IOException {
        this(host, findPort(host, portStart, portEnd));
    }
    
    private String buildURL(String route) {
        StringBuilder url = new StringBuilder(this.url).append(route).append("oauth=").append(oAuth);
        if (csrf != null) {
            url.append("&csrf=").append(csrf);
        }
        return url.toString();
    }
    
    public String getCSRF() {
        return csrf;
    }
    
    public long getExpiration() {
        return expiration.getTime();
    }
    
    public String getOAuth() {
        return oAuth;
    }
    
    public int getPort() {
        return port;
    }
    
    public JSONObject pause() throws IOException {
        return request("/remote/pause.json?pause=true&");
    }
    
    public JSONObject play() throws IOException {
        return request("/remote/pause.json?pause=false&");
    }
    
    public JSONObject play(String trackURI) throws IOException {
        return play(trackURI, trackURI);
    }
    
    public JSONObject play(String trackURI, String contextURI) throws IOException {
        return request(String.format("/remote/play.json?uri=%s&context=%s&", trackURI, contextURI));
    }
    
    private JSONObject readJSON(HttpURLConnection connection) throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        return (JSONObject) new JSONTokener(json.toString()).nextValue();
    }
    
    private void refreshOAuth() throws IOException {
        URL url = new URL("https://open.spotify.com/token");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        try {
            expiration = dateFormat.parse(connection.getHeaderField("Expires"));
        }
        catch (ParseException ex) {}
        oAuth = readJSON(connection).getString("t");
    }
    
    protected JSONObject request(String route) throws IOException {
        if (new Date().after(expiration)) {
            refreshOAuth();
        }
        URL url = new URL(buildURL(route));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Origin", "https://open.spotify.com");
        connection.connect();
        return readJSON(connection);
    }
    
    public JSONObject status() throws IOException {
        return request("/remote/status.json?");        
    }
    
    public JSONObject togglePlay() throws IOException {
        return status().getBoolean("playing") ? pause() : play();
    }
    
    public JSONObject version(String service) throws IOException {
        return request(String.format("/service/version.json?service=%s&", service));
    }
    
    private static int findPort(String host, int portStart, int portEnd) throws IOException {
        for (int port = portStart; port < portEnd; port++) {
            try (Socket socket = new Socket(host, port)) {
                return port;
            }
            catch (IOException ex) {
                if (ex instanceof UnknownHostException) {
                    throw ex;
                }
            }
        }
        throw new IOException("No port found.");
    }
    
}