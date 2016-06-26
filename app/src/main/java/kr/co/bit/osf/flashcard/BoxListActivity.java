package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.co.bit.osf.flashcard.common.ActivityId;
import kr.co.bit.osf.flashcard.common.ImageUtil;
import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.common.IntentReturnCode;
import kr.co.bit.osf.flashcard.db.BoxDTO;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class BoxListActivity extends AppCompatActivity {
    // db
    private FlashCardDB db = null;
    private List<BoxDTO> boxList = null;
    // grid view
    private GridView gridView = null;
    private BoxListAdapter adapter = null;
    //dialog
    private DialogInterface dialogInterface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_list);
        Dlog.i("");

        // read state from db
        db = new FlashCardDB(this);
        StateDTO cardState = db.getState();
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

        // read box list
        boxList = db.getBoxAll();
        Dlog.i("getBoxAll:size():" + boxList.size());

        // list view
        gridView = (GridView) findViewById(R.id.boxListGridView);
        adapter = new BoxListAdapter(this, boxList);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dlog.i("setOnItemClickListener:position:" + position);
                if (!(db.updateState(boxList.get(position).getId(), 0))) {
                    Dlog.i("state  : " + db.updateState(boxList.get(position).getId(),0));
                }
                Intent intent = new Intent(getApplicationContext(), CardListActivity.class);
                startActivityForResult(intent, IntentRequestCode.CARD_LIST_VIEW);
                Dlog.i("box:" + boxList.get(position));
            }
        });
        //long click dialog
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // get user action from dialog
                Dlog.i("Item long click dialog: start");
                View dlg = BoxListActivity.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
                Dlog.i("Item long click dialog - add View");
                AlertDialog.Builder builder = new AlertDialog.Builder(BoxListActivity.this);
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
                        View dlg2 = BoxListActivity.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
                        final EditText inputText = (EditText) dlg2.findViewById(R.id.dialogMenuEditTextOne);
                        TextView textView = (TextView) dlg2.findViewById(R.id.dialogTitleTextView);
                        TextView textView2 = (TextView) dlg2.findViewById(R.id.dialogMenuTextViewOne);
                        Dlog.i("dialog:edit box - add dialog item");
                        inputText.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        textView2.setVisibility(View.VISIBLE);
                        Dlog.i("dialog:edit box - set VISIBLE");
                        inputText.setText(boxList.get(position).getName());
                        inputText.setSelection(inputText.length());
                        AlertDialog.Builder input = new AlertDialog.Builder(BoxListActivity.this);
                        textView.setText(R.string.box_edit_dialog_edit_title);
                        textView2.setText(R.string.box_edit_dialog_edit_message);
                        Dlog.i("dialog:edit box - set text");
                        input.setView(dlg2);
                        Dlog.i("dialog:edit box - add dialog view");
                        input.setPositiveButton(R.string.box_edit_dialog_edit_ok_button_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // set new box name
                                        String newBoxName = inputText.getText().toString();
                                        boxList.get(position).setName(newBoxName);
                                        db.updateBox(boxList.get(position));
                                        adapter.notifyDataSetChanged();
                                        Dlog.i("new box name:" + newBoxName);
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
                        View dlg2 = BoxListActivity.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
                        Dlog.i("dialog:delete box - create View");
                        TextView deleteTitle = (TextView) dlg2.findViewById(R.id.dialogTitleTextView);
                        TextView deleteMessage = (TextView) dlg2.findViewById(R.id.dialogMenuTextViewOne);
                        Dlog.i("dialog:delete box - add dialog item");
                        deleteTitle.setVisibility(View.VISIBLE);
                        deleteMessage.setVisibility(View.VISIBLE);
                        Dlog.i("dialog:delete box - item VISIBLE");
                        final AlertDialog.Builder delete = new AlertDialog.Builder(BoxListActivity.this);
                        deleteTitle.setText(R.string.box_edit_dialog_delete_title);
                        deleteMessage.setText(R.string.box_edit_dialog_delete_message);
                        delete.setView(dlg2);
                        delete.setPositiveButton(R.string.box_edit_dialog_delete_ok_button_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // delete box
                                        int deleteBoxId = boxList.get(position).getId();
                                        Dlog.i("delete box name:" + boxList.get(position).getName());
                                        if (db.deleteBox(deleteBoxId)) {
                                            db.deleteCardByBoxId(deleteBoxId);
                                            boxList.remove(position);
                                            Dlog.i("delete box id:" + deleteBoxId);
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
        if(menu == null){
            Dlog.i("menu is null : " + menu );
            finish();
        }
        Dlog.i("onCreateOptionMenu : " + "OK");
        getMenuInflater().inflate(R.menu.activity_box_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == null && boxList == null){
            Dlog.i("item : " + item + " boxList : " + boxList.toString());
            finish();
        }
        Integer id = item.getItemId();
        switch (id) {
            // add box
            // set an EditText view to get user input
            case R.id.box_list_menu_add:
                Dlog.i("dialog:add box");
                View dlg = BoxListActivity.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
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
                AlertDialog.Builder input = new AlertDialog.Builder(BoxListActivity.this);
                Dlog.i("dialog create");
                input.setView(dlg);
                input.setPositiveButton(R.string.box_edit_dialog_add_ok_button_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // add box
                                String addBoxName = inputText.getText().toString();
                                Dlog.i("add box name:" + addBoxName);
                                BoxDTO box = db.addBox(addBoxName);
                                if (box != null) {
                                    // refresh list
                                    Dlog.i("refresh list:size():before:" + boxList.size());
                                    boxList.add(box);
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
            case R.id.signUp:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, SignUpActivity.class));
                break;
            case R.id.box_list_menu_sort_asc:
                Dlog.i("asc sort start");
                Collections.sort(boxList, new NameAscCompare());
                Dlog.i("asc sort - collections sort call");
                db.updateBoxSeq(boxList);
                adapter.notifyDataSetChanged();
                Dlog.i("asc sort end");
                break;

            case R.id.box_list_menu_sort_desc:
                Dlog.i("desc sort start");
                Collections.sort(boxList, new NameDescCompare());
                Dlog.i("desc sort - collections sort call");
                db.updateBoxSeq(boxList);
                adapter.notifyDataSetChanged();
                Dlog.i("desc sort end");
                break;

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
                Collections.sort(boxList,new NoAscCompare());
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
                Dlog.i("credit");
                Intent intent2 = new Intent(this, CekGetGambar.class);
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

    public class BoxListAdapter extends BaseAdapter {
        private Context context;
        private List<BoxDTO> list;

        public BoxListAdapter() {}

        public BoxListAdapter(Context c, List<BoxDTO> list) {
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
            View view = inflater.inflate(R.layout.activity_box_list_item, null);

            Dlog.i("box position:" + position + ", box:" + list.get(position));
            // image
            ImageView imageView = (ImageView) view.findViewById(R.id.boxListViewItemImage);
            CardDTO topCard = db.getTopCardByBoxId(list.get(position).getId());
            if (topCard != null) {
                Dlog.i("topCard:" + topCard);
                ImageUtil.loadCardImageIntoImageView(BoxListActivity.this, topCard, imageView);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.i("requestCode=" + requestCode + ",resultCode=" + resultCode);
        if(data == null){
            finish();
            return;
        }
        if (resultCode == RESULT_OK) {
            int returnCode = data.getIntExtra(IntentExtrasName.RETURN_CODE, 0);
            Dlog.i("returnCode=");
            switch (returnCode) {
                case IntentReturnCode.BOX_LIST_REFRESH:
                    Dlog.i("Box_List_REFRESH : " + IntentReturnCode.BOX_LIST_REFRESH );
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    Dlog.i("BoxListREFRESH error : " + IntentReturnCode.BOX_LIST_REFRESH );
                    return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


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
            return arg0.getId() < arg1.getId() ? -1 : arg0.getId() > arg1.getId() ? 1:0;
        }

    }
}
