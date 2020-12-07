package com.tencent.bugly.hotfix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.hotfix.info.BaseBuildInfo;
import com.tencent.bugly.hotfix.info.BuildInfo;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 如果想更新so，可以将System.loadLibrary替换成Beta.loadLibrary
     */
    static {
        Beta.loadLibrary("native-lib");
    }

    private TextView tvCurrentVersion;
    private Button btnShowToast;
    private Button btnLoadPatch;
    private Button btnKillSelf;
    private Button btnLoadLibrary;
    private Button btnDownloadPatch;
    private Button btnUserPatch;
    private Button btnCheckUpgrade;
    private Button showTinkerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrentVersion = (TextView) findViewById(R.id.tvCurrentVersion);
        btnShowToast = (Button) findViewById(R.id.btnShowToast);
        btnShowToast.setOnClickListener(this);
        btnKillSelf = (Button) findViewById(R.id.btnKillSelf);
        btnKillSelf.setOnClickListener(this);
        btnLoadPatch = (Button) findViewById(R.id.btnLoadPatch);
        btnLoadPatch.setOnClickListener(this);
        btnLoadLibrary = (Button) findViewById(R.id.btnLoadLibrary);
        btnLoadLibrary.setOnClickListener(this);
        btnDownloadPatch = (Button) findViewById(R.id.btnDownloadPatch);
        btnDownloadPatch.setOnClickListener(this);
        btnUserPatch = (Button) findViewById(R.id.btnPatchDownloaded);
        btnUserPatch.setOnClickListener(this);
        btnCheckUpgrade = (Button) findViewById(R.id.btnCheckUpgrade);
        btnCheckUpgrade.setOnClickListener(this);
        showTinkerInfo = (Button) findViewById(R.id.showTinkerInfo);
        showTinkerInfo.setOnClickListener(this);

        tvCurrentVersion.setText("当前版本：" + getCurrentVersion(this));
    }


    /**
     * 根据应用patch包前后来测试是否应用patch包成功.
     * <p>
     * 应用patch包前，提示"This is a bug class"
     * 应用patch包之后，提示"The bug has fixed"
     */
    public void testToast() {
        Toast.makeText(this, LoadBugClass.getBugString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShowToast:  // 测试热更新功能
                testToast();
                break;
            case R.id.btnKillSelf: // 杀死进程
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
            case R.id.btnLoadPatch: // 本地加载补丁测试
                Beta.applyTinkerPatch(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/patch_signed_7zip.apk");
                break;
            case R.id.btnLoadLibrary: // 本地加载so库测试
                NativeCrashJni.getInstance().createNativeCrash();
                break;
            case R.id.btnDownloadPatch:
                Beta.downloadPatch();
                break;
            case R.id.btnPatchDownloaded:
                Beta.applyDownloadedPatch();
                break;
            case R.id.btnCheckUpgrade:
                Beta.checkUpgrade();
                break;
            case R.id.showTinkerInfo:
                showInfo(this);
                break;
        }
    }

    private void showInfo(Context context) {
        // add more Build Info
        final StringBuilder sb = new StringBuilder();
        Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            sb.append(String.format("[patch is loaded] \n"));
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildInfo.TINKER_ID));
            sb.append(String.format("[buildConfig BASE_TINKER_ID] %s \n", BaseBuildInfo.BASE_TINKER_ID));

            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName(ShareConstants.TINKER_ID)));
            sb.append(String.format("[packageConfig patchMessage] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName("patchMessage")));
            sb.append(String.format("[TINKER_ID Rom Space] %d k \n", tinker.getTinkerRomSpace()));

        } else {
            sb.append(String.format("[patch is not loaded] \n"));
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildInfo.TINKER_ID));
            sb.append(String.format("[buildConfig BASE_TINKER_ID] %s \n", BaseBuildInfo.BASE_TINKER_ID));

            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", ShareTinkerInternals.getManifestTinkerID(getApplicationContext())));
        }
        sb.append(String.format("[BaseBuildInfo Message] %s ", BaseBuildInfo.TEST_MESSAGE));

        final TextView v = new TextView(context);
        v.setText(sb);
        v.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.setTextColor(Color.BLACK);
        v.setTypeface(Typeface.MONOSPACE);
        final int padding = 16;
        v.setPadding(padding, padding, padding, padding);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(v);
        final AlertDialog alert = builder.create();
        alert.show();
    }


    /**
     * 获取当前版本.
     *
     * @param context 上下文对象
     * @return 返回当前版本
     */
    public String getCurrentVersion(Context context) {
        try {
            PackageInfo packageInfo =
                    context.getPackageManager().getPackageInfo(this.getPackageName(),
                            PackageManager.GET_CONFIGURATIONS);
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName;

            return versionName + "." + versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("MainActivity", "onBackPressed");

        Beta.unInit();
    }
}
