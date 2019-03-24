package hanlonglin.com.datatransport_flatbuffer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    String pers[] = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final static int REQ_PERS_CODE=10;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if(checkPers(pers))
            gotoMain();
    }

    private boolean checkPers(String[] permissions) {
        List<String> needReqPers = new ArrayList<>();
        for (String per : permissions) {
            if (ContextCompat.checkSelfPermission(this, per) != PackageManager.PERMISSION_GRANTED) {
                needReqPers.add(per);
            }
        }
        if (needReqPers.size() == 0) {
            return true;
        }else{
            ActivityCompat.requestPermissions(this,needReqPers.toArray(new String[needReqPers.size()]),REQ_PERS_CODE);
            return false;
        }
    }

    private void gotoMain() {
        startActivity(new Intent(StartActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQ_PERS_CODE){
            for(int result:grantResults){
                if(result!=PackageManager.PERMISSION_GRANTED)
                    return ;
            }
            gotoMain();
        }
    }
}
