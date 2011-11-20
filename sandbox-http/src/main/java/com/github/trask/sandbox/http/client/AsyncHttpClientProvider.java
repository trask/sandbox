/**
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.trask.sandbox.http.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.github.trask.sandbox.executors.DaemonExecutors;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

/**
 * @author Trask Stalnaker
 */
public class AsyncHttpClientProvider {

    private static final boolean USE_NETTY_BLOCKING_IO = false;

    private static int count;

    public AsyncHttpClient get() {
        ExecutorService executorService = DaemonExecutors.newCachedThreadPool("RH-HTTP" + count++);
        ScheduledExecutorService scheduledExecutorService =
                DaemonExecutors.newSingleThreadScheduledExecutor("RH--HTTP" + count++);
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder()
                .setMaxRequestRetry(0)
                .setExecutorService(executorService)
                .setScheduledExecutorService(scheduledExecutorService);
        NettyAsyncHttpProviderConfig providerConfig = new NettyAsyncHttpProviderConfig();
        if (USE_NETTY_BLOCKING_IO) {
            providerConfig.addProperty(NettyAsyncHttpProviderConfig.USE_BLOCKING_IO, true);
        }
        providerConfig.addProperty(NettyAsyncHttpProviderConfig.BOSS_EXECUTOR_SERVICE,
                executorService);
        builder.setAsyncHttpClientProviderConfig(providerConfig);
        return new AsyncHttpClient(builder.build());
    }
}
