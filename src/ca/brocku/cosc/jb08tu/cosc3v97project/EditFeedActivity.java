package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditFeedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId		= "";
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_feed);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed id
			feedId = bundle.getString(Feeds._ID);
			Feed feed = mDatabase.getFeed(mDB, feedId);
			
			// update interface
			setTitle("Edit " + feed.getName());
			
			// get EditText
			final EditText txtName = (EditText)findViewById(R.id.editTextName);
			final EditText txtURL = (EditText)findViewById(R.id.editTextURL);
			
			// set EditText
			txtName.setText(feed.getName());
			txtURL.setText(feed.getURL());
		}
		
		if(bundle != null) {
			final Button btnEditFeed = (Button)findViewById(R.id.buttonEditFeed);
			btnEditFeed.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// get EditText
					final EditText txtName = (EditText)findViewById(R.id.editTextName);
					final EditText txtURL = (EditText)findViewById(R.id.editTextURL);
					
					// get EditText values
					String name = txtName.getText().toString();
					String url = txtURL.getText().toString();
					
					// update database
					mDatabase.editFeed(mDB, feedId, name, url);
					
					// return to feed activity
					finish();
				}
			});
		}
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_edit_feed, menu);
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
}
