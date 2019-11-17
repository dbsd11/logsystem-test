/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package group.bison.test.logsystem.parse.jlogstash;

import group.bison.test.logsystem.parse.jlogstash.core.assembly.AssemblyPipeline;
import group.bison.test.logsystem.parse.jlogstash.core.assembly.CmdLineParams;
import group.bison.test.logsystem.parse.jlogstash.core.exception.ExceptionUtil;
import group.bison.test.logsystem.parse.jlogstash.core.log.LogComponent;
import group.bison.test.logsystem.parse.jlogstash.core.log.LogbackComponent;
import group.bison.test.logsystem.parse.jlogstash.proc.JavaProcessFork;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:24:26
 * Company: www.dtstack.com
 *
 * @author sishu.yss
 */
public class JlogstashMain {
    private static final Logger logger = LoggerFactory.getLogger(JlogstashMain.class);

    private static AssemblyPipeline assemblyPipeline = new AssemblyPipeline();

    private static LogComponent logbackComponent = new LogbackComponent();

    public static void main(String[] args) {
        boolean isFork = Arrays.asList(args).stream().anyMatch(arg -> arg.contains("FORK"));
        if (isFork) {
            processFork(args);
            return;
        }

        String[] runArgs = Arrays.asList(String.join(" ", args).split(" ")).stream().filter(arg -> StringUtils.isNotEmpty(arg)).collect(Collectors.toList()).toArray(new String[0]);
        processRun(runArgs);

        logger.info("Jlogstash Started");
    }

    public static void processFork(String[] args) {
        try {
            FileAlterationObserver observer = new FileAlterationObserver("/tmp", FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.nameFileFilter("pid-jlogstash")));
            observer.addListener(new FileAlterationListenerAdaptor() {
                private JavaProcessFork processFork = null;

                @Override
                public void onStart(FileAlterationObserver observer) {
                    try {
                        if (processFork == null) {
                            processFork = JavaProcessFork.fork(String.join(" ", args).replaceAll("FORK", ""));

                            writeJlogstashPidFile();
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFileDelete(File file) {
                    //停止现在的进程
                    try {
                        processFork.terminateProcess();
                        processFork = null;
                    } catch (Exception e) {
                    }

                    onStart(observer);
                }

                private void writeJlogstashPidFile() throws Exception {
                    File jlogstashPid = new File("/tmp/pid-jlogstash");
                    if (!jlogstashPid.exists()) {
                        jlogstashPid.createNewFile();
                    }

                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/tmp/pid-jlogstash")))) {
                        bw.write(String.valueOf(processFork.getChildProcessPid()));
                        bw.flush();
                    } catch (Exception e) {
                    }
                }
            });
            FileAlterationMonitor monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(10), observer);
            monitor.start();
        } catch (Exception e) {
            logger.error("jlogstash fork error:{}", ExceptionUtil.getErrorMessage(e));
        }
    }

    public static void processRun(String[] args) {
        try {
            CommandLine cmdLine = OptionsProcessor.parseArg(args);
            CmdLineParams.setLine(cmdLine);
            //logger config
            logbackComponent.setupLogger();
            //assembly pipeline
            assemblyPipeline.assemblyPipeline();
        } catch (Exception e) {
            logger.error("jlogstash start error:{}", ExceptionUtil.getErrorMessage(e));
            System.exit(-1);
        }
    }
}
