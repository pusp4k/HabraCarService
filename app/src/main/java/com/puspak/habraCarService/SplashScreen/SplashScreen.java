package com.puspak.habraCarService.SplashScreen;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.puspak.habraCarService.MainActivity;
import com.puspak.habraCarService.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiresApi(api = Build.VERSION_CODES.O)
public class SplashScreen extends AppCompatActivity {

    private String TAG = SplashScreen.class.getSimpleName();
    private static final int REQUEST_PERMISSION = 1234;
    private static final int APP_UPDATE_REQUEST_CODE = 200;

    private static String[] appPermission = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Check Runtime Permission
        checkRunTimePermission();

        // get dynamic link
        //getDynamicLink();

        // check in app update
        checkInAppUpdate();

    }

    private void checkRunTimePermission() {
        if (requestPermission()) {
            goToHome();
        }
    }

    private boolean requestPermission() {
        List<String> reqPermissions = new ArrayList<>();

        // -----------------------------------------------
        // Run Time Permission Check
        // -----------------------------------------------
        for (String perm : appPermission) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                reqPermissions.add(perm);
            }
        }

        // -----------------------------------------------
        //  Prompt user for unauthorized permissions
        // -----------------------------------------------
        if (!reqPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    reqPermissions.toArray(new String[0]), REQUEST_PERMISSION);

            return false;
        }
        // All Permission Granted
        return true;
    }

    // Get Referral Code
    private void getDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    // Get deep link from result (may be null if no link is found)
                    Uri deepLink = null;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                    }

                    if (deepLink != null) {
                        Log.d(TAG, "Deep Link: " + deepLink.toString());
                        String referralId = deepLink.getQueryParameter("invitedby");

                        if(referralId != null) {
                            Log.d(TAG, "getDynamicLink: " + referralId);
                        }

                    } else {
                        Log.d(TAG, "getDynamicLink: no link found");
                    }
                })
                .addOnFailureListener(this, e -> Log.w(TAG, "getDynamicLink:onFailure", e));
    }

    private void checkInAppUpdate() {
        // Creates instance of the manager.
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        // Checks that the platform will allow the specified type of update.
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                Log.e("Update Required", "OK");

                // Request the update.
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            this,
                            // Include a request code to later monitor this update request.
                            APP_UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("Update Not Required", "OK");
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }


    private void goToHome() {
        startActivity(new Intent(SplashScreen.this, MainActivity.class));
        finish();

    }

    /**
     * @param requestCode  REQUEST_PERMISSION
     * @param permissions  String
     * @param grantResults Integer
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {

            HashMap<String, Integer> permissionResult = new HashMap<>();
            int deniedCount = 0;


            // Gather permission grant result
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResult.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }

            }

            if (deniedCount == 0) {
                goToHome();
            } else {
                for (Map.Entry<String, Integer> entry : permissionResult.entrySet()) {
                    String permName = entry.getKey();

                    // ------------------------------------------------------------------------------------ //
                    // Permission is Denied (This is the first time, When "Never Ask Again" is Not Checked)
                    // So ask Again Explaining User the usage of Permission
                    // ShouldShowRequestPermissionRational will return true
                    // -----------------------------------------------------------------------------//
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        showDialog("", R.string.app_permission_msg, R.string.yes,

                                (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    requestPermission();
                                },
                                R.string.no, (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    finish();
                                }, false);
                    } else {
                        // Permission is denied [Never ask again is checked]
                        // Ask user to go to settings and manually allow permission
                        showDialog("", R.string.permission_denied_msg, R.string.setting_msg,
                                (dialogInterface, i) -> {
                                    dialogInterface.dismiss();

                                    // Go to Settings
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", getPackageName(), null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                },
                                R.string.no, (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    finish();
                                }, false);
                        break;
                    }

                }
            }

        }

    }

    public void showDialog(String title, int msg, int positiveLabel,
                           DialogInterface.OnClickListener positiveClick,
                           int negativeLabel, DialogInterface.OnClickListener negativeClick,
                           boolean isCancelAble) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(isCancelAble);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveClick);
        builder.setNegativeButton(negativeLabel, negativeClick);

        AlertDialog alert = builder.create();
        alert.show();
    }

}