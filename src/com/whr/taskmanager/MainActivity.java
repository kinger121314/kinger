      package com.whr.taskmanager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechUtility;
import com.iflytek.speech.TextUnderstander;
import com.iflytek.speech.TextUnderstanderListener;
import com.iflytek.speech.UnderstanderResult;
import com.iflytek.speech.UtilityConfig;
import com.whr.fragment.BaseFragment;
import com.whr.taskmanager.bean.TabInfo;
import com.whr.taskmanager.util.ApkInstaller;
import com.whr.taskmanager.util.PushUtils;
import com.whr.taskmanager.util.XmlParser;

public class MainActivity extends IndicatorFragmentActivity {
	private static final String ACTION_INPUT = "com.iflytek.speech.action.voiceinput";

	/** 外部设置的弹出框完成按钮文字 */
	public static final String TITLE_DONE = "title_done";
	/** 外部设置的弹出框取消按钮文字 */
	public static final String TITLE_CANCEL = "title_cancel";
	private static final int REQUEST_CODE_SEARCH = 1099;
	private static final int INSTALL_SERVER = 0x001;
	private static final int CANCEL_DIALOG = 0x002;
	private Dialog mLoadDialog;
	private TextUnderstander mTextUnderstander;

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case INSTALL_SERVER:
				String url = SpeechUtility.getUtility(MainActivity.this)
						.getComponentUrl();
				String assetsApk = "SpeechService.apk";
				processInstall(MainActivity.this, url, assetsApk);
				break;

			case CANCEL_DIALOG:
				if (mLoadDialog != null) {
					mLoadDialog.dismiss();
				}
				break;
				
			default:
				break;
			}
			return false;
		}
	});
	/**
	 * 初期化监听器（文本到语义）。
	 */
	private InitListener textUnderstanderListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule arg0, int code) {
			if (code == ErrorCode.SUCCESS) {
			}
		}
	};
	private TextUnderstanderListener textListener = new TextUnderstanderListener.Stub() {

		@Override
		public void onResult(final UnderstanderResult result)
				throws RemoteException {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (null != result) {
						String text = XmlParser.parseNluResult(result
								.getResultString());
						Toast.makeText(MainActivity.this, "text:" + text,
								Toast.LENGTH_LONG).show();
					} else {

					}
				}
			});
		}

		@Override
		public void onError(int errorCode) throws RemoteException {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ex) {
		}

		// 设置你申请的应用appid
		SpeechUtility.getUtility(MainActivity.this).setAppid(
				getString(R.string.app_id));
		mTextUnderstander = new TextUnderstander(this, textUnderstanderListener);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.action_add_new:
			intent = new Intent(this, NewTaskActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;

		case R.id.action_voice:
			if (!checkSpeechServiceInstalled()) {
				final Dialog dialog = new Dialog(this, R.style.dialog);

				LayoutInflater inflater = getLayoutInflater();
				View alertDialogView = inflater.inflate(
						R.layout.superman_alertdialog, null);
				dialog.setContentView(alertDialogView);
				Button okButton = (Button) alertDialogView
						.findViewById(R.id.ok);
				Button cancelButton = (Button) alertDialogView
						.findViewById(R.id.cancel);
				TextView comeText = (TextView) alertDialogView
						.findViewById(R.id.title);
				comeText.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
				okButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						Message message = new Message();
						message.what = INSTALL_SERVER;
						mHandler.sendMessage(message);
					}
				});
				cancelButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
				WindowManager.LayoutParams lp = dialog.getWindow()
						.getAttributes();
				lp.width = MainActivity.this.getResources().getDisplayMetrics().widthPixels; // 设置宽度
				dialog.getWindow().setAttributes(lp);
				break;
			}
			if (isActionSupport(this)) {
				intent = new Intent();
				// 指定action名字
				intent.setAction(ACTION_INPUT);
				intent.putExtra(SpeechConstant.PARAMS, "asr_ptt=0");
				intent.putExtra(SpeechConstant.VAD_EOS, "1000");
				// 设置弹出框的两个按钮名称
				intent.putExtra(TITLE_DONE, "确定");
				intent.putExtra(TITLE_CANCEL, "取消");
				startActivityForResult(intent, REQUEST_CODE_SEARCH);
			}
			break;

		case R.id.action_user:
			intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_SEARCH && resultCode == RESULT_OK) {
			// 取得识别的字符串
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			String res = results.get(0);
			int tcode = mTextUnderstander.understandText(res, textListener);
			if (tcode != 0) {
				Toast.makeText(MainActivity.this, "Error:" + res,
						Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	protected int supplyTabs(List<TabInfo> tabs) {
		tabs.add(new TabInfo(0, getString(R.string.fragment_one),
				BaseFragment.class));
		tabs.add(new TabInfo(1, getString(R.string.fragment_two),
				BaseFragment.class));
		tabs.add(new TabInfo(2, getString(R.string.fragment_three),
				BaseFragment.class));

		return 0;
	}

	public boolean checkSpeechServiceInstalled() {
		String packageName = UtilityConfig.DEFAULT_COMPONENT_NAME;
		List<PackageInfo> packages = getPackageManager()
				.getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			if (packageInfo.packageName.equals(packageName)) {
				return true;
			} else {
				continue;
			}
		}
		return false;
	}

	/**
	 * 如果服务组件没有安装，有两种安装方式。 1.直接打开语音服务组件下载页面，进行下载后安装。
	 * 2.把服务组件apk安装包放在assets中，为了避免被编译压缩，修改后缀名为mp3，然后copy到SDcard中进行安装。
	 */
	private boolean processInstall(Context context, String url, String assetsApk) {
		// 直接下载方式
		ApkInstaller.openDownloadWeb(context, url);
		// 本地安装方式
		// if(!ApkInstaller.installFromAssets(context, assetsApk)){
		// Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_SHORT).show();
		// return false;
		// }
		return true;
	}

	/**
	 * 判断action是否存在。
	 * 
	 * @param context
	 * @return
	 */
	public boolean isActionSupport(Context context) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(ACTION_INPUT);
		// 检索所有可用于给定的意图进行的活动。如果没有匹配的活动，则返回一个空列表。
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}
