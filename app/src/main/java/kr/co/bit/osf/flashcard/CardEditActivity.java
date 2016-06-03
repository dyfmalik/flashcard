package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import kr.co.bit.osf.flashcard.common.ImageUtil;
import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.debug.Dlog;
import kr.co.bit.osf.flashcard.models.User;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class CardEditActivity extends AppCompatActivity {
    // dto
    private CardDTO card = null;
    // intent
    private int intentRequestCode = 0;
    private int intentResultCode = RESULT_CANCELED;
    // view
    private ImageView imageView = null;
    private TextView cardEditTextView = null;
    // camera, gallery
    private File photoFile = null;
    private String photoFilePath = null;
    private DialogInterface dialogInterface;
    // delete card
    List<CardDTO> cardList = null;
    // activity state
    private String activityStateDataName = "activityStateDataName";
    private ActivityState currentState;

    //by me
    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    //by me
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";
    private static final String TAG = "Storage#MainActivity";
    private ProgressDialog mProgressDialog;
    // [START declare_ref]
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    // [END declare_ref]
    private BroadcastReceiver mDownloadReceiver;
    //by me
    TextView textLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_edit);


        //by me
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
        mStorageRef = FirebaseStorage.getInstance().getReference();
        textLink=(TextView)findViewById(R.id.textViewLink);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //


        Dlog.i("started");

        // full screen
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        // get intent data
        Intent intent = getIntent();
        intentRequestCode = intent.getIntExtra(IntentExtrasName.REQUEST_CODE, 0);
        Dlog.i("intentRequestCode:" + intentRequestCode);
        switch (intentRequestCode) {
            case IntentRequestCode.CARD_ADD:
                Dlog.i("intentRequestCode in case:" + intentRequestCode);
                card = intent.getParcelableExtra(IntentExtrasName.SEND_DATA);
                card.setType(FlashCardDB.CardEntry.TYPE_USER);
            case IntentRequestCode.CARD_EDIT:
            case IntentRequestCode.CARD_DELETE:
                Dlog.i("intentRequestCode in case:" + intentRequestCode);
                card = intent.getParcelableExtra(IntentExtrasName.SEND_DATA);
                if (card == null) {
                    Dlog.i("getParcelableExtra: sendData:no data");
                    finish();
                    return;
                }
                break;
            //delete cardList
            case IntentRequestCode.CARD_DELETE_LIST:
                Dlog.i("intentRequestCode in case:" + intentRequestCode);
                cardList = intent.getParcelableArrayListExtra(IntentExtrasName.SEND_DATA);
                Dlog.i("CARD_DELETE_LIST:cardList:size():" + cardList.size());
                if (cardList == null) {
                    Dlog.i("getParcelableArrayListExtra:sendData: no data");
                    finish();
                    return;
                }
                break;
        }

        // process requested task
        Dlog.i("getExtras:sendData:" + card);
        intentResultCode = RESULT_OK;
        if (intentRequestCode == IntentRequestCode.CARD_DELETE) {
            Dlog.i("delete data:" + card);
            // delete card
            FlashCardDB db = new FlashCardDB(CardEditActivity.this);
            if (db.deleteCard(card.getId()) == false) {
                intentResultCode = RESULT_CANCELED;
                Dlog.i("delete error:" + card);
            }
            finish();
            return;
        }
        //delete cardList
        if (intentRequestCode == IntentRequestCode.CARD_DELETE_LIST) {
            Dlog.i("delete data:" + cardList);
            //delete cardList
            if (cardList.size() > 0) {
                FlashCardDB db = new FlashCardDB(CardEditActivity.this);
                Dlog.i("delete data:size():" + cardList.size());
                if (db.deleteCard(cardList) == false) {
                    intentResultCode = RESULT_CANCELED;
                    Dlog.i("delete error:" + card);
                }
                finish();
                return;
            }
        }

        imageView = (ImageView) findViewById(R.id.cardEditImageView);
        cardEditTextView = (TextView) findViewById(R.id.cardEditTextView);

        //show card
        if (intentRequestCode == IntentRequestCode.CARD_EDIT) {
            ImageUtil.loadCardImageIntoImageView(this, card, imageView);
        }
        if (card.getName() != null) {
            cardEditTextView.setText(card.getName());
        }

        //imageView - card Image
        (findViewById(R.id.frameLayout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageClicked();
            }
        });

        //textView - card Name
        (findViewById(R.id.cardEditTextView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textClicked();
            }
        });

        //yes, no Button
        (findViewById(R.id.cardEditYesButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("yes button:check:card:" + card);
                boolean isOk = false;
                switch (intentRequestCode) {
                    case IntentRequestCode.CARD_ADD:
                    case IntentRequestCode.CARD_EDIT:
                        Dlog.i("yes button:check:intentRequestCode" + intentRequestCode);
                        isOk = updatedCardIsOk(true);
                        break;
                }

                if (isOk) {
                    Dlog.i("yes button:check:ok:" + card);
                    FlashCardDB db = new FlashCardDB(getApplicationContext());
                    card.setName(cardEditTextView.getText().toString().trim());
                    if (intentRequestCode == IntentRequestCode.CARD_ADD) {
                        Dlog.i("yes button:check:ok:add" + card);
                        db.addCard(card);
                        submitPost();
                    } else {
                        Dlog.i("yes button:check:ok:update" + card);
                        db.updateCard(card);
                    }
                    finish();
                } else {
                    Dlog.i("yes button:check:fail:" + card);
                }
            }
        });

        (findViewById(R.id.cardEditNoButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentResultCode = RESULT_CANCELED;
                finish();
            }
        });



        //by me
        // Download receiver
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "downloadReceiver:onReceive:" + intent);
                hideProgressDialog();

                if (MyDownloadService.ACTION_COMPLETED.equals(intent.getAction())) {
                    String path = intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH);
                    long numBytes = intent.getLongExtra(MyDownloadService.EXTRA_BYTES_DOWNLOADED, 0);

                    // Alert success
                    showMessageDialog("Success", String.format(Locale.getDefault(),
                            "%d bytes downloaded from %s", numBytes, path));
                }

                if (MyDownloadService.ACTION_ERROR.equals(intent.getAction())) {
                    String path = intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH);

                    // Alert failure
                    showMessageDialog("Error", String.format(Locale.getDefault(),
                            "Failed to download from %s", path));
                }
            }
        };
        //by me
    }
    //by me
    private void submitPost() {
        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(CardEditActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            //writeNewPost(userId, user.username, title, body);
                        }

                        // Finish this Activity, back to the stream
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    //by me for downloading downloadpath
    private void showMessageDialog(String title, String message) {
        android.support.v7.app.AlertDialog ad = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

    private void imageClicked() {
        Dlog.i("CardEditActivity: imageClicked");
        View dlg = CardEditActivity.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
        // get user action from dialog
        final CharSequence[] items = {
                getString(R.string.card_edit_image_dialog_camera_button_text),
                getString(R.string.card_edit_image_dialog_gallery_button_text),
                "Ambil Dari Google"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(CardEditActivity.this);
        TextView editTitle = (TextView)dlg.findViewById(R.id.dialogTitleTextView);
        editTitle.setText(R.string.card_edit_image_dialog_title);
        editTitle.setVisibility(View.VISIBLE);

        TextView editMenuOne = (TextView)dlg.findViewById(R.id.dialogMenuTextViewOne);
        editMenuOne.setText(R.string.card_edit_image_dialog_camera_button_text);
        editMenuOne.setVisibility(View.VISIBLE);
        editMenuOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("cardEdit:photoCaptureButton clicked");
                //capture card
                photoFile = ImageUtil.getOutputMediaFile(ImageUtil.MEDIA_TYPE_IMAGE);
                Dlog.i("cardEdit:photoCaptureButton clicked");
                photoFilePath = photoFile.getAbsolutePath();

                //by me


                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                mFileUri = Uri.fromFile(photoFile);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, IntentRequestCode.CAPTURE_IMAGE);
                }
                dialogInterface.dismiss();


            }
        });

        TextView editmenuTwo = (TextView)dlg.findViewById(R.id.dialogMenuTextViewTwo);
        editmenuTwo.setText(R.string.card_edit_image_dialog_gallery_button_text);
        editmenuTwo.setVisibility(View.VISIBLE);
        editmenuTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("cardEdit:galleryButton clicked");
                // select card in gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        IntentRequestCode.SELECT_PICTURE);
                dialogInterface.dismiss();
            }

        });
       /* builder.setView(dlg);
        dialogInterface = builder.show();*/


        //get from google
        TextView editmenuThree = (TextView)dlg.findViewById(R.id.dialogMenuTextViewThree_);
        editmenuThree.setText("Get From Google");
        editmenuThree.setVisibility(View.VISIBLE);
        editmenuThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("cardEdit:GoogleButton clicked");
                // select card in gallery
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image*//*");
                startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        IntentRequestCode.SELECT_PICTURE);
                Toast.makeText(CardEditActivity.this, "Google has clicked", Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }

        });
        builder.setView(dlg);
        dialogInterface = builder.show();
    }

    //by me [START upload_from_uri]
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL
                        mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();


                        // [START_EXCLUDE]
                        hideProgressDialog();
                        //by me
                        textLink.setText(mDownloadUrl.toString());
                        //updateUI(mAuth.getCurrentUser());
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        mDownloadUrl = null;

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(CardEditActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                        //updateUI(mAuth.getCurrentUser());
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_uri]

    private void textClicked() {

        Dlog.i("CardEditActivity: textClicked");

        AlertDialog.Builder alert = new AlertDialog.Builder(CardEditActivity.this);
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder");
        View dlg = CardEditActivity.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);

        TextView editTitle = (TextView)dlg.findViewById(R.id.dialogTitleTextView);
        editTitle.setText(R.string.card_edit_text_dialog_title);
        editTitle.setVisibility(View.VISIBLE);

        TextView editMenuOne = (TextView)dlg.findViewById(R.id.dialogMenuTextViewOne);
        editMenuOne.setText(R.string.card_edit_text_dialog_message);
        editMenuOne.setVisibility(View.VISIBLE);

        final EditText editText = (EditText)dlg.findViewById(R.id.dialogMenuEditTextOne);
        editText.setVisibility(View.VISIBLE);
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder:setView");
        if (card.getName() != null) {
            editText.setText(card.getName());
            editText.setSelection(card.getName().length());
        } else {
            editText.setText("");
        }
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder:setText");
        alert.setPositiveButton(R.string.card_edit_text_dialog_ok_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = editText.getText().toString();
                cardEditTextView.setText(name);
                card.setName(name);
            }
        });

        alert.setNegativeButton(R.string.card_edit_text_dialog_cancel_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.setView(dlg);
        dialogInterface = alert.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.i("requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IntentRequestCode.CAPTURE_IMAGE:
                case IntentRequestCode.SELECT_PICTURE:
                    if (requestCode == IntentRequestCode.SELECT_PICTURE) {
                        photoFilePath = ImageUtil.getImagePathFromIntentData(this, data);
                    }
                    card.setImagePath(photoFilePath);
                    card.setType(FlashCardDB.CardEntry.TYPE_USER);
                    Dlog.i("photoFilePath:" + photoFilePath);
                    Dlog.i("photoFilePath:" + card);
                    ImageUtil.loadCardImageIntoImageView(this, card, imageView);
                    Dlog.i("photoFilePath:" + card.getImagePath());
                    Dlog.i("photoFilePath:" + card.getImagePath());
                    uploadFromUri(mFileUri);
                    break;
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // restore saved activity state
        currentState = savedInstanceState.getParcelable(activityStateDataName);
        try {
            Dlog.i(currentState.toString());
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
        // camera, gallery
        photoFilePath = currentState.getPhotoFilePath();
        // image, text
        try {
            ImageUtil.loadCardImageIntoImageView(this, currentState.getCard(), imageView);
            cardEditTextView.setText(getResources().getString(R.string.card_edit_text_view_hint));
            if (currentState.card.getName().length() > 0) {
                cardEditTextView.setText(currentState.card.getName());
            }
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save current activity state
        currentState = new ActivityState(intentRequestCode, intentResultCode,
                photoFilePath, card);
        outState.putParcelable(activityStateDataName, currentState);
        Dlog.i(currentState.toString());

        //by me
        outState.putParcelable(KEY_FILE_URI, mFileUri);
        outState.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
        //
    }

    @Override
    public void finish() {
        // return data
        if (intentResultCode == RESULT_OK) {
            Intent data = new Intent();
            switch (intentRequestCode) {
                case IntentRequestCode.CARD_ADD:
                case IntentRequestCode.CARD_EDIT:
                case IntentRequestCode.CARD_DELETE:
                    if (updatedCardIsOk() == true) {
                        data.putExtra(IntentExtrasName.RETURN_DATA, card);
                    } else {
                        Dlog.i("updatedCardIsOk() == false");
                        intentResultCode = RESULT_CANCELED;
                    }
                    break;
                case IntentRequestCode.CARD_DELETE_LIST:
                    break;
            }
            setResult(intentResultCode, data);
        } else {
            setResult(intentResultCode);
        }
        Dlog.i("setResult:" + intentResultCode);
        super.finish();
    }

    public boolean updatedCardIsOk(boolean printMassege) {
        // check card content
        boolean isOk = (card != null);
        String errorMessage = "";

        // check image
        if (isOk) {
            try {
                if (card.getType() == FlashCardDB.CardEntry.TYPE_DEMO) {
                    isOk = (card.getImageName().length() > 0);
                    Dlog.i("getImageName().length():check:" + card.getImageName().length());
                } else {
                    isOk = (card.getImagePath().length() > 0);
                    Dlog.i("getImagePath().length():check:" + card.getImagePath().length());
                }
            } catch (Exception e) {
                isOk = false;
                Dlog.i("getImagePath():error:");
            }
            if (isOk == false) {
                errorMessage = getResources().getString(R.string.card_edit_error_message_no_image);
            }
        }

       // check text
        if (isOk) {
            try {
                isOk = (card.getName().length() > 0);
                Dlog.i("getName().length():check:" + card.getName().length());
            } catch (Exception e) {
                isOk = false;
                Dlog.i("getName():error:");
            }
            if (isOk == false) {
                errorMessage = getResources().getString(R.string.card_edit_error_message_no_text);
            }
        }

        if(printMassege == true) {
            if (isOk == false) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }

        return isOk;
    }

    public boolean updatedCardIsOk() {
        return updatedCardIsOk(false);
    }

    private class ActivityState implements Parcelable {
        // intent
        private int intentRequestCode;
        private int intentResultCode;
        // camera, gallery
        private String photoFilePath;
        // card
        private CardDTO card;

        public ActivityState(int intentRequestCode, int intentResultCode,
                             String photoFilePath, CardDTO card) {
            this.intentRequestCode = intentRequestCode;
            this.intentResultCode = intentResultCode;
            this.photoFilePath = photoFilePath;
            this.card = card;
        }

        protected ActivityState(Parcel in) {
            intentRequestCode = in.readInt();
            intentResultCode = in.readInt();
            photoFilePath = in.readString();
            card = in.readParcelable(CardDTO.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(intentRequestCode);
            dest.writeInt(intentResultCode);
            dest.writeString(photoFilePath);
            dest.writeParcelable(card, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public final Creator<ActivityState> CREATOR = new Creator<ActivityState>() {
            @Override
            public ActivityState createFromParcel(Parcel in) {
                return new ActivityState(in);
            }

            @Override
            public ActivityState[] newArray(int size) {
                return new ActivityState[size];
            }
        };

        public int getIntentRequestCode() {
            return intentRequestCode;
        }

        public void setIntentRequestCode(int intentRequestCode) {
            this.intentRequestCode = intentRequestCode;
        }

        public int getIntentResultCode() {
            return intentResultCode;
        }

        public void setIntentResultCode(int intentResultCode) {
            this.intentResultCode = intentResultCode;
        }

        public String getPhotoFilePath() {
            return photoFilePath;
        }

        public void setPhotoFilePath(String photoFilePath) {
            this.photoFilePath = photoFilePath;
        }

        public CardDTO getCard() {
            return card;
        }

        public void setCard(CardDTO card) {
            this.card = card;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ActivityState that = (ActivityState) o;

            if (intentRequestCode != that.intentRequestCode) return false;
            if (intentResultCode != that.intentResultCode) return false;
            if (!photoFilePath.equals(that.photoFilePath)) return false;
            return card.equals(that.card);

        }

        @Override
        public int hashCode() {
            int result = intentRequestCode;
            result = 31 * result + intentResultCode;
            result = 31 * result + photoFilePath.hashCode();
            result = 31 * result + card.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ActivityState{" +
                    "intentRequestCode=" + intentRequestCode +
                    ", intentResultCode=" + intentResultCode +
                    ", photoFilePath='" + photoFilePath + '\'' +
                    ", card=" + card +
                    '}';
        }
    }


    //by me
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    //by me
    @Override
    public void onStart() {
        super.onStart();
        // Register download receiver
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mDownloadReceiver, MyDownloadService.getIntentFilter());
    }
    //by me
    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadReceiver);
    }
}
