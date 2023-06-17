package brightspark.asynclocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Simple config handler for properties files with support for categories, comments, defaults and numerical range limits.
 *
 * @author bright_spark
 */
class SparkConfig {
	private static final String CATEGORY_START = "#>";
	private static final String CATEGORY_END = "<#";
	private static final String COMMENT = "#";
	private static final String COMMENT_REGEX = "#\\s*";
	private static final String INTERNAL_COMMENT = "#~";
	private static final String INTERNAL_COMMENT_REGEX = "#~\\s*";
	private static final String INTERNAL_COMMENT_SEPARATOR = ", ";
	private static final String INTERNAL_COMMENT_SEPARATOR_REGEX = "\\s*,\\s*";
	private static final String DEFAULT = "default:";
	private static final String MIN = "min:";
	private static final String MAX = "max:";
	private static final String EQUALS = "=";
	private static final String NEW_LINE = System.lineSeparator();
	private static final String NEW_LINE_REGEX = "\\n";
	private static final Set<Class<?>> VALID_TYPES = Set.of(
		boolean.class, Boolean.class,
		int.class, Integer.class,
		float.class, Float.class,
		String.class
	);

	private static final Logger LOG = LoggerFactory.getLogger(SparkConfig.class);

	/**
	 * Reads the config from the file at the path to the configClass.
	 *
	 * @param path        The location of the config file
	 * @param configClass The config class
	 */
	public static void read(Path path, Class<?> configClass) throws IOException, IllegalAccessException {
		if (Files.notExists(path)) return;
		Map<EntryKey, Entry> entries = readEntries(path);

		List<ConfigField> configFields = getConfigFields(configClass);
		for (ConfigField configField : configFields) {
			Field field = configField.field;
			String name = configField.getName();

			Entry entry = entries.get(new EntryKey(name, configField.getCategoryName()));
			if (entry != null) {
				Class<?> type = field.getType();
				Object newValue = parseValueToType(entry.value, type);

				String internalComment = entry.internalComment;
				if (!internalComment.isBlank()) {
					String defaultValue = null;
					String min = null;
					String max = null;
					String[] parts = internalComment.split(INTERNAL_COMMENT_SEPARATOR_REGEX);
					for (String part : parts) {
						int colonIndex = part.indexOf(":");
						String partValue = part.substring(colonIndex + 1).trim();
						if (part.startsWith(DEFAULT)) defaultValue = partValue;
						else if (part.startsWith(MIN)) min = partValue;
						else if (part.startsWith(MAX)) max = partValue;
						else LOG.warn("Invalid part of internal comment: " + part);
					}

					if (newValue == null && defaultValue != null)
						newValue = parseValueToType(defaultValue, type);
					if (isLessThan(min, newValue))
						throw new IllegalStateException("Config '" + name + " value '" + newValue + "' is less than the minimum of " + min);
					if (isGreaterThan(max, newValue))
						throw new IllegalStateException("Config '" + name + " value '" + newValue + "' is greater than the maximum of " + max);
				}

				field.set(null, newValue);
			} else
				LOG.warn("Config '{}' has no value in file!", name);
		}
	}

	private static Map<EntryKey, Entry> readEntries(Path path) throws IOException {
		String internalComment = "";
		List<String> comments = new ArrayList<>();
		Map<EntryKey, Entry> entries = new HashMap<>();
		String category = null;
		for (String line : Files.readAllLines(path)) {
			// Ignore blank links and category borders
			if (line.isBlank() || line.startsWith("##")) continue;

			if (line.startsWith(CATEGORY_START) && line.endsWith(CATEGORY_END)) {
				// Category line
				category = line.substring(2, line.length() - 2).trim();
			} else if (line.startsWith(INTERNAL_COMMENT)) {
				// Internal comment line - should be max 1 per config
				internalComment = line.replaceFirst(INTERNAL_COMMENT_REGEX, "");
			} else if (line.startsWith(COMMENT)) {
				// Comment line
				comments.add(line.replaceFirst(COMMENT_REGEX, ""));
			} else {
				String[] parts = line.split(EQUALS, 2);
				if (parts.length != 2) {
					LOG.warn("Invalid config line -> '" + line + "'");
					continue;
				}

				// Config line
				String comment = comments.isEmpty() ? "" : String.join(NEW_LINE, comments);
				String name = parts[0].trim();
				String value = parts[1].trim();
				entries.put(new EntryKey(name, category), new Entry(name, value, category, comment, internalComment));
			}
		}

		return entries;
	}

	/**
	 * Writes the config to the file at the path from the configClass.
	 *
	 * @param path        The location of the config file
	 * @param configClass The config class
	 */
	public static void write(Path path, Class<?> configClass) throws IOException, IllegalAccessException {
		StringBuilder sb = new StringBuilder();

		ConfigCategory lastConfigCategory = null;
		List<ConfigField> configFields = getConfigFields(configClass);
		for (ConfigField configField : configFields) {
			if (configField.category != lastConfigCategory) {
				lastConfigCategory = configField.category;
				sb.append(lastConfigCategory.getCategory());
			}

			StringBuilder comment = configField.getComment();
			if (comment != null) sb.append(comment);

			StringBuilder internalComment = configField.getInternalComment();
			if (internalComment != null) sb.append(internalComment);

			sb.append(configField.getConfig());
		}

		Files.writeString(path, sb.toString());
	}

