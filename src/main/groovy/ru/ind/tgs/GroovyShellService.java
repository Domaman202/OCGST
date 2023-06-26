package ru.ind.tgs;

import groovy.lang.Binding;
import org.apache.groovy.groovysh.Groovysh;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.tools.shell.IO;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Author:      nik <br>
 * Date:        05.04.12, 18:00 <br>
 * Description: <br>
 */
public class GroovyShellService {
    private static final Logger LOGGER = LogManager.getLogger(GroovyShellService.class);

    private int listenPort;

    private static final List<String> DEFAULT_IMPORTS = Arrays.asList(
            "java.util.*",
            "static java.util.concurrent.TimeUnit.*"
    );
    private List<String> imports;
    private GroovyShellServer server;
    private Binding bindings;

    public void start() throws IOException {
        server = new GroovyShellServer();
        server.start();
    }

    public void stop() throws IOException {
        server.stopServer();
    }

    public void setListenPort(final int listenPort) {
        this.listenPort = listenPort;
    }

    public void setImports(final List<String> imports) {
        this.imports = imports;
    }

    public void setBindings(Binding bindings) {
        this.bindings = bindings;
    }

    private final class GroovyShellServer extends Thread {
        private final ServerSocket ss;
        private volatile boolean isRunning = true;

        private GroovyShellServer() throws IOException {
            ss = new ServerSocket(listenPort, 0, InetAddress.getByName(null)); // loopback interface only
        }

        public void stopServer() throws IOException {
            isRunning = false;
            ss.close();
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    final Socket s = ss.accept();

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                final TelnetStream telnet = new TelnetStream(s.getInputStream(), s.getOutputStream());

                                telnet.getOutputStream().writeWONT(34); // linemode
                                telnet.getOutputStream().writeWILL(1); // echo
                                telnet.getOutputStream().writeWILL(3); // supress go ahead

                                final Groovysh sh = new Groovysh(bindings, new IO(telnet.getInputStream(), telnet.getOutputStream(), telnet.getOutputStream()));

                                sh.getImports().addAll(DEFAULT_IMPORTS);
                                if (imports != null) {
                                    sh.getImports().addAll(imports);
                                }

                                sh.run(null);
                            } catch (IOException e) {
                                // ignore
                            } finally {
                                try {
                                    s.close();
                                } catch (IOException e) {
                                    // ignore
                                }
                            }
                        }
                    }.start();
                } catch (Throwable t) {
                    LOGGER.error("Error: " + t, t);
                }
            }
        }
    }
}