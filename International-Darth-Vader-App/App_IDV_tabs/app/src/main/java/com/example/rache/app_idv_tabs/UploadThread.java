package com.example.rache.app_idv_tabs;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.EditText;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;

/**
 * Title: UploadThread
 * Description: This thread will handle uploading files from the mobile device to the Amazon
 *              Web Services S3 Bucket database.
 */

public class UploadThread extends Thread {
    String fileName;
    String titleText;
    String tagText;
    CheckBox explicitBox;
    Context context;
    String bucketName;
    int counter;

    /* Thread constructor that retrieves information from PlaceholderFragment to be used
       in the thread */
    public UploadThread(String audioClipName, EditText title, EditText tag, CheckBox explicit,
                        Context appContext, String bucket, int counterNum){
        fileName    = audioClipName;
        titleText   = title.getText().toString();
        tagText     = tag.getText().toString();
        explicitBox = explicit;
        context     = appContext;
        bucketName  = bucket;
        counter     = counterNum;
    }

    /* run() will call the actual file upload operation */
    public void run() {uploadAudio(fileName, titleText, tagText, explicitBox, context, bucketName,
            counter);}

    /* Taking user-provided data and putting it into Amazon S3 bucket */
    public void uploadAudio(String fileName, String titleText, String tagText, CheckBox explicitBox,
                            Context context, String bucketName, int counter){
        AmazonS3Client as3client;
        long lengthOfFileToUpload;
        File audioClip = new File(fileName);

        if(titleText.equals("")) titleText = titleText + "VoiceClip" + counter;

        final String key = titleText.trim() + ".m4a";
        final String tagKey = "tag";
        final String explicitKey = "explicit";

        lengthOfFileToUpload = audioClip.length();
        as3client = Util.getS3Client(context);

        PutObjectRequest por = new PutObjectRequest(bucketName, key, audioClip);
        ObjectMetadata om = new ObjectMetadata();

        om.addUserMetadata(tagKey, tagText.trim());
        om.addUserMetadata(explicitKey, "Explicit: " + explicitBox.isChecked());

        om.setContentLength(lengthOfFileToUpload);
        por.withMetadata(om);
        as3client.putObject(por);
    }
}
