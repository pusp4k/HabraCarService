package com.androweb.voyage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.androweb.voyage.CustomDialog.CustomProgressDialog;
import com.androweb.voyage.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Toolbar toolbar;
    private DrawerLayout drawerlayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingactionbutton;
    private CircleImageView imgUser;
    private TextView txtUserName;
    private TextView txtUserMobile;
    private CustomProgressDialog customDialog;
    private Fragment currentFragment;
    private FragmentManager fragmentManager;
    private ImageView ivCover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        drawerlayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        View navView = navigationView.getHeaderView(0);
        ivCover = navView.findViewById(R.id.ivCover);
        imgUser = navView.findViewById(R.id.ivLogo);
        txtUserName = navView.findViewById(R.id.userName);
        txtUserMobile = navView.findViewById(R.id.userPhone);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        floatingactionbutton = findViewById(R.id.btFab);

        customDialog = new CustomProgressDialog(this);

        setupToolbar();

        setupNavigationDrawer();

        setupNavigationHeaderUi();

        fragmentManager = getSupportFragmentManager();

        openHomeFragment();
    }

    private void openHomeFragment() {

    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
    }

    private void setupNavigationDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,drawerlayout,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerlayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(MainActivity.this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {
        drawerlayout =findViewById(R.id.drawerLayout);
        if(drawerlayout.isDrawerOpen(GravityCompat.START)) {
            drawerlayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_menu_wallet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void setupNavigationHeaderUi () {
        Picasso.get()
                .load(R.drawable.app_header_bg)
                .into(ivCover);

        Picasso.get()
                .load(R.drawable.img_app_icon) // TODO CHANGE WITH USER PHOTO
        .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .into(imgUser);

        txtUserName.setText(Utils.getUserName(this));
        txtUserMobile.setText(Utils.getUserMobile(this));
    }
}