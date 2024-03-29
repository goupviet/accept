package org.accept.ui;

import org.accept.domain.ValidationResult;
import org.accept.impl.gwz.GWZAccept;
import org.accept.impl.settings.Settings;
import org.accept.util.commandline.CommandLineConfig;
import org.accept.util.exception.StackTracePrinter;
import org.tinyweb.Request;
import org.tinyweb.WebApplication;
import org.tinyweb.WebPage;
import org.tinyweb.internal.nano.NanoWebEngine;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class WebUI {

    Settings settings = new Settings();

    GWZAccept accept = new GWZAccept();
    final static Logger log = Logger.getLogger(WebUI.class.toString());

    @WebPage
    public String files(Request request) throws Exception {
        String dir = request.getParameters().get("dir");
        //TODO: should decoding be a part of TinyWeb?
        dir = java.net.URLDecoder.decode(dir, "UTF-8");

        String out = accept.getFiles(dir);
        //TODO, veeeeeeeeeeery hacky!!!!
        if (!out.contains("<li")) {
            accept.createExampleStory(new File(dir));
            out = accept.getFiles(dir);
        }
        return out;
    }

    @WebPage
    public String settings(Request request) throws Exception {
        return settings.getContent();
    }

    @WebPage
    public String validate(Request request) throws Exception {
        try {
            String content = request.getParameters().get("content");
            String guid = request.getParameters().get("guid");

            log.info("Using settings:\n" + settings.getContent() + "\n");
            log.info("Received request to validate following:\n" + content + "\n");
            return accept.validate(content, settings, guid);
        } catch (Throwable e) {
            ValidationResult result = new ValidationResult();
            String info = "Unable to validate the story. The cause is:\n" + e.getMessage();
            result.setFullLog(new StackTracePrinter().print(e));
            result.setInfo(info);
            result.setStatus(ValidationResult.Status.red);
            return result.toJSON();
        }
    }

    @WebPage
    public String getProgress(Request request) throws Exception {
        String guid = request.getParameters().get("guid");
        return accept.getProgress(guid);
    }

    @WebPage
    public String kill(Request request) throws Exception {
        String guid = request.getParameters().get("guid");
        return accept.kill(guid);
    }

    @WebPage
    public String saveSettings(Request request) throws Exception {
        String s = request.getParameters().get("settings");
        settings.save(s);
        return "Settings SAVED.";
    }

    //For development you must change the working dir to Accept/html !!!
    public static void main(String[] args) {
        CommandLineConfig config = new CommandLineConfig(args);

        int port = config.getPort(80);
        // workDir/storiesDir not yet used...
        String workDir = config.getWorkDir(".");
        String storiesDir = config.getStoriesDir(".");

        WebApplication app = new WebApplication(new NanoWebEngine("."));
        app.addWebPage(new WebUI());
        app.start(port);

        System.out.println("*********************");
        System.out.println("Welcome to the Accept tool! (Hit enter to stop the tool)");
        System.out.println("Listening on port: " + port + " (you can change the port passing param: -port 8888");
        try {
            System.out.println("Go to the app: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/index.html");
        } catch (UnknownHostException e) {}
        System.out.println("*********************");

        try { System.in.read(); } catch( Throwable t ) {};

        //TODO: @BeforeStep, @AfterStep for GivWenZen
    }
}