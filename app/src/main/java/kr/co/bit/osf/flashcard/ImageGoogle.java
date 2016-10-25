package kr.co.bit.osf.flashcard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;



public class ImageGoogle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_google);
//
    }
    public void logout(View v)
    {
        //FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, SignUpActivity.class));

    }
}
