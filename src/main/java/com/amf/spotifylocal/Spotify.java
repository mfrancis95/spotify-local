package com.amf.spotifylocal;

import java.io.IOException;

public interface Spotify<T> {
    
    T pause() throws IOException;
    
    T play() throws IOException;
    
    T play(String trackURI) throws IOException;
    
    T togglePlay() throws IOException;
    
}