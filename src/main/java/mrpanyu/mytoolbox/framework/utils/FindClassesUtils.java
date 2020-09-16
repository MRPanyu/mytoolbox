package mrpanyu.mytoolbox.framework.utils;

import java.io.File;
import java.io.FileFilter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FindClassesUtils {

	public static Set<Class<?>> findClassesByBaseClass(String basePackage, boolean recursive, Class<?> baseClass) {
		Set<Class<?>> foundClasses = new LinkedHashSet<Class<?>>();
		Set<Class<?>> allClasses = findClasses(basePackage, recursive);
		for (Class<?> c : allClasses) {
			if (baseClass.isAssignableFrom(c)) {
				foundClasses.add(c);
			}
		}
		return foundClasses;
	}

	public static Set<Class<?>> findClasses(String basePackage, boolean recursive) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = basePackage.replace('.', '/');
		Set<Class<?>> classes = new TreeSet<Class<?>>(new Comparator<Class<?>>() {
			public int compare(Class<?> o1, Class<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		try {
			Enumeration<URL> urls = cl.getResources(path);
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					String dirPath = URLDecoder.decode(url.getPath(), "UTF-8");
					findClassesFile(new File(dirPath), basePackage, recursive, classes);
				} else if ("jar".equals(protocol)) {
					findClassesJar(url, basePackage, recursive, classes);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return classes;
	}

	private static void findClassesFile(File dir, String packageName, boolean recursive, Collection<Class<?>> classes)
			throws Exception {
		File[] children = dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".class");
			}
		});
		for (File child : children) {
			if (child.isDirectory() && recursive) {
				String childPackageName = packageName + "." + child.getName();
				findClassesFile(child, childPackageName, recursive, classes);
			}
			if (child.isFile()) {
				String fname = child.getName();
				String classShortName = fname.substring(0, fname.length() - 6);
				String className = packageName + "." + classShortName;
				addForName(className, classes);
			}
		}
	}

	private static void findClassesJar(URL url, String packageName, boolean recursive, Collection<Class<?>> classes)
			throws Exception {
		JarURLConnection conn = (JarURLConnection) url.openConnection();
		JarFile jarFile = conn.getJarFile();
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (entryName.endsWith(".class") && !entry.isDirectory()) {
				String entryPackageName = "";
				if (entryName.indexOf("/") > 0) {
					entryPackageName = entryName.substring(0, entryName.lastIndexOf('/')).replace('/', '.');
				}
				if ((recursive && entryPackageName.startsWith(packageName)) || entryPackageName.equals(packageName)) {
					String classShortName = entryName.substring(entryName.lastIndexOf('/') + 1, entryName.length() - 6);
					String className = entryPackageName + "." + classShortName;
					addForName(className, classes);
				}
			}
		}
	}

	private static void addForName(String className, Collection<Class<?>> classes) {
		try {
			Class<?> cls = Class.forName(className);
			classes.add(cls);
		} catch (Throwable e) {
		}
	}

	public static void main(String[] args) {
		Set<Class<?>> classes = findClasses("org.apache.commons.lang3", true);
		for (Class<?> cls : classes) {
			System.out.println(cls.getName());
		}
	}

}
