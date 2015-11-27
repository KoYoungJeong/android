package setup;

import android.content.Context;
import android.net.ConnectivityManager;

import com.tosslab.jandi.app.JandiApplication;

import org.mockito.Mockito;

/**
 * Created by jsuch2362 on 15. 11. 19..
 */
public class MockUtil {

    public static void networkOff() {
        JandiApplication.setContext(Mockito.mock(Context.class));
        ConnectivityManager connectivityManager = Mockito.mock(ConnectivityManager.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.doReturn(null).when(connectivityManager).getActiveNetworkInfo();
        Mockito.doReturn(connectivityManager).when(JandiApplication.getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}
