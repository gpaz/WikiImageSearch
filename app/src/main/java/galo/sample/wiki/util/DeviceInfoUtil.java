package galo.sample.wiki.util;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

/**
 * Utilities used for finding out certain information about the current device.
 * Created by Galo on 2/27/2015.
 */
public class DeviceInfoUtil
{
    /**
     * Sampled from <a href="http://stackoverflow.com/questions/7593829/how-to-get-the-processor-number-on-android">
     * http://stackoverflow.com/questions/7593829/how-to-get-the-processor-number-on-android</a>
     * @return
     * Returns the total number of processors the device has as per the '/proc/cpuinfo' file
     * shows, but at least 1.
     */
    public static int getNumberOfProcessors()
    {
        int nCpuCount = 0;
        Scanner sc = null;
        try
        {
            File cpuInfoFile = new File("/proc/cpuinfo");
            if(cpuInfoFile.exists() && cpuInfoFile.isFile() && cpuInfoFile.canRead())
            {
                sc = new Scanner(new FileInputStream(cpuInfoFile));
                while(sc.hasNextLine())
                {
                    String line = sc.nextLine();
                    if(line.startsWith("processor"))
                        ++nCpuCount;
                    else if(line.startsWith("Features"))
                        break;
                }
            }
        }
        catch(Exception e)
        {
            // do nothing
            Log.d(DeviceInfoUtil.class.getSimpleName(), "Unable to count device cpus.");
        }
        finally
        {
            if(sc != null)
                sc.close();
        }

        return Math.max(1, nCpuCount);
    }

    /**
     *
     * From <a href="http://stackoverflow.com/questions/7593829/how-to-get-the-processor-number-on-android">
     * http://stackoverflow.com/questions/7593829/how-to-get-the-processor-number-on-android</a>
     * @return
     * The number of available cpus are available to the VM.  This can be different than the actual number
     * of processors the device actually can have as noted through the link listed above.
     */
    public static int getNumberOrAvailableVMProcessors()
    {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Checks, by using the Context's configuration whether it is in portrait or not.
     * @param context
     * The Context.
     * @return
     * True if in portrait mode, as defined by the Configuration class, false otherwise.
     */
    public static boolean isPortrait(Context context)
    {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * From <a href="http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android">
     * http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android</a>
     * @param context
     * The context used to obtain the ConnectivityManager from.
     * @return
     * True if connected to a network, false if not.  This does not necessarily mean that the device
     * has Internet connectivity.
     */
    public static boolean checkInternetConnectivity(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
