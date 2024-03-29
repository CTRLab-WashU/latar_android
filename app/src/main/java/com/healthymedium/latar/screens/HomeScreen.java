package com.healthymedium.latar.screens;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthymedium.latar.BaseFragment;
import com.healthymedium.latar.Proctor;
import com.healthymedium.latar.R;
import com.healthymedium.latar.network.TcpConnection;
import com.healthymedium.latar.network.models.DeviceInfo;

@SuppressLint("ValidFragment")
public class HomeScreen extends BaseFragment {

    TextView textViewDeviceInfo;
    TextView textViewStatus;
    TextView textViewAddress;
    Button button;

    Handler handler = new Handler();

    public HomeScreen() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setLenient()
                .create();
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        textViewDeviceInfo = view.findViewById(R.id.textViewDeviceInfo);
        textViewDeviceInfo.setText(gson.toJson(deviceInfo));

        textViewAddress = view.findViewById(R.id.textViewAddress);
        textViewAddress.setText("Server Address: "+ Proctor.get().getServerAddress());
        textViewAddress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                buildAddressDialog();
                return false;
            }
        });


        textViewStatus = view.findViewById(R.id.textViewStatus);
        button = view.findViewById(R.id.button);
        if(Proctor.get().isConnected()){
            button.setText("Disconnect");
            button.setOnClickListener(OnClickListenerDisconnect);
            textViewStatus.setText("Connected");
        } else {
            button.setText("Connect");
            button.setOnClickListener(OnClickListenerConnect);
            textViewStatus.setText("Not Connected");
        }

        if(!Proctor.get().hasServerAddress()) {
            button.setEnabled(false);
        }

        handler = new Handler(Looper.getMainLooper());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Proctor.get().setListener(new Proctor.Listener() {
            @Override
            public void onServiceBound() {
                Proctor.getTcpConnection().addListener(connectionListener);
            }

            @Override
            public void onServiceUnbound() {
                Proctor.getTcpConnection().removeListener(connectionListener);
            }

            @Override
            public void onServerFound(String address) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(textViewAddress!=null){
                            textViewAddress.setOnLongClickListener(null);
                            textViewAddress.setText("Server Address: "+ Proctor.get().getServerAddress());
                            button.setEnabled(true);
                        }
                    }
                },100);

            }
        });


//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                NavigationManager.getInstance().popBackStack();
//                //DisplayParams params = new DisplayParams();
//                //params.count = 20;
//                //params.interval = 500;
//                //NavigationManager.getInstance().open(new DisplayLatencyScreen(params));
//                NavigationManager.getInstance().open(new TapLatencyScreen());
//            }
//        },2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        Proctor.get().removeListener();
    }


    TcpConnection.Listener connectionListener = new TcpConnection.Listener() {
        @Override
        public void onConnected() {
            button.setEnabled(false);
            button.setText("Disconnect");
            textViewStatus.setText("Connected");
            button.setOnClickListener(OnClickListenerDisconnect);
            button.setEnabled(true);
        }

        @Override
        public void onDisconnected() {
            button.setEnabled(false);
            button.setText("Connect");
            textViewStatus.setText("Not Connected");
            button.setOnClickListener(OnClickListenerConnect);
            button.setEnabled(true);
        }

        @Override
        public void onError(Exception e) {
            button.setEnabled(false);
            button.setText("Connect");
            textViewStatus.setText("Not Connected");
            button.setOnClickListener(OnClickListenerConnect);
            button.setEnabled(true);
        }
    };

    View.OnClickListener OnClickListenerConnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            button.setEnabled(false);
            button.setText("Connecting");
            textViewStatus.setText("");
            Proctor.get().connect();
        }
    };

    View.OnClickListener OnClickListenerDisconnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            button.setEnabled(false);
            button.setText("");
            textViewStatus.setText("");
            Proctor.get().disconnect();
            button.setEnabled(true);
        }
    };

    AlertDialog dialog;

    void buildAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        View view = View.inflate(getContext(),R.layout.dialog_address,null);
        alertDialog.setView(view);


        final EditText editText = view.findViewById(R.id.editText);
        final Button buttonCancel = view.findViewById(R.id.buttonCancel);
        final Button buttonSave = view.findViewById(R.id.buttonSave);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonCancel.setEnabled(false);
                buttonSave.setEnabled(false);
                dialog.dismiss();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonCancel.setEnabled(false);
                buttonSave.setEnabled(false);

                String string = editText.getText().toString();
                Proctor.get().setServerAddress(string);
                if (textViewAddress != null) {
                    textViewAddress.setText("Server Address: " + Proctor.get().getServerAddress());
                    button.setEnabled(true);
                }

                dialog.dismiss();
            }
        });

        dialog = alertDialog.show();
    }




}
