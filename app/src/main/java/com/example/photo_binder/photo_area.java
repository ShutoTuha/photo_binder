package com.example.photo_binder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout; // LinearLayout をインポート
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
    // private Uri selectedImageUri; // 単一選択用は imageUris リストで代替するのでコメントアウトまたは削除も検討
    // private ImageView selectedImageView; // 単一表示用も image_container_layout で代替

    private final List<Uri> imageUris = new ArrayList<>(); // 選択された全ての画像のURIを保持するリスト
    private LinearLayout imageContainerLayout; // 画像を表示するLinearLayout

    private final ActivityResultLauncher<Intent> openImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                                Intent data = result.getData();
                                if (data.getClipData() != null) {
                                    // マルチ選択の場合
                                    int count = data.getClipData().getItemCount();
                                    for (int i = 0; i < count; i++) {
                                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                        if (imageUri != null) {
                                            imageUris.add(imageUri); // リストに追加
                                            Log.d(TAG, "追加された画像のURI: " + imageUri.toString());
                                        }
                                    }
                                } else if (data.getData() != null) {
                                    // シングル選択の場合
                                    Uri imageUri = data.getData();
                                    if (imageUri != null) {
                                        imageUris.add(imageUri); // リストに追加
                                        Log.d(TAG, "追加された画像のURI: " + imageUri.toString());
                                    }
                                }

                                if (!imageUris.isEmpty()) {
                                    Toast.makeText(photo_area.this, imageUris.size() + "枚の画像が選択されました", Toast.LENGTH_LONG).show();
                                    displayImages(); // 選択された画像を表示するメソッドを呼び出し
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

        imageContainerLayout = findViewById(R.id.image_container_layout); // LinearLayout を取得

        // selectedImageView = findViewById(R.id.your_image_view_id); // もし単一のImageViewも使うなら

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button comebutton = findViewById(R.id.comebutton);
        comebutton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // 戻るボタンでこの画面を終了させる場合
        });

        Button addbutton = findViewById(R.id.addbutton);
        addbutton.setOnClickListener(v -> {
            openImagePicker();
        });

        // 既に選択済みの画像があれば表示（例：画面回転時など）
        if (!imageUris.isEmpty()) {
            displayImages();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 複数選択を許可
        openImageLauncher.launch(intent);
    }

    private void displayImages() {
        if (imageContainerLayout == null) return;

        imageContainerLayout.removeAllViews(); // 既存のビューをクリア（再表示のため）

        for (Uri imageUri : imageUris) {
            ImageView imageView = new ImageView(this);
            // ImageViewのレイアウトパラメータを設定 (例: 幅は親に合わせ、高さはコンテンツに合わせる)
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 16); // マージン（画像間のスペース）
            imageView.setLayoutParams(layoutParams);

            imageView.setImageURI(imageUri); // 画像をセット
            imageView.setAdjustViewBounds(true); // 画像のアスペクト比を保つ
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // スケールタイプ

            imageContainerLayout.addView(imageView); // LinearLayout に ImageView を追加
        }
    }

    // (オプション) 永続的なパーミッションが必要な場合
    // ... (既存のコメント部分はそのまま)
}