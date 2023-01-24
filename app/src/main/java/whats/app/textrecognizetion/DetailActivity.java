package whats.app.textrecognizetion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private TextView textView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        textView = findViewById(R.id.processed_text);


        // Using Shared Preferences to get Data
        sharedPreferences = getSharedPreferences("TEXT_DATA", MODE_PRIVATE);
        String data = sharedPreferences.getString("TEXT","");
        textView.setText(data);
    }

//    private Bitmap decompressedBitmap(byte[] bytes){
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//        return bitmap;
//    }
}