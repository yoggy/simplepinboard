package net.sabamiso.android.simplepinboard;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

import net.sabamiso.android.simplepinboard.R;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SimplePinboardActivity extends Activity implements Runnable {
	
	// API specification:
	//   http://pinboard.in/api/
	// example:
	//   https://api.pinboard.in/v1/posts/add?url=...&description=...&title=...
	//   use GET method & Basic Authentication
	final String pinboard_base_url = "https://api.pinboard.in/v1/posts/add";
	
	SharedPreferences pref;
    EditText editURL;
    EditText editTitle;
    String username;
    String password;
    String url;
    String title;
    
    ProgressDialog progress;  
    Thread thread;
    Handler finish_handler; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        editURL = (EditText)findViewById(R.id.editURL);
        editTitle = (EditText)findViewById(R.id.editTitle);

        Intent intent = getIntent();
    	title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
    	if (title != null) editTitle.setText(title);
    	
    	url = intent.getStringExtra(Intent.EXTRA_TEXT);
    	if (url != null) editURL.setText(url);

        pref = getSharedPreferences("pref",MODE_PRIVATE);
        username = pref.getString("username", "");
        password = pref.getString("password", "");
        if (username == "") {
        	Intent config_intent = new Intent(this, ConfigActivity.class);
        	config_intent.setAction(Intent.ACTION_VIEW);
        	config_intent.putExtra(Intent.EXTRA_TEXT, url);
        	config_intent.putExtra(Intent.EXTRA_SUBJECT, title);
        	startActivity(config_intent);
        	return;
        }
        
        Button buttonPost = (Button) findViewById(R.id.buttonPost);
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	startPost();
            }
        });
        
        Button buttonCancelPost = (Button) findViewById(R.id.buttonCancelPost);
        buttonCancelPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	SimplePinboardActivity.this.finish();
            }
        });
        
        finish_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	int code = msg.getData().getInt("code");
            	finishPost(code);
            }
        };
        
        //
        startPost();
    }
    
	@Override
    public void onPause() {
    	super.onPause();
    	this.finish();
    }

    void startPost() {
    	url   = editURL.getText().toString();
    	title = editTitle.getText().toString();
    	
    	progress = new ProgressDialog(this);
    	progress.setTitle("Simple Pinboard");
    	progress.setMessage("Post to Pinboard.in...");
    	progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progress.setCancelable(true);  
    	progress.setOnCancelListener(new DialogInterface.OnCancelListener() {  
          public void onCancel(DialogInterface dialog) {  
            finishPost(-1);  
          }  
        });
    	progress.show();
    	
    	thread = new Thread(this);
    	thread.start();
	}

	@Override
	public void run() {
		int code = do_http_access(url, title);
		thread = null;
		send_finish_message(code);
	}
	
    int do_http_access(String url, String title) {
    	String get_url = pinboard_base_url;
    	get_url += "?url=";
    	get_url += URLEncoder.encode(url);
    	get_url += "&description=";
    	get_url += URLEncoder.encode(title);
    	
    	//
    	DefaultHttpClient http_client = new DefaultHttpClient();  
        HttpParams params = http_client.getParams();  
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        HttpConnectionParams.setSoTimeout(params, 5000);

        HttpGet http_get = new HttpGet(get_url);  
        URI get_uri = http_get.getURI();
        
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(username, password);
        AuthScope scope = new AuthScope(get_uri.getHost(), get_uri.getPort());
        http_client.getCredentialsProvider().setCredentials(scope, credential);
        
        int code = -1;
        try {  
            HttpResponse res = http_client.execute(http_get);  
            code = res.getStatusLine().getStatusCode();
        } catch (IOException e) {  
        	Toast.makeText(SimplePinboardActivity.this, "post error..."+e.toString(),Toast.LENGTH_LONG).show();
        	code = -2;
        }     
        return code;
    }
	
	void send_finish_message(int code) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt("code", code);
        msg.setData(b);
        finish_handler.sendMessage(msg);
    }
	
    void finishPost(int code){
        progress.dismiss();
        
        if (code == -1) {
        	Toast.makeText(SimplePinboardActivity.this, "Canceled...",Toast.LENGTH_LONG).show();
        }
        else if (code == -2) {
        	// IOException...
        }
        else if (code != 200) {
        	Toast.makeText(SimplePinboardActivity.this, "HTTP Status Error...status=" + code,Toast.LENGTH_LONG).show();
        }
        else {
        	Toast.makeText(SimplePinboardActivity.this, "POST Success",Toast.LENGTH_LONG).show();
            SimplePinboardActivity.this.finish();
        }
    }
}