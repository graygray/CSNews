package com.graylin.csnews;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		if (MainActivity.isDebug) {
			Log.e("gray", "SettingsActivity.java:onPostCreate");
		}

		setupSimplePreferencesScreen();
		
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (MainActivity.isDebug) {
			Log.e("gray", "SettingsActivity.java:setupSimplePreferencesScreen");
		}
		
		if (!isSimplePreferences(this)) {
			return;
		}
		
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

//		// Add 'notifications' preferences, and a corresponding header.
//		PreferenceCategory fakeHeader = new PreferenceCategory(this);
//		fakeHeader.setTitle(R.string.pref_header_notifications);
//		getPreferenceScreen().addPreference(fakeHeader);
//		addPreferencesFromResource(R.xml.pref_notification);
//
//		// Add 'data and sync' preferences, and a corresponding header.
//		fakeHeader = new PreferenceCategory(this);
//		fakeHeader.setTitle(R.string.pref_header_data_sync);
//		getPreferenceScreen().addPreference(fakeHeader);
//		addPreferencesFromResource(R.xml.pref_data_sync);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("pref_textSize"));
		bindPreferenceSummaryToValue(findPreference("pref_swipeTime"));
		bindPreferenceSummaryToValue(findPreference("pref_translate_language"));
		bindPreferenceSummaryToValue(findPreference("pref_script_theme"));
		bindPreferenceSummaryToValue(findPreference("pref_auto_delete_file"));
		bindPreferenceSummaryToValue(findPreference("pref_pref_bg_play_times"));
//		bindPreferenceSummaryToValue2(findPreference("pref_seekbar"));
//		bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
//		bindPreferenceSummaryToValue(findPreference("sync_frequency"));
		
		setContentView(R.layout.admob);
		if (!MainActivity.isPremium) {
			// load AD
			// Create an ad.
			AdView adView = new AdView(this);
			adView.setAdSize(AdSize.SMART_BANNER);
			adView.setAdUnitId(MainActivity.AD_UNIT_ID);

			// Add the AdView to the view hierarchy. The view will have no size
			// until the ad is loaded.
			LinearLayout layout = (LinearLayout) findViewById(R.id.admobSettingLayout);
			layout.addView(adView);

			// Start loading the ad in the background.
			adView.loadAd(new AdRequest.Builder().build());
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		if (MainActivity.isDebug) {
			Log.e("gray", "SettingsActivity.java:onIsMultiPane");
		}
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		if (MainActivity.isDebug) {
			Log.e("gray", "SettingsActivity.java:isXLargeTablet");
		}
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		if (MainActivity.isDebug) {
			Log.e("gray", "SettingsActivity.java:isSimplePreferences");
		}
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (MainActivity.isDebug) {
			Log.e("gray", "SettingsActivity.java:onBuildHeaders");
		}
		
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			
			if (MainActivity.isDebug) {
				Log.e("gray", "SettingsActivity.java: onPreferenceChange, stringValue:" + stringValue);
			}

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				
				if (MainActivity.isDebug) {
					Log.e("gray", "SettingsActivity.java: onPreferenceChange, index:" + index);
				}

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				if (MainActivity.isDebug) {
					Log.e("gray", "SettingsActivity.java:onPreferenceChange, else, " + "");
				}
				
				Integer tempInt = 0;
				if (preference.getKey().equalsIgnoreCase("pref_textSize") ) {
					try {
						tempInt = Integer.valueOf(stringValue);
					} catch (Exception e) {
						Log.e("gray", "MainActivity.java: onPreferenceChange, else, " + e.toString() );
					}
					if (tempInt < 8) {
						tempInt = 8;
					} else if (tempInt > 50){
						tempInt = 50;
				    }
					stringValue = tempInt.toString();
				} else if (preference.getKey().equalsIgnoreCase("pref_swipeTime")) {
					try {
						tempInt = Integer.valueOf(stringValue);
					} catch (Exception e) {
						Log.e("gray", "MainActivity.java: onPreferenceChange, else, " + e.toString() );
					}
					if (tempInt < 1) {
						tempInt = 1;
					} else if (tempInt > 20){
						tempInt = 20;
				    }
					stringValue = tempInt.toString();
				}
			
				if (MainActivity.isDebug) {
					Log.e("gray", "SettingsActivity.java:onPreferenceChange, else, " + "");
				}
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		if (MainActivity.isDebug) {
			Log.e("gray", "SettingsActivity.java:bindPreferenceSummaryToValue");
		}
		
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

//	private static void bindPreferenceSummaryToValue2(Preference preference) {
//		if (MainActivity.isDebug) {
//			Log.e("gray", "SettingsActivity.java:bindPreferenceSummaryToValue");
//		}
//		
//		// Set the listener to watch for value changes.
//		preference
//				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
//
//		// Trigger the listener immediately with the preference's
//		// current value.
//		sBindPreferenceSummaryToValueListener.onPreferenceChange(
//				preference,
//				PreferenceManager.getDefaultSharedPreferences(
//						preference.getContext()).getInt(preference.getKey(),
//						0));
//	}
	
	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			
			if (MainActivity.isDebug) {
				Log.e("gray", "SettingsActivity.java:GeneralPreferenceFragment.onCreate");
			}
			
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("pref_textSize"));
			bindPreferenceSummaryToValue(findPreference("pref_swipeTime"));
			bindPreferenceSummaryToValue(findPreference("pref_translate_language"));
			bindPreferenceSummaryToValue(findPreference("pref_script_theme"));
			bindPreferenceSummaryToValue(findPreference("pref_auto_delete_file"));
			bindPreferenceSummaryToValue(findPreference("pref_pref_bg_play_times"));
//			bindPreferenceSummaryToValue2(findPreference("pref_seekbar"));
			
		}
	}

	/**
	 * This fragment shows notification preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends
			PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);

			if (MainActivity.isDebug) {
				Log.e("gray", "SettingsActivity.java:NotificationPreferenceFragment.onCreate");
			}
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
//			bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
		}
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DataSyncPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_data_sync);

			if (MainActivity.isDebug) {
				Log.e("gray", "SettingsActivity.java:DataSyncPreferenceFragment.onCreate");
			}
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
//			bindPreferenceSummaryToValue(findPreference("sync_frequency"));
		}
	}
	
	public class myDialogPreference extends DialogPreference {

	    public myDialogPreference(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    @Override
	    protected void onDialogClosed(boolean positiveResult) {
	        super.onDialogClosed(positiveResult);
	        persistBoolean(positiveResult);
	    }

	}
	
	protected boolean isValidFragment (String fragmentName)
	{
		return true;
	}
	
}
