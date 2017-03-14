package com.archsorceress.cardkeeper;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.archsorceress.cardkeeper.model.CardItem;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

import java.util.UUID;

import io.realm.Realm;



public class AddNewCardDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_URL = "image url";
    private EditText cardTitle,cardDetail;
    private ColorSeekBar colorSeekBarBackground,colorSeekBarText;
    String imageUrl;

    public AddNewCardDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Create new instance of fragment
     * @param imageUrl
     * @return A new instance of fragment AddNewCardFragment.
     */
    public static AddNewCardDialogFragment newInstance(String imageUrl) {
        AddNewCardDialogFragment fragment = new AddNewCardDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainview = inflater.inflate(R.layout.fragment_add_new_card, container, false);
        cardTitle = (EditText) mainview
                .findViewById(R.id.editText_cardtitle);
        cardDetail = (EditText) mainview
                .findViewById(R.id.editText_carddetail);
        colorSeekBarBackground = (ColorSeekBar) mainview.findViewById(R.id.colorSlider_background);
        colorSeekBarText = (ColorSeekBar) mainview.findViewById(R.id.colorSlider_text);
        Button okButton = (Button) mainview.findViewById(R.id.button_ok);
        Button cancelButton = (Button) mainview.findViewById(R.id.button_cancel);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
                AddNewCardDialogFragment.this.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewCardDialogFragment.this.dismiss();
            }
        });
        return mainview;
    }

    private void addItem() {
        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String key = UUID.randomUUID().toString();
                String title = cardTitle.getText().toString();
                String detail = cardDetail.getText().toString();
                CardItem cardItem = realm.createObject(CardItem.class,key).create(key,title,detail,imageUrl,colorSeekBarBackground.getColor(),colorSeekBarText.getColor());
                realm.insertOrUpdate(cardItem);
            }
        });
    }
}
