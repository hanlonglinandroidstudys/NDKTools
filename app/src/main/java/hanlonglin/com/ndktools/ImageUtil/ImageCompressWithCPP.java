package hanlonglin.com.ndktools.ImageUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * 使用c++的方式压缩图片
 */
public class ImageCompressWithCPP {

    private ImageCompressWithCPP() {
    }

    private static ImageCompressWithCPP instance = new ImageCompressWithCPP();

    public static ImageCompressWithCPP getInstance() {
        return instance;
    }

    private final static int DEFAULT_QUALITY = 50;

    public String compressBitmap(Bitmap bitmap, String filename, boolean optimize) {
        return compressBitmap(bitmap, DEFAULT_QUALITY, filename, optimize);
    }

    public String compressBitmap(String srcFile, boolean optimize) {
        Bitmap src_bitmap= BitmapFactory.decodeFile(srcFile);
        String dstFile = srcFile.substring(0, srcFile.lastIndexOf("/"))+"/"+"终极压缩.jpg";
        return compressBitmap(src_bitmap, DEFAULT_QUALITY, dstFile, optimize);
    }
    public String compressBitmap(Bitmap bitmap, int quality, String filename, boolean optimize) {
        String com_ret = "";
        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            Bitmap result = null;
            result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, null, rect, null);
            com_ret = compressBitmap(result, result.getWidth(), result.getHeight(), quality, filename.getBytes(), optimize);
            result.recycle();

        } else {
            com_ret = compressBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), quality, filename.getBytes(), optimize);
        }
        return com_ret;
    }

    private native String compressBitmap(Bitmap bitmap, int w, int h, int quality, byte[] filename, boolean optimize);

    private native String compressBitmap2(Bitmap bitmap, int w, int h, int quality, byte[] filename, boolean optimize);

    static{
        System.loadLibrary("bitherlibjin");
        System.loadLibrary("jpegbither");
    }
}
