package com;

import com.rocui.util.file.Jile;
import com.rocui.util.file.JileEach;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import org.markdown4j.Markdown4jProcessor;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("-option [in path] [out path] [[template Path]]");
        } else if (args.length == 2) {
            parse(args[0], args[1], null);
        } else {
            parse(args[0], args[1], args[2]);
        }
    }

    private static void parse(String in, String out, String tempath) throws Exception {
        final String three = tempath;
        if (Jile.with(in).file().isDirectory()) {
            final String one = in.endsWith("\\") ? in.substring(0, in.length() - 1) : in;
            final String two = out.endsWith("\\") ? out.substring(0, out.length() - 1) : out;
            Jile.with(in).browse(new JileEach() {
                @Override
                public boolean each(Jile file) throws Exception {
                    if (file.file().isFile()) {
                        if (file.file().getName().endsWith(".md")) {
                            String path = file.file().getAbsolutePath();
                            String newpath = two + path.substring(one.length(), path.length() - 3) + ".html";
                            String a = file.read();
                            a = new Markdown4jProcessor().process(a);
                            HashMap<String, Object> vars = new HashMap<>();
                            vars.put("title", file.file().getName().split("\\.")[0]);
                            vars.put("content", a);
                            Jile.with(newpath).write(templete(three, vars));
                            System.out.println("[parse] " + path + " ---> " + newpath);
                        }
                    }
                    return false;
                }
            });
        } else {
            if (Jile.with(in).file().getName().endsWith(".md")) {
                String a = Jile.with(in).read();
                a = new Markdown4jProcessor().process(a);
                Jile.with(out).write(a);
                HashMap<String, Object> vars = new HashMap<>();
                vars.put("title", Jile.with(in).file().getName().split("\\.")[0]);
                vars.put("content", a);
                Jile.with(out).write(templete(three, vars));
            }
        }
    }

    private static String templete(String tempath, HashMap<String, Object> vars) throws Exception {
        if (null != tempath) {
            System.out.println("[tempath] " + tempath);
        } else {
            System.out.println("[template] default");
        }
        if (null != tempath && !tempath.equals("")) {
            return parseTemplateString(Jile.with(tempath).read(), vars);
        } else {
            String temp = StreamToString(Main.class.getClassLoader().getResourceAsStream("md.tpl"));
            return parseTemplateString(temp, vars);
        }
    }

    private static String parseTemplateString(String tempString, HashMap<String, Object> vars) throws Exception {
        Configuration cfg = new Configuration();
        StringTemplateLoader loader = new StringTemplateLoader();
        loader.putTemplate("md", tempString);
        cfg.setTemplateLoader(loader);
        cfg.setDefaultEncoding("UTF-8");
        Template template = cfg.getTemplate("md");
        StringWriter writer = new StringWriter();
        template.process(vars, writer);
        return writer.toString();
    }

    public static String StreamToString(InputStream in) throws Exception {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}
