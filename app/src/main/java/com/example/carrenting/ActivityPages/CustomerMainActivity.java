package com.example.carrenting.ActivityPages;

import static com.example.carrenting.Service.Map.Constants.ERROR_DIALOG_REQUEST;
import static com.example.carrenting.Service.Map.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.carrenting.Service.Map.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static java.lang.Integer.parseInt;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.carrenting.Adapter.ChatroomRecyclerAdapter;
import com.example.carrenting.FragmentPages.Customer.CustomerActivityFragment;
import com.example.carrenting.FragmentPages.Customer.CustomerHomeFragment;
import com.example.carrenting.FragmentPages.Customer.CustomerMapFragment;
import com.example.carrenting.FragmentPages.Customer.UserInfor.MyProfileFragment;
import com.example.carrenting.Model.Chatroom;
import com.example.carrenting.Model.User;
import com.example.carrenting.Model.UserClient;
import com.example.carrenting.Model.UserLocation;
import com.example.carrenting.R;
import com.example.carrenting.Service.Map.ChatroomActivity;
import com.example.carrenting.Service.Map.LocationService;
import com.example.carrenting.Service.Map.MapMainActivity;
import com.example.carrenting.Service.Map.UserListFragment;
import com.example.carrenting.Service.UserAuthentication.LoginActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;


public class CustomerMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int AVATAR_REQUEST_CODE = 99;
    public static final int FRONT_CICARD_REQUEST_CODE = 100;
    public static final int BEHIND_CICARD_REQUEST_CODE = 101;
    //Side Navigation
    private static final int FRAGMENT_HOME = 0;
    private static final int FRAGMENT_ACTIVITY = 1;
    private static final int FRAGMENT_MESSAGE = 2;
    private static final int FRAGMENT_MAP = 3;

    private static final int FRAGMENT_MY_PROFILE = 4;
    private static final int FRAGMENT_OWNER_STATE = 5;


    private int mCurrentFragment = FRAGMENT_HOME;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private ImageView imgAvatar;
    private TextView tvName, tvEmail;
    final private MyProfileFragment myProfileFragment = new MyProfileFragment();

    private BottomNavigationView mbottomNavigationView;
    private FirebaseFirestore mDb;
    User mUser;
    private CustomerMainActivity mMainActivity;
    //Chat
    private ProgressBar mProgressBar;
    private ArrayList<Chatroom> mChatrooms = new ArrayList<>();
    private Set<String> mChatroomIds = new HashSet<>();
    private ListenerRegistration mChatroomEventListener;
    private CustomerMessageFragment myCustomerMessageFragment = new CustomerMessageFragment();
    private ChatroomRecyclerAdapter mChatroomRecyclerAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted = false;
    private RecyclerView mChatroomRecyclerView;
    //Map
    private UserLocation mUserLocation;
    private ListenerRegistration mUserListEventListener;
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mMainActivity = this;

        // Bottom Navigation

        mbottomNavigationView = findViewById(R.id.bottomNavigationView);
        mbottomNavigationView.setBackground(null);
        mbottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.home:
                    replaceFragment(new CustomerHomeFragment());
                    mNavigationView.setCheckedItem(R.id.nav_home);
                    mCurrentFragment = FRAGMENT_HOME;

                    break;
                case R.id.activity:
                    replaceFragment(new CustomerActivityFragment());
                    mNavigationView.setCheckedItem(R.id.nav_activity);
                    mCurrentFragment = FRAGMENT_ACTIVITY;
                    break;
/*                case R.id.user:
                    replaceFragment(new UserFragment());
                    break;*/
                case R.id.map:
                    inflateMapListFragment();
                    mNavigationView.setCheckedItem(R.id.nav_map);
                    mCurrentFragment = FRAGMENT_MAP;
                    break;
            }
            setTitleToolbar();
            return true;
        }
        );
        //Side Navigation
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNavigationView = findViewById(R.id.navigation_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar,R.string.open_drawer, R.string.close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);

        //
        replaceFragment(new CustomerHomeFragment());
        mNavigationView.setCheckedItem(R.id.nav_home);
        mbottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);
        setTitleToolbar();

        initUI();
        showUserInformation();
        //Map
        getAllUser();
        getAllUserLocation();
        //Chat
