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
import android.util.Log;
import android.view.View;
import android.provider.MediaStore.Files.FileColumns;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {



    public static final String EXTRA_MESSAGE = "com.example.sledd.helloworld";
    private static final String TAG = "MainActivity";
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private File myPDF;
    private static LinkedList<Uri> imageList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //opening gallery page
    public void btnGallHandler(View view) {
        //Intent i = new Intent(getApplicationContext(), gallerySelection.class);
        //startActivity(i);

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select a Picture!"), PICK_IMAGE_REQUEST_CODE);


    }

    //opening camera page
    public void btnCamHandler(View view) {

        Intent x = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(x);

    }

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


        }
    }


    public void onConvertPdfClick(View view) throws DocumentException, java.io.IOException
    {
        createPdf();
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
            promptForNextAction();

        }
    }
}
