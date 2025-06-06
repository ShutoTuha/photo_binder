package com.example.photo_binder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
// import android.widget.LinearLayout; // LinearLayout は不要になるのでコメントアウトまたは削除
import android.widget.GridLayout; // GridLayout をインポート
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class photo_area extends AppCompatActivity {

    private static final String TAG = "PhotoAreaActivity";
    // private Uri selectedImageUri;
    // private ImageView selectedImageView;

    private final List<Uri> imageUris = new ArrayList<>();
    private GridLayout imageContainerLayout; // LinearLayout から GridLayout に変更

    private final ActivityResultLauncher<Intent> openImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                                Intent data = result.getData();
                                if (data.getClipData() != null) {
                                    int count = data.getClipData().getItemCount();
                                    for (int i = 0; i < count; i++) {
                                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                        if (imageUri != null) {
                                            imageUris.add(imageUri);
                                            Log.d(TAG, "追加された画像のURI: " + imageUri.toString());
                                        }
                                    }
                                } else if (data.getData() != null) {
                                    Uri imageUri = data.getData();
                                    if (imageUri != null) {
                                        imageUris.add(imageUri);
                                        Log.d(TAG, "追加された画像のURI: " + imageUri.toString());
                                    }
                                }

                                if (!imageUris.isEmpty()) {
                                    Toast.makeText(photo_area.this, imageUris.size() + "枚の画像が選択されました", Toast.LENGTH_LONG).show();
                                    displayImages();
                                } else {
                                    Log.e(TAG, "有効な画像URIが取得できませんでした。");
                                    Toast.makeText(photo_area.this, "画像URIの取得に失敗しました。", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Log.d(TAG, "画像選択がキャンセルされたか、失敗しました。");
                                Toast.makeText(photo_area.this, "画像選択がキャンセルされました。", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_area);

        imageContainerLayout = findViewById(R.id.image_container_layout); // GridLayout を取得
        // GridLayout の列数を設定 (XMLでも設定可能ですが、コードからも設定できます)
        // imageContainerLayout.setColumnCount(3); // XMLで設定する場合は不要

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button comebutton = findViewById(R.id.comebutton);
        comebutton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Button addbutton = findViewById(R.id.addbutton);
        addbutton.setOnClickListener(v -> {
            openImagePicker();
        });

        if (!imageUris.isEmpty()) {
            displayImages();
        }
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        openImageLauncher.launch(intent);
    }

    private void displayImages() {
        if (imageContainerLayout == null) return;

        imageContainerLayout.removeAllViews();

        // 画面の幅を取得 (マージンやパディングを考慮)
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int spacingInPixels = 16; // 画像間のスペース（dpからpxに変換する必要がある場合は別途対応）
        int columnCount = imageContainerLayout.getColumnCount(); // GridLayoutの列数を取得
        // 1列あたりの幅を計算 (パディングやマージンを考慮)
        // ここでは GridLayout のパディングも考慮に入れるとより正確になります。
        // 簡単のため、ここでは GridLayout の左右パディングは無いものとして計算します。
        int imageWidth = (screenWidth - (spacingInPixels * (columnCount - 1))) / columnCount;


        for (Uri imageUri : imageUris) {
            ImageView imageView = new ImageView(this);

            // GridLayout.LayoutParams を使用
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = imageWidth; // 計算した幅を設定
            layoutParams.height = imageWidth; // 高さを幅と同じにして正方形に近づける (お好みで調整)
            // マージンを設定 (右と下にマージンを設定してスペースを作る)
            layoutParams.setMargins(0, 0, spacingInPixels, spacingInPixels);
            imageView.setLayoutParams(layoutParams);

            imageView.setImageURI(imageUri);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // CENTER_CROP でセルを埋めるように調整

            imageContainerLayout.addView(imageView);
        }
    }
}