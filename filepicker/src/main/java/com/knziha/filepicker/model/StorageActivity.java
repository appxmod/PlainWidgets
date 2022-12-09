package com.knziha.filepicker.model;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.knziha.filepicker.utils.CMNF;

public class StorageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 700);
        Toast.makeText(this, "请选择sd卡路径", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent duco) {
        super.onActivityResult(requestCode, resultCode, duco);
		CMNF.Log("onActivityResult", resultCode);
        switch (requestCode) {
            case 700:if(resultCode==RESULT_OK){
                Uri treeUri = duco.getData();
				CMNF.Log("RESULT_OK", treeUri);
                if(treeUri!=null) {
                    int GRANTFLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    grantUriPermission(getPackageName(), treeUri, GRANTFLAGS);
                    getContentResolver().takePersistableUriPermission(treeUri, GRANTFLAGS);
                }
                break;
            }
        }
    }
}
