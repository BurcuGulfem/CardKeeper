package com.archsorceress.cardkeeper;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.archsorceress.cardkeeper.adapter.CardAdapter;
import com.archsorceress.cardkeeper.model.CardItem;
import com.github.clans.fab.FloatingActionMenu;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private Realm realm;

    private static final short
            RESULT_GALLERY = 0,
            RESULT_CAMERA = 1,
            RESULT_QRCODE= 2,
            RESULT_BARCODE = 3,
            PERMISSION_CAMERA_ACCESS = 100,
            PERMISSION_READ_EXTERNAL_STORAGE= 101;

    private  FloatingActionMenu actionMenu;
    protected CardAdapter cardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionMenu = (FloatingActionMenu) findViewById(R.id.fab); //We need this to be able to close the menu

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();
        cardAdapter = new CardAdapter(getApplicationContext(),realm.where(CardItem.class).findAllAsync(),true);
        RecyclerView cardsListView = (RecyclerView) findViewById(R.id.recyclerview_cardslist);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        cardsListView.setLayoutManager(mLayoutManager);
        cardsListView.setAdapter(cardAdapter);
        setUpItemTouchHelper(cardsListView);
    }

    private void setUpItemTouchHelper(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                final int swipedPosition = viewHolder.getAdapterPosition();
                final CardItem cardItem = cardAdapter.getItem(swipedPosition);
                if(cardItem!=null) {
                    showDeleteItemDialog(cardItem,swipedPosition);
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void showDeleteItemDialog(final CardItem cardItem, final int position){
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_confirm_deletion)
                .setMessage(getString(R.string.dialog_message_deletion))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        removeItemFromDB(cardItem);
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //to redraw the swiped item
                        cardAdapter.notifyItemChanged(position);
                    }
                }).show();
    }

    public void removeItemFromDB(final CardItem cardItem){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                cardItem.deleteFromRealm();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_all_cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_info:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Android M requires to get permission at runtime
     * versions before that get that permission at app install
     * @return (boolean) camera permission
     */
    public boolean getPermission(int requestCode){
        if (android.os.Build.VERSION.SDK_INT >= 21) {
           switch (requestCode){
               case PERMISSION_CAMERA_ACCESS:
                   if (checkSelfPermission(Manifest.permission.CAMERA)
                           != PackageManager.PERMISSION_GRANTED) {
                       requestPermissions(new String[]{Manifest.permission.CAMERA},
                               PERMISSION_CAMERA_ACCESS);
                       return false;
                   }
                   else return true;
               case PERMISSION_READ_EXTERNAL_STORAGE:
                   if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                           != PackageManager.PERMISSION_GRANTED) {
                       requestPermissions(new String[]{Manifest.permission.CAMERA},
                               PERMISSION_READ_EXTERNAL_STORAGE);
                       return false;
                   }
                   else return true;
               default:
                   return false;
           }
        }
        else return true; //if API is lower than 23, permission is already granted
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CAMERA_ACCESS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraActivity();
                } else {
                    Toast.makeText(this,R.string.toast_camera_permission_denied,Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //read images into file.
                } else {
                    Toast.makeText(this,R.string.toast_read_external_storage_permission_denied,Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * These click handlers are called from the FloatingActionButtons in activity_main.xml */
    public void onGalleryButtonClick(View view){
        actionMenu.close(true);
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent , RESULT_GALLERY );
    }

    /**
     * Opens Camera intent if the user gives permission, otherwise fail
     * @param view defined in activity_main     */
    public void onCameraButtonClick(View view){
        actionMenu.close(true);
        if(getPermission(PERMISSION_CAMERA_ACCESS)) {
            startCameraActivity();
        }
    }

    public void onQRCodeButtonClick(View view){
        Toast.makeText(this, R.string.alert_not_implemented, Toast.LENGTH_SHORT).show();
    }

    public void onBarcodeButtonClick(View view){
        Toast.makeText(this, R.string.alert_not_implemented, Toast.LENGTH_SHORT).show();
    }

    public void startCameraActivity(){
        Toast.makeText(this, R.string.alert_not_implemented, Toast.LENGTH_SHORT).show();
//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(cameraIntent, RESULT_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_GALLERY :
                if (null != data) {
                    if (resultCode == Activity.RESULT_OK){
                        showNewCardDialog(data.getData().toString());
                    }
                }
                break;
            case RESULT_CAMERA:
                break;
            default:
                break;
        }
    }

    public void showNewCardDialog(String imageUrl){
        AddNewCardDialogFragment addNewCard = AddNewCardDialogFragment.newInstance(imageUrl);
        addNewCard.show(getSupportFragmentManager(),"add_new_card");
    }

}