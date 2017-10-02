package com.amf.spotifylocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SpotifyDBus implements Spotify<String> {
    
    private static SpotifyDBus spotify;
    
    private SpotifyDBus() {}
    
    public static SpotifyDBus getInstance() {
        return spotify == null ? spotify = new SpotifyDBus() : spotify;
    }
    
    public String next() throws IOException {
        return send("Next");
    }
    
    public String pause() throws IOException {
        return send("Pause");
    }
    
    public String play() throws IOException {
        return send("Play");
    }
    
    public String play(String trackURI) throws IOException {
        return send("OpenUri", "string:" + trackURI);
    }
    
    public String previous() throws IOException {
        return send("Previous");
    }
    
    private String send(String action) throws IOException {
        return send(action, null);
    }
    
    private String send(String action, String parameter) throws IOException {
        ProcessBuilder builder;
        if (parameter == null) {
            builder = new ProcessBuilder("dbus-send", "--print-reply", "--dest=org.mpris.MediaPlayer2.spotify", "/org/mpris/MediaPlayer2", "org.mpris.MediaPlayer2.Player." + action);
        }
        else {
            builder = new ProcessBuilder("dbus-send", "--print-reply", "--dest=org.mpris.MediaPlayer2.spotify", "/org/mpris/MediaPlayer2", "org.mpris.MediaPlayer2.Player." + action, parameter);
        }
        Process process = builder.start();
        StringBuilder string = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                string.append(line);
            }
        }
        return string.toString();        
    }    
    
    public String togglePlay() throws IOException {
        return send("PlayPause");
    }
    
}