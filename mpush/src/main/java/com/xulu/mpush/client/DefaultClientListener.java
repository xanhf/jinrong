/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.xulu.mpush.client;



import android.text.TextUtils;
import android.util.Log;

import com.xulu.mpush.api.Client;
import com.xulu.mpush.api.ClientListener;
import com.xulu.mpush.message.RequestMarketMessage;
import com.xulu.mpush.util.thread.ExecutorManager;

import java.util.concurrent.Executor;

/*package*/ final class DefaultClientListener implements ClientListener {
    private final Executor executor = ExecutorManager.INSTANCE.getDispatchThread();
    public ClientListener listener;

    public void setListener(ClientListener listener) {
        Log.d("ssss", "setListener: "+listener);
        this.listener = listener;
    }

    @Override
    public void onConnected(final Client client) {
        if (listener != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onConnected(client);
                }
            });
        }
        client.fastConnect();
    }

    @Override
    public void onDisConnected(final Client client) {
        if (listener != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onDisConnected(client);
                }
            });
        }
        AckRequestMgr.I().clear();
    }

    @Override
    public void onHandshakeOk(final Client client, final int heartbeat) {
        if (listener != null) {//dispatcher已经使用了Executor，此处直接同步调用
            listener.onHandshakeOk(client, heartbeat);
        }
        String userId = ClientConfig.I.getUserId();
        if (!TextUtils.isEmpty(userId)){
            client.bindUser(userId, ClientConfig.I.getTags());
        }
    }

    @Override
    public void onReceivePush(final Client client, final byte[] content, int messageId) {
        if (listener != null) {//dispatcher已经使用了Executor，此处直接同步调用
            listener.onReceivePush(client, content, messageId);
        }
    }

    @Override
    public void onKickUser(String deviceId, String userId) {
        if (listener != null) {//dispatcher已经使用了Executor，此处直接同步调用
            listener.onKickUser(deviceId, userId);
        }
    }

    @Override
    public void onBind(Client client,boolean success, String userId) {
        if (listener != null) {//dispatcher已经使用了Executor，此处直接同步调用
            listener.onBind(client,success, userId);
        }
    }

    @Override
    public void onUnbind(boolean success, String userId) {
        if (listener != null) {//dispatcher已经使用了Executor，此处直接同步调用
            listener.onUnbind(success, userId);
        }
    }

    @Override
    public void onReceiverMarket(RequestMarketMessage receiver) {
        if (listener != null) {//dispatcher已经使用了Executor，此处直接同步调用
            listener.onReceiverMarket(receiver);
        }
    }
}