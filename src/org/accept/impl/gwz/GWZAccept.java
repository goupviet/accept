package org.accept.impl.gwz;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.accept.domain.ValidationResult;
import org.accept.impl.settings.AcceptSettings;
import org.accept.impl.settings.Settings;
import org.accept.util.files.FileIO;

public class GWZAccept {
	
	final static Logger log = Logger.getLogger(GWZAccept.class.toString());
    ProcessRunner runner = new ProcessRunner();

	public String validate(String content, Settings settings, String guid) {
        //TODO: those files are not deleted until the end of the program run
		File input = new FileIO().writeToTempFile(content);
		File output = new FileIO().createTempFile();

        AcceptSettings acceptSettings = new AcceptSettings(settings.getContent());
        String command = acceptSettings.buildCommand() + " org.accept.impl.gwz.GWZMain \"" + input + "\" \"" + output + "\"";

		log.info("About to run this command: \n" + command);

		String out = runner.run(command, output, guid);
		return out;
	}

    public String getProgress(String guid) {
        return runner.getOutput(guid);
    }

    public String getFiles(String dir) {
        //TODO: ugly code copy-pasted from web. I confess.
        //TODO It should be changed to only show .story files
        if (dir.charAt(dir.length() - 1) == '\\') {
            dir = dir.substring(0, dir.length() - 1) + "/";
        } else if (dir.charAt(dir.length() - 1) != '/') {
            dir += "/";
        }

        StringBuffer out = new StringBuffer();
        if (new File(dir).exists()) {
            String[] files = new File(dir).list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.charAt(0) != '.';
                }
            });
            Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
            out.append("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
            // All dirs
            for (String file : files) {
                if (new File(dir, file).isDirectory()) {
                    out.append("<li class=\"directory collapsed\"><a href=\"#\" rel=\"" + dir + file + "/\">"
                            + file + "</a></li>");
                }
            }
            // All files
            for (String file : files) {
                if (!new File(dir, file).isDirectory() && file.endsWith(".story")) {
                    int dotIndex = file.lastIndexOf('.');
                    String ext = dotIndex > 0 ? file.substring(dotIndex + 1) : "";
                    out.append("<li class=\"file ext_" + ext + "\"><a href=\"#\" rel=\"" + dir + file + "\">"
                            + file + "</a></li>");
                }
            }
            out.append("</ul>");
        }
        String s = out.toString();
        return s;
    }

    public void createExampleStory(File dir) {
        File file = new File(dir, "example.story");
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file: " + file, e);
        }
        new FileIO().write(file, "#I generated this example for you\n" +
                "#because there were no stories in " + dir.getAbsolutePath()  + " folder.\n" +
                "#Go ahead and validate it. If it fails read *carefully* the exception message.\n" +
                "Given\n" +
                "  calculator is turned on\n" +
                "  I enter 7\n" +
                "  I enter 10\n" +
                "When\n" +
                "  I add the numbers\n" +
                "Then\n" +
                "  The calculator shows 17");
    }

    public String kill(String guid) {
        runner.kill(guid);
        return "Process with guid: " + guid + " has been killed!";
    }
}