package com.mcraesolutions.utils;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

/**
 * Created by jwhitakermcrae on 4/19/15.
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Return MIME type string from input URL string, using file extensions.
     *
     * @param url
     * @return
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Return true if input path is image file, false otherwise
     *
     * @param filepath
     * @return
     */
    public static boolean isImage(String filepath) {
        if (filepath != null) {
            String mime = getMimeType(filepath);
            if (mime != null) { return (mime.startsWith("image/")); }
        }
        return false; // null string
    }

    /**
     * Calculate and return the BitmapFactory.Options.inSampleSize attribute value to load the given
     * image size.
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Load and return a Bitmap from the input path. If path points to an image file the image
     * bitmap will be returned, scaled to the input dimensions. If the path points to a video file
     * an early frame will be pulled from the video and returned, scaled to the input dimensions.
     *
     * @param filepath
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromPath(String filepath, int reqWidth, int reqHeight) {

        Bitmap image = null;

        // load image bitmap either from file or by grabbing a frame of video from file
        if (isImage(filepath)) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filepath, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            image = BitmapFactory.decodeFile(filepath, options);

            // handle file not found
            if (image == null) {
                Log.w(TAG, "Unable to load image file: " + filepath);
                return null; // return null, TODO: throw exception instead???
            }

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Bitmap size after loading scaled version: " + image.getWidth() + " x " + image.getHeight());
            }
        }
        else { // ERROR

            Log.w(TAG, "Attempting to load sample bitmap from invalid file: " + filepath);
            return null; // return null, TODO: throw exception instead???
        }

        return Bitmap.createScaledBitmap(image, reqWidth, reqHeight, false);
    }

    /**
     * Asynchronous task used for loading a bitmap file from path.
     *
     * @author wmcrae
     */
    static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String mFilepath;
        private int mReqWidth;
        private int mReqHeight;

        public BitmapWorkerTask(ImageView imageView, int reqWidth, int reqHeight) {
            mReqWidth = reqWidth;
            mReqHeight = reqHeight;

            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            mFilepath = params[0];
            return decodeSampledBitmapFromPath(mFilepath, mReqWidth, mReqHeight);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /**
     * Drawable subclass to store a reference back to the worker task. This is to assist in handling
     * concurrency issues.
     *
     * @author wmcrae
     *
     */
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Helper method to check if another running task is already associated with the ImageView. If
     * so, it attempts to cancel the previous task by calling cancel().
     *
     * @param filepath
     * @param imageView
     * @return
     */
    public static boolean cancelPotentialWork(String filepath, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask =
                getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null && filepath != null) {
            final String bitmapFilepath = bitmapWorkerTask.mFilepath;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapFilepath == null || !filepath.equals(bitmapFilepath)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            }
            else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /**
     * Helper method used to retrieve the task associated with a particular ImageView.
     *
     * @param imageView
     * @return
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * Efficiently load a Bitmap image from the specified path and of the specified dimensions. The
     * heavy lifting is done on an asynchronous thread.
     *
     * @param res
     * @param filepath
     * @param imageView
     * @param reqWidth
     * @param reqHeight
     */
    public static void loadBitmap(Resources res, String filepath,
                                  ImageView imageView, int reqWidth, int reqHeight) {
        if (cancelPotentialWork(filepath, imageView)) {
            final BitmapWorkerTask task =
                    new BitmapWorkerTask(imageView, reqWidth, reqHeight);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(res, null, task);
            imageView.setImageDrawable(asyncDrawable); // set placeholder bitmap
            task.execute(filepath); // start loading real bitmap (in background thread)
        }
    }
}
