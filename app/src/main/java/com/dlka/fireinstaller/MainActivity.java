package com.dlka.fireinstaller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends ListActivity implements
		OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener {

	private TemplateSource templateSource;
	private TemplateData template;

	public static final String PREFSFILE = "settings";
	private static final String ALWAYS_GOOGLE_PLAY = "always_link_to_google_play";
	private static final String TEMPLATEID = "templateid";
	public static final String SELECTED = "selected";
	private static final String APP_TAG = "com.dlka.fireinstaller";
	private static final String PROPERTY_ID = "App";
	private static final String TAG = "fireinstaller";
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
      }
    private AdView adView;

	public String fireip="";    
	
	private final String mailtag="0.7";
	
	public int count=1;

	// the helper object
	//IabHelper mHelper;
	
      HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
      

	@Override
	protected void onCreate(Bundle b) {
		  StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
		super.onCreate(b);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
		ListView listView = getListView();
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	  Tracker t = (getTracker(
            TrackerName.APP_TRACKER));

        t.setScreenName("MainView");

        t.send(new HitBuilders.AppViewBuilder().build());
        
        
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        Tracker t1 = analytics.newTracker(R.xml.global_tracker);
        t1.send(new HitBuilders.AppViewBuilder().build());
        
        // Create the adView.
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-8761501900041217/6245885681");
        adView.setAdSize(AdSize.BANNER);

        LinearLayout layout = (LinearLayout)findViewById(R.id.bannerLayout);

        layout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       
        .addTestDevice("89CADD0B4B609A30ABDCB7ED4E90A8DE")
        .addTestDevice("CCCBB7E354C2E6E64DB5A399A77298ED")  //current Nexus 4
        .build();
        
        adView.loadAd(adRequest);

        

//String base64EncodedPublicKey = getString(R.xml.app_license);
//Log.d(TAG, "Creating IAB helper.");
//mHelper = new IabHelper(this, base64EncodedPublicKey);
//mHelper.enableDebugLogging(false);

// Start setup. This is asynchronous and the specified listener
// will be called once setup completes.

//Log.d(TAG, "Starting setup.");

//mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
	//public void onIabSetupFinished(IabResult result) {
	//Log.d(TAG, "Setup finished.");
        //if (!result.isSuccess()) {
	// Oh noes, there was a problem.
        //toast("in app billing error: " + result);
        //return;
        //}
	// Have we been disposed of in the meantime? If so, quit.
        //if (mHelper == null)
        //return;
        //}	
        //});
        
        }

	 @Override
	  public void onDestroy() {
	    adView.destroy();
	    super.onDestroy();
	  }

	 
	@Override
	protected void onResume() {
		super.onResume();
		adView.resume();
		templateSource = new TemplateSource(this);
		templateSource.open();
		List<TemplateData> formats = templateSource.list();
		ArrayAdapter<TemplateData> adapter = new ArrayAdapter<TemplateData>(this,
				android.R.layout.simple_spinner_item, formats);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SharedPreferences prefs = getSharedPreferences(PREFSFILE, 0);
		Iterator<TemplateData> it = formats.iterator();
		while (it.hasNext()) {
			template = it.next();
			if (template.id == prefs.getLong(TEMPLATEID, 0)) {
				break;
			}
			template = null;
		}
		setListAdapter(new AppAdapter(this, R.layout.app_item,
				new ArrayList<SortablePackageInfo>(), R.layout.app_item));
		new ListTask(this, R.layout.app_item).execute("");
		
		final Button bs = (Button)findViewById(R.id.button2);
		bs.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
			copyMenuSelect();
			}
		});

	}

	
	
	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
		SharedPreferences.Editor editor = getSharedPreferences(PREFSFILE, 0).edit();
		editor.putBoolean(ALWAYS_GOOGLE_PLAY,true);
		//TODO editor.sharedprefs for saving/reading ip, make default ip go there if empty first
		if (template != null) {
			editor.putLong(TEMPLATEID, template.id);
		}
		ListAdapter adapter = getListAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
			editor.putBoolean(SELECTED + "." + spi.packageName, spi.selected);
		}
		editor.commit();
	}

	@Override
	protected void onStart()
	{
	super.onStart();
		
	 final Dialog dialog = new Dialog(this);
	    dialog.setContentView(R.layout.dialog);
	    dialog.setTitle("Hello");

	    Button button = (Button) dialog.findViewById(R.id.Button01);
	    button.setOnClickListener(new OnClickListener() {  
	        @Override  
	        public void onClick(View view) {  
	            dialog.dismiss();            
	        }  
	    });

	    dialog.show();
	    
	}
	@Override
	protected void onStop()
	{
	super.onStop();	
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

	    View v = getCurrentFocus();
	    boolean ret = super.dispatchTouchEvent(event);

	    if (v instanceof EditText) {
	        View w = getCurrentFocus();
	        int scrcoords[] = new int[2];
	        w.getLocationOnScreen(scrcoords);
	        float x = event.getRawX() + w.getLeft() - scrcoords[0];
	        float y = event.getRawY() + w.getTop() - scrcoords[1];

	      //  Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
	        if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 

	            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
	        }
	    }
	return ret;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.copy: { 
				
			copyMenuSelect();	
				break;
			}
			case (R.id.deselect_all): {
				ListAdapter adapter = getListAdapter();
				int count = adapter.getCount();
				for (int i = 0; i < count; i++) {
					SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
					spi.selected = false;
				}
				((AppAdapter) adapter).notifyDataSetChanged();
				break;
			}
			case (R.id.select_all): {
				ListAdapter adapter = getListAdapter();
				int count = adapter.getCount();
				for (int i = 0; i < count; i++) {
					SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
					spi.selected = true;
				}
				((AppAdapter) adapter).notifyDataSetChanged();
				break;
			}
			case (R.id.item_help): {
				//TODO implement help screen

				 final Dialog dialog = new Dialog(this);
				    dialog.setContentView(R.layout.dialog);
				    dialog.setTitle("Hello");

				    Button button = (Button) dialog.findViewById(R.id.Button01);
				    button.setOnClickListener(new OnClickListener() {  
				        @Override  
				        public void onClick(View view) {  
				            dialog.dismiss();            
				        }  
				    });

				    dialog.show();
				break;
			} 
			case (R.id.item_mail):{
				StringBuffer buffer = new StringBuffer();
			    buffer.append("mailto:");
			    buffer.append("feedback@kulsch-it.de");
			    buffer.append("?subject=");
			    buffer.append("Fireinstaller"+mailtag);
			    buffer.append("&body=Please provide Androidversion and Device Model for Bugreport if you know it.");
			    String uriString = buffer.toString().replace(" ", "%20");

			    startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), "Contact Developer"));
			    break;
			}
			case (R.id.item_donate): {
				//TODO open donate-link or even in-app purchase

				Toast.makeText(this, "Isn't implemented yet. But i'd be happy if you just buy any of my apps @ google play", Toast.LENGTH_LONG).show();
			
				break;
			} 
			case (R.id.item_settings): {
				//TODO implement settings screen for setting ip, checkbox for auto-connect adb on app-startup, checkbox disable ads..

				Toast.makeText(this, "Coming soon.", Toast.LENGTH_LONG).show();
			
				break;
			} 
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private void copyMenuSelect() {
		
		if (!isNothingSelected()) {
			fireip =  ((EditText) findViewById(R.id.editText1)).getText().toString().trim();
			Toast.makeText(this, "Installing at IP"+fireip, Toast.LENGTH_LONG).show();
			Log.d("Fireinstaller","IP ausgelesen:"+fireip);
        
			//CharSequence buf = pushFireTv();
	        pushFireTv();//TODO make background/async task

	
	        
	        
	        //String qry=buf.toString(); // TODO: Save IP into prefs or file 
		}
		else{Toast.makeText(this, "no app selected", Toast.LENGTH_LONG).show();}
	}
	
	
	private void pushFireTv() {

//		StringBuilder ret = new StringBuilder();
		ListAdapter adapter = getListAdapter();
		int count = adapter.getCount();

		
		Log.d("Fireinstaller2", "connecting adb to "+fireip);
		Process adb = null;
		try {
			adb = Runtime.getRuntime().exec("sh");
			
		} catch (IOException e1) {
			Log.e("Fireinstaller", "error");
		}
		
		DataOutputStream outputStream = new DataOutputStream(adb.getOutputStream());
		try {
			outputStream.writeBytes("/system/bin/adb" +" connect "+fireip+"\n ");
			outputStream.flush();
			Log.d("fireinstaller", "/system/bin/adb" +" connect "+fireip+"\n ");

		} catch (IOException e1) {
			Log.e("Fireinstaller", "error");
		}

		
		for (int i = 0; i < count; i++) {
			SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
			if (spi.selected) {
				Log.d("Fireinstaller2", " ret.append package: " + spi.packageName + ", sourceDir: " + spi.sourceDir);
				
	//			ret.append(i+" ");

				Toast.makeText(this, "Pushing now()"+spi.displayName, Toast.LENGTH_LONG).show();
				
				
				        Log.d("Fireinstaller2", "filesrc " +spi.sourceDir);
				        try {
				        	//move apk to fire tv here
				    		//Foreach Entry do and show progress thing:
				            Log.d("Fireinstaller2", "/system/bin/adb install "+spi.sourceDir+"\n");
	
				            Toast.makeText(this, "Pushing now(2/2)"+spi.displayName, Toast.LENGTH_LONG).show();
				           
				            //String command="/system/bin/adb install "+spi.sourceDir+"\n";
				           // new PushingTask().execute(outputStream, command);
				            
				            outputStream.writeBytes("/system/bin/adb install "+spi.sourceDir+"\n");
				        	
				           outputStream.flush();
				    	
				    		
				           } catch (IOException e) {
				            	Log.e("Fireinstaller", "error");
				            }
				    
		
		}}

		//After pushing:
		try {
			outputStream.close();
			adb.waitFor();
		} catch (IOException e) {
			Log.e("Fireinstaller", "error");
		} catch (InterruptedException e) {
			Log.e("Fireinstaller", "error");
		}
		adb.destroy();
		
		//return ret;
		return;

}
	public static String noNull(String input) {
		if (input == null) {
			return "";
		}
		return input;
	}

	public boolean isNothingSelected() {
		ListAdapter adapter = getListAdapter();
		if (adapter != null) {
			int count = adapter.getCount();
			for (int i = 0; i < count; i++) {
				SortablePackageInfo spi = (SortablePackageInfo) adapter.getItem(i);
				if (spi.selected) {
					return false;
				}
			}
		}
		Toast.makeText(this, R.string.msg_warn_nothing_selected, Toast.LENGTH_LONG)
				.show();
		return true;
	}


	
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
		template = (TemplateData) parent.getAdapter().getItem(pos);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AppAdapter aa = (AppAdapter) getListAdapter();
		SortablePackageInfo spi = aa.getItem(position);
		spi.selected = !spi.selected;
		aa.notifyDataSetChanged();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
			return true;
	}
	

	public static void openUri(Context ctx, Uri uri) {
		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
			ctx.startActivity(browserIntent);
		}
		catch (ActivityNotFoundException e) {
			
			Toast.makeText(ctx, "no webbrowser found", Toast.LENGTH_SHORT).show();
		}
	}
	      
	       
	
	        /**
	         * Enum used to identify the tracker that needs to be used for tracking.
	         *
	         * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
	         * storing them all in Application object helps ensure that they are created only once per
	         * application instance.
	         */
	  
	        synchronized Tracker getTracker(TrackerName trackerId) {
	            if (!mTrackers.containsKey(trackerId)) {

	              GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
	              Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
	                  : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
	                      : analytics.newTracker(R.xml.global_tracker);
	              mTrackers.put(trackerId, t);

	            }
	            return mTrackers.get(trackerId);
	          }
			  
	
}

