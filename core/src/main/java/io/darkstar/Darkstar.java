package io.darkstar;

@SuppressWarnings("unchecked")
public class Darkstar {

    public static void main(String[] args) throws Exception {

        long startupMillis = System.currentTimeMillis();

        String yamlFilePath;

        if (args.length == 0) {
            yamlFilePath = "/etc/darkstar/config.yaml";
        } else if (args.length == 1) {
            yamlFilePath = args[0];
        } else {
            System.err.println("Only a single (optional) argument is supported: the file path to config.yaml.  " +
                    "If unspecified, the default is /etc/darkstar/config.yaml.");
            return;
        }

        final String configFileLocation = applyUserHome(yamlFilePath);

        try {
            new DarkstarServer(startupMillis, configFileLocation).start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    private static String applyUserHome(String path) {
        String toReplace = "${user.home}";

        while (path.contains(toReplace)) {
            int index = path.indexOf(toReplace);
            String pre = "";
            if (index > 0) {
                pre = path.substring(0, index);
            }
            String post = path.substring(index + toReplace.length());
            String home = System.getProperty("user.home");
            path = pre + home + post;
            //System.out.println("pre: " + pre + ", home: " + home + ", post: " + post);
        }

        return path;
    }
}
