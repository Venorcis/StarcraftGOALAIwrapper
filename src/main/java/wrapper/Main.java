
package wrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import goal.preferences.CorePreferences;
import goal.tools.Run;
import goal.tools.SingleRun;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.MASValidator;
import languageTools.program.mas.MASProgram;
import swiprolog.SwiInstaller;

public class Main {
	public static void main(final String[] args) {
		// Get the directory the JAR is located in (assumed to be bwapi-data/AI)
		Path working;
		try {
			working = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// NEW logging
		final Path write = working.getParent().resolve("write");
		try {
			System.setOut(new PrintStream(new FileOutputStream(write.resolve("stdout.log").toFile())));
			System.setErr(new PrintStream(new FileOutputStream(write.resolve("stderr.log").toFile())));
		} catch (final Exception e) {
			// e.printStackTrace();
		}
		// Make sure SWI is extracted to a specific directory when running
		SwiInstaller.overrideDirectory(working.resolve("swi").toString());
		// Get the agent code ZIP resource and extract it to its own directory
		Path agentdir;
		try {
			final String name = "Bot";
			agentdir = working.resolve(name);
			unzip(name + ".zip", agentdir);
		} catch (final Exception e) {
			e.printStackTrace();
			return;
		}
		// Copy the connector.jar resource to the agent code directory
		final Path env = agentdir.resolve("connector.jar");
		try {
			final InputStream source = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(env.getFileName().toString());
			if (source != null) {
				Files.copy(source, env);
			}
		} catch (final Exception e) {
			// e.printStackTrace();
		}

		// Check if we have 1 mas2g and if it is error-free;
		// if it is, then fix its init parameters, and run it :)
		final SortedSet<File> mas2g = Run.getMASFiles(agentdir.toFile(), true);
		if (mas2g.size() == 1) {
			final MASProgram mas = parse(mas2g.iterator().next(), env.toFile());
			if (mas != null) {
				try {
					CorePreferences.setRemoveKilledAgent(true);
					if (args.length == 0) { // SSCAIT (single-core)
						CorePreferences.setSequentialExecution(true);
					}
					final SingleRun run = new SingleRun(mas);
					run.run(true);
				} catch (final GOALRunFailedException e) {
					e.printStackTrace();
				} finally {
					System.exit(0);
				}
			}
		} else {
			System.err.println("Found " + mas2g.size() + " mas2g files in " + agentdir);
		}
	}

	private static File unzip(final String zipfilename, final Path path) throws IOException {
		final File base = path.toFile();
		if (!base.exists()) {
			base.mkdirs();
			final InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(zipfilename);
			final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				final File file = new File(base, entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					final File parent = file.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
					byte[] buffer = new byte[4096];
					int count = -1;
					while ((count = zis.read(buffer)) > 0) {
						out.write(buffer, 0, count);
					}
					out.close();
				}
				zis.closeEntry();
			}
			zis.close();
			fis.close();
		}
		return base;
	}

	private static MASProgram parse(final File mas, final File env) {
		final FileRegistry registry = new FileRegistry();
		final MASValidator validator = new MASValidator(mas.getPath(), registry);
		validator.validate();
		if (validator.getProgram() != null) {
			validator.process();
			final MASProgram mas2g = validator.getProgram();
			mas2g.setEnvironmentfile(env);
			final Object mapinfo = mas2g.getInitParameters().get("draw_mapinfo");
			final Object unitinfo = mas2g.getInitParameters().get("draw_unitinfo");
			final Object managers = mas2g.getInitParameters().get("managers");
			final Object percepts = mas2g.getInitParameters().get("percepts");
			mas2g.resetInitParameters();
			mas2g.addInitParameter("auto_menu", "OFF");
			mas2g.addInitParameter("debug", "false");
			mas2g.addInitParameter("invulnerable", "false");
			mas2g.addInitParameter("game_speed", 50);
			mas2g.addInitParameter("own_race", "random");
			mas2g.addInitParameter("starcraft_location", "");
			if ("true".equals(mapinfo) || "false".equals(mapinfo)) {
				mas2g.addInitParameter("draw_mapinfo", mapinfo);
			}
			if ("true".equals(unitinfo) || "false".equals(unitinfo)) {
				mas2g.addInitParameter("draw_unitinfo", unitinfo);
			}
			if (managers != null) {
				mas2g.addInitParameter("managers", managers);
			}
			if (percepts != null) {
				mas2g.addInitParameter("percepts", percepts);
			}
			return mas2g;
		} else {
			System.err.println(mas.getName() + " invalid: " + registry.getAllErrors());
			return null;
		}
	}
}
