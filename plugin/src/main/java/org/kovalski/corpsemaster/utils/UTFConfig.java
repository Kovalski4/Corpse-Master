package org.kovalski.corpsemaster.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.kovalski.corpsemaster.Main;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class UTFConfig extends YamlConfiguration {

    public UTFConfig(File file) {

        if (!file.exists()) {
            Main instance = Main.getInstance();
            instance.saveDefaultConfig();
        }

        try {
            load(file);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load Configuration " + file.getName(), e);
        }
    }

    @Override
    public void save(File file) throws IOException {
        Validate.notNull(file, "File can't be null");
        Files.createParentDirs(file);
        String data = this.saveToString();

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            writer.write(data);
        }
    }

    @Override
    public String saveToString() {
        try {
            Field optionField = Reflections.getField(getClass(), "yamlOptions");
            Field representerField = Reflections.getField(getClass(), "yamlRepresenter");
            Field yamlField = Reflections.getField(getClass(), "yaml");

            if (optionField != null && representerField != null && yamlField != null) {
                optionField.setAccessible(true);
                representerField.setAccessible(true);
                yamlField.setAccessible(true);

                DumperOptions yamlOptions = (DumperOptions) optionField.get(this);
                Representer yamlRepresenter = (Representer) representerField.get(this);
                Yaml yaml = (Yaml) yamlField.get(this);
                DumperOptions.FlowStyle flow = DumperOptions.FlowStyle.BLOCK;

                yamlOptions.setIndent(this.options().indent());
                yamlOptions.setDefaultFlowStyle(flow);
                yamlOptions.setAllowUnicode(true);
                yamlRepresenter.setDefaultFlowStyle(flow);

                String header = this.buildHeader();
                String dump = yaml.dump(this.getValues(false));

                if (dump.equals("{}\n")) dump = "";
                return header + dump;
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error in converting Configuration to String", e);
        }
        return "Error: Cannot be saved to String";
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File can't be null");
        this.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
    }

    public static class Reflections {

        static final Field modifiers = getField(Field.class, "modifiers");

        public Reflections() {
            setAccessible(true, modifiers);
        }


        static void setAccessible(boolean state, Field... fields) {
            try {
                for (Field field : fields) {
                    field.setAccessible(state);
                    if (Modifier.isFinal(field.getModifiers())) {
                        field.setAccessible(true);
                        modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
                    }
                }
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.WARNING, "Could not set Fields accessible", ex);
            }
        }

        static Field getField(Class<?> clazz, String name) {
            Field field = null;
            for (Field f : getFields(clazz)) {
                if (f.getName().equals(name)) field = f;
            }
            return field;
        }

        static List<Field> getFields(Class<?> clazz) {
            List<Field> buf = new ArrayList<>();

            do {
                try {
                    Collections.addAll(buf, clazz.getDeclaredFields());
                } catch (Exception ignored) {
                }
            } while ((clazz = clazz.getSuperclass()) != null);

            return buf;
        }
    }
}