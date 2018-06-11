package me.xujichang.mqttsdk.preference;

/**
 * Des:
 *
 * @author xujichang
 * created at 2018/6/11 - 15:55
 */
public class MqttPreference {
    /**
     * topic名称
     */
    public String topic;
    /**
     * 服务器IP
     */
    public String serverIp;
    /**
     * Android ClientID前缀
     */
    public String clientPrefix;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getClientPrefix() {
        return clientPrefix;
    }

    public void setClientPrefix(String clientPrefix) {
        this.clientPrefix = clientPrefix;
    }
}
