package com.williamsportsda.williamsport.extras;



import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public class FileIntentService extends IntentService {
	
	public static final String PARAM_FILE = "FILE";
	public static final String PARAM_URL = "URL";
	public static final String DOWNLOAD_SUCCESS = "SUCCESS";	
    public static final int DOWNLOAD_COMPLETE = 1000;
    
    public FileIntentService() {
        super("FileIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
    	
        String urlToDownload = intent.getStringExtra(PARAM_URL);
        String file = intent.getStringExtra(PARAM_FILE);
        
        Boolean downloadSuccess = true;
        
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        try {
            URL url = new URL("http://www.williamsportsda.com/" + urlToDownload);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/pdf");

            connection.setDoInput(true);                      
            
            connection.connect();
            InputStream input = connection.getInputStream();
            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {            	
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
        	downloadSuccess = false;
        }

        Bundle result = new Bundle();
        result.putBoolean(DOWNLOAD_SUCCESS ,downloadSuccess);
        result.putString(PARAM_FILE ,file);
        receiver.send(DOWNLOAD_COMPLETE, result);
    }
}
