package com.hunofox.pictureselector.widget.pictureEditWeidgt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;

/**
 * 项目名称：WxApproval
 * 项目作者：胡玉君
 * 创建日期：2017/8/23 11:02.
 * ----------------------------------------------------------------------------------------------------
 * 文件描述：
 * ----------------------------------------------------------------------------------------------------
 */

public class ImageUtils {

    /**
     * 旋转图片
     */
    public static Bitmap rotate(Bitmap bitmap,
                                int degree, boolean isRecycle) {
        Matrix m = new Matrix();
        m.setRotate(degree, (float) bitmap.getWidth() / 2,
                (float) bitmap.getHeight() / 2);
        try {
            Bitmap bm1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            if (isRecycle) bitmap.recycle();
            return bm1;
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 获取图片的Exif方向
     */
    public static int getBitmapExifRotate(String path) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }
        }
        return degree;
    }

    /*
     * 根据相片的Exif旋转图片
     */
    public static Bitmap rotateBitmapByExif(Bitmap bitmap, String path, boolean isRecycle) {
        int digree = getBitmapExifRotate(path);
        if (digree != 0) {
            // 旋转图片
            bitmap = ImageUtils.rotate(bitmap, digree, isRecycle);
        }
        return bitmap;
    }

    /**
     * @param path
     * @param context
     * @return
     */
    public static Bitmap createBitmapFromPath(String path, Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int screenW = display.getWidth();
        int screenH = display.getHeight();
        return createBitmapFromPath(path, screenW, screenH);
    }

    /**
     * 获取一定尺寸范围内的的图片，防止oom。参考系统自带相机的图片获取方法
     *
     * @param path      路径
     * @param maxWidth  图片的最大宽
     * @param maxHeight 图片的最大高
     * @return 经过按比例缩放的图片
     * @throws IOException
     */
    public static Bitmap createBitmapFromPath(String path, int maxWidth, int maxHeight) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = null;
        if (path.endsWith(".3gp")) {
            return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        } else {
            try {
                options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                int width = options.outWidth;
                int height = options.outHeight;
                options.inSampleSize = computeBitmapSimple(width * height, maxWidth * maxHeight * 2);
                options.inPurgeable = true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = false;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(path, options);
                return ImageUtils.rotateBitmapByExif(bitmap, path, true);
            } catch (OutOfMemoryError error) {//内容溢出，则再次缩小图片
                options.inSampleSize *= 2;
                bitmap = BitmapFactory.decodeFile(path, options);
                return ImageUtils.rotateBitmapByExif(bitmap, path, true);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 计算bitmap的simple值
     *
     * @param realPixels,图片的实际像素，
     * @param maxPixels,压缩后最大像素
     * @return simple值
     */
    public static int computeBitmapSimple(int realPixels, int maxPixels) {
        if (maxPixels <= 0) {
            return 1;
        }
        try {
            if (realPixels <= maxPixels) {//如果图片尺寸小于最大尺寸，则直接读取
                return 1;
            } else {
                int scale = 2;
                while (realPixels / (scale * scale) > maxPixels) {
                    scale *= 2;
                }
                return scale;
            }
        } catch (Exception e) {
            return 1;
        }
    }
}
