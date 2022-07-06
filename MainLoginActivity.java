package com.project.alumni;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment;
import com.coderconsole.cextracter.cmodels.CEmail;
import com.coderconsole.cextracter.cmodels.CPhone;
import com.coderconsole.cextracter.cquery.CQuery;
import com.coderconsole.cextracter.cquery.base.CList;
import com.coderconsole.cextracter.i.ICFilter;
import com.coderconsole.cextracter.i.IContact;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Query;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.irozon.alertview.AlertActionStyle;
import com.irozon.alertview.AlertStyle;
import com.irozon.alertview.AlertView;
import com.irozon.alertview.objects.AlertAction;
import com.nordan.dialog.Animation;
import com.nordan.dialog.DialogType;
import com.nordan.dialog.NordanAlertDialog;
import com.nordan.dialog.NordanLoadingDialog;
import com.onurkaganaldemir.ktoastlib.KToast;
import com.project.alumni.models.ApiConfig;
import com.project.alumni.models.Contacts;
import com.project.alumni.models.ServerResponse;
import com.project.alumni.models.User;
import com.project.alumni.utils.Address;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressWarnings("ALL")
public class MainLoginActivity extends AppCompatActivity {
    private TextView textViewRegistration;
    private EditText editTextUsername,editTextPassword,editTextCode;
    private Button buttonLogin;

    private ProgressBar progressBar;
    private TextView textViewForgot;
    private TextView textViewInvite;

