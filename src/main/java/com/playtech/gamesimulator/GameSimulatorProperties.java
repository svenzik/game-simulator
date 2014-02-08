package com.playtech.gamesimulator;


import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

public class GameSimulatorProperties {

    private static ResourceBundle rb = ResourceBundle.getBundle("GameSimulator");

    private static GameSimulatorProperties ourInstance = new GameSimulatorProperties();

    public static String get(String property){
        return getInstance().getProperty(property);
    }

    private String getProperty(String property) {
        return rb.getString(property);
    }

    private static GameSimulatorProperties getInstance() {
        return ourInstance;
    }
}
