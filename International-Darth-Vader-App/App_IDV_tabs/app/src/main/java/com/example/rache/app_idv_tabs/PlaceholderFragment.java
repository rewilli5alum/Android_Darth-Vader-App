package com.example.rache.app_idv_tabs;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.example.rache.app_idv_tabs.R.id.titleEditText;

/**
 * Created by rache on 9/12/2016.
 */
public class PlaceholderFragment extends Fragment {

    private static final String COGNITO_POOL_ID = "us-east-1:4073e1ea-8a8b-49e9-900a-d4426ef84497";
    private static final String BUCKET_NAME = "vader-raw-audio";

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String LOG_TAG = "ALERT";
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private EditText titleText;
    private EditText tagText;
    private CountDownTimer recordTimer;
    private EditText recordCountdown;
    private CheckBox explicitBox;
    private Button recordAudio;
    private Button previewAudio;
    private Button stopRecordAudio;
    private Button uploadAudio;
    private File idvClipsFilePath;
    private String mOutputFile;

    private ListView clipList;
    private Spinner filtersSpinner;
    private Button refreshButton;
    private Button playButton;
    private InputStream is;
    private Object selectedClip;
    private Object selectedFilter;
    private EditText tagSearch;
    private Button applyTagSearch;
    private CheckBox isExplicitTagSearch;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private BroadcastReceiver btReceiver;
    private BroadcastReceiver btConnection;
    private ConnectBT btThread;
    private String deviceInfo = " ";

    private int pitchValue;
    private int speedValue;
    private int trebleValue;
    private int bassValue;
    private int overdriveValue;

    private int counter = 0;

    public PlaceholderFragment() {
    }

    // Returns a new instance of this fragment for the given section number
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*** Upload tab ***/
        if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
            final View rootView = inflater.inflate(R.layout.fragment_upload_audio, container, false);

            /* Widget initialization */
            titleText = (EditText) rootView.findViewById(titleEditText);
            tagText = (EditText) rootView.findViewById(R.id.tagEditText);
            explicitBox = (CheckBox) rootView.findViewById(R.id.explicitCheckBox);
            filtersSpinner = (Spinner) rootView.findViewById(R.id.filterSpinner);
            recordAudio = (Button) rootView.findViewById(R.id.recordButton);
            stopRecordAudio = (Button) rootView.findViewById(R.id.stopRecordButton);
            previewAudio = (Button) rootView.findViewById(R.id.previewButton);
            uploadAudio = (Button) rootView.findViewById(R.id.uploadButton);
            recordCountdown = (EditText) rootView.findViewById(R.id.recordTimer);

