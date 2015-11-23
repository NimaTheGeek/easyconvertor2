package com.nimafarahani.easyconvertor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
//<<<<<<< Updated upstream
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;

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
    private RecyclerView.Adapter myAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageSampleRecycler = (RecyclerView) findViewById(R.id.my_recycler_view);

        setupRecycler();

        //new stuff
        myAdapter = new ImageSamplesAdapter(mSelectedImages, MainActivity.this);
        mImageSampleRecycler.setAdapter(myAdapter);



    }

    private void setupRecycler() {

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.num_columns_image_samples));
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mImageSampleRecycler.setLayoutManager(gridLayoutManager);

    }

    //Button Listener for opening gallery page
    public void btnGallHandler(View view) {

        pickImages();

    }

    // Starts the multi image picker gallery
    private void pickImages(){

        //You can change many settings in builder like limit , Pick mode and colors
        new Picker.Builder(this, this ,R.style.AppTheme)
                .build()
                .startActivity();

    }

    // listeners for multi image picker
    // When the selected pictures are returned from picker gallery...
    @Override
    public void onPickedSuccessfully(ArrayList<ImageEntry> images) {
        // call adaptor here for listview

        mSelectedImages = images;
        //setupImageSamples();
        Log.d(TAG, "Picked images  " + images.toString());

        myAdapter = new ImageSamplesAdapter(mSelectedImages, MainActivity.this);
        mImageSampleRecycler.setAdapter(myAdapter);
        //myAdapter.notifyDataSetChanged();
    }

    // When there are no picture selected from the picker gallery...
    @Override
    public void onCancel() {
        //Log.i(TAG, "User canceled picker activity");
        Toast.makeText(this, "User canceled picker activity", Toast.LENGTH_SHORT).show();

    }

    // button listener for converting images to pdf file
    public void onConvertPdfClick(View view) throws DocumentException, java.io.IOException
    {
        createPdf();
        Toast.makeText(this, "Pdf file created", Toast.LENGTH_SHORT).show();
    }



    // function that converts image data into a pdf file
    public void createPdf() throws  DocumentException, java.io.IOException
    {
        File pdfFolder = new File(Environment.getExternalStorageDirectory(), "EasyConvert"); // check this warning, may be important for diff API levels

        //ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);


        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
            Log.i(TAG, "Folder successfully created");
        }

        if (mSelectedImages != null)
        {

           // progress.setVisibility(View.VISIBLE);

            Date date = new Date();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);

            myPDF = new File(pdfFolder + "/" + timeStamp + ".pdf");

            OutputStream output = new FileOutputStream(myPDF);

            Document document = new Document();
            PdfWriter.getInstance(document, output);

            long startTime, estimatedTime;

            document.open();
            //document.add(new Paragraph("~~~~Hello World!!~~~~"));
            for (int i = 0; i < mSelectedImages.size(); i++)
            {

                // create bitmap from URI in our list
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(mSelectedImages.get(i).path)) );

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

                float fractionalProgress = (i+1)/mSelectedImages.size() * 100;

               // progress.setProgress(Math.round(fractionalProgress));



            }

            //progress.cancel();
            mSelectedImages = null;
            document.close();
            promptForNextAction();

            myAdapter = new ImageSamplesAdapter(mSelectedImages, MainActivity.this);
            mImageSampleRecycler.setAdapter(myAdapter);

            //progress.setVisibility(View.GONE);

        }
    }

    private void viewPdf(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(myPDF), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void emailNote()
    {
        Intent email = new Intent(Intent.ACTION_SEND);
        //email.putExtra(Intent.EXTRA_SUBJECT,mSubjectEditText.getText().toString());
        //email.putExtra(Intent.EXTRA_TEXT, mBodyEditText.getText().toString());
        Uri uri = Uri.parse(myPDF.getAbsolutePath());
        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("message/rfc822");
        startActivity(email);
    }

    public void promptForNextAction()
    {
        final String[] options = { "email", "preview",
                "cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("PDF Saved, What Next?");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("email")){
                    emailNote();
                }else if (options[which].equals("preview")){
                    viewPdf();
                }else if (options[which].equals("cancel")){
                    dialog.dismiss();
                }
            }
        });

        builder.show();

    }



    // on activity result for old gallery and camera code (possibly obsolete)
    /*
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
    */

}
