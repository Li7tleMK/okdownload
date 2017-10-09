/*
 * Copyright (C) 2017 Jacksgong(jacksgong.com)
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
 */

package cn.dreamtobe.okdownload;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;

import cn.dreamtobe.okdownload.core.breakpoint.BreakpointStore;
import cn.dreamtobe.okdownload.core.breakpoint.BreakpointStoreOnCache;
import cn.dreamtobe.okdownload.core.breakpoint.DownloadStrategy;
import cn.dreamtobe.okdownload.core.connection.DownloadConnection;
import cn.dreamtobe.okdownload.core.connection.DownloadUrlConnection;
import cn.dreamtobe.okdownload.core.dispatcher.CallbackDispatcher;
import cn.dreamtobe.okdownload.core.dispatcher.DownloadDispatcher;
import cn.dreamtobe.okdownload.core.file.DefaultProcessFileStrategy;
import cn.dreamtobe.okdownload.core.file.DownloadOutputStream;
import cn.dreamtobe.okdownload.core.file.DownloadUriOutputStream;
import cn.dreamtobe.okdownload.core.file.ProcessFileStrategy;

public class OkDownload {

    private static volatile OkDownload SINGLETON;

    public final DownloadDispatcher downloadDispatcher;
    public final CallbackDispatcher callbackDispatcher;
    public final BreakpointStore breakpointStore;
    public final DownloadConnection.Factory connectionFactory;
    public final DownloadOutputStream.Factory outputStreamFactory;
    public final ProcessFileStrategy processFileStrategy;
    public final DownloadStrategy downloadStrategy;

    public final Context context;

    DownloadMonitor monitor;
    boolean lenience = false;

    OkDownload(Context context, DownloadDispatcher downloadDispatcher, CallbackDispatcher callbackDispatcher,
               BreakpointStore breakpointStore, DownloadConnection.Factory connectionFactory,
               DownloadOutputStream.Factory outputStreamFactory,
               ProcessFileStrategy processFileStrategy, DownloadStrategy downloadStrategy) {
        this.context = context;
        this.downloadDispatcher = downloadDispatcher;
        this.callbackDispatcher = callbackDispatcher;
        this.breakpointStore = breakpointStore;
        this.connectionFactory = connectionFactory;
        this.outputStreamFactory = outputStreamFactory;
        this.processFileStrategy = processFileStrategy;
        this.downloadStrategy = downloadStrategy;
    }

    public static DownloadTask obtainTask(String url, Uri fileUri) {
        return new DownloadTask(url, fileUri);
    }

    public static DownloadTask obtainTask(String url, File file) {
        return new DownloadTask(url, Uri.fromFile(file));
    }

    public void setMonitor(DownloadMonitor monitor) {
        this.monitor = monitor;
    }

    public static OkDownload with() {
        if (SINGLETON == null) {
            synchronized (OkDownload.class) {
                if (SINGLETON == null) {
                    if (OkDownloadProvider.context == null) {
                        throw new IllegalStateException("context == null");
                    }
                    SINGLETON = new Builder(OkDownloadProvider.context).build();
                }
            }
        }
        return SINGLETON;
    }

    public static void setSingletonInstance(@NonNull OkDownload okDownload) {
        if (SINGLETON != null) {
            throw new IllegalArgumentException(("OkDownload must be null."));
        }

        synchronized (OkDownload.class) {
            if (SINGLETON != null) {
                throw new IllegalArgumentException(("OkDownload must be null."));
            }
            SINGLETON = okDownload;
        }
    }

    public static class Builder {
        private DownloadDispatcher downloadDispatcher;
        private CallbackDispatcher callbackDispatcher;
        private BreakpointStore breakpointStore;
        private DownloadConnection.Factory connectionFactory;
        private ProcessFileStrategy processFileStrategy;
        private DownloadStrategy downloadStrategy;
        private DownloadOutputStream.Factory outputStreamFactory;
        private DownloadMonitor monitor;
        private final Context context;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder downloadDispatcher(DownloadDispatcher downloadDispatcher) {
            this.downloadDispatcher = downloadDispatcher;
            return this;
        }

        public Builder callbackDispatcher(CallbackDispatcher callbackDispatcher) {
            this.callbackDispatcher = callbackDispatcher;
            return this;
        }

        public Builder breakpointStore(BreakpointStore breakpointStore) {
            this.breakpointStore = breakpointStore;
            return this;
        }

        public Builder connectionFactory(DownloadConnection.Factory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public Builder outputStreamFactory(DownloadOutputStream.Factory outputStreamFactory) {
            this.outputStreamFactory = outputStreamFactory;
            return this;
        }

        public Builder processFileStrategy(ProcessFileStrategy processFileStrategy) {
            this.processFileStrategy = processFileStrategy;
            return this;
        }

        public Builder downloadStrategy(DownloadStrategy downloadStrategy) {
            this.downloadStrategy = downloadStrategy;
            return this;
        }

        public Builder monitor(DownloadMonitor monitor) {
            this.monitor = monitor;
            return this;
        }

        public OkDownload build() {
            if (downloadDispatcher == null) {
                downloadDispatcher = new DownloadDispatcher();
            }

            if (callbackDispatcher == null) {
                callbackDispatcher = new CallbackDispatcher();
            }

            if (breakpointStore == null) {
                breakpointStore = new BreakpointStoreOnCache();
            }

            if (connectionFactory == null) {
                connectionFactory = new DownloadUrlConnection.Factory();
            }

            if (outputStreamFactory == null) {
                outputStreamFactory = new DownloadUriOutputStream.Factory();
            }

            if (processFileStrategy == null) {
                processFileStrategy = new DefaultProcessFileStrategy();
            }

            if (downloadStrategy == null) {
                downloadStrategy = new DownloadStrategy();
            }

            OkDownload okDownload = new OkDownload(context, downloadDispatcher, callbackDispatcher,
                    breakpointStore, connectionFactory, outputStreamFactory, processFileStrategy,
                    downloadStrategy);

            okDownload.setMonitor(monitor);

            return okDownload;
        }
    }
}
