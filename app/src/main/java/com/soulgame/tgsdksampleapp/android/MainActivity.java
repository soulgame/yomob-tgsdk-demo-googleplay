package com.soulgame.tgsdksampleapp.android;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.soulgame.sgsdk.tgsdklib.TGSDK;
import com.soulgame.sgsdk.tgsdklib.TGSDKServiceResultCallBack;
import com.soulgame.sgsdk.tgsdklib.ad.ITGADListener;
import com.soulgame.sgsdk.tgsdklib.ad.ITGBannerADListener;
import com.soulgame.sgsdk.tgsdklib.ad.ITGPreloadListener;
import com.soulgame.sgsdk.tgsdklib.ad.ITGRewardVideoADListener;
import com.soulgame.sgsdk.tgsdklib.ad.TGBannerType;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    static private String[] adSceneNames = null;
    private ArrayAdapter<String> adapter = null;
    private ProgressDialog loadingDlg = null;
    static private boolean hasInited = false;
    static private boolean hasPreloaded = false;
    private boolean couldReward = false;
    static private final String TAG = "TGSDKSampleApp";
    private Map<String, String> sceneNameMap = null;

    private void showResult(final String result) {
        android.util.Log.d(TAG, "[log] : " + result);
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (hasInited && hasPreloaded && null != loadingDlg) {
                    loadingDlg.cancel();
                    loadingDlg = null;
                }
                (new Builder(MainActivity.this))
                        .setMessage(result)
                        .setTitle(R.string.app_name)
                        .setPositiveButton(R.string.ok_label, null)
                        .show();
            }
        });
    }

    private void showResult(int sid) {
        showResult(getString(sid));
    }

    private ITGBannerADListener bannerADListener = new ITGBannerADListener() {

        @Override
        public void onBannerLoaded(String scene, String result) {
            showResult("onBannerLoaded("+scene+", " + result + " )");
        }

        @Override
        public void onBannerFailed(String scene, String result, String error) {
            showResult("onBannerFailed("+scene+", " + result + ", " + error + " )");
        }

        @Override
        public void onBannerClick(String scene, String result) {
            showResult("onBannerClick("+scene+", " + result + " )");
        }

        @Override
        public void onBannerClose(String scene, String result) {
            showResult("onBannerClose("+scene+", " + result + " )");
        }
    };

    final private ITGRewardVideoADListener rewardAdListener = new ITGRewardVideoADListener() {

        @Override
        public void onADAwardFailed(String arg0, String arg1) {
            android.util.Log.d(TAG, getResources().getString(R.string.reward_fail));
        }

        @Override
        public void onADAwardSuccess(String arg0) {
            couldReward = true;
            android.util.Log.d(TAG, getResources().getString(R.string.reward_success));
        }

    };

    final private ITGADListener listener = new ITGADListener() {

        @Override
        public void onADClick(String arg0) {
            showResult(R.string.ad_click);
        }

        @Override
        public void onADClose(String arg0) {
            showResult(R.string.ad_close);
            if (couldReward) {
                showResult(getString(R.string.could_to_get_reward));
            } else {
                showResult(getString(R.string.not_to_get_reward));
            }
        }

        @Override
        public void onADComplete(String arg0) {
            showResult(R.string.ad_completed);
        }

        @Override
        public void onShowFailed(String arg0, String arg1) {
            showResult(R.string.ad_show_fail);
        }

        @Override
        public void onShowSuccess(String arg0) {
            // showResult(R.string.ad_show_success);
        }

    };

    final private ITGPreloadListener preloadListener = new ITGPreloadListener() {

        @Override
        public void onPreloadFailed(String arg0, String arg1) {
            hasPreloaded = true;
            showResult(R.string.preload_fail);
        }

        @Override
        public void onPreloadSuccess(String arg0) {
            hasPreloaded = true;
            showResult(R.string.preload_success);
            if (arg0.length() > 0) {
                Spinner sceneSpinner = (Spinner) MainActivity.this.findViewById(R.id.spinnerAdScene);
                if (null != adapter) {
                    adapter.clear();
                    sceneSpinner.setAdapter(adapter);
                }
                String[] adScenes = arg0.split(",");
                int adScenesLength = adScenes.length;
                if (null == sceneNameMap) {
                    sceneNameMap = new HashMap<>(adScenesLength);
                }
                String sid;
                String sName;
                adSceneNames = new String[adScenesLength];
                for (int i = 0; i < adScenesLength; i++) {
                    sid = adScenes[i];
                    sName = TGSDK.getSceneNameById(sid) + "("+sid.substring(0, 4)+")";
                    adSceneNames[i] = sName;
                    sceneNameMap.put(sName, sid);
                }
                adapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        adSceneNames);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sceneSpinner.setAdapter(adapter);
                sceneSpinner.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCPADLoaded(String arg0) {
            // showResult(R.string.cp_ad_loaded);
        }

        @Override
        public void onVideoADLoaded(String arg0) {
            // showResult(R.string.video_ad_loaded);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasInited) {
            loadingDlg = ProgressDialog.show(
                    this,
                    getString(R.string.app_name),
                    getString(R.string.sdk_loading),
                    true);

            TGSDK.setDebugModel(true);
            TGSDK.initialize(this, new TGSDKServiceResultCallBack() {

                @Override
                public void onFailure(Object arg0, String arg1) {
                    hasInited = true;
                    showResult(R.string.sdk_init_fail);
                }

                @Override
                public void onSuccess(Object arg0, Map<String, String> arg1) {
                    hasInited = true;
                    showResult(R.string.sdk_init_success);
                }
            });

            android.util.Log.d("TGSDK", "getUserGDPRConsentStatus = "+TGSDK.getUserGDPRConsentStatus());
            TGSDK.setUserGDPRConsentStatus("yes");
            android.util.Log.d("TGSDK", "getIsAgeRestrictedUser = " + TGSDK.getIsAgeRestrictedUser());
            TGSDK.setIsAgeRestrictedUser("no");

            TGSDK.preloadAd(this, preloadListener);
            TGSDK.setADListener(listener);
            TGSDK.setRewardVideoADListener(rewardAdListener);
            TGSDK.setBannerADListener(bannerADListener);

            int y = 800;
            WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Point point = new Point();
                manager.getDefaultDisplay().getSize(point);
                y = point.y - 200;
            }
            TGSDK.setBannerConfig("banner0", TGBannerType.TGBannerNormal,
                    0, y, -1, 200, 30);
            y -= 220;
            TGSDK.setBannerConfig("banner1", TGBannerType.TGBannerNormal,
                    0, y, -1, 200, 30);
            y -= 220;
            TGSDK.setBannerConfig("banner2", TGBannerType.TGBannerNormal,
                    0, y, -1, 200, 30);

        } else if (null != loadingDlg) {
            loadingDlg.cancel();
            loadingDlg = null;
        }

        if (null != adSceneNames && adSceneNames.length > 0) {
            Spinner sceneSpinner = (Spinner) findViewById(R.id.spinnerAdScene);
            if (null != adapter) {
                adapter.clear();
                sceneSpinner.setAdapter(adapter);
            }
            adapter = new ArrayAdapter<>(
                    MainActivity.this,
                    android.R.layout.simple_spinner_item,
                    adSceneNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sceneSpinner.setAdapter(adapter);
            sceneSpinner.setVisibility(View.VISIBLE);
        }

        Button btnShowAd = (Button) findViewById(R.id.btnShowAd);
        btnShowAd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Spinner sceneSpinner = (Spinner) findViewById(R.id.spinnerAdScene);
                if (null == sceneSpinner.getSelectedItem()) {
                    showResult(R.string.please_choose_scene);
                    return;
                }
                final String scenekey = sceneSpinner.getSelectedItem().toString();
                final String sceneid = sceneNameMap.get(scenekey);
                couldReward = false;
                if (TGSDK.couldShowAd(sceneid)) {
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            (new Builder(MainActivity.this))
                                    .setMessage(R.string.check_play_ad)
                                    .setTitle(R.string.ad_play)
                                    .setPositiveButton(R.string.yes_label, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                            TGSDK.showAd(MainActivity.this, sceneid);
                                        }
                                    })
                                    .setNegativeButton(R.string.no_label, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                            // TODO Auto-generated method stub
                                            TGSDK.reportAdRejected(sceneid);
                                        }
                                    })
                                    .show();
                        }
                    });

                } else {
                    showResult(R.string.ad_not_ready);
                }
            }
        });
        Button btnShowTestView = (Button) findViewById(R.id.btnShowTestView);
        btnShowTestView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner sceneSpinner = (Spinner) findViewById(R.id.spinnerAdScene);
                if (null == sceneSpinner.getSelectedItem()) {
                    showResult(R.string.please_choose_scene);
                    return;
                }
                final String scenekey = sceneSpinner.getSelectedItem().toString();
                final String sceneid = sceneNameMap.get(scenekey);
                couldReward = false;
                TGSDK.showTestView(MainActivity.this, sceneid);
            }
        });
        findViewById(R.id.btnCloseBanner).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner sceneSpinner = (Spinner) findViewById(R.id.spinnerAdScene);
                if (null == sceneSpinner.getSelectedItem()) {
                    showResult("Choose a scene please.");
                    return;
                }
                final String scenekey = sceneSpinner.getSelectedItem().toString();
                final String sceneid = sceneNameMap.get(scenekey);
                TGSDK.closeBanner(MainActivity.this, sceneid);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        TGSDK.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TGSDK.onStop(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TGSDK.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TGSDK.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TGSDK.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        TGSDK.onActivityResult(this, reqCode, resCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        TGSDK.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
