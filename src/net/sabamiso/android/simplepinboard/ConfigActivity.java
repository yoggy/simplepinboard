package net.sabamiso.android.simplepinboard;

import net.sabamiso.android.simplepinboard.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigActivity extends Activity {
	
	SharedPreferences pref;
    EditText editUsername;
    EditText editPassword;
    
    String url;
    String title;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
        
        Intent intent = getIntent();
        url = intent.getStringExtra(Intent.EXTRA_TEXT);
        title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        
        pref = getSharedPreferences("pref",MODE_PRIVATE);
        
        editUsername = (EditText)findViewById(R.id.editUsername);
        editPassword = (EditText)findViewById(R.id.editPassword);
        
        // 
        pref.getString("password", "");

        editUsername.setText(pref.getString("username", ""));
        editPassword.setText(pref.getString("password", ""));

        Button buttonOK = (Button) findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	SharedPreferences.Editor e = pref.edit();
            	e.putString("username", editUsername.getText().toString());
            	e.putString("password", editPassword.getText().toString());
            	e.commit();
            	
                Toast.makeText(ConfigActivity.this, "Save Configuration",Toast.LENGTH_LONG).show();
                
                if (url != null) {
                	Intent intent = new Intent(ConfigActivity.this, SimplePinboardActivity.class);
                	intent.setAction(Intent.ACTION_VIEW);
                	intent.putExtra(Intent.EXTRA_TEXT, url);
                	intent.putExtra(Intent.EXTRA_SUBJECT, title);
                	startActivity(intent);
                }
                else {
                	ConfigActivity.this.finish();
                }
            }
        });

        Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Toast.makeText(ConfigActivity.this, "Configuration Canceled...", Toast.LENGTH_LONG).show();
                ConfigActivity.this.finish();
            }
        });
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	//Toast.makeText(this, "Configuration Canceled...", Toast.LENGTH_SHORT).show();
    	this.finish();
    }
}