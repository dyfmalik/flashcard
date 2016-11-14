package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
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

import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;


import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.bit.osf.flashcard.common.ActivityId;
import kr.co.bit.osf.flashcard.common.ImageConfig;
import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.common.IntentReturnCode;
import kr.co.bit.osf.flashcard.db.BoxDTO;
import kr.co.bit.osf.flashcard.db.Card;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.FlashCardDB_;
import kr.co.bit.osf.flashcard.db.Kategori;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class BoxListActivity_ extends AppCompatActivity implements Response.Listener<String> {
    // db
    private FlashCardDB db = null;
    private FlashCardDB_ db_ = null;
    private List<BoxDTO> boxList = null;
    //private List<Kategori> kategoriList=null;
    // grid view
    //private GridView gridView = null;
    //private BoxListAdapter adapter = null;
    //dialog
    private DialogInterface dialogInterface = null;

    //bm
    //ProgressDialog progressDialog;
    //ArrayList<CardDTO> rest;
    //String Error;
    ArrayList<Kategori> kategoriList;
    private static final int REQUEST_RESPONSE = 1;
    //public static final String ID_KAT = "id_kat";
    //String total;
    GridView gridView;
    String newBoxName;
    String id_kat_position;
    FunDapter<Kategori> adapter;
    Kategori kategori;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_list_);
        Dlog.i("");

        //by me
        gridView=(GridView)findViewById(R.id.boxListGridView);
        kategori=new Kategori();

        // read state from db
        //commented by me
        db = new FlashCardDB(this);
        db_=new FlashCardDB_(this);
        final StateDTO cardState = db.getState();
        Dlog.i("read card state:" + cardState);

        // state.cardId > 0 : start card view activity
        if (cardState.getBoxId() > 0) {
            Intent intent = new Intent(this, CardListActivity.class);
            startActivity(intent);
        }

        // help image
        boolean isPortrait = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        ImageView helpImageView = (ImageView) (findViewById(R.id.boxListHelpImage));
        if (db.isShowHelp(ActivityId.BoxList) && isPortrait) {
            Dlog.i("show help image");
            helpImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dlog.i("help image clicked");
                    v.setVisibility(View.GONE);
                    // update help count
                    db.updateHelpCount(ActivityId.BoxList);
                }
            });
        } else {
            helpImageView.setVisibility(View.GONE);
        }


        //bm: editing gridview & setting kategori list
        getData();
        ///bm


        // read box list
        /*boxList = db.getBoxAll();
        Dlog.i("getBoxAll:size():" + boxList.size()); bm*/


        // list view
        gridView = (GridView) findViewById(R.id.boxListGridView);
        /*adapter = new BoxListAdapter(this, kategoriList);
        gridView.setAdapter(adapter);*/


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kategori kategori1=kategoriList.get(position);
                int id_kat=kategori1.id_kat;
                Intent intent = new Intent(getApplicationContext(), CardListActivity_.class);
                //Intent intent = new Intent(getApplicationContext(), CardActivity.class);
                //startActivity(intent);
                intent.putExtra("id_kat", Integer.toString(id_kat));
                startActivityForResult(intent, REQUEST_RESPONSE);

                //Toast.makeText(getApplicationContext(), Integer.toString(id_kat),Toast.LENGTH_SHORT).show();

                /*Intent intent = new Intent(getApplicationContext(), CardListActivity.class);
                startActivityForResult(intent, IntentRequestCode.CARD_LIST_VIEW);*/

            }
        });

        //long click dialog
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // get user action from dialog
                Dlog.i("Item long click dialog: start");
                View dlg = BoxListActivity_.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
                Dlog.i("Item long click dialog - add View");
                AlertDialog.Builder builder = new AlertDialog.Builder(BoxListActivity_.this);
                Dlog.i("Item long click dialog - add AlertDialog.Builder");
                TextView textView1 = (TextView) dlg.findViewById(R.id.dialogMenuTextViewOne);
                TextView textView2 = (TextView) dlg.findViewById(R.id.dialogMenuTextViewTwo);
                Dlog.i("Item long click dialog - add item");
                textView1.setText(R.string.box_edit_dialog_menu_edit_text);
                textView1.setVisibility(View.VISIBLE);
                textView2.setText(R.string.box_edit_dialog_menu_delete_text);
                textView2.setVisibility(View.VISIBLE);
                Dlog.i("Item long click dialog - update Text,VISIBLE");
                //update box dialog
                textView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Dlog.i("dialog:edit box");
                        View dlg2 = BoxListActivity_.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
                        final EditText inputText = (EditText) dlg2.findViewById(R.id.dialogMenuEditTextOne);
                        TextView textView = (TextView) dlg2.findViewById(R.id.dialogTitleTextView);
                        TextView textView2 = (TextView) dlg2.findViewById(R.id.dialogMenuTextViewOne);
                        Dlog.i("dialog:edit box - add dialog item");
                        inputText.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        textView2.setVisibility(View.VISIBLE);
                        Dlog.i("dialog:edit box - set VISIBLE");
                        //inputText.setText(boxList.get(position).getName());bm
                        inputText.setText(kategoriList.get(position).nama_kat);
                        inputText.setSelection(inputText.length());
                        AlertDialog.Builder input = new AlertDialog.Builder(BoxListActivity_.this);
                        textView.setText(R.string.box_edit_dialog_edit_title);
                        textView2.setText(R.string.box_edit_dialog_edit_message);
                        Dlog.i("dialog:edit box - set text");
                        input.setView(dlg2);
                        Dlog.i("dialog:edit box - add dialog view");
                        input.setPositiveButton(R.string.box_edit_dialog_edit_ok_button_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // set new box name
                                        newBoxName = inputText.getText().toString();
                                        id_kat_position=Integer.toString(kategoriList.get(position).id_kat);
                                        //boxList.get(position).setName(newBoxName);bm
                                        //kategoriList.get(position).nama_kat=newBoxName;
                                        db_.editKategori(kategoriList.get(position).id_kat, newBoxName );
                                        //editKategori();
                                        /*getData();*/
                                        kategoriList.get(position).nama_kat=newBoxName;
                                        //db.updateBox(boxList.get(position));
                                        //adapter.notifyDataSetChanged();
                                        adapter.updateData(kategoriList);
                                        Toast.makeText(getApplicationContext(),kategoriList.get(position).nama_kat, Toast.LENGTH_SHORT ).show();
                                        //Dlog.i("new box name:" + newBoxName);

                                    }
                                });
                        dialogInterface.dismiss();
                        input.show();
                    }
                });
                // delete box dialog
                textView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Dlog.i("dialog:delete box");
                        View dlg2 = BoxListActivity_.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
                        Dlog.i("dialog:delete box - create View");
                        TextView deleteTitle = (TextView) dlg2.findViewById(R.id.dialogTitleTextView);
                        TextView deleteMessage = (TextView) dlg2.findViewById(R.id.dialogMenuTextViewOne);
                        Dlog.i("dialog:delete box - add dialog item");
                        deleteTitle.setVisibility(View.VISIBLE);
                        deleteMessage.setVisibility(View.VISIBLE);
                        Dlog.i("dialog:delete box - item VISIBLE");
                        final AlertDialog.Builder delete = new AlertDialog.Builder(BoxListActivity_.this);
                        deleteTitle.setText(R.string.box_edit_dialog_delete_title);
                        deleteMessage.setText(R.string.box_edit_dialog_delete_message);
                        delete.setView(dlg2);
                        delete.setPositiveButton(R.string.box_edit_dialog_delete_ok_button_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // delete box
                                        int deleteBoxId = kategoriList.get(position).id_kat;
                                        /*Dlog.i("delete box name:" + boxList.get(position).getName());*/
                                       /* if (db.deleteBox(deleteBoxId)) {
                                            db.deleteCardByBoxId(deleteBoxId);
                                            boxList.remove(position);
                                            Dlog.i("delete box id:" + deleteBoxId);
                                            adapter.notifyDataSetChanged();
                                        }*/
                                        db_.hapusKategori(deleteBoxId);
                                        if(db_.successDelete==1)
                                        {
                                            kategoriList.remove(position);
                                            adapter.notifyDataSetChanged();
                                        }


                                    }
                                });
                        delete.setNegativeButton(R.string.box_edit_dialog_delete_cancel_button_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Canceled.
                                    }
                                });
                        dialogInterface.dismiss();
                        delete.show();

                    }
                });
                builder.setView(dlg);
                dialogInterface = builder.show();

                return true;
            }
        });
    }

    //menu option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu == null) {
            Dlog.i("menu is null : " + menu);
            finish();
        }
        Dlog.i("onCreateOptionMenu : " + "OK");
        getMenuInflater().inflate(R.menu.activity_box_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null && boxList == null) {
            Dlog.i("item : " + item + " boxList : " + kategoriList.toString());
            finish();
        }
        Integer id = item.getItemId();
        switch (id) {
            // add box
            // set an EditText view to get user input
            case R.id.box_list_menu_add:
                Dlog.i("dialog:add box");
                View dlg = BoxListActivity_.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
                final EditText inputText = (EditText) dlg.findViewById(R.id.dialogMenuEditTextOne);
                TextView titleTextView = (TextView) dlg.findViewById(R.id.dialogTitleTextView);
                TextView textView = (TextView) dlg.findViewById(R.id.dialogMenuTextViewOne);
                Dlog.i("add R.id.dialog Item");
                titleTextView.setText(R.string.box_edit_dialog_add_title);
                textView.setText(R.string.box_edit_dialog_add_message);
                Dlog.i("dialog Item setText");
                inputText.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                Dlog.i("dialog Item setVisible");
                AlertDialog.Builder input = new AlertDialog.Builder(BoxListActivity_.this);
                Dlog.i("dialog create");
                input.setView(dlg);
                input.setPositiveButton(R.string.box_edit_dialog_add_ok_button_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // add box
                                String addBoxName = inputText.getText().toString();
                                Dlog.i("add box name:" + addBoxName);
                                //BoxDTO box = db.addBox(addBoxName);
                                //bm
                                db_.tambahKategori(addBoxName);

                                kategori.nama_kat = addBoxName;
                                String box=kategori.nama_kat;
                                if (box != null) {
                                    // refresh list
                                    Dlog.i("refresh list:size():before:" + kategoriList.size());
                                    //kategoriList.add(2, );
                                    Dlog.i("refresh list:size():after:" + boxList.size());
                                    gridView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    // move last position
                                    if (adapter.getCount() > 0) {
                                        gridView.smoothScrollToPosition(adapter.getCount() - 1);
                                    }
                                }
                            }
                        });
                input.setNegativeButton(R.string.box_edit_dialog_add_cancel_button_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });
                input.show();

                break;
            //??
            case R.id.box_list_menu_sort_asc:
                Dlog.i("asc sort start");
                Collections.sort(boxList, new NameAscCompare());
                Dlog.i("asc sort - collections sort call");
                db.updateBoxSeq(boxList);
                adapter.notifyDataSetChanged();
                Dlog.i("asc sort end");
                break;
            //??
            case R.id.box_list_menu_sort_desc:
                Dlog.i("desc sort start");
                Collections.sort(boxList, new NameDescCompare());
                Dlog.i("desc sort - collections sort call");
                db.updateBoxSeq(boxList);
                adapter.notifyDataSetChanged();
                Dlog.i("desc sort end");
                break;
            //???
            case R.id.box_list_menu_sort_shuffle:
                Dlog.i("shuffle start");
                Collections.shuffle(boxList);
                Dlog.i("Shuffle - collections sort call");
                db.updateBoxSeq(boxList);
                adapter.notifyDataSetChanged();
                Dlog.i("shuffle end");
                break;
            case R.id.box_list_menu_sort_reset:
                Dlog.i("Id Asc start");
                Collections.sort(boxList, new NoAscCompare());
                Dlog.i("Id Asc - collections sort call");
                db.updateBoxSeq(boxList);
                adapter.notifyDataSetChanged();
                Dlog.i("Id Asc end");
                break;
            case R.id.box_list_menu_credit:
                Dlog.i("credit");
                Intent intent = new Intent(this, CreditActivity.class);
                startActivity(intent);
                break;
            case R.id.coba2:
                Toast.makeText(this, "coba2",Toast.LENGTH_LONG).show();
                Intent intent2 = new Intent(this, CardActivity.class);
                startActivity(intent2);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Dlog.i("");
        // save current state
        if (db != null) {
            db.updateState(0, 0);
        }
    }

    //by me
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
                Picasso.with(getApplicationContext()).load("http://10.0.2.2/flashcard/" + url).into(imageView);
                /*imageView.setPadding(0, 0, 0, 0);*/
                //imageView.setAdjustViewBounds(true);
            }
        });
        adapter=new FunDapter<>(getApplicationContext(),kategoriList,R.layout.activity_box_list_item_, dictionary);
        gridView.setAdapter(adapter);

    }

    /*public class BoxListAdapter extends BaseAdapter {
        private Context context;
        //private List<BoxDTO> list;
        private ArrayList<Kategori> list;

        public BoxListAdapter() {
        }

        public BoxListAdapter(Context c, ArrayList<Kategori> list) {
            context = c;
            this.list = list;
            Dlog.i("adapter:list:size():" + list.size());
        }

        @Override
        public int getCount() {
            if (list != null) {
                return list.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            Dlog.i("position:" + position);
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.activity_box_list_item_, null);

            Dlog.i("box position:" + position + ", box:" + list.get(position));
            // image
            ImageView imageView = (ImageView) view.findViewById(R.id.boxListViewItemImage);
            //Card topCard = db.getTopCardByBoxId(list.get(position).id_kat);
            getTopCardByBoxId(list.get(position).id_kat);
            if (topCard != null) {
                Dlog.i("topCard:" + topCard);
                ImageConfig.loadCardImageIntoImageView(BoxListActivity_.this, topCard, imageView);
            } else {
                Dlog.i("empty_image");
                imageView.setImageResource(R.drawable.default_image_empty_image);
            }
            // text
            TextView nameTextView = (TextView) view.findViewById(R.id.boxListViewItemText);
            nameTextView.setText(list.get(position).getName());
            // card count
            TextView countTextView = (TextView) view.findViewById(R.id.boxListViewItemCount);
            countTextView.setText("" + db.getCardCountByBoxId(list.get(position).getId()));

            return view;
        }
    }*/

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.i("requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (data == null) {
            finish();
            return;
        }
        if (resultCode == RESULT_OK) {
            int returnCode = data.getIntExtra(IntentExtrasName.RETURN_CODE, 0);
            Dlog.i("returnCode=");
            switch (returnCode) {
                case IntentReturnCode.BOX_LIST_REFRESH:
                    Dlog.i("Box_List_REFRESH : " + IntentReturnCode.BOX_LIST_REFRESH);
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    Dlog.i("BoxListREFRESH error : " + IntentReturnCode.BOX_LIST_REFRESH);
                    return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }*/


    static class NameAscCompare implements Comparator<BoxDTO> {


        @Override
        public int compare(BoxDTO arg0, BoxDTO arg1) {
            return arg0.getName().compareTo(arg1.getName());
        }

    }

    static class NameDescCompare implements Comparator<BoxDTO> {


        @Override
        public int compare(BoxDTO arg0, BoxDTO arg1) {
            return arg1.getName().compareTo(arg0.getName());
        }
    }


    static class NoAscCompare implements Comparator<BoxDTO> {

        @Override
        public int compare(BoxDTO arg0, BoxDTO arg1) {
            return arg0.getId() < arg1.getId() ? -1 : arg0.getId() > arg1.getId() ? 1 : 0;
        }

    }


    //by me
    private void getData(){

        String url="http://10.0.2.2/flashcard/webservice/getAllKategori.php";

        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(),"Error while reading data",Toast.LENGTH_SHORT).show();
                if(error instanceof com.android.volley.TimeoutError){
                    Toast.makeText(getApplicationContext(), "Time Out Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.NoConnectionError){
                    Toast.makeText(getApplicationContext(), "No Connection Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.AuthFailureError){
                    Toast.makeText(getApplicationContext(), "Authentication Failure Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.NetworkError){
                    Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.ServerError){
                    Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
                }
                else if(error instanceof com.android.volley.ParseError){
                    Toast.makeText(getApplicationContext(), "JSON Parse Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }



}
