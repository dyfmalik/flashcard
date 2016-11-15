package kr.co.bit.osf.flashcard.db;

import android.content.Context;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.DynamicImageLoader;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosalgeek.android.json.JsonConverter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kr.co.bit.osf.flashcard.MySingleton;
import kr.co.bit.osf.flashcard.R;

/**
 * Created by Dyferdiansyah on 15/11/2016.
 */

public class FlashCardDB_ {

    Context context;
    //public GridView gridView;
    public int successDelete=0;
    //public ArrayList<Kategori> kategoriList;
    //public FunDapter<Kategori> adapter;

    public FlashCardDB_(Context context) {
        this.context=context;
    }


    public void editKategori(final int id_kat, final String nama_kategori) {

        String url = "http://10.0.2.2/flashcard/webservice/editKategori.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {


                //Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                if(response.contains("berhasil")){
                    Toast.makeText(context, "berhasil", Toast.LENGTH_SHORT).show();
                }
                else if (response.contains("gagal1")){
                    Toast.makeText(context, "gagal1", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(context, "gagal2", Toast.LENGTH_SHORT).show();
                }


            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof com.android.volley.TimeoutError){
                    Toast.makeText(context, "Time Out Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.NoConnectionError){
                    Toast.makeText(context, "No Connection Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.AuthFailureError){
                    Toast.makeText(context, "Authentication Failure Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.NetworkError){
                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.ServerError){
                    Toast.makeText(context, "Server Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.ParseError){
                    Toast.makeText(context, "JSON Parse Error", Toast.LENGTH_SHORT).show();
                }

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_kat", Integer.toString(id_kat));
                params.put("nama_kategori", nama_kategori );
                /*params.put("id_kat", "1");
                params.put("nama_kategori", "yuuu" );*/
                return params;
            }


        };
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);

    }

    public void hapusKategori(final int id_kat) {

        String url = "http://10.0.2.2/flashcard/webservice/delete_kategori.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {


                //Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                if(response.contains("Berhasil Dihapus")){
                    Toast.makeText(context, "berhasil Dihapus", Toast.LENGTH_SHORT).show();
                    successDelete=1;

                }
                else if (response.contains("Gagal Dihapus")){
                    Toast.makeText(context, "gagal1 dihapus", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else
                {
                    Toast.makeText(context, "there is file missing", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }


            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof com.android.volley.TimeoutError){
                    Toast.makeText(context, "Time Out Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.NoConnectionError){
                    Toast.makeText(context, "No Connection Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.AuthFailureError){
                    Toast.makeText(context, "Authentication Failure Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.NetworkError){
                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.ServerError){
                    Toast.makeText(context, "Server Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.ParseError){
                    Toast.makeText(context, "JSON Parse Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_kat", Integer.toString(id_kat));

                return params;
            }


        };
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);


    }

    public void tambahKategori(final String nama_kat) {

        String url = "http://10.0.2.2/flashcard/webservice/tambahKategori.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {


                //Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                if(response.contains("Berhasil")){
                    Toast.makeText(context, "Berhasil ditambah", Toast.LENGTH_SHORT).show();
                    successDelete=1;

                }
                else if (response.contains("Gagal")){
                    Toast.makeText(context, "gagal1 ditambah", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else
                {
                    Toast.makeText(context, "there is file missing", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }


            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof com.android.volley.TimeoutError){
                    Toast.makeText(context, "Time Out Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.NoConnectionError){
                    Toast.makeText(context, "No Connection Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.AuthFailureError){
                    Toast.makeText(context, "Authentication Failure Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.NetworkError){
                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.ServerError){
                    Toast.makeText(context, "Server Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }
                else if(error instanceof com.android.volley.ParseError){
                    Toast.makeText(context, "JSON Parse Error", Toast.LENGTH_SHORT).show();
                    successDelete=0;
                }

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nama_kat", nama_kat);

                return params;
            }


        };
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);


    }


    /*public GridView getData(final GridView gridView){
        this.gridView=gridView;

        String url="http://10.0.2.2/flashcard/webservice/getAllKategori.php";

        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                Log.d("gue", response);
                kategoriList=new JsonConverter<Kategori>().toArrayList(response,Kategori.class);
                BindDictionary<Kategori> dictionary=new BindDictionary<>();
                dictionary.addStringField(R.id.boxListViewItemText, new StringExtractor<Kategori>() {
                    @Override
                    public String getStringValue(Kategori kategori, int i) {
                        return kategori.nama_kat;
                    }
                });
                dictionary.addDynamicImageField(R.id.boxListViewItemImage, new StringExtractor<Kategori>() {
                    @Override
                    public String getStringValue(Kategori kategori, int i) {
                        return kategori.img_url;
                    }
                }, new DynamicImageLoader() {
                    @Override
                    public void loadImage(String url, ImageView imageView) {
                        Picasso.with(context).load("http://10.0.2.2/flashcard/" + url).into(imageView);
                    *//*imageView.setPadding(0, 0, 0, 0);*//*
                        //imageView.setAdjustViewBounds(true);
                    }
                });
                adapter=new FunDapter<>(context, kategoriList, R.layout.activity_box_list_item_, dictionary);
                gridView.setAdapter(adapter);

            }
        } ,new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(),"Error while reading data",Toast.LENGTH_SHORT).show();
                if(error instanceof com.android.volley.TimeoutError){
                    Toast.makeText(context, "Time Out Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.NoConnectionError){
                    Toast.makeText(context, "No Connection Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.AuthFailureError){
                    Toast.makeText(context, "Authentication Failure Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.NetworkError){
                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.ServerError){
                    Toast.makeText(context, "Server Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.ParseError){
                    Toast.makeText(context, "JSON Parse Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        MySingleton.getInstance(context).addToRequestQueue(stringRequest);
        return gridView;
    }
*/


}
