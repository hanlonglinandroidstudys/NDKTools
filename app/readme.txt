-----------图片压缩的方法：

java方法：
    ---1.质量压缩：
         * 质量压缩并不会改变图片在内存中的大小，仅仅会减小图片所占用的磁盘空间的大小，
         * 因为质量压缩不会改变图片的分辨率，而图片在内存中的大小是根据width*height*一个像素的所占用的字节数计算的，
         * 宽高没变，在内存中占用的大小自然不会变，质量压缩的原理是通过改变图片的位深和透明度来减小图片占用的磁盘空
         * 间大小，所以不适合作为缩略图，可以用于想保持图片质量的同时减小图片所占用的磁盘空间大小。另外，由于png是无
         * 损压缩，所以设置quality无效

         * 应用场景：
         * 图片压缩到本地 转为base64上传服务器

           见方法： hanlonglin.com.ndktools.ImageUtil.ImageCompressWithJava.QulityCompress(int options)

    ---2.尺寸压缩：
         * 尺寸压缩
         * 通过缩放图片的像素，减小图片占用内存的大小
         *
         * 应用场景：
         * 缩略图，小图

            见方法：hanlonglin.com.ndktools.ImageUtil.ImageCompressWithJava.SizeCompress(int radio)

    ---3.采样率压缩：
         * 采样率压缩
         * 采样率压缩是通过设置BitmapFactory.Options.inSampleSize，来减小图片的分辨率，
         * 进而减小图片所占用的磁盘空间和内存大小。设置的inSampleSize会导致压缩的图片的
         * 宽高都为1/inSampleSize，整体大小变为原始图片的inSampleSize平方分之一
         *
         * 应用场景：
         * 从sd卡加载大图

            见方法：hanlonglin.com.ndktools.ImageUtil.ImageCompressWithJava.SampleCompress(int targetW,int targetH)

c++方法：

    ---1.使用libjpeg库压缩图片，原理是去掉图片中argb中的a

            见方法：hanlonglin.com.ndktools.ImageUtil.ImageCompressWithCPP.compressBitmap(Bitmap bitmap, int w, int h, int quality, byte[] filename, boolean optimize);
