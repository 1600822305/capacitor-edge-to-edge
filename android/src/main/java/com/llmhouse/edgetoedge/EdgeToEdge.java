package com.llmhouse.edgetoedge;

import com.getcapacitor.Logger;

public class EdgeToEdge {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
