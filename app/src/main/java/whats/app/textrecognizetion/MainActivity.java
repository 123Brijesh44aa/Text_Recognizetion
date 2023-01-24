package whats.app.textrecognizetion;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 2003;
    private Button CameraBtn;
    private File photoFile;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog  = new ProgressDialog(this);
        progressDialog.setMessage("recognizing...");
        progressDialog.setTitle("Text Recognition");
        progressDialog.setCanceledOnTouchOutside(false);

        CameraBtn = findViewById(R.id.camera);

        CameraBtn.setOnClickListener(view->{
            photoFile = createPhotoFile();
            Uri fileUri = FileProvider.getUriForFile(this,"com.brijesh.fileprovider2",photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
            startActivityForResult(intent,CAMERA_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                progressDialog.show();
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Log.e("IMAGE_BITMAP","Width of Bitmap is : "+bitmap.getWidth()+", and Height is : "+bitmap.getHeight());

                // Getting Portrait Image Bitmap
                Bitmap portraitBitmap = flipIMage(bitmap);

                Log.e("IMAGE_BITMAP","Width of PortraitBitmap is : "+portraitBitmap.getWidth()+", and Height is : "+portraitBitmap.getHeight());
                // Rescaling the size of bitmap
                Bitmap rescaledBitmap = rescaledBitmap(portraitBitmap);
                Log.e("IMAGE_BITMAP","Width of RescaledBitmap is : "+rescaledBitmap.getWidth()+", and Height is : "+rescaledBitmap.getHeight());
                // compress Bitmap before sending
//                byte[] compressedBitmap = compressBitmap(rescaledBitmap);

                detectText(rescaledBitmap);
            }
        }
    }

    // RESCALING BITMAP
    private Bitmap rescaledBitmap(Bitmap bitmap){
        Bitmap rescaledBitmap;
        rescaledBitmap = Bitmap.createScaledBitmap(bitmap,
                (70*(bitmap.getWidth()))/(100),
                (70*(bitmap.getHeight()))/(100),
                false);
        return rescaledBitmap;
    }

    // COMPRESSING THE BITMAP
//    private byte[] compressBitmap(Bitmap bitmap){
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG,50,stream);
//        byte[] compressedBitmap;
//        compressedBitmap = stream.toByteArray();
//        return compressedBitmap;
//    }

    private File createPhotoFile(){
        File photoFileDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"ML_IMAGE_RECOGNIZER_");
        if (!photoFileDir.exists()){
            photoFileDir.mkdirs();
        }
        @SuppressLint("SimpleDateFormat")
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(photoFileDir.getPath()+File.separator+name+".jpg");
        return file;
    }

    // FOR ROTATION ISSUE
    private static int fixOrientation(Bitmap bitmap) {
        if (bitmap.getWidth() > bitmap.getHeight()) {
            return 90;
        }
        return 0;
    }

    // GETTING PORTRAIT IMAGE
    public static Bitmap flipIMage(Bitmap bitmap) {
//       fixing issue of image reflection due to front camera settings
        Matrix matrix = new Matrix();
        int rotation = fixOrientation(bitmap);
        matrix.postRotate(rotation);
//        matrix.preScale(-1, 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    private void detectText(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result = recognizer.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
//                        Log.e("TEXT_BLOCK",text.getText());
                        // Using Shared Preferences for sending data from one activity to another activity
                        sharedPreferences = getSharedPreferences("TEXT_DATA",MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putString("TEXT",text.getText());
                        editor.apply();
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
//                        intent.putExtra("TEXT",text.getText());
                        startActivity(intent);
                        progressDialog.cancel();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Could not recognize",Toast.LENGTH_LONG).show();
                        progressDialog.cancel();
                    }
                });
    }
}