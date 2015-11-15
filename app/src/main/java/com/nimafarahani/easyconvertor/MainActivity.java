package com.nimafarahani.easyconvertor;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.provider.MediaStore.Files.FileColumns;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
//<<<<<<< Updated upstream
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;
//=======
import com.itextpdf.text.Document;

//>>>>>>> Stashed changes

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity implements Picker.PickListener {



    public static final String EXTRA_MESSAGE = "com.example.sledd.helloworld";
    private static final String TAG = "MainActivity";
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int TAKE_PICTURE_REQUEST_CODE = 2;
    private File myPDF;
    private static LinkedList<Uri> imageList;
    private ArrayList<ImageEntry> mSelectedImages;
    private RecyclerView mImageSampleRecycler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //opening gallery page
    public void btnGallHandler(View view) {
        //Intent i = new Intent(getApplicationContext(), gallerySelection.class);
        //startActivity(i);

        // code for old image selection
        /*
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select a Picture!"), PICK_IMAGE_REQUEST_CODE);
        */

        //code for new gallery
        pickImages();

    }

    //opening camera page
    public void btnCamHandler(View view) {

        Intent x = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(x, TAKE_PICTURE_REQUEST_CODE);

    }

    private void pickImages(){

        //You can change many settings in builder like limit , Pick mode and colors
        new Picker.Builder(this, this ,R.style.AppTheme_NoActionBar)
                .build()
                .startActivity();

    }

    // button listener for converting images to pdf file
    public void onConvertPdfClick(View view) throws DocumentException, java.io.IOException
    {
        createPdf();
    }

    // listeners for multi image picker
    @Override
    public void onPickedSuccessfully(ArrayList<ImageEntry> images) {
        // call adaptor here for listview

        mSelectedImages = images;
        setupImageSamples();
        Log.d(TAG, "Picked images  " + images.toString());
    }


    private void setupImageSamples() {
        mImageSampleRecycler.setAdapter(new ImageSamplesAdapter());
    }

    @Override
    public void onCancel() {
        Log.i(TAG, "User canceled picker activity");
        Toast.makeText(this, "User canceld picker activtiy", Toast.LENGTH_SHORT).show();

    }


    private class ImageSamplesAdapter extends RecyclerView.Adapter<ImageSampleViewHolder> {


        @Override
        public ImageSampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final ImageView imageView = new ImageView(parent.getContext());
            return new ImageSampleViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(ImageSampleViewHolder holder, int position) {

            final String path = mSelectedImages.get(position).path;
            loadImage(path, holder.thumbnail);
        }

        @Override
        public int getItemCount() {
            return mSelectedImages.size();
        }


        private void loadImage(final String path, final ImageView imageView) {
            imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 440));

            Glide.with(MainActivity.this)
                    .load(path)
                    .asBitmap()
                    .into(imageView);


        }


    }

    class ImageSampleViewHolder extends RecyclerView.ViewHolder {

        protected ImageView thumbnail;

        public ImageSampleViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView;
        }
    }





    // on activity result for old gallery and camera code (possibly obsolete
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null )
        {
            Uri uri = data.getData();

            if (imageList == null)
                imageList = new LinkedList<>();

            imageList.add(uri);
            Log.i(TAG, "This is the Image name: " + uri.getLastPathSegment());
            Log.i(TAG, "This the length of the list: " + imageList.size());


        } else if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null )
        {
            // data.getData() is null here
            Uri uri = data.getData();

            if (imageList == null)
                imageList = new LinkedList<>();

            imageList.add(uri);

            Log.e(TAG, "Added image from camera!");
        }
        else {
            Log.e(TAG, "oops");
            Log.e(TAG, Integer.toString(requestCode));
            Log.e(TAG, Integer.toString(resultCode) + " " +  Integer.toString(RESULT_OK) + " " + Integer.toString(RESULT_CANCELED));
            Log.e(TAG, data == null ? "data is null" : "data is not null");
            //Log.e(TAG, data.getData() == null ? "data.getData() is null" : "data.getData() is not null");

        }

    }



    public void createPdf() throws  DocumentException, java.io.IOException
    {
        File pdfFolder = new File(Environment.getExternalStorageDirectory(), "EasyConvert"); // check this warning, may be important for diff API levels

        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
            Log.i(TAG, "Folder successfully created");
        }

        if (imageList != null)
        {
            Date date = new Date();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);

            myPDF = new File(pdfFolder + "/" + timeStamp + ".pdf");

            OutputStream output = new FileOutputStream(myPDF);

            Document document = new Document();
            PdfWriter.getInstance(document, output);

            long startTime, estimatedTime;

            document.open();
            //document.add(new Paragraph("~~~~Hello World!!~~~~"));
            for (int i = 0; i < imageList.size(); i++)
            {

                // create bitmap from URI in our list
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageList.get(i));

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                startTime = System.currentTimeMillis();

                // changed from png to jpeg, lowered processing time greatly
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                estimatedTime = System.currentTimeMillis() - startTime;

                Log.e(TAG, "compressed image into stream: " + estimatedTime);

                byte[] byteArray = stream.toByteArray();

                // instantiate itext image
                com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(byteArray);

                //img.scalePercent(40, 40);
                //img.setAlignment(Element.ALIGN_CENTER);

                img.scaleAbsolute(PageSize.LETTER.getWidth(), PageSize.LETTER.getHeight());
                img.setAbsolutePosition(
                        (PageSize.LETTER.getWidth() - img.getScaledWidth()) / 2,
                        (PageSize.LETTER.getHeight() - img.getScaledHeight()) / 2
                );
                document.add(img);
                document.newPage();



            }
            imageList = null;
            document.close();
           // promptForNextAction();

        }
    }
}
