package sunder.com.a360degreeinfodynamics.uploadimagewithmultipart;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;


public class MainActivity extends AppCompatActivity {
    MaterialProgressBar material_progress_bar;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1, REQUEST_CAMERA_ID = 2, SELECT_FILE_ID = 3;
    private String userChoosenTask;
    private Bitmap thumbnail1;
    byte[] b1;
    private CircleImageView imageView1;
    private String encodedImageonGallery, encodedImageonCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        material_progress_bar = (MaterialProgressBar) findViewById(R.id.material_progress_bar);
        imageView1= (CircleImageView) findViewById(R.id.imageView1);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"Take Photo", "Choose from Gallery", "Cancel"};
                AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                build.setTitle("Add Photo");
                build.setCancelable(false);
                build.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean result = Utility.checkPermission(MainActivity.this);
                        if (items[which].equals("Take Photo")) {
                            userChoosenTask = "Take Photo";
                            if (result)
                                cameraIntent();

                        } else if (items[which].equals("Choose from Gallery")) {
                            userChoosenTask = "Choose from Library";
                            if (result)
                                galleryIntent();
                        } else if (items[which].equals("Cancel")) {
                            userChoosenTask = "Cancel";
                            dialog.dismiss();
                        }

                    }
                });
                android.support.v7.app.AlertDialog alert = build.create();
                alert.show();
            }
        });


    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    onCaptureImageResult(data);
                    Log.i("resultCode", "resultCode:" + "" + data);
                    break;
                case 1:
                    onSelectFromGalleryResult(data);
                    Log.i("resultCode", "resultCode:" + "" + data);
                    break;
            }

        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //  Toast.makeText(getActivity(), "Hello12", Toast.LENGTH_SHORT).show();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        b1 = bytes.toByteArray();
        encodedImageonGallery = Base64.encodeToString(b1, Base64.DEFAULT);
        GalleryimageUpload(encodedImageonGallery);

    }

    private void onCaptureImageResult(Intent data) {
        thumbnail1 = (Bitmap) data.getExtras().get("data");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail1.compress(Bitmap.CompressFormat.PNG, 90, bytes);
        b1 = bytes.toByteArray();
        encodedImageonCapture = Base64.encodeToString(b1, Base64.DEFAULT);
        GalleryimageUpload(encodedImageonCapture);
    }

    private void GalleryimageUpload(final String encodedImageonGallery) {
        material_progress_bar.setVisibility(View.VISIBLE);
        String url = "Your url";
        File f = new File(MainActivity.this.getCacheDir(), "filename1.jpg");
        try {
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(b1);
            fos.flush();
            fos.close();
            MultipartRequest multipartRequest = new MultipartRequest(url, null, f, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    try {
                        String str = new String(response.data, "UTF-8");
                        try {
                            System.out.println("Networkonse12 " + str);
                            JSONObject obj = new JSONObject(str);
                            JSONObject json = obj.getJSONObject("response");
                            String message = json.getString("message");
                            if (json.getString("code").equalsIgnoreCase("200")) {
                                Toast.makeText(MainActivity.this, "Upload successfully!", Toast.LENGTH_SHORT).show();
                                String upload_image_path = "http://iiieyelobby.com/web-api/uploads/" + message;
                                ImageRequest request = new ImageRequest(upload_image_path,
                                        new Response.Listener<Bitmap>() {
                                            @Override
                                            public void onResponse(Bitmap bitmap) {
                                                imageView1.setImageBitmap(bitmap);
                                            }
                                        }, 0, 0, null,
                                        new Response.ErrorListener() {
                                            public void onErrorResponse(VolleyError error) {
                                            }
                                        });
                                // Access the RequestQueue through your singleton class.
                                NetworkHelper.getInstance(MainActivity.this).addToRequestQueue(request);

                            } else {
                                Toast.makeText(MainActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    material_progress_bar.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Upload failed!\r\n" + error.toString(), Toast.LENGTH_SHORT).show();
                    material_progress_bar.setVisibility(View.GONE);
                }
            });

            int socketTimeout = 30000;//30 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            multipartRequest.setRetryPolicy(policy);
            NetworkHelper.getInstance(MainActivity.this).getRequestQueue().add(multipartRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
