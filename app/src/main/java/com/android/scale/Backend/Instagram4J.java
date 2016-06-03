package com.android.scale.Backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by mmadhusoodan on 4/15/16.
 */
public class Instagram4J {

    final private static Logger log = Logger.getLogger(Instagram4J.class);
    private static final String TAG = "Log-Instagram4J";
    private static final String ACCESS_TOKEN = "226613286.a60a41e.872ee12ae47243458045e72a9a08b009";

    public static ArrayList<Bitmap> getBitmapFromURLList(ArrayList<String> imgURLList) {
        try {
            ArrayList<Bitmap> bitmapList = new ArrayList<>();
            for (int i = 0; i < imgURLList.size(); i++) {
                URL url = new URL(imgURLList.get(i).toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(true);
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                bitmapList.add(myBitmap);
            }
            return bitmapList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String streamToString(InputStream is) throws IOException {
        String str = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }

    public ArrayList<Bitmap> getBitmapsFromTagSearch(String tag) {

        String URL = "https://api.instagram.com/v1/tags/" + tag + "/media/recent?access_token=" + ACCESS_TOKEN;
        ArrayList<Bitmap> bitmapList = new ArrayList<>();
        ArrayList<String> imgURLList = new ArrayList<>();
        try {
            URL url = new URL(URL);
            Log.d("getBitmapsFromTagSearch", "Opening URL " + url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            String response = streamToString(urlConnection.getInputStream());
            JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();

            JSONArray dataArray = (JSONArray) jsonObj.get("data");
            int n = dataArray.length();
            for (int i = 0; i < n; i++) {
                jsonObj = (JSONObject) dataArray.get(i);
                jsonObj = (JSONObject) jsonObj.get("images");
                jsonObj = (JSONObject) jsonObj.get("standard_resolution");
                String imgURL = jsonObj.get("url").toString();
                imgURLList.add(imgURL);
            }
            bitmapList = getBitmapFromURLList(imgURLList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bitmapList;
    }

    public ArrayList<String> getImageURLFromTagSearch(String tag) {

        String URL = "https://api.instagram.com/v1/tags/" + tag + "/media/recent?access_token=" + ACCESS_TOKEN;
        ArrayList<String> imgURLList = new ArrayList<>();
        try {
            URL url = new URL(URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            String response = streamToString(urlConnection.getInputStream());
            JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();

            JSONArray dataArray = (JSONArray) jsonObj.get("data");
            int n = dataArray.length();
            for (int i = 0; i < n; i++) {
                jsonObj = (JSONObject) dataArray.get(i);
                jsonObj = (JSONObject) jsonObj.get("images");
                jsonObj = (JSONObject) jsonObj.get("standard_resolution");
                String imgURL = jsonObj.get("url").toString();
                imgURLList.add(imgURL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return imgURLList;
    }
}