            /* Countdown timer initialization */
            recordTimer = new CountDownTimer(16000, 1000) {

                public void onTick(long millisUntilFinished) {
                    recordCountdown.setText("" + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    recordCountdown.setText("");
                    stopRecording();
                }
            };

            /* OnClickListeners for Title and Tag entries */
            titleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    titleTagDialog(view);
                }
            });

            tagText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    titleTagDialog(view);
                }
            });

            /* Audio File initialization */
            File existingFile = Environment.getExternalStorageDirectory();
            String existingFilePath = existingFile.getPath();
            idvClipsFilePath = new File(existingFilePath+"/IDVclips");
            boolean success;

            // Check if directory already created/exists
            if(idvClipsFilePath.isDirectory()){
                if(idvClipsFilePath.exists()){
                    Log.i(LOG_TAG, "Directory exists and is already created");
                }
            }else{
                success = idvClipsFilePath.mkdir();
                Log.i(LOG_TAG, "File directory successfully created: " + success);
            }
            //mOutputFile = foo + "/IDV.m4a"; <----DO NOT DELETE. EVER.

            /* Record and playback audio */
            recordAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    // Error checking: cannot contain / or \
                    if(titleText.getText().toString().contains("/") ||
                            titleText.getText().toString().contains("\\") ){
                        Toast.makeText(getContext(),
                                "Title cannot contain / or \\ ", Toast.LENGTH_SHORT).show();
                    }else if(tagText.getText().toString().contains("/") ||
                            tagText.getText().toString().contains("\\")){
                        Toast.makeText(getContext(),
                                "Tags cannot contain / or \\ ", Toast.LENGTH_SHORT).show();
                    }else if(!titleText.getText().toString().trim().equals("")){
                        mOutputFile = idvClipsFilePath + "/" +
                                titleText.getText().toString() + ".m4a";
                        startRecording();
                        recordTimer.start();
                    }else{
                        // If user doesn't give a title, provides default title Voice+counter
                        Toast.makeText(getContext(),
                                "This clip will be named: \"Voice0" + counter +"\"",
                                Toast.LENGTH_SHORT).show();
                        mOutputFile = idvClipsFilePath + "/" + "Voice" + counter + ".m4a";
                        counter++;
                        startRecording();
                        recordTimer.start();
                    }
                }
            });

            stopRecordAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    recordTimer.cancel();
                    stopRecording();
                    recordCountdown.setText("");
                }
            });

            previewAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    startPlaying();
                }
            });

            uploadAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // First check if connected to wifi
                    ConnectivityManager connManager = (ConnectivityManager)
                            getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if(mWifi.isConnected()){
                        if(!(mOutputFile==null)){
                                Context context = getActivity().getApplicationContext();
                                UploadThread ut = new UploadThread(mOutputFile, titleText, tagText,
                                        explicitBox, context, BUCKET_NAME, counter);
                                ut.start();
                                Toast.makeText(getContext(),
                                        "Upload complete", Toast.LENGTH_SHORT).show();
                                titleText.setText("");
                                tagText.setText("");
                        }else{
                            Toast.makeText(getContext(),
                                    "Please record a clip to upload", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getContext(),
                                "Please connect to a Wifi network", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return rootView;

        } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
            /** Play tab (Bluetooth playback)**/
            final View rootView = inflater.inflate(R.layout.fragment_bluetooth_operation, container,
                    false);
            Context context = getContext();
            clipList = (ListView) rootView.findViewById(R.id.availableClipList);
            refreshButton = (Button) rootView.findViewById(R.id.refreshButton);
            playButton = (Button) rootView.findViewById(R.id.playButton);
            filtersSpinner = (Spinner) rootView.findViewById(R.id.filterSpinner);
            tagSearch = (EditText)rootView.findViewById(R.id.tagSearch);
            applyTagSearch = (Button)rootView.findViewById(R.id.applyTagSearch);
            isExplicitTagSearch = (CheckBox)rootView.findViewById(R.id.isExplicitTag);

            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            int REQUEST_ENABLE_BT = 66;

            // Check that bluetooth is available and enabled on device
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            if(mBluetoothAdapter.isDiscovering()) mBluetoothAdapter.cancelDiscovery();

            // Find devices already paired to Pi and connect to them
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0) {
                for(BluetoothDevice btDevice : pairedDevices) {
                    if (btDevice.getName().contains("raspberrypi")) {
                        BluetoothDevice device = btDevice;
                        btThread = new ConnectBT(device.getAddress(), mBluetoothAdapter);
                        btThread.start();
                        break;
                    }
                }
            }

            // Checking for successful Bluetoooth connection
            btConnection = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                        Toast.makeText(getContext(),
                                "Connected to Vader via Bluetooth", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            // Checking for when Bluetooth connection disconnects
            btReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                        BluetoothDevice device =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        deviceInfo = device.getName() + "-" + device.getAddress();
                        if(deviceInfo.contains("raspberrypi")){
                            Toast.makeText(getContext(),
                                    "Bluetooth has disconnected. Please standby",
                                    Toast.LENGTH_SHORT).show();

                            String deviceAddress = deviceInfo.substring((deviceInfo.indexOf("-")+1),
                                    (deviceInfo.length()));
                            btThread = new ConnectBT(deviceAddress, mBluetoothAdapter);
                            btThread.start();
                        }
                    }
                }
            };

            // Intent filter for successful Bluetooth connection
            IntentFilter btConnected = new IntentFilter();
            btConnected.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            getActivity().registerReceiver(btReceiver, btConnected);

            // Intent filter for disconnected Bluetooth connection
            IntentFilter btCnxn = new IntentFilter();
            btCnxn.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            getActivity().registerReceiver(btReceiver, btCnxn);

            // Compiling list of audio clips
            final ArrayAdapter forAvailableClips =
                    new ArrayAdapter(context, android.R.layout.simple_list_item_1);
            forAvailableClips.clear();
            clipList.setAdapter(forAvailableClips);

            clipList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedClip = clipList.getItemAtPosition(i);
                    Toast.makeText(getContext(),
                            "Selected clip: " + selectedClip, Toast.LENGTH_SHORT).show();
                }
            });

            filtersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedFilter = filtersSpinner.getItemAtPosition(i).toString();
                    if(selectedFilter.toString().contains("Custom")) scrollBarDialog(view);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(btThread == null){
                        Toast.makeText(getContext(),
                                "Please connect to Vader via Bluetooth", Toast.LENGTH_SHORT).show();
                    }else {
                        forAvailableClips.clear();
                        mBluetoothSocket = btThread.getSocket();

                        try{
                            mBluetoothSocket.getOutputStream().write("<rfDir>".getBytes());
                            is = mBluetoothSocket.getInputStream();

                            byte[] buffer = new byte[256];
                            int bBytes;
                            String readMessage = "";

                            while(true){
                                mBluetoothSocket.getOutputStream().write("<zzz>".getBytes());
                                bBytes = is.read(buffer);
                                readMessage = new String(buffer,0,bBytes);
                                if(readMessage.contains("<stop>"))break;
                                String tagR = Integer.toString(readMessage.indexOf(">"));
                                String tagL = Integer.toString(readMessage.indexOf("<"));
                                readMessage = readMessage.substring((Integer.parseInt(tagL)+1),
                                        Integer.parseInt(tagR));

                                forAvailableClips.add(readMessage);
                            }
                            forAvailableClips.notifyDataSetChanged();
                            Toast.makeText(getContext(),
                                    "Refreshing clip list", Toast.LENGTH_SHORT).show();
                        }catch(Exception e){
                            Log.e(LOG_TAG, "BluetoothSocket connection failed: " + e);
                            Toast.makeText(getContext(),
                                    "Bluetooth Connection failed. \nPlease standby",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try{
                        String clipName = selectedClip.toString();
                        clipName = clipName.trim();
                        clipName = "<" + clipName + ">";
                        String filterName = selectedFilter.toString();
                        filterName = filterName.trim();

                        // No filter applied
                        if(filterName.contains("None")){

                            mBluetoothSocket.getOutputStream().write("<dvPlay>".getBytes());
                            SystemClock.sleep(50);

                            mBluetoothSocket.getOutputStream().write(clipName.getBytes());
                            Toast.makeText(getContext(), "Playing clip", Toast.LENGTH_SHORT).show();
                        }else if(filterName.contains("Darth")) {
                            // Darth Vader filter applied
                            mBluetoothSocket.getOutputStream().write("<modPlay>".getBytes());
                            SystemClock.sleep(50);
                            mBluetoothSocket.getOutputStream().write(clipName.getBytes());
                            SystemClock.sleep(50);

                            //pitch
                            mBluetoothSocket.getOutputStream().write("<-190>".getBytes());
                            SystemClock.sleep(50);

                            //speed
                            mBluetoothSocket.getOutputStream().write("<1.1>".getBytes());
                            SystemClock.sleep(50);

                            //treble
                            mBluetoothSocket.getOutputStream().write("<25>".getBytes());
                            SystemClock.sleep(50);

                            //bass
                            mBluetoothSocket.getOutputStream().write("<6>".getBytes());
                            SystemClock.sleep(50);

                            //overdrive
                            mBluetoothSocket.getOutputStream().write("<22>".getBytes());
                            SystemClock.sleep(50);

                            //loudness
                            mBluetoothSocket.getOutputStream().write("<-10>".getBytes());
                            SystemClock.sleep(50);

                            //echo gain in
                            mBluetoothSocket.getOutputStream().write("<0.8>".getBytes());
                            SystemClock.sleep(50);

                            //echo gain out
                            mBluetoothSocket.getOutputStream().write("<0.9>".getBytes());
                            SystemClock.sleep(50);

                            //echo delay
                            mBluetoothSocket.getOutputStream().write("<73>".getBytes());
                            SystemClock.sleep(50);

                            //echo decay
                            mBluetoothSocket.getOutputStream().write("<0.3>".getBytes());
                            Toast.makeText(getContext(), "Playing clip", Toast.LENGTH_SHORT).show();
                        }else if(filterName.contains("3PO")){
                            // C-3PO filter applied
                            mBluetoothSocket.getOutputStream().write("<modPlay>".getBytes());
                            SystemClock.sleep(50);
                            mBluetoothSocket.getOutputStream().write(clipName.getBytes());
                            SystemClock.sleep(50);

                            //pitch
                            mBluetoothSocket.getOutputStream().write("<67>".getBytes());
                            SystemClock.sleep(50);

                            //speed
                            mBluetoothSocket.getOutputStream().write("<1.03>".getBytes());
                            SystemClock.sleep(50);

                            //treble
                            mBluetoothSocket.getOutputStream().write("<22>".getBytes());
                            SystemClock.sleep(50);

                            //bass
                            mBluetoothSocket.getOutputStream().write("<15>".getBytes());
                            SystemClock.sleep(50);

                            //overdrive
                            mBluetoothSocket.getOutputStream().write("<10>".getBytes());
                            SystemClock.sleep(50);

                            //loudness
                            mBluetoothSocket.getOutputStream().write("<-9>".getBytes());
                            SystemClock.sleep(50);

                            //echo gain in
                            mBluetoothSocket.getOutputStream().write("<0.8>".getBytes());
                            SystemClock.sleep(50);

                            //echo gain out
                            mBluetoothSocket.getOutputStream().write("<0.9>".getBytes());
                            SystemClock.sleep(50);

                            //echo delay
                            mBluetoothSocket.getOutputStream().write("<72>".getBytes());
                            SystemClock.sleep(50);

                            //echo decay
                            mBluetoothSocket.getOutputStream().write("<0.2>".getBytes());
                            Toast.makeText(getContext(), "Playing clip", Toast.LENGTH_SHORT).show();
                        }else if(filterName.contains("Custom")){
                            // Custom filter applied
                            mBluetoothSocket.getOutputStream().write("<modPlay>".getBytes());
                            SystemClock.sleep(50);
                            mBluetoothSocket.getOutputStream().write(clipName.getBytes());
                            SystemClock.sleep(50);

                            //pitch
                            String entry = String.valueOf(pitchValue-1000);
                            entry = "<" + entry + ">";
                            mBluetoothSocket.getOutputStream().write(entry.getBytes());
                            SystemClock.sleep(50);

                            //speed
                            entry = String.valueOf(speedValue);
                            entry = "<" + entry + ">";
                            mBluetoothSocket.getOutputStream().write(entry.getBytes());
                            SystemClock.sleep(50);

                            //treble
                            entry = String.valueOf(trebleValue);
                            entry = "<" + entry + ">";
                            mBluetoothSocket.getOutputStream().write(entry.getBytes());
                            SystemClock.sleep(50);

                            //bass
                            entry = String.valueOf(bassValue);
                            entry = "<" + entry + ">";
                            mBluetoothSocket.getOutputStream().write(entry.getBytes());
                            SystemClock.sleep(50);

                            //overdrive
                            entry = String.valueOf(overdriveValue);
                            entry = "<" + entry + ">";
                            mBluetoothSocket.getOutputStream().write(entry.getBytes());
                            SystemClock.sleep(50);

                            //echo gain in
                            mBluetoothSocket.getOutputStream().write("<0.8>".getBytes());
                            SystemClock.sleep(50);

                            //echo gain out
                            mBluetoothSocket.getOutputStream().write("<0.9>".getBytes());
                            SystemClock.sleep(50);

                            //echo delay
                            mBluetoothSocket.getOutputStream().write("<72>".getBytes());
                            SystemClock.sleep(50);

                            //echo decay
                            mBluetoothSocket.getOutputStream().write("<0.3>".getBytes());
                            Toast.makeText(getContext(), "Playing clip", Toast.LENGTH_SHORT).show();
                        }
                    } catch(Exception e){
                        Log.e(LOG_TAG, "NOPE: " + e);
                        Toast.makeText(getContext(),
                                "Please refresh clip list and select a clip",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            tagSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchTagDialog(view);
                }
            });

            applyTagSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String entry = tagSearch.getText().toString();

                    if(mBluetoothSocket == null){
                        Toast.makeText(getContext(),
                                "Please connect to a Vader via Bluetooth",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        searchTagDialog(view);
                        boolean isExplicit = isExplicitTagSearch.isChecked();
                        String explicitValue = isExplicit ? "t" : "f";

                        if(!entry.equals("")){
                            try{
                                mBluetoothSocket.getOutputStream().write("<tagFltr>".getBytes());
                                SystemClock.sleep(50);
                                mBluetoothSocket.getOutputStream().write(explicitValue.getBytes());
                                SystemClock.sleep(50);
                                mBluetoothSocket.getOutputStream().write("<tag>".getBytes());
                                SystemClock.sleep(50);
                                entry = "<" + entry + ">";
                                mBluetoothSocket.getOutputStream().write(entry.getBytes());
                                Toast.makeText(getContext(),
                                        "Search applied. Press 'Refresh' to see changes",
                                        Toast.LENGTH_SHORT).show();
                            }catch(IOException ioe){
                                Log.e(LOG_TAG, "Error with tag search: " + ioe);
                            }
                        }else {
                            try{
                                mBluetoothSocket.getOutputStream().write("<tagFltr>".getBytes());
                                SystemClock.sleep(50);
                                mBluetoothSocket.getOutputStream().write(explicitValue.getBytes());
                                SystemClock.sleep(50);
                                mBluetoothSocket.getOutputStream().write("<no>".getBytes());
                                Toast.makeText(getContext(),
                                        "Search applied. Press 'Refresh' to see changes",
                                        Toast.LENGTH_SHORT).show();
                            }catch(IOException ioe){
                                Log.e(LOG_TAG, "Error with tag search: " + ioe);
                            }
                        }
                    }
                    tagSearch.setText("");
                }
            });

            return rootView;
        } else {
            View rootView = inflater.inflate(R.layout.fragment_control_audio, container, false);
           return rootView;
        }
    }

    private void startRecording() {
        // Checking if recorder is already being used
        if(mRecorder != null) mRecorder.release();

        mRecorder = new MediaRecorder();
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setOutputFile(mOutputFile);
        } catch(RuntimeException re){
            Log.e(LOG_TAG, "MediaRecorder 'setAudioSource' function failed: " + re);
        }

        try {
            mRecorder.prepare();
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "MediaRecorder 'prepare()' failed: " + ioe);
        }

        try{
            mRecorder.start();
            recordTimer.start();

        }catch(IllegalStateException ise){
            Log.e(LOG_TAG, "MediaRecorder 'mRecorder.start()' function failed: " + ise);
        }
    }

    private void stopRecording() {
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void startPlaying(){
        if(mPlayer != null){
            try{
                mPlayer.release();
            }catch(Exception e){
                Log.e(LOG_TAG, "MediaPlayer, unable to release: " + e);
            }
        }

        mPlayer = new MediaPlayer();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(mOutputFile);
            mPlayer.setDataSource(fis.getFD());
            try{
                mPlayer.prepare();
            }catch(Exception e){
                Log.e(LOG_TAG, "MediaPlayer, unable to prepare: " + e);
            }
        } catch(Exception e){
            Log.e(LOG_TAG, "MediaPlayer error: " + e);
        }finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
        if(mPlayer != null) mPlayer.start();
    }

    public int searchTagDialog(View rootView){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater =
                (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);

        final View layoutView = inflater.inflate(R.layout.dialog_search_tag,
                (ViewGroup)rootView.findViewById(R.id.layout_dialog_tag_search));

        final EditText tagSearchText = (EditText)layoutView.findViewById(R.id.searchTagD);

        popDialog.setTitle("Tag search");
        popDialog.setView(layoutView);

        if(!tagSearch.getText().toString().equals(""))
            tagSearchText.setText(tagSearch.getText().toString().trim());

        popDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateTagSearchText(tagSearchText.getText().toString());
                dialogInterface.dismiss();
            }
        });

        popDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        popDialog.create();
        popDialog.show();

        return 1;
    }

    public int titleTagDialog(View rootView){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);

        final View layoutView = inflater.inflate(R.layout.dialog_title_text_layout,
                (ViewGroup)rootView.findViewById(R.id.layout_dialog_title_tag));

        final EditText title = (EditText)layoutView.findViewById(R.id.titleTextD);
        final EditText tag = (EditText)layoutView.findViewById(R.id.tagTextD);

        popDialog.setTitle("Audio Clip Information");
        popDialog.setView(layoutView);

        if(!titleText.getText().toString().equals(""))
            title.setText(titleText.getText().toString().trim());
        if(!tagText.getText().toString().equals(""))
            tag.setText(tagText.getText().toString().trim());

        popDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateTitleText(title.getText().toString());
                updateTagText(tag.getText().toString());
                dialogInterface.dismiss();
            }
        });

        popDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        popDialog.create();
        popDialog.show();

        return 1;
    }

    public int scrollBarDialog(View rootView){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater =
                (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);

        final View layoutView = inflater.inflate(R.layout.dialog_layout,
                (ViewGroup)rootView.findViewById(R.id.layout_dialog));

        final TextView text1 = (TextView)layoutView.findViewById(R.id.pitchText);
        TextView text2 = (TextView)rootView.findViewById(R.id.speedText);

        popDialog.setTitle("Custom Filter Settings");
        popDialog.setView(layoutView);

        SeekBar seek1 = (SeekBar)layoutView.findViewById(R.id.pitchBar);
        SeekBar seek2 = (SeekBar)layoutView.findViewById(R.id.speedBar);
        SeekBar seek3 = (SeekBar)layoutView.findViewById(R.id.trebleBar);
        SeekBar seek4 = (SeekBar)layoutView.findViewById(R.id.bassBar);
        SeekBar seek5 = (SeekBar)layoutView.findViewById(R.id.overdriveBar);

        seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                pitchValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //empty
            }
        });

        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                speedValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //empty
            }
        });

        seek3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                trebleValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //empty
            }
        });
        seek4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                bassValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //empty
            }
        });
        seek5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                overdriveValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //empty
            }
        });


        popDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        popDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        popDialog.create();
        popDialog.show();

        return 1;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void updateTitleText(String title){
        if(title!=null)titleText.setText(title);
    }

    public void updateTagText(String tag){
        if(tag!=null)tagText.setText(tag);
    }

    public void updateTagSearchText(String tagSearchString){
        if(tagSearchString!=null)tagSearch.setText(tagSearchString);
    }
}

