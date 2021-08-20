package com.example.pictionarie;

import com.example.pictionarie.model.Messages;
import com.example.pictionarie.model.Player;
import com.example.pictionarie.model.Score;
import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Server {

    //Drawer Events
    public static String DRAWER_CLEAR = "DrawerClear";
    public static String DRAWER_UNDO = "DrawerUndo";
    public static String DRAWER_HEIGHT = "DrawerHeight";
    public static String DRAWER_WIDTH  = "DrawerWidth";
    public static String DRAWER_CURRENT_SEG_VALUES = "DrawerCurrentSegValues";
    public static String DRAWER_CURRENT_SEG_POINTS = "DrawerCurrentSegPoints";
    public static String DRAWER_SEGMENTS = "DrawerSegments";
    public static String DRAWER_CLEAR_CURRENT_POINTS = "DrawerClearCurrentPoints";

    //Receiver Events
    public static String RECEIVER_CLEAR = "ReceiverClear";
    public static String RECEIVER_UNDO = "ReceiverUndo";
    public static String RECEIVER_HEIGHT = "ReceiverHeight";
    public static String RECEIVER_WIDTH = "ReceiverWidth";
    public static String RECEIVER_CURRENT_SEG_VALUES = "ReceiverCurrentSegValues";
    public static String RECEIVER_CURRENT_SEG_POINTS = "ReceiverCurrentSegPoints";
    public static String RECEIVER_SEGMENTS = "ReceiverSegments";
    public static String RECEIVER_CLEAR_CURRENT_POINTS = "ReceiverClearCurrentPoints";


    public static String RENEW_BOARD = "RenewBoard";


    public static String URL = "http://192.168.0.105:3035/";
    public static Socket socket = init();
    public static List<Player> allPlayerList = new ArrayList<>();
    public static Gson gson = new Gson();
    public static String serverCode;
    public static List<Messages> messagesList = new ArrayList<>();
    public static int CODE_LENGTH = 6;
    public static List<Score> totalScores = new ArrayList<>();

    private static Socket init() {
        try {
            return IO.socket(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return  null;
        }
    }


    public static Player player;








}
