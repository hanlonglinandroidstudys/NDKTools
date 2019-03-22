
package hanlonglin.com.ndktools.ImageUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * 图片压缩
 */

public class ImageCompressWithJava {

    private final static int DEFAULT_SAMPLE_W=360;
    private final static int DEFAULT_SAMPLE_H=240;

    //单例模式
    private ImageCompressWithJava() {
    }

    private static ImageCompressWithJava instance = new ImageCompressWithJava();

    public static ImageCompressWithJava getInstance() {
        return instance;
    }

    private final static String TAG = "ImageCompressWithJava";
    String imgPath = "";
    String imgDir = "";

    public ImageCompressWithJava setImgPath(String imgPath) {
        this.imgPath = imgPath;
        this.imgDir = imgPath.substring(0, imgPath.lastIndexOf("/"));
        Log.e(TAG, "imgPath:" + imgPath + ",imgDir:" + imgDir);
        return this;
    }

    private boolean assertImagePath() {
        if (imgPath == null || imgPath == "") {
            return false;
        }
        return true;
    }

    /**
     * 质量压缩
     * 质量压缩并不会改变图片在内存中的大小，仅仅会减小图片所占用的磁盘空间的大小，
     * 因为质量压缩不会改变图片的分辨率，而图片在内存中的大小是根据width*height*一个像素的所占用的字节数计算的，
     * 宽高没变，在内存中占用的大小自然不会变，质量压缩的原理是通过改变图片的位深和透明度来减小图片占用的磁盘空
     * 间大小，所以不适合作为缩略图，可以用于想保持图片质量的同时减小图片所占用的磁盘空间大小。另外，由于png是无
     * 损压缩，所以设置quality无效
     *
     * 应用场景：
     * 图片压缩到本地 转为base64上传服务器
     */
    public boolean QulityCompress(int options) {
        if (!assertImagePath()) return false;

        Bitmap bitmap_src = BitmapFactory.decodeFile(imgPath);

        //int options = 20;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap_src.compress(Bitmap.CompressFormat.JPEG, options, bos);
        try {
            FileOutputStream fos = new FileOutputStream(imgDir + "/" + "质量压缩_"+options+".jpg");
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 尺寸压缩
     * 通过缩放图片的像素，减小图片占用内存的大小
     *
     * 应用场景：
     * 缩略图，小图
     * @param radio
     * @return
     */
    public boolean SizeCompress(int radio){
        if (!assertImagePath()) return false;
        if(radio<0) return false;

        Bitmap bitmap_src=BitmapFactory.decodeFile(imgPath);
        Bitmap bitmap_result=Bitmap.createBitmap(bitmap_src.getWidth()/radio,bitmap_src.getHeight()/radio,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap_result);
        Rect rect=new Rect(0,0,bitmap_src.getWidth()/radio,bitmap_src.getHeight()/radio);
        canvas.drawBitmap(bitmap_src,null,rect,null);
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bitmap_result.compress(Bitmap.CompressFormat.JPEG,100,bos);

        try {
            FileOutputStream fos=new FileOutputStream(imgDir+"/"+"尺寸压缩_"+radio+".jpg");
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 采样率压缩
     * 采样率压缩是通过设置BitmapFactory.Options.inSampleSize，来减小图片的分辨率，
     * 进而减小图片所占用的磁盘空间和内存大小。设置的inSampleSize会导致压缩的图片的
     * 宽高都为1/inSampleSize，整体大小变为原始图片的inSampleSize平方分之一
     *
     * 应用场景：
     * 从sd卡加载大图
     */
    public boolean SampleCompress(int targetW,int targetH) {
        if (!assertImagePath()) return false;

        //Bitmap bitmap_src = BitmapFactory.decodeFile(imgPath);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true; //设置为true后，读取图片时只读取宽和高
        Bitmap bitmapTemp = BitmapFactory.decodeFile(imgPath,options);
        Log.e(TAG,"bitmapTemp.Width():"+options.outWidth+",bitmapTemp.Height():"+options.outHeight);
        int sample=caculateSample(options,targetW,targetH);
        Log.e(TAG,"simpleSize="+sample);
        options.inSampleSize=sample;
        options.inJustDecodeBounds=false;
        Bitmap bitmapResult=BitmapFactory.decodeFile(imgPath,options);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmapResult.compress(Bitmap.CompressFormat.JPEG,100,bos);
        try {
            FileOutputStream fos=new FileOutputStream(imgDir + "/" + "采样率压缩_"+targetW+"_"+targetH+".jpg");
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean SampleCompress() {
        return SampleCompress(DEFAULT_SAMPLE_W,DEFAULT_SAMPLE_H);
    }
    /**
     * 计算缩放参数
     * @param options
     * @param targetW  目标宽度
     * @param targetH  目标高度
     * @return
     */
    private int caculateSample(BitmapFactory.Options options,int targetW,int targetH) {
        int width=options.outWidth;
        int height=options.outHeight;
        int sampleW=width/targetW;
        int sampleH=height/targetH;
        return sampleW>sampleH?sampleH:sampleW;
    }

}
