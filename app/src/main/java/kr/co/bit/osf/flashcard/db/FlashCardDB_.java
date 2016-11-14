package kr.co.bit.osf.flashcard.db;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import kr.co.bit.osf.flashcard.MySingleton;

/**
 * Created by Dyferdiansyah on 15/11/2016.
 */

public class FlashCardDB_ {

    Context context;
    public int successDelete=0;

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
                if(response.contains("Berhasil ditambah")){
                    Toast.makeText(context, "Berhasil ditambah", Toast.LENGTH_SHORT).show();
                    successDelete=1;

                }
                else if (response.contains("Gagal ditambah")){
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


}
