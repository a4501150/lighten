package com.frostflames.lighten;

import com.frostflames.lighten.websocket.LightenWebSocketClient;

import java.time.Duration;

public class App {
    public static void main(String[] args) {

        Duration duration = Duration.ofMinutes(1);
        LightenWebSocketClient lightenWebSocketClient
                = new LightenWebSocketClient("wss://stream.binance.com:9443", duration);
    }
}
