//package ro.ananimarius.allridev3;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//
//import androidx.annotation.Nullable;
//
//public class MyServices extends Service {
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        // Service is created
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // Service is started
//        return START_STICKY;
//    }
//
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        // App is removed from recent apps
//        super.onTaskRemoved(rootIntent);
//        // Perform any clean up operations here
//        // For example, disconnect from the server
//        // stop any running threads, etc.
//        //new DriverHomeActivity.SignOutTask().execute();
//        new DriverHomeActivity.StaticSignOutTask().execute();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        // Service is destroyed
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}
