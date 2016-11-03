package kr.co.bit.osf.flashcard;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.DynamicImageLoader;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Request;
import com.kosalgeek.android.json.JsonConverter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kr.co.bit.osf.flashcard.db.Card;


public class CardActivity extends AppCompatActivity implements Response.Listener<String> {

    final String TAG=this.getClass().getSimpleName();
    ListView lvCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        lvCard=(ListView)findViewById(R.id.listCardView);
        Toast.makeText(this, "created", Toast.LENGTH_SHORT).show();
        getData();
    }
    public void getData(){
        String url="http://10.0.2.2/flashcard/webservice/getCards.php";

        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Error while reading data",Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }


    @Override
    public void onResponse(String response) {
        Log.d("gue", response);
        ArrayList<Card> cardlist=new JsonConverter<Card>().toArrayList(response,Card.class);
        BindDictionary<Card> dictionary=new BindDictionary<>();
        dictionary.addStringField(R.id.tvText, new StringExtractor<Card>() {
            @Override
            public String getStringValue(Card card, int i) {
                return card.cback;
            }
        });
        dictionary.addDynamicImageField(R.id.ivImage, new StringExtractor<Card>() {
            @Override
            public String getStringValue(Card card, int i) {
                return card.url;
            }
        }, new DynamicImageLoader() {
            @Override
            public void loadImage(String url, ImageView imageView) {
                Picasso.with(getApplicationContext()).load("http://10.0.2.2/flashcard/" + url).into(imageView);
                /*imageView.setPadding(0, 0, 0, 0);
                imageView.setAdjustViewBounds(true);*/
            }
        });
        FunDapter<Card> adapter=new FunDapter<>(getApplicationContext(),cardlist,R.layout.content_list, dictionary);
        lvCard.setAdapter(adapter);
    }
}
