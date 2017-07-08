package com.knoxpo.photogallery.network;

import android.net.Uri;
import android.util.Log;

import com.knoxpo.photogallery.model.GalleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tejas Sherdiwala on 12/1/2016.
 * &copy; Knoxpo
 */

public class FlickrFetch {
    private static final String
            TAG = FlickrFetch.class.getSimpleName(),
            API_KEY = "297c4368dfa610682c7fc205a8f6ebbb",
            FETCH_RECENTS_METHOD="flickr.photos.getRecent",
            SEARCH_METHOD="flickr.photos.search";
    private static final Uri ENDPOINT = Uri
                .parse("https://api.flickr.com/services/rest/")
                .buildUpon()
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("format","json")
                .appendQueryParameter("nojsoncallback","1")
                .appendQueryParameter("extras","url_s")
                .build();



    public byte[] getUrlBytes(String urlSpec)throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in  = connection.getInputStream();
            if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + " :with " + urlSpec);
            }
            int readBytes = 0;
            byte buffer[] = new byte[1024];
            while ((readBytes = in.read(buffer))>0){
                    out.write(buffer,0,readBytes);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec ) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url = builtUrl(FETCH_RECENTS_METHOD,null);
        return downloadGalleyItems(url);
    }
    public List<GalleryItem> searchPhotos(String query){
        String url = builtUrl(SEARCH_METHOD,query);
        return downloadGalleyItems(url);
    }
    private String builtUrl(String method,String query){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method",method);
        if(method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);
        }

        return uriBuilder.build().toString();

    }
    private List<GalleryItem> downloadGalleyItems(String url){
        List<GalleryItem> items = new ArrayList<>();
        try{

            String jSonString = getUrlString(url);
            Log.d(TAG, "Response: "+jSonString);
            JSONObject jsonObject = new JSONObject(jSonString);
            parseItem(items,jsonObject);

        }catch (IOException e){
            Log.e(TAG,"Fail to Fetch Items",e);
        }catch (JSONException e){
            Log.e(TAG,"Fail to parse Json",e);
        }
        return items;
    }

    private void parseItem(List<GalleryItem> items,JSONObject jsonBody)throws IOException,JSONException{
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i=0;i<photoJsonArray.length();i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }

    }
}
