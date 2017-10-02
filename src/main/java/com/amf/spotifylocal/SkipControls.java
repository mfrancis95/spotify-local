package com.amf.spotifylocal;

import java.io.IOException;

public interface SkipControls<T> {
    
    T next() throws IOException;
    
    T previous() throws IOException;
    
}