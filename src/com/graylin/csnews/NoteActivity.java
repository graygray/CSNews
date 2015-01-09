package com.graylin.csnews;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class NoteActivity extends Activity {

//	public TextView mTextView;
	public EditText mTextView;
	public AdView adView;
	public String noteContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		
		mTextView = (EditText) findViewById(R.id.tv_noteContent);
		
		try {
			noteContent = readFileAsString(Environment.getExternalStorageDirectory().getPath()+"/"+Environment.DIRECTORY_DOWNLOADS+"/"+ PlayActivity.NoteFileName);
			setResultText( noteContent );
		} catch (IOException e) {
			if (MainActivity.isDebug) {
				Log.e("gray", "NoteActivity.java:onCreate, IOException:" + e.toString());
			}
			showAlertDialog("Information", "You probably don't note anything yet.");
			e.printStackTrace();
		}
		
		if (!MainActivity.isPremium) {
			// load AD
			// Create an ad.
			adView = new AdView(this);
			adView.setAdSize(AdSize.SMART_BANNER);
			adView.setAdUnitId(MainActivity.AD_UNIT_ID);

			// Add the AdView to the view hierarchy. The view will have no size
			// until the ad is loaded.
			LinearLayout layout = (LinearLayout) findViewById(R.id.ADLayoutNote);
			layout.addView(adView);

			// Start loading the ad in the background.
			adView.loadAd(new AdRequest.Builder().build());
		}
	}

	public static String readFileAsString(String filePath) throws java.io.IOException
	{
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    String line, results = "";
	    while( ( line = reader.readLine() ) != null)
	    {
	        results += line;
	        results += "\n";
	    }
	    reader.close();
	    return results;
	}
	
	public void saveContentToNote() {
		
		FileWriter fw;
		BufferedWriter bw;

		noteContent = mTextView.getText().toString();
		
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory().getPath()+"/"+Environment.DIRECTORY_DOWNLOADS+"/"+PlayActivity.NoteFileName, false);
			bw = new BufferedWriter(fw);
			bw.write(noteContent);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setResultText(String s){
		
		s = s.replaceAll("\n\n\n\n\n", "\n");
		s = s.replaceAll("\n\n\n\n", "\n");
//		mTextView.setText(s+"\n\n");
		
		// use HTML format
		s = s.replaceAll("\n", "<br>");
		
		// highlight function
		s = PlayActivity.highlightContent(s);
		mTextView.setText(Html.fromHtml(s+"<br><br>"));
		mTextView.setTextSize(MainActivity.textSize);
		mTextView.setSelection(mTextView.getText().length());
		
		int randomThemeNO;
		if (MainActivity.scriptTheme == 20) {
			// random theme
			randomThemeNO = (int)(Math.random()*20);
		} else {
			randomThemeNO = MainActivity.scriptTheme;
		}
		
		switch (randomThemeNO) {
		case 0:	// Black  -  White
			mTextView.setTextColor(0xff000000);
			mTextView.setBackgroundColor(0xffffffff);
			break;
		case 1:	// White  -  Black
			mTextView.setTextColor(0xffffffff);
			mTextView.setBackgroundColor(0xff000000);
			break;
		case 2: // Red - White
			mTextView.setTextColor(0xffDC143C);
			mTextView.setBackgroundColor(0xffffffff);
			break;
		case 3: // White  -  Red
			mTextView.setTextColor(0xffffffff);
			mTextView.setBackgroundColor(0xffA8050A);
			break;
		case 4: // Orange  -  Black
			mTextView.setTextColor(0xFFFFA500);
			mTextView.setBackgroundColor(0xff000000);
			break;
		case 5: // White  -  Orange
			mTextView.setTextColor(0xffffffff);
			mTextView.setBackgroundColor(0xFFFFA500);
			break;
		case 6: // Black  -  Orange
			mTextView.setTextColor(0xff000000);
			mTextView.setBackgroundColor(0xFFFFA500);
			break;
		case 7: // Black  -  Yellow
			mTextView.setTextColor(0xff000000);
			mTextView.setBackgroundColor(0xffFFF396);
			break;
		case 8: // Green - White
			mTextView.setTextColor(0xff00C22E);
			mTextView.setBackgroundColor(0xffffffff);
			break;
		case 9: // Green - Black
			mTextView.setTextColor(0xff00C22E);
			mTextView.setBackgroundColor(0xff000000);
			break;
		case 10: // White - Green
			mTextView.setTextColor(0xffffffff);
			mTextView.setBackgroundColor(0xff00C22E);
			break;
		case 11: // Black - Green
			mTextView.setTextColor(0xff000000);
			mTextView.setBackgroundColor(0xff00C22E);
			break;
		case 12: // LightBlue  -  White
			mTextView.setTextColor(0xFF4169E1);
			mTextView.setBackgroundColor(0xffffffff);
			break;
		case 13: // White - LightBlue
			mTextView.setTextColor(0xffffffff);
			mTextView.setBackgroundColor(0xFF4169E1);
			break;
		case 14: // Black - LightBlue
			mTextView.setTextColor(0xff000000);
			mTextView.setBackgroundColor(0xFF4169E1);
			break;
		case 15: // White  -  Blue
			mTextView.setTextColor(0xffffffff);
			mTextView.setBackgroundColor(0xff1038AA);
			break;
		case 16: // Pink  -  White
			mTextView.setTextColor(0xFFFF1493);
			mTextView.setBackgroundColor(0xffffffff);
			break;
		case 17: // LightPurple - White
			mTextView.setTextColor(0xffC71585);
			mTextView.setBackgroundColor(0xffffffff);
			break;
		case 18: // White - LightPurple
			mTextView.setTextColor(0xffffffff);
			mTextView.setBackgroundColor(0xffC71585);
			break;
		case 19: // White  -  Purple
			mTextView.setTextColor(0xffffffff);
			mTextView.setBackgroundColor(0xff4D0D2A);
			break;
		default:
			break;
		}
	}
	
	public void showAlertDialog(String title, String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(NoteActivity.this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
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
		
		if (MainActivity.isDebug) {
			Log.e("gray", "MainActivity.java: onDestroy");	
		}
		
		saveContentToNote();
		
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.note, menu);
		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.action_share_note:
			
			saveContentToNote();
			
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND); 
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My 10minNews Notes - " + MainActivity.videoDate);
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, noteContent);
			startActivity(Intent.createChooser(sharingIntent, "Share via ..."));
			break;

		case R.id.action_show_kb:
			
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mTextView, InputMethodManager.SHOW_FORCED);

			break;
			
		case R.id.action_hide_kb:
			
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mTextView.getWindowToken(), 0);
			
			break;
	
		default:
			break;
		}
		return true;
	}
}
