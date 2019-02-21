package cookbook.chapter9.portingexecutableaui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ImageView img1, img2;
	private TextView text1, text2;
	private EditText edit1;
	private Context mContext;
	private static final int ACTIVITY_SELECT_IMAGE = 1;
	private static final String DELIMITER = " ";
	private static final String TEST_FILE_NAME = "cookbook_ch9_test.png";
	private String inputImagePath = "";
	private String outputImageDir = "/sdcard/";
	private String increaseOrDecrese = "+";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getApplicationContext();
		setContentView(R.layout.activity_main);
		//copy the test file from assets to /sdcard/
		copyAssetFile(TEST_FILE_NAME, "/sdcard/" + TEST_FILE_NAME);
		//text view for the program output
		text1 = (TextView) findViewById(R.id.text1);
		text1.setText("");
		//text view to display image dimension
		text2 = (TextView) findViewById(R.id.text2);
		text2.setText("");
		//spinner to choose either increase or decrease the image dimension
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		ArrayList<String> options = new ArrayList<String>();
		options.add("+");
		options.add("-");
		ArrayAdapter<String> profileAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, options);
		profileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(profileAdapter);	
		spinner.setSelection(0);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View view,
					int pos, long id) {
				if (0 == pos) {
					increaseOrDecrese = "+"; 
				} else {
					increaseOrDecrese = "-";
				}
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		//edit the amount of change
		edit1 = (EditText) findViewById(R.id.editText1);
		edit1.setText("200");	//set 50 pixels as default
		edit1.setInputType(InputType.TYPE_CLASS_PHONE);
		//choose an image
		Button btn1 = (Button) findViewById(R.id.button1);
		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				text1.setText("");
				Intent i = new Intent(Intent.ACTION_PICK,
			               android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, ACTIVITY_SELECT_IMAGE); 
			}
		});
		//change width 
		Button btn2 = (Button) findViewById(R.id.button2);
		btn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//construct the command 
				String inputImgFileName = inputImagePath.substring(inputImagePath.lastIndexOf("/")+1);
				String outputImgFileName = System.currentTimeMillis() + inputImgFileName;
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append(inputImagePath).append(DELIMITER)
				.append(outputImageDir).append(outputImgFileName).append(DELIMITER)
				.append("h").append(increaseOrDecrese).append(edit1.getText().append(DELIMITER));
				text1.setText(strBuilder.toString());
				naMain(strBuilder.toString());
				//display the output image
				Uri uri = Uri.fromFile(new File(outputImageDir + outputImgFileName));
				img2.setImageURI(uri);
				text2.setText(getImageDimension(inputImagePath) + ";" + 
						getImageDimension(outputImageDir + outputImgFileName));
			}
		});
		//change height
		Button btn3 = (Button) findViewById(R.id.button3);
		btn3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String inputImgFileName = inputImagePath.substring(inputImagePath.lastIndexOf("/")+1);
				String outputImgFileName = System.currentTimeMillis() + inputImgFileName;
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append(inputImagePath).append(DELIMITER)
				.append(outputImageDir).append(outputImgFileName).append(DELIMITER)
				.append("v").append(increaseOrDecrese).append(edit1.getText().append(DELIMITER));
				text1.setText(strBuilder.toString());
				naMain(strBuilder.toString());
				//display the output image
				Uri uri = Uri.fromFile(new File(outputImageDir + outputImgFileName));
				img2.setImageURI(uri);
				text2.setText(getImageDimension(inputImagePath) + ";" + 
						getImageDimension(outputImageDir + outputImgFileName));
			}
		});
		//display the before and after images
		img1 = (ImageView) findViewById(R.id.imageView1);
		//use the file we copied from assets folder as the default
		Uri uri = Uri.fromFile(new File("/sdcard/" + TEST_FILE_NAME));
		img1.setImageURI(uri);
		inputImagePath = "/sdcard/" + TEST_FILE_NAME;
		text2.setText(getImageDimension(inputImagePath));
		img2 = (ImageView) findViewById(R.id.imageView2);
	}
	
	private void copyAssetFile(String assetFilename, String outFileName) {
	    InputStream myInput;
		try {
			myInput = mContext.getAssets().open(assetFilename);
		    OutputStream myOutput = new FileOutputStream(outFileName);
		    //transfer bytes from the assetFile to the outputfile
		    byte[] buffer = new byte[2048];
		    int length;
		    while ((length = myInput.read(buffer))>0){
		        myOutput.write(buffer, 0, length);
		    }
		    myOutput.flush();
		    myOutput.close();
		    myInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, 
		       Intent imageReturnedIntent) {
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
	    switch(requestCode) { 
	    case ACTIVITY_SELECT_IMAGE:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = imageReturnedIntent.getData();
	            String[] filePathColumn = {MediaStore.Images.Media.DATA};
	            Cursor cursor = getContentResolver().query(
	                               selectedImage, filePathColumn, null, null, null);
	            cursor.moveToFirst();
	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            String filePath = cursor.getString(columnIndex);
	            cursor.close();
	            if (!filePath.endsWith(".png")) {
	            	text1.setText("Only png images are supported!");
	            	Toast.makeText(mContext, "Only png images are supported, please select a png image!", Toast.LENGTH_SHORT).show();
	            } else {
	            	inputImagePath = filePath;
	            	img1.setImageURI(selectedImage);
	            	text2.setText(getImageDimension(inputImagePath));
	            }
	        }
	    }
	}
	
	private String getImageDimension(String pFilePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		//Returns null, sizes are in the options variable
		BitmapFactory.decodeFile(pFilePath, options);
		int width = options.outWidth;
		int height = options.outHeight;
		return width + "x" + height;
	}
	
	private static native int naMain(String cmdStr);
    
    static {
        System.loadLibrary("fusch");
    }
}
