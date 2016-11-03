package com.example.takepicture.samplepicture.activity;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.takepicture.samplepicture.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Mochamad Taufik on 03-Nov-16.
 * Email   : thidayat13@gmail.com
 */

public class MainActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private String imagePath = "";
    private Uri imgUri;
    private Button mDelete;

    private TextView mTvPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPreview   = (ImageView) findViewById(R.id.image_preview);
        mTvPath     = (TextView) findViewById(R.id.path);
        mDelete     = (Button) findViewById(R.id.delete);

        ivPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                AlertDialog.Builder alertPrinter =
                        new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                        R.layout.list_item_action);

                arrayAdapter.add("Ambil Foto");
                arrayAdapter.add("Pilih dari galeri");

                alertPrinter.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);

                        if (strName.equals("Ambil Foto")) {
                            if (v.getId() == R.id.image_preview) {
                                String fileName = System.currentTimeMillis() + ".jpg";
                                ContentValues values = new ContentValues();

                                values.put(MediaStore.Images.Media.TITLE, fileName);
                                values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");

                                imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                                startActivityForResult(intent, 1);
                            }
                        } else {
                            startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 2);
                        }
                    }
                });

                alertPrinter.show();
            }
        });

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    (new File(imagePath)).delete();
                    mTvPath.setText("");
                    ivPreview.setImageResource(R.color.transparent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if(requestCode == 1) {
                imagePath = ImagePath(imgUri);
                performCrop();
            }

            else if(requestCode == 2) {
                imgUri = data.getData();
                performCrop();

            }
            else if(requestCode == 3){
                Bundle extras = data.getExtras();

                imagePath = imgUri.getPath();

                Log.i("TAG", "After Crop selectedImagePath " + imagePath);

                imagePath = getPath(imgUri);
                Log.i("TAG", "Absolute Path " + imagePath);

                if (extras != null) {
                        // Bitmap photo = extras.getParcelable("data");
                    Log.i("TAG", "Inside Extra " + imagePath);
                    Bitmap photo = (Bitmap) extras.get("data");

                    imagePath = String.valueOf(System.currentTimeMillis())
                            + ".jpg";

                    Log.i("TAG", "new selectedImagePath before file "
                            + imagePath);

                    File directory = new File(Environment.getExternalStorageDirectory()+File.separator
                            +"sampleapp"+File.separator+"cache");

                    directory.mkdirs();

                    File file = new File(Environment.getExternalStorageDirectory() + "/sampleapp/cache/",
                            imagePath);

                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        photo.compress(Bitmap.CompressFormat.PNG, 95, fos);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(this,
                                "Image cropper crashed",
                                Toast.LENGTH_LONG).show();
                    }

                    imagePath = Environment.getExternalStorageDirectory()
                            + "/sampleapp/cache/" + imagePath;
                    Log.i("TAG", "After File Created  " + imagePath);

                    Bitmap bm = decodeFile(imagePath);
                    ivPreview.setImageBitmap(bm);
                    mTvPath.setText("Image Path :"+ imagePath);
                }
            }
        }
    }

    private void performCrop(){
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(imgUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, 3);

        } catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static Bitmap decodeFile(String path) {
        int orientation;
        try {
            if (path == null) {
                return null;
            }
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bm = BitmapFactory.decodeFile(path, o2);
            Bitmap bitmap = bm;

            ExifInterface exif = new ExifInterface(path);

            orientation = exif
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Log.e("ExifInteface .........", "rotation =" + orientation);


            Log.e("orientation", "" + orientation);
            Matrix m = new Matrix();

            if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                m.postRotate(180);

                Log.e("in orientation", "" + orientation);
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                        bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                m.postRotate(90);
                Log.e("in orientation", "" + orientation);
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                        bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                m.postRotate(270);
                Log.e("in orientation", "" + orientation);
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                        bm.getHeight(), m, true);
                return bitmap;
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }


    private String ImagePath(Uri imageUri) {
        String imgPath = "";
        Cursor cursor = null;

        try {
            String[] proj = { MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Thumbnails._ID,
                    MediaStore.Images.ImageColumns.ORIENTATION };

            cursor                  = getContentResolver().query(imageUri, proj, null, null, null);
            int columnIndex         = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int columnIndexThumb    = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
            int file_ColumnIndex    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            int size = cursor.getCount();

            if (size > 0) {
                int thumbID = 0;

                if (cursor.moveToFirst()) {
                    int imageID = cursor.getInt(columnIndex);

                    thumbID = cursor.getInt(columnIndexThumb);

                    String Path = cursor.getString(file_ColumnIndex);

                    imgPath = Path;
                }
            }

            cursor.close();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        cursor.close();

        return imgPath;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imgUri != null) {
            outState.putString("cameraImageUri", imgUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            imgUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }

    public String getPath(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                res = cursor.getString(column_index);
            }
        }else {
            Toast.makeText(getApplicationContext(),"An Error Occured",Toast.LENGTH_LONG).show();
        }

        if (cursor != null) {
            cursor.close();
        }
        return res;
    }
}
