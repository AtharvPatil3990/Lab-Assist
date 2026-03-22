package com.android.labassist;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class NetworkMonitor extends LiveData<Boolean> {
    private final ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    public NetworkMonitor(Context context){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateConnection();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                boolean isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                boolean hasTransport = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                       networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                       networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);

                boolean isActuallyConnected = hasInternet && isValidated && hasTransport;

                if(isActuallyConnected != AppNetworkState.isNetworkConnected) {
                    AppNetworkState.isNetworkConnected = isActuallyConnected;
                    postValue(isActuallyConnected);
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                postValue(false);
                AppNetworkState.isNetworkConnected = false;
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if(connectivityManager != null && networkCallback != null){
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private void updateConnection(){
        if (connectivityManager == null) return;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            AppNetworkState.isNetworkConnected = false;
            postValue(false);
            return;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (capabilities == null) return;

        boolean isActuallyConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));

        AppNetworkState.isNetworkConnected = isActuallyConnected;
        postValue(isActuallyConnected);
    }
}