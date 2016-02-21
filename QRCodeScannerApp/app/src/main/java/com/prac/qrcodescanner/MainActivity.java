package test.com.qrcodescanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.prac.qrcodescanner.Contents;
import com.prac.qrcodescanner.QRCodeEncoder;
import com.prac.qrcodescanner.R;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * read https://github.com/dm77/barcodescanner for more details
 * this is just basic example implemented
 */
public class MainActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private String TAG = "MainActivity";
    private LinearLayout viewLayout;
    private EditText viewText;
    private TextView scan, generate;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(R.layout.activity_main);                // Set the scanner view as the content view
        viewLayout = (LinearLayout) findViewById(R.id.Screen);
        viewText = (EditText) findViewById(R.id.text);
        scan = (TextView) findViewById(R.id.scan);
        generate = (TextView) findViewById(R.id.generate);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateModeActivated();
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanModeActivated();
            }
        });
        viewText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Find screen size
                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3 / 4;

                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(s.toString(),
                        null,
                        Contents.Type.TEXT,
                        BarcodeFormat.QR_CODE.toString(),
                        smallerDimension);
                try {
                    Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                    ImageView imageView = new ImageView(getApplicationContext());
                    imageView.setImageBitmap(bitmap);
                    generateModeActivated(imageView);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void generateModeActivated(ImageView imageView) {
        generateModeActivated();
        viewLayout.addView(imageView);
    }

    private void generateModeActivated() {
        viewLayout.removeAllViews();
        viewText.setVisibility(View.VISIBLE);
    }

    private void scanModeActivated() {
        viewLayout.removeAllViews();
        viewText.setVisibility(View.GONE);
        viewLayout.addView(mScannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onResume() {
        super.onResume();
        scanModeActivated();
    }


    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        String resultText = rawResult.getText();
        showDialog("Code Scan Results", rawResult.getBarcodeFormat().toString() + " : " + resultText);
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
    }

    public Dialog showDialog(String title, String msg) {

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setButton("Rescan?", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                mScannerView.startCamera();          // Start camera on resume
            }
        });
        alertDialog.show();
        return alertDialog;

    }
}