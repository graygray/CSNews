package com.graylin.csnews;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.annotation.SuppressLint;
import android.app.Activity;  
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;  
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;  
import android.widget.LinearLayout;
import android.widget.ListView;  
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static boolean isDebug = false;
//	public static boolean isDebug = true;

	public static boolean isPremium = false;
	public static boolean isNeedUpdate = false;
	public static boolean waitFlag = false;
	public static boolean isUpdateVideoTimeOut = false;
	public static boolean isEverLoaded = false;
	
	public static final int MAX_LIST_ARRAY_SIZE = 20;
	public static String[] cnnListStringArray = new String [MAX_LIST_ARRAY_SIZE];
	public static String[] cnnScriptAddrStringArray = new String [MAX_LIST_ARRAY_SIZE];
	
	public final String scriptAddressStringPrefix = "http://transcripts.cnn.com/TRANSCRIPTS/";
	public final String scriptAddressStringPostfix = "/sn.01.html";
	public static String scriptAddressString = "";
	
	public final String videoAddressStringPrefix = "http://podcasts.cnn.net/cnn/big/podcasts/studentnews/video/";
	public final String videoAddressStringPostfix = ".cnn.m4v";
	public static String videoAddressString = "";
	public static String videoAddressString2 = "";
	public static String videoAddressString3 = "";
	public static String videoAddressString4 = "";
	public static String videoAddressString5 = "";
	public static String videoAddressString6 = "";
	public static String videoDate = "";
	
	public ListView listView;
	public ArrayAdapter<String> adapter;
	public AdView adView;
	// ProgressDialog, wait network work to be done
	public ProgressDialog mProgressDialog;
	
	// HTML page
	public static final String CNNS_URL = "http://edition.cnn.com/US/studentnews/quick.guide/archive/";
	
    // XPath query
	public String XPATH = "";
	
	// SharedPreferences instance
	public static SharedPreferences sharedPrefs;
	public static SharedPreferences.Editor sharedPrefsEditor;
	public String lastUpdateTime = "";
	public String currentTime = "";
	public boolean isTooLongNoUpdate = false;
	public boolean isCheckVidoeUpdate = false;
	public boolean isVideoUpdate = false;
	
	// settings variable
	public static boolean isEnableDownload;
	public static int textSize;
	public static int swipeTime;
	public static int scriptTheme;
	public static int autoDelete;
	public static int bgStopTimes;
	public static String translateLanguage;
	public static boolean isEnableLongPressTranslate;
	public static boolean isEnableSoftButtonTranslate;
	public static boolean isVideoControlBar;
	
	// broadcast receiver
	public AudioManager audioManager;
	public ComponentName componentName = null;
	
	// orientation
	public static int originOrientation;
	public static int previousOrientation;
	
	// video
	public static  String cnnVideoName;
	
	// admob
	public static final String AD_UNIT_ID = "ca-app-pub-5561117272957358/8626647607";
	public static final String AD_SPLASH_UNIT_ID = "ca-app-pub-5561117272957358/1103380808";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (isDebug) {
			Log.e("gray", "MainActivity.java: START ===============");
		}
		
		// register broadcast event
		audioManager =(AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		componentName = new ComponentName(this, RemoteControlReceive.class);
		audioManager.registerMediaButtonEventReceiver(componentName);  
		
		// get SharedPreferences instance
		if (sharedPrefs == null) {
			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		}
		if (sharedPrefsEditor == null) {
			sharedPrefsEditor = sharedPrefs.edit();  
		}
		
		// get initial data
		isEnableDownload = sharedPrefs.getBoolean("pref_download", false);
		isVideoControlBar = sharedPrefs.getBoolean("pref_videoControlBar", true);
		try {
			textSize = Integer.valueOf(sharedPrefs.getString("pref_textSize", "18"));
		} catch (Exception e) {
			Log.e("gray", "MainActivity.java: onCreate, Exception : " + e.toString() );
		}
        if (textSize < 8) {
        	textSize = 8;
		} else if (textSize > 50){
        	textSize = 50;
        }
        
        try {
			swipeTime = Integer.valueOf(sharedPrefs.getString("pref_swipeTime", "3"));
		} catch (Exception e) {
			Log.e("gray", "MainActivity.java: onCreate, Exception : " + e.toString() );
		}
        if (swipeTime < 1) {
        	swipeTime = 1;
		} else if (swipeTime > 20){
			swipeTime = 20;
        }
        
        isEverLoaded = sharedPrefs.getBoolean("isEverLoaded", false);
        if (isEverLoaded) {
        	
        	if (isDebug) {
        		Log.e("gray", "MainActivity.java: isEverLoaded !!");
			}
        	for (int i = 0; i < MAX_LIST_ARRAY_SIZE; i++) {
				cnnListStringArray[i] = sharedPrefs.getString("cnnListString_"+i, "");
				cnnScriptAddrStringArray[i] = sharedPrefs.getString("cnnScriptAddrString_"+i, "");
				
				if (isDebug) {
					Log.e("gray", "MainActivity.java: cnnListStringArray[i]:" + cnnListStringArray[i]);
					Log.e("gray", "MainActivity.java: cnnScriptAddrStringArray[i]:" + cnnScriptAddrStringArray[i]);
				}
			}
        	showListView();
        	
		} else {
			
        	TextView tv  = new TextView(this);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setText(Html.fromHtml(
            		"How to use this app please have a look at <b>Information</b>; " +
					"errors, bugs or questions please see <b>Q & A</b> first.<br>"
            		)); 
            tv.setTextSize(18);
            tv.setPadding(20, 20, 20, 20);
            new AlertDialog.Builder(MainActivity.this)
            .setTitle("First Time to Use Message")
            .setView(tv)
            .show();
			
//			showAlertDialog("First Use Message", 
//					"How to use this app please have a look at \"Information\" page; " +
//					"errors, bugs or questions please check \"Q & A\" page first.\n\n"
//					);
			
			// initial array
			for (int i = 0; i < MAX_LIST_ARRAY_SIZE; i++) {
				cnnListStringArray[i] = "initail string value";
				cnnScriptAddrStringArray[i] = "initail string value";
			}
		}
        
        scriptTheme = Integer.valueOf( sharedPrefs.getString("pref_script_theme", "0") );
        translateLanguage = sharedPrefs.getString("pref_translate_language", "zh-TW");
        isEnableSoftButtonTranslate = sharedPrefs.getBoolean("pref_soft_button_translate", true);
        autoDelete = Integer.valueOf( sharedPrefs.getString("pref_auto_delete_file", "0") );
        bgStopTimes = Integer.valueOf( sharedPrefs.getString("pref_pref_bg_play_times", "0") );
		
        // =============================================================================
		// check if too long no update, (> 1 day)
		// if current date string different, then update
		SimpleDateFormat s1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String currentDate = s1.format(new Date());
		isTooLongNoUpdate = false;
		
		// get last update date
		String lastUpdateDate = sharedPrefs.getString("lastUpdateDate", "");
		
		if (currentDate.equalsIgnoreCase(lastUpdateDate)) {
			isTooLongNoUpdate = false;
		} else {
			isTooLongNoUpdate = true;
			sharedPrefsEditor.putString("lastUpdateDate", currentDate);
			sharedPrefsEditor.commit();
		}
		if (isDebug) {
			Log.e("gray", "MainActivity.java: currentDate: " + currentDate);
			Log.e("gray", "MainActivity.java: lastUpdateDate: " + lastUpdateDate);
			Log.e("gray", "MainActivity.java: isTooLongNoupdate: " + isTooLongNoUpdate);
		}
		
		// manage file (auto delete)
		if (isTooLongNoUpdate) {
			if ( autoDelete != 0 ) {
				new Thread(new Runnable() 
				{ 
					@Override
					public void run() 
					{ 
						deleteCNNSFiles(autoDelete);
					} 
				}).start();
			} 
		}	
        // =============================================================================
        
        // =============================================================================
        // check if need to update
        // if there is new video, then update
		// get string of last video source (every hour check right now) 
        isNeedUpdate = false;
        waitFlag = false;
        isUpdateVideoTimeOut = false;
        String lastVideosource = sharedPrefs.getString("lastVideosource", "");
        if (isNetworkAvailable()) {
        	
        	SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd-kk", Locale.US);
        	currentTime = s.format(new Date());
        	isCheckVidoeUpdate = false;
        	
        	// get last update time
        	lastUpdateTime = sharedPrefs.getString("lastUpdateTime", "");
        	
        	if (currentTime.equalsIgnoreCase(lastUpdateTime)) {
        		isCheckVidoeUpdate = false;
        	} else {
        		isCheckVidoeUpdate = true;
        	}
        	if (isDebug) {
        		Log.e("gray", "MainActivity.java: currentTime: " + currentTime);
        		Log.e("gray", "MainActivity.java: lastUpdateTime: " + lastUpdateTime);
        		Log.e("gray", "MainActivity.java: isCheckVidoeUpdate: " + isCheckVidoeUpdate);
        	}
        	
        	// try to check whether video update
        	if (isCheckVidoeUpdate || isTooLongNoUpdate) {
//        	if (true) {
//        		Log.e("gray", "MainActivity.java:onCreate, check video update..");
        		new Thread(new Runnable() 
        		{ 
        			@Override
        			public void run() 
        			{
        				try {
        					if (isCheckVidoeUpdate) {
        						isVideoUpdate();
							}
        					handler.sendEmptyMessage(2);
        				} catch (Exception e) {
        					Log.e("gray", "MainActivity.java:onCreate, " + "isVideoUpdate, Exception : " + e.toString());
        					e.printStackTrace();
        				}
        			} 
        		}).start();
			}
        	
        } else {
        	if (lastVideosource.equalsIgnoreCase("")) {
				//never have cnns data
				showAlertDialog("Error", "Never get data from CNN student news! Please enable network and try again.");
			} 
        }
        // =============================================================================

		if (!isPremium) {
			// load AD
			// Create an ad.
		    adView = new AdView(this);
//		    adView.setAdSize(AdSize.BANNER);
		    adView.setAdSize(AdSize.SMART_BANNER);
//		    adView.setAdUnitId("a151e4fa6d7cf0e");
		    adView.setAdUnitId(AD_UNIT_ID);

		    // Add the AdView to the view hierarchy. The view will have no size
		    // until the ad is loaded.
		    LinearLayout layout = (LinearLayout) findViewById(R.id.ADLayout);
		    layout.addView(adView);

		    // Start loading the ad in the background.
		    adView.loadAd(new AdRequest.Builder().build());
		}
		
         if (isDebug) {
			Log.e("gray", "MainActivity.java: END =================");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (isDebug) {
			Log.e("gray", "MainActivity.java: onActivityResult, requestCode: " + requestCode);
			Log.e("gray", "MainActivity.java: onActivityResult, resultCode: " + resultCode);
		}
		
		// reload AD
		if (!isPremium) {
			adView.loadAd(new AdRequest.Builder().build());
		}
		
        switch (requestCode) {
		case 0:
			// back to main page from settings page, set settings value to variable
			if (isDebug) {
				Log.e("gray", "MainActivity.java: pref_download :" + sharedPrefs.getBoolean("pref_download", false) );
				Log.e("gray", "MainActivity.java: pref_videoControlBar :" + sharedPrefs.getBoolean("pref_videoControlBar", false) );
				Log.e("gray", "MainActivity.java: pref_textSize :" + sharedPrefs.getString("pref_textSize", "18") );
				Log.e("gray", "MainActivity.java: pref_swipeTime :" + sharedPrefs.getString("pref_swipeTime", "3") );
				Log.e("gray", "MainActivity.java: pref_script_theme :" + sharedPrefs.getString("pref_script_theme", "0") );
				Log.e("gray", "MainActivity.java: pref_translate_language :" + sharedPrefs.getString("pref_translat_language", "zh-TW") );
				Log.e("gray", "MainActivity.java: pref_auto_delete_file :" + sharedPrefs.getString("pref_auto_delete_file", "0") );
				Log.e("gray", "MainActivity.java: pref_pref_bg_play_times :" + sharedPrefs.getString("pref_pref_bg_play_times", "0") );
			}
			
			isEnableDownload = sharedPrefs.getBoolean("pref_download", false);
			isVideoControlBar = sharedPrefs.getBoolean("pref_videoControlBar", true);
			
			try {
				textSize = Integer.valueOf(sharedPrefs.getString("pref_textSize", "18"));
			} catch (Exception e) {
				Log.e("gray", "MainActivity.java: onCreate, Exception : " + e.toString() );
			}
	        if (textSize < 8) {
	        	textSize = 8;
			} else if (textSize > 50){
	        	textSize = 50;
	        }
	        
	        try {
				swipeTime = Integer.valueOf(sharedPrefs.getString("pref_swipeTime", "3"));
			} catch (Exception e) {
				Log.e("gray", "MainActivity.java: onCreate, Exception : " + e.toString() );
			}
	        if (swipeTime < 1) {
	        	swipeTime = 1;
			} else if (swipeTime > 20){
				swipeTime = 20;
	        }
			
	        scriptTheme = Integer.valueOf( sharedPrefs.getString("pref_script_theme", "0") );
			translateLanguage = sharedPrefs.getString("pref_translate_language", "zh-TW");
			isEnableSoftButtonTranslate = sharedPrefs.getBoolean("pref_soft_button_translate", true);
			autoDelete = Integer.valueOf( sharedPrefs.getString("pref_auto_delete_file", "0") );
			bgStopTimes = Integer.valueOf( sharedPrefs.getString("pref_pref_bg_play_times", "0") );
			
			if (autoDelete != 0) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						deleteCNNSFiles(autoDelete);
						handler.sendEmptyMessage(1);
					}
				}).start();
			}
			
			break;
			
		case 1:
			setRequestedOrientation(originOrientation);
			showListView();
			
			break;
			
		case 2:
			showListView();
			break;

		}
	}
	
	public static boolean isFileExist(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return false;
		} else {
			return true;
		}
	}
	
	public void showListView() {
		
		// Get ListView object from res
	    listView = (ListView) findViewById(R.id.mainListView);
	    
	    ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();  
        for(int i = 0; i < MAX_LIST_ARRAY_SIZE; i++) {
        	
        	HashMap<String, Object> map = new HashMap<String, Object>();  
        	
    		String [] tempSA = new String [32];
            tempSA = cnnScriptAddrStringArray[i].split("/");
            if (isDebug) {
                Log.e("gray", "MainActivity.java:cnnListStringArray, length : " + tempSA.length);
                for (int j = 0; j < tempSA.length; j++) {
                    Log.e("gray", "MainActivity.java:showListView, " + j + " : " + tempSA[j]);
                }
            }
            
            int archiveYear = 0, archiveMonth, archiveDay, realYear = 0, realMonth = 0, realDay = 0;
            String archiveMonthS = null, archiveDayS = null, realMonthS = null, realDayS = null;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
//                Date date = df.parse("2013-12-31");
                Date date = df.parse(tempSA[1] + "-" + tempSA[2] + "-" + tempSA[3]);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
 
                archiveYear = cal.get(Calendar.YEAR);
                archiveMonth = cal.get(Calendar.MONTH) + 1;
                archiveDay = cal.get(Calendar.DAY_OF_MONTH);
                archiveMonthS = String.format(Locale.US, "%02d", archiveMonth);
                archiveDayS = String.format(Locale.US, "%02d", archiveDay);
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
                realYear = cal.get(Calendar.YEAR);
                realMonth = cal.get(Calendar.MONTH) + 1;
                realDay = cal.get(Calendar.DAY_OF_MONTH);
                realMonthS = String.format(Locale.US, "%02d", realMonth);
                realDayS = String.format(Locale.US, "%02d", realDay);
                
            } catch (ParseException e) {
                Log.e("gray", "MainActivity.java:showListView, ParseException, " + e.toString());
                e.printStackTrace();
            }
    		
    		if (isDebug) {
    			Log.e("gray", "MainActivity.java: archive date : " + archiveYear + archiveMonthS + archiveDayS);
    			Log.e("gray", "MainActivity.java: real date : " + realYear + realMonthS + realDayS);
			}
    		
    		// check if video file already download, or set path to local dir
    		cnnVideoName = "/sn-" + realMonthS + realDayS + (realYear-2000) + videoAddressStringPostfix;
    		if (isDebug) {
    			Log.e("gray", "path: " + Environment.getExternalStorageDirectory().getPath()+"/"+Environment.DIRECTORY_DOWNLOADS+cnnVideoName);
			}
    		
    		// put title
            map.put("ItemTitle", cnnListStringArray[i]);
            
            // put script image
            if (isFileExist(Environment.getExternalStorageDirectory().getPath()+"/"+Environment.DIRECTORY_DOWNLOADS+cnnVideoName+".txt")) {
            	map.put("ItemImage_news", R.drawable.ic_newspaper_o);  
            } else {
            	map.put("ItemImage_news", R.drawable.ic_newspaper_x);  
			}
            
            // put video image
            if (isFileExist(Environment.getExternalStorageDirectory().getPath()+"/"+Environment.DIRECTORY_DOWNLOADS+cnnVideoName)) {
            	map.put("ItemImage_video", R.drawable.ic_video_o);  
            } else {
            	map.put("ItemImage_video", R.drawable.ic_video_x);  
			}
            listItem.add(map);  
        }  
	    
        SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem, R.layout.cnn_listview, new String[] {"ItemTitle","ItemImage_news", "ItemImage_video"}, new int[] {R.id.ItemTitle,R.id.ItemImage_news,R.id.ItemImage_video});  
            
        listView.setAdapter(listItemAdapter);  
        
	    listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				
				if (isDebug) {
					Log.e("gray", "MainActivity.java:onItemClick" + "Position : " + position + ", id : " + id);
				}
				
				String [] tempSA = new String [32];
	            tempSA = cnnScriptAddrStringArray[position].split("/");
	            if (isDebug) {
	                Log.e("gray", "MainActivity.java:cnnListStringArray, length : " + tempSA.length);
	                for (int j = 0; j < tempSA.length; j++) {
	                    Log.e("gray", "MainActivity.java:showListView, " + j + " : " + tempSA[j]);
	                }
	            }
	            
	            int archiveYear = 0, archiveMonth, archiveDay, realYear = 0, realMonth = 0, realDay = 0;
	            String archiveMonthS = null, archiveDayS = null, realMonthS = null, realDayS = null;
	            DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	            try {
//	                Date date = df.parse("2013-12-31");
	                Date date = df.parse(tempSA[1] + "-" + tempSA[2] + "-" + tempSA[3]);
	                Calendar cal = Calendar.getInstance();
	                cal.setTime(date);
	 
	                archiveYear = cal.get(Calendar.YEAR);
	                archiveMonth = cal.get(Calendar.MONTH) + 1;
	                archiveDay = cal.get(Calendar.DAY_OF_MONTH);
	                archiveMonthS = String.format(Locale.US, "%02d", archiveMonth);
	                archiveDayS = String.format(Locale.US, "%02d", archiveDay);
	                
	                cal.add(Calendar.DAY_OF_MONTH, 1);
	                realYear = cal.get(Calendar.YEAR);
	                realMonth = cal.get(Calendar.MONTH) + 1;
	                realDay = cal.get(Calendar.DAY_OF_MONTH);
	                realMonthS = String.format(Locale.US, "%02d", realMonth);
	                realDayS = String.format(Locale.US, "%02d", realDay);
	                
	            } catch (ParseException e) {
	                Log.e("gray", "MainActivity.java:showListView, ParseException, " + e.toString());
	                e.printStackTrace();
	            }
	    		
	            cnnVideoName = "/sn-" + realMonthS + realDayS + (realYear-2000) + videoAddressStringPostfix;
	            
				scriptAddressString =  scriptAddressStringPrefix + (realYear-2000) + realMonthS + "/" + realDayS + scriptAddressStringPostfix;
				videoAddressString = videoAddressStringPrefix + archiveYear + "/" + archiveMonthS + "/" + archiveDayS + "/sn-" + realMonthS + realDayS + (realYear-2000) + videoAddressStringPostfix; 
				videoAddressString2 = videoAddressStringPrefix + realYear + "/" + realMonthS + "/" + realDayS + "/sn-" + realMonthS + realDayS + (realYear-2000) + videoAddressStringPostfix; 
				videoAddressString3 = videoAddressStringPrefix + realYear + "/" + realMonthS + "/" + realDayS + "/orig-sn-" + realMonthS + "-" + realDayS + "-" + (realYear-2000) + videoAddressStringPostfix; 
				videoAddressString4 = videoAddressStringPrefix + archiveYear + "/" + archiveMonthS + "/" + archiveDayS + "/orig-sn-" + realMonthS +  realDayS +  (realYear-2000) + videoAddressStringPostfix; 
				videoAddressString5 = videoAddressStringPrefix + realYear + "/" + realMonthS + "/" + realDayS + "/orig-sn-" + realMonthS + realDayS + (realYear-2000) + videoAddressStringPostfix; 
				videoDate = realMonthS + realDayS;
				
				if (isDebug) {
					Log.e("gray", "MainActivity.java:onItemClick, " + "scriptAddressString:" + scriptAddressString);
					Log.e("gray", "MainActivity.java:onItemClick, " + "vodeoAddressString:" + videoAddressString);
					Log.e("gray", "MainActivity.java:onItemClick, " + "vodeoAddressString2:" + videoAddressString2);
					Log.e("gray", "MainActivity.java:onItemClick, " + "vodeoAddressString3:" + videoAddressString3);
					Log.e("gray", "MainActivity.java:onItemClick, " + "vodeoAddressString4:" + videoAddressString4);
					Log.e("gray", "MainActivity.java:onItemClick, " + "vodeoAddressString5:" + videoAddressString5);
				}
			
				originOrientation = getResources().getConfiguration().orientation;
				previousOrientation = getResources().getConfiguration().orientation;
				
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, PlayActivity.class);
				startActivityForResult(intent, 1);
			}
	    }); 
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {
        	
           	switch (msg.what) {
    			case 0:
    				
    				try {
    					mProgressDialog.dismiss();
    					mProgressDialog = null;
    					showListView();
    				} catch (Exception e) {
    					// nothing
    				}
    				
    				break;

    			case 1:
    				showListView();
    				break;
    				
    			case 2:
    				
    				// check list update
    				if (isDebug) {
    					Log.e("gray", "MainActivity.java:handler, case 2, isVideoUpdate:" + isVideoUpdate);
    					Log.e("gray", "MainActivity.java:handler, case 2, isTooLongNoUpdate:" + isTooLongNoUpdate);
					}
    				if ( isVideoUpdate || isTooLongNoUpdate ) {	
    					
						showProcessDialog("Please Wait...", "Update Data From CNN Student News...");
						new Thread(new Runnable() 
						{ 
							@Override
							public void run() 
							{ 
								try {
									getCNNSList();
									handler.sendEmptyMessage(0);
								} catch (Exception e) {
									Log.e("gray", "MainActivity.java:getCNNSList, Exception:" + e.toString());
									e.printStackTrace();
								}
							} 
						}).start();
						
    				} 
    				break;
           	}
        }  
    };  
	
    public void isVideoUpdate() throws Exception {
    	
    	String resultS = "";
    	String newestVideoSource = "";
	    Object[] resultSNode;
	
	    // config cleaner properties
	    HtmlCleaner htmlCleaner = new HtmlCleaner();
	    CleanerProperties props = htmlCleaner.getProperties();
	    props.setAllowHtmlInsideAttributes(false);
	    props.setAllowMultiWordAttributes(true);
	    props.setRecognizeUnicodeChars(true);
	    props.setOmitComments(true);
	    
	    // create URL object
	    URL url = new URL("http://rss.cnn.com/services/podcasting/studentnews/rss.xml");
	    // get HTML page root node
	    TagNode root = htmlCleaner.clean(url);

	    // query XPath
	    XPATH = "//pubDate";
	    resultSNode = root.evaluateXPath(XPATH);

	    // process data if found any node
	    if(resultSNode.length > 0) {
	    	
	    	if (isDebug) {
	    		Log.e("gray", "MainActivity.java:isVideoUpdate, resultSNode.length:" + resultSNode.length);
	    		for (int i = 0; i < resultSNode.length; i++) {
	    			
	    			TagNode resultNode = (TagNode)resultSNode[i];
	    			resultS = resultNode.getText().toString();
	    			Log.e("gray", "MainActivity.java:isVideoUpdate, " + resultS);
	    		}
			}
	    	
	    	TagNode resultNode = (TagNode)resultSNode[0];
	    	newestVideoSource = resultNode.getText().toString();
	    	
	    } else {
	    	Log.e("gray", "MainActivity.java:isVideoUpdate, resultSNode.length <= 0, err!!");
		}
	    
//	    String [] tempSA = new String [32];
//        tempSA = newestVideoSource.split(" ");
//        if (true) {
//            Log.e("gray", "MainActivity.java:isVideoUpdate, length : " + tempSA.length);
//            for (int j = 0; j < tempSA.length; j++) {
//                Log.e("gray", "MainActivity.java:isVideoUpdate, " + j + " : " + tempSA[j]);
//            }
//        }
//        newestVideoSource = tempSA[5];
        
		// get last video source string
		String lastVideosource = sharedPrefs.getString("lastVideosource", "");
		if (newestVideoSource.equalsIgnoreCase(lastVideosource)) {
			isVideoUpdate = false;
//			isNeedUpdate = false;
		} else {
			isVideoUpdate = true;
//			isNeedUpdate = true;
			sharedPrefsEditor.putString("lastVideosource", newestVideoSource);
			sharedPrefsEditor.commit();
		}
		if (isDebug) {
			Log.e("gray", "MainActivity.java: newestVideoSource: " + newestVideoSource);
			Log.e("gray", "MainActivity.java: lastVideosource: " + lastVideosource);
//			Log.e("gray", "MainActivity.java: isNeedUpdate: " + isNeedUpdate);
		}
			
		waitFlag = true;
    }
    
	public void getCNNSList() throws Exception {
	    
		String resultS = "";
		String matchString = "CNN Student News";
//		String lastVideosource = sharedPrefs.getString("lastVideosource", "");
//		String comparedDateS = "";
	    int arrayIndex = 0;
	    Object[] resultSNode;
	
	    if (isDebug) {
	    	Log.e("gray", "MainActivity.java:getCNNSList, " + "");
		}
	    
	    // config cleaner properties
	    HtmlCleaner htmlCleaner = new HtmlCleaner();
	    CleanerProperties props = htmlCleaner.getProperties();
	    props.setAllowHtmlInsideAttributes(false);
	    props.setAllowMultiWordAttributes(true);
	    props.setRecognizeUnicodeChars(true);
	    props.setOmitComments(true);
	
	    // create URL object
	    URL url = new URL(CNNS_URL);
	    // get HTML page root node
	    TagNode root = htmlCleaner.clean(url);

	    // query XPath
	    XPATH = "//div[@class='cnn_spccovt1cllnk cnn_spccovt1cll2']//h2//a";
	    resultSNode = root.evaluateXPath(XPATH);

	    // process data if found any node
    	if(resultSNode.length > 0) {
    		
    		if (isDebug) {
    			Log.e("gray", "MainActivity.java:getCNNSList, resultSNode.length:" + resultSNode.length);
    		}
    		for (int i = 0; i < resultSNode.length; i++) {
    			
    			TagNode resultNode = (TagNode)resultSNode[i];
    			resultS = resultNode.getText().toString();
    			
//    			String [] tempSA = new String [32];
//    	        tempSA = resultS.split(" ");
//    	        if (isDebug) {
//    	            Log.e("gray", "MainActivity.java:isVideoUpdate, length : " + tempSA.length);
//    	            for (int j = 0; j < tempSA.length; j++) {
//    	                Log.e("gray", "MainActivity.java:isVideoUpdate, " + j + " : " + tempSA[j]);
//    	            }
//    	        }
//    	        comparedDateS = tempSA[5];
    			
    			if (resultS.regionMatches(0, matchString, 0, matchString.length())) {
//    				if (isDebug) {
//    					Log.e("gray", "MainActivity.java:getCNNSList, lastVideosource:" + lastVideosource);
//    					Log.e("gray", "MainActivity.java:getCNNSList, resultS:" + resultS);
//    					Log.e("gray", "MainActivity.java:getCNNSList, comparedDateS:" + comparedDateS);
//					}
//					if (lastVideosource.equalsIgnoreCase(comparedDateS) ) {
//					if (isNeedUpdate) {
						
	    				resultS = resultS.replace("CNN Student News -", "");
	    				cnnListStringArray[arrayIndex] = resultS;
	    				cnnScriptAddrStringArray[arrayIndex] = resultNode.getAttributeByName("href");
	    				
	    				sharedPrefsEditor.putString("cnnListString_"+arrayIndex, cnnListStringArray[arrayIndex]);
	    				sharedPrefsEditor.putString("cnnScriptAddrString_"+arrayIndex, cnnScriptAddrStringArray[arrayIndex]);
	    				if (isDebug) {
	    					Log.e("gray", "MainActivity.java:getCNNSList, i:" + (i) + ", arrayIndex:" + arrayIndex + ", getAttributeByName = " + resultNode.getAttributeByName("href"));
	    				}
	    				
	    				arrayIndex++;
//					} else {
//						break;
//					}
//					} else {
//						break;
//					}
    			} else {
    				if (isDebug) {
    					Log.e("gray", "MainActivity.java:getCNNSList, string not match!!" );
    				}
    			}
    		}
    		
    	} else {
    		Log.e("gray", "resultSNode.length <= 0, err!!");
    	}
	    
	    // query XPath
	    XPATH = "//div[@class='cnn_mtt1imghtitle']//span//a";
	    resultSNode = root.evaluateXPath(XPATH);

	    // process data if found any node
	    if(resultSNode.length > 0) {
	    	
	    	if (isDebug) {
	    		Log.e("gray", "MainActivity.java:getCNNSList, resultSNode.length:" + resultSNode.length);
			}
	    	for (int i = 0; i < resultSNode.length; i++) {
				
	    		TagNode resultNode = (TagNode)resultSNode[i];
	    		resultS = resultNode.getText().toString();
	    		
	    		if (resultS.regionMatches(0, matchString, 0, matchString.length())) {
					
	    			resultS = resultS.replace("CNN Student News -", "");
	    			cnnListStringArray[arrayIndex] = resultS;
	    			cnnScriptAddrStringArray[arrayIndex] = resultNode.getAttributeByName("href");
	    			
	    			sharedPrefsEditor.putString("cnnListString_"+arrayIndex, cnnListStringArray[arrayIndex]);
	    			sharedPrefsEditor.putString("cnnScriptAddrString_"+arrayIndex, cnnScriptAddrStringArray[arrayIndex]);
	    			if (isDebug) {
	    				Log.e("gray", "MainActivity.java:getCNNSList, i:" + (i) + ", arrayIndex:" + arrayIndex + ", getAttributeByName = " + resultNode.getAttributeByName("href"));
					}
	    			
	    			arrayIndex++;
				} else {
					if (isDebug) {
						Log.e("gray", "MainActivity.java: string not match!!" );
					}
				}
	    	}
	    	
	    } else {
	    	Log.e("gray", "resultSNode.length <= 0, err!!");
		}
	    
	    // query XPath
	    XPATH = "//div[@class='archive-item story cnn_skn_spccovstrylst']//h2//a";
	    resultSNode = root.evaluateXPath(XPATH);

	    // process data if found any node
	    if(resultSNode.length > 0) {
	    	
	    	if (isDebug) {
	    		Log.e("gray", "MainActivity.java:getCNNSList, resultSNode.length:" + resultSNode.length);
			}
	    	for (int i = 0; arrayIndex < MAX_LIST_ARRAY_SIZE; i++) {
				
	    		TagNode resultNode = (TagNode)resultSNode[i];
	    		resultS = resultNode.getText().toString();
	    		
	    		if (resultS.regionMatches(0, matchString, 0, matchString.length())) {
					
	    			resultS = resultS.replace("CNN Student News Transcript -", "");
	    			resultS = resultS.replace("CNN Student News -", "");
	    			cnnListStringArray[arrayIndex] = resultS;
	    			cnnScriptAddrStringArray[arrayIndex] = resultNode.getAttributeByName("href");
	    			
	    			sharedPrefsEditor.putString("cnnListString_"+arrayIndex, cnnListStringArray[arrayIndex]);
	    			sharedPrefsEditor.putString("cnnScriptAddrString_"+arrayIndex, cnnScriptAddrStringArray[arrayIndex]);
	    			if (isDebug) {
	    				Log.e("gray", "MainActivity.java:getCNNSList, i:" + (i) + ", arrayIndex:" + arrayIndex + ", getAttributeByName = " + resultNode.getAttributeByName("href"));
					}
	    			
	    			arrayIndex++;
				} else {
					if (isDebug) {
						Log.e("gray", "MainActivity.java: string not match!!" );
					}
				}
	    	}
	    	
	    	sharedPrefsEditor.putBoolean("isEverLoaded", true);
    		sharedPrefsEditor.putString("lastUpdateTime", currentTime);
	    	sharedPrefsEditor.commit();
	        
	    } else {
	    	Log.e("gray", "resultSNode.length <= 0, err!!");
		}
	}
	
	public static String getScriptAddress() {
		return scriptAddressString;
	}
  
	public static String getVideoAddress() {
		return videoAddressString;
	}
	
    public void showProcessDialog(CharSequence title, CharSequence message){
    	
		mProgressDialog = ProgressDialog.show(MainActivity.this, title, message, true);
		mProgressDialog.setCancelable(true); 
    }
    
	public boolean isNetworkAvailable() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networInfo = conManager.getActiveNetworkInfo();
		if (networInfo == null || !networInfo.isAvailable()){
			return false;
		} else {
			return true;
		}
	}
	
	public void showAlertDialog(String title, String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
	}
    
	public void deleteCNNSFiles(int deleteParameter){
		
		if (isDebug) {
			Log.e("gray", "MainActivity.java:deleteCNNSFiles, " + "");
		}
		
		// Directory path
		String path = Environment.getExternalStorageDirectory().getPath()+"/"+Environment.DIRECTORY_DOWNLOADS;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		int survivalDay = 0;
		
		switch (deleteParameter) {
			case 1:
				survivalDay = 5;
				break;
			case 2:
				survivalDay = 10;
				break;
			case 3:
				survivalDay = 15;
				break;
			case 4:
				survivalDay = 20;
				break;
			case 5:
				survivalDay = 25;
				break;
			case 6:
				survivalDay = 30;
				break;
		}

		if (listOfFiles != null) {
			
			for (int i = 0; i < listOfFiles.length; i++) {
				
				if (listOfFiles[i].isFile()) {
					
					Date currentDate = new Date();
					long lCurrentDate = currentDate.getTime();
					if (listOfFiles[i].getName().contains(".cnn.m4v")) {
						
						Date lastModDate = new Date(listOfFiles[i].lastModified());
						long diff = lCurrentDate - lastModDate.getTime();
						
						if (isDebug) {
							Log.e("gray", "MainActivity.java:manageCNNSFiles, currentDate : " + currentDate);
							Log.e("gray", "MainActivity.java:manageCNNSFiles, file : " + listOfFiles[i].getName() + " : " + lastModDate.toString());
							Log.e("gray", "MainActivity.java:manageCNNSFiles, Difference is : " + (diff/(1000*60*60*24)) + " days.");
						}
						
						if ((diff/(1000*60*60*24)) > survivalDay ) {
							listOfFiles[i].delete();
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (isDebug) {
			Log.e("gray", "MainActivity.java: onCreateOptionsMenu");
		}
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		
		if (isDebug) {
			Log.e("gray", "MainActivity.java:onOptionsItemSelected, " + "");
		}
        switch (item.getItemId()) {
        
        
        case R.id.action_update_list:
        	
        	showProcessDialog("Please Wait...", "Update Data From CNN Student News...");
	        new Thread(new Runnable() 
			{ 
				@Override
				public void run() 
				{ 
					try {
						getCNNSList();
						handler.sendEmptyMessage(0);
					} catch (Exception e) {
						Log.e("gray", "MainActivity.java:getCNNSList, Exception:" + e.toString());
						e.printStackTrace();
					}
				} 
			}).start();
	        break;
	        
        case R.id.action_settings:
        	if (isDebug) {
        		Log.e("gray", "MainActivity.java:onOptionsItemSelected, case R.id.action_settings");
        	}
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, 0);
            break;
            
        case R.id.action_info:
        	if (isDebug) {
        		Log.e("gray", "MainActivity.java:onOptionsItemSelected, case R.id.action_info");
        	}
        	TextView tv  = new TextView(this);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setText(Html.fromHtml(
            		"<b>What's new in this version (2.17)?</b><br><br>" +
    				"1. You can edit note now.<br>" +
    				"2. You can set how many times to stop at background mode.<br>" +
    				"3. You can set theme as random.<br>" +
					"<br>" +
					"<font color='red'><b>iOS version of this app was published at Apple store, " +
					"please search it use keyword : \"10min News\" or \"CNN Student News\"</b></font><br>" +
					"<br>" +
					"Have any idea, suggestion or bug report just mail me :<br>" +
					"<a href='mailto:llkkqq@gmail.com'>llkkqq@gmail.com (Gray Lin)</a><br>" +
					"Meanwhile, I keep a copy at <a href='http://lkqlinsblog.blogspot.tw/2014/01/android-besr-app-for-cnn-student-news.html'>my blog</a><br>" +
					"<br>" +
        			"<b>Usage & Features:</b><br><br>" +
        			"<b>1. Quick translate : ( 2 method )</b><br>" +
        			"<i>a.</i> By double click a word.<br>" +
        			"<i>b.</i> By long press to select a word, then click <i>Translate</i> button. ( you can disable this button at <i>Settings</i> )<br>" +
        			"All need network support.<br><br>" +
        			"<b>2. Quick Note :</b><br>" +
        			"After you look up a word and translate it which will be automatically saved to a file " +
        			"( at <i>"+Environment.getExternalStorageDirectory().getPath()+"/"+Environment.DIRECTORY_DOWNLOADS+"</i> ); " +
        			"then you can check what you note at <i>Quick Note</i> page, also to share it out.<br>" +
        			"PS : Whatever you note will be highlighted in both script & note.<br><br>" +
        			"<b>3. Video operation :</b><br>" +
        			"<i>a.</i> Pause/Resume by click central area of video view.<br>" +
        			"<i>b.</i> Rewind by click left area (1/6 zone) of video.<br>" +
					"<i>c.</i> Fast Forward by click right area(5/6 zone) of video.<br>" +
					"<i>d.</i> Swipe(slide on video) Left/Right to continuous Rewind/Fast Forward.<br>" +
					"<i>e.</i> Swipe Up/Down to Zoom Out/In.<br><br>" +
        			"<b>4. Script & video download :</b><br>" +
        			"The script will be downloaded automatically; " +
        			"but video need to be enabled first at <i>Settings</i>.<br>" +
        			"If the downloaded file exist at " +
        			"<i>"+Environment.getExternalStorageDirectory().getPath()+"/"+Environment.DIRECTORY_DOWNLOADS+"</i>" +
        			", the icon will be different obviously.<br>" +
        			"You can perform offline jobs ( without network ) after downloading video & script files.<br><br>" +
        			"<b>5. Playing video at background :</b><br>" +
        			"<i>a.</i> enabled when video is playing.<br>" +
        			"<i>b.</i> key Play/Pause to start/stop.<br>" +
        			"<i>c.</i> key Previous/Rewind to rewind.<br>" +
        			"<i>d.</i> key Next/Fast Forward to stop background service.<br>" +
        			"<i>e.</i> back to foreground or close app will stop background service.<br>" +
        			"PS : Background service will also comsume battery, remember to use <i>d.</i> or <i>e.</i> to stop it.<br><br>" +
        			"<b>6. Auto delete related file :</b><br>" +
        			"Auto delete if <i>modified timestamp of compared file > your setting value</i>; to enable this feature at <i>Settings</i>, " +
        			"default is disable.<br><br>" +
        			"*********************<br>" +
        			"If you like this app or think it's useful, please help to rank it at " +
        			"<a href='https://play.google.com/store/apps/details?id=com.graylin.10minNews'><b>Google Play</b></a>, thanks~^^<br>" +
        			"*********************<br>"
            		)); 
            tv.setTextSize(18);
            tv.setPadding(20, 20, 20, 20);
            new AlertDialog.Builder(MainActivity.this)
            .setTitle("Information")
            .setView(tv)
//            .setPositiveButton("OK!", new DialogInterface.OnClickListener()
//            {
//
//                public void onClick(DialogInterface dialog, int which)
//                {
//
//                }
//            })
            .show();
            
            break;
            
        case R.id.action_qa:
        	if (isDebug) {
        		Log.e("gray", "MainActivity.java:onOptionsItemSelected, case R.id.action_qa");
        	}
//        	showAlertDialog("Q & A", 
//        			"1. No update for a long time?\n" +
//        			"It's depend on CNN Student News..\n" +
//        			"The show is suspended when student is on vacation.\n\n" +
//        			"Take a look at CNN Student News:\n" +
//        			"http://edition.cnn.com/studentnews/\n" +
//        			"and it's archive here:\n" +
//        			"http://edition.cnn.com/US/studentnews/quick.guide/archive/\n\n" +
//        			"2. When will the list update?\n" +
//        			"We get video from here :\n" +
//        			"http://rss.cnn.com/services/podcasting/studentnews/rss.xml\n" +
//        			"When there is a new video here, we will update the list.\n" +
//        			"If video is already on website, but the App's list still not be updated, please try it an hour later.\n\n" +
//        			"3. How translation works?\n" +
//        			"I just send a translated query to some website, and get the translated result to show, but I don't know if it's appropriate for your language(or none for your language);" +
//					"If you have better or suggested website, just feel free to mail me.\n\n" +
//        			"4. Any suggestion or bug just:\n" +
//        			"mail to : llkkqq@gmail.com\n"
//        			);
        	
        	TextView tv2  = new TextView(this);
            tv2.setMovementMethod(LinkMovementMethod.getInstance());
            tv2.setText(Html.fromHtml(
            		"<b>1. No update for a long time?</b><br>" +
            		"It's depend on CNN Student News...<br>" +
            		"The show is suspended when student is on vacation.<br><br>" +
            		"Take a look at <a href='http://edition.cnn.com/studentnews/'>CNN Student News</a><br>" +
            		"and it's archive : <a href='http://edition.cnn.com/US/studentnews/quick.guide/archive/'>here</a><br><br>" +
            		"<b>2. When will the list auto update?</b><br>" +
            		"We get video from <a href='http://rss.cnn.com/services/podcasting/studentnews/rss.xml'>here</a><br>" +
            		"When there is a new video, we will update the list.<br>" +
            		"If video is already on website, but the App's list still not be updated, please try it an hour later or try manual update.<br><br>" +
            		"<b>3. Newest video or script not work?</b><br>" +
            		"Because the video or script of newest day may still not upload, you can take a look at website; " +
            		"If you see them at website but not work on your device, you can wait for a while and retry, check your friends' device or report it to me.<br>" +
            		"<br><font color='red'>PS : If the video just can't be download, you can :<br>" +
            		"&nbsp;&nbsp;a. download the video file yourself, then<br>" +
            		"&nbsp;&nbsp;b. put it into your smart phone's download path, then<br>" +
            		"&nbsp;&nbsp;c. rename it following the naming rule : \"sn-mmddyy.cnn.m4v\", for example : Feb 7, 2014 >> sn-020714.cnn.m4v</font><br>" +
            		"<br>" +
            		"<b>4. Any suggestion or bug:</b><br>" +
            		"mail to : <a href='mailto:llkkqq@gmail.com'>llkkqq@gmail.com (Gray Lin)</a><br>"
            		)); 
            tv2.setTextSize(18);
            tv2.setPadding(20, 20, 20, 20);
            new AlertDialog.Builder(MainActivity.this)
            .setTitle("Q & A")
            .setView(tv2)
//            .setPositiveButton("OK!", new DialogInterface.OnClickListener()
//            {
//
//                public void onClick(DialogInterface dialog, int which)
//                {
//
//                }
//            })
            .show();
            
            break;
            
        // remove quick note list
        /*case R.id.action_notelist:
        	if (isDebug) {
        		Log.e("gray", "MainActivity.java:onOptionsItemSelected, case R.id.action_notelist");
        	}
            Intent intent = new Intent();
			intent.setClass(MainActivity.this, NoteListActivity.class);
			startActivityForResult(intent, 2);
            break;*/
        }
 
        return true;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	public void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}
	  
	@Override
	public void onDestroy() {
		
		if (isDebug) {
			Log.e("gray", "MainActivity.java: onDestroy");	
		}
		
		// Destroy the AdView.
	    if (adView != null) {
	      adView.destroy();
	    }
		
		if (componentName != null ) {
			audioManager.unregisterMediaButtonEventReceiver(componentName);
			componentName = null;
		}
		
		super.onDestroy();
	}

}
