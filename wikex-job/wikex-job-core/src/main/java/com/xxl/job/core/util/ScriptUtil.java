package com.xxl.job.core.util;

import com.xxl.job.core.context.XxlJobHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  1. Embedded compilers like "PythonInterpreter" cannot reference external packages, so it is recommended to use Java to call console processes via "Runtime.getRuntime().exec()" to run scripts (shell or python);
 *  2. Because implementation uses Java calling console process, the target machine's PATH environment must be properly configured with the corresponding compiler;
 *  3. Currently, script execution logs can only be retrieved all at once after the script finishes executing, real-time log retrieval cannot be guaranteed; therefore, to ensure log timeliness, it is recommended to output logs to a specified log file;
 *  4. Python error output has higher priority than standard output, reflected in the log file, so it is recommended to log via logging to keep consistency with exception info; otherwise, print logs may get out of order.
 *
 * Created by william on 17/2/25.
 */
public class ScriptUtil {

    /**
     * make script file
     *
     * @param scriptFileName
     * @param content
     * @throws IOException
     */
    public static void markScriptFile(String scriptFileName, String content) throws IOException {
        // make file,   filePath/gluesource/666-123456789.py
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(scriptFileName);
            fileOutputStream.write(content.getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (Exception e) {
            throw e;
        }finally{
            if(fileOutputStream != null){
                fileOutputStream.close();
            }
        }
    }

    /**
     * Script execution with real-time log output to file
     *
     * @param command
     * @param scriptFile
     * @param logFile
     * @param params
     * @return
     * @throws IOException
     */
    public static int execToFile(String command, String scriptFile, String logFile, String... params) throws IOException {

        FileOutputStream fileOutputStream = null;
        Thread inputThread = null;
        Thread errThread = null;
        try {
            // file
            fileOutputStream = new FileOutputStream(logFile, true);

            // command
            List<String> cmdarray = new ArrayList<>();
            cmdarray.add(command);
            cmdarray.add(scriptFile);
            if (params!=null && params.length>0) {
                for (String param:params) {
                    cmdarray.add(param);
                }
            }
            String[] cmdarrayFinal = cmdarray.toArray(new String[cmdarray.size()]);

            // process-exec
            final Process process = Runtime.getRuntime().exec(cmdarrayFinal);

            // log-thread
            final FileOutputStream finalFileOutputStream = fileOutputStream;
            inputThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        copy(process.getInputStream(), finalFileOutputStream, new byte[1024]);
                    } catch (IOException e) {
                        XxlJobHelper.log(e);
                    }
                }
            });
            errThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        copy(process.getErrorStream(), finalFileOutputStream, new byte[1024]);
                    } catch (IOException e) {
                        XxlJobHelper.log(e);
                    }
                }
            });
            inputThread.start();
            errThread.start();

            // process-wait
            int exitValue = process.waitFor();      // exit code: 0=success, 1=error

            // log-thread join
            inputThread.join();
            errThread.join();

            return exitValue;
        } catch (Exception e) {
            XxlJobHelper.log(e);
            return -1;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    XxlJobHelper.log(e);
                }

            }
            if (inputThread != null && inputThread.isAlive()) {
                inputThread.interrupt();
            }
            if (errThread != null && errThread.isAlive()) {
                errThread.interrupt();
            }
        }
    }

    /**
     * Data stream copy (Input auto-close, Output not closed)
     *
     * @param inputStream
     * @param outputStream
     * @param buffer
     * @return
     * @throws IOException
     */
    private static long copy(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
        try {
            long total = 0;
            for (;;) {
                int res = inputStream.read(buffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    total += res;
                    if (outputStream != null) {
                        outputStream.write(buffer, 0, res);
                    }
                }
            }
            outputStream.flush();
            //out = null;
            inputStream.close();
            inputStream = null;
            return total;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Script execution with real-time log output to file
     *
     * Advantages: Supports outputting target data in real-time to the specified log file
     * Disadvantages:
     *      Standard output and error output priority is fixed, which may not match the order in the script
     *      Java cannot get logs in real time
     *
     *      <!-- commons-exec -->
     * 		<dependency>
     * 			<groupId>org.apache.commons</groupId>
     * 			<artifactId>commons-exec</artifactId>
     * 			<version>${commons-exec.version}</version>
     * 		</dependency>
     *
     * @param command
     * @param scriptFile
     * @param logFile
     * @param params
     * @return
     * @throws IOException
     */
    /*public static int execToFileB(String command, String scriptFile, String logFile, String... params) throws IOException {
        // Standard output: print (null if watchdog timeout)
        // Error output: logging + exception (still exists if watchdog timeout)
        // Standard input

        FileOutputStream fileOutputStream = null;   //
        try {
            fileOutputStream = new FileOutputStream(logFile, true);
            PumpStreamHandler streamHandler = new PumpStreamHandler(fileOutputStream, fileOutputStream, null);

            // command
            CommandLine commandline = new CommandLine(command);
            commandline.addArgument(scriptFile);
            if (params!=null && params.length>0) {
                commandline.addArguments(params);
            }

            // exec
            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(null);
            exec.setStreamHandler(streamHandler);
            int exitValue = exec.execute(commandline);  // exit code: 0=success, 1=error
            return exitValue;
        } catch (Exception e) {
            XxlJobLogger.log(e);
            return -1;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    XxlJobLogger.log(e);
                }

            }
        }
    }*/

}
