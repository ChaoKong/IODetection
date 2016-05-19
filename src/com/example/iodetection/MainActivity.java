package com.example.iodetection;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements
ConnectionCallbacks, OnConnectionFailedListener,LocationListener {
	
	
	
	private static final String TAG = "IODetectionMainActivity";
	public Logger logger = new Logger(true,TAG);
	int End_truth = -10;
    NotificationManager manager;
    Notification myNotication;
	public static int raw_counter = 0;
	

	File root = null;
	File dir = null;
	JSONObject Ground_truth = null;
	JSONObject tmp_truth = null;
	JSONObject Location = null;
	JSONObject Result1 = null;
	JSONObject Result2 = null;

	

	TextView textAllResult_reading;
	TextView textResultComparison_reading;
	
	
	File ResultFile = null;
	FileOutputStream foutResult = null;
	OutputStreamWriter outwriterResult = null;
	
	public int calculate_mode1 = 0;
	
	public int calculate_mode2 = 0;
	
	public String location1="";
	public String location2="";
	
	String result1_Str = "call start result";
	int result1 = 0;
	double result1_con = 0.0;
	String result2_Str = "call end result";
	int result2 = 0;
	double result2_con = 0.0;

	public Thread waitForSending = null;
	Context context = null;
	
	File GroundTruthFile = null;
	FileOutputStream foutGroundtruth = null;
	OutputStreamWriter outwriterGroundtruth = null;
	public String Groundtruthfile_str= "";
	
	GoogleApiClient mGoogleApiClient;
	LocationRequest mLocationRequest;
	Location mLocation;
	Location mLastLocation;
	int LocationFlag = 0;
	int StartTransmit = 0;
	String location_global ="";
	public Thread LocationThread = null;
	public int LocationThreadstart = 0;
    public long start_location_time = 0;
    public long stop_location_time = 0;
    
    
	private boolean isRecording = false;
	int sampleRate = 48000;
    private Thread recordingThread = null;
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    
    
	AudioManager	am	= null;
	AudioRecord record =null;
	MediaPlayer mediaPlayer = null;
	
	SensorManager mySensorManager = null;
	Sensor LightSensor = null;
	Sensor ProxiSensor = null;
	
	
	File SoundFile = null;
	FileOutputStream foutSound = null;
	OutputStreamWriter outwriterSound = null;
	
	
	File SoundDataFile = null;
	FileOutputStream foutSoundData = null;
	OutputStreamWriter outwriterSoundData = null;
	
	String device_ID;
	
	File ProxiFile = null;
	FileOutputStream foutProxi = null;
	OutputStreamWriter outwriterProxi = null;
	
	File AllInfoFile = null;
	FileOutputStream foutAllInfo = null;
	OutputStreamWriter outwriterAllInfo = null;
	
	
	String tmpGroundTruthFile_str="tmpGroundTruth.txt";
	
	
	WifiManager wifiManager;
	WifiInfo wifiInfo = null; 
	
	TelephonyManager telephonyManager = null;
	
	BufferedReader bufferedReader = null;
	
	File InputRGBFile = null;
	
	
    private ArrayList<Double> lightValue = null;
    private ArrayList<Double> RValue = null;
    private ArrayList<Double> GValue = null;
    private ArrayList<Double> BValue = null;
    private ArrayList<Double> WValue = null;
    private ArrayList<Integer> TValue = null;
    private ArrayList<Integer> WifiValue = null;
    
    
    private float ReadProxi = 0;
    private int RecordFlag = 0;

    
    
    public int Audio_flag = 0;

  
    public int proxi_count = 0;
    public long proxi_time = 0;
    public long cur_proxi_time = 0;
    public int proxi_start = 0;
    public long stop_proxi_time = 0;
    public long stop_proxi_time_end = 0;
    public long stop_proxi_time_end2 = 0;
    public Thread proxiThread = null;
    public Thread proxiThread2 = null;
    public int register_light = 0;
    public int proxi_thread_start = 0;
    public int proxi_thread_start2 = 0;
    
    public int CallType = 0;
    
    public int RGBAvailabe = 0;
    
    public int isS6 = 0;
    public int Previous_volume = 10;
    
    StringBuilder finalString = null;
    
    public static JSONObject TestObject = null;
    
    public int TestType =0;
    public int Result_TestType = 0;
    public int runAudioTest=0;
    public int audio_in_use = 0;
    //   1 : light test;  2: Audio test; 3: All test;
    
    public String deviceModel;
    
    private SharedPreferences sp_audio,sp1_audio;
    private SharedPreferences.Editor spEditor_audio;
    
    private SharedPreferences sp_sent,sp1_sent;
    private SharedPreferences.Editor spEditor_sent;
    
    private String audio_state = "TRUE";
    
    private String sent_state = "FALSE";
    
    Button AudioBtn;
	Button IndoorBtn;
	Button OutdoorBtn;
	Button AllTestBtn;
	Drawable TestBtnBackground;
	
	
	
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	
		
		setContentView(R.layout.activity_main);
		

		textAllResult_reading = (TextView) findViewById(R.id.All_result);
		//textConfidence_reading = (TextView) findViewById(R.id.Confidence);
		textResultComparison_reading  = (TextView) findViewById(R.id.UserInput_reading);
		IndoorBtn = (Button) findViewById(R.id.Indoor_switch);
		OutdoorBtn =  (Button) findViewById(R.id.Outdoor_switch);
		AllTestBtn=(Button) findViewById(R.id.All_switch);
		AudioBtn = (Button) findViewById(R.id.AudioSwitch);
		
		
		
		context = this;
		
		root = android.os.Environment.getExternalStorageDirectory();
		dir = new File(root.getAbsolutePath() + "/IODetection");
		dir.mkdirs();
		Groundtruthfile_str = dir+"/GroundTruthFile.txt";
		
    	// Create an instance of GoogleAPIClient.
    	if (mGoogleApiClient == null) {
    	    mGoogleApiClient = new GoogleApiClient.Builder(this)
    	        .addConnectionCallbacks((ConnectionCallbacks) this)
    	        .addOnConnectionFailedListener((OnConnectionFailedListener) this)
    	        .addApi(LocationServices.API)
    	        .build();
    	}
    	mGoogleApiClient.connect();

		final int[] mFiles = new int[] { R.raw.light_model6, R.raw.light_range_set6, R.raw.audio_model4, R.raw.audio_range4, R.raw.chirp14_file };
		final CharSequence[] filenames = { "model_light", "range_light", "model_audio", "range_audio", "chirp_file" };
		for (int i = 0; i < mFiles.length; i++) {
			try {
				if (dir.mkdirs() || dir.isDirectory()) {
					String str_song_name = dir + "/" + filenames[i];
					CopyRAWtoSDCard(mFiles[i], str_song_name);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		AllTestBtn.setEnabled(true);
		
		TestBtnBackground = AllTestBtn.getBackground();
		
		isS6 =0;
		AudioBtn.setVisibility(View.GONE);
    	deviceModel = Build.MODEL;
    	String S6 = "g920t";
    	if (deviceModel.toLowerCase().contains(S6.toLowerCase())){
    		isS6 =1;
    		AudioBtn.setVisibility(0);
    		audio_state = getAudioState(context);
    		if (audio_state.equals("FALSE"))
    		{
    			AudioBtn.setText("Use Audio? OFF");
    		}
    		else
    		{
    			AudioBtn.setText("Use Audio?  ON");
    		}
    	}
	
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(broadcastReceiver, new IntentFilter("ActiveTestResult"));
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    unregisterReceiver(broadcastReceiver);
	    logger.d("status"+"onDestroy");
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    logger.d("status"+"onPause");
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    logger.d("status"+"onstart");

	}
	
	@Override
	public void onRestart() {
	    super.onRestart();
	    logger.d("status"+"onRestart");

	}
	
	@Override
	public void onStop() {
	    super.onStop();
	    logger.d("status"+ "onStop");
	    this.finish();
	}
	
		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	
	BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
 
            String message = b.getString("message");
            logger.d("message in broadcastReceiver"+message);
			if (message!=null)
			{
				
				try {
					Ground_truth = new JSONObject(message);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.d("get message from service intent"+Ground_truth.toString());
				
				mGoogleApiClient.connect();
				LocationFlag = 0;
				audio_in_use = 0;
				
				LocationThread = null;
				getLocation();
				
				try {
					JSONObject tmp_Result2 = Ground_truth.getJSONObject("result2");	
					logger.d(Ground_truth.toString());
					logger.d("tmp_result2: "+tmp_Result2.toString());
					Result_TestType = tmp_Result2.getInt("testType");
					JSONObject callEnd = Ground_truth.getJSONObject("callEnd");
					audio_in_use = callEnd.getInt("Audioflag");
					logger.d("audio in use: "+String.valueOf(audio_in_use));
					logger.d("result test type: "+String.valueOf(Result_TestType));
					
//					if (Result_TestType==3)
//					{
//						Ground_truth.put("Start_ground_truth", End_truth);
//					}
					
					writeToFile(Ground_truth.toString(),tmpGroundTruthFile_str);
					updateSentState("FALSE",context);
					Ground_truth.put("End_ground_truth",-10);
					SendInformation();
					logger.d("testType in main activity"+ String.valueOf(Result_TestType));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					
					if ((Result_TestType==3) && (audio_in_use==1))
					{
						Result1 = Ground_truth.getJSONObject("result");
						
						Result2 = Ground_truth.getJSONObject("result2");
						result1_Str = Result1.getString("Result");
						result2_Str = Result2.getString("Result");
						calculate_mode1 = Result1.getInt("mode");
						calculate_mode2 = Result2.getInt("mode");
						printResults(result2_Str, 2, calculate_mode2,3);}
					else
					{
						Result2 = Ground_truth.getJSONObject("result2");
				
						result2_Str = Result2.getString("Result");
		
						calculate_mode2 = Result2.getInt("mode");
						printResults(result2_Str, 2, calculate_mode2,Result_TestType);}
					

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}

 
        };
	};
	
	
	
    public void updateAudioState(String state,Context context){
        sp_audio = PreferenceManager.getDefaultSharedPreferences(context);
        spEditor_audio = sp_audio.edit();
        spEditor_audio.putString("audio_state", state);
        spEditor_audio.commit();
        logger.d( "finish update audio state"+state);
    }
    
    public String getAudioState(Context context){
        sp1_audio = PreferenceManager.getDefaultSharedPreferences(context);
        String st =sp1_audio.getString("audio_state", "TRUE");
        logger.d("get audio state as :"+st);
        return st;
    }
    
    
    public void updateSentState(String state,Context context){
        sp_sent = PreferenceManager.getDefaultSharedPreferences(context);
        spEditor_sent = sp_sent.edit();
        spEditor_sent.putString("send_state", state);
        spEditor_sent.commit();
        logger.d( "update state to: "+state);
    }
    
    public String getSentState(Context context){
        sp1_sent = PreferenceManager.getDefaultSharedPreferences(context);
        String st =sp1_sent.getString("send_state", "");
        logger.d("get send state as :"+st);
        return st;
    }
	
    
    public void printResults(String result_str,int CallType, int calculate_mode, int testType){
    	
    	String tmp_result_str = result_str;
    	
		if ((testType == 3) && (audio_in_use ==1) ) {
			
			int tmp_result = 0;
			double tmp_confidence = 0;
			String tmp_result_Str = "";
			String[] splitStr_result1 = result1_Str.split("\\s+");
			result1 = Integer.parseInt(splitStr_result1[0]);
			result1_con = Double.parseDouble(splitStr_result1[1]);
			
			String[] splitStr_result2 = result2_Str.split("\\s+");
			result2 = Integer.parseInt(splitStr_result2[0]);
			result2_con = Double.parseDouble(splitStr_result2[1]);
			logger.d("result1_str: "+result1_Str);
			logger.d("result2_str: "+result2_Str);
			
			
			if (result1 == result2) {
				tmp_result = result1;
				tmp_confidence = 1 - (1 - result1_con) * (1 - result2_con);
			} else {
				if (result1_con > result2_con) {
					tmp_result = result1;
					tmp_confidence = result1_con * (1 - result2_con);
				} else {
					tmp_result = result2;
					tmp_confidence = result2_con * (1 - result1_con);

				}
			}

			if (tmp_result == -1) {
				tmp_result_Str = "Result: " + "You are indoor !";
				
			} else if (tmp_result == 1) {

				tmp_result_Str = "Result: " + "You are outdoor !";

			}
			NumberFormat formatter = new DecimalFormat("0.###"); 
			String confidence_str = formatter.format(tmp_confidence);
			textAllResult_reading.setText(tmp_result_Str);
			//textConfidence_reading.setText("Confidence: "+confidence_str);
		}

		else {
			String[] splitStr_result1 = tmp_result_str.split("\\s+");
			result1 = Integer.parseInt(splitStr_result1[0]);
			result1_con = Double.parseDouble(splitStr_result1[1]);
			if (result1 == -1) {
				result1_Str = "Result: " + "You are indoor !";
			} else if (result1 == 1) {
				
					result1_Str = "Result: " + "You are outdoor !";
				
			} else {
				result1_Str = "Result: " + "Unknown";
			}
			NumberFormat formatter = new DecimalFormat("0.###"); 
			String confidence_str = formatter.format(result1_con);
			textAllResult_reading.setText(result1_Str);
			//textConfidence_reading.setText("Confidence: "+confidence_str);
			
		}
		
		AllTestBtn.setText("Start Detection");
		AllTestBtn.getBackground().clearColorFilter();
		AllTestBtn.setEnabled(true);
		
    
    }
    

	public void SendInformation() {

		Thread t = new Thread() {

			public void run() {
				Looper.prepare(); // For Preparing Message Pool for the
									// child Thread
				HttpClient client = new DefaultHttpClient();
				HttpResponse response;
				JSONObject json = new JSONObject();

				try {
					HttpPost post = new HttpPost("http://psychic-rush-755.appspot.com/upload");
					StringEntity se = new StringEntity(Ground_truth.toString());
					se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					post.setEntity(se);
					response = client.execute(post);

					/* Checking response */
					if (response != null) {
						InputStream in = response.getEntity().getContent(); // Get
																			// the
																			// data
																			// in
																			// the
																			// entity
						String s = getStringFromInputStream(in);
						
						logger.d("Jack-Response"+ s);
					}
					Ground_truth = null;
					Ground_truth = new JSONObject();
					raw_counter = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		t.start();	
		logger.d("send start"+ Ground_truth.toString());
		if (waitForSending!= null)
		{
			if (!waitForSending.isInterrupted());
			{
				waitForSending.interrupt();
				waitForSending = null;
				logger.d("stop waitForSending: stop waitForSending in SendInformation");
				
			}
		}
		
	}
	
	
	private void CopyRAWtoSDCard(int id, String path) throws IOException {
	    InputStream in = getResources().openRawResource(id);
	    FileOutputStream out = new FileOutputStream(path);
	    byte[] buff = new byte[1024];
	    int read = 0;
	    try {
	        while ((read = in.read(buff)) > 0) {
	            out.write(buff, 0, read);
	        }
	    } finally {
	        in.close();
	        out.close();
	    }
	}
	
	public void waitSending() {

		waitForSending = new Thread() {

			public void run() {
				while(!Thread.interrupted())
			    {
					if (Ground_truth != null) {
						logger.d("ground truth in wait sending"+Ground_truth.toString() );
						if ( Ground_truth.has("Location2") && Ground_truth.has("End_ground_truth")) {
							logger.d("satify requirements: triger the SendInformation ");
							SendInformation();
							break;
						}
					} else {
						break;
					}
					logger.d("return waitForsending thread");
					Thread.currentThread().interrupt();
					return;			
			    }
			}
		};
		waitForSending.start();
	}

	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
	
	
    private void writeToFile(String data, String file) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(file, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            logger.d( "write file successfully");
        }
        catch (IOException e) {
            //Log.e(TAG, "File write failed: " + e.toString());
        } 
    }
    
    
    private String readFromFile(String file) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            //Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            //Log.e(TAG, "Can not read file: " + e.toString());
        }

        return ret;
    }
	

	@Override
	public void onConnected(Bundle connectionHint) {
		// Provides a simple way of getting a device's location and is well
		// suited for
		// applications that do not require a fine-grained location and that do
		// not need location
		// updates. Gets the best and most recent location currently available,
		// which may be null
		// in rare cases when a location is not available.
		mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (LocationListener) this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
	
	}
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes
		// might be returned in
		// onConnectionFailed.
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We
		// call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (LocationFlag == 0){
			location_global = Double.toString(location.getLatitude()) + " "+Double.toString(location.getLongitude());	
			LocationFlag = 1;	
			logger.d("locationChanged"+ location_global);
		}

	}
	
	public void getLocation() {


		start_location_time = System.currentTimeMillis();
		stop_location_time = start_location_time + 3000;
	    logger.d("start_location_time start"+String.valueOf(start_location_time));
		logger.d("stop_location_time start"+String.valueOf(stop_location_time));

		if (LocationThreadstart == 0) {
			LocationThreadstart = 1;
			logger.d("start Locationthread: start Location thread");
			LocationThread = new Thread() {

				public void run() {
					while (!Thread.interrupted()) {

						while (true) {

							if (start_location_time > stop_location_time) {
								logger.d("start_location_time"+ String.valueOf(start_location_time));
								logger.d("stop_location_time"+ String.valueOf(stop_location_time));
								location2 = "fail";
								break;
							}

							if ((mLastLocation != null) || (location_global.length() > 2)) {
								logger.d("mLastLocation"+ mLastLocation.toString());
								logger.d("location_global"+ location_global);
								if (mLastLocation != null) {
									location2 = Double.toString(mLastLocation.getLatitude()) + " "
											+ Double.toString(mLastLocation.getLongitude());

								} else {

									location2 = location_global;
								}
								break;
							}
							start_location_time = System.currentTimeMillis();
							//logger.d("get location in thread", "get location in thread");
						}
						

						putLocation();
						Thread.currentThread().interrupt();
						return;
						
					}
				}
			};
			LocationThread.start();
		}

	}

	private void putLocation() {
		
		logger.d("put location 2"+location2);

		if (Ground_truth!=null)
		{
			try {
				Ground_truth.put("Location2",location2);
				LocationThreadstart = 0;		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
    private void init(){
    		

		InputRGBFile = new File("/sys/devices/virtual/sensors/light_sensor/raw_data");
		
		
		RGBAvailabe = 0;
		if (InputRGBFile.exists())
		{
			
			RGBAvailabe = 1;
			try {
				bufferedReader = new BufferedReader(new FileReader(InputRGBFile));
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				RGBAvailabe = 0;
				logger.d("try to read RGB, unavailable"+ "try to read RGB, unavailable");
				e2.printStackTrace();
				
			}
						
			logger.d("RGB available"+String.valueOf(RGBAvailabe));
			
		}
		
		
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        
        mySensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        
        
    	telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
    	device_ID= telephonyManager.getDeviceId();
    	
    	
		SoundFile = new File(dir, "SoundRecord.txt");
    	if (!SoundFile.exists()){
    		try {
    			SoundFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
		SoundDataFile = new File(dir, "SoundData.txt");
    	if (!SoundDataFile.exists()){
    		try {
    			SoundDataFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);


		ProxiSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (ProxiSensor != null) {
			// textProxi_available.setText("Sensor PROXIMITY Available");
			ProxiFile = new File(dir, "ProxiRecord.txt");
			if (!ProxiFile.exists()) {
				try {
					ProxiFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {
			// textProxi_available.setText("Sensor PROXIMITY NOT Available");
			// textTemper_reading.setText("Temperature(C): ");
		}
		
    	
    	
    	AllInfoFile = new File(dir,"AllInfoRecord.txt");
    	if (!AllInfoFile.exists()){
    		try {
    			AllInfoFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	

    	proxi_count = 0;
    	RecordFlag = 0;
    	proxiThread = null;
    	proxiThread2 = null;
    	register_light = 0;
    	runAudioTest = 0;
    	proxi_thread_start = 0;
    	
    	TestType = 0;
    	
    	deviceModel = Build.MODEL;
    	Log.i(TAG, deviceModel);
    	String S6 = "g920t";
    	if (deviceModel.toLowerCase().contains(S6.toLowerCase())){
    		isS6 =1;
    	}
    	
    	TestObject = new JSONObject();
    	Log.i(TAG, "init successfully");
    }
    
    

    
		
	public void AudioEnableSwitch(View view) throws IOException  {
		if (isS6==1) {
			
			audio_state = getAudioState(context);
			logger.d("get audio state from getAudiostate:	" + audio_state);
			
			if (audio_state.equals("FALSE")) {
					AudioBtn.setText("Use Audio?  ON");
					audio_state = "TRUE";
					updateAudioState(audio_state, context);
					logger.d("set audio in using");
			
			} else {
					AudioBtn.setText("Use Audio? OFF");
					audio_state = "FALSE";
					updateAudioState(audio_state, context);
					logger.d("set audio to stop");
				
			}
		}
	}
	
	
	public void IndoorSwitch(View view) throws IOException {
		
		sent_state = getSentState(context);
		logger.d("sent state in indoor switch: "+sent_state);
		textAllResult_reading.setText("Result: " + "You are indoor !");
		if (sent_state.equals("FALSE"))
		{
			updateSentState("TRUE",context);
			
			String tmp_groundth = readFromFile(tmpGroundTruthFile_str);
			logger.d("read tmp groud truth from file: "+tmp_groundth);
			try {
				Ground_truth = null;
				Ground_truth = new JSONObject(tmp_groundth);
				Ground_truth.put("End_ground_truth",-1);
				if (audio_in_use==1)
				{
					Ground_truth.put("Start_ground_truth",-1);
				}
				SendInformation();
				writeToFile("clear",tmpGroundTruthFile_str);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			logger.d("ground truth in indoor switch: "+Ground_truth.toString());

		}		
	}
	
	
	public void OutdoorSwitch(View view) throws IOException {
		sent_state = getSentState(context);
		logger.d("sent state in outdoor switch: "+sent_state);
		textAllResult_reading.setText("Result: " + "You are outdoor !");
		if (sent_state.equals("FALSE"))
		{
			updateSentState("TRUE",context);
			
			String tmp_groundth = readFromFile(tmpGroundTruthFile_str);
			logger.d("read tmp groud truth from file: "+tmp_groundth);
			try {
				Ground_truth = null;
				Ground_truth = new JSONObject(tmp_groundth);
				Ground_truth.put("End_ground_truth",1);
				if (audio_in_use==1)
				{
					Ground_truth.put("Start_ground_truth",1);
				}
				SendInformation();
				writeToFile("clear",tmpGroundTruthFile_str);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			logger.d("ground truth in outdoor switch: "+Ground_truth.toString());

		}
	}
	
	
	
	
	private void runAudiotest()
	{
		audio_state = getAudioState(context);
		if ((isS6==1) && (audio_state.equals("TRUE")))
		{
			Audio_flag = 1;
			startRecording();
			try {
				Thread.sleep(80);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startEmitting();
			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			stopEmitting();
			stopRecording();
		
		}
		else
		{
			Audio_flag=0;
		}
		
	}
	
	
	boolean isAllTest = false;

	public void AllSwitch(View view) throws IOException {

		AllTestBtn.setEnabled(false);
		AllTestBtn.setText("Detecting...");	
		AllTestBtn.getBackground().setColorFilter(new LightingColorFilter(Color.rgb(120, 176, 209),0));
		textAllResult_reading.setText("Result:	");
		//textConfidence_reading.setText("Confidence: ");		
		
		init();
		
		TestType = 3;
		logger.d("start to register proxisensro");
		registerProxiSensor();

		
		
	} 
    
 
    
    private void registerLightSensor() {
    	
	    lightValue = new ArrayList<Double>();
	    RValue = new ArrayList<Double>();
	    GValue = new ArrayList<Double>();
	    BValue = new ArrayList<Double>();
	    WValue = new ArrayList<Double>();
	    TValue = new ArrayList<Integer>();
	    WifiValue = new ArrayList<Integer>();	
	    long curTime = System.currentTimeMillis();
	    logger.d("create array"+"create array "+String.valueOf(curTime));
    	
		if(LightSensor != null){
			
			if(AllInfoFile.exists()){
				  
				  try{
		    		
					  foutAllInfo = new FileOutputStream(AllInfoFile,true);
					  outwriterAllInfo= new OutputStreamWriter(foutAllInfo);
				
				  } catch(Exception e)
				  {

				  }
			}
			mySensorManager.registerListener(LightSensorListener, LightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}

		if (proxiThread!= null)
		{
			if (!proxiThread.isInterrupted());
			{
				proxiThread.interrupt();
				proxiThread = null;
				logger.d("stop proxi thread: stop proxi thread in register light");
				
			}
		}
		
    }
    
    
    
    private void unregisterLightSensor()
    {
    	if(LightSensor != null){
			mySensorManager.unregisterListener(LightSensorListener,LightSensor);
			
			try {
				outwriterAllInfo.flush();
				outwriterAllInfo.close();
				foutAllInfo.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    	}      	
	    lightValue.clear();
	    RValue.clear();
	    GValue.clear();
	    BValue.clear();
	    WValue.clear();
	    TValue.clear();
	    WifiValue.clear();
	   	
    }
    
    
    private void unregisterProxiSensor()
    {
    	
    	if(ProxiSensor != null){
			mySensorManager.unregisterListener(ProxiSensorListener,ProxiSensor);
			try {
				//outwriterProxi.flush();
				outwriterProxi.close();
				foutProxi.close();
			} catch (IOException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} 
	
    }
    
    
    
    private void registerProxiSensor() {
    	
		if ( ProxiSensor != null){
			
			if(ProxiFile.exists()){
			  
				try{
	    		
					foutProxi = new FileOutputStream(ProxiFile,true);
					outwriterProxi = new OutputStreamWriter(foutProxi);
				} catch(Exception e)
				{
				}
			}
			mySensorManager.registerListener(ProxiSensorListener, ProxiSensor, SensorManager.SENSOR_DELAY_FASTEST);
			logger.d("register proximity sensor");
		}		
    }
        

    private void startEmitting(){
    	
    	Context context = this;
		am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mediaPlayer = MediaPlayer.create(this, R.raw.chirp814);
		mediaPlayer.setVolume(0.6f, 0.6f);
		logger.d("mediaPlayer create"+mediaPlayer.toString());
    	Previous_volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
    	logger.d("previous volume: "+ String.valueOf(Previous_volume));
    	am.setStreamVolume(AudioManager.STREAM_MUSIC,10,0);
    	logger.d("current volume: "+ String.valueOf(10));
    	mediaPlayer.setVolume(0.6f, 0.6f);
    	mediaPlayer.start();	
    }
    
    
    private void stopEmitting() {
    	mediaPlayer.pause();
    	mediaPlayer.release();
    	mediaPlayer = null;
    }   
    
   
    
    private void startRecording() {

		int min = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, min);
        record.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

        //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        int num = 0;
        short sData[] = new short[BufferElements2Rec];

        
        try {
        	foutSound = new FileOutputStream(SoundFile,true);
        	outwriterSound = new OutputStreamWriter(foutSound);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        try {
        	foutSoundData = new FileOutputStream(SoundDataFile);
        	outwriterSoundData = new OutputStreamWriter(foutSoundData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format

            num=record.read(sData, 0, BufferElements2Rec);
            System.out.println("Short wirting to file" + sData.toString());
            if (num == BufferElements2Rec){
	            long curTime = System.currentTimeMillis();
	            String curTimeStr = ""+curTime+";   ";
	            Log.i(TAG,curTimeStr);
	            try {
	            	outwriterSound.append(curTimeStr);
	    		} catch (IOException e1) {
	    			// TODO Auto-generated catch block
	    			e1.printStackTrace();
	    		}
	        	String strI = ""+(sData.length);
	        	Log.i(TAG, strI);
	        	
	        	for ( int i=0; i<sData.length;i++){
	        		try {
	        			String tempS = Short.toString(sData[i])+"    ";
	        			outwriterSound.append(tempS);
	        			outwriterSoundData.append(tempS);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
        		try {
        			String tempS = "\n";
        			outwriterSound.append(tempS);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
        	try {
        		outwriterSound.flush();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
        	try {
        		outwriterSoundData.flush();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            
            
        }
       
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != record) {
        	
        	am.setStreamVolume(AudioManager.STREAM_MUSIC,Previous_volume,0);
            isRecording = false;
            record.stop();
            record.release();
            record = null;

            recordingThread = null;
        }
    }
        
    private final SensorEventListener LightSensorListener= new SensorEventListener(){

    	@Override
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    		// TODO Auto-generated method stub
    	}
    	@Override
    	public void onSensorChanged(SensorEvent event) {
    		if(event.sensor.getType() == Sensor.TYPE_LIGHT){
    			
   			
    			double lightvalue = 0.0;
    			int Wifi_RSSI=0;
    			long timeSta = System.currentTimeMillis();
    			String curTimeStr = ""+timeSta+";   ";
    			logger.d("light sensor change:"+ curTimeStr);
				
			    			
    			if (lightValue.size()==5){
   				
    				
    				logger.d("light value size is 5");
    				RecordFlag =0;
    				double Light_Sum = 0;
    				double R_Sum = 0;
    				double G_Sum = 0;
    				double B_Sum = 0;
    				double W_Sum = 0;
    				double Wifi_Sum = 0;
    				for ( int i = 0; i<5; i++){
    					Light_Sum = Light_Sum + lightValue.get(i);
    					R_Sum = R_Sum + RValue.get(i);
    					G_Sum = G_Sum + GValue.get(i);
    					B_Sum = B_Sum + BValue.get(i);
    					W_Sum = W_Sum + WValue.get(i);
    					Wifi_Sum = Wifi_Sum + (double)WifiValue.get(i);	
    					
    				}
    				Light_Sum = Light_Sum/5;
    				R_Sum = R_Sum/5;
    				G_Sum = G_Sum/5;
    				B_Sum = B_Sum/5;
    				W_Sum = W_Sum/5;
    				Wifi_Sum = Wifi_Sum/5;
    					
    				String Light_RGB_Wifi = String.valueOf(Light_Sum)+" "+String.valueOf(R_Sum)+" "+String.valueOf(G_Sum)+" "+String.valueOf(B_Sum)+" "+String.valueOf(W_Sum)+" "+String.valueOf(Wifi_Sum);
    				   
    				logger.d("get Ave info");
    				
    				sendFinalJSON(TestType,Light_RGB_Wifi,Audio_flag);
    				
                    unregisterLightSensor();
                    logger.d("unregister light sensor");
    			 					
    			}

    			
	            
	            lightvalue = (double) (event.values[0]);
	            logger.d(String.valueOf(lightvalue));				
    			SupplicantState supState; 
    			
    			wifiInfo = wifiManager.getConnectionInfo();
    			
    			supState = wifiInfo.getSupplicantState();

	            
	            logger.d(String.valueOf(wifiInfo.getRssi()));
	            Wifi_RSSI = wifiInfo.getRssi();
	            finalString = new StringBuilder();
	            if (RGBAvailabe==1) {
					try {
						bufferedReader = new BufferedReader(new FileReader(InputRGBFile));
					} catch (FileNotFoundException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					if (bufferedReader != null) {
						String line;
						try {
							while ((line = bufferedReader.readLine()) != null) {

								finalString.append(line);
								String[] tmp_RGB = line.split(",");
								int tmp_I = 3 - Integer.parseInt(tmp_RGB[5]);
								double tmp_time = Math.pow(4, tmp_I);
								double tmp_R = (Double.parseDouble(tmp_RGB[0])) * tmp_time;
								double tmp_G = (Double.parseDouble(tmp_RGB[1])) * tmp_time;
								double tmp_B = (Double.parseDouble(tmp_RGB[2])) * tmp_time;
								double tmp_W = (Double.parseDouble(tmp_RGB[3])) * tmp_time;

								lightValue.add(lightvalue);
								WifiValue.add(Wifi_RSSI);
								RValue.add(tmp_R);
								GValue.add(tmp_G);
								BValue.add(tmp_B);
								WValue.add(tmp_W);
								logger.d( line);
							

							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						bufferedReader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	            else{
					lightValue.add(lightvalue);
					WifiValue.add(Wifi_RSSI);
					RValue.add(0.0);
					GValue.add(0.0);
					BValue.add(0.0);
					WValue.add(0.0);
					
	            }
	            
	            logger.d("light value size"+ String.valueOf(lightValue.size()));

				if (RecordFlag==1) {
					String Light_RGB_Wifi = String.valueOf(lightvalue) +" "+finalString.toString()+" "+String.valueOf(Wifi_RSSI);
					logger.d("Light RGB wifi"+ Light_RGB_Wifi);
					writeJSON(outwriterAllInfo,timeSta,"rawData",Light_RGB_Wifi);
					
					logger.d("call add");
					sendJSON(timeSta,"rawData",Light_RGB_Wifi);
					finalString = null;

				}
		
    		}
    	}
    };
    
    private final SensorEventListener ProxiSensorListener= new SensorEventListener(){

    	@Override
    	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    		// TODO Auto-generated method stub
    
    	}

    	@Override
    	public void onSensorChanged(SensorEvent event) {
    		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
    			
    			proxi_time = System.currentTimeMillis();
    			cur_proxi_time = proxi_time;
    			stop_proxi_time = proxi_time + 100;
    			stop_proxi_time_end = cur_proxi_time + 100;
    			String curTimeStr = ""+proxi_time+";   ";
    			ReadProxi = event.values[0];
    			
    			logger.d("proxi count"+String.valueOf(proxi_count));
    			logger.d("proxi value"+String.valueOf(ReadProxi));
    			logger.d("proxi time"+String.valueOf(proxi_time));
    			logger.d("cur proxi time"+String.valueOf(cur_proxi_time));
    			logger.d("stop_proxi_time_end"+String.valueOf(stop_proxi_time_end));
    			logger.d("stop_proxi_time"+String.valueOf(stop_proxi_time));
    			logger.d("RecordFlag"+String.valueOf(RecordFlag));
    			
    			
    			if (ReadProxi<1)
    			{
    			
    			}
    			
        		if (ReadProxi>1)
        		{	
        		
        			
        				if (RecordFlag ==1){
            				if (proxiThread!= null)
            				{
            					if (!proxiThread.isInterrupted());
            					{
            						proxiThread.interrupt();
            						proxiThread = null;
            						logger.d("stop thread: stop proxithread light sensor");     						
            					}
            				}
        					
        				}
						if (RecordFlag == 0) {
			    			if (proxi_thread_start ==0)
			    			{
			    				proxi_thread_start = 1;
			    				logger.d("start proxithread: first start proxithread");
			    				proxiThread = new Thread() {

			    					public void run() {
			    						while(!Thread.interrupted())
			    					    {
				    						while (RecordFlag==0 )
				    						{
				    							if ((cur_proxi_time > stop_proxi_time_end) && (ReadProxi>1))
				    							{
					    							logger.d("cur proxi time in EndingCallFlag 1: "+ String.valueOf(cur_proxi_time));
					    							logger.d("stop_proxi_time end in EndingCallFlag 1: "+String.valueOf(stop_proxi_time_end));
					    							logger.d("readProxi: "+String.valueOf(ReadProxi));
				    								break;
				    							}
				    							cur_proxi_time = System.currentTimeMillis();
//				    							logger.d("cur proxi time in thread", String.valueOf(cur_proxi_time));
//				    							logger.d("stop_proxi_time in thread", String.valueOf(stop_proxi_time));
				    						
				    						}

				    						RecordFlag = 1;
				    						logger.d("RecordFlag in thread: "+String.valueOf(RecordFlag));
				    						unregisterProxiSensor();
				    						
				    						if (TestType == 1) {
				    							if (register_light == 0) {
				    								register_light = 1;
				    								logger.d("register light in test type 1");
				    								registerLightSensor();

				    							}
				    						}
				    						
				    						if ((TestType ==2) && (isS6==1)){
				    							if (runAudioTest==0){
				    								runAudioTest=1;
				    								runAudiotest();	
				    								logger.d("run audio test in type 2");
				    								String tmp_Light_RGB_Wifi = "0 0 0 0 0 0";
				    								sendFinalJSON(TestType,tmp_Light_RGB_Wifi,Audio_flag);
				    							}
				    						}
				    						
				    						if (TestType ==3){
				    							
				    							if (isS6==1)
				    							{
				    								if (runAudioTest==0){
				    									runAudioTest=1;
					    								runAudiotest();
					    								logger.d("run audio test in type 3");
				    								}

				    							}
				    							else
				    							{
				    								Audio_flag = 0;
				    							}
				    							
				    							if (register_light == 0) {
				    								register_light = 1;
				    								logger.d("register light in test type 3");
				    								registerLightSensor();
				    							}

				    						}
				    						
				    						
				    						Thread.currentThread().interrupt();
				    						return;

			    					    }

			    					}
			    				};
			    				proxiThread.start();
			    			}							
						}
        			}
        			
        		proxi_count = proxi_count + 1;
	            try {
	            	outwriterProxi.append(curTimeStr);
	    		} catch (IOException e1) {
	    			// TODO Auto-generated catch block
	    			e1.printStackTrace();
	    		}
	            try {
					outwriterProxi.append(String.valueOf(event.values[0])+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            try {
					outwriterProxi.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
    		}
    	}
     
    }; 
    

    
    
	public void writeJSON(OutputStreamWriter myWriter, long timestamp, String tag, String info) {
		JSONObject object = new JSONObject();
		try {

			object.put("timestamp", timestamp);
			object.put(tag, info);
			String content = object.toString() + "\n";
			myWriter.append(content);
			logger.d("write json"+ content);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendJSON(long timestamp, String tag, String info){
		JSONObject tmp_object = new JSONObject();
		logger.d("write json start");
		try {
			tmp_object.put("timestamp", timestamp);
			tmp_object.put(tag, info);
			
			TestObject.put("raw" + String.valueOf(raw_counter), tmp_object);

			logger.d("send json: "+tmp_object.toString());
			raw_counter = raw_counter + 1;
			logger.d("raw_counter in sendjson: "+String.valueOf(raw_counter));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	

	public void sendFinalJSON(int Type, String info, int useAudio) {
		try {
			logger.d("start to send inent");

				logger.d("start to send call start");
				TestObject.put("AveValue", info);
				TestObject.put("testType",Type);
				TestObject.put("Audioflag", Audio_flag);
				logger.d("RGBAvailable: "+String.valueOf(RGBAvailabe));
				TestObject.put("RGBAvailable", RGBAvailabe);
        		JSONObject tmp_object = new JSONObject();
        		try {
					tmp_object.put("deviceID", device_ID);
					tmp_object.put("deviceModel", deviceModel);
        		} catch (JSONException e) {
        			e.printStackTrace();
        		}
        		TestObject.put("device_info", tmp_object);
        		
        		tmp_object= null;
        		
				Intent it = new Intent();
				it.setAction("active_test");
				String tmp_intent_mes = TestObject.toString();
				it.putExtra("activeTest", tmp_intent_mes);
				it.setClass(this, MyService.class);
				this.startService(it);
				logger.d("send active test");
      			logger.d("send active test" + tmp_intent_mes);
    			raw_counter = 0;
            	TestObject = null;
            	TestObject = new JSONObject();		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}


