package ca.brocku.cosc.jb08tu.cosc3v97project;

import ca.brocku.cosc.jb08tu.cosc3v97project.FeedDatabase.Feeds;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class FeedActivity extends Activity {
	protected FeedDatabaseHelper	mDatabase	= null;
	protected Cursor				mCursor		= null;
	protected SQLiteDatabase		mDB			= null;
	private static String			feedId		= "";
	private static Feed				feed		= null;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		
		mDatabase = new FeedDatabaseHelper(this.getApplicationContext());
		mDB = mDatabase.getWritableDatabase();
	}
	
	@Override public void onStart() {
		super.onStart();
		displayFeedItems();
	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_feed, menu);
		
		// edit feed
		Bundle bundle = this.getIntent().getExtras();
		Intent intent = new Intent(this, EditFeedActivity.class);
		intent.putExtras(bundle);
		Utilities.setIntentOnMenuItem(menu, R.id.menu_edit_feed, intent);
		
		// delete feed
		MenuItem menuItem = menu.findItem(R.id.menu_unsubscribe);
		menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override public boolean onMenuItemClick(MenuItem item) {
				Builder dialog = new AlertDialog.Builder(FeedActivity.this);
				dialog.setMessage("Unsubscribe from " + feed.getName() + "?");
				dialog.setNegativeButton(R.string.button_cancel, null);
				dialog.setPositiveButton(R.string.button_unsubscribe, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mDatabase.deleteFeed(mDB, "" + feedId);
						Intent intent = new Intent(FeedActivity.this, MainActivity.class);
						startActivityForResult(intent, 0);
					}
				});
				dialog.show();
				return true;
			}
		});
		
		return true;
	}
	
	@Override protected void onDestroy() {
		super.onDestroy();
		if(mCursor != null) {
			mCursor.close();
		}
		if(mDB != null) {
			mDB.close();
		}
		if(mDatabase != null) {
			mDatabase.close();
		}
	}
	
	private void displayFeedItems() {
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			// get feed
			feedId = bundle.getString("id");
			// String numItems = bundle.getString("numItems");
			feed = mDatabase.getFeed(mDB, feedId);
			
			// update interface
			// if(numItems != null) {
			// setTitle(feed.getName());
			// }
			// else {
			// setTitle(feed.getName() + " (" + numItems + ")");
			// }
			
			// check for network connection
			if(Utilities.hasNetworkConnection(this.getApplicationContext())) {
				Utilities.downloadNewFeedItems(this.getApplicationContext(), mDatabase, mDB, feed);
			}
			else {
				returnToMain();
			}
			
			// load all feed items
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(Feeds.FEED_ITEMS_TABLE_NAME);
			String columns[] = {Feeds._ID, Feeds.FEED_ITEM_TITLE, Feeds.FEED_ITEM_PUB_DATE};
			mCursor = queryBuilder.query(mDB, columns, Feeds.FEED_ITEM_FEED_ID + "=? AND " + Feeds.FEED_ITEM_IS_READ + "=?", new String[] {feed.getId(), "0"}, null, null, Feeds.FEED_ITEMS_DEFAULT_SORT_ORDER);
			Utilities.loadFeedItemsFromDatabase(this, mDatabase, mDB, mCursor, feed);
			mCursor.close();
			
			final ListView lstFeedItems = (ListView)findViewById(R.id.listViewFeedItems);
			int count = lstFeedItems.getCount();
			String numItems = "" + count;
			numItems += " item";
			if(count != 1) {
				numItems += "s";
			}
			setTitle(feed.getName() + " (" + numItems + ")");
		}
	}
	
	private void returnToMain() {
		Builder dialog = new AlertDialog.Builder(FeedActivity.this);
		dialog.setMessage(R.string.message_no_network);
		dialog.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent = new Intent(FeedActivity.this, MainActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		dialog.show();
	}
}
