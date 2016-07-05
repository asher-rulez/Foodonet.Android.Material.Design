package upp.foodonet.material;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import UIUtil.RoundedImageView;

public class ProfileViewAndEditActivity extends FooDoNetCustomActivityConnectedToService implements View.OnClickListener, IFooDoNetServerCallback {

    RoundedImageView riv_user_avatar;
    EditText et_user_name;
    EditText et_phone_number;
    Button btn_edit_save_profile;

    boolean isEditModeOn;
    boolean userAvatarEdited;
    String prevName;
    boolean isNameEdited;

    String prevPhone;
    boolean isPhoneEdited;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view_and_edit);

        riv_user_avatar = (RoundedImageView)findViewById(R.id.riv_user_profile_image);
        riv_user_avatar.setOnClickListener(this);

        et_user_name = (EditText)findViewById(R.id.et_profile_user_name);
        et_user_name.setTag(et_user_name.getKeyListener());
        et_user_name.setKeyListener(null);

        et_phone_number = (EditText)findViewById(R.id.et_profile_phone_number);
        et_phone_number.setTag(et_phone_number.getKeyListener());
        et_phone_number.setKeyListener(null);

        btn_edit_save_profile = (Button)findViewById(R.id.btn_update_profile);
        btn_edit_save_profile.setOnClickListener(this);

        LoadProfileData();

        et_user_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if(isEditModeOn)
                    setNameEdited(prevName.compareTo(editable.toString()) != 0);
            }
        });
        et_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if(isEditModeOn)
                    setPhoneEdited(prevPhone.compareTo(editable.toString()) != 0);
            }
        });

        btn_edit_save_profile.setText(getString(R.string.edit_button_text));
        isEditModeOn = false;
        userAvatarEdited = false;

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void LoadProfileData(){
        riv_user_avatar.setImageDrawable(
                CommonUtil.GetBitmapDrawableFromFile(getString(R.string.user_avatar_file_name),
                        getString(R.string.image_folder_path), 90, 90));
        et_user_name.setText(CommonUtil.GetMyUserNameFromPreferences(this));
        et_phone_number.setText(CommonUtil.GetMyPhoneNumberFromPreferences(this));
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.riv_user_profile_image:
                if(!isEditModeOn)
                    return;
                //todo selecting new avatar or making photo
                break;
            case R.id.btn_update_profile:
                if(!isEditModeOn){
                    isEditModeOn = true;
                    prevName = et_user_name.getText().toString();
                    et_user_name.setKeyListener((KeyListener)et_user_name.getTag());
                    prevPhone = et_phone_number.getText().toString();
                    et_phone_number.setKeyListener((KeyListener)et_phone_number.getTag());
                    userAvatarEdited = false;
                    btn_edit_save_profile.setText(R.string.update_button_text);
                    btn_edit_save_profile.setEnabled(false);
                    return;
                }
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_saving_profile));
                //todo: check what was updated, cause image is updated apart from profile on server
                SendUpdatedProfileDetails();
                break;
        }
    }

    public void setNameEdited(boolean nameEdited) {
        isNameEdited = nameEdited;
        EnableSaveButtonIfEditedAndValid();
    }

    public void setPhoneEdited(boolean phoneEdited) {
        isPhoneEdited = phoneEdited;
        EnableSaveButtonIfEditedAndValid();
    }

    private boolean isAnythingEdited(){
        return isNameEdited || isPhoneEdited || userAvatarEdited;
    }

    private void EnableSaveButtonIfEditedAndValid(){
        btn_edit_save_profile.setEnabled(isAnythingEdited() && ValidateNameField() && ValidatePhoneField());
    }

    private void SendUpdatedProfileDetails(){
        InternalRequest irProfile = new InternalRequest(InternalRequest.ACTION_PUT_EDIT_USER);
        irProfile.SocialNetworkType = CommonUtil.GetSocialAccountTypeFromPreferences(this);
        irProfile.SocialNetworkToken = "token1";
        irProfile.SocialNetworkID = CommonUtil.GetSocialAccountIDFromPreferences(this);
        irProfile.PhoneNumber = et_phone_number.getText().toString();
        irProfile.UserName = et_user_name.getText().toString();
        irProfile.Email = CommonUtil.GetMyEmailFromPreferences(this);
        irProfile.DeviceUUID = CommonUtil.GetIMEI(this);
        irProfile.ServerSubPath = getString(R.string.server_post_register_user) + "/" + String.valueOf(CommonUtil.GetMyUserID(this));
        HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
        connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, irProfile);
    }


    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.Status){
            case InternalRequest.STATUS_OK:
                //todo: if image also was updated  - check if updating completed
                prevName = et_user_name.getText().toString();
                CommonUtil.SaveMyUserNameToPreferences(this, prevName);
                isNameEdited = false;
                et_user_name.setTag(et_user_name.getKeyListener());
                et_user_name.setKeyListener(null);
                CommonUtil.RemoveValidationFromEditText(this, et_user_name);

                prevPhone = et_phone_number.getText().toString();
                CommonUtil.SaveMyPhoneNumberToPreferences(this, prevPhone);
                isPhoneEdited = false;
                et_phone_number.setTag(et_phone_number.getKeyListener());
                et_phone_number.setKeyListener(null);
                CommonUtil.RemoveValidationFromEditText(this, et_phone_number);

                btn_edit_save_profile.setText(getString(R.string.edit_button_text));
                btn_edit_save_profile.setEnabled(true);

                //save image
                isEditModeOn = false;
                break;
            case InternalRequest.STATUS_FAIL:
                Toast.makeText(this, "failed to update profile", Toast.LENGTH_SHORT).show();
                break;
        }
        if(progressDialog != null)
            progressDialog.dismiss();
    }

    //todo add callback from avatar update, checks if profile was updated and finished and returns screen to readonly state

    private boolean ValidatePhoneField() {
        if (et_phone_number.getText().length() == 0) {
            CommonUtil.SetEditTextIsValid(this, et_phone_number, false);
            //Toast.makeText(this, getString(R.string.validation_phone_number_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        if (!CommonUtil.CheckPhoneNumberString(this, et_phone_number.getText().toString())) {
            CommonUtil.SetEditTextIsValid(this, et_phone_number, false);
            //Toast.makeText(this, getString(R.string.validation_phone_number_invalid), Toast.LENGTH_LONG).show();
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, et_phone_number, true);
        return true;
    }

    private boolean ValidateNameField(){
        if(et_user_name.getText().length() == 0){
            CommonUtil.SetEditTextIsValid(this, et_user_name, false);
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, et_user_name, true);
        return true;
    }
}
