package hanlonglin.com.ndktools;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import hanlonglin.com.ndktools.ImageUtil.ImageCompressWithCPP;
import hanlonglin.com.ndktools.ImageUtil.ImageCompressWithJava;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    private final static String TAG="MainActivity";

    private final static int REQ_CHOOSE_PIC = 10; //选择照片
    private String imagePath;

    ImageView imageView;
    Button btn_choosepic;
    Button btn_compress_quality;
    Button btn_compress_sample;
    Button btn_compress_size;
    Button btn_compress_cpp;
    EditText ed_quality;
    EditText ed_samlpe_w;
    EditText ed_sample_h;
    EditText ed_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        initView();
    }

    private void initView() {
        imageView = (ImageView) findViewById(R.id.imageView);
        btn_choosepic=(Button)findViewById(R.id.btn_choosepic);
        btn_compress_quality = (Button) findViewById(R.id.btn_compress_quality);
        btn_compress_sample = (Button) findViewById(R.id.btn_compress_sample);
        btn_compress_size=(Button)findViewById(R.id.btn_compress_size);
        btn_compress_cpp=(Button)findViewById(R.id.btn_compress_cpp);
        ed_quality=(EditText)findViewById(R.id.ed_quality);
        ed_samlpe_w=(EditText)findViewById(R.id.ed_simple_w);
        ed_sample_h=(EditText)findViewById(R.id.ed_sample_h);
        ed_size=(EditText)findViewById(R.id.ed_size);
        btn_choosepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChoosePic();
            }
        });
        btn_compress_quality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compress_quality();
            }
        });
        btn_compress_sample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compress_sample();
            }
        });
        btn_compress_size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compress_size();
            }
        });
        btn_compress_cpp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compress_cpp();
            }
        });
    }

    //选择图片
    private void onChoosePic() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CHOOSE_PIC);
    }

    //质量压缩
    private void compress_quality(){
        int quality=Integer.parseInt(ed_quality.getText().toString());
        boolean ret = ImageCompressWithJava.getInstance().setImgPath(imagePath).QulityCompress(quality);
        if (ret) {
            Toast.makeText(MainActivity.this, "质量压缩成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "质量压缩失败", Toast.LENGTH_SHORT).show();
        }
    }

    //采样率压缩
    private void compress_sample(){
        int w=Integer.parseInt(ed_samlpe_w.getText().toString());
        int h=Integer.parseInt(ed_sample_h.getText().toString());
        boolean ret = ImageCompressWithJava.getInstance().setImgPath(imagePath).SampleCompress(w,h);
        if (ret) {
            Toast.makeText(MainActivity.this, "采样率压缩成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "采样率压缩失败", Toast.LENGTH_SHORT).show();
        }
    }

    //尺寸压缩
    private void compress_size(){
        int radio=Integer.parseInt(ed_size.getText().toString());
        boolean ret = ImageCompressWithJava.getInstance().setImgPath(imagePath).SizeCompress(radio);
        if (ret) {
            Toast.makeText(MainActivity.this, "尺寸压缩成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "尺寸压缩失败", Toast.LENGTH_SHORT).show();
        }
    }

    //终极压缩
    private void compress_cpp(){
        String ret= ImageCompressWithCPP.getInstance().compressBitmap(imagePath,false);
        Log.e(TAG,"终极压缩返回："+ret);
        if(ret.equals("1")){
            Toast.makeText(MainActivity.this, "终级压缩成功", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this, "终极压缩失败", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //在相册里面选择好相片之后调回到现在的这个activity中
        switch (requestCode) {
            case REQ_CHOOSE_PIC://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
                if (resultCode == RESULT_OK) {//resultcode是setResult里面设置的code值
                    try {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imagePath = cursor.getString(columnIndex);  //获取照片路径
                        Log.e(TAG,"imagePath:"+imagePath);
                        cursor.close();
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        imageView.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        // TODO Auto-generatedcatch block
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
