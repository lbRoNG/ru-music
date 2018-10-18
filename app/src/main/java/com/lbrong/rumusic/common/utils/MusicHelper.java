package com.lbrong.rumusic.common.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lbrong.rumusic.bean.Song;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lbRoNG
 * @since 2018/10/18
 */
public final class MusicHelper {
    private static MusicHelper musicHelper;

    private MusicHelper() {}

    public static MusicHelper build() {
        if (musicHelper == null) {
            synchronized (MusicHelper.class) {
                if (musicHelper == null) {
                    musicHelper = new MusicHelper();
                }
            }
        }
        return musicHelper;
    }

    public @Nullable
    List<Song> getLocalMusic(@NonNull Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        if (ObjectHelper.requireNonNull(contentResolver)) {
            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    , null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            if (cursor == null) {
                return null;
            }

            List<Song> songList = new ArrayList<>();
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    Song m = new Song();
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

                    if (isMusic != 0 && duration / (500 * 60) >= 1) {
                        m.setId(id);
                        m.setTitle(title);
                        m.setArtist(artist);
                        m.setDuration(duration);
                        m.setSize(size);
                        m.setUrl(url);
                        m.setAlbum(album);
                        m.setAlbumId(albumId);
                        m.setBitmap(new WeakReference<>(getArtwork(contentResolver,id,albumId,true,true)));
                        songList.add(m);
                    }
                    cursor.moveToNext();
                }

                cursor.close();
                return songList;
            }
        }
        return null;
    }

    private Bitmap getArtWorkFormFile(ContentResolver resolver, long songId, long albumId) {
        Bitmap bitmap = null;
        if (albumId < 0 && songId < 0) {
            throw new IllegalArgumentException("Must specify an album or song");
        }

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fileDescriptor = null;
            if (albumId < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songId + "/albumart");
                ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
                if (parcelFileDescriptor != null) {
                    fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fileDescriptor = pfd.getFileDescriptor();
                }
            }

            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            options.inSampleSize = calculateInSampleSize(options, 50, 50);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private Bitmap getArtwork(ContentResolver resolver, long songId, long albumId,
                              boolean allowDefault, boolean small) {
        if (albumId < 0) {
            if (songId < 0) {
                Bitmap bitmap = getArtWorkFormFile(resolver, songId, albumId);
                if (bitmap != null) {
                    return bitmap;
                }
            }
            return null;
        }
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
        if (uri != null) {
            InputStream inputStream;
            try {
                inputStream = resolver.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                if (small) {
                    options.inSampleSize = calculateInSampleSize(options, 50, 50);
                } else {
                    options.inSampleSize = calculateInSampleSize(options, 600, 600);
                }
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                inputStream = resolver.openInputStream(uri);
                return BitmapFactory.decodeStream(inputStream, null, options);


            } catch (FileNotFoundException e) {
                Bitmap bitmap = getArtWorkFormFile(resolver, songId, albumId);
                if (bitmap != null) {
                    if (bitmap.getConfig() == null) {
                        bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);
                        if (bitmap == null && allowDefault) {
                            return null;
                        }
                    }
                }
                return bitmap;
            }
        }
        return null;
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
}
