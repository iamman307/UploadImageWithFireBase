package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ImageView filepic;
    public Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filepic = findViewById(R.id.file);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        filepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });
    }
            private void choosePicture(){ //使用intent來選擇圖片 https://www.jianshu.com/p/67d99a82509b
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");  //設置文件類型
                intent.setAction(Intent.ACTION_GET_CONTENT); //讓使用者選擇資料，並返回所選
                /*https://vimsky.com/zh-tw/examples/detail/java-method-android.content.Intent.setType.html*/

                startActivityForResult(intent,1 ); //a want b to open something (intent, 辨別是哪個Activity回傳的資料，因為我可能一個Activity能夠開啟很多不同的Activity)
            }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //(
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode ==RESULT_OK && data != null &&data.getData()!=null) {//設定47、48功能畫面的結果為RESULT_OK
            //先確定自己被呼叫(requestCode==1)；確定上面結束前設定的功能ok(resultCode == RESULT_OK)；取得intent中的資料(data)
            imageUri = data.getData();  //使用getData方法取得傳回的Intent所包含的URI

            filepic.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {
        final ProgressDialog pd =new ProgressDialog(this); //顯示上傳進度
        /*http://tw.gitbook.net/android/android_progressbar.html*/
        pd.setTitle("Uploading Image...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();


        StorageReference riversRef = storageReference.child("images/"+randomKey); //要將圖片上傳到雲端，要先創建對圖片完整路徑的引用

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(findViewById(android.R.id.content),"Image Uploaded.",Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"Failed to Upload",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pd.setMessage("Progress: "+ (int)progressPercent + "%");
                    }
                });
    }
}
