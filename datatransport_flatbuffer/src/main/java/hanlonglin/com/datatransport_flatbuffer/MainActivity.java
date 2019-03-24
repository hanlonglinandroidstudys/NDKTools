package hanlonglin.com.datatransport_flatbuffer;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.flatbuffers.FlatBufferBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import hanlonglin.com.datatransport_flatbuffer.flatbuffter_model.model.Basic;
import hanlonglin.com.datatransport_flatbuffer.flatbuffter_model.model.Car;
import hanlonglin.com.datatransport_flatbuffer.flatbuffter_model.model.Items;

public class MainActivity extends AppCompatActivity {

    Button btn_serialize;
    Button btn_reserilize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_serialize = (Button) findViewById(R.id.btn_serialize);
        btn_reserilize = (Button) findViewById(R.id.btn_re_serialize);
        btn_serialize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialize();
            }
        });
        btn_reserilize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reSerialize();
            }
        });
    }

    //=============序列化=================
    private void serialize() {

        FlatBufferBuilder builder = new FlatBufferBuilder();
        //创建Car对象
        int id1 = builder.createString("兰博基尼");
        int car1 = Car.createCar(builder, 1000L, 8888L, id1);

        int id2 = builder.createString("奥迪A8");
        int car2 = Car.createCar(builder, 5000L, 6555L, id2);

        //创建basic对象
        int carListVector1 = Basic.createCarListVector(builder, new int[]{car1, car2});
        int nid1 = builder.createString("基本1");
        int basic1 = Basic.createBasic(builder, 12, nid1, 20000L, true, 100, carListVector1);

        int carListVector2 = Basic.createCarListVector(builder, new int[]{car1, car2});
        int nid2 = builder.createString("基本2");
        int basic2 = Basic.createBasic(builder, 12, nid2, 20000L, true, 100, carListVector2);

        //创建Items
        //Items是root 有所区别
        int basicVector = Items.createBasicVector(builder, new int[]{basic1, basic2});
        Items.startItems(builder);

        Items.addItemId(builder, 1000L);
        Items.addTimestemp(builder, 122);
        Items.addBasic(builder, basicVector);

        int rootItem = Items.endItems(builder);
        Items.finishItemsBuffer(builder, rootItem);

        //===============保存数据到文件=================

        File sdcardFile = Environment.getExternalStorageDirectory();
        File file = new File(sdcardFile, "Items.txt");
        if (!file.exists()) {
            file.delete();
        }

        ByteBuffer data = builder.dataBuffer();
        FileOutputStream fos = null;
        FileChannel channel = null;
        try {
            fos = new FileOutputStream(file);
            channel = fos.getChannel();
            while (data.hasRemaining()) {
                channel.write(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (channel != null)
                    channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(this, "序列化完成", Toast.LENGTH_SHORT).show();
    }

    //====================反序列化=======================
    public void reSerialize() {

        File sdcardFile = Environment.getExternalStorageDirectory();
        File file = new File(sdcardFile, "Items.txt");
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在！", Toast.LENGTH_SHORT).show();
            return;
        }

        FileInputStream fis = null;
        FileChannel channel = null;
        try {
            fis = new FileInputStream(file);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            channel = fis.getChannel();
            int readBytes = 0;
            while ((readBytes = channel.read(byteBuffer)) != -1) {
                System.out.println("读取数据：" + readBytes);
            }
            //把指针回到最初的状态，准备从byteArray中读取数据
            byteBuffer.flip();
            Items items = Items.getRootAsItems(byteBuffer);
            //读取数据测试看看是否一致
            Log.e("TAG", "items id:" + items.ItemId());
            Log.e("TAG", "items timetemp:" + items.timestemp());

            int length = items.basicLength();
            Log.e("TAG", "basic length:" + length);
            for (int i = 0; i < length; i++) {
                Log.e("TAG", "basic" + i + ":name:" + items.basic(i).name());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel != null)
                    channel.close();
                if (fis != null)
                    fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
