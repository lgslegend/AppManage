
package com.codeartist.applocker.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.codeartist.applocker.R;
import com.codeartist.applocker.activity.DummyActivity;
import com.codeartist.applocker.db.DBManager;
import com.codeartist.applocker.interfaces.OnHomePressedListener;
import com.codeartist.applocker.receiver.ExpiredReceiver;
import com.codeartist.applocker.utility.Constants;
import com.codeartist.applocker.utility.HomeWatcher;
import com.codeartist.applocker.utility.Preferences;
import com.codeartist.applocker.utility.Utils;
import com.eftimoff.patternview.PatternView;

import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * Created by bjit-16 on 11/14/16.
 */

public class AppLockerService extends Service {

    // String CURRENT_PACKAGE_NAME;
    private Timer mTimer;
    private View widget;
    private static List<String> activityList;
    private Dialog checkerDialog;

    public static final int MSG_APP_UNLOCK = 1;
    public static final int MSG_APP_LOCK = 2;
    public static final int MSG_APP_PERMITTED = 3;
    public static final int MSG_APP_LOCK_SCREEN = 4;
    private DBManager mDb;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
            String packageName = msg.getData().getString(Constants.KEY_PKG_NAME);
            switch (msg.what) {
                case MSG_APP_UNLOCK:
                    activityList.remove(packageName);
                    Utils.deleteFromAppLockerTable(packageName, mDb);
                    // Toast.makeText(getApplicationContext(), "MSG_APP_UNLOCK! "+ packageName,
                    // Toast.LENGTH_SHORT).show();
                    break;
                case MSG_APP_LOCK:
                    activityList.add(packageName);
                    Utils.insertInToAppLockerTable(packageName, mDb);
                    // Toast.makeText(getApplicationContext(), "MSG_APP_LOCK! "+ packageName,
                    // Toast.LENGTH_SHORT).show();
                    break;
                case MSG_APP_PERMITTED:
                    activityList.remove(packageName);
                    sendMessageToHandler(Constants.KEY_CLOSE_DIALOG);
                    // Toast.makeText(getApplicationContext(), "MSG_APP_PERMITTED! "+ packageName,
                    // Toast.LENGTH_SHORT).show();
                    break;
                case MSG_APP_LOCK_SCREEN:
                   if (activityList.size() > 0)
                    {
                        showLockerScreen(getApplicationContext().getPackageName());
                    }
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger for sending messages to
     * the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private HomeWatcher mHomeWatcher = new HomeWatcher(this);

    public void destroyDialog() {
        if (checkerDialog != null && checkerDialog.isShowing()) {
            checkerDialog.dismiss();
            checkerDialog = null;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }

        mDb = DBManager.getInstance(getApplicationContext());
        activityList = Utils.getLockedApp(mDb);
        if (isScreenOn) {
            scheduleMethod();
        }

        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                goToHomeScreen();
                if (closeDialog.getState() == Thread.State.NEW) {
                    closeDialog.start();
                } else {
                    closeDialog.run();
                }
            }

            @Override
            public void onHomeLongPressed() {
                // goToHomeScreen();
                /*
                 * Intent closeSysDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                 * sendBroadcast(closeSysDialog);
                 */
                Log.e("recent app", "new activity fired");
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                        && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                  //  sendBroadcast(new Intent("closeRecent"));
                }
                destroyDialog();
                //scheduleMethod();
                /*
                 * Intent i = new Intent(AppLockerService.this, DummyActivity.class);
                 * i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i);
                 */

                  if (closeDialog.getState() == Thread.State.NEW) {
                      closeDialog.start();
                  } else {
                  closeDialog.run();
                  }

            }
        });
        mHomeWatcher.startWatch();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOnOffReceiver, filter);
        IntentFilter filterClock = new IntentFilter();
        filterClock.addAction(Intent.ACTION_TIME_CHANGED);
        filterClock.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mTimeChangedReceiver, filterClock);
        IntentFilter protectorCompleteFilter = new IntentFilter(
                Constants.PROTECTOR_COMPLETE_BROADCAST_KEY);
        registerReceiver(mProtectComplete, protectorCompleteFilter);
        startForeground(Constants.FOREGROUND_NOTIFICATION_ID, Utils.createNotification(this));
        // restartService(60 * 60 * 1000, true);
        // Log.e("activity on TOp", "" + "onCreate");
        // Toast.makeText(getApplicationContext(), "service onCreate! ", Toast.LENGTH_SHORT).show();
    }

    private void goToHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private Thread closeDialog = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                sendMessageToHandler(Constants.KEY_CLOSE_DIALOG);
                Thread.sleep(150);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Utils.isMyServiceRunning(ProtectorLockService.class, this)) {
            stopService(new Intent(this, ProtectorLockService.class));
        }
        startService(new Intent(this, ProtectorLockService.class));

        return START_STICKY;
    }

    private BroadcastReceiver mScreenOnOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.i("[BroadcastReceiver]", "MyReceiver");

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i("[BroadcastReceiver]", "Onnn");
                activityList = Utils.getLockedApp(mDb);
                scheduleMethod();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i("[BroadcastReceiver]", "OFF");
                removeScheduleTask();
            }
        }
    };

    private BroadcastReceiver mProtectComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopService(new Intent(context, ProtectorLockService.class));
        }
    };

    private BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("activity on TOp", "............time changed........");
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                isScreenOn = pm.isInteractive();
            } else {
                isScreenOn = pm.isScreenOn();
            }

            if (isScreenOn) {
                scheduleMethod();
            }
            // restartService(3000, false);
        }
    };

    private void removeScheduleTask() {
        // Log.e("remove schdule", mTimer + "");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) { // restartService(1000, false); if
        Log.e("remove schdule", "onTask re moved called");
        Intent i = new Intent(AppLockerService.this, DummyActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent restartService = new Intent(getApplicationContext(), this.getClass());
            restartService.setPackage(getPackageName());
            PendingIntent restartServicePI = PendingIntent.getService(getApplicationContext(), 1,
                    restartService,
                    PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext()
                    .getSystemService(Context.ALARM_SERVICE);
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000,
                    restartServicePI);
        }
   // }
    super.onTaskRemoved(rootIntent);
    }

    private void restartService(int interval, boolean isRepeating) {
        // stopSelf();
        Log.e("activity on TOp", "restart service");
        Intent restartServiceIntent = new Intent(getApplicationContext(), ExpiredReceiver.class);
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        if (isRepeating) {
            alarmService.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    interval, restartServicePendingIntent);
        } else {
            alarmService.set(
                    AlarmManager.ELAPSED_REALTIME,
                    System.currentTimeMillis() + interval,
                    restartServicePendingIntent);
        }

    }

    private void scheduleMethod() {
        // Log.e("schdule task", mTimer + "");
        removeScheduleTask();
        mTimer = new Timer();
        // Log.e("activity on TOp", "" + "scheduleMethod");
        int timeInterval = Preferences.loadInt(this, Constants.KEY_LOCKER_ACCURACY,
                Constants.ACCURACY_HIGH);

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // This method will check for the Running apps after every 500ms
                checkRunningApps();
            }
        }, 0, timeInterval);

    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String packageName = (String) msg.obj;
            // Log.e("mHandler", packageName + "");
            if (String.valueOf(packageName) == Constants.KEY_CLOSE_DIALOG) {
                destroyDialog();
                scheduleMethod();
             return;
            }

            showLockerScreen(packageName);
            // showPatternDialog(packageName);
            removeScheduleTask();
        }
    };

    private void showLockerScreen(String packageName) {
        int lockType = Preferences.loadInt(getApplicationContext(), Constants.KEY_LOCKER_TYPE,
                Constants.PATTERN_LOCK);
        if (lockType == Constants.PATTERN_LOCK) {
            showPatternDialogOption(packageName);
            /*
             * Intent intent = new Intent(this, PatternVerifierActivity.class);
             * intent.putExtra(Constants.KEY_PKG_NAME, packageName);
             * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent);
             */
        } else {
            showCheckerDialog(packageName);
            /*
             * Intent intent = new Intent(this, PasswordVerifierActivity.class);
             * intent.putExtra(Constants.KEY_PKG_NAME, packageName);
             * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent);
             */
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkRunningApps() {
        if (activityList == null || activityList.isEmpty()) {
            return;
        }

        // Provide the package name(s) of apps here, you want to show password activity
        String activityOnTop = getTopActivity();

        if (activityOnTop != null && activityList.contains(activityOnTop)) {
            sendMessageToHandler(activityOnTop);
        }
    }

    private void sendMessageToHandler(String ob) {
        Message msg = Message.obtain(); // Creates an new Message instance
        msg.obj = ob; // Put the string into Message, into "obj" field.
        msg.setTarget(mHandler); // Set the Handler
        msg.sendToTarget(); // Send the message
    }

    private String getTopActivity() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);
        String activityOnTop = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // intentionally using string value as Context.USAGE_STATS_SERVICE was
            // strangely only added in API 22 (LOLLIPOP_MR1)
            @SuppressWarnings("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager) getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(),
                            usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    activityOnTop = mySortedMap.get(
                            mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            // activityOnTop = mActivityManager.getRunningAppProcesses().get(0).processName;
            activityOnTop = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
        Log.e("activity on TOp", "" + activityOnTop);
        return activityOnTop;
    }

    private void showCheckerDialog(final String packageName) {
        if (checkerDialog == null || !checkerDialog.isShowing()) {

            final Context context = getApplicationContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            widget = layoutInflater.inflate(R.layout.applock_number_lock_view, null);
            final ImageButton check = (ImageButton) widget.findViewById(R.id.button_check);
            final EditText password = (EditText) widget
                    .findViewById(R.id.edt1);

            Button button0 = (Button) widget.findViewById(R.id.button0);
            Button button1 = (Button) widget.findViewById(R.id.button1);
            Button button2 = (Button) widget.findViewById(R.id.button2);
            Button button3 = (Button) widget.findViewById(R.id.button3);
            Button button4 = (Button) widget.findViewById(R.id.button4);
            Button button5 = (Button) widget.findViewById(R.id.button5);
            Button button6 = (Button) widget.findViewById(R.id.button6);
            Button button7 = (Button) widget.findViewById(R.id.button7);
            Button button8 = (Button) widget.findViewById(R.id.button8);
            Button button9 = (Button) widget.findViewById(R.id.button9);
            Button buttonC = (Button) widget.findViewById(R.id.buttonC);

            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("1");
                }
            });

            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("2");
                }
            });

            button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("3");
                }
            });

            button4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("4");
                }
            });

            button5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("5");
                }
            });

            button6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("6");
                }
            });

            button7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("7");
                }
            });

            button8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("8");
                }
            });

            button9.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("9");
                }
            });

            button0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.append("0");
                }
            });

            buttonC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int length = password.getText().length();
                    if (length > 0) {
                        password.getText().delete(length - 1, length);
                    }
                }
            });
            password.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
                    return true;
                }

            });
            checkerDialog = new Dialog(context, R.style.DialogTheme);
            checkerDialog.setCanceledOnTouchOutside(false);
            checkerDialog.setCancelable(false);
            checkerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            checkerDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            checkerDialog.getWindow().setFormat(PixelFormat.TRANSLUCENT);
            checkerDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            checkerDialog.setContentView(widget);
            checkerDialog.getWindow().setGravity(Gravity.CENTER);
            checkerDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_BACK && checkerDialog != null && checkerDialog.isShowing()) {
                        // goToHomeScreen();
                        moveFrontRecentTask();

                        /*
                         * destroyDialog(); scheduleMethod();
                         */

                        sendBroadcast(new Intent("closeActivity"));

                        if (closeDialog.getState() == Thread.State.NEW) {
                            closeDialog.start();
                        } else {
                            closeDialog.run();
                        }
                        return true;

                    }
                    return false;
                }
            });

            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String savedPassword = Preferences.loadString(getApplicationContext(),
                            Preferences.KEY_APP_LOCKER_PASSWORD, null);
                    if (savedPassword != null && password.getText().toString() != null
                            && password.getText().toString().equals(savedPassword)) {
                        if (closeDialog.getState() == Thread.State.NEW) {
                            closeDialog.start();
                        } else {
                            closeDialog.run();
                        }
                        /*
                         * destroyDialog(); scheduleMethod();
                         */
                        activityList.remove(packageName);

                    } else {
                        Toast.makeText(getApplicationContext(), "密码错误",
                                Toast.LENGTH_LONG).show();
                    }

                }
            });

            checkerDialog.show();
        }

    }

    /*private void showPatternDialog(final String packageName) {
        if (checkerDialog == null || !checkerDialog.isShowing()) {

            final Context context = getApplicationContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            widget = layoutInflater.inflate(R.layout.applock_pattern_lock_layout, null);
            final Lock9View pattern = (Lock9View) widget.findViewById(R.id.lock_9_view);
            checkerDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            checkerDialog.setCanceledOnTouchOutside(false);
            checkerDialog.setCancelable(false);
            checkerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            checkerDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            checkerDialog.getWindow().setFormat(PixelFormat.TRANSLUCENT);
            checkerDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            checkerDialog.setContentView(widget);
            checkerDialog.getWindow().setGravity(Gravity.CENTER);
            checkerDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                        destroyDialog();
                        scheduleMethod();
                        return true;

                    }
                    return false;
                }
            });

            *//*
             * check.setOnClickListener(new View.OnClickListener() {
             * @Override public void onClick(View view) { String savedPassword =
             * Preferences.loadString(getApplicationContext(), Preferences.KEY_APP_LOCKER_PASSWORD,
             * null); if (savedPassword != null && password.getText().toString() != null &&
             * password.getText().toString().equals(savedPassword)) { destroyDialog();
             * activityList.remove(packageName); scheduleMethod(); } else {
             * Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_LONG).show();
             * } } });
             *//*

            *//*
             * pattern.setOnPatternDetectedListener(new PatternView.OnPatternDetectedListener() {
             * @Override public void onPatternDetected() { String savedPassword =
             * Preferences.loadString(getApplicationContext(), Preferences.KEY_APP_LOCKER_PASSWORD,
             * null); if (savedPassword != null && pattern.getPatternString() != null &&
             * pattern.getPatternString().equals(savedPassword)) { destroyDialog();
             * activityList.remove(packageName); scheduleMethod(); } else {
             * Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_LONG).show();
             * } } });
             *//*

            pattern.setCallBack(new Lock9View.CallBack() {
                @Override
                public void onFinish(String password) {
                    String savedPassword = Preferences.loadString(getApplicationContext(),
                            Preferences.KEY_APP_LOCKER_PASSWORD, null);
                    if (savedPassword != null && password != null
                            && password.equals(savedPassword)) {
                        destroyDialog();
                        activityList.remove(packageName);
                        scheduleMethod();
                    } else {
                        Toast.makeText(getApplicationContext(), "Wrong Password",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

            checkerDialog.show();
        }

    }
*/

      private void killApps() {
          List<ApplicationInfo> packages;
          PackageManager pm;
          pm = getPackageManager(); // get a list of installed apps. // packages =
          pm.getInstalledApplications(0);
          ActivityManager mActivityManager = (ActivityManager)
                  getSystemService(Context.ACTIVITY_SERVICE);
          List<ActivityManager.RunningAppProcessInfo> pids
                  = mActivityManager.getRunningAppProcesses();
          int processid = 0;
          for (int i = 0; i < pids.size(); i++) {
              ActivityManager.RunningAppProcessInfo info = pids.get(i);
              processid = info.pid;
          }
        //  mActivityManager.moveTaskToFront(processid, ActivityManager.MOVE_TASK_WITH_HOME); //
          android.os.Process.killProcess(processid);
      }


    private void moveFrontRecentTask() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);
        ActivityManager.RecentTaskInfo recentTask = null;
        try {
            recentTask = mActivityManager
                    .getRecentTasks(3, ActivityManager.RECENT_IGNORE_UNAVAILABLE).get(1);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            goToHomeScreen();
        }
        // maintains state more accurately - only available in API 11+ (3.0/Honeycomb+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB
                && recentTask != null) {
            // final ActivityManager am = (ActivityManager)
            // getSystemService(Context.ACTIVITY_SERVICE);
            //killApps();
            mActivityManager.moveTaskToFront(recentTask.id, ActivityManager.MOVE_TASK_WITH_HOME);
        } else { // API 10 and below (Gingerbread on down)
            /*
             * Intent restartTaskIntent = new Intent(recentTask.baseIntent); if (restartTaskIntent
             * != null) { restartTaskIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
             * try { startActivity(restartTaskIntent); } catch (ActivityNotFoundException e) {
             * Log.w("MyApp", "Unable to launch recent task", e); } }
             */
        }
    }

    private void showPatternDialogOption(final String packageName) {
        if (checkerDialog == null || !checkerDialog.isShowing()) {

            final Context context = getApplicationContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            widget = layoutInflater.inflate(R.layout.applock_pattern_lock_option, null);
            final PatternView pattern = (PatternView) widget.findViewById(R.id.patternView);
            checkerDialog = new Dialog(context, R.style.DialogTheme);
            checkerDialog.setCanceledOnTouchOutside(false);
            checkerDialog.setCancelable(false);
            checkerDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            checkerDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            checkerDialog.getWindow().setFormat(PixelFormat.TRANSLUCENT);
            checkerDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            checkerDialog.setContentView(widget);
            checkerDialog.getWindow().setGravity(Gravity.CENTER);
            checkerDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && checkerDialog != null && checkerDialog.isShowing()) {
                        // goToHomeScreen();
                        // goLauncherScreen();
                        sendBroadcast(new Intent("closeActivity"));
                        // killApps();
                        moveFrontRecentTask();

                        if (closeDialog.getState() == Thread.State.NEW) {
                            closeDialog.start();
                        } else {
                            closeDialog.run();
                        }
                        return true;

                    }
                    return false;
                }
            });

            /*
             * check.setOnClickListener(new View.OnClickListener() {
             * @Override public void onClick(View view) { String savedPassword =
             * Preferences.loadString(getApplicationContext(), Preferences.KEY_APP_LOCKER_PASSWORD,
             * null); if (savedPassword != null && password.getText().toString() != null &&
             * password.getText().toString().equals(savedPassword)) { destroyDialog();
             * activityList.remove(packageName); scheduleMethod(); } else {
             * Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_LONG).show();
             * } } });
             */

            /*
             * pattern.setOnPatternDetectedListener(new PatternView.OnPatternDetectedListener() {
             * @Override public void onPatternDetected() { String savedPassword =
             * Preferences.loadString(getApplicationContext(), Preferences.KEY_APP_LOCKER_PASSWORD,
             * null); if (savedPassword != null && pattern.getPatternString() != null &&
             * pattern.getPatternString().equals(savedPassword)) { destroyDialog();
             * activityList.remove(packageName); scheduleMethod(); } else {
             * Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_LONG).show();
             * } } });
             */

            /*
             * pattern.setCallBack(new Lock9View.CallBack() {
             * @Override public void onFinish(String password) { String savedPassword =
             * Preferences.loadString(getApplicationContext(), Preferences.KEY_APP_LOCKER_PASSWORD,
             * null); if (savedPassword != null && password != null &&
             * password.equals(savedPassword)) { destroyDialog(); activityList.remove(packageName);
             * scheduleMethod(); } else { Toast.makeText(getApplicationContext(), "Wrong Password",
             * Toast.LENGTH_LONG).show(); } } });
             */
            pattern.setOnPatternDetectedListener(new PatternView.OnPatternDetectedListener() {
                @Override
                public void onPatternDetected() {
                    String patternString = pattern.getPatternString();
                    if (patternString == null) {
                        // patternString = pattern.getPatternString();
                        // patternView.clearPattern();
                        return;
                    }

                    String savedPassword = Preferences.loadString(getApplicationContext(),
                            Preferences.KEY_APP_LOCKER_PASSWORD, null);
                    if (savedPassword != null && patternString != null
                            && patternString.equals(savedPassword)) {
                        destroyDialog();
                        activityList.remove(packageName);
                        scheduleMethod();
                    } else {
                        Toast.makeText(getApplicationContext(), "密码错误",
                                Toast.LENGTH_LONG).show();
                    }
                }

            });

            checkerDialog.show();
        }

    }

    @Override
    public void onDestroy() {

        Log.e("activity on TOp", "" + "onDestroy");
        removeScheduleTask();
        if (mScreenOnOffReceiver != null) {
            unregisterReceiver(mScreenOnOffReceiver);
        }

        if (mTimeChangedReceiver != null) {
            unregisterReceiver(mTimeChangedReceiver);
        }

        if (mProtectComplete != null) {
            unregisterReceiver(mProtectComplete);
        }

        if (mHomeWatcher != null) {
            mHomeWatcher.stopWatch();
        }
        stopForeground(true);
        destroyDialog();
        restartService(1000, false);
        super.onDestroy();

    }

}
