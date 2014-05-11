package com.whr.taskmanager;

import com.whr.taskmanager.bean.Task;
import com.whr.taskmanager.bean.Task.MentionAction;
import com.whr.taskmanager.util.AlarmAlertWakeLock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmAlertActivity extends Activity {
	private static final long[] sVibratePattern = new long[] { 500, 500 };

	private MediaPlayer mMediaPlayer;

	private Vibrator mVibrator;

	private AlertDialog mDialog;

	String title;
	String content;

	boolean mIsVoice;

	boolean mIsVibrator;

	private TextView mTitle;

	private TextView mContent;

	private Button m5mRepeat;
	private Button mClose;

	/**
	 * 获取的是铃声的Uri
	 * 
	 * @param ctx
	 * @param type
	 * @return
	 */
	public Uri getDefaultRingtoneUri(Context ctx, int type) {

		return RingtoneManager.getActualDefaultRingtoneUri(ctx, type);

	}

	/**
	 * 获取的是铃声相应的Ringtone
	 * 
	 * @param ctx
	 * @param type
	 */
	public Ringtone getDefaultRingtone(Context ctx, int type) {
		return RingtoneManager.getRingtone(ctx,
				RingtoneManager.getActualDefaultRingtoneUri(ctx, type));

	}

	/**
	 * 播放铃声
	 * 
	 * @param ctx
	 * @param type
	 */

	public void PlayRingTone(Context ctx, int type) {
		mMediaPlayer = MediaPlayer
				.create(ctx, getDefaultRingtoneUri(ctx, type));
		mMediaPlayer.setLooping(true);
		mMediaPlayer.start();

	}

	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 点亮屏幕
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.layout_alert);
		initView();
		Intent intent = getIntent();
		title = intent.getStringExtra("title");
		content = intent.getStringExtra("content");
		mIsVoice = intent.getBooleanExtra("voice", false);
		mIsVibrator = intent.getBooleanExtra("vibrator", false);
		mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		mTitle.setText(title);
		mContent.setText(content);
		if (mIsVibrator) {
			mVibrator.vibrate(sVibratePattern, 0);
		}
		if (mIsVoice) {
			PlayRingTone(this, RingtoneManager.TYPE_ALARM);
		}
	}

	private void initView() {
		mTitle = (TextView) findViewById(R.id.title);
		mContent = (TextView) findViewById(R.id.content);
		m5mRepeat = (Button) findViewById(R.id.add_5_m);
		mClose = (Button) findViewById(R.id.close);

		m5mRepeat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						Intent i = new Intent(AlarmAlertActivity.this,
								AlarmAlertActivity.class);
						Bundle bundleRet = new Bundle();
						bundleRet.putString("title", "" + title);
						bundleRet.putString("content", "" + content);
						bundleRet.putBoolean("voice", mIsVoice);
						bundleRet.putBoolean("vibrator", mIsVibrator);
						i.putExtras(bundleRet);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						AlarmAlertActivity.this.startActivity(i);
					}
				}, 5*60*1000);
				AlarmAlertActivity.this.finish();
			}
		});

		mClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlarmAlertActivity.this.finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		AlarmAlertWakeLock.acquireCpuWakeLock(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		
	}
	@Override
	protected void onStop() {
		super.onStop();
	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AlarmAlertWakeLock.releaseCpuLock();
		try {
			if (mIsVibrator) {
				mVibrator.cancel();
			}
			if (mIsVoice) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}
		} catch (Exception e) {
		}
		try {
			AlarmAlertActivity.this.finish();
		} catch (Exception e) {
		}
	}
}
