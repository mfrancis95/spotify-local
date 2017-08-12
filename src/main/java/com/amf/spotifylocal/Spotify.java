package com.amf.spotifylocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Spotify {
    
    private String csrf, oAuth, url;
    
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
        URL url = new URL("https://open.spotify.com/token");
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        JSONObject object = (JSONObject) new JSONTokener(json.toString()).nextValue();
        oAuth = object.getString("t");
        csrf = request("/simplecsrf/token.json").getString("token");
    }
    
    public Spotify(String host, int portStart, int portEnd) throws IOException {
        this(host, findPort(host, portStart, portEnd));
    }
    
    public String getCSRF() {
        return csrf;
    }
    
    public String getOAuth() {
        return oAuth;
    }
    
    public int getPort() {
        return port;
    }
    
    public JSONObject pause() throws IOException {
        return request(String.format("/remote/pause.json?oauth=%s&csrf=%s&pause=true", oAuth, csrf));
    }
    
    public JSONObject play() throws IOException {
        return request(String.format("/remote/pause.json?oauth=%s&csrf=%s&pause=false", oAuth, csrf));
    }
    
    public JSONObject play(String trackURI) throws IOException {
        return play(trackURI, trackURI);
    }
    
    public JSONObject play(String trackURI, String contextURI) throws IOException {
        return request(String.format("/remote/play.json?oauth=%s&csrf=%s&uri=%s&context=%s", oAuth, csrf, trackURI, contextURI));
    }
    
    private JSONObject request(String path) throws IOException {
        URL url = new URL(this.url + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Origin", "https://open.spotify.com");
        connection.connect();
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }
        return (JSONObject) new JSONTokener(json.toString()).nextValue();
    }
    
    public JSONObject status() throws IOException {
        return request(String.format("/remote/status.json?oauth=%s&csrf=%s", oAuth, csrf));        
    }
    
    public JSONObject togglePlay() throws IOException {
        return status().getBoolean("playing") ? pause() : play();
    }
    
    public JSONObject version(String service) throws IOException {
        return request("/service/version.json?service=" + service);
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