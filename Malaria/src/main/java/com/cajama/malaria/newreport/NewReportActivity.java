package com.cajama.malaria.newreport;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cajama.android.customviews.DateDisplayPicker;
import com.cajama.background.DataBaseHelper;
import com.cajama.malaria.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewReportActivity extends SherlockActivity{
    AssembleData assembleData;
    ViewFlipper VF;
    GridView new_report_photos_layout;
    ImageAdapter images;
    private static final int CAMERA_REQUEST = 1888;
    private static final int PHOTO_REQUEST = 4214;
	private static final String TAG = "NewReportActivity";
	private Uri fileUri;
    private String imageFilePath, required = "is a required field.";;
    private int displayedchild;
    private Resources res;
    private String[] step_subtitles;
    ArrayList<String> entryList = new ArrayList<String>();
    ArrayList<String> accountList = new ArrayList<String>();
    ArrayList<Map<String,String>> entries = new ArrayList<Map<String, String>>();
    Toast userToast, passToast, requiredToast;
    boolean isCancelDialogOpen = false, isDeleteDialogOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_report);

        userToast = Toast.makeText(getApplicationContext(), "No existing user!", Toast.LENGTH_LONG);
        passToast = Toast.makeText(getApplicationContext(), "Unmatched username and password!", Toast.LENGTH_LONG);
        requiredToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);

        TextView textView = (TextView) findViewById(R.id.progressText);
        textView.setVisibility(View.INVISIBLE);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar_Horizontal);
        progressBar.setVisibility(View.INVISIBLE);
        /*ProgressBar progressBar1 = (ProgressBar) findViewById(R.id.progressbar_default);
        progressBar1.setVisibility(View.INVISIBLE);*/

        /*Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);*/

        Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
        spinner.setAdapter(new CustomAdapter(NewReportActivity.this, R.layout.row, getResources().getStringArray(R.array.gender_array)));

        final Spinner spinner2 = (Spinner) findViewById(R.id.species_spinner);
        spinner2.setSelection(4);
        final Spinner spinner3 = (Spinner) findViewById(R.id.case_spinner);
        spinner2.setAdapter(new CustomAdapter(NewReportActivity.this, R.layout.diagnosis_row, getResources().getStringArray(R.array.species_array)));

        AdapterView.OnItemSelectedListener listener1 = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (getResources().getStringArray(R.array.species_array)[i].equalsIgnoreCase("none")) {
                    spinner3.setSelection(0);
                    spinner2.setEnabled(false);
                    spinner2.setClickable(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        spinner2.setOnItemSelectedListener(listener1);
        /*ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.species_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);*/
        spinner3.setAdapter(new CustomAdapter(NewReportActivity.this, R.layout.row, getResources().getStringArray(R.array.case_array)));

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (getResources().getStringArray(R.array.case_array)[i].equalsIgnoreCase("uncomplicated")) {
                    spinner2.setSelection(4);
                    spinner2.setEnabled(false);
                    spinner2.setClickable(false);
                }
                else {
                    spinner2.setEnabled(true);
                    spinner2.setClickable(true);
                    spinner2.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
        spinner3.setOnItemSelectedListener(listener);
        spinner3.setSelection(1);
        /*ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
                R.array.case_array, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);*/

        Spinner spinner4 = (Spinner) findViewById(R.id.region_spinner);
        spinner4.setAdapter(new CustomAdapter(NewReportActivity.this, R.layout.row, getResources().getStringArray(R.array.region_array)));
        //ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this,R.array.region_array, android.R.layout.simple_spinner_item);
        //adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner4.setAdapter(adapter4);


        VF = (ViewFlipper) findViewById(R.id.viewFlipper);
        getSupportActionBar().setSubtitle("Step 1 of " + VF.getChildCount());

        images = new ImageAdapter(this);
        new_report_photos_layout = (GridView) findViewById(R.id.new_report_photos_layout);
        new_report_photos_layout.setEmptyView(findViewById(R.id.empty_list_view));
        new_report_photos_layout.setAdapter(images);

        new_report_photos_layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	Intent intent = new Intent(getApplicationContext(), FullscreenPhotoActivity.class);
                File imageFile = new File(images.getItem(position).path);
                fileUri = Uri.fromFile(imageFile);
                intent.putExtra("pos", position);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, PHOTO_REQUEST);
            }
        });

        res = getResources();
        step_subtitles = new String[]{
                res.getString(R.string.patient_details),
                res.getString(R.string.patient_details),
                res.getString(R.string.patient_details),
                res.getString(R.string.slide_photos),
                res.getString(R.string.diagnosis),
                res.getString(R.string.summary),
                res.getString(R.string.submit)
        };
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int displayedchild = VF.getDisplayedChild();
        getSupportActionBar().setSubtitle(String.format("Step %d of %d - %s", displayedchild + 1, VF.getChildCount(), step_subtitles[displayedchild]));

        switch(displayedchild) {
            case 0: menu.findItem(R.id.action_prev).setTitle(R.string.cancel);
                    menu.findItem(R.id.action_photo).setVisible(false);
                    menu.findItem(R.id.action_next).setTitle(R.string.next);
                    break;
            case 1: menu.findItem(R.id.action_prev).setTitle(R.string.back);
                    menu.findItem(R.id.action_photo).setVisible(false);
                    menu.findItem(R.id.action_next).setTitle(R.string.next);
                    break;
            case 2: menu.findItem(R.id.action_prev).setTitle(R.string.back);
                    menu.findItem(R.id.action_photo).setVisible(false);
                    menu.findItem(R.id.action_next).setTitle(R.string.next);
                    break;
            case 3: menu.findItem(R.id.action_prev).setTitle(R.string.back);
                    menu.findItem(R.id.action_photo).setVisible(true);
                    menu.findItem(R.id.action_next).setTitle(R.string.next);
                    break;
            case 4: menu.findItem(R.id.action_prev).setTitle(R.string.back);
                    menu.findItem(R.id.action_photo).setVisible(false);
                    menu.findItem(R.id.action_next).setTitle(R.string.next);
                    break;
            case 5: menu.findItem(R.id.action_prev).setTitle(R.string.back);
                    menu.findItem(R.id.action_photo).setVisible(false);
                    menu.findItem(R.id.action_next).setTitle(R.string.next);
                    break;
            case 6: menu.findItem(R.id.action_prev).setTitle(R.string.back);
                    menu.findItem(R.id.action_photo).setVisible(false);
                    menu.findItem(R.id.action_next).setTitle(R.string.submit);
                    break;
            default: break;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.new_report, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        removeToasts();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(VF.getWindowToken(), 0);
        switch (item.getItemId()) {
            case R.id.action_prev:
                if (VF.getDisplayedChild() == 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder
                            .setTitle(R.string.warning)
                            .setMessage(R.string.new_report_cancel_warning)
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    for (int c=0; c<images.getCount(); c++) {
                                        File file = new File(images.getItem(c).path);
                                        file.delete();
                                    }

                                    isCancelDialogOpen = false;
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    isCancelDialogOpen = false;
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    if (!isCancelDialogOpen) {
                        isCancelDialogOpen = true;
                        alertDialog.show();
                    }
                } else {
                    VF.showPrevious();
                }
                invalidateOptionsMenu();
                return true;
            case R.id.action_next:
                //invalidateOptionsMenu();
                /*InputMethodManager imm1 = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(VF.getWindowToken(), 0);*/
                if(VF.getDisplayedChild() == 4){
                    generateSummary();
                    VF.showNext();
                }
                else if(VF.getDisplayedChild() != VF.getChildCount()-1) {
                    if (checkRequiredFields(VF.getDisplayedChild())) VF.showNext();
                    //return false;
                }
                else if(VF.getDisplayedChild() == VF.getChildCount()-1){
                    if (checkRequiredFields(VF.getDisplayedChild()) && checkCredentials()) submitFinishedReport();
                }
                invalidateOptionsMenu();
                return true;
            case R.id.action_photo:
                System.out.println("asdfasdf");
            	Intent cameraIntent = new Intent(this, Picture.class);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imageFilePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) +  "/" + timeStamp + "_slide.jpg";

                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imageFilePath)));
                startActivityForResult(cameraIntent, CAMERA_REQUEST);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkRequiredFields(int display) {
        switch (display) {
            case 2:
                EditText address = (EditText) findViewById(R.id.address);
                if (address.getText().toString().trim().length() == 0) {
                    //Toast.makeText(getApplicationContext(), "Address is a required field.", Toast.LENGTH_LONG).show();
                    requiredToast.setText("Address " + required);
                    requiredToast.show();
                    return false;
                }
                return true;
            case 6:
                EditText username = (EditText) findViewById(R.id.username);
                EditText password = (EditText) findViewById(R.id.password);
                if (username.getText().toString().trim().length() == 0) {
                    requiredToast.setText("Username " + required);
                }
                else if (password.getText().toString().trim().length() == 0) {
                    requiredToast.setText("Password " + required);
                }
                else return true;
                requiredToast.show();
                return false;
            default:
                return true;
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
        	String newimagepath = ((Uri) data.getParcelableExtra(android.provider.MediaStore.EXTRA_OUTPUT)).getPath();
            Log.d(TAG, newimagepath);
            File f = new File(newimagepath);
            if(f.exists()) {
                Log.d(TAG, "newimagepath exists");

                Bitmap bmp = null;

                while(bmp == null) {
                    bmp = decodeSampledBitmapFromResource(newimagepath, 100,100);
                }
                myBitmap bmpp = new myBitmap();
                bmpp.image = bmp;
                bmpp.path = newimagepath;

                images.AddImage(bmpp);
                images.notifyDataSetChanged();
            }
        } else if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "photo request");
            int pos = data.getIntExtra("pos", -1);

            if (pos != -1 ){
                File file = new File(images.getItem(pos).path);
                file.delete();

                images.remove(pos);
                images.notifyDataSetChanged();
            }
        } else Log.d(TAG, "wala sa cases");
    }

    @Override
    public void onBackPressed() {
        invalidateOptionsMenu();
        if (VF.getDisplayedChild() == 0) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setTitle(R.string.warning)
                    .setMessage(R.string.new_report_cancel_warning)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            for (int c=0; c<images.getCount(); c++) {
                                File file = new File(images.getItem(c).path);
                                file.delete();
                            }

                            isDeleteDialogOpen = false;
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            isDeleteDialogOpen = false;
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();

            if (!isDeleteDialogOpen) {
                isDeleteDialogOpen = true;
                alertDialog.show();
            }
        } else {
            VF.showPrevious();
        }
    }

    private String checkEmpty(String value){
        if (value.trim().length()==0) value = getString(R.string.no_input);
        return value;
    }

    private HashMap<String,String> putEntry(String label,String value){
        HashMap<String,String> line = new HashMap<String, String>();
        line.put("label",label);
        line.put("value",value);
        return line;
    }

    private ArrayList<Map<String,String>> buildSummary(){
        String fname, mname, lname, birthday, gender, diagnosisHuman, diagnosisNotes, photoCount, dateCreated, timeCreated, latitude,longitude;
        String caseMalaria,slideNumber, drugsGiven, examResult,age,address, region;
        //date & time
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        entries.clear();
        entryList.clear();
        //date
        dateCreated = today.format("%m/%d/%Y");
        entries.add(putEntry(getString(R.string.date_created), dateCreated));
        entryList.add(dateCreated);
        //time
        timeCreated = today.format("%H:%M:%S");
        entries.add(putEntry(getString(R.string.time_created),timeCreated));
        entryList.add(timeCreated);

        //latitude & longitude
        GetLocation getLoc = new GetLocation(this);
        //latitude
        latitude = getLoc.getLatitude();
        entries.add(putEntry(getString(R.string.latitude),latitude));
        entryList.add(latitude);
        //longitude
        longitude = getLoc.getLongitude();
        entries.add(putEntry(getString(R.string.longitude),longitude));
        entryList.add(longitude);

        //first name
        EditText editText1 = (EditText)findViewById(R.id.given_name_textfield);
        fname = editText1.getText().toString();
        fname = checkEmpty(fname);
        entries.add(putEntry(getString(R.string.given_name),fname));
        entryList.add(fname);

        //middle name
        EditText editText2=(EditText)findViewById(R.id.middle_name_textfield);
        mname=editText2.getText().toString();
        mname = checkEmpty(mname);
        entries.add(putEntry(getString(R.string.middle_name),mname));
        entryList.add(mname);

        //last name
        EditText editText3=(EditText)findViewById(R.id.last_name_textfield);
        lname=editText3.getText().toString();
        lname = checkEmpty(lname);
        entries.add(putEntry(getString(R.string.last_name),lname));
        entryList.add(lname);

        //birthday
        DateDisplayPicker dateDP=(DateDisplayPicker)findViewById(R.id.clientEditCreate_BirthDateDayPicker);
        birthday=dateDP.getText().toString();
        birthday = checkEmpty(birthday);
        entries.add(putEntry(getString(R.string.birthday),birthday));
        entryList.add(birthday);

        //age
        EditText editAge=(EditText)findViewById(R.id.age_textfield);
        age=editAge.getText().toString();
        age = checkEmpty(age);
        entries.add(putEntry(getString(R.string.age),age));
        entryList.add(age);

        //sex
        Spinner spinner1=(Spinner)findViewById(R.id.gender_spinner);
        gender=spinner1.getSelectedItem().toString();
        entries.add(putEntry(getString(R.string.sex),gender));
        entryList.add(gender);

        //address
        EditText editAddress=(EditText)findViewById(R.id.address);
        address=editAddress.getText().toString();
        address = checkEmpty(address);
        entries.add(putEntry(getString(R.string.address),address));
        entryList.add(address);

        //slide number
        EditText editTextSlide = (EditText) findViewById(R.id.slide_number);
        slideNumber = editTextSlide.getText().toString();
        slideNumber = checkEmpty(slideNumber);
        entries.add(putEntry(getString(R.string.slide_number),slideNumber));
        entryList.add(slideNumber);

        //region
        Spinner regionSpinner = (Spinner) findViewById(R.id.region_spinner);
        region = regionSpinner.getSelectedItem().toString();
        entries.add(putEntry(getString(R.string.region), region));
        entryList.add(region);

        //number of images
        GridView gridView1 = (GridView)findViewById(R.id.new_report_photos_layout);
        photoCount = Integer.toString(gridView1.getCount());
        //entries.add(putEntry(getString(R.string.num_photos),photoCount));

        //malaria case
        Spinner spinner3=(Spinner)findViewById(R.id.case_spinner);
        caseMalaria = spinner3.getSelectedItem().toString();
        entries.add(putEntry(getString(R.string.case_malaria),caseMalaria));
        entryList.add(caseMalaria);

        //malaria species
        Spinner spinner2=(Spinner)findViewById(R.id.species_spinner);
        diagnosisHuman=spinner2.getSelectedItem().toString();
        entries.add(putEntry(getString(R.string.diagnosis),diagnosisHuman));
        entryList.add(diagnosisHuman);

        //drugs given
        EditText editTextDrugs = (EditText)findViewById(R.id.drugs_given);
        drugsGiven = editTextDrugs.getText().toString();
        drugsGiven = checkEmpty(drugsGiven);
        entries.add(putEntry(getString(R.string.drugs_given),drugsGiven));
        entryList.add(drugsGiven);

        //exam result
        EditText editTextResult = (EditText)findViewById(R.id.exam_result);
        examResult = editTextResult.getText().toString();
        examResult = checkEmpty(examResult);
        entries.add(putEntry(getString(R.string.exam_result),examResult));
        entryList.add(examResult);

        //diagnostic notes
        EditText editText4=(EditText)findViewById(R.id.diagnostic_notes);
        diagnosisNotes =editText4.getText().toString();
        diagnosisNotes = checkEmpty(diagnosisNotes);
        entries.add(putEntry(getString(R.string.remarks),diagnosisNotes));
        entryList.add(diagnosisNotes);

        return entries;
    }

    private void generateSummary() {
        ArrayList<Map<String,String>> list = buildSummary();
        String[] from = {"label","value"};
        int[] to = {R.id.label, R.id.value};
        ListView lView = (ListView) findViewById(R.id.summaryLabels);
        //summaryAdapter adapter = new summaryAdapter(this, list);
        SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.summary_row, from, to);
        lView.setAdapter(adapter);
    }

    private String getAccountData(){
        String USERNAME, PASSWORD;
        EditText editText1=(EditText )findViewById(R.id.username);
        EditText editText2=(EditText )findViewById(R.id.password);
        USERNAME           =editText1.getText().toString().trim();
        PASSWORD           =editText2.getText().toString().trim();
        Log.v("write","USERNAME: " + USERNAME + " PASSWORD: " + PASSWORD);
        accountList.add(USERNAME);
        accountList.add(PASSWORD);
        Log.v("write","stuff: " + accountList.get(0) + accountList.get(1));

        return USERNAME;
    }

    private void submitFinishedReport() {

        ArrayList<String> imageList = new ArrayList<String>();
        for (int i=0; i < images.getCount();i++ ) imageList.add(i,images.getItem(i).path);

        String USERNAME = getAccountData();

        assembleData = new AssembleData(getApplicationContext(),entryList,imageList,accountList,USERNAME);
        TextView textView = (TextView) findViewById(R.id.progressText);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar_Horizontal);
        assembleData.setView(progressBar, textView);
        textView.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        Thread assembleDataThread = new Thread(myThread);
        assembleDataThread.start();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "asdasdasd");
            if (intent.getStringExtra("finish").equals("finish")) finish();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(AssembleData.BROADCAST_FINISH));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private Runnable myThread = new Runnable(){

        @Override
        public void run()
        {
            assembleData.start();
        }
    };

    private boolean checkCredentials() {
    	byte[] passBytes;
    	String USERNAME, PASSWORD;

        EditText editText1=(EditText )findViewById(R.id.username);
        EditText editText2=(EditText )findViewById(R.id.password);
        USERNAME           =editText1.getText().toString().trim();
        PASSWORD           =editText2.getText().toString().trim();

        DataBaseHelper helper = new DataBaseHelper(this);
        helper.openDataBase();

        Cursor cursor = helper.getPair(USERNAME);
        
        try {
			passBytes = PASSWORD.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
            passBytes = sha.digest(passBytes);
            Log.d(TAG+" checkCredentials()", USERNAME);
            
        	if (cursor == null) {
        		//Toast.makeText(getApplicationContext(), "No existing user!", Toast.LENGTH_LONG).show();
                userToast.show();
        		return false;
        	}
        	
        	cursor.moveToFirst();
            byte[] test = cursor.getString(1).getBytes("UTF-8");
            MessageDigest s = MessageDigest.getInstance("SHA-1");
            test = s.digest(test);
            String f = byteArrayToHexString(test);

            Log.d(TAG, cursor.getString(1));
            Log.d(TAG, f);

            String temp = byteArrayToHexString(passBytes);
            Log.d(TAG, temp);

        	if (!cursor.getString(1).equals(temp)) {
        		//Toast.makeText(getApplicationContext(), "Unmatched username and password!", Toast.LENGTH_LONG).show();
                passToast.show();
        		return false;
        	}
        	
        	Log.d(TAG+" checkCredentials()", "login success!");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
            if (cursor != null) cursor.close();
            helper.close();
        }
        
    	return true;
    }

    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
    
    public static Bitmap decodeSampledBitmapFromResource(String filepath, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filepath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filepath, options);
	}

    private void removeToasts() {
        if (requiredToast.getView().isShown()) requiredToast.cancel();
        if (userToast.getView().isShown()) userToast.cancel();
        if (passToast.getView().isShown()) passToast.cancel();
    }
}
