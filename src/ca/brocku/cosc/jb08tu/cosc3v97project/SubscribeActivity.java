package ca.brocku.cosc.jb08tu.cosc3v97project;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SubscribeActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subscribe);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
		displaySubscribe();
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_subscribe, menu);
		return true;
	}
	
	@Override protected void onDestroy() {
		super.onDestroy();
		if(mDB != null) {
			mDB.close();
		}
		if(mDatabase != null) {
			mDatabase.close();
		}
	}
	
	private void displaySubscribe() {
		final Button btnSubscribe = (Button)findViewById(R.id.buttonSubscribe);
		btnSubscribe.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// get EditText
				final EditText txtURL = (EditText)findViewById(R.id.editTextURL);
				
				// get EditText value
				String url = txtURL.getText().toString().trim();
				if(!url.equals("") && !url.startsWith("http")) {
					url = "http://" + url;
				}
				
				// check for valid URL
				url = Utilities.getValidURL(url);
				if(url.equals("")) {
					Builder dialog = new AlertDialog.Builder(SubscribeActivity.this);
					dialog.setMessage(R.string.message_invalid_rss);
					dialog.setPositiveButton(R.string.button_ok, null);
					dialog.show();
					return;
				}
				
				// get feed name
				String name = UtilitiesXML.getFeedTitle(url);
				
				// update database
				Feed feed = mDatabase.addFeed(mDB, name, url);
				
				if(Utilities.hasNetworkConnection(getApplicationContext())) {
					Utilities.downloadNewFeedItems(getApplicationContext(), mDatabase, mDB, feed);
				}
				
				// return to main activity
				Intent intent = new Intent(SubscribeActivity.this, MainActivity.class);
				startActivityForResult(intent, 0);
				finish();
			}
		});
	}
}