	private static List<ConfigField> getConfigFields(Class<?> configClass) {
		LOG.debug("Getting config fields from class " + configClass.getName());
		List<ConfigField> configs = new ArrayList<>();

		// Uncategorized configs
		for (Field field : configClass.getDeclaredFields()) {
			ConfigField config = createConfigField(null, field);
			if (config != null) configs.add(config);
		}

		// Categorized configs
		for (Class<?> innerClass : configClass.getDeclaredClasses()) {
			if (!Modifier.isPrivate(innerClass.getModifiers())) {
				Category category = innerClass.isAnnotationPresent(Category.class)
					? innerClass.getAnnotation(Category.class) : null;
				ConfigCategory configCategory = new ConfigCategory(innerClass, category);
				for (Field field : innerClass.getDeclaredFields()) {
					ConfigField config = createConfigField(configCategory, field);
					if (config != null) configs.add(config);
				}
			}
		}

		LOG.debug("Got " + configs.size() + " configs");
		return configs;
	}

	private static ConfigField createConfigField(ConfigCategory configCategory, Field field) {
		int modifiers = field.getModifiers();
		if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
			Class<?> type = field.getType();
			if (!VALID_TYPES.contains(type)) {
				LOG.warn("The config '{}' value type {} is not supported!", field.getName(), type.getName());
				return null;
			}
			Config config = field.isAnnotationPresent(Config.class) ? field.getAnnotation(Config.class) : null;
			ConfigField configField = new ConfigField(field, config, configCategory);
			LOG.debug("Found config: {}", configField);
			return configField;
		}
		return null;
	}

	private static Object parseValueToType(String value, Class<?> type) {
		if (value.isBlank() && !type.isPrimitive())
			return null;
		if (value.isBlank() && (type == int.class || type == float.class))
			return 0;
		if (type == boolean.class || type == Boolean.class)
			return Boolean.parseBoolean(value);
		if (type == int.class || type == Integer.class)
			return Integer.parseInt(value);
		if (type == float.class || type == Float.class)
			return Float.parseFloat(value);
		if (type == String.class)
			return value;
		throw new IllegalStateException("Config value '" + value + "' cannot be parsed to type " + type.getName());
	}

	private static boolean isLessThan(String min, Object value) {
		if (min == null || !(value instanceof Number))
			return false;
		if (value instanceof Integer)
			return (Integer) value < Float.parseFloat(min);
		if (value instanceof Float)
			return (Float) value < Float.parseFloat(min);
		return false;
	}

	private static boolean isGreaterThan(String max, Object value) {
		if (max == null || !(value instanceof Number))
			return false;
		if (value instanceof Integer)
			return (Integer) value > Float.parseFloat(max);
		if (value instanceof Float)
			return (Float) value > Float.parseFloat(max);
		return false;
	}

	private SparkConfig() {}

	private record Entry(String name, String value, String category, String comment, String internalComment) {}

	private record EntryKey(String name, String category) {}

	private record ConfigField(Field field, Config config, ConfigCategory category) {
		String getName() {
			return config != null && !config.value().isBlank() ? config().value() : field.getName();
		}

		String getCategoryName() {
			return category != null ? category.getName() : null;
		}

		StringBuilder getComment() {
			if (config == null || config.comment().isBlank()) return null;

			String[] commentLines = config.comment().split(NEW_LINE_REGEX);
			StringBuilder sb = new StringBuilder();
			for (String commentLine : commentLines)
				sb.append(COMMENT).append(" ").append(commentLine).append(NEW_LINE);
			return sb;
		}

		StringBuilder getInternalComment() throws IllegalAccessException {
			// Since primitives can't be null, we'll assume any value for them is a default value
			boolean hasDefault = !field.getType().isPrimitive() && field.get(null) == null;
			boolean hasMin = config != null && config.min() != Float.MIN_NORMAL;
			boolean hasMax = config != null && config.max() != Float.MAX_VALUE;
			if (!hasDefault && !hasMin && !hasMax) return null;

			StringBuilder sb = new StringBuilder();
			sb.append(INTERNAL_COMMENT).append(" ");
			List<String> internalCommentParts = new ArrayList<>();
			if (hasDefault) internalCommentParts.add(DEFAULT + " " + field.get(null));
			if (hasMin) internalCommentParts.add(MIN + " " + config.min());
			if (hasMax) internalCommentParts.add(MAX + " " + config.max());
			sb.append(String.join(INTERNAL_COMMENT_SEPARATOR, internalCommentParts)).append(NEW_LINE);
			return sb;
		}

		StringBuilder getConfig() throws IllegalAccessException {
			String name = getName();
			Object rawValue = field.get(null);
			String value = rawValue == null ? "" : rawValue.toString();
			StringBuilder sb = new StringBuilder();
			sb.append(name).append(" ").append(EQUALS).append(" ").append(value).append(NEW_LINE).append(NEW_LINE);
			return sb;
		}
	}

	private record ConfigCategory(Class<?> clazz, Category category) {
		String getName() {
			return category != null && !category.value().isBlank() ? category().value() : clazz.getSimpleName();
		}

		StringBuilder getCategory() {
			String name = getName();
			String border = "#".repeat(name.length() + 6);
			StringBuilder sb = new StringBuilder();
			sb.append(border).append(NEW_LINE);
			sb.append(CATEGORY_START).append(" ").append(name).append(" ").append(CATEGORY_END).append(NEW_LINE);
			sb.append(border).append(NEW_LINE).append(NEW_LINE);
			return sb;
		}
	}

	/**
	 * Optional annotation to provide extra data for configs.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Config {
		/**
		 * Name override for the config in the file. By default, uses the field name.
		 */
		String value() default "";

		String comment() default "";

		float min() default Float.MIN_NORMAL;

		float max() default Float.MAX_VALUE;
	}

	/**
	 * Optional annotation to provide name override for categories.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Category {
		/**
		 * Name override for the category in the file. By default, uses the class name.
		 */
		String value() default "";
	}
}
