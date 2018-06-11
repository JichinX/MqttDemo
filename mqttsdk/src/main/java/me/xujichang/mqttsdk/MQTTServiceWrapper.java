package me.xujichang.mqttsdk;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.ref.WeakReference;

import io.reactivex.observers.ResourceObserver;
import me.xujichang.mqttsdk.preference.MqttPreference;

/**
 * Des:使用MQTT协议 来获取消息
 *
 * @author xujichang
 * created at 2018/4/24 - 19:12
 */
public class MQTTServiceWrapper {
    private static String TAG = MQTTServiceWrapper.class.getSimpleName();
    private static MQTTServiceWrapper instance;
    private MqttAndroidClient mAndroidClient;
    private WeakReference<Context> mContextWeakReference;
    private MqttPreference mPreference;
    private ResourceObserver<String> mResourceObserver;
    private MessageReceiveListener mMessageReceiveListener;

    private MQTTServiceWrapper() {
    }


    public void start() {
        String clientId = mPreference.getClientPrefix() + System.currentTimeMillis();
        mAndroidClient = new MqttAndroidClient(mContextWeakReference.get(), mPreference.getServerIp(), clientId);
        mAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    //重连
                    subscribeTopic();
                } else {
                    //连接成功
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                //丢失链接
                Log.i(TAG, "connectionLost: " + cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //接收到一条消息
                Log.i(TAG, "messageArrived: topic-" + topic + " message" + message.toString());

                if (null != mResourceObserver) {
                    //发送消息
                }
                if (null != mMessageReceiveListener) {
                    mMessageReceiveListener.onReceivedMessage(message.toString());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    Log.i(TAG, "deliveryComplete: " + token.getMessage().toString());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
        MqttConnectOptions options = new MqttConnectOptions();
        //重连
        options.setAutomaticReconnect(true);
        //
        options.setCleanSession(false);
        //长连接间隔
//        options.setKeepAliveInterval(Const.ALIVE_INTERVAL);
//        options.setConnectionTimeout(Const.CONNECTION_TIMEOUT);
        try {
            mAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "mAndroidClient.connect onSuccess: ");
                    //链接服务器成功
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //连接服务器失败
                    Log.i(TAG, "mAndroidClient.connect onFailure: " + exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            //链接服务器失败
        }
    }

    /**
     * 订阅话题
     */
    private void subscribeTopic() {
        try {
            mAndroidClient.subscribe(mPreference.getTopic(), 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //订阅成功
                    Log.i(TAG, "mAndroidClient.subscribe onSuccess: ");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //订阅失败
                    Log.i(TAG, "mAndroidClient.subscribe onFailure: " + exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            //订阅失败
        }
    }

    //============================单例============================
    public static MQTTServiceWrapper getInstance() {

        return ClassHolder.sService;
    }

    /**
     * 订阅消息
     *
     * @param observer
     * @return
     */
    public MQTTServiceWrapper withObserver(ResourceObserver<String> observer) {
        mResourceObserver = observer;
        return getInstance();
    }

    public MQTTServiceWrapper withMessageReceiveListener(MessageReceiveListener listener) {
        mMessageReceiveListener = listener;
        return getInstance();
    }

    public void clear() {
        mAndroidClient.unregisterResources();
        mAndroidClient.close();
    }

    static class ClassHolder {
        static MQTTServiceWrapper sService = new MQTTServiceWrapper();
    }

    public MQTTServiceWrapper withContext(Context context) {
        mContextWeakReference = new WeakReference<>(context);
        return getInstance();
    }

    public MQTTServiceWrapper withPreference(MqttPreference preference) {
        mPreference = preference;
        return getInstance();
    }

    //=========================end=============================
    public void destory() {

    }

    public interface MessageReceiveListener {
        void onReceivedMessage(String message);

        void onReceivedError(String error);
    }
}
