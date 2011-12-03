package dk.nindroid.rss.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import dk.nindroid.rss.R;

public class FeedSettings extends Activity{
	public static final String FEED_ID = "feed_id";
	
	CheckBox mActive;
	EditText mTitle;
	EditText mExtra;
	Spinner mSorting;
	int mId;
	FeedsDbAdapter mDb;
	
	String mTitleString;
	String mExtraString;
	
	String mSharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSharedPreferences = this.getIntent().getExtras().getString(ManageFeeds.SHARED_PREFS_NAME);
		
		setContentView(R.layout.feed_settings);
		mActive = (CheckBox)findViewById(R.id.active);
		mTitle = (EditText)findViewById(R.id.title);
		mExtra = (EditText)findViewById(R.id.extra);
		mSorting = (Spinner)findViewById(R.id.sortOrder);
		
		mId = getIntent().getIntExtra(FEED_ID, -1);
		
		mDb = new FeedsDbAdapter(this).open();
		setData();
		mDb.close();
		
		boolean local = true;
		int sortId = local ? R.array.sortOrderFiles : R.array.sortOrderOnline;
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, sortId, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mSorting.setAdapter(adapter);
	    
	    ((Button)findViewById(R.id.ok)).setOnClickListener(new OkClicked());
	    ((Button)findViewById(R.id.cancel)).setOnClickListener(new CancelClicked());
	}
		
	void setData(){
		Cursor c = mDb.fetchFeed(mId);
		if(c.moveToFirst()){
			int iTitle = c.getColumnIndex(FeedsDbAdapter.KEY_TITLE);
			int iExtra = c.getColumnIndex(FeedsDbAdapter.KEY_EXTRA);
			int iSorting = c.getColumnIndex(FeedsDbAdapter.KEY_SORTING);
			int iUserTitle = c.getColumnIndex(FeedsDbAdapter.KEY_USER_TITLE);
			int iUserExtra = c.getColumnIndex(FeedsDbAdapter.KEY_USER_EXTRA);
			
			this.mTitleString = c.getString(iTitle);
			this.mExtraString = c.getString(iExtra);
			int sorting = c.getInt(iSorting);
			String uTitle = c.getString(iUserTitle);
			String uExtra = c.getString(iUserExtra);
			SharedPreferences sp = getSharedPreferences(mSharedPreferences, 0);
			boolean enabled = sp.getBoolean("feed_" + Integer.toString(mId), true);
			this.mActive.setChecked(enabled);
			
			this.mTitle.setHint(mTitleString);
			this.mExtra.setHint(mExtraString);
			
			if(empty(uTitle)){
				this.mTitle.setText(mTitleString);
			}else{
				this.mTitle.setText(uTitle);
			}
			if(empty(uExtra)){
				this.mExtra.setText(mExtraString);
			}else{
				this.mExtra.setText(uExtra);
			}
			
			mSorting.setSelection(sorting);
		}
	}
	
	boolean empty(String s){
		return s == null || s.length() == 0;
	}
	
	void saveFeed(){
		mDb.updateFeed(mId, mSorting.getSelectedItemPosition(), mTitle.getText().toString(), mExtra.getText().toString());
		Editor e = getSharedPreferences(mSharedPreferences, 0).edit();
		e.putBoolean("feed_" + mId, this.mActive.isChecked());
		e.commit();
	}
	
	private class OkClicked implements OnClickListener{
		@Override
		public void onClick(View v) {
			mDb.open();
			saveFeed();
			mDb.close();
			FeedSettings.this.finish();
		}
	}
	private class CancelClicked implements OnClickListener{
		@Override
		public void onClick(View v) {
			FeedSettings.this.finish();
		}
	}
}