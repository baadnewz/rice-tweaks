package com.ice.box;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.ice.box.helpers.ChangeLog;
import com.ice.box.helpers.TweaksHelper;

import static com.ice.box.helpers.Constants.*;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    protected NavigationView navigationView;
    private int mThemeId = R.style.ThemeLight;
    private TweaksHelper tweaksHelper;
    private boolean isFragmentOpen;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNightly = sharedPref.getBoolean("isNightly", false);
        mThemeId = sharedPref.getInt("THEMEID", mThemeId);
        setTheme(mThemeId);

        tweaksHelper = new TweaksHelper(this);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        Menu menu = navigationView.getMenu();
        switchFragment("Home", getString(R.string.app_name));
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean isNightly = sharedPref.getBoolean("isNightly", false);
        final boolean isFreeVersion = sharedPref.getBoolean(isNightlyKey,false);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
/*              int fromSettings = 0;
                Bundle passedFrom = getIntent().getExtras();
                if (passedFrom != null) {
                    fromSettings = passedFrom.getInt("FromSettings");
                }
                if (fromSettings == 0) { }*/
                if (!isNightly) {
                    alertRomUpdate();
                } else {
                    alertNightlyUpdate();
                }
                //if (isFreeVersion) alertMattPiggy();
            }
        }, 1500);
        ChangeLog cl = new ChangeLog(this);
        if (cl.firstRun())
            cl.getLogDialog().show();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isFragmentOpen) {
            switchFragment("Home", getString(R.string.app_name));
            isFragmentOpen = !isFragmentOpen;
            navigationView.getMenu().getItem(0).setChecked(true);
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Change Header logo according to theme
        ImageView banner = (ImageView) findViewById(R.id.imageView);
        if (mThemeId == R.style.ThemeDark) {
            banner.setBackgroundResource(R.drawable.header_image_dark);
        } else {
            banner.setBackgroundResource(R.drawable.header_image);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_changelog:
                ChangeLog cl = new ChangeLog(this);
                cl.getFullLogDialog().show();
                break;
            case R.id.action_exit:
                this.finish();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, 2000);
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchFragment(final String fragmentClass, final String fragmentTitle) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(fragmentTitle);
                FragmentTransaction fm = getFragmentManager().beginTransaction();
                fm.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                fm.replace(R.id.container,
                        Fragment.instantiate(
                                MainActivity.this,
                                "com.ice.box.fragments." + fragmentClass))
                        .commit();
            }
        }, 0);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_about:
                isFragmentOpen = true;
                switchFragment("Home", getString(R.string.app_name));
                break;
            case R.id.nav_ui:
                isFragmentOpen = true;
                switchFragment("UI", getString(R.string.nav_ui));
                break;
            case R.id.nav_misc:
                isFragmentOpen = true;
                switchFragment("Misc", getString(R.string.nav_misc));
                break;
/*            case R.id.colors:
                isFragmentOpen = true;
                switchFragment("Colors", getString(R.string.nav_colors));
                break;*/
            case R.id.app:
                isFragmentOpen = true;
                switchFragment("App", getString(R.string.nav_app));
                break;
            case R.id.nav_colors_backuprestore:
                isFragmentOpen = true;
                switchFragment("ColorsBackupRestore", getString(R.string.Backup_Restore));
                break;
            case R.id.nav_colors_statusbar:
                isFragmentOpen = true;
                switchFragment("ColorsStatusbar", getString(R.string.statusbar_color_preference_title));
                break;
            case R.id.nav_colors_navbar:
                isFragmentOpen = true;
                switchFragment("ColorsNavBar", getString(R.string.navbar_color_preference_title));
                break;
            case R.id.nav_colors_header:
                isFragmentOpen = true;
                switchFragment("ColorsHeader", getString(R.string.header_color_preference_title));
                break;
            case R.id.nav_colors_qs:
                isFragmentOpen = true;
                switchFragment("ColorsQuickSettings", getString(R.string.quick_settings_preference_title));
                break;
            case R.id.nav_colors_notifcation:
                isFragmentOpen = true;
                switchFragment("ColorsNotification", getString(R.string.notifications_color_preference_title));
                break;
            case R.id.nav_license:
                isFragmentOpen = true;
                switchFragment("License", getString(R.string.settings_license));
                break;
            case R.id.nav_settings:
                isFragmentOpen = true;
                switchFragment("Settings", getString(R.string.action_settings));
                break;
            case R.id.nav_sys_info:
                isFragmentOpen = true;
                switchFragment("SysInfo", getString(R.string.nav_system_info));
                break;
            default:
                isFragmentOpen = false;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void alertRomUpdate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean romupdate = sharedPref.getBoolean("romupdate", false);
        boolean romupdate_start = sharedPref.getBoolean("romupdate_start", false);
        int currentRomVerion = sharedPref.getInt(localStableVersionKey, 1);
        int onlineRomVersion = sharedPref.getInt(onlineStableVersionKey, 1);
        boolean isICE = (sharedPref.getBoolean("isICE", false));
        if (isICE && !romupdate && romupdate_start && currentRomVerion < onlineRomVersion) {
            tweaksHelper.createRomNotification();
        }
    }

    private void alertNightlyUpdate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean nightlyupdate = sharedPref.getBoolean("nightlyupdate", false);
        boolean nightlyupdate_start = sharedPref.getBoolean("nightlyupdate_start", false);
        final int nightliesOnlineCurrentRevision = sharedPref.getInt(onlineNightlyVersionKey, 1);
        final int nightliesOfflineCurrentRevision = sharedPref.getInt(localNightlyVersionKey, 1);
        boolean isICE = (sharedPref.getBoolean("isICE", false));
        if (isICE && !nightlyupdate && nightlyupdate_start && nightliesOfflineCurrentRevision < nightliesOnlineCurrentRevision) {
            tweaksHelper.createNightlyNotification();
        }

    }
    private void alertMattPiggy() {

        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
        alertDialog2.setTitle(this.getResources().getString(R.string.app_name));
        alertDialog2.setMessage(this.getResources().getString(R.string.matt_donation));
        alertDialog2.setNeutralButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(riceWebsiteLink + "donation.html"));
                        startActivity(i);
                        dialog.dismiss();
                        dialog.cancel();
                    }
                });
        alertDialog2.show();

    }
}