    CallbackManager callbackManager;
    private LoginButton faceBookButton;
    GoogleSignInOptions gso;
    private TextView textViewUseEmail;
    private LinearLayout linearLayout1,linearLayout2;
    private boolean clicked=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);


        editTextUsername=findViewById(R.id.editTextPhone);
        editTextPassword=findViewById(R.id.editTextPass);
        editTextCode=findViewById(R.id.editTextCountryCode);
        textViewRegistration=findViewById(R.id.goToPage);
        textViewForgot=findViewById(R.id.tvForgotPassword);
        textViewInvite=findViewById(R.id.inviteFriends);
        textViewUseEmail=findViewById(R.id.useEmail);
        linearLayout1=findViewById(R.id.linearEmail);
        linearLayout2=findViewById(R.id.linearPhone);


        buttonLogin=findViewById(R.id.buttonContinues);
        faceBookButton=findViewById(R.id.login_button);
        faceBookButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));

        callbackManager = CallbackManager.Factory.create();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        textViewRegistration.setOnClickListener(v -> {startActivity(
                new Intent(MainLoginActivity.this,MainLogActivity.class));
            finish();
        });

        textViewUseEmail.setOnClickListener(v -> {

        startActivity(new Intent(this,MainLoginEmailActivity.class));
        finish();

        });

        textViewForgot.setOnClickListener(v -> {

            BottomForgotPassword bottomForgotPassword=new BottomForgotPassword();

            Bundle bundle=new Bundle();
            bottomForgotPassword.setArguments(bundle);
            bottomForgotPassword.show(getSupportFragmentManager(),"TAG");
        });

    buttonLogin.setOnClickListener(v -> {
        loginUser();
    });


        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn){

            accessToken.setCurrentAccessToken(null);
            //setExpressLoginStatus()
        }


        textViewInvite.setOnClickListener(v -> {

        BottomInviteFriends bottomForgotPassword=new BottomInviteFriends();

        Bundle bundle=new Bundle();
        bottomForgotPassword.setArguments(bundle);
        bottomForgotPassword.show(getSupportFragmentManager(),"TAG");

    });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable @org.jetbrains.annotations.Nullable Intent data) {

            callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loginUser(){

        String username=editTextUsername.getText().toString().trim();
        String password=editTextPassword.getText().toString().trim();
        String ecode=editTextCode.getText().toString().trim();
        Dialog dialog= NordanLoadingDialog.createLoadingDialog(MainLoginActivity.this,"Checking...Please wait.");

     //   String realPass=editTextPass1.getText().toString().trim();


        // Toast.makeText(Login.this, username, Toast.LENGTH_SHORT).show();

        if(TextUtils.isEmpty(username) || username.isEmpty()){
            editTextUsername.setError("Required");
            editTextUsername.requestFocus();

        }
        if(username.length()<9 ||username.length()>9){
            editTextUsername.setError("Enter a valid phone");
            editTextUsername.requestFocus();

        }
        else if(TextUtils.isEmpty(password) || password.isEmpty()){
            editTextPassword.setError("Required");
            editTextPassword.requestFocus();

        }else if(TextUtils.isEmpty(ecode) || ecode.isEmpty()){
            editTextCode.setError("Required");
            editTextCode.requestFocus();

        }else{

            class UserLogin extends AsyncTask<Void,Void,String> {


                @Override
                protected void onPreExecute() {
                    super.onPreExecute();


                    dialog.show();
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);

                    //Toast.makeText(Login.this, s, Toast.LENGTH_SHORT).show();

                    if(s.isEmpty()){

                        new NordanAlertDialog.Builder(MainLoginActivity.this)
                                .setDialogType(DialogType.ERROR)
                                .setAnimation(Animation.SLIDE)
                                .isCancellable(true)
                                .setTitle("Failed!")
                                .setMessage("Please check your internet connection!")
                                .setPositiveBtnText("Ok!")
                                .onPositiveClicked(() -> {/* Do something here */})
                                .build().show();

                        dialog.dismiss();
                    }

                    try {

                        JSONObject jsonObject = new JSONObject(s);
                        // Toast.makeText(Login.this, jsonObject.toString(), Toast.LENGTH_SHORT).show();

                        String messagee = jsonObject.getString("message");
                        // Toast.makeText(Login.this, messagee, Toast.LENGTH_SHORT).show();
                        if (messagee.contains("Connection failed")) {
                            Toast.makeText(MainLoginActivity.this, "Connection failed, Please Check and retry", Toast.LENGTH_SHORT).show();
                        }else if(jsonObject.getString("message").equals("Incorrect username or password")) {


                            new NordanAlertDialog.Builder(MainLoginActivity.this)
                                    .setDialogType(DialogType.ERROR)
                                    .setAnimation(Animation.SLIDE)
                                    .isCancellable(true)
                                    .setTitle("Failed!")
                                    .setMessage("Incorrect phone or password, Try again.")
                                    .setPositiveBtnText("Ok!")
                                    .onPositiveClicked(() -> {/* Do something here */})
                                    .build().show();

                            dialog.dismiss();

                        }else
                        {
                            if (!jsonObject.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                                JSONObject userJson = jsonObject.getJSONObject("user");

                                User user = new User(
                                        userJson.getInt("id"),
                                        userJson.getString("username"),
                                        userJson.getString("email"),
                                        userJson.getString("status"),
                                        userJson.getString("profile"),
                                        userJson.getString("followers"),
                                        userJson.getString("following"),
                                        userJson.getString("searches"),
                                        userJson.getString("location"),
                                        userJson.getString("bio"),
                                        userJson.getString("private"),
                                        userJson.getBoolean("private"),
                                        userJson.getString("business"),
                                        userJson.getString("link")


                                );
                                com.betacode.free.snappystory.model.User users = new com.betacode.free.snappystory.model.User(
                                        userJson.getInt("id"),
                                        userJson.getString("username"),
                                        userJson.getString("email"),
                                        userJson.getString("status"),
                                        userJson.getString("profile"),
                                        userJson.getString("followers"),
                                        userJson.getString("following"),
                                        userJson.getString("searches"),
                                        userJson.getString("location"),
                                        userJson.getString("bio"),
                                        userJson.getString("private"),
                                        userJson.getBoolean("private"),
                                        userJson.getString("business")


                                );

                                SharedPrefManagers.getInstance(getApplicationContext()).userLogin(user);
                                com.betacode.free.snappystory.model.SharedPrefManagers.getInstance(getApplicationContext()).userLogin(users);

                                //Toast.makeText(Login.this, user.getIdentifierID(), Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(MainLoginActivity.this, MainActivity.class));
                                finish();
                            } else {



                            }

                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @SuppressWarnings("deprecation")
                @Override
                protected String doInBackground(Void... voids) {
                    String urlLogin=new Address().getBASE_APP_URL() +"api.php?apicall=mainlogin";

                    RequestHandler requestHandler=new RequestHandler();
                    HashMap<String,String> params=new HashMap<>();
                    params.put("username",username);
                    params.put("password",password);
                    params.put("ecode",ecode);



                    return requestHandler.sendPostRequest(urlLogin,params);
                }
            }
            UserLogin userLogin=new UserLogin();
            userLogin.execute();
        }

    }


    public static class BottomForgotPassword extends SuperBottomSheetFragment {

        private EditText editTextEmail;
        private AppCompatButton appCompatButton;
        private ProgressBar progressBar;

        @Override
        public @Nullable View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            return inflater.inflate(R.layout.forgot_layout, container, false);
        }

        @Override
        public void onViewCreated(@NonNull @NotNull View view, @androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            View rootView=view.getRootView();

            editTextEmail=rootView.findViewById(R.id.editTextPass);
            appCompatButton=rootView.findViewById(R.id.sendButton);
            progressBar=rootView.findViewById(R.id.progressVideos);


            appCompatButton.setOnClickListener(v -> {

                String mail=editTextEmail.getText().toString();

                if(TextUtils.isEmpty(mail)){

                    editTextEmail.requestFocus();
                    editTextEmail.setError("Required");
                }else{

                    progressBar.setVisibility(View.VISIBLE);

                    String ROOT_URL=new Address().getBASE_APP_SUBURL();

                    OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build();

                    Retrofit builder = new Retrofit.Builder()
                            .baseUrl(ROOT_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create()).build();
                    ApiConfig fileUploadService  = builder.create(ApiConfig.class);
                    Call<ServerResponse> call = fileUploadService.sendEmailForgotPassword(mail);
                    call.enqueue(new Callback<ServerResponse>() {
                        @Override
                        public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                            Log.e("main", "the message is ----> " + response.body().getMessage());
                            Log.e("main", "the error is ----> " + response.body().getSuccess());

                            progressBar.setVisibility(View.GONE);



                        }

                        @Override
                        public void onFailure(Call<ServerResponse> call, Throwable t) {
                            Log.e("main", "on error is called and the error is  ----> " + t.getMessage());
                            progressBar.setVisibility(View.GONE);

                        }
                    });

                }
            });

        }

    }

    public static class BottomVerifyPassword extends SuperBottomSheetFragment {

        @Override
        public @Nullable View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            return inflater.inflate(R.layout.forgot_layout_second, container, false);
        }

        @Override
        public void onViewCreated(@NonNull @NotNull View view, @androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            View rootView=view.getRootView();


        }

    }

    public static class BottomInviteFriends extends SuperBottomSheetFragment {

        private RecyclerView recyclerView;
        Context contexts;
        List<Contacts> contactsArrayList=new ArrayList<>();

        @Override
        public @Nullable View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            return inflater.inflate(R.layout.fragment_user, container, false);
        }

        @Override
        public void onViewCreated(@NonNull @NotNull View view, @androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            View rootView=view.getRootView();

            recyclerView=rootView.findViewById(R.id.user_list_recycle_view);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(contexts,LinearLayoutManager.VERTICAL,false));


            Query q = com.github.tamir7.contacts.Contacts.getQuery();
            q.include(Contact.Field.DisplayName, Contact.Field.GivenName, Contact.Field.PhotoUri);
            List<Contact> contacts = q.find();


            InviteContactAdapter inviteContactAdapter=new InviteContactAdapter(contexts,contactsArrayList);
            inviteContactAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(inviteContactAdapter);

        }

        @Override
        public void onAttach(@NonNull @NotNull Context context) {
            super.onAttach(context);
            contexts=context;
        }
    }

    private void loginnUserWithFaceBook(String email,String name){

        Dialog dialog= NordanLoadingDialog.createLoadingDialog(MainLoginActivity.this,"Checking...Please wait.");


        class UserLogin extends AsyncTask<Void,Void,String> {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();


                dialog.show();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                //Toast.makeText(Login.this, s, Toast.LENGTH_SHORT).show();

                if(s.isEmpty()){

                    dialog.dismiss();
                    AlertView alert = new AlertView("Please check your internet connection",
                            "Seems like you are not connected to the internet",
                            AlertStyle.BOTTOM_SHEET);
                    alert.addAction(new AlertAction("Okay", AlertActionStyle.DEFAULT, action -> {


                    }));

                    alert.addAction(new AlertAction("Cancel", AlertActionStyle.NEGATIVE, action -> {


                    }));


                    alert.show(MainLoginActivity.this);
                }

                try {

                    JSONObject jsonObject = new JSONObject(s);
                    // Toast.makeText(Login.this, jsonObject.toString(), Toast.LENGTH_SHORT).show();

                    String messagee = jsonObject.getString("message");
                    // Toast.makeText(Login.this, messagee, Toast.LENGTH_SHORT).show();
                    if (messagee.contains("Connection failed")) {
                        Toast.makeText(MainLoginActivity.this, "Connection failed, Please Check and retry", Toast.LENGTH_SHORT).show();
                    }else if(jsonObject.getString("message").equals("not found")) {


                    }else
                    {
                        if (!jsonObject.getBoolean("error")) {
                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            JSONObject userJson = jsonObject.getJSONObject("user");

                            User user = new User(
                                    userJson.getInt("id"),
                                    userJson.getString("username"),
                                    userJson.getString("email"),
                                    userJson.getString("status"),
                                    userJson.getString("profile"),
                                    userJson.getString("followers"),
                                    userJson.getString("following"),
                                    userJson.getString("searches"),
                                    userJson.getString("location"),
                                    userJson.getString("bio"),
                                    userJson.getString("private"),
                                    userJson.getBoolean("private"),
                                    userJson.getString("business"),
                                    userJson.getString("link")


                            );
                            com.betacode.free.snappystory.model.User users = new com.betacode.free.snappystory.model.User(
                                    userJson.getInt("id"),
                                    userJson.getString("username"),
                                    userJson.getString("email"),
                                    userJson.getString("status"),
                                    userJson.getString("profile"),
                                    userJson.getString("followers"),
                                    userJson.getString("following"),
                                    userJson.getString("searches"),
                                    userJson.getString("location"),
                                    userJson.getString("bio"),
                                    userJson.getString("private"),
                                    userJson.getBoolean("private"),
                                    userJson.getString("business")


                            );

                            SharedPrefManagers.getInstance(getApplicationContext()).userLogin(user);
                            com.betacode.free.snappystory.model.SharedPrefManagers.getInstance(getApplicationContext()).userLogin(users);

                            //Toast.makeText(Login.this, user.getIdentifierID(), Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(MainLoginActivity.this, MainActivity.class));
                            finish();
                        } else {



                        }

                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            protected String doInBackground(Void... voids) {
                String urlLogin=new Address().getBASE_APP_URL() +"api.php?apicall=fblogin";

                RequestHandler requestHandler=new RequestHandler();
                HashMap<String,String> params=new HashMap<>();
                params.put("username",email);
                params.put("name",name);



                return requestHandler.sendPostRequest(urlLogin,params);
            }
        }
        UserLogin userLogin=new UserLogin();
        userLogin.execute();

    }

}