//        initSupportActionBar();
//        initChatroomRecyclerView();
    }


    // Tiến hành từng trang

    private void openHomeFragment() {
        if (mCurrentFragment != FRAGMENT_HOME)
        {
            replaceFragment(new CustomerHomeFragment());
            mCurrentFragment = FRAGMENT_HOME;
        }
    }
    private void openActivityFragment() {
        if (mCurrentFragment != FRAGMENT_ACTIVITY)
        {
            replaceFragment(new CustomerActivityFragment());
            mCurrentFragment = FRAGMENT_ACTIVITY;
        }
    }
    private void openMapFragment() {
        if (mCurrentFragment != FRAGMENT_MAP)
        {
            inflateMapListFragment();
            mCurrentFragment = FRAGMENT_MAP;
        }
    }
    private void openMessageFragment() {
        if (mCurrentFragment != FRAGMENT_MESSAGE)
        {
            Intent i = new Intent(CustomerMainActivity.this, ChatMainActivity.class);
            startActivity(i);
            ((Activity) CustomerMainActivity.this).overridePendingTransition(0, 0);
        }
    }
    private void openMyProfileFragment() {
        if (mCurrentFragment != FRAGMENT_MY_PROFILE)
        {
            replaceFragment(myProfileFragment);
            mCurrentFragment = FRAGMENT_MY_PROFILE;
        }
    }
    private void openOwnerStateFragment() {
        if (mCurrentFragment != FRAGMENT_OWNER_STATE)
        {
            Intent i = new Intent(CustomerMainActivity.this, OwnerMainActivity.class);
            startActivity(i);
            ((Activity) CustomerMainActivity.this).overridePendingTransition(0, 0);
        }
    }
    // Hàm thay trang

    private void replaceFragment(Fragment fragment)
    {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_customer, fragment);
        fragmentTransaction.commit();
    }

    //Nếu từng phần tử side navigation được chọn

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.nav_home)
        {
            openHomeFragment();
            mbottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);
        }
        else if (id == R.id.nav_activity)
        {
            openActivityFragment();
            mbottomNavigationView.getMenu().findItem(R.id.activity).setChecked(true);
        }
        else if (id == R.id.nav_map)
        {
            openMapFragment();
            mbottomNavigationView.getMenu().findItem(R.id.map).setChecked(true);
        }
        else if (id == R.id.nav_message)
        {
            openMessageFragment();
        }
        else if (id == R.id.nav_infor)
        {
            openMyProfileFragment();
            mbottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);
        }
        else if (id == R.id.nav_sign_owner)
        {
            openOwnerStateFragment();
            mbottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);
        }
        else if (id == R.id.nav_security_setup)
        {

        }
        else if (id == R.id.nav_notification_setup)
        {

        }
        else if (id == R.id.nav_delete_account)
        {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Bạn thực sự muốn xóa tài khoản ?");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Có",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                user.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(CustomerMainActivity.this, "Tài khoản đã bị xóa.", Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(CustomerMainActivity.this, LoginActivity.class);
                                                    startActivity(intent);
                                                    ((Activity) CustomerMainActivity.this).overridePendingTransition(0, 0);
                                                } else {
                                                    Toast.makeText(CustomerMainActivity.this, "Xóa tài khoản không thành công", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });

            builder1.setNegativeButton(
                    "Không",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
        else if (id == R.id.nav_sign_out)
        {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Bạn muốn đăng xuất tài khoản ?");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Có",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(CustomerMainActivity.this, StartAppActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

            builder1.setNegativeButton(
                    "Không",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();

        }

        setTitleToolbar();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    // Đặt tên toolbar

    private void setTitleToolbar() {
        String title = "";
        switch (mCurrentFragment) {
            case FRAGMENT_HOME:
                title = getString(R.string.nav_home);
                break;
            case FRAGMENT_ACTIVITY:
                title = getString(R.string.nav_activity);
                break;
            case FRAGMENT_MESSAGE:
                title = getString(R.string.nav_message);
                break;
            case FRAGMENT_MAP:
                title = getString(R.string.nav_map);
                break;
            case FRAGMENT_MY_PROFILE:
                title = getString(R.string.nav_infor);
                break;

        }
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle(title);
        }
    }

    private void initUI() {
        imgAvatar = mNavigationView.getHeaderView(0).findViewById(R.id.img_avatar);
        tvName = mNavigationView.getHeaderView(0).findViewById(R.id.tv_name_header);
        tvEmail = mNavigationView.getHeaderView(0).findViewById(R.id.tv_email_header);
    }
    public void showUserInformation() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        mDb.setFirestoreSettings(settings);
        DocumentReference newUserRef = mDb
                .collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid());
        newUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mUser = documentSnapshot.toObject(User.class);
            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (mUser == null)
                {
                    return;
                }
                String name = mUser.getUsername();
                String email = mUser.getEmail();
                if (mUser.getAvatarURL() != null)
                {
                    Uri photoUrl = Uri.parse(mUser.getAvatarURL());
                    Glide.with(mMainActivity).load(photoUrl).error(R.drawable.ic_avatar_default).into(imgAvatar);
                }

                if (name == null)
                {
                    tvName.setVisibility(View.GONE);
                }
                else
                {
                    tvName.setVisibility(View.VISIBLE);
                    tvName.setText(name);
                }
                tvEmail.setText(email);

            }
        });

    }
    public void onClickRequestPermission(int CodeRequest)
    {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            this.openGallery();
            return;
        }
        if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            this.openGallery();
        }
        else
        {
            String [] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            this.requestPermissions(permission, CodeRequest);
        }
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    final private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK)
                    {
                        Intent intent = result.getData();
                        if (intent == null)
                        {
                            return;
                        }
                        Uri uri = intent.getData();
                        if (myProfileFragment.getFlag_image() == 0)
                        {
                            myProfileFragment.setAvatarUri(uri);
                            Glide.with(CustomerMainActivity.this).load(uri).error(R.drawable.ic_avatar_default).into(imgAvatar);
                            Glide.with(CustomerMainActivity.this).load(uri).into(myProfileFragment.getImgAvatar());
                        }
                        else if (myProfileFragment.getFlag_image() == 1)
                        {
                            myProfileFragment.setFrontUri(uri);
                            Glide.with(CustomerMainActivity.this).load(uri).into(myProfileFragment.getIvFrontCiCard());
                        }
                        else if (myProfileFragment.getFlag_image() == 2)
                        {
                            myProfileFragment.setBehindUri(uri);
                            Glide.with(CustomerMainActivity.this).load(uri).into(myProfileFragment.getIvBehindCiCard());
                        }
                    }
                }
            }
    );

    public String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    // MAP
    public void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            myCustomerMessageFragment.getChatrooms();
            myCustomerMessageFragment.getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    public void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                CustomerMainActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(CustomerMainActivity.this);


        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    myCustomerMessageFragment.getChatrooms();
                    myCustomerMessageFragment.getUserDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }
    private void buildAlertMessageNoGps() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }


    public boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }


    private void getAllUser()
    {
        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_users));

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){

                            // Clear the list and add all the users again
                            mUserList.clear();
                            mUserList = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                //Toast.makeText(getActivity(),i,Toast.LENGTH_SHORT);
                                User user = doc.toObject(User.class);
                                mUserList.add(user);
                            }

                            Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                        }
                    }
                });
    }

    private void getAllUserLocation(){
        CollectionReference usersRef = mDb
                .collection(getString(R.string.collection_user_locations));

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){

                            // Clear the list and add all the users again
                            mUserLocations.clear();
                            mUserLocations = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                //Toast.makeText(getActivity(),i,Toast.LENGTH_SHORT);
                                UserLocation userLocation = doc.toObject(UserLocation.class);
                                mUserLocations.add(userLocation);
                            }
                            Log.d(TAG, "onEvent: user location list size: " + mUserLocations.size());
                        }
                    }
                });
    }

    private void inflateMapListFragment(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        CustomerMapFragment fragment = new CustomerMapFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_all_user), mUserList);
        bundle.putParcelableArrayList(getString(R.string.intent_all_user_locations), mUserLocations);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.frame_layout_customer, fragment);
        transaction.commit();

    }

}